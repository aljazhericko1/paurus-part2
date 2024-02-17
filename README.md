# Paurus import app

Simple CLI app, which takes entries from data.txt and inserts them into a DB and prints out start and end insertion time.

## How to run

1. Install H2 database from https://www.h2database.com/html/main.html
2. Run the code

##

We're sharing another perspective to this problem for better
understanding. Let's say that we mark events (data in data set) with a
number (match_id) and type of the event (timestamp of processing to be
exact) and another number that tells us time of occurrence/sequence in
queue. Format is then: number, letter and number (i.e. 1A1 translates
as: match_id 1, event type A, first event of such type 1).

A = processed in 1s
B = processed in 1ms

Queue (fills from left to right, first element to be processed is on far
right):

3B3 3A2 2A2 1A4 1B3 1B2 3B1 2A1 1A1
1. What can be done to speed up the process of writing it to the output?
   It's important to answer this before you continue.
2. When you have the answer for the question above you have to make sure
   that 1A1 is written to output before 1B2, even though that (let's hope
   so) event 1B2 should be written before 1A1 due to processing speed.