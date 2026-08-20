package net.pixelbank.burntfighters.compat.burnt;

import java.lang.reflect.Method;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

/**
 * Reflective bridge to Burnt's own single-block extinguish routine.
 *
 * <p>Burnt keeps its fire and smoldering conversion tables inside procedure
 * classes rather than behind an API, so reflection is the only way to reach
 * them without a hard compile dependency. Going through the procedure matters:
 * it converts smoldering blocks into their burnt variants instead of deleting
 * them, and it keeps Burnt's world-level flame counter in sync. Removing the
 * blocks ourselves would destroy player builds and permanently desync that
 * counter.
 */
final class BurntExtinguishProcedure {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String MOD_ID = "burnt";
    private static final String PROCEDURE =
            "net.pixelbank.burnt.procedures.ExtinguishProcedure";

    private static boolean initialised;
    private static Method execute;

    private BurntExtinguishProcedure() {
    }

    /** Runs Burnt's extinguish at {@code pos}. Returns true if the block changed. */
    static boolean executeAt(LevelAccessor level, BlockPos pos) {
        if (!init())
            return false;

        var before = level.getBlockState(pos);
        try {
            execute.invoke(null, level, (double) pos.getX(), (double) pos.getY(), (double) pos.getZ());
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("Burnt's ExtinguishProcedure threw; disabling the bridge", e);
            execute = null;
            return false;
        }
        return !level.getBlockState(pos).equals(before);
    }

    static boolean isAvailable() {
        return init();
    }

    private static synchronized boolean init() {
        if (initialised)
            return execute != null;

        initialised = true;
        if (!ModList.get().isLoaded(MOD_ID))
            return false;

        try {
            execute = Class.forName(PROCEDURE).getMethod(
                    "execute", LevelAccessor.class, double.class, double.class, double.class);
            LOGGER.debug("Bound to Burnt's ExtinguishProcedure");
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.warn("Burnt is present but {} could not be bound; Burnt fires will not be extinguished",
                    PROCEDURE, e);
            execute = null;
        }
        return execute != null;
    }
}
