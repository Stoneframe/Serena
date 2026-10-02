package stoneframe.serena.gui.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Notification;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.service.notification.StatusBarNotification;
import android.text.SpannableStringBuilder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class MessageNotificationDecoderTest
{
    private static final String PACKAGE = "com.google.android.apps.messaging";
    private static final String NUMBER = "+46701234567";
    private static final String BODY = "#Serena\nKöp mjölk";
    private static final long TIMESTAMP = 123456789L;

    private Context context;
    private Person user;
    private Person sender;

    @Before
    public void setUp()
    {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        user = person("Me", "tel:+46707654321", "self");
        sender = person("Ada", "tel:" + NUMBER, "ada");
    }

    @Test
    public void decode_incomingText_preservesNumberBodyAndTimestamp()
    {
        // Arrange
        StatusBarNotification notification = notification(style().addMessage(BODY, TIMESTAMP, sender));

        // Act
        List<MessageNotificationDecoder.DecodedMessage> messages = MessageNotificationDecoder.decode(notification);

        // Assert
        assertEquals(1, messages.size());
        assertEquals(NUMBER, messages.get(0).getOriginatingNumber());
        assertEquals(BODY, messages.get(0).getMessageBody());
        assertEquals(TIMESTAMP, messages.get(0).getTimestamp());
    }

    @Test
    public void decode_frameworkMessagingStyle_extractsIncomingMessage()
    {
        // Arrange
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
        {
            Assume.assumeTrue("Framework Person requires API 28", false);
            return;
        }
        android.app.Person frameworkUser = new android.app.Person.Builder().setName("Me").setUri(user.getUri()).build();
        android.app.Person frameworkSender = new android.app.Person.Builder().setName("Ada").setUri(sender.getUri()).build();
        Notification notification = new Notification.Builder(context, "decoder-test")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setStyle(new Notification.MessagingStyle(frameworkUser).setGroupConversation(false)
                .addMessage(BODY, TIMESTAMP, frameworkSender))
            .build();

        // Act
        List<MessageNotificationDecoder.DecodedMessage> messages = MessageNotificationDecoder.decode(wrap(PACKAGE, notification));

        // Assert
        assertEquals(1, messages.size());
        assertEquals(NUMBER, messages.get(0).getOriginatingNumber());
        assertEquals(BODY, messages.get(0).getMessageBody());
        assertEquals(TIMESTAMP, messages.get(0).getTimestamp());
    }

    @Test
    public void decode_nationalNumberAndOrdinaryText_preservesUnvalidatedInput()
    {
        // Arrange
        Person nationalSender = person("Ada", "tel:070%20123%2045%2067", null);
        StatusBarNotification notification = notification(style().addMessage("Hello", TIMESTAMP, nationalSender));

        // Act
        List<MessageNotificationDecoder.DecodedMessage> messages = MessageNotificationDecoder.decode(notification);

        // Assert
        assertEquals("070 123 45 67", messages.get(0).getOriginatingNumber());
        assertEquals("Hello", messages.get(0).getMessageBody());
    }

    @Test
    public void decode_encodedTelephoneUri_decodesPayload()
    {
        // Arrange
        Person encodedSender = person("Ada", "tel:%2B46701234567", null);
        StatusBarNotification notification = notification(style().addMessage(BODY, TIMESTAMP, encodedSender));

        // Act
        List<MessageNotificationDecoder.DecodedMessage> messages = MessageNotificationDecoder.decode(notification);

        // Assert
        assertEquals(NUMBER, messages.get(0).getOriginatingNumber());
    }

    @Test
    public void decode_multipleMessages_preservesSuppliedOrderAndExcludesHistory()
    {
        // Arrange
        NotificationCompat.MessagingStyle style = style()
            .addMessage("Second timestamp", 20, sender)
            .addMessage("First timestamp", 10, sender)
            .addHistoricMessage(new NotificationCompat.MessagingStyle.Message("History", 5, sender));

        // Act
        List<MessageNotificationDecoder.DecodedMessage> messages = MessageNotificationDecoder.decode(notification(style));

        // Assert
        assertEquals(2, messages.size());
        assertEquals("Second timestamp", messages.get(0).getMessageBody());
        assertEquals("First timestamp", messages.get(1).getMessageBody());
    }

    @Test
    public void decode_outgoingMessages_excludesNullSenderAndMatchingIdentities()
    {
        // Arrange
        NotificationCompat.MessagingStyle style = style()
            .addMessage("Null sender", 1, (Person)null)
            .addMessage("Matching URI", 2, person("Other name", user.getUri(), "other"))
            .addMessage("Matching key", 3, person("Other name", "tel:" + NUMBER, user.getKey()))
            .addMessage(BODY, TIMESTAMP, sender);

        // Act
        List<MessageNotificationDecoder.DecodedMessage> messages = MessageNotificationDecoder.decode(notification(style));

        // Assert
        assertEquals(1, messages.size());
        assertEquals(BODY, messages.get(0).getMessageBody());
    }

    @Test
    public void decode_sameDisplayNameAndEmptyKeys_keepsIncomingMessage()
    {
        // Arrange
        NotificationCompat.MessagingStyle style = new NotificationCompat.MessagingStyle(person("Ada", null, ""))
            .setGroupConversation(false)
            .addMessage(BODY, TIMESTAMP, person("Ada", "tel:" + NUMBER, ""));

        // Act
        List<MessageNotificationDecoder.DecodedMessage> messages = MessageNotificationDecoder.decode(notification(style));

        // Assert
        assertEquals(1, messages.size());
    }

    @Test
    public void decode_missingOrUnsupportedSenderUris_returnsEmpty()
    {
        // Arrange
        NotificationCompat.MessagingStyle style = style()
            .addMessage(BODY, 1, person("Ada", null, null))
            .addMessage(BODY, 2, person("Ada", "mailto:ada@example.com", null))
            .addMessage(BODY, 3, person("Ada", "content://contacts/1", null))
            .addMessage(BODY, 4, person("Ada", "tel:", null))
            .addMessage(BODY, 5, person("Ada", "tel:%20", null));

        // Act
        List<MessageNotificationDecoder.DecodedMessage> messages = MessageNotificationDecoder.decode(notification(style));

        // Assert
        assertTrue(messages.isEmpty());
    }

    @Test
    public void decode_blankTextAndAttachments_excludesThem()
    {
        // Arrange
        NotificationCompat.MessagingStyle style = style()
            .addMessage(null, 1, sender)
            .addMessage("", 2, sender)
            .addMessage(" \n\t ", 3, sender)
            .addMessage(new NotificationCompat.MessagingStyle.Message(BODY, 4, sender)
                .setData("image/png", Uri.parse("content://attachments/1")))
            .addMessage(BODY, TIMESTAMP, sender);

        // Act
        List<MessageNotificationDecoder.DecodedMessage> messages = MessageNotificationDecoder.decode(notification(style));

        // Assert
        assertEquals(1, messages.size());
        assertEquals(TIMESTAMP, messages.get(0).getTimestamp());
    }

    @Test
    public void decode_wrongApplication_returnsEmpty()
    {
        // Arrange
        Notification notification = build(style().addMessage(BODY, TIMESTAMP, sender));

        // Act
        List<MessageNotificationDecoder.DecodedMessage> messages =
            MessageNotificationDecoder.decode(wrap("other.application", notification));

        // Assert
        assertTrue(messages.isEmpty());
    }

    @Test
    public void decode_groupConversation_returnsEmpty()
    {
        // Arrange
        StatusBarNotification notification = notification(style().setGroupConversation(true)
            .addMessage(BODY, TIMESTAMP, sender));

        // Act
        List<MessageNotificationDecoder.DecodedMessage> messages = MessageNotificationDecoder.decode(notification);

        // Assert
        assertTrue(messages.isEmpty());
    }

    @Test
    public void decode_groupSummary_returnsEmpty()
    {
        // Arrange
        Notification notification = build(style().addMessage(BODY, TIMESTAMP, sender));
        notification.flags |= Notification.FLAG_GROUP_SUMMARY;

        // Act
        List<MessageNotificationDecoder.DecodedMessage> messages = MessageNotificationDecoder.decode(wrap(PACKAGE, notification));

        // Assert
        assertTrue(messages.isEmpty());
    }

    @Test
    public void decode_missingStyle_returnsEmpty()
    {
        // Arrange
        Notification notification = builder().setContentTitle(NUMBER).setContentText(BODY).build();

        // Act
        List<MessageNotificationDecoder.DecodedMessage> messages = MessageNotificationDecoder.decode(wrap(PACKAGE, notification));

        // Assert
        assertTrue(messages.isEmpty());
    }

    @Test
    public void decode_otherStyle_returnsEmpty()
    {
        // Arrange
        Notification notification = builder().setStyle(new NotificationCompat.BigTextStyle().bigText(BODY)).build();

        // Act
        List<MessageNotificationDecoder.DecodedMessage> messages = MessageNotificationDecoder.decode(wrap(PACKAGE, notification));

        // Assert
        assertTrue(messages.isEmpty());
    }

    @Test
    public void decode_nullNotification_returnsEmpty()
    {
        // Arrange
        StatusBarNotification notification = null;

        // Act
        List<MessageNotificationDecoder.DecodedMessage> messages = MessageNotificationDecoder.decode(notification);

        // Assert
        assertTrue(messages.isEmpty());
    }

    @Test
    public void decode_malformedMessageExtras_returnsEmpty()
    {
        // Arrange
        Notification notification = build(style().addMessage(BODY, TIMESTAMP, sender));
        Bundle malformedMessage = new Bundle();
        malformedMessage.putCharSequence("text", BODY);
        malformedMessage.putLong("time", TIMESTAMP);
        malformedMessage.putBundle("person", null);
        notification.extras.putParcelableArray(Notification.EXTRA_MESSAGES, new Bundle[] { malformedMessage });

        // Act
        List<MessageNotificationDecoder.DecodedMessage> messages = MessageNotificationDecoder.decode(wrap(PACKAGE, notification));

        // Assert
        assertTrue(messages.isEmpty());
    }

    @Test
    public void decode_wrongMessageExtrasType_returnsEmpty()
    {
        // Arrange
        Notification notification = build(style().addMessage(BODY, TIMESTAMP, sender));
        notification.extras.putString(Notification.EXTRA_MESSAGES, "invalid");

        // Act
        List<MessageNotificationDecoder.DecodedMessage> messages = MessageNotificationDecoder.decode(wrap(PACKAGE, notification));

        // Assert
        assertTrue(messages.isEmpty());
    }

    @Test
    public void decode_mutableText_returnsSnapshot()
    {
        // Arrange
        SpannableStringBuilder text = new SpannableStringBuilder(" \n" + BODY + "\n ");
        StatusBarNotification notification = notification(style().addMessage(text, TIMESTAMP, sender));

        // Act
        List<MessageNotificationDecoder.DecodedMessage> messages = MessageNotificationDecoder.decode(notification);
        text.replace(0, text.length(), "Changed");

        // Assert
        assertEquals(" \n" + BODY + "\n ", messages.get(0).getMessageBody());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void decode_validMessage_returnsUnmodifiableList()
    {
        // Arrange
        StatusBarNotification notification = notification(style().addMessage(BODY, TIMESTAMP, sender));

        // Act
        List<MessageNotificationDecoder.DecodedMessage> messages = MessageNotificationDecoder.decode(notification);

        // Assert
        messages.clear();
    }

    private static Person person(String name, String uri, String key)
    {
        return new Person.Builder().setName(name).setUri(uri).setKey(key).build();
    }

    private NotificationCompat.MessagingStyle style()
    {
        return new NotificationCompat.MessagingStyle(user).setGroupConversation(false);
    }

    private NotificationCompat.Builder builder()
    {
        return new NotificationCompat.Builder(context, "decoder-test").setSmallIcon(android.R.drawable.ic_dialog_info);
    }

    private Notification build(NotificationCompat.MessagingStyle style)
    {
        return builder().setStyle(style).build();
    }

    private StatusBarNotification notification(NotificationCompat.MessagingStyle style)
    {
        return wrap(PACKAGE, build(style));
    }

    private static StatusBarNotification wrap(String packageName, Notification notification)
    {
        return new StatusBarNotification(packageName, packageName, 1, null, Process.myUid(), Process.myPid(), 0,
            notification, Process.myUserHandle(), TIMESTAMP);
    }
}
