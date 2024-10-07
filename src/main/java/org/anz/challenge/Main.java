package org.anz.challenge;

import org.anz.challenge.util.HelperUtil;
import org.anz.challenge.util.Log;
import org.anz.challenge.util.RuntimeUtil;
import org.anz.challenge.data.Values;
import org.anz.challenge.process.ExecutionType;
import org.anz.challenge.process.ProcessLine;
import org.anz.challenge.process.ProcessLineThread;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Main {

    // Formula for calculating VWAP
    // Total of (price * volume) for each trade / total volume

    /*
        args[0] - input csv filename. examples: 10_thousand.csv, 100_thousand.csv, 500_thousand.csv, 1_million.csv, 10_million.csv
        args[1] - execution type. 1 for sequential (low memory consumption) / 2 for parallel, 3 for single threaded, 4 for multithreaded (high memory consumption)

        Low memory footprint / Comparatively slower performance / Optimal for less than 1 million records
        1 sequential (From: -Xms2M -Xmx4M to Recommended: -Xms16M -Xmx32M)

        High memory footprint / Comparatively faster performance / Optimal for more than 1 million records
        3 single threaded (Not Recommended) (Need to adjust -Xmx param with increasing dataset. Approx -Xmx256M for 1_million.csv and increasing there after)
        2 parallel / 4 multithreaded (Need to adjust -Xmx param with increasing dataset. Approx -Xmx128M (suggested -Xmx256M or more) for 1_million.csv and increasing there after)
     */
    public static void main (String[] args) {
        RuntimeUtil.printSystemInformation();
        Log.info("Execution Started");

        // Get input file and execution type from arguments
        String inputFileName;
        ExecutionType executionType;

        switch (args.length) {
            case 0:
                inputFileName = "10_thousand.csv";
                executionType = ExecutionType.Sequential;
                break;
            case 1:
                inputFileName = args[0];
                executionType = ExecutionType.Sequential;
                break;
            case 2:
            default:
                inputFileName = args[0];
                executionType = ExecutionType.fromFlag(args[1]);
                break;
        }

        // Printing inputs
        Log.info("Input File Name: " + inputFileName);
        System.out.println("Input File Name: " + inputFileName);
        Log.info("Execution Type: " + executionType);
        System.out.println("Execution Type: " + executionType);


        long startTime;
        long endTime;
        // Execution for sum of (price * volume) and sum of volume
        switch (executionType) {
            case Parallel:
                startTime = System.currentTimeMillis();
                // Maintaining map for sum of (price * volume) and sum of volume
                Map<String, Values> parallelCalMap = new ConcurrentHashMap<>();

                // Reading CSV in parallel
                try (Stream<String> lines = Files.lines(Paths.get(inputFileName)).parallel()) {
                    lines.skip(1).forEach(line -> {
                        StringTokenizer tokens = new StringTokenizer(line, ",");
                        String timestamp = tokens.nextToken();
                        String currencyPair = tokens.nextToken();
                        double price = Double.parseDouble(tokens.nextToken());
                        long volume = Long.parseLong(tokens.nextToken());

                        ProcessLine.execute(timestamp, currencyPair, price, volume, parallelCalMap);
                    });
                } catch (IOException e) {
                    Log.error("Parallel :: Error reading input file", e);
                    System.out.println("Parallel :: Error reading input file");
                } catch (OutOfMemoryError oe) {
                    Log.error("Parallel :: Out of memory.", oe);
                }
                parallelCalMap.values().forEach(Values::calculateVWAP); // Final calculation of VWAP
                HelperUtil.printFinalOutput(parallelCalMap); // Printing the result (can return the map if printing is not needed)
                endTime = System.currentTimeMillis();
                break;
            case SingleThread:
            case MultiThread:
                startTime = System.currentTimeMillis();
                // Maintaining map for sum of (price * volume) and sum of volume
                Map<String, Values> threadSafeMap = Collections.synchronizedMap(new HashMap<>());

                ExecutorService threadExecutor;
                if (ExecutionType.SingleThread.equals(executionType)) {
                    threadExecutor = Executors.newSingleThreadExecutor();
                } else {
                    int threadCount = Runtime.getRuntime().availableProcessors() + 2;
                    Log.info("Thread Count: " + threadCount);
                    threadExecutor = Executors.newFixedThreadPool(threadCount);
                }

                // Reading CSV sequentially
                try (BufferedReader br = Files.newBufferedReader(Paths.get(inputFileName))) {
                    Iterable<CSVRecord> records = CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).build().parse(br);
                    for (CSVRecord record : records) {
                        String timestamp = record.get("TIMESTAMP");
                        String currencyPair = record.get("CURRENCY-PAIR");
                        double price = Double.parseDouble(record.get("PRICE"));
                        long volume = Long.parseLong(record.get("VOLUME"));
                        threadExecutor.execute(new ProcessLineThread(timestamp, currencyPair, price, volume, threadSafeMap));
                    }
                } catch (IOException e) {
                    Log.error("Thread :: Error reading input file.", e);
                    System.out.println("Thread :: Error reading input file.");
                } catch (OutOfMemoryError oe) {
                    Log.error("Thread :: Out of memory.", oe);
                    System.out.println("Thread :: Out of memory.");
                } finally {
                    threadExecutor.shutdown();
                    try {
                        assert threadExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                    } catch (InterruptedException ie) {
                        Log.error("Thread :: Thread " + Thread.currentThread() + " Interrupted", ie);
                        System.out.println("Thread :: Thread " + Thread.currentThread() + " Interrupted");
                    }
                }

                threadSafeMap.values().forEach(Values::calculateVWAP); // Final calculation of VWAP
                HelperUtil.printFinalOutput(threadSafeMap); // Printing the result (can return the map if printing is not needed)
                endTime = System.currentTimeMillis();
                break;
            case Sequential:
            default:
                startTime = System.currentTimeMillis();
                // Maintaining map for sum of (price * volume) and sum of volume
                Map<String, Values> seqCalMap = new HashMap<>();

                // Reading CSV sequentially
                try (BufferedReader br = Files.newBufferedReader(Paths.get(inputFileName))) {
                    Iterable<CSVRecord> records = CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).build().parse(br);
                    for (CSVRecord record : records) {
                        // "9:30 AM", "AUD/USD", 0.6905, 106198
                        // process the line.
                        String timestamp = record.get("TIMESTAMP");
                        String currencyPair = record.get("CURRENCY-PAIR");
                        double price = Double.parseDouble(record.get("PRICE"));
                        long volume = Long.parseLong(record.get("VOLUME"));
                        ProcessLine.execute(timestamp, currencyPair, price, volume, seqCalMap);

                    }
                } catch (IOException e) {
                    Log.error("Sequential :: Error reading input file.", e);
                    System.out.println("Sequential :: Error reading input file.");
                }
                seqCalMap.values().forEach(Values::calculateVWAP); // Final calculation of VWAP
                HelperUtil.printFinalOutput(seqCalMap); // Printing the result (can return the map if printing is not needed)
                endTime = System.currentTimeMillis();
                break;
        }

        long elapsedTime = endTime - startTime;

        System.out.println("Total Time: " + elapsedTime + "ms");
        Log.info("Total Time: " + elapsedTime + "ms");

        RuntimeUtil.printSystemInformation();
    }

}