package io.github.alexander288.burntfighters;

import com.mojang.logging.LogUtils;

import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import io.github.alexander288.burntfighters.compat.create.CreateCompat;
import io.github.alexander288.burntfighters.compat.firefighting.FireFightingCompat;
import org.slf4j.Logger;

@Mod(BurntFighters.MOD_ID)
public final class BurntFighters {
    public static final String MOD_ID = "burnt_fighters";

    private static final Logger LOGGER = LogUtils.getLogger();

    public BurntFighters() {
        // Each compat class references its target mod's types directly, so it
        // must not be loaded unless that mod is present.
        if (ModList.get().isLoaded("create")) {
            CreateCompat.register();
            LOGGER.info("Create detected; registered Burnt fire spouting behaviour");
        }

        if (ModList.get().isLoaded(FireFightingCompat.MOD_ID)) {
            FireFightingCompat.register();
            LOGGER.info("Create: FireFighting Additions detected; registered Burnt spray interaction");
        }
    }
}
