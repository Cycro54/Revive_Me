Shtuff to do now
* ~Bug in 1.21.1: When you are a revived and you haven't let go of a button before you are downed, it will keep holding that button when you get revived.~
* ~Add the ModPublisher gradle plugin to all versions for easier upload to Curseforge and Modrinth (Don't directly add the API token!):~ https://modpublisher.fdd-docs.com/
* ~Fix kill method in FallenCapability for all versions except 1.20.1 (fixed in there), player's don't gain credit for killing players in the fallen state~
* ~Make it so players in the fallen state can't eat (mixin)~
* ~When adding items to the sacrificial items list inside FallenCapability, copy the items instead of adding them directly.~
* ~Fix: Player can't eat while another player is down nearby (probably because the mod thinks the living player is trying to revive the downed player)~
* ~Fix: players can fly while in the fallen state (Mekanism is causing this, I can probs fix it)~
  
Shtuff to do next large update
* Revive items with customizable options: (https://github.com/Cycro54/Revive_Me/issues/33), (https://github.com/Cycro54/Revive_Me/issues/35) (4-10 hours)
* ~Make it so you can change what effects you revive with (2 hours 40 min)~
* ~(Config) Option to stop the premature removal of the fallen penalty timer effect. (30 min.)~
* ~(Config) Pause fallen Timer on DC (30 MIN)~
* ~Fix the Revive Me fail command text (The text that shows isn't correct.)~
* ~Player appears to be crouching after being revived, they shouldn't be~
* ~Make it so player knows if check hotbar is turned off when sacrificing items (30 min.)~
* ~Stop self revive options from killing you if canGiveUp is set to false (15-30 min.)~
* ~The config comment "#How long the Help call effects will last in SECONDS" has a mistake, must fix! (5 min)~
* ~Change Destroy mobs default timer to 30 from 20 seconds~
* ~Config for if the Death Timer is dispellable or not (15 min.)~
* ~Be able to see what revives and downs a player (when revived, and when entering fallen state) (15 min.)~
* Add a entity blacklist(which can also be made into a whitelist) for the Kill Revive Effect (so players can't kill chickens and call it a day), (be able to block/allow classes,and MODIDs) (https://github.com/Cycro54/Revive_Me/issues/46) (30 min)
* Make a blacklist for revertable effects and add kill timer effect to it. (30 min.)
*  (Config) If your timer runs out, instead of outright dying, you will be able to take damage instead

Shtuff to maybe do
* sneak R to toggle auto call (30 min.)
* Add stats to statistic page for Revive me
* Think about making a custom data pack or something, (change the way the player looks when they are down, change a couple textures n stuff maybe??) (Look at Posture mod: https://www.curseforge.com/minecraft/mc-mods/posture)
* Have a way to extend your timer, (using items or something)
* Attribute creation/modification (https://github.com/Cycro54/Revive_Me/issues/45)
* ~Add compatibility with MrCrayfishs controller mod~
* (Config) Add a way for certain types of damage sources to bypass my mod besides invulnerability
* (Config) Add option to kill player even when they are revived, but to respawn them exactly where they die
* Maybe add a config that makes it so if the regular death event runs first, all of the listeners will at least know if the Death event is canceled with my mod
