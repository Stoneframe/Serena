package stoneframe.serena.gui.tasks;

import android.app.Notification;
import android.net.Uri;
import android.service.notification.StatusBarNotification;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MessageNotificationDecoder
{
    private static final String GOOGLE_MESSAGES_PACKAGE = "com.google.android.apps.messaging";

    /** Returns an immutable snapshot of supported incoming messages, without importing tasks. */
    public static List<DecodedMessage> decode(@Nullable StatusBarNotification notification)
    {
        if (notification == null || !GOOGLE_MESSAGES_PACKAGE.equals(notification.getPackageName()))
        {
            return Collections.emptyList();
        }

        try
        {
            return decodeNotification(notification.getNotification());
        }
        catch (RuntimeException e)
        {
            // Malformed notification extras must not reach the import path.
            return Collections.emptyList();
        }
    }

    private static List<DecodedMessage> decodeNotification(Notification notification)
    {
        if ((notification.flags & Notification.FLAG_GROUP_SUMMARY) != 0)
        {
            return Collections.emptyList();
        }

        NotificationCompat.MessagingStyle style =
            NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification);

        if (style == null || style.isGroupConversation())
        {
            return Collections.emptyList();
        }

        return decodeMessages(style);
    }

    private static List<DecodedMessage> decodeMessages(NotificationCompat.MessagingStyle style)
    {
        List<DecodedMessage> messages = new ArrayList<>();

        for (NotificationCompat.MessagingStyle.Message message : style.getMessages())
        {
            DecodedMessage decoded = decodeMessage(message, style.getUser());

            if (decoded != null)
            {
                messages.add(decoded);
            }
        }

        return Collections.unmodifiableList(messages);
    }

    @Nullable
    private static DecodedMessage decodeMessage(NotificationCompat.MessagingStyle.Message message, Person user)
    {
        Person sender = message.getPerson();
        CharSequence text = message.getText();
        String body = text == null ? null : text.toString();

        if (isOutgoing(sender, user) || body == null || body.trim().isEmpty()
            || message.getDataMimeType() != null || message.getDataUri() != null)
        {
            return null;
        }

        String number = getPhoneNumber(sender);

        return number == null ? null : new DecodedMessage(number, body, message.getTimestamp());
    }

    private static boolean isOutgoing(@Nullable Person sender, Person user)
    {
        return sender == null
            || matchesIdentity(sender.getUri(), user.getUri())
            || matchesIdentity(sender.getKey(), user.getKey());
    }

    private static boolean matchesIdentity(@Nullable String senderValue, @Nullable String userValue)
    {
        return senderValue != null && !senderValue.trim().isEmpty() && senderValue.equals(userValue);
    }

    @Nullable
    private static String getPhoneNumber(Person sender)
    {
        if (sender.getUri() == null)
        {
            return null;
        }

        Uri uri = Uri.parse(sender.getUri());
        String number = uri.getSchemeSpecificPart();

        return "tel".equalsIgnoreCase(uri.getScheme()) && number != null && !number.trim().isEmpty()
            ? number : null;
    }

    public static final class DecodedMessage
    {
        private final String originatingNumber;
        private final String messageBody;
        private final long timestamp;

        private DecodedMessage(String originatingNumber, String messageBody, long timestamp)
        {
            this.originatingNumber = originatingNumber;
            this.messageBody = messageBody;
            this.timestamp = timestamp;
        }

        public String getOriginatingNumber()
        {
            return originatingNumber;
        }

        public String getMessageBody()
        {
            return messageBody;
        }

        public long getTimestamp()
        {
            return timestamp;
        }
    }
}
