package searchSystem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Random;

import main.Main;

import java.util.Comparator;







import types.Course;
import types.Lab;
import types.Schedule;
import types.TimeSlot;

public class SearchModel {
	
	public final int MAX_FACTS_SIZE = 5000;
	public final int START_STATE_SIZE = 50;
	
	
	Comparator<Schedule> comp;
	private ArrayList<Schedule> facts;
	private Schedule currentBestSchedule; 		//Something else we could do is have an arary of our 10 bests
	private OrTree orTree;
	private ConstraintChecker checker;
	private ArrayList<Course> courses;	 		//The courses available to the system
	private ArrayList<Lab> labs; 				//The labs available to the system	
	private ArrayList<TimeSlot> courseSlots; 	//The course time slots available to the system
	private ArrayList<TimeSlot> labSlots; 		//The lab times lots available to the system
	private double failCount;
	
	
	public SearchModel(ArrayList<Course> courseList, 
			ArrayList<Lab> labList, 
			ArrayList<TimeSlot> cSlots, 
			ArrayList<TimeSlot> lSlots,
			ConstraintChecker check)
	{	
		courses = courseList;
		labs = labList;
		courseSlots = cSlots;
		labSlots = lSlots;
		checker = check;
		
		comp = new ScheduleEvalComparator();
		facts = new ArrayList<Schedule>();
		orTree = new OrTree(courseSlots, labSlots, checker);
		
		currentBestSchedule = null;
		setFailCount(0);
			
	}
	public void doMutate(Schedule parent)
	{
		int successCount = 0;
		Schedule cleanSched = generateCleanSchedule();
		
		while(successCount < 1)
		{
			cleanSched = generateCleanSchedule();
			cleanSched = orTree.fillSchedule(cleanSched, parent, null);

			if(orTree.getKill() == false)
			{
				successCount++;
			}
			else
			{
				setFailCount(getFailCount() + 1);
			}
			orTree.setRecursiveCount(0);
			orTree.setKill(false); 
			
			//Collections.sort(facts, new ScheduleEvalComparator());
		}


		cleanSched.setEvalRating(checker.eval(cleanSched));
		facts.add(cleanSched);	
		//System.out.println(cleanSched.getEvalRating());
	}
	
	public void doCrossOver(Schedule parent1, Schedule parent2)
	{
		int successCount = 0;
		Schedule cleanSched = generateCleanSchedule();
		
		
		while(successCount < 1)
		{
			cleanSched = generateCleanSchedule();
			cleanSched = orTree.fillSchedule(cleanSched, parent1, parent2);

			if(orTree.getKill() == false)
			{
				successCount++;
			}
			else
			{
				setFailCount(getFailCount() + 1);
			}
			orTree.setRecursiveCount(0);
			orTree.setKill(false); 
			
			//Collections.sort(facts, new ScheduleEvalComparator());
		}
		
		cleanSched.setEvalRating(checker.eval(cleanSched));
		facts.add(cleanSched);	
		//System.out.println(cleanSched.getEvalRating());
	}
	
	/**
	 * When our facts pool gets too big, we must cull it down to some size by
	 * cutting off the lower portion of the pool
	 * @param facts
	 * 		A set of schedules that is too big and needs to be cut down
	 */
	public void cull()
	{
		
		for(int i = 0; i<facts.size();i++)
		{
			facts.get(i).setEvalRating(checker.eval(facts.get(i)));
		}
		
		Collections.sort(facts, new ScheduleEvalComparator());
		
//		System.out.println(facts.size());
		for(int i = 8000;i<facts.size();i++)
		{
			facts.remove(i);
		}
		
		Collections.sort(facts, new ScheduleEvalComparator());
		
		//System.out.println("Culled");
		
//		System.out.println(facts.size());
	}
	
	public ArrayList<Schedule> getFacts()
	{
		return facts;
	}
	
	/**
	 * Creates the initial schedules for our system to work with.
	 * We do not care how good these schedules are.
	 * It's very important to know that we have to create a deep
	 * copy of each course and lab when we make a new schedule or else
	 * ALL courses and ALL labs will point to the same courses and labs
	 * and all schedules will be the same...
	 */
	public void createStartState()
	{
		
		Schedule cleanSched;	
		int successCount = 0, failCount = 0;
		double start_time = System.nanoTime();
		
		
		cleanSched = generateCleanSchedule();

		while(successCount <START_STATE_SIZE)
		{		
			
			if(failCount > 10000)
			{
				System.out.println("NO sched valid");
				System.exit(0);
			}
			//Clean schedule with new objects
			cleanSched = generateCleanSchedule();

			cleanSched = orTree.fillSchedule(cleanSched,null,null);
			if(orTree.getKill() == true)
			{
				failCount++;
				//System.out.println("Failed a creation");
			}
			else
			{
				
				
				if(cleanSched != null)
				{
					successCount++;
				cleanSched.setEvalRating(checker.eval(cleanSched));
				facts.add(cleanSched);
				//Main.displaySchedule(cleanSched);
				//System.out.println("\n===================");
				//System.out.println("Creating Successful");
				}
				else
					failCount++;
				}
			
			orTree.setRecursiveCount(0);
			orTree.setKill(false);
			cleanSched = null;
			
		}
		
		//System.out.println("Fail count: " + failCount);
		

	}
	
	/**
	 * Because java is all refereces, when we want to add a schedule to facts
	 * we have to make sure that the courses and labs are completely new objects
	 * so we use this method to generate a schedule that contains course objects
	 * and new lab objects
	 * @return 
	 * 		new schedule objects with all new objects
	 */
	public Schedule generateCleanSchedule()
	{
		
		ArrayList<Course> newCourses = new ArrayList<Course>();
		ArrayList<Lab> newLabs = new ArrayList<Lab>();
		Schedule newSched;
		
		//Clones the empty courses/labs so that we aren't references the same courses and labs
		for(int i = 0; i<courses.size();i++)
		{
			newCourses.add(courses.get(i).clone());
		}
		
		for(int i = 0; i<labs.size();i++)
		{
			newLabs.add(labs.get(i).clone());
		}
		
		return newSched = new Schedule(newCourses, newLabs);
	}
	
	/**
	 * Checks that everything in the schedule has a time slot
	 * and that it passes the hard constraints.
	 * @param s
	 * 		A Complete schedule
	 * @return
	 * 		true or falsed based on if the orTree is doing what 
	 * 		it's supposed to
	 */
	private boolean basicOrTreeTest(Schedule s)
	{
		
		
		boolean isComplete = true;
		
		//Checks that all courses have an assigned time slot
		for(int j = 0;j<s.getCourses().size();j++)
		{
			if(s.getCourses().get(j).getSlot() == null){
				System.out.println("course "+j);
				isComplete = false;
				}
		}
		
		//Checks that all labs have an assigned time slot
		for(int j = 0;j<s.getLabs().size();j++)
		{
			if(s.getLabs().get(j).getSlot() == null)
			{	System.out.println("labs "+j);
				isComplete = false;}
		}
		
		//System.out.println("Creating a schedule with no parents is working: " + 
		//(checker.checkHardConstr(s) && isComplete==true));
		
		return isComplete;
	}
	public double getFailCount() {
		return failCount;
	}
	public void setFailCount(double failCount) {
		this.failCount = failCount;
	}
	
	public void test()
	{

		for(int i = 0;i<courses.size();i++)
		{
			System.out.print("\n" + courses.get(i).getDepartment());
			System.out.print(" " + courses.get(i).getNumber());
			System.out.print(" " + courses.get(i).getSection());
			System.out.print(" Index: " + courses.get(i).getIndex());
			
		}
		System.out.println("\n----------");
		for(int i = 0;i<labs.size();i++)
		{
			System.out.print("\n" + labs.get(i).getDepartment());
			System.out.print(" " + labs.get(i).getNumber());
			System.out.print(" " + labs.get(i).getLectureNum());
			System.out.print(" " + labs.get(i).getSection());
			System.out.print(" Index: " + labs.get(i).getIndex());
		}
		Schedule test = new Schedule(courses, labs);
		Schedule temp = generateCleanSchedule();
		System.out.println(temp == test);
		

		System.out.println("----------");
		System.out.println("----------");
		System.out.println("----------");
		System.out.println("----------");
		System.out.println("----------");
		for(int i = 0;i<temp.getCourses().size();i++)
		{
			System.out.print("\n" + temp.getCourses().get(i).getDepartment());
			System.out.print(" " + temp.getCourses().get(i).getNumber());
			System.out.print(" " + temp.getCourses().get(i).getSection());
			System.out.print(" Index: " + temp.getCourses().get(i).getIndex());
			
		}
		

		System.out.println("----------");
		for(int i = 0;i<temp.getLabs().size();i++)
		{
			System.out.print("\n" + temp.getLabs().get(i).getDepartment());
			System.out.print(" " + temp.getLabs().get(i).getNumber());
			System.out.print(" " + temp.getLabs().get(i).getLectureNum());
			System.out.print(" " + temp.getLabs().get(i).getSection());
			System.out.print(" Index: " + temp.getLabs().get(i).getIndex());
		}
			
	}
}
	


/**
 * Class that will tells java collections how to compare schedules
 * so that it can sort the list if need be.
 * Even though we're working with Eval ratings being of type double
 * Comparator only takes int. hence why we do it this way instead of
 * simply returning x.getEvalRating() < y.getEvalRating()
 * @author Brandon
 *
 */
class ScheduleEvalComparator implements Comparator<Schedule>
{
	 @Override
	 public int compare(Schedule x, Schedule y)
	 {
		 if(x.getEvalRating() < y.getEvalRating())
			 return -1;
		 if(x.getEvalRating() > y.getEvalRating())
			 return 1;
	     return 0;
	 }
}

