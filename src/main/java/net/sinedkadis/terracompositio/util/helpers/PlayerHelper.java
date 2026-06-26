package net.sinedkadis.terracompositio.util.helpers;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

public class PlayerHelper {
    public static void addOrDropToPlayer(@NotNull Player pPlayer, ItemStack toAdd) {
        addOrDropToPlayer(pPlayer, toAdd, false);
    }

    public static void addOrDropToPlayer(@NotNull Player pPlayer, ItemStack toAdd, boolean addInCreative) {
        if (addInCreative || !pPlayer.isCreative()) {
            if (!pPlayer.addItem(toAdd)) {
                pPlayer.drop(toAdd, false);
            }
        }
    }

    public static void message(Player pPlayer, Component pMessageComponent) {
        if (pPlayer instanceof ServerPlayer player)
            player.sendSystemMessage(pMessageComponent, true);
    }

    public static @Nullable EntityHitResult getEntityHitResult(Minecraft mc, LocalPlayer player, Level level) {
        EntityHitResult entityResult;
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPosition = camera.getPosition();
        Vec3 traceEnd;
        float playerReach = (float) player.getEntityReach();
        if (mc.hitResult == null) {
            Vec3 lookVector = new Vec3(camera.getLookVector().mul(playerReach));
            traceEnd = cameraPosition.add(lookVector);
        } else if (mc.hitResult.getType() != HitResult.Type.BLOCK) {
            traceEnd = mc.hitResult.getLocation();
            traceEnd = cameraPosition.add(traceEnd.subtract(cameraPosition).normalize().scale(playerReach * 1.001));
        } else {
            traceEnd = mc.hitResult.getLocation();
            traceEnd = cameraPosition.add(traceEnd.subtract(cameraPosition));
        }
        AABB bound = new AABB(cameraPosition, traceEnd);
        Predicate<Entity> predicate = (e) -> canBeTarget(e, player);


        entityResult = getEntityHitResult(level, player, cameraPosition, traceEnd, bound, predicate);
        return entityResult;
    }

    public static @Nullable EntityHitResult getEntityHitResult(Level worldIn, Entity projectile, Vec3 startVec, Vec3 endVec, AABB boundingBox, Predicate<Entity> filter) {
        double d0 = Double.MAX_VALUE;
        Entity entity = null;

        for (Entity entity1 : worldIn.getEntities(projectile, boundingBox, filter)) {
            AABB axisalignedbb = entity1.getBoundingBox();
            if (axisalignedbb.getSize() < 0.3) {
                axisalignedbb = axisalignedbb.inflate(0.3);
            }

            if (axisalignedbb.contains(startVec)) {
                entity = entity1;
                break;
            }

            Optional<Vec3> optional = axisalignedbb.clip(startVec, endVec);
            if (optional.isPresent()) {
                double d1 = startVec.distanceToSqr(optional.get());
                if (d1 < d0) {
                    entity = entity1;
                    d0 = d1;
                }
            }
        }

        return entity == null ? null : new EntityHitResult(entity);
    }

    private static boolean canBeTarget(Entity target, Entity viewEntity) {
        if (target.isRemoved()) {
            return false;
        } else if (target.isSpectator()) {
            return false;
        } else if (target == viewEntity.getVehicle()) {
            return false;
        } else {
            if (target instanceof Projectile projectile) {
                if (projectile.tickCount <= 10) {
                    return false;
                }
            }

            if (target.isMultipartEntity() && !target.isPickable()) {
                return false;
            } else {
                if (viewEntity instanceof Player player) {
                    if (target.isInvisibleTo(player)) {
                        return false;
                    }

                    Minecraft mc = Minecraft.getInstance();
                    return mc.gameMode == null || !mc.gameMode.isDestroying() || target.getType() != EntityType.ITEM;
                } else return !target.isInvisible();
            }
        }
    }
}
