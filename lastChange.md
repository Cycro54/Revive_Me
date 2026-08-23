# 1.16.5-${mod_version}

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