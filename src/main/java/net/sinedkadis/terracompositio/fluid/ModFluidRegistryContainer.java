package net.sinedkadis.terracompositio.fluid;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import lombok.Getter;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockAndTintGetter;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundAction;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.extensions.IForgeBucketPickup;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.registries.RegistryObject;
import net.sinedkadis.terracompositio.block.ModBlocks;
import net.sinedkadis.terracompositio.block.custom.FlowCauldronBlock;
import net.sinedkadis.terracompositio.item.ModItems;
import net.sinedkadis.terracompositio.item.custom.ModBucketItem;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModFluidRegistryContainer implements IForgeBucketPickup{
    public final RegistryObject<FluidType> type;
    public final FluidType.Properties typeProperties;
    public final RegistryObject<LiquidBlock> block;
    public final RegistryObject<Item> bucket;
    @Getter
    private ForgeFlowingFluid.Properties properties;
    public final RegistryObject<ForgeFlowingFluid.Source> source;
    public final RegistryObject<ForgeFlowingFluid.Flowing> flowing;
    private static final Logger LOGGER = LogUtils.getLogger();

    public ModFluidRegistryContainer(String name, FluidType.Properties typeProperties,
                                  Supplier<IClientFluidTypeExtensions> clientExtensions, @Nullable AdditionalProperties additionalProperties,
                                  BlockBehaviour.Properties blockProperties, Item.Properties itemProperties) {
        this.typeProperties = typeProperties;
        this.type = ModFluids.FLUID_TYPES.register(name, () -> new FluidType(this.typeProperties) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(clientExtensions.get());
            }

            @Override
            public @Nullable SoundEvent getSound(SoundAction action) {
                //LOGGER.debug("getSound called");
                if (action == SoundActions.BUCKET_EMPTY){
                    //LOGGER.debug("getSound called at empty bucket action");
                    return SoundEvents.BUCKET_EMPTY;
                }
                return super.getSound(action);
            }
        });

        this.source = ModFluids.FLUIDS.register(name + "_source",
                () -> new ForgeFlowingFluid.Source(this.properties));
        this.flowing = ModFluids.FLUIDS.register(name + "_flowing",
                () -> new ForgeFlowingFluid.Flowing(this.properties));

        this.properties = new ForgeFlowingFluid.Properties(this.type, this.source, this.flowing);
        if (additionalProperties != null) {
            this.properties.explosionResistance(additionalProperties.explosionResistance)
                    .levelDecreasePerBlock(additionalProperties.levelDecreasePerBlock)
                    .slopeFindDistance(additionalProperties.slopeFindDistance).tickRate(additionalProperties.tickRate);
        }

        this.block = ModBlocks.BLOCKS.register(name, () -> new LiquidBlock(this.source, blockProperties.noLootTable()));
        this.properties.block(this.block);

        this.bucket = ModItems.ITEMS.register(name + "_bucket", () -> new BucketItem(this.source, itemProperties){
            @Override
            public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
                BlockPos pPos = context.getClickedPos();
                if (context.getItemInHand().getItem() == ModFluids.FLOW_FLUID.bucket.get()){
                    if (context.getLevel().getBlockState(pPos)== Blocks.CAULDRON.defaultBlockState()){
                        context.getLevel().setBlock(pPos,ModBlocks.FLOW_CAULDRON.get().defaultBlockState().setValue(FlowCauldronBlock.LEVEL,3),1);
                        context.getPlayer().setItemInHand(context.getHand(),new ItemStack(Items.BUCKET));
                        context.getPlayer().playSound(SoundEvents.BUCKET_EMPTY); //TODO: fix sound when clicked with empty bucket on flow
                        return InteractionResult.SUCCESS;
                    }
                }
                if (context.getItemInHand().getItem() == ModFluids.BIRCH_JUICE_FLUID.bucket.get()){
                    if (context.getLevel().getBlockState(pPos)== Blocks.CAULDRON.defaultBlockState()){
                        context.getLevel().setBlock(pPos,ModBlocks.BIRCH_JUICE_CAULDRON.get().defaultBlockState().setValue(FlowCauldronBlock.LEVEL,3),1);
                        context.getPlayer().setItemInHand(context.getHand(),new ItemStack(Items.BUCKET));
                        context.getPlayer().playSound(SoundEvents.BUCKET_EMPTY);
                        return InteractionResult.SUCCESS;
                    }
                }

                return super.onItemUseFirst(stack, context);
            }
            @Override
            public UseAnim getUseAnimation(ItemStack pStack) {
                if (pStack.is(ModFluids.BIRCH_JUICE_FLUID.bucket.get())) {
                    return UseAnim.DRINK;
                }
                return super.getUseAnimation(pStack);
            }

            @Override
            public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
                BlockHitResult blockhitresult = getPlayerPOVHitResult(pLevel, pPlayer, super.getFluid() == Fluids.EMPTY ? net.minecraft.world.level.ClipContext.Fluid.SOURCE_ONLY : net.minecraft.world.level.ClipContext.Fluid.NONE);
                if (blockhitresult.getType() == HitResult.Type.MISS) {
                    return ItemUtils.startUsingInstantly(pLevel, pPlayer, pHand);
                }
                return super.use(pLevel, pPlayer, pHand);
            }

            @Override
            public int getUseDuration(ItemStack pStack) {
                return pStack.is(ModFluids.BIRCH_JUICE_FLUID.bucket.get()) ? 32 : 1;
            }

            @Override
            public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
                super.finishUsingItem(pStack, pLevel, pLivingEntity);
                if (pStack.is(ModFluids.BIRCH_JUICE_FLUID.bucket.get())) {
                    Player player = pLivingEntity instanceof Player ? (Player) pLivingEntity : null;
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer) player, pStack);
                    }
                    if (player != null) {
                        player.awardStat(Stats.ITEM_USED.get(this));
                        if (!player.getAbilities().instabuild) {
                            pStack.shrink(1);
                        }
                        player.addEffect(new MobEffectInstance(MobEffects.SATURATION,5,0,false,false));
                    }
                    if (player == null || !player.getAbilities().instabuild) {
                        if (pStack.isEmpty()) {
                            return new ItemStack(Items.BUCKET);
                        }

                        if (player != null) {
                            player.getInventory().add(new ItemStack(Items.BUCKET));
                        }
                    }

                    pLivingEntity.gameEvent(GameEvent.DRINK);
                }
                return pStack;
            }
            public ICapabilityProvider initCapabilities(@NotNull ItemStack stack, @javax.annotation.Nullable CompoundTag nbt) {
                return new FluidBucketWrapper(stack);
            }
        });
        this.properties.bucket(this.bucket);
    }

    public ModFluidRegistryContainer(String name, FluidType.Properties typeProperties,
                                  Supplier<IClientFluidTypeExtensions> clientExtensions, BlockBehaviour.Properties blockProperties,
                                  Item.Properties itemProperties) {
        this(name, typeProperties, clientExtensions, null, blockProperties, itemProperties);
    }

    public static IClientFluidTypeExtensions createExtension(ClientExtensions extensions) {
        return new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getFlowingTexture() {
                return extensions.flowing;
            }

            @Nullable
            @Override
            public ResourceLocation getOverlayTexture() {
                return extensions.overlay;
            }

            @Override
            public ResourceLocation getRenderOverlayTexture(Minecraft minecraft) {
                return extensions.renderOverlay;
            }

            @Override
            public ResourceLocation getStillTexture() {
                return extensions.still;
            }

            @Override
            public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                return extensions.tintFunction == null ? 0xFFFFFFFF : extensions.tintFunction.apply(state, getter, pos);
            }

            @Override
            public @NotNull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level,
                                                    int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                return extensions.fogColor == null
                        ? IClientFluidTypeExtensions.super.modifyFogColor(camera, partialTick, level, renderDistance,
                        darkenWorldAmount, fluidFogColor)
                        : extensions.fogColor;
            }

            @Override
            public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick,
                                        float nearDistance, float farDistance, FogShape shape) {
                RenderSystem.setShaderFogStart(2f);
                RenderSystem.setShaderFogEnd(4f);
            }
        };
    }

    @Override
    public Optional<SoundEvent> getPickupSound(BlockState state) {
        return Optional.of(SoundEvents.BUCKET_FILL);
    }


    public static class AdditionalProperties {
        private int levelDecreasePerBlock = 1;
        private float explosionResistance = 1;
        private int slopeFindDistance = 4;
        private int tickRate = 5;

        public AdditionalProperties explosionResistance(float resistance) {
            this.explosionResistance = resistance;
            return this;
        }

        public AdditionalProperties levelDecreasePerBlock(int decrease) {
            this.levelDecreasePerBlock = decrease;
            return this;
        }

        public AdditionalProperties slopeFindDistance(int distance) {
            this.slopeFindDistance = distance;
            return this;
        }

        public AdditionalProperties tickRate(int rate) {
            this.tickRate = rate;
            return this;
        }
    }

    public static class ClientExtensions {
        private ResourceLocation still;
        private ResourceLocation flowing;
        private ResourceLocation overlay;
        private ResourceLocation renderOverlay;
        private Vector3f fogColor;
        private TriFunction<FluidState, BlockAndTintGetter, BlockPos, Integer> tintFunction;

        private final String modid;

        public ClientExtensions(String modid, String fluidName) {
            this.modid = modid;
            still(fluidName);
            flowing(fluidName);
            overlay(fluidName);
        }

        public ClientExtensions flowing(String name) {
            return flowing(name, "block");
        }

        public ClientExtensions flowing(String name, String folder) {
            this.flowing = new ResourceLocation(this.modid, folder + "/" + name + "_flowing");
            return this;
        }

        public ClientExtensions fogColor(float red, float green, float blue) {
            this.fogColor = new Vector3f(red, green, blue);
            return this;
        }

        public ClientExtensions overlay(String name) {
            return overlay(name, "block");
        }

        public ClientExtensions overlay(String name, String folder) {
            this.overlay = new ResourceLocation(this.modid, folder + "/" + name + "_overlay");
            return renderOverlay(new ResourceLocation(this.modid, "textures/" + folder + "/" + name + "_overlay.png"));
        }

        public ClientExtensions renderOverlay(ResourceLocation path) {
            this.renderOverlay = path;
            return this;
        }

        public ClientExtensions still(String name) {
            return still(name, "block");
        }

        public ClientExtensions still(String name, String folder) {
            this.still = new ResourceLocation(this.modid, folder + "/" + name + "_still");
            return this;
        }

        public ClientExtensions tint(int tint) {
            this.tintFunction = ($0, $1, $2) -> tint;
            return this;
        }

        public ClientExtensions tint(TriFunction<FluidState, BlockAndTintGetter, BlockPos, Integer> tinter) {
            this.tintFunction = tinter;
            return this;
        }

    }
}
