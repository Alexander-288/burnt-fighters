# Changelog

## 1.0.0

First release.

### Create

- A spout running water extinguishes a Burnt fire beneath it, 250 mB per block.

### Create: FireFighting Additions

- Nozzles now extinguish Burnt fires that previously did nothing when sprayed:
  burning doors, trapdoors, campfires, sails, fire barrels, crops, cave vines,
  envelopes, smoldering coal, bamboo and leaf piles.

  FFA's built-in Burnt bridge calls Burnt's `ExtinguishProcedure`, the reduced
  single-block routine behind bare-hand extinguishing. Burnt's own extinguisher
  spray calls `ExtinguishBlockProcedure`, which handles considerably more. This
  mod dispatches to the right procedure per block type.

- Soot is washed off by water.

- Integration runs through FFA's public `NozzleSprayInteractionRegistry`, not a
  mixin, so it does not break when FFA updates internals.

### Behaviour

- Water is surface-only: it extinguishes the block a spray lands on, not blocks
  behind it. FFA's rays stop at the first solid block regardless, so nothing
  deeper was ever reachable.

- Nothing is ever deleted to put it out. Burning blocks convert to their burnt
  variants with block properties preserved, and Burnt's world flame counter
  stays in sync.

- Burnt variants from other mods are recognised automatically when they follow
  Burnt's `smoldering_X` to `burnt_X` naming convention.
