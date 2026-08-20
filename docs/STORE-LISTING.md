# Store listing copy

Paste-ready for CurseForge and Modrinth. Both render Markdown.

---

## Summary (185 chars)

Makes Create and Create: FireFighting Additions actually put out Burnt Basic fires — including the burning doors, campfires, sails and fire barrels that currently ignore water completely.

---

## Full description

# Burnt Fighters

**Burnt Basic** sets your world on fire. **Create** and **Create: FireFighting
Additions** give you the machinery to fight it.

They don't fully understand each other. Point a nozzle at a burning door and
nothing happens. This mod is the missing bridge.

## What it fixes

Spray these with a FireFighting Additions nozzle and, right now, nothing happens
at all. With Burnt Fighters installed, they go out:

- Burning **doors** and **trapdoors**
- Burning and ember **campfires**
- **Sails** and sail frames
- Lit **fire barrels** — defused back to an inert barrel before they go off
- **Crops**, **cave vines** and **envelopes**
- **Smoldering coal** — recovers to a full coal block
- **Bamboo** and **leaf piles**
- **Soot**, washed clean by water

It also adds **Create spout** support: a spout running water puts out a Burnt
fire beneath it for 250 mB, so you can build automated suppression.

## Why they didn't work

FireFighting Additions already ships a Burnt bridge — but it calls Burnt's
single-block extinguish routine, the one behind putting fires out by hand.
Burnt's own extinguisher spray calls a different, much more complete routine.
Everything in that list above falls into the gap between the two.

Burnt Fighters routes each block to the right routine.

## Your build survives

Burnt's "on fire" blocks aren't flames — they're *your walls, doors and beams,
currently alight*. Putting them out by deleting them would take your house with
them.

Every conversion here goes through Burnt's own logic, so burning blocks become
their burnt variants with orientation and state preserved, and Burnt's internal
fire tracking stays consistent. Nothing is ever destroyed to extinguish it.

## Water reaches the surface

Water puts out what the spray actually lands on, not blocks hidden behind it.
Nothing is lost by this — nozzle spray already stops at the first solid block.

A **foam** fluid that soaks deeper into burning material is planned.

## Compatibility

- **Server-side only.** Clients don't need it installed to join a server.
- **Works inside Sable sublevels**, so airships and moving contraptions are fine.
- **Other mods' burnt variants** are picked up automatically when they follow
  Burnt's `smoldering_X` → `burnt_X` naming.

## Requirements

| Mod | |
|---|---|
| Burnt Basic | **Required** |
| Create | Optional — enables spout support |
| Create: FireFighting Additions | Optional — enables nozzle support |

You get whichever integrations you have mods for.

---

Unofficial. Not affiliated with or endorsed by the authors of the mods it
integrates with.

Credit to **Pixelbank** for Burnt Basic and **Mikoalopex** for Create:
FireFighting Additions.

Source (MIT): https://github.com/Alexander-288/burnt-fighters

---

## Project settings

### CurseForge

| Field | Value |
|---|---|
| Category | Addons (secondary: Technology) |
| Mod loader | NeoForge |
| Game version | 1.21.1 |
| License | Custom — link the repo LICENSE |

Relationships: Burnt Basic = Required Dependency. Create = Optional Dependency.
Create: FireFighting Additions = Optional Dependency.

### Modrinth

| Field | Value |
|---|---|
| Environment — client | **Optional** |
| Environment — server | **Required** |
| License | MIT |
| Categories | Utility, Technology |
| Loader | NeoForge |
| Game version | 1.21.1 |

Dependencies: same three, same types.

The client/server split matters — marking this client-required makes it look
wrongly incompatible to modpack tooling.
