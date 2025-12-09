Shtuff to do now
* Bug in 1.21.1: When you are a revived and you haven't let go of a button before you are downed, it will keep holding that button when you get revived.
* Add the ModPublisher gradle plugin to all versions for easier upload to Curseforge and Modrinth (Don't directly add the API token!): https://modpublisher.fdd-docs.com/
* ~Fix kill method in FallenCapability for all versions except 1.20.1 (fixed in there), player's don't gain credit for killing players in the fallen state~
* ~Make it so players in the fallen state can't eat (mixin)~
* ~When adding items to the sacrificial items list inside FallenCapability, copy the items instead of adding them directly.~
* ~Fix: Player can't eat while another player is down nearby (probably because the mod thinks the living player is trying to revive the downed player)~
* ~Fix: players can fly while in the fallen state (Mekanism is causing this, I can probs fix it)~

Shtuff to do next large update
* Make it so you can change what effects you revive with
* (Config) Option to stop the premature removal of the fallen penalty timer effect.
* (Config) Add option to kill player even when they are revived, but to respawn them exactly where they die
* (Config) Add a way for certain types of damage sources to bypass my mod besides invulnerability
* Fix the Revive Me fail command text (The text that shows isn't correct.)
* Maybe add a config that makes it so if the regular death event runs first, all of the listeners will at least know if the Death event is canceled with my mod
* Player appears to be crouching after being revived, they shouldn't be
* Make it so RANDOM ITEMS takes Hotbar items by default, or make it obvious that it won't take Hotbar
* When using self revive options from killing you if canGiveUp is set to false
* The config comment "#How long the Help call effects will last in SECONDS" has a mistake, must fix!
* Change Destroy mobs default timer to 30 from 20 seconds
* Add in auto help call config
* Change hotbar item sacrifice config to where it checks your inventory first before checking your hotbar.
* Allow players to have more than one way to REVIVE others (instead of only FOOD, it can be FOOD, HEALTH, XP, etc.)
* Add abiltiy do dispel the Mob Kill Effect Timer
* Be able to see what revives and downs a player (when revived, when entering fallen state)

Shtuff to maybe do
* If more people ask for revive items with customizable options, I might add it: https://github.com/Cycro54/Revive_Me/issues/33
* Also check this too for revive items: https://github.com/Cycro54/Revive_Me/issues/35
* Add stats to statistic page for Revive me
* (Config) If your timer runs out, instead of outright dying, you will be targetable by mobs.
* Add a custom Pose for the fallen state without using a custom renderer, customize STANDING pose
