package com.irinberry.camkey;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Reads the pose (and dimension identity) used when appending a keyframe.
 */
public final class CameraCapture {
    private CameraCapture() {
    }

    public static CameraKeyframe from(ServerPlayer player) {
        return new CameraKeyframe(
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot()
        );
    }

    /**
     * Dimension identity stored on the sequence, not on each keyframe.
     */
    public static ResourceLocation dimensionOf(ServerPlayer player) {
        return player.level().dimension().location();
    }
}
