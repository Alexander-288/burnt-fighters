package net.pixelbank.burntfighters.compat.create;

import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.pixelbank.burntfighters.compat.burnt.BurntFireConnector;

/**
 * Lets a Create spout put out a Burnt fire directly beneath it.
 *
 * <p>Registered against Burnt's fire tags, so the block at {@code pos} is
 * already known to be a fire block by the time this runs — no area scan needed.
 */
public final class BurntFireSpoutingBehavior implements BlockSpoutingBehaviour {
    public static final BurntFireSpoutingBehavior INSTANCE = new BurntFireSpoutingBehavior();

    /** Water consumed per block extinguished, in millibuckets. */
    private static final int COST_PER_BLOCK = 250;

    private BurntFireSpoutingBehavior() {
    }

    @Override
    public int fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack fluid, boolean simulate) {
        if (fluid.isEmpty() || !fluid.getFluid().isSame(Fluids.WATER))
            return 0;
        if (fluid.getAmount() < COST_PER_BLOCK)
            return 0;
        if (!BurntFireConnector.isBurntFire(level, pos))
            return 0;

        if (simulate)
            return COST_PER_BLOCK;

        return BurntFireConnector.extinguish(level, pos) ? COST_PER_BLOCK : 0;
    }
}
