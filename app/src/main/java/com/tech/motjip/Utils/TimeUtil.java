package com.tech.motjip.Utils;

import android.os.Build;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil {

    private static final Locale KOREAN_LOCALE =
            Locale.KOREA;

    private static final String KOREA_ZONE_ID =
            "Asia/Seoul";

    public static String formatChatTime(
            String time
    ) {

        if (time == null
                || time.trim().isEmpty()) {

            return "";
        }

        String trimmedTime =
                time.trim();

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                LocalDateTime dateTime =
                        parseToKoreaDateTime(
                                trimmedTime
                        );

                if (dateTime == null) {

                    return trimmedTime;
                }

                LocalDate today =
                        LocalDate.now(
                                ZoneId.of(
                                        KOREA_ZONE_ID
                                )
                        );

                LocalDate messageDate =
                        dateTime.toLocalDate();

                if (messageDate.isEqual(
                        today
                )) {

                    return dateTime.format(
                            DateTimeFormatter.ofPattern(
                                    "a h:mm",
                                    KOREAN_LOCALE
                            )
                    );
                }

                if (messageDate.isEqual(
                        today.minusDays(
                                1
                        )
                )) {

                    return "어제";
                }

                if (messageDate.getYear()
                        == today.getYear()) {

                    return dateTime.format(
                            DateTimeFormatter.ofPattern(
                                    "M월 d일",
                                    KOREAN_LOCALE
                            )
                    );
                }

                return dateTime.format(
                        DateTimeFormatter.ofPattern(
                                "yyyy.MM.dd",
                                KOREAN_LOCALE
                        )
                );
            }

            return formatChatTimeLegacy(
                    trimmedTime
            );

        } catch (Exception e) {

            return trimmedTime;
        }
    }

    public static String formatMessageTime(
            String time
    ) {

        if (time == null
                || time.trim().isEmpty()) {

            return "";
        }

        String trimmedTime =
                time.trim();

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                LocalDateTime dateTime =
                        parseToKoreaDateTime(
                                trimmedTime
                        );

                if (dateTime == null) {

                    return trimmedTime;
                }

                return dateTime.format(
                        DateTimeFormatter.ofPattern(
                                "a h:mm",
                                KOREAN_LOCALE
                        )
                );
            }

            return formatMessageTimeLegacy(
                    trimmedTime
            );

        } catch (Exception e) {

            return trimmedTime;
        }
    }

    public static String formatMessageDateHeader(
            String time
    ) {

        if (time == null
                || time.trim().isEmpty()) {

            return "";
        }

        String trimmedTime =
                time.trim();

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                LocalDateTime dateTime =
                        parseToKoreaDateTime(
                                trimmedTime
                        );

                if (dateTime == null) {

                    return "";
                }

                return dateTime.format(
                        DateTimeFormatter.ofPattern(
                                "yyyy년 M월 d일 EEEE",
                                KOREAN_LOCALE
                        )
                );
            }

            Date date =
                    parseLegacyDate(
                            trimmedTime
                    );

            if (date == null) {

                return "";
            }

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "yyyy년 M월 d일 EEEE",
                            KOREAN_LOCALE
                    );

            format.setTimeZone(
                    TimeZone.getTimeZone(
                            KOREA_ZONE_ID
                    )
            );

            return format.format(
                    date
            );

        } catch (Exception e) {

            return "";
        }
    }

    public static String getMessageDateKey(
            String time
    ) {

        if (time == null
                || time.trim().isEmpty()) {

            return "";
        }

        String trimmedTime =
                time.trim();

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                LocalDateTime dateTime =
                        parseToKoreaDateTime(
                                trimmedTime
                        );

                if (dateTime == null) {

                    return "";
                }

                return dateTime.toLocalDate()
                        .toString();
            }

            Date date =
                    parseLegacyDate(
                            trimmedTime
                    );

            if (date == null) {

                return "";
            }

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "yyyy-MM-dd",
                            KOREAN_LOCALE
                    );

            format.setTimeZone(
                    TimeZone.getTimeZone(
                            KOREA_ZONE_ID
                    )
            );

            return format.format(
                    date
            );

        } catch (Exception e) {

            return "";
        }
    }

    private static LocalDateTime parseToKoreaDateTime(
            String time
    ) {

        try {

            if (time.contains(
                    "T"
            )) {

                return LocalDateTime.parse(
                        removeNanoIfNeeded(
                                time
                        )
                );
            }

            if (time.matches(
                    "\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?"
            )) {

                String cleanedTime =
                        removeNanoIfNeeded(
                                time
                        );

                LocalTime localTime =
                        LocalTime.parse(
                                cleanedTime,
                                DateTimeFormatter.ofPattern(
                                        "HH:mm:ss"
                                )
                        );

                LocalDate today =
                        LocalDate.now(
                                ZoneId.of(
                                        KOREA_ZONE_ID
                                )
                        );

                return LocalDateTime.of(
                        today,
                        localTime
                );
            }

            if (time.matches(
                    "\\d{2}:\\d{2}"
            )) {

                LocalTime localTime =
                        LocalTime.parse(
                                time,
                                DateTimeFormatter.ofPattern(
                                        "HH:mm"
                                )
                        );

                LocalDate today =
                        LocalDate.now(
                                ZoneId.of(
                                        KOREA_ZONE_ID
                                )
                        );

                return LocalDateTime.of(
                        today,
                        localTime
                );
            }

            if (time.matches(
                    "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}(\\.\\d+)?"
            )) {

                String normalized =
                        removeNanoIfNeeded(
                                time.replace(
                                        " ",
                                        "T"
                                )
                        );

                return LocalDateTime.parse(
                        normalized
                );
            }

        } catch (Exception e) {

            return null;
        }

        return null;
    }

    private static String removeNanoIfNeeded(
            String value
    ) {

        if (value == null) {

            return "";
        }

        String trimmed =
                value.trim();

        if (!trimmed.contains(
                "."
        )) {

            return trimmed;
        }

        int dotIndex =
                trimmed.indexOf(
                        "."
                );

        String front =
                trimmed.substring(
                        0,
                        dotIndex
                );

        String nano =
                trimmed.substring(
                        dotIndex + 1
                );

        if (nano.length() > 9) {

            nano =
                    nano.substring(
                            0,
                            9
                    );
        }

        while (nano.length() < 9) {

            nano =
                    nano + "0";
        }

        return front
                + "."
                + nano;
    }

    private static String formatChatTimeLegacy(
            String time
    ) {

        Date date =
                parseLegacyDate(
                        time
                );

        if (date == null) {

            return time;
        }

        SimpleDateFormat todayFormat =
                new SimpleDateFormat(
                        "yyyyMMdd",
                        KOREAN_LOCALE
                );

        todayFormat.setTimeZone(
                TimeZone.getTimeZone(
                        KOREA_ZONE_ID
                )
        );

        Date now =
                new Date();

        String today =
                todayFormat.format(
                        now
                );

        String messageDay =
                todayFormat.format(
                        date
                );

        if (today.equals(
                messageDay
        )) {

            SimpleDateFormat timeFormat =
                    new SimpleDateFormat(
                            "a h:mm",
                            KOREAN_LOCALE
                    );

            timeFormat.setTimeZone(
                    TimeZone.getTimeZone(
                            KOREA_ZONE_ID
                    )
            );

            return timeFormat.format(
                    date
            );
        }

        SimpleDateFormat yearFormat =
                new SimpleDateFormat(
                        "yyyy",
                        KOREAN_LOCALE
                );

        yearFormat.setTimeZone(
                TimeZone.getTimeZone(
                        KOREA_ZONE_ID
                )
        );

        String currentYear =
                yearFormat.format(
                        now
                );

        String messageYear =
                yearFormat.format(
                        date
                );

        if (currentYear.equals(
                messageYear
        )) {

            SimpleDateFormat dateFormat =
                    new SimpleDateFormat(
                            "M월 d일",
                            KOREAN_LOCALE
                    );

            dateFormat.setTimeZone(
                    TimeZone.getTimeZone(
                            KOREA_ZONE_ID
                    )
            );

            return dateFormat.format(
                    date
            );
        }

        SimpleDateFormat oldDateFormat =
                new SimpleDateFormat(
                        "yyyy.MM.dd",
                        KOREAN_LOCALE
                );

        oldDateFormat.setTimeZone(
                TimeZone.getTimeZone(
                        KOREA_ZONE_ID
                )
        );

        return oldDateFormat.format(
                date
        );
    }

    private static String formatMessageTimeLegacy(
            String time
    ) {

        Date date =
                parseLegacyDate(
                        time
                );

        if (date == null) {

            return time;
        }

        SimpleDateFormat timeFormat =
                new SimpleDateFormat(
                        "a h:mm",
                        KOREAN_LOCALE
                );

        timeFormat.setTimeZone(
                TimeZone.getTimeZone(
                        KOREA_ZONE_ID
                )
        );

        return timeFormat.format(
                date
        );
    }

    private static Date parseLegacyDate(
            String time
    ) {

        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss.SSS",
                "yyyy-MM-dd HH:mm:ss",
                "HH:mm:ss",
                "HH:mm"
        };

        for (String pattern : patterns) {

            try {

                SimpleDateFormat format =
                        new SimpleDateFormat(
                                pattern,
                                KOREAN_LOCALE
                        );

                format.setTimeZone(
                        TimeZone.getTimeZone(
                                KOREA_ZONE_ID
                        )
                );

                return format.parse(
                        trimFractionForLegacy(
                                time
                        )
                );

            } catch (ParseException ignored) {

            }
        }

        return null;
    }

    private static String trimFractionForLegacy(
            String time
    ) {

        if (time == null) {

            return "";
        }

        String trimmed =
                time.trim();

        if (!trimmed.contains(
                "."
        )) {

            return trimmed;
        }

        int dotIndex =
                trimmed.indexOf(
                        "."
                );

        String front =
                trimmed.substring(
                        0,
                        dotIndex
                );

        String fraction =
                trimmed.substring(
                        dotIndex + 1
                );

        if (fraction.length() > 3) {

            fraction =
                    fraction.substring(
                            0,
                            3
                    );
        }

        while (fraction.length() < 3) {

            fraction =
                    fraction + "0";
        }

        return front
                + "."
                + fraction;
    }
}