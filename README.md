##What is it?
This program implements a genetic/evolutionary algorithm to build an optimize a schedule for a department. 
The scheduling includes courses, labs, course time slos, lab time slots. The optimization involves 
a list of preferences, schedulings that are incompatible, unwanted schedulings and pairs of labs/courses that should be taught at the same time.

The r.txt and r2.txt are the "real"/more complex problems. They contain:
  - 21 course slots
  - 32 lab slots
  - 48 courses
  - 134 labs
  - ~440 not compatible entries
  - 2 unwanted entries
  - ~ 77 preferences, each with their own penalty values
  
A very very rough estimate of the search space is ~13432^4821

#How does it work?
  It works by implementing a genetic/evolutionary algorithm. It starts by building 50 valid, yet poorly optimized solutions and
adding them into the "population." Once the starting state is constructed, the algorithm randomly chooses to do a mutate or a crossover.
When it does a mutate it picks 1 random schedule from the top 15% (in terms of optimization) and mutates it (randomly makes some changes). 
It then evaluates the new schedules fitness and puts it back into the population. If it chooses to do a crossover it picks 1 schedule from
the top 15% and then 1 completely random schedule and takes portions of each schedule and swaps them. It then evaluates the new
schedule and adds it into the population. As this process repeats the population size grows and grows. Once there are 15,000 schedules
it sorts them in terms of optimization and deletes the worst 5,000. As time goes on the average optimization (fitness) level of the population
grows. The algorithm can terminate on an iteration count, time count, or optimization level. It's worth noting that the most optimal 
solution for this problem is unknown (according to the professor). So the longer you run the program, the better your solution will be (in theory).

##How to run it
  The source code has been included, but does not compile nicely outside of the IDE that it was developed in (Eclipse). An executable
  jar has been included. Once the jar is run, it will prompt the user for an input file. Use any of the following: RealExample.txt, 
  ShortExample.txt, ShortExample2.txt, r.txt. r2.txt. After, it will prompt for parameter input. For general use, all 1's are fine.
  These parameters allow you to customize the optimization.
  
  The program in the jar file is hardcoded to run over a million times because a very good solution was needed for submission. 
  Therefore, it will takes around 1 hour to run the real examples, and around 1 minute to run the short examples.
  
  
##Other
This was a group project for CPSC 433 (Introduction to Artificial Intelligence). 
What wasn't coded by myself:
  - Anything to do with parsing the input file and handling user input
  - OrTree.java

Everything else was designed and coded by Brandon Brien. 
