package io.github.alexander288.burntfighters.compat.burnt;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

/**
 * Reflective bridge to Burnt's extinguish procedures.
 *
 * <p>Burnt exposes no API, so extinguishing means reflecting into its procedure
 * classes. Delegating to them matters: they convert burning blocks back into
 * their burnt variants rather than deleting them, and they keep the world-level
 * flame counter in sync.
 *
 * <h2>Why we dispatch per block instead of calling Burnt's bulk routine</h2>
 *
 * Burnt's {@code ExtinguishBlockProcedure} is the complete routine, but it
 * sweeps a fixed 6x6x6 region offset -3..+2 from the coordinate given. It takes
 * no radius and no filter, so it cannot express either of the things this mod
 * needs: a tunable radius, or a fluid that only reaches the surface.
 *
 * <p>So we drive Burnt's <em>single-block</em> procedures ourselves and control
 * the region. {@code ExtinguishProcedure} covers most of it — the fire tag, all
 * the burning_* tags, soot, and several smoldering blocks — and internally
 * dispatches to Burnt's bamboo and sooty sub-procedures. Doors and trapdoors
 * have their own procedures, called directly. The handful of blocks Burnt only
 * converts inside the bulk routine are covered by {@link #DIRECT} below.
 */
public final class BurntExtinguish {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String MOD_ID = "burnt";
    private static final String PROCEDURES = "net.pixelbank.burnt.procedures.";

    private static boolean initialised;
    private static Method single;
    private static Method door;
    private static Method trapdoor;

    /**
     * Conversions Burnt performs only inside its bulk routine, which has no
     * single-block entry point to call.
     *
     * <p>Mirrors {@code ExtinguishBlockProcedure}. If Burnt changes these, this
     * table goes stale — everything else here delegates and cannot.
     */
    private static final Map<String, String> DIRECT = new LinkedHashMap<>();

    static {
        DIRECT.put("burnt:smoldering_coal", "minecraft:coal_block");
        DIRECT.put("burnt:fire_barrel_active", "burnt:fire_barrel");
        DIRECT.put("burnt:smoldering_crops", "burnt:burnt_crops");
        DIRECT.put("burnt:smoldering_cave_vines", "burnt:burnt_cave_vines_plant");
        DIRECT.put("burnt:smoldering_cave_vines_plant", "burnt:burnt_cave_vines_plant");
        DIRECT.put("burnt:smoldering_envelope", "burnt:burnt_envelope");
        DIRECT.put("burnt:smoldering_sail", "burnt:burnt_sail");
        DIRECT.put("burnt:smoldering_sail_frame", "burnt:burnt_sail");
        DIRECT.put("burnt:smoldering_symmetric_sail", "burnt:burnt_symmetric_sail");
        DIRECT.put("burnt:smoldering_campfire", "burnt:burnt_campfire");
        DIRECT.put("burnt:ember_campfire", "burnt:burnt_campfire");
    }

    /** Blocks Burnt destroys outright rather than converting. */
    private static final String LUMISENE = "supplementaries:lumisene";

    /** Sentinel for "looked this block up, there is no conversion". */
    private static final Block NO_CONVERSION = Blocks.AIR;

    private static final Map<Block, Block> CONVERSIONS = new ConcurrentHashMap<>();

    /**
     * Burnt's cross-mod naming convention: a {@code smoldering_X} block
     * extinguishes to {@code burnt_X} in the same namespace. Burnt applies this
     * rule itself for Dynamic Trees branches, and it is how any future mod's
     * burnt variants become extinguishable without code changes here.
     */
    private static final String SMOLDERING_PREFIX = "smoldering_";
    private static final String BURNT_PREFIX = "burnt_";

    /**
     * Whether this mod can extinguish the block, independently of Burnt's tags.
     *
     * <p>Needed because several of Burnt's own burning blocks are in no fire
     * tag at all — active fire barrels, envelopes, sails and ember campfires
     * among them — so a tag-only check silently skips them.
     */
    public static boolean handles(BlockState state) {
        return conversionFor(state) != null || destroys(state);
    }

    private static boolean destroys(BlockState state) {
        return state.is(BurntFireConnector.IW_LEAF_PILES)
                || BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().equals(LUMISENE);
    }

    /** Resolved lazily and cached; the registry does not change at runtime. */
    private static Block conversionFor(BlockState state) {
        Block block = state.getBlock();
        Block cached = CONVERSIONS.get(block);
        if (cached != null)
            return cached == NO_CONVERSION ? null : cached;

        Block resolved = resolveConversion(block);
        CONVERSIONS.put(block, resolved == null ? NO_CONVERSION : resolved);
        return resolved;
    }

    private static Block resolveConversion(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);

        String explicit = DIRECT.get(id.toString());
        if (explicit != null)
            return lookup(ResourceLocation.parse(explicit));

        if (id.getPath().startsWith(SMOLDERING_PREFIX)) {
            return lookup(ResourceLocation.fromNamespaceAndPath(
                    id.getNamespace(), BURNT_PREFIX + id.getPath().substring(SMOLDERING_PREFIX.length())));
        }
        return null;
    }

    private static Block lookup(ResourceLocation id) {
        Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
        return block == null || block == Blocks.AIR ? null : block;
    }

    private BurntExtinguish() {
    }

    /**
     * Extinguishes exactly one block.
     *
     * @return true if the block changed
     */
    public static boolean extinguishOne(Level level, BlockPos pos) {
        if (!init())
            return false;

        BlockState before = level.getBlockState(pos);
        if (before.isAir())
            return false;

        Method procedure = procedureFor(before);
        if (procedure != null && invoke(procedure, level, pos)) {
            if (!level.getBlockState(pos).equals(before))
                return true;
        }

        return convertDirectly(level, pos, before);
    }

    /**
     * Extinguishes every burning block within {@code radius} of {@code center},
     * as a cube. Radius 0 touches only the centre block.
     *
     * @return how many blocks changed
     */
    public static int extinguishRegion(Level level, BlockPos center, int radius) {
        if (!init())
            return 0;

        int changed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    pos.setWithOffset(center, dx, dy, dz);
                    if (!level.isLoaded(pos))
                        continue;
                    if (!BurntFireConnector.isSprayTarget(level.getBlockState(pos)))
                        continue;
                    if (extinguishOne(level, pos.immutable()))
                        changed++;
                }
            }
        }

        return changed;
    }

    public static boolean isAvailable() {
        return init() && single != null;
    }

    private static Method procedureFor(BlockState state) {
        if (door != null && state.is(BurntFireConnector.BURNING_DOORS))
            return door;
        if (trapdoor != null && state.is(BurntFireConnector.BURNING_TRAPDOORS))
            return trapdoor;
        return single;
    }

    private static boolean invoke(Method procedure, LevelAccessor level, BlockPos pos) {
        try {
            procedure.invoke(null, level, (double) pos.getX(), (double) pos.getY(), (double) pos.getZ());
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("Burnt's {} threw at {}", procedure.getDeclaringClass().getSimpleName(), pos, e);
            return false;
        }
    }

    /** Applies a conversion Burnt only performs inside its bulk routine. */
    private static boolean convertDirectly(Level level, BlockPos pos, BlockState before) {
        if (destroys(before)) {
            level.destroyBlock(pos, false);
            return true;
        }

        Block target = conversionFor(before);
        if (target == null)
            return false;

        level.setBlock(pos, copyProperties(before, target.defaultBlockState()), 3);
        return true;
    }

    private static BlockState copyProperties(BlockState from, BlockState to) {
        BlockState result = to;
        for (Property<?> property : from.getProperties()) {
            if (result.hasProperty(property))
                result = copyProperty(from, result, property);
        }
        return result;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(
            BlockState from, BlockState to, Property<T> property) {
        return to.setValue(property, from.getValue(property));
    }

    private static synchronized boolean init() {
        if (initialised)
            return single != null;

        initialised = true;
        if (!ModList.get().isLoaded(MOD_ID))
            return false;

        single = bind("ExtinguishProcedure");
        door = bind("DoorExtinguishProcedure");
        trapdoor = bind("TrapdoorExtinguishProcedure");

        if (single == null)
            LOGGER.warn("Burnt is present but its extinguish procedures could not be bound; "
                    + "Burnt fires will not be extinguished");
        return single != null;
    }

    private static Method bind(String name) {
        try {
            return Class.forName(PROCEDURES + name).getMethod(
                    "execute", LevelAccessor.class, double.class, double.class, double.class);
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.debug("Could not bind {}{}#execute", PROCEDURES, name, e);
            return null;
        }
    }
}
