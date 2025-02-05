package net.sinedkadis.terracompositio.util;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TCUtil {
    public static @NotNull List<BlockPos> getNearBlocks(BlockPos pPos, int range) {
        List<BlockPos> toReplace = new ArrayList<>();
        for (int x = range * -1; x <= range; x++){
            for (int y = range * -1; y <= range; y++){
                for (int z =range * -1; z <= range; z++){
                    toReplace.add(new BlockPos(pPos.getX() + x,
                            pPos.getY() + y,
                            pPos.getZ() + z));
                }
            }
        }
        return toReplace;
    }
    public static @NotNull List<BlockPos> getNearBlocks(BlockPos pos){
        return getNearBlocks(pos,1);
    }
}
