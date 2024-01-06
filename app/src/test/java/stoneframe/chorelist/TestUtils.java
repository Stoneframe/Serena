package stoneframe.chorelist;

import org.joda.time.DateTime;

public class TestUtils
{
    public static final DateTime MOCK_NOW = new DateTime()
        .withDate(2017, 2, 5).withTimeAtStartOfDay();

    public static final DateTime MOCK_MONDAY = new DateTime().withDate(2024, 1, 1);

    public static final DateTime MOCK_TUESDAY = new DateTime().withDate(2024, 1, 2);

    public static final DateTime MOCK_WEDNESDAY = new DateTime().withDate(2024, 1, 3);

    public static final DateTime MOCK_THURSDAY = new DateTime().withDate(2024, 1, 4);

    public static final DateTime MOCK_FRIDAY = new DateTime().withDate(2024, 1, 5);

    public static final DateTime MOCK_SATURDAY = new DateTime().withDate(2024, 1, 6);

    public static final DateTime MOCK_SUNDAY = new DateTime().withDate(2024, 1, 7);

    public static DateTime createDateTime(int year, int month, int day)
    {
        return new DateTime().withDate(year, month, day).withTimeAtStartOfDay();
    }
}
