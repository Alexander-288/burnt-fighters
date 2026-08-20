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
 * already known to be burning by the time this runs — no scan needed to find
 * it.
 *
 * <p>Extinguishing goes through Burnt's full routine, which clears the
 * surrounding region rather than the single block. That is deliberate: a spout
 * fed with water is a fire-suppression machine, and the alternative single-block
 * routine cannot put out burning doors, campfires or sails at all. The cost
 * reflects the area cleared.
 */
public final class BurntFireSpoutingBehavior implements BlockSpoutingBehaviour {
    public static final BurntFireSpoutingBehavior INSTANCE = new BurntFireSpoutingBehavior();

    /** Water consumed per successful extinguish, in millibuckets. */
    private static final int COST = 1000;

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

        // The procedure reports nothing about what it changed, so judge success
        // by whether the block we were aimed at stopped burning.
        var before = level.getBlockState(pos);
        BurntFireConnector.extinguishAround(level, pos, true);
        return level.getBlockState(pos).equals(before) ? 0 : COST;
    }
}
