package stoneframe.chorelist;

import org.joda.time.DateTime;

public class TestUtils
{

    public static final DateTime MOCK_NOW = new DateTime()
        .withDate(2017, 2, 5).withTimeAtStartOfDay();

    public static DateTime createDateTime(int year, int month, int day)
    {
        return new DateTime().withDate(year, month, day).withTimeAtStartOfDay();
    }

}
