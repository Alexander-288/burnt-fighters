# Store listing copy

Paste-ready text for CurseForge and Modrinth. Keep in sync with the README.

---

## Summary / short description

> Makes Create and Create: FireFighting Additions actually put out Burnt Basic
> fires — including the burning doors, campfires, sails and fire barrels that
> currently ignore water entirely.

CurseForge caps the summary around 255 characters; Modrinth around 256. The
above fits both.

---

## Full description

### What it does

Burnt Basic adds a fire that spreads through your build. Create and Create:
FireFighting Additions add machinery to fight fires. They do not fully
understand each other. This mod is the bridge.

**Create** — a spout running water extinguishes a Burnt fire beneath it,
250 mB per block.

**Create: FireFighting Additions** — nozzles now extinguish Burnt fires that
previously did nothing at all when sprayed:

- burning doors and trapdoors
- burning and ember campfires
- sails and sail frames
- lit fire barrels, which defuse back to an inert barrel
- crops, cave vines and envelopes
- smoldering coal, which recovers to a full coal block
- bamboo and leaf piles
- soot, washed off by water

### Why those did not work

FireFighting Additions ships its own Burnt bridge, but it calls Burnt's
`ExtinguishProcedure` — the reduced, single-block routine behind bare-hand
extinguishing. Burnt's own extinguisher spray calls `ExtinguishBlockProcedure`,
which handles considerably more. Everything in the list above is in the gap
between the two.

This mod dispatches to the correct procedure for each block type.

### Nothing gets deleted

Burnt's `on_fire` tag is not a tag of flames — it contains structural blocks
that happen to be burning. Extinguishing them by removing them would delete the
burning parts of your build.

Every conversion here goes through Burnt's own routines, so burning blocks turn
into their burnt variants with properties preserved, and Burnt's world-level
flame counter stays in sync.

### Water is surface-only

Water extinguishes the block a spray lands on, not blocks behind it. Nothing is
lost by this — FireFighting Additions' rays stop at the first solid block
regardless, so deeper blocks were never reachable.

A foam fluid that soaks into material is planned.

### Compatibility

- Works inside Sable sublevels. Nothing here depends on level identity.
- Server-side only. Clients do not need it installed to join.
- Burnt variants added by other mods are picked up automatically when they
  follow Burnt's `smoldering_X` to `burnt_X` naming convention.

### Requirements

- **Burnt Basic** — required
- **Create** — optional, enables the spout integration
- **Create: FireFighting Additions** — optional, enables the nozzle integration

Unofficial. Not affiliated with or endorsed by the authors of any mod it
integrates with.

Source: https://github.com/Alexander-288/burnt-fighters (MIT)

---

## Project settings

### CurseForge

| Field | Value |
|---|---|
| Category | Addons (secondary: Technology) |
| Mod loader | NeoForge |
| Game version | 1.21.1 |
| License | Custom — link to the repo LICENSE |

Relationships:

| Project | Type |
|---|---|
| Burnt Basic | Required dependency |
| Create | Optional dependency |
| Create: FireFighting Additions | Optional dependency |

### Modrinth

| Field | Value |
|---|---|
| Environment — client | Optional |
| Environment — server | Required |
| License | MIT |
| Categories | Utility, Technology |
| Loader | NeoForge |
| Game version | 1.21.1 |

Dependencies: same three, same types.

The client/server split matters. This mod is pure server-side logic, and
marking it client-required makes it look wrongly incompatible to modpack
tooling.
