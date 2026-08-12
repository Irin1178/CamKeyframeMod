package com.irinberry.camkey;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * Client-only cinematic session. The mixin reads {@link #currentPose(float)} each frame.
 */
public final class CameraClientPlayback {
    private static final CameraClientPlayback INSTANCE = new CameraClientPlayback();

    private volatile Session session;

    private CameraClientPlayback() {
    }

    public static CameraClientPlayback get() {
        return INSTANCE;
    }

    public void start(String sequenceName, List<CameraKeyframe> keyframes, double durationSeconds) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            int durationTicks = Math.max(1, (int) Math.round(durationSeconds * 20.0));
            session = new Session(sequenceName, List.copyOf(keyframes), durationTicks);
        });
    }

    /**
     * @return interpolated feet pose for this frame, or {@code null} if inactive
     */
    public static CameraKeyframe currentPose(float partialTick) {
        return INSTANCE.pose(partialTick);
    }

    private CameraKeyframe pose(float partialTick) {
        Session active = session;
        if (active == null) {
            return null;
        }

        double progress = (active.elapsedTicks + partialTick) / (double) active.durationTicks;
        if (progress >= 1.0) {
            CameraKeyframe last = active.keyframes.get(active.keyframes.size() - 1);
            finish(active);
            return last;
        }

        int lastIndex = active.keyframes.size() - 1;
        double scaled = progress * lastIndex;
        int index = Math.min((int) Math.floor(scaled), lastIndex - 1);
        float localT = (float) (scaled - index);
        return active.keyframes.get(index).interpolate(active.keyframes.get(index + 1), localT);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        Session active = session;
        if (active != null) {
            active.elapsedTicks++;
        }
    }

    @SubscribeEvent
    public void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        stop(false);
    }

    @SubscribeEvent
    public void onClone(ClientPlayerNetworkEvent.Clone event) {
        stop(false);
    }

    @SubscribeEvent
    public void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        stop(false);
    }

    @SubscribeEvent
    public void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            stop(false);
        }
    }

    private void finish(Session completed) {
        if (session != completed) {
            return;
        }
        stop(true);
    }

    private void stop(boolean completed) {
        Session ended = session;
        session = null;
        if (ended == null) {
            return;
        }
        if (completed) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.execute(() -> {
                if (minecraft.player != null) {
                    minecraft.player.displayClientMessage(
                            Component.literal("Finished sequence '" + ended.sequenceName + "'"),
                            false
                    );
                }
            });
        }
    }

    private static final class Session {
        private final String sequenceName;
        private final List<CameraKeyframe> keyframes;
        private final int durationTicks;
        private volatile int elapsedTicks;

        private Session(String sequenceName, List<CameraKeyframe> keyframes, int durationTicks) {
            this.sequenceName = sequenceName;
            this.keyframes = keyframes;
            this.durationTicks = durationTicks;
        }
    }
}
