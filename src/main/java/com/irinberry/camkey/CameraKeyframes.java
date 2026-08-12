package com.irinberry.camkey;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Common mod entrypoint. Game-bus listeners for CamKey commands and playback
 * will be registered here in later steps.
 */
@Mod(CameraKeyframes.MODID)
public class CameraKeyframes {
    public static final String MODID = "camkey";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CameraKeyframes() {
        // Game bus: required later for RegisterCommandsEvent and playback ticks.
        NeoForge.EVENT_BUS.register(this);
    }
}
