package stoneframe.serena.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.joda.time.LocalDate;
import org.junit.Test;

public class SmsTaskMessageParserTest
{
    @Test
    public void parse_messageWithValidDeadlineAndExtraLines_returnsTaskAndIgnoresExtraLines()
    {
        SmsTaskMessageParser.ParsedTask parsedTask = SmsTaskMessageParser.parse(
            "\r\n  #Serena  \r\n \r\n Take out the trash \r\n 2026-09-30 \r\n ignored line \r\n");

        assertNotNull(parsedTask);
        assertEquals("Take out the trash", parsedTask.getDescription());
        assertEquals(new LocalDate(2026, 9, 30), parsedTask.getDeadline());
        assertFalse(parsedTask.isDeadlineUnreadable());
    }

    @Test
    public void parse_messageWithoutDeadline_returnsTaskWithoutDeadlineWarning()
    {
        SmsTaskMessageParser.ParsedTask parsedTask = SmsTaskMessageParser.parse(
            "#Serena\nTake out the trash");

        assertNotNull(parsedTask);
        assertNull(parsedTask.getDeadline());
        assertFalse(parsedTask.isDeadlineUnreadable());
    }

    @Test
    public void parse_blankDeadline_returnsTaskWithoutDeadlineWarning()
    {
        SmsTaskMessageParser.ParsedTask parsedTask = SmsTaskMessageParser.parse(
            "#Serena\nTake out the trash\n \t ");

        assertNotNull(parsedTask);
        assertNull(parsedTask.getDeadline());
        assertFalse(parsedTask.isDeadlineUnreadable());
    }

    @Test
    public void parse_unpaddedDeadline_returnsTaskWithDeadline()
    {
        SmsTaskMessageParser.ParsedTask parsedTask = SmsTaskMessageParser.parse(
            "#Serena\nTake out the trash\n2026-9-30");

        assertNotNull(parsedTask);
        assertEquals(new LocalDate(2026, 9, 30), parsedTask.getDeadline());
        assertFalse(parsedTask.isDeadlineUnreadable());
    }

    @Test
    public void parse_unreadableDeadline_returnsTaskWithDeadlineWarning()
    {
        SmsTaskMessageParser.ParsedTask parsedTask = SmsTaskMessageParser.parse(
            "#Serena\nTake out the trash\nnot a date");

        assertNotNull(parsedTask);
        assertNull(parsedTask.getDeadline());
        assertTrue(parsedTask.isDeadlineUnreadable());
    }

    @Test
    public void parse_impossibleCalendarDate_returnsTaskWithDeadlineWarning()
    {
        SmsTaskMessageParser.ParsedTask parsedTask = SmsTaskMessageParser.parse(
            "#Serena\nTake out the trash\n2026-02-30");

        assertNotNull(parsedTask);
        assertNull(parsedTask.getDeadline());
        assertTrue(parsedTask.isDeadlineUnreadable());
    }

    @Test
    public void parse_pastDeadline_preservesDeadline()
    {
        SmsTaskMessageParser.ParsedTask parsedTask = SmsTaskMessageParser.parse(
            "#Serena\nOld task\n2001-01-01");

        assertNotNull(parsedTask);
        assertEquals(new LocalDate(2001, 1, 1), parsedTask.getDeadline());
        assertFalse(parsedTask.isDeadlineUnreadable());
    }

    @Test
    public void parse_hashtagNotOnFirstNonEmptyLine_returnsNull()
    {
        assertNull(SmsTaskMessageParser.parse("Please do this\n#Serena\nTask"));
    }

    @Test
    public void parse_missingDescription_returnsNull()
    {
        assertNull(SmsTaskMessageParser.parse("#Serena\n \t\n"));
    }

    @Test
    public void parse_nullBody_returnsNull()
    {
        assertNull(SmsTaskMessageParser.parse(null));
    }
}
