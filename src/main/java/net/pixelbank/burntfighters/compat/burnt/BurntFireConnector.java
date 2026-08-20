package net.pixelbank.burntfighters.compat.burnt;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Everything this mod knows about Burnt's burning blocks.
 *
 * <p>The tags come from Burnt's own datapack, so they resolve to nothing when
 * Burnt is absent and every query here answers false.
 *
 * <p>Recognition is done purely by tag. Create: FireFighting Additions' own
 * bridge additionally guesses from the block's registry path — matching
 * anything containing "fire", "flame", "burning" or "smoldering" — which
 * false-positives on unrelated blocks from other mods (coral, campfires,
 * decorative braziers) and then pays for a reflective call that does nothing.
 */
public final class BurntFireConnector {
    private BurntFireConnector() {
    }

    // Flames proper.
    public static final TagKey<Block> FIRE = tag("fire");
    public static final TagKey<Block> ACTIVE_FIRE = tag("active_fire");
    public static final TagKey<Block> WOOD_FIRE = tag("wood_fire");
    public static final TagKey<Block> COPPER_FIRE = tag("copper_fire");
    public static final TagKey<Block> TALL_FLAMES = tag("tall_flames");

    /** Structural blocks that are currently burning. */
    public static final TagKey<Block> ON_FIRE = tag("on_fire");

    // Burnt routes these two through their own procedures, so they are named.
    public static final TagKey<Block> BURNING_DOORS = tag("burning_doors");
    public static final TagKey<Block> BURNING_TRAPDOORS = tag("burning_trapdoors");

    /** Soot deposits. Not fire, but water washes them off. */
    public static final TagKey<Block> SOOTY = tag("sooty");

    /** Immersive Weathering leaf piles, which Burnt destroys rather than converts. */
    public static final TagKey<Block> IW_LEAF_PILES = tag("iw_leaf_piles");

    /**
     * Every tag whose blocks a water spray should act on.
     *
     * <p>The burning_* set is listed explicitly rather than relying on
     * {@code on_fire} alone: doors, trapdoors, bamboo, grass and leaves each
     * live in their own tag and are the exact cases FFA's bridge silently
     * fails to extinguish.
     */
    public static final List<TagKey<Block>> FIRE_TAGS = List.of(
            FIRE,
            ACTIVE_FIRE,
            ON_FIRE,
            WOOD_FIRE,
            COPPER_FIRE,
            TALL_FLAMES,
            tag("smoldering_leaves"),
            tag("smoldering_logs"),
            tag("smoldering_planks"),
            tag("burning_logs"),
            tag("burning_wood"),
            tag("burning_stripped_logs"),
            tag("burning_stripped_wood"),
            tag("burning_planks"),
            tag("burning_stairs"),
            tag("burning_slabs"),
            tag("burning_fences"),
            tag("burning_fence_gates"),
            BURNING_DOORS,
            BURNING_TRAPDOORS,
            tag("burning_bamboo"),
            tag("burning_grass"),
            tag("burning_leaves"));

    private static TagKey<Block> tag(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("burnt", path));
    }

    /** True if the block is burning in some form Burnt understands. */
    public static boolean isBurntFire(BlockState state) {
        for (TagKey<Block> tag : FIRE_TAGS) {
            if (state.is(tag))
                return true;
        }
        return false;
    }

    public static boolean isBurntFire(Level level, BlockPos pos) {
        return level != null
                && level.isLoaded(pos)
                && isBurntFire(level.getBlockState(pos));
    }

    /**
     * Fire, soot, and anything else we know how to put out.
     *
     * <p>The {@link BurntExtinguish#handles} arm is not redundant. Several of
     * Burnt's own burning blocks are in no fire tag whatsoever — active fire
     * barrels, envelopes, sails, ember campfires — so a tag-only test drops
     * them silently.
     */
    public static boolean isSprayTarget(BlockState state) {
        return isBurntFire(state) || state.is(SOOTY) || BurntExtinguish.handles(state);
    }

    /**
     * Extinguishes fire at {@code pos} using the given agent.
     *
     * <p>{@link SuppressionAgent#WATER} affects only the impact block;
     * {@link SuppressionAgent#FOAM} soaks into the surrounding region.
     *
     * @return how many blocks changed
     */
    public static int extinguish(Level level, BlockPos pos, SuppressionAgent agent) {
        if (level == null || level.isClientSide || !level.isLoaded(pos))
            return 0;

        return BurntExtinguish.extinguishRegion(level, pos, agent.radius());
    }
}
