package com.tokenwise.utils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.time.temporal.ChronoUnit;

public class Helper {

    // Helper to safely parse Long
    public static Long getLong(Object obj) {
        if (obj instanceof Number) return ((Number) obj).longValue();
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    // Helper to safely cast to List<Map<String, Object>>
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getList(Object obj) {
        if (obj instanceof List<?>) {
            List<?> list = (List<?>) obj;
            if (!list.isEmpty() && list.get(0) instanceof Map) {
                return (List<Map<String, Object>>) obj;
            }
        }
        return null;
    }

    // Helper: Converts window or start/end to [startEpoch, endEpoch]
    public static long[] resolveTimeRange(String window, Long start, Long end) {
        Instant now = Instant.now();
        Instant startTime;
        Instant endTime = now;

        if (window != null && !window.isEmpty()) {
            switch (window.toLowerCase()) {
                case "last_hour":     startTime = now.minus(1, ChronoUnit.HOURS); break;
                case "last_6_hours":  startTime = now.minus(6, ChronoUnit.HOURS); break;
                case "last_day":      startTime = now.minus(1, ChronoUnit.DAYS);  break;
                case "last_2_days":   startTime = now.minus(2, ChronoUnit.DAYS);  break;
                case "last_week":     startTime = now.minus(7, ChronoUnit.DAYS);  break;
                case "last_month":    startTime = now.minus(30, ChronoUnit.DAYS); break;
                default:              startTime = now.minus(1, ChronoUnit.DAYS);  break;
            }
        } else if (start != null && end != null) {
            startTime = Instant.ofEpochSecond(start);
            endTime = Instant.ofEpochSecond(end);
        } else {
            // Default: last day
            startTime = now.minus(1, ChronoUnit.DAYS);
        }
        return new long[]{startTime.getEpochSecond(), endTime.getEpochSecond()};
    }
}

