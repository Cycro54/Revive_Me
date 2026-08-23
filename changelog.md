# 1.16.5-1.18.1

**Fixes**
```
-Overkill correctly registers which damage sources are allowed now
-Damage sources that are suppose to bypass fallen state work again
```
# 1.16.5-1.18.0

**Additions**
```
-Give up on the main self-revive screen by holding the sneak and rightOption keybinds OR just hold rightOption keybind if there is only 1 self revive option.
-(config) Timer_Type - What happens when the timer ends, Targetable: Become targetable by mobs (does the opposite if Fallen_Is_Targetable is true), Death: Causes you to die, Revive: Uses self revive, if none left, uses player revive)
-(config) Fallen_Damage_Scale In/Out - Percentage of damage a fallen player takes/deals
-(config) Overkill_Amount - How much damage you have to take to be revived, 0 disables this feature, -1 is max health, Less than 1 is percentage.
-(config) Overkill_Penalty_Percentage - Increases overkill cost each time you are revived, stacks additively.
-(config) Overkill_Whitelist - What damage sources count towards overkill revive. Add "//" to make it a blacklist instead. Add ';Type;' to block/allow certain attackers. All types: ';NON_ENTITY;', ';NON_TEAM;', ';TEAM;', ';SELF;', ';MOB;', ';PLAYER;'. Type "/" to block/allow all damage sources
-(config) Can_Toggle_GUI - If a fallen player can toggle the GUI on and off
-(config) Fallen_Is_Targetable - If fallen players can be targeted by mobs (will do the opposite if timer hits 0)

```

**Changes**
```
-If self revive items is disabled, you can no longer open the Revive Item Screen.
-(config) Allowed_Keybinds - Add ';' to make the keybind only work when the revive gui is toggled off)
-(config) Die_When_Timer_Ends has been removed.
```

**Fixes**
```
-All config lists should work with Configured (mod) now
```
# 1.16.5-1.17.8

**Changes**
```
-Fallen Pose Sleep can now rotate with camera
-Time_left can now be set to 0 if you have Die_When_Timer_Ends set to false
-Run_Death_Event_First is now false by default (less compatibility issues this way)
```

**Fixes**
```
-Corpse mod no longer deletes all of your items on self-destruct
-Revive effects can now be hidden (again)
```
# 1.16.5-1.17.7

**Fixes**
```
-(Config) Kill_On_Fail works again
```
# 1.16.5-1.17.6

**Additions**

```
-(Config) Damage_Source_Whitelist - What damage sources that can bypass fallen state
-(Config) Can_Pick_Up_items - if fallen players can grab items
```

**Changes**

```
-FixCommand and ReviveCommand now accounts for multiple players
-You can now remove Penalty timer with effect command regardless of Can_Remove_Penalty_Timer config
```

**Fixes**

`-Revive effects can now be properly hidden`

# 1.16.5-1.17.1

<div><pre><strong>Additions</strong><br>-(Config) Max_Total_Revives - Max amount of time you can be revived<br>-(Config) Max_Player_Revives - Max amount of times someone else can revive you<br>-(Config) Revive_Radius - Radius teammates must be within for you to enter the fallen state<br><br><strong>Changes</strong><br>-(Config) Time_Left can now go to -1 to disable it<br>-(Config) Max_Self_Revives has been moved to General Fallen State Settings<br>-Fallen Effect now warns people if it can be removed if they leave and rejoin a world<br>-Removed Cancel_Revive_On_Damage since it wasn't being used<br>-Every effect can now be hidden through config<br>-Blocked keybinds are now checked once you enter the fallen state<br><br><strong>Fixes</strong><br>-Revive Me! now removes effects one at a time instead of all at once<br>-Fallen Penalty now happens on the 2nd down<br>-OpenGL Key error no longer spams in log<br>-Reviver's capability is properly synced on successful revive</pre></div>

# 1.16.5-1.16.1

<div><pre><strong>Changes</strong><br>-Penalty Timer now warns people once if it can't be removed<br>-Split up Self Revive methods inside Fallen Capability<br><br><strong>Fixes</strong><br>-Controllable should now work properly again<br>-Refresh revive items config now works properly<br>-No longer can use self revive if it's disabled</pre></div>

# 1.16.5-1.16.0

<div><pre><strong>Additions</strong><br>-(Config) Revive_Me_Enabled: Enables/disables Revive Me!<br>-(Config) Reset_Revive_On_Hit: Resets revive when reviver takes damage<br>-(Config) Reviver_Must_Look: If the reviver has to look at the revivee<br>-(Config) Fallen_Perspective: What camera perspective a player has whilst fallen<br><br><strong>Changes</strong><br>-Fallen effects are removed on DC<br>-Player fallen pose should properly render now<br>-Fallen glow and death timer visibilty is reduced for non-teammates<br>-Golden apple revive item now requires 2 golden apples by default<br>-Revive requirements no longer show whilst in the fallen state<br><br><strong>Fixes</strong><br>-Fallen effects should now be removed on revive<br>-Death timer should show the correct time<br>-Death timer should restart if reviver/revivee changes dimensions<br>-Call for help and change self-revive screen now checks if the player is holding shift key instead of crouching</pre></div>

# 1.16.5-1.15.44

<div><pre><strong>Changes</strong><br>-Die_When_Timer_Ends is now false by default<br><br><strong>Fixes</strong><br>-Revive Tool tips should work now<br>-Item_User config should now properly block/allow revive items</pre></div>

# 1.16.5-1.15.43

<div><pre><strong>Fixes</strong><br>-Only_Use_Available_Options config should work now</pre></div>

# 1.16.5-1.15.42

<div><pre><strong>Fixes</strong><br>-Kill Revive now checks if source entity is a Living Entity<br>-Removed refresh item exploit</pre></div>

# 1.16.5-1.15.41

<div><div><pre><strong>Additions</strong><br>-Refresh_Item_List config: Refresh revive item list while in the fallen state<br><br><strong>Fixes</strong><br>-Gave revive commands higher permission<br>-Checks if reviver is present before applying item cooldown</pre></div></div>
# 1.16.5-1.15.40

<div><div><pre><strong>Fixes</strong><br>-clampLoop shouldn't infinitely loop anymore</pre></div></div>

# 1.16.5-1.15.39

<div><pre><strong>Changes<br></strong>-bed revive item explodes if used in the nether or end<br><strong>Fixes</strong><br>-Revive item command output is now blocked if Silence_Commands is true<br>-You can now change your selected item if you have no self-revives left<br>-The revive-items-example.json should now be properly updated</pre></div>

# 1.16.5-1.15.36

<div><pre><strong>Changes</strong><br>-If Death Timer somehow ends up on a non-player, it will deal (10 * amp) damage<br><br><strong>Fixes</strong><br>-Give up screen should display the correct amount of seconds now</pre></div>

# 1.16.5-1.15.35

<div><pre><strong>Changes</strong><br>-Nether Star Item: Moved lightning bolt up 3 blocks<br><br><strong>Fixes</strong><br>-Game shouldn't crash when changing to Revive item screen</pre></div>

# 1.16.5-1.15.34

<div><pre><strong>Additions</strong><br>-Revive item system complete with examples and tooltips (took the most time...)<br>-Overheal config: Revive fallen players by healing them<br>-Controllable compatibility<br>-/revivemereload command for Config and Items<br>-Die_When_Timer_Ends config: Makes fallen players targetable if set to false<br>-Fallen_Health config: Fallen players can now heal and have a max amount of health<br>-Pause_Fallen_Timer_On_Disconnect config: if the fallen timer should pause on Disconnect<br>-Revive_Kill_Blacklist config: Entities glow black if blacklisted, green if not<br>-Revive_Effects config: What effects you revive with (unless you use items)<br>-Revert_Effect_Blacklist config: What effects will be blocked/allowed on revive<br>-Can_Remove_Penalty_Timer config: If penalty timer can be removed before it expires<br>-Added 3 keybinds: left option, right option, and tooltip combination<br><br><strong>Changes</strong><br>-Revive Me! config is now in reviveme folder<br>-Extended death and revive messages<br>-Revive call is toggleable<br>-Bold Text can be turned off<br>-Experience revive type now takes the correct amount of experience<br>-Removed Specific item from self revive and reviver penalty types<br>-Sound events adjusted<br><br><strong>Fixes</strong><br>-Items should no longer glitch out when reviving someone<br>-Blacklists/Whitelists should now properly block/allow things<br>-Lots of other things I can't remember...</pre></div>

# 1.16.5-1.14.27

<div><div><pre><strong>Changes</strong><br>-A reviver can no longer revive 2 people at once<br>-Fallen players are now only seen to be in creative while ticking through entities<br>-Adjustments to default self revive settings<br><br><br><strong>Fixes</strong><br>-Probably fixed pose issues<br>-Status effects self revive now shows the correct debuffs<br>-Players that aren't the host should be able to eat food now if the host enters the fallen state<br>-"Already revived" message now displays properly</pre></div></div>

## 1.16.5-1.14.26:

<div><pre><strong>Fixes</strong><br>-Removing player effects should no longer crash the game<br>-Blocked keybinds should no longer be active when in the fallen state<br>-Players in the fallen state should no longer be allowed to eat</pre></div>

## 1.16.5-1.14.25:

<div><pre><strong>Changes</strong><br>-Replaced Float.parseFloat with Math.round. Was causing issues of sorts...<br><br><strong>Fixes</strong><br>-Now checks and prevents even more Keybind methods while in the fallen state</pre></div>

## 1.16.5-1.14.24:

<div><pre><strong>Fixes</strong><br>-Hopefully stopped all blocked modded keybinds from being used whilst in the fallen state</pre></div>

## 1.16.5-1.14.22:

<div><pre><strong>Additions</strong><br>-(Config) Show_Item_Name: enable/disable the display name for specific item<br><br><strong>Changes</strong><br>-Gave more visual hints to know when a self-revive choice will cause you to self-destruct<br>-Edited Self Revive Mob text</pre></div>

## 1.16.5-1.14.21:

<div><pre><strong>Additions</strong><br>-Added README.md<br><strong>Fixes</strong><br>-Now checks if NBT item data configs are empty when checking for item data<br>-Chance now kills you if you run out of revives whilst in a singleplayer world</pre></div>

## 1.16.5-1.14.19:

<div><pre><strong>Fixes</strong><br>-Added null check for Item_Data and Penalty_Item_Data</pre></div>

## 1.16.5-1.14.18:

<div><pre><strong>Additions</strong><br>-(Config) Revive Chance Kill on Fail Config: Kill player if they fail the Revive Chance<br>-Fallen players now glow<br>-(Config) Include_Hotbar_Items: If Random Item sacrifice checks hotbar too<br><br><strong>Changes</strong><br>-(Config) Blocked Commands &amp; Harmful Effects Blacklist now can be turned into whitelists (by adding "//")<br>-(Config) Item Data and Specific Item Data now check if string is a valid Json NBT<br>-Removed InstaKillMsg.java<br>-Damage checks for my mod now starts on the hurtMethod, not isInvulnerableTo Method.<br>-Edited fallen noise conditions.<br><br><strong>fixes</strong><br>-Config now reloads properly<br>-You can no longer die on the same tick that you enter the fallen state<br>-Config text for "Time_Left" has been corrected<br>-Green overlay for self revive no longer stretches forever<br>-Player now dies if they have no revives left, and it's singleplayer.<br>-null check when rendering negative status effects<br>-Reviving someone now shows for everyone nearby<br>-Revive sound event now plays correctly.</pre></div>

## 1.16.5-1.12.28:

<div><pre><strong>Additions</strong><br>-Self Revive Specific Item: Sacrifice a specific item to revive<br>-Self Revive Kill: Kill enough mobs in time to fully revive<br>-Status Effects Self Revive: Gain Harmful effects on revive<br>-Experience Self Revive: Lose a portion of XP on revive<br>-Added configs for new Self revive options<br>-Config Allowed_Keybinds: Allowed Keybinds while in the fallen state<br>-Config Max_Self_revives<br>-Config Run_Death_Event_First: Run LivingDeathEvent first before this mod<br>-Config Cancel_Revive_On_Damage<br>-Config Disable_Self_Revive_On_PVP<br>-Config Randomize_Self_Revive_Options<br>-Config Only_Use_Available_Options: Only show self-revive options you can use (when available)<br>-Config Self_Penalty_Percentage: Self Revive Penalty that increases each time you self revive<br>-Config Die_On_Disconnect<br>-Config Sound_Level: Master control for Revive Me sounds<br><br><strong>Changes</strong><br>-Multiplayer Revive options are no longer tied to the player.<br>-Revive Me code will now run before or after Death event<br>-Reworked Self Revive UI<br>-Sounds are a bit louder<br>-Multiplayer Penalty item can now have NBT data<br>-Sorted Config options<br><br><strong>Fixes</strong><br>-Totem of Undying now works properly<br>-RestartDeathTimerMsg now checks if target player is null<br>-Now you stop using your item when reviving someone<br>-Double damage shouldn't kill the player anymore</pre></div>

 

## 1.16.5-1.11.2

<div><div><pre>Additions<br>-Added Revert_Effects_On_Revive Config: Gain back potion effects you had before entering the fallen state<br><br>Changes<br>- Effects are no longer constantly being removed while in the dying state<br><br>Fixes<br>- Interact_With_Inventory config now properly limits inventory access<br>- Revive Penalty no longer shows when trying to kill a fallen player<br>- Dying in lava/fire no longer causes your armor to fully repair itself</pre></div></div>

## 1.16.5-1.10.17

<div><pre><strong>Fixes</strong><br>-Turns off self-revive buttons when opening a screen<br><br><strong>Changes</strong><br>-Changed the license to LGPLv3<br>-Sacrificial items now look for matching itemstacks instead of general items</pre></div>

## 1.16.5-1.10.15

<div><pre><strong>Fixes</strong><br>-Keybinds now deactivate properly while in the dying state<br>-Drop keybind now works while in the dying state</pre></div>

## 1.16.5-1.10.14

<div><pre><strong>Fixes</strong><br>-Modded items or events that use the vanilla ATTACK and USE keybind no longer work while in the dying state<br>-Unconventional modded keybinds no longer work while in the dying state</pre></div>

## 1.16.5-1.10.11

<div><pre><strong>Fixes</strong><br>-Corrected Invocore version in Mods.toml</pre></div>

## 1.16.5-1.10.10

<div><pre><strong>Additions</strong><br>-Added sounds to reviving, fallen state, when fully revived, and when calling for help<br>-Added Call for Help button<br>-Now you can make chat messages only get sent to players nearby with the Universal_Chat_Messages config<br><br><strong>Changes</strong><br>-Decreased distance you can see a fallen player when they are not calling for help<br><br><strong>Fixes</strong><br>-Mobs should now fully ignore you while in the fallen state<br>-reviveme command no longer sends 2 chat messages</pre></div>

## 1.16.5-1.7.0

**Additions**  
\-You can now stop Command/Regular chat messages from displaying through the config

**Changes**  
\-Now a revive chat message is sent when you revive someone normally

**Fixes**  
\-No longer displays the wrong number while dying if your world is old  
\-Food level now updates on CLIENT when used to revive someone

## 1.16.5-1.6.26:

<div><pre><strong>Fixes</strong><br>-You no longer die twice</pre></div>

## 1.16.5-1.6.24:

<div><pre><strong>Additions</strong><br>-You can now see how much you have and what you'll have after when trying to revive someone<br><br><strong>Changes</strong><br>-You can now block all commands while fallen if you just add "/" in the Blocked Commands config list.<br>-You can now set the revived players food to 0<br>-You can now set the penalty time reduction to the max<br><br><strong>Fixes</strong><br>-You will now instantly kill a fallen player if you attack them.<br>-You can now revive a fallen player if you have the exact amount needed<br>-No longer assumes the entity is a player when executing commands<br>-Accurately displays penalty amount when reviving a player<br>-No longer uses a client-only method for food<br>-No longer crashes when a player being revived dies</pre></div>

## 1.16.5-1.6.16:

<div><pre><strong>Fixes</strong><br>-Player can jump in all liquids when restricted to it while in the fallen state</pre></div>

## 1.16.5-1.6.15:

<div><pre><strong>Additions</strong><br>-Ported the self-revive system from newer versions<br>-Can now change player pose when in the fallen state (Crouch, Prone, and Sleep)<br>-Now have the option to turn on inventory interaction while in the fallen state.<br>-Can now stop fallen players from typing in commands<br>-Can take XP levels when a player enters the fallen state<br>-Added can't kill yet message when PVP timer is enabled<br><br><strong>Changes</strong><br>-Made the fall plate stuff smaller so you can see the player easier.<br>-Fix command removes invulnerability set by previous versions.<br><br><strong>Fixes</strong><br>-Players now revive correctly all the time.<br>-Player will no longer get stuck dying constantly when the timer runs out<br>-Fixed Embeddium Extras incompatibility<br>-Right-Clicking on fallen players no longer causes the death timer to increase<br>-Last Damage Source no longer bypasses fallen state<br>-Revive chance and Sacrificial item percentage config is now sent to the client<br>-Properly stops the player jumping while in the fallen state if disabled<br>-Food penalty type is now properly scaled<br>-Server and Client configs are now synced</pre></div>

## 1.16.5-1.5.3:

<div><pre><strong>Fixes</strong><br>- Removed client-side code in common SlotMixin class<br>- Attack fallen event now checks if the attacked entity is a player<br><br></pre></div>

## 1.16.5-1.5.2:

<div><pre><strong>Fixes</strong><br>- Mod now properly stops players from dropping items, using modded keybinds, and interacting with containers while in the dying state.<br>- When a player is placed in the dying state they are booted out of whatever screen they are currently on.<br>- You can now instantly kill a player if they are in the dying state again.<br><br></pre></div>

## 1.16.5-1.5.1:

<div><pre><strong>Fixes</strong><br>- Death event has been moved to high priority to avoid compatibility issues<br><br></pre></div>

## 1.16.5-1.5.0:

<div><pre><strong>Additions</strong><br>- Added the Fix and Revive command. Fix reruns the code that puts you into the dying state, and revive instantly revives someone.<br><br><strong>Changes</strong><br>- You can no longer use modded keybinds while downed. Vanilla keybinds still work.<br>- Removed old network test code and other stuff not a part of the mod<br><br><strong>Fixes</strong><br>-Ported new dying system to avoid scrubbing mob memories and breaking other mods.<br><br></pre></div>

## 1.16.5-1.4.0:

**Additions**  
\- Being revived shows up for other players to see when a player is being revived  
\- Added more lang stuff

 

**Changes**  
\-Can't revive someone while crouching  
\-Changed the timer image to black  
\-Silenced most of the LOGGER debug comments  
\-Penalty amount is now saved as a Double rather than an Integer  
\-You now use the attack button to end yourself when fallen  
\-Screen zooms in the longer you hold the kill button (when fallen)

\- Now uses code from Invocore (my code library)

 

**Fixes**  
\-Text rendering looks a tad better  
\-Food can no longer increase while fallen  
\-Stops fallen player from using an item once they become fallen  
\-Reviver can no longer use items while they are reviving someone

 

## 1.16.5-1.2.2:

**Additions**

\- Displays a message in chat when a player is downed

 

**Fixes**  
\- Changing dimensions now sync cap data to clients properly

##   
1.16.5-1.2.1:

**Additions**

\- You now have 3 seconds of invulnerability on revive

 

**Fixes**  
\- Mobs no longer target fallen players  
\- Client Capability syncing

\- Fallen graphics render correctly now when the timer is disabled

 

## 1.16.5-1.1.4:

**Changes**  
\- When you switch to creative mode the revive timer will be canceled  
\- You can kill downed players by sneak-clicking them  
\- Timer can be set to 0 to disable it  
\- Default penalty is now 10 food instead of 10 health

 

**Fixes**  
\- Fixed green circle not rendering correctly for revive timer  
\- First Aid is now compatible with this mod  
\- Now works in a hardcore world  
\- When a player is revived the sync message is now sent to all players nearby instead of just the reviver and the revived  
\- Totem of Undying works now!