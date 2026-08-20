package net.pixelbank.burntfighters.compat.create;

import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.api.registry.SimpleRegistry;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.pixelbank.burntfighters.compat.burnt.BurntFireConnector;

/**
 * Wires this mod's behaviours into Create's registries.
 *
 * <p>Must only be touched when Create is actually loaded — the class references
 * Create types directly and will fail to link otherwise.
 */
public final class CreateCompat {
    private CreateCompat() {
    }

    public static void register() {
        for (TagKey<Block> tag : BurntFireConnector.FIRE_TAGS) {
            BlockSpoutingBehaviour.BY_BLOCK.registerProvider(
                    SimpleRegistry.Provider.forBlockTag(tag, BurntFireSpoutingBehavior.INSTANCE));
        }
    }
}
