package net.sinedkadis.terracompositio.registries;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.TerraCompositioAPI;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMember;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;
import net.sinedkadis.terracompositio.network.TCPackets;
import net.sinedkadis.terracompositio.network.packets.S2CPlayerCfeContainerSync;

import java.util.Arrays;

@Mod.EventBusSubscriber(modid = TerraCompositio.MOD_ID)
public class TCCommands {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("terracompositio")
                        .requires(source -> source.hasPermission(2)) // op only
                        .then(
                                Commands.literal("print-cfe-data")
                                        .executes(TCCommands::printCFEData)
                        )
                        .then(
                                Commands.argument("print-cfe-data", EntityArgument.entity()))
                                        .executes(ctx -> {
                                            Entity entity = EntityArgument
                                                    .getEntity(ctx,"cfe network member entity");
                                            return TCCommands.printCFEData(ctx,entity);
                                        })
                        .then(
                                Commands.literal("clear-cfe-data")
                                        .executes(TCCommands::clearCFEData)
                        )
                        .then(
                                Commands.argument("clear-cfe-data", EntityArgument.entity()))
                        .executes(ctx -> {
                            Entity entity = EntityArgument
                                    .getEntity(ctx,"cfe network member entity");
                            return TCCommands.clearCFEData(ctx,entity);
                        })
                        .then(
                                Commands.literal("clear-all-queues")
                                        .executes(TCCommands::clearAllQueues)
                        )
                        );
    }

    private static int clearAllQueues(CommandContext<CommandSourceStack> ctx) {
        TerraCompositioAPI.instance().getCFENetworkInstance().getAllCFENetworkMembers(ctx.getSource().getLevel()).stream()
                .map(CFENetworkMember::getMainHandler)
                .forEach(icfeHandler -> icfeHandler.setQueued(0));
        return 0;
    }

    private static int clearCFEData(CommandContext<CommandSourceStack> ctx, Entity... entities) {
        CommandSourceStack source = ctx.getSource();
        CFENetworkMemberEntity memberEntity;
        if (Arrays.stream(entities).toList().isEmpty()) {
            ServerPlayer player = source.getPlayer();
            if (player == null) {
                source.sendFailure(Component.literal("No entities were provided"));
                return 0;
            }
            memberEntity = (CFENetworkMemberEntity) player;
        } else {
            Entity entity = entities[0];
            if (entity instanceof CFENetworkMemberEntity memberEntity1)
                memberEntity = memberEntity1;
            else {
                source.sendFailure(Component.literal("Entity has to be CFENetworkMemberEntity"));
                return 0;
            }
        }
        ICFEHandler mainHandler = memberEntity.getMainHandler();
        mainHandler.clear();
        if (memberEntity instanceof ServerPlayer serverPlayer) {
            TCPackets.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new S2CPlayerCfeContainerSync(mainHandler.getCFE()));
        }

        NonNullSupplier<Exception> exception = Exception::new;
        memberEntity.getEntity().getArmorSlots().forEach(itemStack -> {
            try {
                itemStack.getCapability(TCCapabilities.CFE).orElseThrow(exception).clear();
            } catch (Exception ignored) {

            }
        });

        source.sendSuccess(() ->
                        Component.literal("Data cleared"),
                true);
        return Command.SINGLE_SUCCESS;
    }

    private static int printCFEData(CommandContext<CommandSourceStack> ctx, Entity... entities) {
        CommandSourceStack source = ctx.getSource();
        CFENetworkMemberEntity memberEntity;
        if (Arrays.stream(entities).toList().isEmpty()) {
            ServerPlayer player = source.getPlayer();
            if (player == null) {
                source.sendFailure(Component.literal("No entities were provided"));
                return 0;
            }
            memberEntity = (CFENetworkMemberEntity) player;
        } else {
            Entity entity = entities[0];
            if (entity instanceof CFENetworkMemberEntity memberEntity1)
                memberEntity = memberEntity1;
            else {
                source.sendFailure(Component.literal("Entity has to be CFENetworkMemberEntity"));
                return 0;
            }
        }

        StringBuilder message = new StringBuilder();

        ICFEHandler mainHandler = memberEntity.getMainHandler();
        message.append(mainHandler.toString()).append("\n\n");

        source.sendSuccess(() ->
                        Component.literal(message.toString()),
                true);
        return Command.SINGLE_SUCCESS;
    }
}