package com.irinberry.camkey;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.List;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Brigadier registration and user-facing feedback for {@code /camkey}.
 */
public final class CamKeyCommands {
    private CamKeyCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("camkey")
                        .requires(source -> source.hasPermission(Commands.LEVEL_ALL))
                        .then(Commands.literal("add")
                                .then(Commands.argument("sequenceName", StringArgumentType.word())
                                        .executes(CamKeyCommands::add)))
                        .then(Commands.literal("play")
                                .then(Commands.argument("sequenceName", StringArgumentType.word())
                                        .then(Commands.argument("durationSeconds", DoubleArgumentType.doubleArg(0.05))
                                                .executes(CamKeyCommands::play))))
        );
    }

    private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        String sequenceName = StringArgumentType.getString(context, "sequenceName");
        ResourceLocation dimension = CameraCapture.dimensionOf(player);
        CameraSequenceSavedData data = CameraSequenceSavedData.get(player.server);

        if (!data.addKeyframe(sequenceName, CameraCapture.from(player), dimension)) {
            ResourceLocation sequenceDimension = data.getSequence(sequenceName)
                    .map(CameraSequence::dimension)
                    .orElse(null);
            source.sendFailure(Component.literal(
                    "Sequence '" + sequenceName + "' belongs to " + sequenceDimension
                            + " (you are in " + dimension + ")"
            ));
            return 0;
        }

        int count = data.getSequence(sequenceName).map(CameraSequence::size).orElse(0);
        source.sendSuccess(
                () -> Component.literal("Added keyframe to '" + sequenceName + "' (" + count + " keyframe"
                        + (count == 1 ? "" : "s") + ")"),
                true
        );
        return count;
    }

    private static int play(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        String sequenceName = StringArgumentType.getString(context, "sequenceName");
        double durationSeconds = DoubleArgumentType.getDouble(context, "durationSeconds");
        CameraSequenceSavedData data = CameraSequenceSavedData.get(player.server);
        CameraSequence sequence = data.getSequence(sequenceName).orElse(null);

        if (sequence == null) {
            source.sendFailure(Component.literal("Unknown sequence '" + sequenceName + "'"));
            return 0;
        }
        if (sequence.size() < 2) {
            source.sendFailure(Component.literal(
                    "Sequence '" + sequenceName + "' needs at least 2 keyframes (has " + sequence.size() + ")"
            ));
            return 0;
        }

        ResourceLocation playerDimension = CameraCapture.dimensionOf(player);
        if (sequence.dimension() == null || !sequence.dimension().equals(playerDimension)) {
            source.sendFailure(Component.literal(
                    "Sequence '" + sequenceName + "' belongs to " + sequence.dimension()
                            + " (you are in " + playerDimension + ")"
            ));
            return 0;
        }

        if (!CameraPlaybackStarter.startLocal(sequenceName, List.copyOf(sequence.keyframes()), durationSeconds)) {
            source.sendFailure(Component.literal("Playback is only available in local single-player."));
            return 0;
        }
        source.sendSuccess(
                () -> Component.literal("Playing '" + sequenceName + "' over " + durationSeconds + " seconds"),
                true
        );
        return 1;
    }
}
