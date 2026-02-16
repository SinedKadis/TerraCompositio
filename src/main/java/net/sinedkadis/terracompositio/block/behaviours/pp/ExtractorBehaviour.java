package net.sinedkadis.terracompositio.block.behaviours.pp;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.sinedkadis.terracompositio.api.TCCapabilities;
import net.sinedkadis.terracompositio.block.entity.PathPointerBlockEntity;
import net.sinedkadis.terracompositio.registries.TCItems;
import net.sinedkadis.terracompositio.util.TCUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ExtractorBehaviour extends PPInputBehaviour{

    private final int range = 5;
    List<LivingEntity> entityList = List.of();

    public ExtractorBehaviour(PathPointerBlockEntity blockEntity) {
        super(blockEntity);
    }


    @Override
    public void init() {
        validateCFEBehaviour();
    }

    @Override
    public void onUpdate() {
        if (invalidBehaviours()) return;
        updateMaxCFE();
        collectCFE();
    }

    @Override
    protected void collectCFE() {
        int freeSpace = thisCFEBehaviour.getFreeSpace();
        if (freeSpace < 1) return;
        scanCompatibleEntities().forEach(livingEntity ->
                livingEntity.getCapability(TCCapabilities.CFE).ifPresent(icfeHandler ->
                        TCUtil.tryCFETransfer(thisCFEBehaviour, icfeHandler, freeSpace)));
    }

    public @NotNull List<LivingEntity> scanCompatibleEntities() {
        if (isEntityListValid()) return entityList;

        Level level = blockEntity.getLevel();
        if (level == null) return List.of();

        AABB searchRange = new AABB(blockEntity.getBlockPos()
                .relative(Direction.WEST, range)
                .relative(Direction.SOUTH, range)
                .relative(Direction.DOWN, range),
                blockEntity.getBlockPos()
                        .relative(Direction.EAST, range)
                        .relative(Direction.NORTH, range)
                        .relative(Direction.UP, range));
        List<Entity> entities = level.getEntities(null,
                searchRange);
        List<LivingEntity> found = entities.stream()
                .map(entity -> entity instanceof LivingEntity livingEntity ? livingEntity : null)
                .filter(Objects::nonNull)
                .filter(livingEntity -> livingEntity.hasItemInSlot(EquipmentSlot.HEAD))
                .filter(livingEntity -> livingEntity.getItemBySlot(EquipmentSlot.HEAD).is(TCItems.TECHNETIUM_CROWN.get()))
                .toList();
        entityList = found;
        return found;
    }

    public boolean isEntityListValid() {
        Level level = blockEntity.getLevel();
        if (level == null) return false;

        return entityList.stream().allMatch(livingEntity ->
                !livingEntity.isRemoved()
                        && blockEntity.getBlockPos().closerToCenterThan(livingEntity.position(), range)
                        && livingEntity.hasItemInSlot(EquipmentSlot.HEAD)
                        && livingEntity.getItemBySlot(EquipmentSlot.HEAD).is(TCItems.TECHNETIUM_CROWN.get()));
    }
}