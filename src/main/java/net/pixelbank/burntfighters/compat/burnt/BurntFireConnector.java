package net.pixelbank.burntfighters.compat.burnt;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Everything this mod knows about Burnt's fire blocks.
 *
 * <p>The tags come from Burnt's own datapack, so they resolve to nothing when
 * Burnt is absent and every query here answers false.
 */
public final class BurntFireConnector {
    /** Flames proper, plus smoldering crops and vines. */
    public static final TagKey<Block> FIRE = tag("fire");
    /** Structural blocks that are currently burning: logs, doors, stairs, leaves. */
    public static final TagKey<Block> ON_FIRE = tag("on_fire");
    /** Spreading flames only. */
    public static final TagKey<Block> ACTIVE_FIRE = tag("active_fire");

    private BurntFireConnector() {
    }

    private static TagKey<Block> tag(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("burnt", path));
    }

    public static boolean isBurntFire(BlockState state) {
        return state.is(FIRE) || state.is(ON_FIRE) || state.is(ACTIVE_FIRE);
    }

    public static boolean isBurntFire(Level level, BlockPos pos) {
        return level != null
                && level.isLoaded(pos)
                && isBurntFire(level.getBlockState(pos));
    }

    /**
     * Extinguishes a single block through Burnt's own rules.
     *
     * @return true if the block actually changed
     */
    public static boolean extinguish(Level level, BlockPos pos) {
        if (level == null || level.isClientSide || !level.isLoaded(pos))
            return false;
        if (!isBurntFire(level.getBlockState(pos)))
            return false;

        return BurntExtinguishProcedure.executeAt(level, pos);
    }

    /**
     * Extinguishes every Burnt fire in the cube of the given radius around
     * {@code center}.
     *
     * @return how many blocks changed
     */
    public static int extinguishArea(Level level, BlockPos center, int radius) {
        if (level == null || level.isClientSide || !level.isLoaded(center))
            return 0;

        int changed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    pos.setWithOffset(center, dx, dy, dz);
                    if (!level.isLoaded(pos))
                        continue;
                    if (!isBurntFire(level.getBlockState(pos)))
                        continue;
                    if (BurntExtinguishProcedure.executeAt(level, pos))
                        changed++;
                }
            }
        }

        return changed;
    }
}
