# 1.21.11-6.2.1

**Fixes**
```
-(Config) Kill_On_Fail works again
```
# 1.21.11-6.2.0

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

# 1.21.11-6.1.1

<div><pre><strong>Additions</strong><br>-(Config) Max_Total_Revives - Max amount of time you can be revived<br>-(Config) Max_Player_Revives - Max amount of times someone else can revive you<br>-(Config) Revive_Radius - Radius teammates must be within for you to enter the fallen state<br><br><strong>Changes</strong><br>-(Config) Time_Left can now go to -1 to disable it<br>-(Config) Max_Self_Revives has been moved to General Fallen State Settings<br>-Fallen Effect now warns people if it can be removed if they leave and rejoin a world<br>-Removed Cancel_Revive_On_Damage since it wasn't being used<br>-Every effect can now be hidden through config<br>-Blocked keybinds are now checked once you enter the fallen state<br><br><strong>Fixes</strong><br>-Revive Me! now removes effects one at a time instead of all at once<br>-Fallen Penalty now happens on the 2nd down<br>-OpenGL Key error no longer spams in log<br>-Reviver's capability is properly synced on successful revive</pre></div>

# 1.21.11-6.0.13

<div><pre><strong>Changes</strong><br>-Penalty Timer now warns people once if it can't be removed<br>-Split up Self Revive methods inside Fallen Capability<br><br><strong>Fixes</strong><br>-Controllable should now work properly again<br>-Refresh revive items config now works properly<br>-No longer can use self revive if it's disabled</pre></div>