package org.anz.challenge.util;

import org.anz.challenge.data.Values;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HelperUtil {
    public static String getTimeRange(String timestamp) throws ParseException {
        if (isValidTimeStamp(timestamp)) {
            DateFormat format = new SimpleDateFormat("hh:mm a");
            Instant instant = format.parse(timestamp).toInstant();
            LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            String start = ldt.get(ChronoField.HOUR_OF_AMPM) + 1 + ":00 " + (ldt.get(ChronoField.AMPM_OF_DAY) == 0 ? "AM" : "PM");
            String end = ldt.plusHours(1).get(ChronoField.HOUR_OF_AMPM) + 1 + ":00 " + (ldt.get(ChronoField.AMPM_OF_DAY) == 0 ? "AM" : "PM");

            return ("0" + start).substring(start.length() - 7) + " - " + ("0" + end).substring(end.length() - 7);
        }
        return "XXXXXXX";
    }

    public static boolean isValidTimeStamp(String timestamp) {
        DateFormat format = new SimpleDateFormat("hh:mm a");
        try {
            format.parse(timestamp);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    public static void printFinalOutput(Map<String, Values> inputMap) {
        Map<String, Map<String, Values>> output = new HashMap<>();

        for (Map.Entry<String, Values> entry : inputMap.entrySet()) {
            String key = entry.getKey();
            Values value = entry.getValue();

            String currencyKey = key.substring(0, key.indexOf("~"));
            String timeRangeKey = key.substring(key.indexOf('~') + 1);

            output.putIfAbsent(currencyKey, new LinkedHashMap<>());
            output.get(currencyKey).put(timeRangeKey, value);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

        output.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().entrySet().stream()
                                .sorted(Map.Entry.comparingByKey(
                                        (timeRange1, timeRange2) -> {
                                            // Extract the start time for comparison
                                            LocalTime startTime1 = LocalTime.parse(timeRange1.split(" - ")[0], formatter);
                                            LocalTime startTime2 = LocalTime.parse(timeRange2.split(" - ")[0], formatter);
                                            return startTime1.compareTo(startTime2);
                                        }
                                )) // Sort inner keys based on start time
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (oldValue, newValue) -> oldValue, // Not needed, but required for toMap
                                        LinkedHashMap::new // Maintain insertion order
                                )),
                        (oldValue, newValue) -> oldValue, // This will never be called as there are no duplicates
                        LinkedHashMap::new // Maintain insertion order for outer map
                )).forEach((currencyPair, map) -> { // Printing final result
                    System.out.println("Currency Pair: " + currencyPair);
                    Log.info("Currency Pair: " + currencyPair);
                    map.forEach((timeRange, value) -> {
                        System.out.println("Time: " + timeRange + "  |  " + "VWAP: " + value.getVwap());
                        Log.info("Time: " + timeRange + "  |  " + "VWAP: " + value.getVwap());
                    });
                    System.out.println();
                    Log.newLine();
                });
    }
}
