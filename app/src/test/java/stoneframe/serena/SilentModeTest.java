package stoneframe.serena;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import stoneframe.serena.mocks.TestContext;
import stoneframe.serena.model.settings.Settings;

public class SilentModeTest
{
    private TestContext context;

    private Settings settings;

    @BeforeEach
    public void before()
    {
        context = new TestContext();

        settings = context.getSettings();
    }

    @org.junit.jupiter.api.Test
    public void isSilenceModeEnabled_initialSettings_modeIsDisabled()
    {
        assertFalse(settings.isSilenceModeEnabled());
    }

    @Test
    public void isSilenceTime_initialSettings_isNotSilenceTime()
    {
        assertFalse(settings.isSilenceTime());
    }

    @ParameterizedTest
    @CsvSource({
        "0, false",
        "11, false",
        "12, true",
        "15, true",
        "18, true",
        "19, false",
        "21, false",
        "23, false",
    })
    public void isSilenceTime_startTimeIsLessThanStopTime_returnCorrectValue(
        int hourOfDay,
        boolean expectedResult)
    {
        // ARRANGE
        LocalTime currentTime = new LocalTime(hourOfDay, 0);

        settings.setSilenceModeEnabled(true);
        settings.setSilenceStartTime(new LocalTime(12, 0));
        settings.setSilenceStopTime(new LocalTime(18, 0));

        context.setCurrentTime(LocalDate.now().toLocalDateTime(currentTime));

        // ACT
        boolean isSilenceTime = settings.isSilenceTime();

        // ASSERT
        assertEquals(expectedResult, isSilenceTime);
    }

    @ParameterizedTest
    @CsvSource({
        "0, true",
        "4, true",
        "6, true",
        "11, false",
        "12, false",
        "15, false",
        "18, false",
        "19, false",
        "21, true",
        "23, true",
    })
    public void isSilenceTime_startTimeIsGreaterThanStopTime_returnCorrectValue(
        int hourOfDay,
        boolean expectedResult)
    {
        // ARRANGE
        LocalTime currentTime = new LocalTime(hourOfDay, 0);

        settings.setSilenceModeEnabled(true);
        settings.setSilenceStartTime(new LocalTime(21, 0));
        settings.setSilenceStopTime(new LocalTime(6, 0));

        context.setCurrentTime(LocalDate.now().toLocalDateTime(currentTime));

        // ACT
        boolean isSilenceTime = settings.isSilenceTime();

        // ASSERT
        assertEquals(expectedResult, isSilenceTime);
    }

    @ParameterizedTest
    @CsvSource({
        "0",
        "4",
        "8",
        "12",
        "16",
        "20",
    })
    public void isSilenceTime_doNotSetStartOrStopButEnable_returnFalse(int hourOfDay)
    {
        // ARRANGE
        LocalTime currentTime = new LocalTime(hourOfDay, 0);

        settings.setSilenceModeEnabled(true);

        context.setCurrentTime(LocalDate.now().toLocalDateTime(currentTime));

        // ACT
        boolean isSilenceTime = settings.isSilenceTime();

        // ASSERT
        assertFalse(isSilenceTime);
    }
}
