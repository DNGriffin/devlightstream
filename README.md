# Devlightstream DC

devlightstream is an Android game streaming client for [Apollo](https://github.com/ClassicOldSong/Apollo) and
[Sunshine](https://github.com/LizardByte/Sunshine), based on [Artemis Android (formerly Moonlight Noir)](https://github.com/ClassicOldSong/moonlight-android).

## Devlightstream DC stick tuning

This experimental Android app has its own DC icon and application ID (`com.dngriffin.devlightstream.dc`). It installs alongside the original `devlightstream`, its debug build, and Artemis. Pair this app with your host separately; settings and pairing data are independent.

In **Settings → Gamepad settings → DC stick tuning**, adjust each stick independently:

- **Input dead zone:** 0–99%; input at or below this radius sends zero.
- **Minimum output:** 0–100%; output starts at this radius immediately after the dead zone.
- **Maximum output:** 0–100%; full tilt reaches this radius. Zero disables the stick. If minimum exceeds maximum, the effective minimum equals maximum.
- **Response curve:** Linear, Quadratic, Cubic, Square root, or S-curve.

Defaults are 5% input dead zone, 0% minimum, 100% maximum, and Linear. Reconnect the stream after changing settings. These settings transform gamepad packets from Android gamepads, USB gamepads, and on-screen sticks. The separate mouse-emulation mode retains its existing behavior.

For a cursor in a game with a large built-in dead zone, try **5% dead zone, 20% minimum, 50% maximum**. With Linear, 5% input sends zero, just beyond 5% starts near 20%, 52.5% input sends 35%, and full tilt sends 50%. The entire remaining 95% of stick travel controls the 20–50% output band.

Direction is preserved. The cap is radial, so diagonals cannot exceed the selected output radius. The client applies dead zone removal, curve, and range mapping once after merging controller input:

```
t = (min(inputRadius, 1) - deadzone) / (1 - deadzone)
outputRadius = minimum + (maximum - minimum) * curve(t)
```

Linear uses `t`; Quadratic `t²`; Cubic `t³`; Square root `√t`; S-curve `t²(3−2t)`. Quadratic and Cubic emphasize precision near the minimum; Square root rises more quickly; S-curve eases both ends. All curves share the same endpoints and use 16-bit gamepad packets.

## Features inherited from Artemis

1. Custom virtual buttons with import and export support.
2. [Custom resolutions](https://github.com/moonlight-stream/moonlight-android/pull/1349).
3. Custom bitrates.
4. [Multiple mouse mode switching](https://github.com/moonlight-stream/moonlight-android/pull/1304) (normal mouse, [multi-touch](https://github.com/moonlight-stream/moonlight-android/pull/1364), touchpad, disabled, local cursor mode).
5. Optimized virtual gamepad skins and free joystick.
6. External monitor mode.
7. Joycon D-pad support.
8. Simplified performance information display.
9. [Game back menu](https://github.com/moonlight-stream/moonlight-android/pull/1171).
10. Custom shortcut commands.
11. Easy soft keyboard switching.
12. Portrait mode.
13. Display on top mode, useful for foldable phones.
14. [Virtual touchpad space and sensitivity adjustment](https://github.com/moonlight-stream/moonlight-android/issues/1348#issuecomment-2236344729) for playing right-click view games, such as Warcraft.
15. Force use device's own vibration motor (in case your gamepad's vibration is not effective).
16. Gamepad debugging page to view gamepad vibration and gyroscope information, as well as Android kernel version information.
17. Trackpad tap/scrolling support
18. Natural track pad mode with touch screen
19. Non-QWERTY keyboard layout support
20. Quick Meta key with physical BACK button
21. Frame rate lock fix for some devices
22. Video scale mode: Fit/Fill/Stretch
23. View pan/zoom support
24. Rotate screen in-game
25. Add option to quit app directly
26. Samsung DeX scrolling support
27. Proper click/scroll/right-click for trackpad on generic Android tablet when using local cursor
28. Virtual Display integration with [Apollo](https://github.com/ClassicOldSong/Apollo)
29. Server Command integration with [Apollo](https://github.com/ClassicOldSong/Apollo)
30. Clipboard sync (requires Apollo)
31. SBS 3D for external Displays (Using AI MiDaS v2 Lite)

## Building

With JDK 17 or later and Android SDK Platform 36 installed, run:

```sh
git submodule update --init --recursive
./gradlew :app:assembleNonRoot_gameDebug
```

Set `ANDROID_HOME` to your Android SDK directory or open the project in Android Studio.
The build uses Android NDK 27.0.12077973 and writes the APKs to `app/build/outputs/apk/nonRoot_game/debug/`.

## Authors

* [Cameron Gutman](https://github.com/cgutman)  
* [Diego Waxemberg](https://github.com/dwaxemberg)  
* [Aaron Neyer](https://github.com/Aaronneyer)  
* [Andrew Hennessy](https://github.com/yetanothername)

Moonlight is the work of students at [Case Western](http://case.edu) and was
started as a project at [MHacks](http://mhacks.org).

For a signed DC release, keep a dedicated signing keystore outside Git, with alias `dc`, and set `DC_KEYSTORE` and `DC_STORE_PASSWORD` before running `./gradlew :app:assembleNonRoot_gameRelease`. Preserve that key for future DC updates. Do not use the original app identity.

Controller tests: `./gradlew :app:testNonRoot_gameDebugUnitTest --tests 'com.limelight.binding.input.*'`.
