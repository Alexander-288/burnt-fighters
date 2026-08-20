package net.pixelbank.burntfighters.compat.burnt;

import java.lang.reflect.Method;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

/**
 * Reflective bridge to Burnt's extinguish procedures.
 *
 * <p>Burnt keeps its conversion tables inside procedure classes rather than
 * behind an API, so reflection is the only way in without a hard dependency.
 * Going through them matters: they convert smoldering blocks back into their
 * burnt variants instead of deleting them, and they keep Burnt's world-level
 * flame counter in sync.
 *
 * <p>Burnt has two entry points and picking the right one is the whole game:
 *
 * <ul>
 *   <li>{@code ExtinguishProcedure} — single block, and an incomplete subset.
 *       It is what Burnt's bare-hand extinguish uses. It does not know about
 *       doors, trapdoors, campfires, sails, fire barrels, crops, cave vines,
 *       envelopes or smoldering coal.
 *   <li>{@code ExtinguishBlockProcedure} — the complete one, and what Burnt's
 *       own extinguisher spray calls when its projectile hits a block. It
 *       dispatches to the door/trapdoor/bamboo sub-procedures and handles
 *       everything the first one misses.
 * </ul>
 *
 * <p>We always want the second. Note that it is <em>not</em> a single-block
 * call: it sweeps a 6x6x6 region, offset -3..+2 on each axis from the
 * coordinate handed to it. Callers must rate-limit accordingly — see
 * {@link #SWEEP_SIZE} and {@link #anchorFor}.
 */
public final class BurntExtinguish {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String MOD_ID = "burnt";
    private static final String SWEEP_PROCEDURE =
            "net.pixelbank.burnt.procedures.ExtinguishBlockProcedure";
    private static final String SINGLE_PROCEDURE =
            "net.pixelbank.burnt.procedures.ExtinguishProcedure";

    /** Edge length of the region {@link #sweep} covers, offset -3..+2 from the anchor. */
    public static final int SWEEP_SIZE = 6;

    /**
     * Grid step used to derive sweep anchors. Deliberately smaller than
     * {@link #SWEEP_SIZE} so consecutive cells overlap and nothing falls
     * between two sweeps.
     */
    private static final int ANCHOR_GRID = 4;

    private static boolean initialised;
    private static Method sweep;
    private static Method single;

    private BurntExtinguish() {
    }

    /**
     * Snaps a position to the sweep grid.
     *
     * <p>The sweep reaches -3..+2 from its anchor, so anchoring at
     * {@code 4k + 1} covers {@code 4k - 2 .. 4k + 3} and therefore fully
     * contains the grid cell {@code 4k .. 4k + 3}. Anchoring at {@code 4k}
     * instead would leave {@code 4k + 3} uncovered.
     *
     * <p>Callers can use the anchor as a dedupe key: every position in a cell
     * yields the same anchor, so one sweep per anchor per tick is enough.
     */
    public static BlockPos anchorFor(BlockPos pos) {
        return new BlockPos(
                Math.floorDiv(pos.getX(), ANCHOR_GRID) * ANCHOR_GRID + 1,
                Math.floorDiv(pos.getY(), ANCHOR_GRID) * ANCHOR_GRID + 1,
                Math.floorDiv(pos.getZ(), ANCHOR_GRID) * ANCHOR_GRID + 1);
    }

    /**
     * Runs Burnt's full extinguish over the region around {@code anchor}.
     *
     * <p>This touches up to 216 blocks. Do not call it per sprayed block —
     * derive an anchor with {@link #anchorFor} and call it once per anchor per
     * tick.
     *
     * @param showSpray whether Burnt plays its splash effect at the anchor
     * @return false only if the bridge is unavailable; the procedure reports
     *         nothing about what it changed
     */
    public static boolean sweep(LevelAccessor level, BlockPos anchor, boolean showSpray) {
        if (!init() || sweep == null)
            return false;

        try {
            sweep.invoke(null, level,
                    (double) anchor.getX(), (double) anchor.getY(), (double) anchor.getZ(), showSpray);
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("Burnt's ExtinguishBlockProcedure threw; disabling the bridge", e);
            sweep = null;
            return false;
        }
    }

    /**
     * Extinguishes exactly one block, and reports whether it changed.
     *
     * <p>Uses Burnt's single-block procedure, which is incomplete — see the
     * class docs. Only worth it where a precise, bounded effect is wanted and
     * the caller needs to know whether anything happened.
     */
    public static boolean single(LevelAccessor level, BlockPos pos) {
        if (!init() || single == null)
            return false;

        var before = level.getBlockState(pos);
        try {
            single.invoke(null, level, (double) pos.getX(), (double) pos.getY(), (double) pos.getZ());
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("Burnt's ExtinguishProcedure threw; disabling the bridge", e);
            single = null;
            return false;
        }
        return !level.getBlockState(pos).equals(before);
    }

    public static boolean isAvailable() {
        return init() && sweep != null;
    }

    private static synchronized boolean init() {
        if (initialised)
            return sweep != null || single != null;

        initialised = true;
        if (!ModList.get().isLoaded(MOD_ID))
            return false;

        sweep = bind(SWEEP_PROCEDURE, LevelAccessor.class,
                double.class, double.class, double.class, boolean.class);
        single = bind(SINGLE_PROCEDURE, LevelAccessor.class,
                double.class, double.class, double.class);

        if (sweep == null) {
            LOGGER.warn("Burnt is present but {} could not be bound. Falling back to the incomplete "
                    + "single-block procedure; burning doors, campfires and sails will not extinguish.",
                    SWEEP_PROCEDURE);
        }
        return sweep != null || single != null;
    }

    private static Method bind(String owner, Class<?>... signature) {
        try {
            return Class.forName(owner).getMethod("execute", signature);
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.debug("Could not bind {}#execute", owner, e);
            return null;
        }
    }
}
