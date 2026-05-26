Shtuff to do now
* ~Bug in 1.21.1: When you are a revived and you haven't let go of a button before you are downed, it will keep holding that button when you get revived.~
* ~Add the ModPublisher gradle plugin to all versions for easier upload to Curseforge and Modrinth (Don't directly add the API token!):~ https://modpublisher.fdd-docs.com/
* ~Fix kill method in FallenCapability for all versions except 1.20.1 (fixed in there), player's don't gain credit for killing players in the fallen state~
* ~Make it so players in the fallen state can't eat (mixin)~
* ~When adding items to the sacrificial items list inside FallenCapability, copy the items instead of adding them directly.~
* ~Fix: Player can't eat while another player is down nearby (probably because the mod thinks the living player is trying to revive the downed player)~
* ~Fix: players can fly while in the fallen state (Mekanism is causing this, I can probs fix it)~
  
Shtuff to do next large update
* ~Revive items with customizable options: (https://github.com/Cycro54/Revive_Me/issues/33), (https://github.com/Cycro54/Revive_Me/issues/35)~
* ~Make it so you can change what effects you revive with~
* ~(Config) Option to stop the premature removal of the fallen penalty timer effect.~
* ~(Config) Pause fallen Timer on DC~
* ~Fix the Revive Me fail command text (The text that shows isn't correct.)~
* ~Player appears to be crouching after being revived, they shouldn't be~
* ~Make it so player knows if check hotbar is turned off when sacrificing items~
* ~Stop self revive options from killing you if canGiveUp is set to false~
* ~The config comment "#How long the Help call effects will last in SECONDS" has a mistake, must fix!~
* ~Change Destroy mobs default timer to 30 from 20 seconds~
* ~Config for if the Death Timer is dispellable or not~
* ~Be able to see what revives and downs a player (when revived, and when entering fallen state)~
* ~Add a entity blacklist(which can also be made into a whitelist) for the Kill Revive Effect (so players can't kill chickens and call it a day), (be able to block/allow classes,and MODIDs) (https://github.com/Cycro54/Revive_Me/issues/46)~
* ~Make a blacklist for revertable effects and add kill timer effect to it.~
* ~(Config) If your timer runs out, instead of outright dying, you will be able to take damage instead~
* ~sneak R to toggle auto call~
* ~(config) add option to turn off bold text~

Shtuff to maybe do later
* ~Make sure people know that you can use Revive Items and a seconday penalty item when reviving someone. Set to ITEM to only allow Revive items and set Item_User to FALLEN or NONE and change penalty type to ITEM to disable other player revive. (Maybe I'll make it simpler later...~
* ~Revive Effects for Revive items don't inherit from Revive_Effects config by default, make sure that is known in Revive Items Example~
* Data pack support for items and attributes (if I ever add attributes)
* Attribute creation/modification (https://github.com/Cycro54/Revive_Me/issues/45)
* Add stats to statistic page for Revive me
* (Probs not adding, someone else can if they want) Think about making a custom resource pack or something, (change the way the player looks when they are down, change a couple textures n stuff maybe??) (Look at Posture mod: https://www.curseforge.com/minecraft/mc-mods/posture)
* ~Add compatibility with MrCrayfishs controller mod~
* (Config) Add a way for certain types of damage sources to bypass my mod besides invulnerability
* (Config) Add option to kill player even when they are revived, but to respawn them exactly where they die
* ~(Config) Maybe add a config that makes it so if the regular death event runs first, all of the listeners will at least know if the Death event is canceled with my mod~ (ONLY DO IF ASKED)
* ~Max revives config (max amount of times you can be revived, 0 disables revive me, -1 disables the max), max player revives config (how many times another player can revive you, setting to 0 disables player revive, -1 disables the max)~
* ~Revive radius config (If can't self revive, the max distance a teammate can be before instant death (0 disables this)~
* Disable/Enable Revive Me for hard-core mode?
* ~Hide potion effects?~
* ~Stop fallen players from picking up items?~
* ~Make the Time_Left config be able to go to -1 to disable it. 0 will either instakill or make you targetable~

[  ] Add a new keybind that allows you to disable the HUD and HUD keybinds
[  ] Ability to give reviver effects after reviving someone

