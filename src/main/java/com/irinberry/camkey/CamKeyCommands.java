package com.irinberry.camkey;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

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
}
