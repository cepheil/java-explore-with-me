package ru.practicum.dto;


import java.time.format.DateTimeFormatter;

public class DateTimeConstants {
    private DateTimeConstants() {
    }

    public static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String URL_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(PATTERN);
    public static final DateTimeFormatter URL_FORMATTER = DateTimeFormatter.ofPattern(URL_PATTERN);

}
