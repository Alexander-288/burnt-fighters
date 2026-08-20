package net.pixelbank.burntfighters;

import com.mojang.logging.LogUtils;

import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.pixelbank.burntfighters.compat.create.CreateCompat;
import org.slf4j.Logger;

@Mod(BurntFighters.MOD_ID)
public final class BurntFighters {
    public static final String MOD_ID = "burnt_fighters";

    private static final Logger LOGGER = LogUtils.getLogger();

    public BurntFighters() {
        if (ModList.get().isLoaded("create")) {
            CreateCompat.register();
            LOGGER.info("Create detected; registered Burnt fire spouting behaviour");
        }
    }
}
