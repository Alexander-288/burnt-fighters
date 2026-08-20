package io.github.alexander288.burntfighters.compat.firefighting;

import com.mikoalopex.createfirefightingadd.api.nozzle.NozzleSprayBlockInteraction;
import com.mikoalopex.createfirefightingadd.api.nozzle.NozzleSprayHitContext;

import net.minecraft.world.level.Level;
import io.github.alexander288.burntfighters.compat.burnt.BurntFireConnector;
import io.github.alexander288.burntfighters.compat.burnt.SuppressionAgent;

/**
 * Puts Burnt fires out when a Create: FireFighting Additions nozzle sprays them.
 *
 * <h2>Why this exists</h2>
 *
 * FFA ships its own Burnt bridge, but it calls Burnt's
 * {@code ExtinguishProcedure} without handling the door and trapdoor
 * procedures, and gates on a registry-name guess. Burning doors, trapdoors,
 * campfires, sails, fire barrels, crops, cave vines, envelopes and smoldering
 * coal all fail to extinguish.
 *
 * <p>This runs off FFA's public spray API rather than a mixin. The callback is
 * documented as additive and cannot cancel built-in effects, which is fine —
 * it fires before FFA's own extinguish pass, so by the time FFA's bridge looks
 * at the block we have already converted it and its call is a no-op.
 *
 * <h2>Reach</h2>
 *
 * Water is surface-only: it extinguishes the block the spray actually landed
 * on and nothing behind it. That is not a limitation we invented — FFA's rays
 * clip on collision and stop at the first solid block anyway, so anything
 * deeper was never reported to us. Water simply does not pretend otherwise.
 *
 * <p>Foam is the agent that soaks past the surface. See
 * {@link SuppressionAgent}.
 */
public final class BurntSprayInteraction implements NozzleSprayBlockInteraction {
    public static final BurntSprayInteraction INSTANCE = new BurntSprayInteraction();

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
        BurntFireConnector.extinguish(context.level(), context.pos(), agentFor(context));
    }

    /**
     * Which agent this spray counts as.
     *
     * <p>Everything is water for now. Once the foam fluid exists this is where
     * it is recognised.
     */
    private static SuppressionAgent agentFor(NozzleSprayHitContext context) {
        return SuppressionAgent.WATER;
    }
}
