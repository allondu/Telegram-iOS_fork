# Courier — a Telegram client for Android with the iOS interface

Courier is an Android app written in Kotlin + Jetpack Compose that recreates the
**Telegram-iOS** interface (`allondu/Telegram-iOS_fork`) while using Telegram's
official **TDLib** as the network engine. It is a real client: you sign in with
your own phone number, the chat list and message history are loaded from
Telegram, and messages you type are actually sent.

> The iOS app is Swift/AsyncDisplayKit built with Bazel. A literal line-by-line
> translation is not a build target; this project ports the *product surface*
> (flows, layout, typography, spacing, motion, colours) to native Android and
> wires it to the same protocol implementation TDLib provides.

## Multi-server / self-hosted servers

OwpenGram's server picker works because its client carries a patched Telegram
Android network stack: it can be given a `host:port`, an RSA key and a DC id.
Courier uses **TDLib**, whose `setTdlibParameters` only takes `api_id`/`api_hash`
(plus a `useTestDc` flag) and connects to Telegram's own data centres, so a
custom-server picker cannot be wired into this build. Getting that feature means
porting this interface onto OwpenGram's own client instead — say the word and
that is a separate branch.

## Download the APK

Every push to `main` builds a debug-signed, ready-to-install APK and publishes
it as a GitHub release:

**[Courier-debug.apk](https://github.com/Meralchen/courier/releases/download/courier-debug-latest/Courier-debug.apk)**

Install it on Android 8.0+ (minSdk 26). The build already carries the OwpenGram
app credentials, so you land on the phone-number screen. The same build is also
kept as the `courier-debug-apk` workflow artifact.

## What works

- **Real Telegram login** through TDLib: phone number → login code → optional
  2FA password. Sessions survive restarts (TDLib's local database).
- **Live chat list** with pinned chats, unread badges, last-message preview,
  relative timestamps and connection status.
- **Live conversations**: history is fetched with `getChatHistory`, incoming
  messages arrive via `updateNewMessage`, sending uses `sendMessage`.
  Messages are marked read with `viewMessages`.
- **Contacts tab** derived from your private chats, grouped alphabetically.
- **Settings** with your real TDLib profile, session/connection info and logout.
- **Liquid-glass UI**: translucent panels with specular rims, animated colour
  blobs, glass tab bar and inputs.
- **iOS-style motion**: spring press-scale on rows/buttons, animated tab and
  chat transitions, bubble entrance animations, animated logo and backdrop.

## What is not implemented (yet)

See [docs/PROGRESS.md](docs/PROGRESS.md) for the full checklist. The important
gaps: media upload/download previews, voice/video notes, stickers, reactions,
calls, folders, notifications, chat management (create/join/leave), profile
photo rendering, and end-to-end "secret chats".

## Build the APK

Requirements: JDK 17 and the Android SDK (platform 34, build-tools 34).

```bash
./gradlew :app:assembleDebug
# -> app/build/outputs/apk/debug/app-debug.apk
```

`./gradlew` downloads Gradle 8.7 on first use. The project uses:
Kotlin 2.0.21, AGP 8.4.2, Compose BOM 2024.06.00, `io.github.tdlib-android:core`
(TDLib with `libtdjni.so` for arm64-v8a, armeabi-v7a and x86_64).

## Telegram API credentials

Telegram requires every client to identify itself with an `api_id` and
`api_hash` (see <https://core.telegram.org/api/obtaining_api_id>).

Courier builds with the credentials the **OwpenGram Android client** publishes
(`api_id` 3893395, `api_hash` `e2adbe…1fdda`), so the APK is usable immediately
and the first screen is the phone number instead of a credentials form. Those
keys are registered to the OwpenGram application — if you publish your own
build, register your own pair at my.telegram.org and override them:

1. **At runtime** — if a build ships without credentials, the first screen asks
   for `api_id`/`api_hash`; they are stored in the app's private
   `SharedPreferences`.
2. **At build time** — put them in `local.properties` (never committed):

   ```properties
   COURIER_API_ID=1234567
   COURIER_API_HASH=0123456789abcdef0123456789abcdef
   ```

   or export `COURIER_API_ID` / `COURIER_API_HASH` before building, or set them
   as repository secrets for CI. They are exposed through `BuildConfig` only.

## Branding / licence

TDLib is distributed under the Boost Software License. The Telegram name and
logo belong to Telegram FZ-LLC; this project deliberately uses its own name and
icon, as the iOS repository's README also requires from client developers.
