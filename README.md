# Paurus import app

SimpleImportApplication is a CLI app, which takes entries from fo_random.txt and inserts them into a DB and prints out start and end insertion time.<br />
ImportApplicationWithDelay is a CLI app, which takes entries from fo_random.txt, adds a processing delay to events and inserts them into a DB in correct order.

## How to run

1. Install H2 database from https://www.h2database.com/html/main.html
2. Run the code

## Answer on questions

1. What can be done to speed up the process of writing it to the output? It's important to answer this before you continue.<br />
This depends on the environment, using a local DB with basically no latency on I/O there is not much that can be done, the file was processed and imported to DB in ~1 second. <br />
The easiest solution to speed it up is not using Java, using some faster language or using a pre-optimized native Java image, which would have no Java start up/JVM optimization cost.

2.  When you have the answer for the question above you have to make sure that 1A1 is written to output before 1B2, even though that (let's hope so) event 1B2 should be written before 1A1 due to processing speed.<br />
Based on this question my guess is the expected response for the first question should be using some kind of parallel processing like having multiple servers.<br />
Having the events sequential in DB is probably important due to having to have them processed in the correct order, otherwise just having them write to DB 
in correct order makes no sense because this can always be done when querying the events by sorting them in the query.
The easiest solution to having them processed in sequence is using a messaging queue, which waits for one event to be processed before processing the next one.<br />
To simulate this I implemented a simple mechanism in code so that each match processes its events one event after another and using a random processing delay.<br />
Executors.newSingleThreadExecutor internally uses a LinkedBlockingQueue and using virtual threads should make it have no limitation on number of parallel processes it can run. 

On production the better solution for this would be to solve this architecturally by implementing a messaging queue.

## Result logs (disable processing log, 0-50ms delay)

2024-02-17 21:28:19.0673 [main] INFO org.paurus.ImportApplicationWithDelay - Starting DB recreation<br />
2024-02-17 21:28:19.0689 [main] INFO org.paurus.ImportApplicationWithDelay - Completed DB recreation<br />
2024-02-17 21:28:19.0689 [main] INFO org.paurus.ImportApplicationWithDelay - Starting file processing<br />
2024-02-17 21:28:19.0956 [main] INFO org.paurus.ImportApplicationWithDelay - Completed file processing<br />
2024-02-17 21:28:19.0956 [main] INFO org.paurus.ImportApplicationWithDelay - Started waiting on all executors to complete<br />
2024-02-17 21:30:01.0142 [main] INFO org.paurus.ImportApplicationWithDelay - Completed waiting on all executors to complete <br />
2024-02-17 21:30:01.0142 [main] INFO org.paurus.ImportApplicationWithDelay - Starting data evaluation<br />
2024-02-17 21:30:01.0358 [main] INFO org.paurus.ImportApplicationWithDelay - Minimum insertion date: 2024-02-17 21:28:19.70679<br />
2024-02-17 21:30:01.0409 [main] INFO org.paurus.ImportApplicationWithDelay - Maximum insertion date: 2024-02-17 21:30:01.085812<br />
2024-02-17 21:30:01.0409 [main] INFO org.paurus.ImportApplicationWithDelay - Completed data evaluation

## Result logs (enabled processing log, 0-50ms delay) 

2024-02-17 21:47:29.0916 [main] INFO org.paurus.ImportApplicationWithDelay - Starting DB recreation<br />
2024-02-17 21:47:29.0925 [main] INFO org.paurus.ImportApplicationWithDelay - Completed DB recreation<br />
2024-02-17 21:47:29.0925 [main] INFO org.paurus.ImportApplicationWithDelay - Starting file processing<br />
2024-02-17 21:47:29.0932 ['sr:match:14198799'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14198799' with delay 17<br />
2024-02-17 21:47:29.0932 ['sr:match:12445138'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:12445138' with delay 34<br />
2024-02-17 21:47:29.0932 ['sr:match:12261810'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:12261810' with delay 18<br />
2024-02-17 21:47:29.0932 ['sr:match:12148234'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:12148234' with delay 38<br />
2024-02-17 21:47:29.0932 ['sr:match:13762991'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:13762991' with delay 20<br />
2024-02-17 21:47:29.0936 ['sr:match:14203679'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14203679' with delay 17<br />
2024-02-17 21:47:29.0936 ['sr:match:11881074'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:11881074' with delay 18<br />
2024-02-17 21:47:29.0938 ['sr:match:13857921'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:13857921' with delay 38<br />
2024-02-17 21:47:29.0936 ['sr:match:14181707'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14181707' with delay 17<br />
...<br />
...<br />
...<br />
2024-02-17 21:48:16.0180 ['sr:match:14198097'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14198097' with delay 17<br />
2024-02-17 21:48:16.0196 ['sr:match:14186125'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14186125' with delay 15<br />
2024-02-17 21:48:16.0196 ['sr:season:48238'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:season:48238' with delay 45<br />
2024-02-17 21:48:16.0212 ['sr:match:14198097'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14198097' with delay 18<br />
2024-02-17 21:48:16.0212 ['sr:match:14186125'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14186125' with delay 42<br />
2024-02-17 21:48:16.0212 ['sr:match:14162845'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14162845' with delay 38<br />
2024-02-17 21:48:16.0244 ['sr:season:48238'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:season:48238' with delay 45<br />
2024-02-17 21:48:16.0244 ['sr:match:14198097'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14198097' with delay 30<br />
2024-02-17 21:48:16.0260 ['sr:match:14186125'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14186125' with delay 15<br />
2024-02-17 21:48:16.0260 ['sr:match:14162845'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14162845' with delay 16<br />
2024-02-17 21:48:16.0276 ['sr:match:14186125'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14186125' with delay 15<br />
2024-02-17 21:48:16.0276 ['sr:match:14198097'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14198097' with delay 18<br />
2024-02-17 21:48:16.0292 ['sr:match:14162845'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14162845' with delay 32<br />
2024-02-17 21:48:16.0292 ['sr:season:48238'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:season:48238' with delay 45<br />
2024-02-17 21:48:16.0292 ['sr:match:14186125'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14186125' with delay 34<br />
2024-02-17 21:48:16.0308 ['sr:match:14198097'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14198097' with delay 17<br />
2024-02-17 21:48:16.0340 ['sr:match:14162845'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14162845' with delay 16<br />
2024-02-17 21:48:16.0340 ['sr:match:14198097'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14198097' with delay 3<br />
2024-02-17 21:48:16.0340 ['sr:season:48238'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:season:48238' with delay 45<br />
2024-02-17 21:48:16.0340 ['sr:match:14186125'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14186125' with delay 24<br />
2024-02-17 21:48:16.0356 ['sr:match:14198097'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14198097' with delay 36<br />
2024-02-17 21:48:16.0372 ['sr:match:14186125'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14186125' with delay 15<br />
2024-02-17 21:48:16.0372 ['sr:match:14162845'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14162845' with delay 32<br />
2024-02-17 21:48:16.0388 ['sr:match:14186125'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:match:14186125' with delay 41<br />
...<br />
...<br />
...<br />
2024-02-17 21:49:12.0295 ['sr:season:48238'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:season:48238' with delay 45<br />
2024-02-17 21:49:12.0343 ['sr:season:48238'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:season:48238' with delay 45<br />
2024-02-17 21:49:12.0391 ['sr:season:48238'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:season:48238' with delay 45<br />
2024-02-17 21:49:12.0439 ['sr:season:48238'] INFO org.paurus.ImportApplicationWithDelay - Processing 'sr:season:48238' with delay 45<br />
2024-02-17 21:49:12.0539 [main] INFO org.paurus.ImportApplicationWithDelay - Completed waiting on all executors to complete<br />
2024-02-17 21:49:12.0539 [main] INFO org.paurus.ImportApplicationWithDelay - Starting data evaluation<br />
2024-02-17 21:49:12.0754 [main] INFO org.paurus.ImportApplicationWithDelay - Minimum insertion date: 2024-02-17 21:47:29.980732<br />
2024-02-17 21:49:12.0885 [main] INFO org.paurus.ImportApplicationWithDelay - Maximum insertion date: 2024-02-17 21:49:12.486921<br />
2024-02-17 21:49:12.0885 [main] INFO org.paurus.ImportApplicationWithDelay - Completed data evaluation


## Known problems

* The implementation has no input validation and just dumps data to DB. As this was implemented already in Part1 I don't think it adds value implementing it again here.
* Everything is in one class.
* Exception handling is done by using @SneakyThrows
* Connections/thread pools are not closed