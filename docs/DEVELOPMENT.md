# Rune Launcher development

## Recommended workstation

- Debian 13
- Current stable Android Studio
- Android SDK Platform 34 (the current compile SDK)
- Android SDK Platform Tools (`adb`)
- Android Studio's bundled JDK

The first successful build should use the repository's existing SDK and
dependency levels. Toolchain modernization will be isolated in a later commit
so functional changes remain easy to review.

## Open and build

1. In Android Studio, choose **Open** and select the repository root.
2. Allow the Gradle project to sync.
3. If prompted for an SDK, install Android SDK Platform 34 and accept its
   licenses.
4. Select the `app` run configuration and the `debug` build variant.
5. Build with **Build > Make Project** before connecting a phone.

Rune's debug application ID is
`io.github.jamiekaj.runelauncher.debug`, so it can coexist with both Hex
Launcher and a future Rune release build.

## Connect an HMD Fusion over USB

1. On the phone, open **Settings > About phone** and tap **Build number** seven
   times to enable Developer options.
2. Open **Settings > System > Developer options** and enable **USB debugging**.
3. Connect the phone with a data-capable USB cable.
4. Accept the phone's RSA debugging prompt.
5. Confirm the connection with `adb devices` or Android Studio's device menu.
6. Press **Run** in Android Studio and select the HMD Fusion.

Rune will appear in Android's Home-app chooser after installation. Keep the
system launcher available while Rune is under development so it is always
possible to switch back through **Settings > Apps > Default apps > Home app**.

## First-device verification

- Complete onboarding, close Rune, and open it again. Onboarding must not
  repeat.
- Swipe to search, launch an app, then return Home. Rune must show its neutral
  home state with no keyboard or search results.
- Confirm Hex Launcher remains separately installed.
- Confirm Android offers Rune in the Home-app chooser.

If a problem occurs, capture Android Studio Logcat filtered by
`io.github.jamiekaj.runelauncher` and include the steps immediately preceding
the problem.
