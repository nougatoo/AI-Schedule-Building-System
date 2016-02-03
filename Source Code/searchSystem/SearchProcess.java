package searchSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import main.Main;
import types.Course;
import types.Lab;
import types.Schedule;
import types.TimeSlot;

public class SearchProcess {
	
	private SearchModel m;
	private ConstraintChecker testingChecker;

	
	public SearchProcess(ArrayList<Course> courseList,
			ArrayList<Lab> labList, 
			ArrayList<TimeSlot> cSlots, 
			ArrayList<TimeSlot> lSlots,
			ConstraintChecker checker)
	{		
		m = new SearchModel(courseList, labList, cSlots, lSlots, checker);
		testingChecker = checker;
	}
	
	
	//Essentially search control K
	public void runSearchControl()
	{

		int top15Percent;
		int numOfSorts = 3500;
		int numBeforeSort = 100;
		double startTime, endTime;
		int extension;
		int randomSchedIndex;
		Random rand = new Random(System.currentTimeMillis());
		
		startTime = System.nanoTime();
		//Start State
		m.createStartState();
		if(m.getFacts().size() == 0)
		{ System.exit(0);
			}
			
		Collections.sort(m.getFacts(), new ScheduleEvalComparator());
		endTime = System.nanoTime();
//		System.out.println((endTime - startTime)/1000000/50);
//		
//		//m.test();
//		
//		System.out.println(ConstraintChecker.count);
//		System.out.println("Top 5 Starting State Ratings: ");
//		for(int i = 0; i<5;i++)
//		{
//			System.out.println(m.getFacts().get(i).getEvalRating());
//		}
//		Main.displaySchedule(m.getFacts().get(0));
//		System.out.println();
		
		
		
		ConstraintChecker.count = 0;
		//Do Mutations and every 50 mutations we resort the list
		startTime = System.nanoTime();
		for(int i =0;i<numOfSorts;i++)
		{
			for(int j = 0;j<numBeforeSort;j++)
			{
				extension = rand.nextInt(2);
				//Gets a good solution to work on
				top15Percent = rand.nextInt((int)(m.getFacts().size()*1)); 
				
				if(extension == 0 ) //Do mutate
				{
					m.doMutate(m.getFacts().get(top15Percent));		
				}
				else //Do cross over
				{
					randomSchedIndex = rand.nextInt(m.getFacts().size());
					m.doCrossOver(m.getFacts().get(top15Percent), m.getFacts().get(randomSchedIndex));
				}
			}
			
			Collections.sort(m.getFacts(), new ScheduleEvalComparator());
			
			if(m.getFacts().size() > 15000)
				m.cull();
		}
		endTime = System.nanoTime();
			
		
		for(int i = 0; i<m.getFacts().size();i++)
		{
			m.getFacts().get(i).setEvalRating(testingChecker.eval(m.getFacts().get(i)));
		}
		
		Collections.sort(m.getFacts(), new ScheduleEvalComparator());
		
		System.out.println("Top Ten Ratings after mutations/crossovers: ");
		for(int i = 0; i<10;i++)
		{
			System.out.println(m.getFacts().get(i).getEvalRating());
		}
		
		System.out.println();
		Collections.sort(m.getFacts(), new ScheduleEvalComparator());
	
		//System.out.println("\nFail count for mutates: " + m.getFailCount());
		//System.out.println("Constraint count: " + ConstraintChecker.count);
		//Prints out our best schedule

//		
//		//m.getFacts().get(0).setEvalRating(testingChecker.eval(m.getFacts().get(0)));
//		Collections.sort(m.getFacts(), new ScheduleEvalComparator());
//		//Debugging Code
//		System.out.println("\nMin: " + testingChecker.evalMinFilled(m.getFacts().get(0)));
//		System.out.println("Pair: " + testingChecker.evalPair(m.getFacts().get(0)));
//		System.out.println("Pref: " + testingChecker.evalPref(m.getFacts().get(0)));
//		//System.out.println("PrefTest: " + testingChecker.evalPrefTest(m.getFacts().get(0)));
//		System.out.println("SecDif: " + testingChecker.evalSecDiff(m.getFacts().get(0)));
		
		
		
		//testingChecker.evalTest(m.getFacts().get(0));
		
//		System.out.println("Time to complete: " + (endTime - startTime)/1000000);
//		System.out.println("Average time per mutate: " + (endTime - startTime)/1000000/(numOfSorts*numBeforeSort));
//		System.out.println();
		//Main.displaySchedule(m.getFacts().get(0));
		
		Main.displaySchedule(m.getFacts().get(0));
		
		
	}
	
	
}
