# SMS task creation

## Purpose

SMS task creation lets someone add a task to a Serena user's list by sending that user a specially formatted phone SMS. Serena creates the task automatically when SMS task creation is enabled and the sender's phone number is trusted.

This feature handles new, single-part phone SMS messages only. It does not scan the inbox, reply to the sender, or handle multipart SMS, MMS, RCS, or messages from chat apps.

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
- `SmsTaskReceiverTest` contains 14 Android instrumentation tests covering accepted and rejected imports, saving and reloading, sender attribution, preserved existing and unsaved tasks, repeated messages, multipart and malformed broadcasts, denied permission, shared initialization, notification-receiver reuse, and failed loading. Tests use separate preference files and one Arrange, Act, and Assert section each.
- On an API-30 emulator, real SMS delivery was checked with Serena open, backgrounded, and without a running process. Reload checks confirmed saved task counts, preserved existing tasks, and sender attribution. Real two-part SMS messages and messages delivered after permission revocation added no tasks. Verification used an isolated application ID and a temporary JUnit instrumentation runner; the configured `AndroidJUnitRunner` is absent from the current test APK.

Not implemented:

- New-task notifications, deadline-warning notifications, and live task-list refresh after SMS import.

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

The SMS receiver must work when Serena's process is not running: load the current Serena data, add the task, call Serena's existing save path, and send the notification after that call returns. This is Serena's current best-effort save behavior; `SharedPreferences.apply()` writes asynchronously and does not report disk-write failures. [Android SharedPreferences API](https://developer.android.com/reference/android/content/SharedPreferences.Editor#apply()). When Serena is already open, refresh the task lists after import.

For a message with no deadline line, Serena posts a generic `New task added` notification on a Tasks notification channel. If a deadline line could not be parsed, it says that the task was added but the deadline could not be read. Notifications do not include the SMS body and open **All Tasks**. If Android notification permission is denied or notifications are turned off, Serena still attempts to save the task; it is visible when the user opens or refreshes Tasks or Today.

## Privacy and platform requirements

Android delivers new SMS broadcasts to apps registered to receive them. Serena must inspect an incoming broadcast to compare its originating number, then parse and act on the body only after the sender matches. The trusted-number list limits which messages Serena processes; it does not prevent Android from delivering the SMS broadcast to Serena. Receiving these broadcasts requires `RECEIVE_SMS`. [Android SMS API](https://developer.android.com/reference/android/provider/Telephony.Sms.Intents)

The trust check is based on the originating number reported with the SMS. It is a user-controlled filter, not strong authentication of the person who sent the message.

Use Android's phone-number contact picker so the user selects each trusted number without Serena reading the full contacts list. [Android contact intents](https://developer.android.com/guide/components/intents-common#Contacts)

The current release process distributes a shared APK, which is the assumption for this feature. Google Play restricts SMS permissions to default SMS or Assistant handlers and specific approved exceptions. A future Play distribution must resolve eligibility before requesting `RECEIVE_SMS`. [Google Play SMS and Call Log policy](https://support.google.com/googleplay/android-developer/answer/10208820?hl=en)

Android 13 and later may require the user to grant `POST_NOTIFICATIONS`. Denying it suppresses the notification but does not prevent task creation. [Android notification permission](https://developer.android.com/develop/ui/compose/notifications/notification-permission)

## Acceptance criteria

- A valid single-part message from a trusted number creates one incomplete task with the sender's contact name and optional deadline.
- When notifications are allowed, a message with no deadline creates a task without a deadline and receives the generic notification.
- When notifications are allowed, a message with an invalid calendar date creates a task without a deadline and receives the deadline warning notification.
- A valid past deadline is retained, and extra lines after the deadline are ignored.
- A multipart message, an untrusted number, a disabled feature, a missing permission, a missing hashtag, or an empty task description creates no task.
- Removing a trusted number prevents later messages from that number from creating tasks.
- Task settings take effect on Save, are discarded on Cancel, and restore exactly from Serena JSON when imported.
- A task received while Serena is open, backgrounded, or not running uses Serena's best-effort save path and can be viewed after returning to the app in ordinary operation; notification denial does not prevent the save attempt.
- Sender attribution appears in All Tasks only. Existing manually created tasks remain without sender attribution and remain readable from saved data.
