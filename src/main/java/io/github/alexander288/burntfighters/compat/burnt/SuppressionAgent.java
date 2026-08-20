package io.github.alexander288.burntfighters.compat.burnt;

/**
 * What a given fluid can do to a fire.
 *
 * <p>The distinction is reach, not capability. Both agents extinguish the same
 * block types — Burnt draws no line between "burning" and "smoldering", those
 * are the same thing in its data — so the tier is expressed as how far into
 * the material the fluid gets.
 *
 * <p>This matters because Create: FireFighting Additions' spray stops at the
 * first block with a collision box. Its rays clip with
 * {@code ClipContext.Block.COLLIDER}, and Burnt never registers its own blocks
 * as flammable, so every burning log and plank occludes. Water therefore only
 * ever reaches the outside face of a burning structure. Foam is what gets into
 * the wall.
 */
public enum SuppressionAgent {
    /** Reaches the surface it lands on and no further. */
    WATER(0),

    /** Soaks into the material around the impact point. */
    FOAM(2);

    private final int radius;

    SuppressionAgent(int radius) {
        this.radius = radius;
    }

    /**
     * Cube radius around the impact point. 0 is the impact block alone; 2 is a
     * 5x5x5 region.
     */
    public int radius() {
        return radius;
    }

    public boolean penetrates() {
        return radius > 0;
    }
}
