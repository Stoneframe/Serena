package stoneframe.serena.model.timeservices;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

public interface TimeService
{
    LocalDateTime getNow();

    LocalDate getToday();
}
