package com.irinberry.camkey;

import java.util.List;

/**
 * Common-to-client handoff for local single-player playback.
 * The client entrypoint registers the handler; dedicated servers leave it unset.
 */
public final class CameraPlaybackStarter {
    @FunctionalInterface
    public interface Handler {
        void start(String sequenceName, List<CameraKeyframe> keyframes, double durationSeconds);
    }

    private static volatile Handler handler;

    private CameraPlaybackStarter() {
    }

    public static void register(Handler clientHandler) {
        handler = clientHandler;
    }

    public static boolean startLocal(String sequenceName, List<CameraKeyframe> keyframes, double durationSeconds) {
        Handler clientHandler = handler;
        if (clientHandler == null) {
            return false;
        }
        clientHandler.start(sequenceName, keyframes, durationSeconds);
        return true;
    }
}
