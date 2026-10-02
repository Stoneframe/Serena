# SMS and RCS task creation

## Purpose

The intended feature lets someone add a task to a Serena user's list by sending that user a specially formatted SMS or RCS message. Serena creates the task automatically when task creation from messages is enabled and the sender's phone number is trusted.

End-to-end RCS support is not implemented. A standalone Google Messages notification decoder is implemented but is not connected to task importing. The active implementation handles new, single-part phone SMS messages only. It does not scan the inbox, reply to the sender, or import multipart SMS, MMS, RCS, or messages from chat apps.

## Implementation status

Implemented:

- `SmsTaskMessageParser` trims each line with `String.trim()`, then reads the exact `#Serena` tag, a task description, and an optional deadline from the first three nonempty lines. Further lines are ignored.
- Deadline text is passed to Joda-Time's `LocalDate.parse()`. An absent or blank deadline produces a `null` deadline without a warning. An unparseable nonempty deadline produces a `null` deadline and a warning flag. Parsed dates are returned, including past dates.
- The parser returns a `ParsedTask` value containing the description, nullable deadline, and deadline warning flag. It does not create or save a Serena task.
- `SmsTaskMessageParserTest` covers valid, absent, blank, unpadded, unparseable, impossible, and past deadlines, along with a misplaced hashtag, a missing description, and a null message body.
- **Tasks > Settings** stores the SMS task creation setting and selected trusted phone numbers, including each selected contact's name. It uses Android's phone-number picker and does not read the full contacts list.
- Task limit settings and SMS settings are separated by a divider. Trusted numbers appear in a bordered, scrollable list with the contact name, phone number, and a bin-icon remove button. An exact duplicate phone number cannot be added.
- The SMS setting and trusted senders are included in JSON data. Version 30 migration defaults older data to disabled with no trusted senders.
- `TaskSmsSettingsTest` and `SmsTaskSettingsStorageTest` cover disabled defaults, sender-list ownership, migrations from versions 27 and 29, and JSON round trips.
- Checking SMS task creation requests `RECEIVE_SMS`; granting keeps it checked and denying unchecks it. Imported enabled settings remain saved when device permission is absent, with an access warning in Task settings.
- `TrustedSmsSenderMatcher` uses libphonenumber 9.0.40 to normalize complete, valid phone numbers to E.164 and returns the first saved sender whose number matches. Internal normalization requires non-null phone numbers; trusted entries require non-null senders and phone numbers. A null originating number returns no match. Missing or unsupported country codes use the `ZZ` region fallback.
- `TrustedSmsSenderMatcherTest` covers regional and international forms, missing and unsupported country codes, padded lowercase country codes, invalid inputs, suffix-only nonmatches, equivalent saved entries, null originating numbers, and matching without a contact name.
- Tasks store an optional `senderName` snapshot, exposed through `Task` and set or cleared through `TaskEditor`. `TaskEditor` also exposes a `senderNameChanged()` listener callback. Names are stored verbatim; `null` means no attribution. Attribution is included in JSON and in-memory checkpoints. Manual tasks and older task data without the field have no attribution; storage remains at version 30.
- `TaskSenderAttributionTest` and `TaskSenderAttributionStorageTest` cover attribution updates, ordinary task edits, trusted-sender replacement, checkpoint restoration, JSON round trips, and loading version-29 and version-30 tasks without attribution.
- All Tasks appends sender attribution to the main description, for example `Buy milk (from: Ada)`. Null, empty, or whitespace-only sender names add no suffix; other names are displayed verbatim. Deadline text remains below the description. Today and the task editor show the description without attribution.
- `TaskManager.importSmsTask()` accepts an already decoded single-part message, checks the enabled setting and sender trust, then parses the body and uses the task editor to add an incomplete task to the current in-memory container. The task receives the parsed description and deadline, the current local date as its ignore-before date, and a snapshot of the matched sender's contact name. The result contains the added task and deadline-warning flag; disabled, unmatched, or rejected messages return `null` without changing task data. Repeated accepted messages create separate tasks, and daily task limits do not restrict creation. The operation does not save to storage or refresh the UI.
- `TaskSmsImportTest` contains 14 tests covering rejection without mutation, task defaults, deadline warnings, sender matching and attribution, repeated messages, daily limits, container replacement, and clock changes. Each test has one Arrange, Act, and Assert section.
- `SmsTaskReceiver` receives `SMS_RECEIVED` broadcasts through a manifest receiver requiring `BROADCAST_SMS` from the sender. It checks `RECEIVE_SMS`, requires exactly one PDU, and decodes it through Android's SMS API. PDU extras are read with the typed `IntentCompat.getSerializableExtra()` API; `isSmsReceptionAllowed()` and `isSinglePartPdu()` check reception eligibility and PDU input. Multipart broadcasts, unrelated actions, malformed extras or PDUs, unsupported formats, and unsuccessful decoding are ignored before loading Serena. The receiver passes the originating number, body, and network country code to `TaskManager.importSmsTask()`, then calls `Serena.save()` once for an accepted import. It does not save rejected imports. When network country information is unavailable, the matcher uses its existing region fallback.
- `GlobalState.getOrLoadSerena()` reuses the active Serena instance or loads and publishes one after a successful load. The SMS receiver and `NotifierReceiver` use this shared initialization path; `MainActivity` reuses the published instance when opened afterward.
- Accepted SMS imports post a notification after `Serena.save()` returns. The high-importance Tasks channel uses three short vibrations and retains one notification, updated for the latest accepted import. Its text is `New task added`, or `New task added, but its deadline could not be read.` for an unreadable deadline. Notifications contain no task description, sender, phone number, or SMS body. Tapping opens All Tasks using a separate immutable pending intent; Routine and Reminder notifications continue to open Today.
- MainActivity creates the Tasks channel and notification-channel cleanup retains it. After an upgrade, task notifications require opening the updated app once to create the channel. The shared posting path checks Android 13 notification permission, app notification access, and the relevant channel's access for Tasks, Routines, and Reminders. Denied notification access does not prevent task creation.
- Enabling SMS task creation in Task settings requests `POST_NOTIFICATIONS` on Android 13 and later when missing, after SMS permission is granted. If SMS permission is already granted, the notification request starts directly. Notification denial keeps SMS creation checked and shows a brief message explaining that tasks will still be added and notification access can be enabled in Android app settings. Opening settings with saved SMS creation enabled does not trigger this request; callbacks do not save settings and are ignored if the checkbox has become unchecked.
- `SmsTaskReceiverTest` contains 16 Android instrumentation tests covering accepted and rejected imports, saving and reloading, sender attribution, preserved existing and unsaved tasks, repeated messages, multipart and malformed broadcasts, denied permission, shared initialization, notification-receiver reuse, failed loading, save failures without notifications, and notification-permission denial without preventing saving. Tests use separate preference files and one Arrange, Act, and Assert section each.
- `TaskImportNotificationTest` contains four Android instrumentation tests covering an initialized channel and its retention, generic text and flags, replacement of deadline warnings, and separate Tasks/Reminder tap destinations with cold and running activity entry.
- On an API-30 emulator, real SMS delivery was checked with Serena open, backgrounded, and without a running process. Reload checks confirmed saved task counts, preserved existing tasks, and sender attribution. Real two-part SMS messages and messages delivered after permission revocation added no tasks. Verification used an isolated application ID and a temporary JUnit instrumentation runner; the configured `AndroidJUnitRunner` is absent from the current test APK.
- Notification verification on the API-30 emulator passed 19 instrumentation tests; the Android 13 notification-permission test was skipped. Real SMS delivery without a running process posted the generic notification, and tapping it opened Tasks with the saved task and attribution. Blocking the Tasks channel suppressed the notification while a subsequent SMS import still saved.
- After adding the Task settings notification permission request, an isolated API-30 settings smoke check with SMS access already granted verified enabling, Save and reload, reopening enabled settings, Cancel, and disabling with Save. The debug build and Android lint passed.
- Accepted SMS imports call `Serena.notifyChange()` once after saving and before posting the notification. All Tasks registers a change listener while resumed, refreshes its adapter on changes and resume, and unregisters on pause. Today uses its existing change listener and refreshes its adapters and section colors. Today continues applying task eligibility, urgency, and daily limits; sender attribution remains in All Tasks only. Rejected imports and save failures do not emit a change.
- `SmsTaskReceiverTest` checks change callback counts and save ordering. `SmsTaskLiveRefreshTest` contains four Android instrumentation tests covering rendered All Tasks and Today rows, attribution, Today section colors, exhausted daily allowances with urgent imports, paused-list catch-up, and repeated listener registration and cleanup. The UI tests observe adapter change events as well as rendered rows and use a fixed clock.
- Live-refresh verification on an API-30 emulator passed 23 Android tests; the Android 13 permission test was skipped. Real modem SMS delivery refreshed the open All Tasks and Today lists, and All Tasks also refreshed with its notification channel blocked. Each modem check observed one adapter change and the rendered task row. Verification used an isolated application ID and a temporary instrumentation runner. JVM tests, debug and test APK builds, and Android lint passed.
- Android 13 / API-33 verification on 2026-10-02 passed five Task settings permission flows: SMS denial, notification denial after SMS approval, approval of both permissions, SMS access already granted, and both permissions already granted. The actual Android dialogs confirmed SMS-before-notification ordering and no notification request after SMS denial. Notification denial showed the explanatory message and left SMS creation checked. Permission callbacks left settings unsaved until Save. With both permissions already granted, enabling showed no permission dialog.
- Synthetic SMS messages delivered through the API-33 emulator modem verified saved sender attribution and one All Tasks adapter refresh after notification denial and approval. Denial suppressed notification posting; approval posted the generic notification, whose pending intent opened All Tasks with the saved attributed task. All 24 receiver, notification, and list-refresh instrumentation tests passed on API 33 with no skips. Verification used an isolated application ID and temporary instrumentation runners; the test APK build passed. `SmsTaskLiveRefreshTest` now opens each list in a fresh activity task.

SMS implementation is complete; RCS notification reception and import integration remain outstanding. Android 13 notification permission behavior has been verified on an API-33 emulator; physical-device verification has not been performed.

### Message notification decoding

- `MessageNotificationDecoder.decode()` uses the existing AndroidX `MessagingStyle` extraction API and returns an immutable list of decoded message values containing the originating phone number, message body, and timestamp.
- It accepts notifications from `com.google.android.apps.messaging` for one-to-one conversations. Null input, notification summaries, group conversations, unsupported styles, and malformed notification data return an empty list.
- Current qualifying messages are returned in their supplied order. Historic messages, attachments, blank text, and outgoing messages are excluded. A null sender or a matching nonempty sender URI/key on the conversation's user identifies an outgoing message; display names are not used for identity matching.
- Senders must expose a nonempty `tel:` URI. The decoder extracts its decoded payload and copies message text verbatim, including newlines and Unicode. Phone-number validation, trusted-number matching, and command parsing remain in the existing SMS importer components.
- `MessageNotificationDecoderTest` uses synthetic notifications to cover extraction, ordering, rejection paths, malformed extras, text snapshots, and immutable output. Each test has one Arrange, Act, and Assert section.
- API-30 verification on 2026-10-02 passed all 19 decoder instrumentation tests with no skips, including AndroidX and framework-built notifications. Verification used an isolated application ID and temporary runner. All 226 JVM tests passed; debug and test APK builds passed, and Android lint reported zero errors and 361 warnings. Real Google Messages notification metadata has not been verified.
- The decoder does not create or save tasks, receive notification callbacks, request access, distinguish SMS from RCS, or prevent duplicate imports.

## Planned RCS support

- Automatically create tasks from incoming RCS text messages when message task creation is enabled and the sender's phone number is trusted.
- Use the same `#Serena` tag, description, optional deadline, and deadline handling as SMS messages.
- Match the sender against the existing trusted phone numbers and save the matched contact name as the task's `senderName` snapshot. A display name alone does not establish trust.
- Use the existing task creation, saving, list refresh, and task notification behavior. Notification denial must not prevent task creation.

Notification-based reception is the selected implementation direction, beginning with the standalone Google Messages decoder. Notification listener access, availability of the sender's telephone URI on real devices, transport selection, and duplicate prevention remain outstanding. Integration will reuse `TaskManager.importSmsTask()` and the existing load, save, list refresh, and notification paths where suitable. Android's IMS single registration APIs require a privileged, preinstalled app with the default SMS role. [Android IMS single registration](https://source.android.com/docs/core/connect/ims-single-registration)

The remaining setup and behavior sections describe the current SMS implementation.

## Setup and trusted senders

SMS task creation is off by default and is configured from **Tasks > Settings**. The user selects one or more specific phone numbers from the device's contacts and can remove them at any time. If a contact has multiple numbers, each trusted number is selected separately. Trust is attached to the selected number, not the contact name; changing a number requires selecting the new number.

The enabled state and trusted numbers are included in Serena's JSON export and are restored exactly when that data is imported. This can enable SMS task creation with the imported trusted numbers. Backups created before this feature default to disabled with no trusted numbers. Settings changes take effect when the user presses Save; Cancel discards the pending changes. Uncommitted SMS setting and trusted-sender edits are not restored if Task settings is recreated. Android's `RECEIVE_SMS` permission is per device and is not included in Serena data, so a restored setting does not import messages until permission is granted on that device.

Enabling the feature requires Android's `RECEIVE_SMS` permission. If permission is denied or later revoked, Serena does not import messages and explains that SMS access is unavailable in Task settings.

`TrustedSmsSenderMatcher` normalizes and compares complete originating and trusted phone numbers as E.164 values. National numbers require a supported ISO country code; without one, international numbers with a `+` prefix can match, including formatting such as `(+46) 70 123 45 67`. If multiple saved entries normalize to the same number, the first saved entry is returned. Invalid numbers, alphabetic sender IDs, and numbers with extensions return no match. SMS broadcast reception calls the matcher through `TaskManager.importSmsTask()`. The matcher does not use display names or partial numbers.

## Message format

The first nonempty line must be `#Serena`. The next nonempty line is the task description. The following nonempty line may be a deadline. Serena uses Joda-Time's `LocalDate.parse()` on that line without additional format checks, so any date text it accepts is valid. For example, both `2026-09-30` and `2026-9-30` are accepted.

```text
#Serena
Take out the trash
2026-09-30
```

Rules:

- Leading and trailing ASCII whitespace is removed from each line; lines empty afterward do not count when finding the first three lines.
- The hashtag must be exactly `#Serena` on its own first nonempty line. A hashtag elsewhere does not qualify.
- The task description must be present and nonempty. One accepted SMS creates one task.
- The deadline is optional; see [Deadline handling](#deadline-handling) for how absent, blank, and unreadable values are handled.
- Nonempty lines after the optional deadline line are ignored.
- A separate SMS with the same text creates a separate task; Serena does not deduplicate tasks by message text.
- A multipart SMS is ignored as a whole, even if one of its parts contains valid task text.

An imported task starts incomplete and uses the same initial `ignore before` date as a task created manually: the receiving device's local date when the SMS arrives. Imported tasks follow Serena's ordinary task rules for eligibility, urgency, editing, and completion. A task with a past deadline follows the same overdue behavior as any other task.

## Deadline handling

The deadline is the third nonempty line. If no third nonempty line is present because the deadline is omitted or blank after trimming, Serena sets the deadline to `null` and sends the generic notification without a deadline warning.

If a third nonempty line is present, Serena passes it to Joda-Time's `LocalDate.parse()`. If parsing fails, Serena creates the task with a `null` deadline and sends a notification that the deadline could not be read. Any date that parses successfully is kept, including a past date.

## Task source and notification

Serena saves a snapshot of the sender's contact name with an imported task and shows it in **All Tasks** only. It does not copy the sender's phone number into the task. Later changes to a contact or trusted-number entry do not change existing tasks. Tasks created manually have no sender attribution.

The SMS receiver loads the current Serena data when the process is not running, adds the task, calls Serena's existing save path, notifies active Serena listeners, and sends the notification after that save call returns. This is Serena's current best-effort save behavior; `SharedPreferences.apply()` writes asynchronously and does not report disk-write failures. [Android SharedPreferences API](https://developer.android.com/reference/android/content/SharedPreferences.Editor#apply()). Visible All Tasks and Today lists refresh after import, including when notifications are unavailable. Lists paused during import refresh when resumed.

For a message with no deadline line, Serena posts a generic `New task added` notification on a Tasks notification channel. If a deadline line could not be parsed, it says that the task was added but the deadline could not be read. Notifications do not include the SMS body and open **All Tasks**. If Android notification permission is denied or notifications are turned off, Serena still attempts to save the task; it is visible when the user opens or refreshes Tasks or Today.

## Privacy and platform requirements

Android delivers new SMS broadcasts to apps registered to receive them. Serena must inspect an incoming broadcast to compare its originating number, then parse and act on the body only after the sender matches. The trusted-number list limits which messages Serena processes; it does not prevent Android from delivering the SMS broadcast to Serena. Receiving these broadcasts requires `RECEIVE_SMS`. [Android SMS API](https://developer.android.com/reference/android/provider/Telephony.Sms.Intents)

The trust check is based on the originating number reported with the SMS. It is a user-controlled filter, not strong authentication of the person who sent the message.

Use Android's phone-number contact picker so the user selects each trusted number without Serena reading the full contacts list. [Android contact intents](https://developer.android.com/guide/components/intents-common#Contacts)

The current release process distributes a shared APK, which is the assumption for this feature. Google Play restricts SMS permissions to default SMS or Assistant handlers and specific approved exceptions. A future Play distribution must resolve eligibility before requesting `RECEIVE_SMS`. [Google Play SMS and Call Log policy](https://support.google.com/googleplay/android-developer/answer/10208820?hl=en)

Android 13 and later may require the user to grant `POST_NOTIFICATIONS`. Task settings requests this permission when the user enables SMS task creation and SMS access is available. Already granted notification permission and devices below Android 13 skip the request. Denying it suppresses the notification but does not prevent task creation; notification access can also be enabled through Android's app settings. [Android notification permission](https://developer.android.com/develop/ui/compose/notifications/notification-permission)

## Acceptance criteria

Current SMS acceptance criteria:

- A valid single-part SMS from a trusted number creates one incomplete task with the sender's contact name and optional deadline.
- When notifications are allowed, a message with no deadline creates a task without a deadline and receives the generic notification.
- When notifications are allowed, a message with an invalid calendar date creates a task without a deadline and receives the deadline warning notification.
- A valid past deadline is retained, and extra lines after the deadline are ignored.
- A multipart SMS, an untrusted number, a disabled feature, a missing permission, a missing hashtag, or an empty task description creates no task.
- Removing a trusted number prevents later messages from that number from creating tasks.
- Task settings take effect on Save, are discarded on Cancel, and restore exactly from Serena JSON when imported.
- A task received while Serena is open, backgrounded, or not running uses Serena's best-effort save path and can be viewed after returning to the app in ordinary operation; notification denial does not prevent the save attempt.
- Sender attribution appears in All Tasks only. Existing manually created tasks remain without sender attribution and remain readable from saved data.

Planned RCS acceptance criterion:

- When message task creation is enabled and the required RCS access is available, a valid RCS text message from a trusted phone number creates one incomplete task with the sender's contact name and optional deadline, saves it, refreshes visible task lists, and posts the existing task notification when notifications are allowed.
