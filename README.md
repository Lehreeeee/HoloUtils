
# HoloUtils

HoloUtils is a Minecraft plugin that provides several utilities for HoloCraft Developers to ease their work. 
The plugin also comes with some small custom features like player title display to prevent making 1 plugin for every small feature.
Custom bug fixes for other will also be implemented in this plugin.

This plugin is primarily designed specifically for [**HoloCraft Minecraft Server**](https://wiki.holocraft.xyz/). Most features are developed to suit the specific needs of our server, and functionality may not be universally applicable to other environments. While some features may be useful outside of HoloCraft, this plugin is not intended for general-purpose use.

## Features
### Network Developer Chat (via Redis)
Enables developers across all servers in the network to communicate with each other using a shared Redis server. Writing `[item]` in a message displays the item in the sender’s main hand.

### Player Title Display
Displays a title above a player's head. Commonly used for showcasing titles earned by completing challenges or participating in events.

### Mob Status Effect Display
Temporarily displays a status symbol above a mob’s head to indicate debuffs.

### Arrow Pass-Through Mechanic
Allows arrows shot by players to pass through other players in designated worlds. This is especially useful during multiplayer boss fights.

### Item PDC Command Utility
Provides commands to get, set, or remove Persistent Data Container (PDC) values from the item in the player’s main hand.  
**Supported data types:** `STRING`, `INTEGER`, `FLOAT`, `DOUBLE`, `LONG`, `BYTE`.

### MMOItems Random Stats Reroll
Allows players to reroll the random stats on MMOItems.  
**Supported requirements for rerolling:**
- Item consumption (MMOItem)
- Vault balance
- Recurrency from RoyaleEconomy

### Network Rewards (via MySQL)
Implements a cross-server reward system where developers can assign rewards on an Event Server, and players can claim them from other servers in the network.

### Damage Leaderboard
Integrated from [MMLeaderboard](https://github.com/Lehreeeee/MMLeaderboard), 
this feature records player damage to mobs that are being tracked (With commands). 
Also supports linking child mob to parent mob so damage dealt to child will be added to it's parent's leaderboard.

Includes PlaceholderAPI (PAPI) placeholders:
```
<uuid> - UUID of mob being tracked | <position> - position of the entry

- %holoutils_damagelb_entry_{<uuid>}_{<position>}% - Returns player name of the entry
- %holoutils_damagelb_damage_{<uuid>}_{<position>}% - Returns player damage of the entry, with percentage appended.
- %holoutils_damagelb_damage_{<uuid>}_{<position>}_simple% - Returns player damage of the entry
- %holoutils_damagelb_damagep_{<uuid>}% - Returns damage of the player that see this placeholder, with percentage appended.
- %holoutils_damagelb_damagep_{<uuid>}_simple% - Returns damage of the player that see this placeholder
- %holoutils_damagelb_duration_{<uuid>}% - Returns the fight duration of this leaderboard
```

## Bug Fixes Implemented
- Certain plugins in the server are causing mob to be alive and immortal even after death, this is fixed by force removing the immortal mob if they have >0 Health after death event is fired.

(Limitation: `- cancelevent{sync=true} @self ~onDeath` can no longer be used in MythicMobs as this option will still force remove the mob)

## Upcoming Features
~~- Network rewards either using MySQL or Redis. Allows developers to give rewards to players in Event Server and players can choose to claim from any other server in the network.~~
- Usage of placeholder in player title display or mob status effect display.
- Animation in player title display.

## Possible Future Updates (Unconfirmed)
- Automatically update outdated configuration files.
- Network developer storage, to store items that can be taken out in another server for testing or whatever purposes.

## Dependencies
- **PlaceholderAPI** [Link here](https://www.spigotmc.org/resources/placeholderapi.6245/) (Soft Depend - Needed to use placeholders in player title display or mob status effect display and damage leaderboard info)
- **MMOItems** [Link here](https://www.spigotmc.org/resources/mmoitems.39267/) (Soft Depend - Needed to enable /reroll commands)
- **ModelEngine4** [Link here](https://mythiccraft.io/index.php?resources/model-engine%E2%80%94ultimate-entity-model-manager-1-19-4-1-21-1.1213/)  (Soft Depend - Will try to put status display tag on specific bone if the mob has a model)
- **MythicMobs** [Link here](https://mythiccraft.io/index.php?resources/mythicmobs.1/) (Soft Depend)

## Other tools
- **MySQL** (Required for network rewards)
- **Redis** (Required for network developer chat)