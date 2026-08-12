package com.irinberry.camkey;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

/**
 * Client-only entrypoint. Kept so the mod still has a physical-client class;
 * no config screen is registered.
 */
@Mod(value = CameraKeyframes.MODID, dist = Dist.CLIENT)
public class CameraKeyframesClient {
    public CameraKeyframesClient() {
    }
}
