# Burnt Fighters

A NeoForge 1.21.1 compatibility mod that lets [Create](https://modrinth.com/mod/create)'s
fluid machinery fight fires from [Burnt Basic](https://modrinth.com/mod/burnt-basic).

Both dependencies are optional. With neither installed the mod is inert.

## What it currently does

A Create **spout** loaded with water extinguishes a Burnt fire directly beneath
it, consuming 250 mB per block.

## How extinguishing works

Burnt's fire is not just flame blocks. Its `burnt:on_fire` tag contains
*structural blocks that are burning* — smoldering doors, stairs, logs, leaves —
and its `burnt:fire` tag contains smoldering crops and vines.

Those must not simply be removed. Burnt's own `ExtinguishProcedure` converts
them back into their burnt variants (`smoldering_hay` → `burnt_hay`, and so on)
and decrements the world-level flame counter that drives Burnt's fire spread.
Deleting the blocks instead would destroy player builds and permanently desync
that counter.

So `BurntExtinguishProcedure` reflects into Burnt's procedure rather than
touching blocks itself, and every extinguish path in this mod goes through it.
No Burnt, no procedure, no effect.

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

## Status

The integration with
[Create: FireFighting Additions](https://modrinth.com/mod/create-firefighting-additions)
is being rebuilt. Note that mod already ships its own Burnt bridge
(`integration/burnt/BurntCompat`), which calls the same `ExtinguishProcedure`,
so any future work here needs to complement it rather than pre-empt it.

## License

MIT
