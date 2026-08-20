package net.pixelbank.burntfighters.compat.firefighting;

import java.util.HashMap;
import java.util.Map;

import com.mikoalopex.createfirefightingadd.api.nozzle.NozzleSprayBlockInteraction;
import com.mikoalopex.createfirefightingadd.api.nozzle.NozzleSprayHitContext;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.pixelbank.burntfighters.compat.burnt.BurntExtinguish;
import net.pixelbank.burntfighters.compat.burnt.BurntFireConnector;

/**
 * Puts Burnt fires out when a Create: FireFighting Additions nozzle sprays them.
 *
 * <h2>Why this exists</h2>
 *
 * FFA ships its own Burnt bridge, but it calls Burnt's
 * {@code ExtinguishProcedure} — the reduced single-block routine used by
 * bare-hand extinguishing. Burnt's own extinguisher spray calls
 * {@code ExtinguishBlockProcedure} instead, which is a strict superset. The
 * difference is not cosmetic: burning doors, trapdoors, campfires, sails, fire
 * barrels, crops, cave vines, envelopes and smoldering coal are handled only by
 * the second one, so spraying any of them with FFA does nothing at all.
 *
 * <p>This runs off FFA's public spray API rather than a mixin. The callback is
 * documented as additive and cannot cancel built-in effects, which is fine —
 * it fires before FFA's own extinguish pass, so by the time FFA's bridge looks
 * at the block we have already converted it and its call is a no-op.
 *
 * <h2>Rate limiting</h2>
 *
 * {@code ExtinguishBlockProcedure} is not a single-block call; it sweeps 216
 * blocks. A nozzle reports hundreds of blocks per tick, so calling it per hit
 * would be catastrophic. Instead each hit is snapped to a coarse grid and at
 * most one sweep runs per grid cell per tick, which collapses a full spray cone
 * into a handful of calls while still covering every block in it.
 */
public final class BurntSprayInteraction implements NozzleSprayBlockInteraction {
    public static final BurntSprayInteraction INSTANCE = new BurntSprayInteraction();

    /** Sweep anchors already handled this tick, per dimension. */
    private final Map<ResourceKey<Level>, LongOpenHashSet> sweptThisTick = new HashMap<>();

    private long currentTick = Long.MIN_VALUE;

    private BurntSprayInteraction() {
    }

    @Override
    public boolean shouldReceive(NozzleSprayHitContext context) {
        Level level = context.level();
        return level != null
                && !level.isClientSide
                && context.isWaterLike()
                && BurntFireConnector.isSprayTarget(context.state());
    }

    @Override
    public void onHit(NozzleSprayHitContext context) {
        Level level = context.level();
        BlockPos anchor = BurntExtinguish.anchorFor(context.pos());

        if (!claim(level, anchor))
            return;

        // Burnt's own splash effect is the only feedback the player gets here:
        // FFA plays its extinguish sound from its bridge, which by this point
        // finds the block already converted and stays silent.
        BurntFireConnector.extinguishAround(level, anchor, true);
    }

    /**
     * Reserves an anchor for this tick.
     *
     * @return true if this is the first hit on that anchor this tick
     */
    private boolean claim(Level level, BlockPos anchor) {
        long tick = level.getGameTime();
        if (tick != currentTick) {
            currentTick = tick;
            sweptThisTick.clear();
        }

        return sweptThisTick
                .computeIfAbsent(level.dimension(), key -> new LongOpenHashSet())
                .add(anchor.asLong());
    }
}
