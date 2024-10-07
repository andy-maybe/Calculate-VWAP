package org.anz.challenge.util.generator;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;

public class BulkCSVGenerator {

    // Method to generate random time in 12-hour format (HH:mm AM/PM)
    private static String getRandomTime() {
        Random rand = new Random();
        int hour = rand.nextInt(12) + 1;
        int minute = rand.nextInt(60);
        String amPm = rand.nextBoolean() ? "AM" : "PM";
        return String.format("%02d:%02d %s", hour, minute, amPm);
    }

    // Method to get a random currency pair
    private static String getRandomCurrencyPair() {
        String[] pairs = {"AUD/USD", "USD/JPY", "NZD/GBP", "INR/AUD", "EUR/USD", "USD/CAD"};
        Random rand = new Random();
        return pairs[rand.nextInt(pairs.length)];
    }

    // Method to generate a random price
    private static String getRandomPrice(String currencyPair) {
        Random rand = new Random();
        double priceStart;
        double priceEnd;
        switch (currencyPair) {
            case "AUD/USD":
                priceStart = 0.6600;
                priceEnd = 0.6999;
                break;
            case "USD/JPY":
                priceStart = 140.0000;
                priceEnd = 149.9999;
                break;
            case "NZD/GBP":
                priceStart = 0.4650;
                priceEnd = 0.4759;
                break;
            case "INR/AUD":
                priceStart = 0.0160;
                priceEnd = 0.0239;
                break;
            case "EUR/USD":
                priceStart = 1.0400;
                priceEnd = 1.1299;
                break;
            case "USD/CAD":
                priceStart = 1.3000;
                priceEnd = 1.4099;
                break;
            default:
                priceStart = 0;
                priceEnd = 1;
        }
        double price = priceStart + (priceEnd - priceStart) * rand.nextDouble(); // Generate price between 0.4 and 1.5
        DecimalFormat df = new DecimalFormat("#.####");
        return df.format(price);
    }

    // Method to generate a random volume
    private static int getRandomVolume() {
        Random rand = new Random();
        return rand.nextInt(100000000); // Generate volume up to 100 million
    }

    public static void main(String[] args) {
        generateCSV("10_thousand", 10000);
        generateCSV("100_thousand", 100000);
        generateCSV("500_thousand", 500000);
        generateCSV("1_million", 1000000);
        generateCSV("10_million", 10000000);
    }

    private static void generateCSV(String fileName, int numOfRecords) {

        try (FileWriter writer = new FileWriter(fileName)) {
            // Write the header
            writer.append("TIMESTAMP,CURRENCY-PAIR,PRICE,VOLUME\n");

            // Generate random dataset
            for (int i = 0; i < numOfRecords; i++) {
                String timestamp = getRandomTime();
                String currencyPair = getRandomCurrencyPair();
                String price = getRandomPrice(currencyPair);
                int volume = getRandomVolume();

                // Write a row in CSV format
                writer.append(timestamp).append(",")
                        .append(currencyPair).append(",")
                        .append(price).append(",")
                        .append(String.valueOf(volume)).append("\n");
            }

            writer.close();
            System.out.println("CSV file generated successfully: " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }
}
