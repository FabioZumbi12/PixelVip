![logo](http://image.prntscr.com/image/e17f1d36af9f4e34b59b5dde7b35b4d9.jpeg)  
![release](https://img.shields.io/github/v/tag/FabioZumbi12/PixelVip?label=version)  
PixelVip is a VIP management plugin for Minecraft servers. It focuses on easy configuration, flexible VIP groups, and a full set of commands for staff and players.

## Highlights
- VIP keys (normal and unique) with optional usage limits
- VIP groups with commands, command chances, and finish actions
- Player VIP time lookup and active VIP switching
- Packages system with optional variants and click-to-select messages
- Offline command queue (run on next join)
- Vault and Essentials integration
- PlaceholderAPI support
- Bungee synchronization (optional)
- Folia-aware scheduling (uses region/entity scheduler when available)

## Compatibility
- Spigot/Paper (API 1.13+)
- Folia (uses region/entity scheduler when available)

## Requirements
- Java 17
- Vault (required)
- Permissions plugin (LuckPerms, PermissionsEx, etc.)
- Optional: Essentials, PlaceholderAPI, WooCommerce integration

## Installation
1) Drop the jar into your `plugins/` folder  
2) Start the server to generate configs  
3) Edit `config.yml`, `vips.yml`, `keys.yml`, and `packages.yml`  
4) Reload with `/pixelvip reload`

## Main features
- **VIP Groups**: configure commands on activation, finish, and change  
- **Keys**: generate keys for VIPs and items, with unique key support  
- **Packages**: give packages with multiple variants  
- **Database**: file or MySQL storage  
- **Payments**: optional WooCommerce order check and delivery

## Commands (default)
- `/pixelvip reload|fileToMysql|mysqlToFile`
- `/newkey <vip> <days> [uses]`
- `/newukey <vip> <days>`
- `/sendkey <player> <key>|<vip> <days> [uses]`
- `/usekey <key>`
- `/viptime [player]`
- `/setactive <vip>`
- `/addvip <player> <vip> <days>`
- `/setvip <player> <vip> <days>`
- `/removevip <player> [vip]`
- `/listvips`
- `/newitemkey <cmd1, cmd2, ...>`
- `/additemkey <key> <cmd>`
- `/listkeys`
- `/delkey <key>`
- `/givepackage <player> <id>`
- `/getvariant <variant>`
- `/listpackages`
- `/addpackage <id> hand|command [cmd1, cmd2]`
- `/delpackage <id>`

## Permissions
**Groups**
- `pixelvip.normal` (default: true)  
  - `pixelvip.cmd.player`
- `pixelvip.admin` (default: op)  
  - `pixelvip.cmd.player`  
  - `pixelvip.cmd.player.others`  
  - `pixelvip.cmd.newkey`  
  - `pixelvip.cmd.sendkey`  
  - `pixelvip.cmd.delkey`  
  - `pixelvip.cmd.newitemkey`  
  - `pixelvip.cmd.additemkey`  
  - `pixelvip.cmd.listkeys`  
  - `pixelvip.cmd.removevip`  
  - `pixelvip.cmd.addvip`  
  - `pixelvip.cmd.setvip`  
  - `pixelvip.cmd.listvips`  
  - `pixelvip.cmd.givepackage`

**Per-command**
- `pixelvip.cmd.pixelvip`
- `pixelvip.cmd.newkey`
- `pixelvip.cmd.newukey`
- `pixelvip.cmd.sendkey`
- `pixelvip.cmd.delkey`
- `pixelvip.cmd.listkeys`
- `pixelvip.cmd.newitemkey`
- `pixelvip.cmd.additemkey`
- `pixelvip.cmd.player`
- `pixelvip.cmd.player.others`
- `pixelvip.cmd.setactive`
- `pixelvip.cmd.addvip`
- `pixelvip.cmd.setvip`
- `pixelvip.cmd.removevip`
- `pixelvip.cmd.listvips`
- `pixelvip.cmd.givepackage`
- `pixelvip.cmd.getvariant`
- `pixelvip.cmd.listpackages`
- `pixelvip.cmd.addpackage`
- `pixelvip.cmd.delpackage`

## Links
Available versions:  
Spigot: https://www.spigotmc.org/resources/pixelvip.30438/  
Bukkit: https://dev.bukkit.org/projects/pixelvip  
Sponge: https://ore.spongepowered.org/FabioZumbi12/PixelVip  

Wiki: https://github.com/FabioZumbi12/PixelVip/wiki  
Source: https://github.com/FabioZumbi12/PixelVip  

Releases:  
https://github.com/FabioZumbi12/PixelVip/releases
