package org.ject.support.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public final class DateTimeUtil {

    private static final String[] DAY_OF_WEEK_NAMES = {"월", "화", "수", "목", "금", "토", "일"};
    public static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter DEFAULT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DEFAULT_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    private DateTimeUtil() {
        // 생성 제한
    }

    /**
     * LocalDateTime → 기본 포맷(yyyy-MM-dd HH:mm)
     */
    public static String format(LocalDateTime dateTime) {
        return format(dateTime, DEFAULT_DATETIME_FORMATTER);
    }

    /**
     * LocalDate → 기본 포맷(yyyy-MM-dd)
     */
    public static String format(LocalDate date) {
        return format(date, DEFAULT_DATE_FORMATTER);
    }

    /**
     * LocalTime → 기본 포맷(HH:mm)
     */
    public static String format(LocalTime time) {
        return format(time, DEFAULT_TIME_FORMATTER);
    }

    /**
     * LocalDateTime → 지정 포맷
     */
    public static String format(LocalDateTime dateTime, DateTimeFormatter formatter) {
        return Optional.ofNullable(dateTime)
                .map(dt -> dt.format(formatter))
                .orElse("");
    }

    /**
     * LocalDate → 지정 포맷
     */
    public static String format(LocalDate date, DateTimeFormatter formatter) {
        return Optional.ofNullable(date)
                .map(d -> d.format(formatter))
                .orElse("");
    }

    /**
     * LocalTime → 지정 포맷
     */
    public static String format(LocalTime time, DateTimeFormatter formatter) {
        return Optional.ofNullable(time)
                .map(t -> t.format(formatter))
                .orElse("");
    }

    /**
     * 날짜 + 요일 한글 표시 (예: "2025년 10월 21일(화) 13:45")
     */
    public static String formatWithDayOfWeek(LocalDateTime dateTime) {
        return Optional.ofNullable(dateTime)
                .map(dt -> {
                    String dayOfWeek = DAY_OF_WEEK_NAMES[dt.getDayOfWeek().getValue() - 1];
                    String datePart = dt.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
                    String timePart = dt.format(DateTimeFormatter.ofPattern("HH:mm"));
                    return String.format("%s(%s) %s", datePart, dayOfWeek, timePart);
                })
                .orElse("");
    }
}
