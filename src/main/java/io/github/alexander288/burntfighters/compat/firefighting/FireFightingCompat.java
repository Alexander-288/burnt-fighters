package io.github.alexander288.burntfighters.compat.firefighting;

import com.mikoalopex.createfirefightingadd.api.nozzle.NozzleSprayInteractionRegistry;

/**
 * Wires this mod into Create: FireFighting Additions' spray API.
 *
 * <p>Must only be touched when that mod is actually loaded — the class
 * references its types directly and will fail to link otherwise.
 */
public final class FireFightingCompat {
    public static final String MOD_ID = "createfirefightingadd";

    private FireFightingCompat() {
    }

    public static void register() {
        NozzleSprayInteractionRegistry.register(BurntSprayInteraction.INSTANCE);
    }
}
