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
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sinedkadis.terracompositio.TerraCompositio;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.api.networks.cfe.CFENetworkMemberEntity;
import net.sinedkadis.terracompositio.api.networks.cfe.ICFEHandler;

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
                        );
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

        NonNullSupplier<Exception> exception = Exception::new;
        memberEntity.getEntity().getArmorSlots().forEach(itemStack -> {
            try {
                switch (LivingEntity.getEquipmentSlotForItem(itemStack)) {
                    case HEAD -> message.append("Head:\n ").append(itemStack.getCapability(TCCapabilities.CFE).orElseThrow(exception)).append("\n\n");
                    case CHEST -> message.append("Chest:\n ").append(itemStack.getCapability(TCCapabilities.CFE).orElseThrow(exception)).append("\n\n");
                    case LEGS -> message.append("Legs:\n ").append(itemStack.getCapability(TCCapabilities.CFE).orElseThrow(exception)).append("\n\n");
                    case FEET -> message.append("Feet:\n ").append(itemStack.getCapability(TCCapabilities.CFE).orElseThrow(exception)).append("\n\n");
                }
            } catch (Exception ignored) {

            }
        });

        source.sendSuccess(() ->
                        Component.literal(message.toString()),
                true);
        return Command.SINGLE_SUCCESS;
    }
}