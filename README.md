# Aniruddha Desai (ANZ Code Problem: Calculate VWAP)

## Problem Description

Given a stream of price data for multiple currency pairs in the form of:

[Timestamp, Currency-pair, Price, Volume]

We would like the solution to output the VWAP calculated over the input stream.

The VWAP should be calculated on an hourly window.

The VWAP should be calculated for each unique currency pair.

#### Example data:

An example stream of data would be something like the following:


| TIMESTAMP | CURRENCY-PAIR |  PRICE  |  VOLUME   |
|-----------|---------------|---------|-----------|
| 9:30 AM   |     AUD/USD   | 0.6905  | 106,198   |
| 9:31 AM   |     USD/JPY   | 142.497 | 30,995    |
| 9:32 AM   |     USD/JPY   | 139.392 | 2,890,000 |
| 9:33 AM   |     AUD/USD   | 0.6899  | 444,134   |
| 9:34 AM   |     NZD/GBP   | 0.4731  | 64,380    |
| 9:35 AM   |     NZD/GBP   | 0.4725  | 8,226,295 |

...etc


### Considerations
The incoming stream of data will be significant, and care should be taken to avoid JVM
crash.

### Deliverable
- Solve the problem as though it were “production level” code.
- The solution submitted should include source-code, configuration and any tests you
  deem necessary.
- Solve the problem in java.
- It is not required to provide any graphical interface.

## Assumption

- Data will be provided for a given day. (24 Hours)
- Data will be in CSV format. [Timestamp, Currency-pair, Price, Volume]
- CSV is already validated.
- VWAP is to be calculated for interval of 1 hour range.

## Usage

Step 1: Download Release v1.0 jar. (Approx. 400mb space will be needed for generating input datasets where jar will be placed. You can easily delete dataset afterwards, its only 5 files.)

Step 2: Execute ```java -cp aniruddha_desai_anz_vwap.jar org.anz.challenge.util.generator.BulkCSVGenerator``` to generate csv dataset. (It will take 2 - 3 Mins to generate dataset)

Step 3: Execute ```java -cp aniruddha_desai_anz_vwap.jar org.anz.challenge.Main 10_thousand 1``` to run VWAP calculation on 10_thousand records in sequential mode (1).

#### Possible dataset values are: 10_thousand, 100_thousand, 500_thousand, 1_million, 10_million

#### Possible Execution modes are: 1 for Sequential, 2 for Parallel, 3 for Single Thread, 4 for Multi Thread

So if you want to run VWAP calculation on dataset of 100_thousand in Parallel mode (2), you will need to run command ```java -cp aniruddha_desai_anz_vwap.jar org.anz.challenge.Main 100_thousand 2```

## Notes
- Logs will be generated under logs/app.log

## Observations

#### Formula used for calculating VWAP = Total of (price * volume of each trade) / total volume

1. Sequential (1) processing has the lowest memory footprint and performs better with smaller sets of data.
    - Optimal for less than 1 million records.
    - JVM memory as low as: ```-Xms2M -Xmx4M``` to Recommended: ```-Xms16M -Xmx32M```
    - Highly unlikely to crash JVM

2. Parallel (3) and Multi Threaded (4) processing performs better when dataset grows larger but so does the memory consumption.
    - Optimal for more than 1 million records
    - Need to adjust ```-Xmx``` param with increasing dataset. For 1 million records, start with Approx. ```-Xmx128M``` (suggested ```-Xmx256M``` or more)
    - JVM can crash in case of insufficient memory.
    - Need constant adjustments to ```-Xmx``` and ```-Xms``` parameters based on the size of input.

3. Not suited for this problem due to poor performance than above 3 solutions.
    - Need to adjust ```-Xmx``` param with increasing dataset. For 1 million records, start with Approx. ```-Xmx256M``` (suggested ```-Xmx512M``` or more)
    - JVM can crash in case of insufficient memory
    - Need constant adjustments to ```-Xmx``` and ```-Xms``` parameters based on the size of input.


## Final thoughts

- Sequential processing is sufficient for low memory - high performance calculation of hourly VWAP as long as dataset is smaller. Dataset of more than 1 million rows will still process with lower memory threshold but processing time will increase.

- Parallel or Multi Thread processing can be opted when dataset is growing more than 1 million records, need better performance and spare more memory.
