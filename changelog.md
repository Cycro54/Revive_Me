# 26.1-7.3.0

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
-(config) Damage_Source_Whitelist - Added ";genericKill" to the default list
```

**Fixes**
```
-All config lists should work with Configured (mod) now
```
# 26.1-7.2.2

**Changes**
```
-Fallen Pose Sleep can now rotate with camera
-Time_Left can now be set to 0 if you have Die_When_Timer_Ends set to false
-Run_Death_Event_First is now false by default (less compatibility issues this way)
```

**Fixes**
```
-Corpse mod no longer deletes all of your items on self-destruct
-Revive effects can now be hidden (again)
-Setting Time_Left to -1 will no longer make you targetable
```
# 26.1-7.2.1

**Fixes**
```
-(Config) Kill_On_Fail works again
```
# 26.1-7.2.0

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

# 26.1-7.1.1

<div><pre><strong>Additions</strong><br>-(Config) Max_Total_Revives - Max amount of time you can be revived<br>-(Config) Max_Player_Revives - Max amount of times someone else can revive you<br>-(Config) Revive_Radius - Radius teammates must be within for you to enter the fallen state<br><br><strong>Changes</strong><br>-(Config) Time_Left can now go to -1 to disable it<br>-(Config) Max_Self_Revives has been moved to General Fallen State Settings<br>-Fallen Effect now warns people if it can be removed if they leave and rejoin a world<br>-Removed Cancel_Revive_On_Damage since it wasn't being used<br>-Every effect can now be hidden through config<br>-Blocked keybinds are now checked once you enter the fallen state<br><br><strong>Fixes</strong><br>-Revive Me! now removes effects one at a time instead of all at once<br>-Fallen Penalty now happens on the 2nd down<br>-OpenGL Key error no longer spams in log<br>-Reviver's capability is properly synced on successful revive</pre></div>

# 26.1-7.0.0
**Changes**  
`-Ported from Minecraft 1.21.11 to 26.1 (epic!)`