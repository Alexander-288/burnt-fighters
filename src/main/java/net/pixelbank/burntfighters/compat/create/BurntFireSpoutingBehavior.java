package net.pixelbank.burntfighters.compat.create;

import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.pixelbank.burntfighters.compat.burnt.BurntFireConnector;
import net.pixelbank.burntfighters.compat.burnt.SuppressionAgent;

/**
 * Lets a Create spout put out a Burnt fire directly beneath it.
 *
 * <p>Registered against Burnt's fire tags, so the block at {@code pos} is
 * already known to be burning by the time this runs — no scan needed to find
 * it.
 *
 * <p>A spout runs water, so it puts out the block it is aimed at and nothing
 * more. Foam is the agent that reaches into material.
 */
public final class BurntFireSpoutingBehavior implements BlockSpoutingBehaviour {
    public static final BurntFireSpoutingBehavior INSTANCE = new BurntFireSpoutingBehavior();

    /** Water consumed per block extinguished, in millibuckets. */
    private static final int COST = 250;

    private BurntFireSpoutingBehavior() {
    }

    @Override
    public int fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack fluid, boolean simulate) {
        if (fluid.isEmpty() || !fluid.getFluid().isSame(Fluids.WATER))
            return 0;
        if (fluid.getAmount() < COST)
            return 0;
        if (!BurntFireConnector.isBurntFire(level, pos))
            return 0;

        if (simulate)
            return COST;

        return BurntFireConnector.extinguish(level, pos, SuppressionAgent.WATER) > 0 ? COST : 0;
    }
}
