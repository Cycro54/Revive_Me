Shtuff to do now
* ~Remove the /r in the Self Revive Options list config, causes the config to reload endlessly~
* ~Remove Instakill Msg~
* ~Fix double damage death issue with the MythicMobs mod~
* ~(Config) Chance will not instantly kill you anymore, instead it will refresh your self revive options.~
* ~(Config) Option to stop random item sacrifice from taking hotbar items.~
* ~Fix circle render not rendering correctly for fall screen death timer for versions 1.18.2 and up~
* ~Stop Revive Me! sounds if player is dead~
* ~Penalty Item Data config needs to check for {}~
* ~Fix Language localization issue~
* ~Add ability to invert certain Config blacklists to whitelists and vice-versa~
* ~Fix keybinds not being stopped correctly on newer versions of Minecraft 1.18.2 and up~
* ~Fix Kill timer not updating correctly~
* ~Fix player revive not working~
* ~Make player glow when in the fallen state~
* ~Get the YSM (Yes! Steve Model) mod to work with my mod (1.18.2 and up)~
* ~Fix black background for when witnessing someone revive someone else~
* ~Fix item data config not working~
* ~1.16.5 and up Fix Keybind mixin issues (I have to check more of the regular methods in the Keybinding class)~
* ~1.19.2 and up (Invocore) and Revive Me! need to fix keybind initialization (Make sure the revive me keybind shows up in the controls screen)~
* Bug in 1.21.1: When you are a revived and you haven't let go of a button before you are downed, it will keep holding that button when you get revived.
* Add the ModPublisher gradle plugin to all versions for easier upload to Curseforge and Modrinth (Don't directly add the API token!): https://modpublisher.fdd-docs.com/
* Fix kill method in FallenCapability for all versions except 1.20.1 (fixed in there), player's don't gain credit for killing players in the fallen state
* Make it so players in the fallen state can't eat (mixin)
* When adding items to the sacrificial items list inside FallenCapability, copy the items instead of adding them directly.

Shtuff to do next large update
* Make it so you can change what effects you revive with
* (Config) Option to stop the premature removal of the fallen penalty timer effect.
* (Config) Add option to kill player even when they are revived, but to respawn them exactly where they die
* (Config) Add a way for certain types of damage sources to bypass my mod besides invulnerability
* Add in a Custom Pose for the fallen state
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

Shtuff to maybe do
* If more people ask for revive items with customizable options, I might add it: https://github.com/Cycro54/Revive_Me/issues/33
* Also check this too for revive items: https://github.com/Cycro54/Revive_Me/issues/35
* Add stats to statistic page for Revive me
* (Config) If your timer runs out, instead of outright dying, you will be targetable by mobs.
