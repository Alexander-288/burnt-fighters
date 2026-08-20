# Burnt Fighters

A NeoForge 1.21.1 compatibility mod that lets [Create](https://modrinth.com/mod/create)'s
fluid machinery — and [Create: FireFighting Additions](https://modrinth.com/mod/create-firefighting-additions)'
nozzles — actually put out fires from [Burnt Basic](https://modrinth.com/mod/burnt-basic).

Every dependency is optional. With none of them installed the mod is inert.

## What it does

- **Create spout** loaded with water extinguishes a Burnt fire beneath it.
- **FireFighting Additions nozzles** extinguish Burnt fires anywhere in the
  spray cone, including the many block types their built-in bridge misses.

## The two extinguish procedures

Burnt exposes no API, so extinguishing means reflecting into its procedure
classes. There are two, and picking the right one is the whole problem:

| | `ExtinguishProcedure` | `ExtinguishBlockProcedure` |
|---|---|---|
| Scope | one block | sweeps 6×6×6, offset −3..+2 |
| Used by | Burnt's bare-hand extinguish | Burnt's own extinguisher spray |
| Doors, trapdoors | ✗ | ✓ |
| Campfires (burnt, ember) | ✗ | ✓ |
| Sails, sail frames | ✗ | ✓ |
| Fire barrels | ✗ | ✓ |
| Crops, cave vines, envelopes | ✗ | ✓ |
| Smoldering coal | ✗ | ✓ |
| Bamboo, leaf piles, DT branches | ✗ | ✓ |

The first is a strict subset of the second. FireFighting Additions' own
`integration/burnt/BurntCompat` calls the first one, which is why spraying a
burning door, campfire or sail with a nozzle does nothing at all. This mod
calls the second — the same one Burnt's own extinguisher projectile uses on
block hit.

Note also that the correct procedure is **not** a single-block call. It sweeps
216 blocks per invocation, so callers must rate-limit; see below.

## Why not just delete the fire blocks?

Burnt's `burnt:on_fire` tag is not a tag of flames. It contains *structural
blocks that are burning* — smoldering doors, stairs, logs, leaves — and
`burnt:fire` contains smoldering crops and vines.

Removing those would delete the player's burning house rather than extinguish
it. Burnt's procedures instead convert each one back to its burnt variant
(`smoldering_hay` → `burnt_hay`, and so on) and decrement the world-level flame
counter that drives Burnt's fire spread. Nothing in this mod touches blocks
directly.

## Integration approach

FireFighting Additions exposes a real extension point —
`api.nozzle.NozzleSprayInteractionRegistry` — which is notified for every block
a spray sample reaches, before its own extinguish pass runs. This mod registers
a callback there. No mixin, no coremodding, nothing that breaks when that mod
updates.

The callback is documented as additive and cannot cancel built-in effects,
which is fine: it runs first, so by the time FFA's own bridge examines the
block we have already converted it and its call is a no-op.

Because the correct procedure sweeps 216 blocks and a nozzle reports hundreds
of blocks per tick, each hit is snapped to a coarse grid (`BurntExtinguish.anchorFor`)
and at most one sweep runs per grid cell per tick. The grid step is smaller than
the sweep extent so cells overlap and nothing falls between two sweeps.

## Building

```bash
./gradlew build
```

Output lands in `build/libs/`.

## Local reference material

`burnt-decompiled/`, `external/`, and the loose `*.jar` / `*.txt` files in the
project root are decompiled sources and inspection dumps of other authors' mods,
kept locally for reference. They are gitignored deliberately — Burnt Basic is
All Rights Reserved and is not ours to redistribute.

## License

MIT
