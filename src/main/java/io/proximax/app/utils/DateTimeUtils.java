package io.proximax.app.utils;

import de.slackspace.openkeepass.domain.Times;
import de.slackspace.openkeepass.domain.TimesBuilder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author thcao
 */
public class DateTimeUtils {

    public static Calendar localDateTime2Calendar(LocalDate localDate, LocalTime localTime) {
        Date date = Date.from(LocalDateTime.of(localDate, localTime).atZone(ZoneId
                .systemDefault()).toInstant());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static LocalDate calendar2LocalDate(Calendar calendar) {
        return calendar.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
    
    public static LocalDate nowDate() {
        Calendar now = Calendar.getInstance();
        return now.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
    
    public static LocalTime nowTime() {
        Calendar now = Calendar.getInstance();
        return now.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }

    public static LocalTime calendar2LocalTime(Calendar calendar) {
        return calendar.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }

    public static Times createTimesDefault() {
        Calendar now = Calendar.getInstance();
        return new TimesBuilder().creationTime(now).lastAccessTime(now).lastModificationTime(now).locationChanged(now).build();
    }
    
    public static Times createFullTimesDefault() {
        Calendar now = Calendar.getInstance();
        return new TimesBuilder().expiryTime(now).creationTime(now).lastAccessTime(now).lastModificationTime(now).locationChanged(now).build();
    }
    
    public static TimesBuilder createTimeBuilder(Times times) {
         TimesBuilder timeBuilder = null;
        if (times != null) {
            timeBuilder = new TimesBuilder(times);
        } else {
            timeBuilder = createTimeBuilderDefault();
        }
        return timeBuilder;
    }
    
    public static TimesBuilder createTimeBuilderDefault() {
        Calendar now = Calendar.getInstance();
        return new TimesBuilder().creationTime(now).lastAccessTime(now).lastModificationTime(now).locationChanged(now);
    }

}
