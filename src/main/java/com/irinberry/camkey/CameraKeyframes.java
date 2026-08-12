package com.irinberry.camkey;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Common mod entrypoint. Game-bus listeners stay here; command parsing lives in
 * {@link CamKeyCommands}.
 */
@Mod(CameraKeyframes.MODID)
public class CameraKeyframes {
    public static final String MODID = "camkey";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CameraKeyframes() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CamKeyCommands.register(event.getDispatcher());
    }
}
