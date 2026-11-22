# Courier architecture

```
MainActivity (ComponentActivity)
└── IosTheme                     design tokens, light/dark palettes
    └── App()                    auth gate + navigation
        ├── SetupScreen          api_id / api_hash (my.telegram.org)
        ├── PhoneScreen          setAuthenticationPhoneNumber
        ├── CodeScreen           checkAuthenticationCode
        ├── PasswordScreen       checkAuthenticationPassword
        └── MainShell
            ├── ChatListScreen   live chats
            ├── ChatScreen       live history + send
            ├── ContactsScreen   private chats by letter
            ├── SettingsScreen   profile, session, logout
            └── GlassTabBar      animated glass tab bar
```

## Telegram layer

`telegram/TdlibClient.kt` is the only place that touches TDLib.

- One `org.drinkless.tdlib.Client` is created lazily with
  `Client.create(updateHandler, updateExceptionHandler, defaultExceptionHandler)`.
- TDLib calls back on its own thread; updates are folded into
  `MutableStateFlow`s (`auth`, `chats`, `messages`, `profile`, `connection`,
  `activeChat`) which Compose collects with `collectAsState()`.
- Caches: `ConcurrentHashMap` for chats, users and message lists. TDLib objects
  are mutable Java beans, so updates patch cached objects in place.
- Login: `UpdateAuthorizationState` drives the state machine; the app answers
  `waitTdlibParameters` automatically, then renders one screen per stage.
- Reads: `getChats`/`loadChats` fill the main list; `getChatHistory(0, 0, 60)`
  loads the newest 60 messages, `viewMessages` marks the chat read.
- Writes: `sendMessage(chatId, topic=null, replyTo=null, options=null,
  markup=null, InputMessageText)`. `updateMessageSendSucceeded` replaces the
  pending bubble with the server message.

### Important TDLib details

- This TDLib build names message contents `MessageText`, `MessagePhoto`, …
  (`MessageContent` is only the base class).
- `getChats(ChatList, limit)` has no offset in this layer; paging is done with
  `loadChats`.
- Optional parameters accept `null`; `FormattedText` needs an empty
  `TextEntity[]`, not `null`.
- TDLib owns its SQLite database in `filesDir/tdlib`; deleting it logs the
  session out.

## UI layer

- `ui/theme/IosTheme.kt` holds the design tokens (colour roles, type ramp) and
  exposes them through `LocalIos`, mirroring how the iOS client centralises its
  presentation theme.
- `ui/components/Components.kt` contains primitives ported from the iOS look:
  avatars with gradients, hairlines, nav bars, ticks, glyphs drawn with Canvas.
- `ui/components/Glass.kt` is the liquid-glass system: layered translucent
  gradient + specular rim (`GlassPanel`), drifting blurred colour blobs
  (`LiquidBackdrop`), glass inputs and pills.
- `ui/App.kt` owns the two navigation axes — auth stage and (tab, open chat) —
  and animates both with `AnimatedContent`.

## Why TDLib

Implementing MTProto (auth keys, DH, SRP 2FA, message encryption, updates,
pagination) by hand is a project of its own and a security liability. TDLib is
Telegram's official, audited implementation, so the port focuses on the part the
iOS repository actually contributes: the interface and interaction design.
