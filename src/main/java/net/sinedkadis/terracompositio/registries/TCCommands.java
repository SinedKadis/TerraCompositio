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
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMember;
import net.sinedkadis.terracompositio.api.networks.ecf.ECFNetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.ecf.IECFHandler;
import net.sinedkadis.terracompositio.network.TCPackets;
import net.sinedkadis.terracompositio.network.packets.S2CPlayerEcfContainerSync;

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
                                Commands.literal("print-ecf-data")
                                        .executes(TCCommands::printCFEData)
                        )
                        .then(
                                Commands.argument("print-ecf-data", EntityArgument.entity()))
                                        .executes(ctx -> {
                                            Entity entity = EntityArgument
                                                    .getEntity(ctx,"ecf network member entity");
                                            return TCCommands.printCFEData(ctx,entity);
                                        })
                        .then(
                                Commands.literal("clear-ecf-data")
                                        .executes(TCCommands::clearECFData)
                        )
                        .then(
                                Commands.argument("clear-ecf-data", EntityArgument.entity()))
                        .executes(ctx -> {
                            Entity entity = EntityArgument
                                    .getEntity(ctx,"ecf network member entity");
                            return TCCommands.clearECFData(ctx, entity);
                        })
                        .then(
                                Commands.literal("clear-all-queues")
                                        .executes(TCCommands::clearAllQueues)
                        )
                        );
    }

    private static int clearAllQueues(CommandContext<CommandSourceStack> ctx) {
        TerraCompositioAPI.instance().getECFNetworkInstance().getAllECFNetworkMembers(ctx.getSource().getLevel()).stream()
                .map(ECFNetworkMember::getMainHandler)
                .forEach(iEcfHandler -> iEcfHandler.setQueued(0));
        return 0;
    }

    private static int clearECFData(CommandContext<CommandSourceStack> ctx, Entity... entities) {
        CommandSourceStack source = ctx.getSource();
        ECFNetworkMemberEntity memberEntity;
        if (Arrays.stream(entities).toList().isEmpty()) {
            ServerPlayer player = source.getPlayer();
            if (player == null) {
                source.sendFailure(Component.literal("No entities were provided"));
                return 0;
            }
            memberEntity = (ECFNetworkMemberEntity) player;
        } else {
            Entity entity = entities[0];
            if (entity instanceof ECFNetworkMemberEntity memberEntity1)
                memberEntity = memberEntity1;
            else {
                source.sendFailure(Component.literal("Entity has to be ECFNetworkMemberEntity"));
                return 0;
            }
        }
        IECFHandler mainHandler = memberEntity.getMainHandler();
        mainHandler.clear();
        if (memberEntity instanceof ServerPlayer serverPlayer) {
            TCPackets.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new S2CPlayerEcfContainerSync(mainHandler.getECF()));
        }

        NonNullSupplier<Exception> exception = Exception::new;
        memberEntity.getEntity().getArmorSlots().forEach(itemStack -> {
            try {
                itemStack.getCapability(TCCapabilities.ECF).orElseThrow(exception).clear();
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
        ECFNetworkMemberEntity memberEntity;
        if (Arrays.stream(entities).toList().isEmpty()) {
            ServerPlayer player = source.getPlayer();
            if (player == null) {
                source.sendFailure(Component.literal("No entities were provided"));
                return 0;
            }
            memberEntity = (ECFNetworkMemberEntity) player;
        } else {
            Entity entity = entities[0];
            if (entity instanceof ECFNetworkMemberEntity memberEntity1)
                memberEntity = memberEntity1;
            else {
                source.sendFailure(Component.literal("Entity has to be ECFNetworkMemberEntity"));
                return 0;
            }
        }

        StringBuilder message = new StringBuilder();

        IECFHandler mainHandler = memberEntity.getMainHandler();
        message.append(mainHandler.toString()).append("\n\n");

        source.sendSuccess(() ->
                        Component.literal(message.toString()),
                true);
        return Command.SINGLE_SUCCESS;
    }
}