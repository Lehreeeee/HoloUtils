
# HoloUtils

HoloUtils is a Minecraft plugin that provides several utilities for HoloCraft Developers to ease their work. 
The plugin also comes with some small custom features like player title display to prevent making 1 plugin for every small feature.
Custom bug fixes for other will also be implemented in this plugin.

This plugin is primarily designed specifically for [**HoloCraft Minecraft Server**](https://discord.gg/MaZzQpCCqf). Most features are developed to suit the specific needs of our server, and functionality may not be universally applicable to other environments. While some features may be useful outside of HoloCraft, this plugin is not intended for general-purpose use.

## Features
- Network Developer Chat using Redis Server, allowing developer from any server in the network to communicate with each other.
- Player title display, can be used to display title on top of player's head. (Mainly used for title achieved by completing some challages or participating in some events)
- Mob status effect display, can be used to temporarily display status symbol on top of mob's head.
- Allow player's arrow to go through other players in some worlds, useful for multiplayer boss fight.
- Commands to get/set/remove data in item's PDC in main hand. (Supported datatypes: STRING, INTEGER, FLOAT, DOUBLE, LONG, BYTE)
- MMOItem's random stats reroll (Supported reroll requirements: MMOItem, Vault Balance and Recurrency from RoyaleEconomy)

## Bug Fixes Implemented
- Certain plugins in the server are causing mob to be alive and immortal even after death, this is fixed by force removing the immortal mob if they have >0 Health after death event is fired.

(Limitation: `- cancelevent{sync=true} @self ~onDeath` can no longer be used in MythicMobs as this option will still force remove the mob)

## Upcoming Features
- Network rewards either using MySQL or Redis. Allows developers to give rewards to players in Event Server and players can choose to claim from any other server in the network.
- Usage of placeholder in player title display or mob status effect display.
- Animation in player title display.

## Possible Future Updates (Unconfirmed)
- Automatically update outdated configuration files
- Implement our own MMOInventory because its development/update is too slow and i need to add some custom features :amewtf:
- Network developer storage, to store items that can be taken out in another server for testing or whatever purposes.

## Dependencies
- **PlaceholderAPI** [Link here](https://www.spigotmc.org/resources/placeholderapi.6245/) (Soft Depend - Needed to use placeholders in player title display or mob status effect display)
- **MMOItems** [Link here](https://www.spigotmc.org/resources/mmoitems.39267/) (Soft Depend - Needed to enable /reroll commands)
- **ModelEngine4** [Link here](https://mythiccraft.io/index.php?resources/model-engine%E2%80%94ultimate-entity-model-manager-1-19-4-1-21-1.1213/)  (Soft Depend - Will try to put status display tag on specific bone if the mob has a model)
  
