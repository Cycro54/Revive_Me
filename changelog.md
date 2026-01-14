# 1.20.1-4.5.4

<div><pre><strong>Fixes</strong><br>-Kill Revive now checks if source entity is a Living Entity</pre></div>

# 1.20.1-4.5.2

<div><div><pre><strong>Additions</strong><br>-Refresh_Item_List config: Refresh revive item list while in the fallen state<br><br><strong>Fixes</strong><br>-Gave revive commands higher permission<br>-Checks if reviver is present before applying item cooldown</pre></div></div>

# 1.20.1-4.5.1

<div><pre><strong>Fixes</strong><br>-Category translation key</pre></div>

# 1.20.1-4.5.0

<div><pre><strong>Additions</strong><br>-Revive item system complete with examples and tooltips (took the most time...)<br>-Overheal config: Revive fallen players by healing them<br>-Controllable compatibility<br>-/revivemereload command for Config and Items<br>-Die_When_Timer_Ends config: Makes fallen players targetable if set to false<br>-Fallen_Health config: Fallen players can now heal and have a max amount of health<br>-Pause_Fallen_Timer_On_Disconnect config: if the fallen timer should pause on Disconnect<br>-Revive_Kill_Blacklist config: Entities glow black if blacklisted, green if not<br>-Revive_Effects config: What effects you revive with (unless you use items)<br>-Revert_Effect_Blacklist config: What effects will be blocked/allowed on revive<br>-Can_Remove_Penalty_Timer config: If penalty timer can be removed before it expires<br>-Added 3 keybinds: left option, right option, and tooltip combination<br><br><strong>Changes</strong><br>-Revive Me! config is now in reviveme folder<br>-Extended death and revive messages<br>-Revive call is toggleable<br>-Bold Text can be turned off<br>-Experience revive type now takes the correct amount of experience<br>-Removed Specific item from self revive and reviver penalty types<br>-Sound events adjusted<br><br><strong>Fixes</strong><br>-Items should no longer glitch out when reviving someone<br>-Blacklists/Whitelists should now properly block/allow things<br>-Lots of other things I can't remember...</pre></div>

# 1.20.1-4.4.10

<div><div><pre><strong>Changes</strong><br>-A reviver can no longer revive 2 people at once<br>-Fallen players are now only seen to be in creative while ticking through entities<br>-Adjustments to default self revive settings<br><br><br><strong>Fixes</strong><br>-Probably fixed pose issues<br>-Status effects self revive now shows the correct debuffs<br>-Players that aren't the host should be able to eat food now if the host enters the fallen state<br>-"Already revived" message now displays properly</pre></div></div>

# 1.20.1-4.4.7

<div><pre><strong>Fixes</strong><br>-Removing player effects should no longer crash the game<br>-Blocked keybinds should no longer be active when in the fallen state<br>-Players in the fallen state should no longer be allowed to eat</pre></div>

# 1.20.1-4.4.3

<div><pre><strong>Additions</strong><br>-Added easier way to publish mod to Modrinth and Curseforge<br><br><strong>Fixes</strong><br>-Gives proper credit to players that kill downed players</pre></div>

# 1.20.1-4.4.1

<div><pre><strong>Changes</strong><br>-Replaced Float.parseFloat with Math.round. Was causing issues of sorts...<br><br><strong>Fixes</strong><br>-Now checks and prevents even more Keybind methods while in the fallen state</pre></div>

# 1.20.1-4.4.0

<div><pre><strong>Additions</strong><br>-(Config) Revive Chance Kill on Fail Config: Kill player if they fail the Revive Chance<br>-Fallen players now glow<br>-(Config) Include_Hotbar_Items: If Random Item sacrifice checks hotbar too<br>-(Config) Show_Item_Name: enable/disable the display name for specific item<br><br><strong>Changes</strong><br>-(Config) Blocked Commands &amp; Harmful Effects Blacklist now can be turned into whitelists (by adding "//")<br>-(Config) Item Data and Specific Item Data now check if string is a valid Json NBT<br>-Removed InstaKillMsg.java<br>-Damage checks for my mod now starts on the hurtMethod, not isInvulnerableTo Method.<br>-Edited fallen noise conditions.<br>-Gave more visual hints to know when a self-revive choice will cause you to self-destruct<br>-Edited Self Revive Mob text<br><br><strong>Fixes</strong><br>-Config now reloads properly<br>-You can no longer die on the same tick that you enter the fallen state<br>-Config text for "Time_Left" has been corrected<br>-Green overlay for self revive no longer stretches forever<br>-Player now dies if they have no revives left, and it's singleplayer.<br>-null check when rendering negative status effects<br>-Reviving someone now shows for everyone nearby<br>-Revive sound event now plays correctly.<br>-Added null check for Item_Data and Penalty_Item_Data<br>-Blocked more keybind methods when in the fallen state<br>-Now checks if NBT item data configs are empty when checking for item data<br>-Chance now kills you if you run out of revives whilst in a singleplayer world<br>-en-us lang file now matches code changes for mod id</pre></div>

# 1.20.1-4.3.0

<div><pre><strong>Additions</strong><br>-Self Revive Specific Item: Sacrifice a specific item to revive<br>-Self Revive Kill: Kill enough mobs in time to fully revive<br>-Status Effects Self Revive: Gain Harmful effects on revive<br>-Experience Self Revive: Lose a portion of XP on revive<br>-Added configs for new Self revive options<br>-Config Allowed_Keybinds: Allowed Keybinds while in the fallen state<br>-Config Max_Self_revives<br>-Config Run_Death_Event_First: Run LivingDeathEvent first before this mod<br>-Config Cancel_Revive_On_Damage<br>-Config Disable_Self_Revive_On_PVP<br>-Config Randomize_Self_Revive_Options<br>-Config Only_Use_Available_Options: Only show self-revive options you can use (when available)<br>-Config Self_Penalty_Percentage: Self Revive Penalty that increases each time you self revive<br>-Config Die_On_Disconnect<br>-Config Sound_Level: Master control for Revive Me sounds<br><br><strong>Changes</strong><br>-Multiplayer Revive options are no longer tied to the player.<br>-Revive Me code will now run before or after Death event<br>-Reworked Self Revive UI<br>-Sounds are a bit louder<br>-Multiplayer Penalty item can now have NBT data<br>-Sorted Config options<br><br><strong>Fixes</strong><br>-Totem of Undying now works properly<br>-RestartDeathTimerMsg now checks if target player is null<br>-Now you stop using your item when reviving someone<br>-Double damage shouldn't kill the player anymore</pre></div>

# 1.20.1-4.2.1

**Additions**  
\-Added Revert\_Effects\_On\_Revive Config: Gain back potion effects you had before entering the fallen state

**Changes**  
\- Effects are no longer constantly being removed while in the dying state

**Fixes**  
\- Interact\_With\_Inventory config now properly limits inventory access  
\- Revive Penalty no longer shows when trying to kill a fallen player  
\- Dying in lava/fire no longer causes your armor to fully repair itself

# 1.20.1-4.1.5

**Fixes**  
\-Turns off self-revive buttons when opening a screen

**Changes**  
\-Sacrificial items now look for matching itemstacks instead of general items

# 1.20.1-4.1.4

<div><pre><strong>Changes</strong><br>-Changed the license to LGPLv3<br><br><strong>Fixes<br></strong>-Penalty timer effect now displays the correct name<br>-Keybinds now deactivate properly while in the dying state<br>-Drop keybind now works while in the dying state<br>-Modded items or events that use the vanilla ATTACK and USE keybind no longer work while in the dying state<br>-Unconventional modded keybinds no longer work while in the dying state</pre></div>

# 1.20.1-4.1.0

<div><pre><strong>Additions</strong><br>-Added sounds to reviving, fallen state, when fully revived, and when calling for help<br>-Added Call for Help button<br>-You can now stop Command/Regular chat messages from displaying through the config<br><br><strong>Changes</strong><br>-Decreased distance you can see a fallen player when they are not calling for help<br>-Now a revive chat message is sent when you revive someone normally<br><br><strong>Fixes</strong><br>-Mobs should now fully ignore you while in the fallen state<br>-No longer displays the wrong number while dying if your world is old<br>-Food level now updates on CLIENT when used to revive someone</pre></div>

# 1.20.1-4.0.10

<div><pre><strong>Fixes</strong><br>-You no longer die twice</pre></div>

# 1.20.1-4.0.9:

<div><pre><strong>Changes</strong><br>-Ported from 1.19.2 to 1.20.1<br>-Now uses only Invocore to render things</pre></div>