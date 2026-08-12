package com.irinberry.camkey;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client-only entrypoint. Registers local cinematic playback for single-player.
 */
@Mod(value = CameraKeyframes.MODID, dist = Dist.CLIENT)
public class CameraKeyframesClient {
    public CameraKeyframesClient() {
        CameraPlaybackStarter.register(CameraClientPlayback.get()::start);
        NeoForge.EVENT_BUS.register(CameraClientPlayback.get());
    }
}
