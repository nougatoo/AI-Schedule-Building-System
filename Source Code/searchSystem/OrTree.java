package searchSystem;

import java.util.ArrayList;
import java.util.Random;
import java.util.Comparator;
import java.util.PriorityQueue;

import main.Main;
import types.Course;
import types.Lab;
import types.Schedule;
import types.TimeSlot;

public class OrTree {
	
	private ArrayList<TimeSlot> courseSlots;
	private ArrayList<TimeSlot> labSlots;
	private ConstraintChecker checker;
	private PriorityQueue<Schedule> tree; 
	private Random rand = new Random();
	private int recursiveCount = 0;
	private boolean kill = false;
	
	//Will need more stuff added to the constructor later
	public OrTree(ArrayList<TimeSlot> cs, ArrayList<TimeSlot> ls, ConstraintChecker check)
	{
		courseSlots = cs;
		labSlots = ls;
		checker = check;
		
		OrTreeDepthComparator comp = new OrTreeDepthComparator();
		tree = new PriorityQueue<Schedule>(10, comp);
	}
	
	/**
	 * No parents. Is used for creating our start state
	 * @return
	 * 		Returns the first schedule that meets hard constraints 
	 */
	public Schedule fillSchedule(Schedule schedToFill)

	{	//base case of when the schedule is complete;
		if(schedToFill.getDepth() == schedToFill.size()){
					return schedToFill;
		}else{
			
			int randIndex = rand.nextInt(schedToFill.size()-1);
			int intital = randIndex -1;
			if (intital == -1)
				intital = schedToFill.getCourses().size()-1;
			if(schedToFill.isCoursesisfull()){
				randIndex = rand.nextInt(schedToFill.size()-1);
			}
			if (randIndex < schedToFill.getCourses().size() && !schedToFill.isCoursesisfull()){
				ArrayList<TimeSlot> possTimes = courseSlots;
				while(possTimes.size() > 0 ){
					//select random timeslot to try
					if(schedToFill.getCourses().get(randIndex).getSlot() == null){
						int timeIndex = rand.nextInt(possTimes.size()-1);
						schedToFill.getCourses().get(randIndex).setSlot(possTimes.get(timeIndex));
						//checks if schedule is valid
						if(checker.checkHardConstr(schedToFill)){
							//Recursion
							schedToFill.setDepth(schedToFill.getDepth()+1);
							Schedule temp = fillSchedule(schedToFill);
							System.out.println("here");
							if( temp == null){
								schedToFill.setDepth(schedToFill.getDepth()-1);
								schedToFill.getCourses().get(randIndex).setSlot(null);
								possTimes.remove(timeIndex);
							}else{
								return temp;
							}
						}else{
							//throw away timeslot and try another one
							schedToFill.getCourses().get(randIndex).setSlot(null);
							possTimes.remove(timeIndex);
						}
						
					}
					//course already has timeslot assigned then find the next null one
					randIndex = (randIndex+1)%courseSlots.size();
					if(randIndex == intital){
						schedToFill.setCoursesisfull(true);
					}
				}
				if(possTimes.size() == 0){
					return null;
				}
			}else{
				randIndex = randIndex - schedToFill.getCourses().size();
				ArrayList<TimeSlot> possTimes = labSlots;
				while(possTimes.size() > 0 ){
					//select random timeslot to try
					if(schedToFill.getLabs().get(randIndex).getSlot() == null){
						System.out.println(possTimes.size()-1);
						int timeIndex = rand.nextInt(possTimes.size());
						//timeIndex -= 1;
						schedToFill.getLabs().get(randIndex).setSlot(possTimes.get(timeIndex));
						//checks if schedule is valid
						if(checker.checkHardConstr(schedToFill)){
							//Recursion
							schedToFill.setDepth(schedToFill.getDepth()+1);
							Schedule temp = fillSchedule(schedToFill);
							if( temp == null){
								schedToFill.setDepth(schedToFill.getDepth()-1);
								schedToFill.getLabs().get(randIndex).setSlot(null);
								possTimes.remove(timeIndex);
							}else{
								return temp;
							}
						}else{
							//throw away timeslot and try another one
							schedToFill.getLabs().get(randIndex).setSlot(null);
							possTimes.remove(timeIndex);
						}
						
					}
					//course already has timeslot assigned then find the next null one
					randIndex = (randIndex+1)%labSlots.size();
					if(randIndex == intital){
						schedToFill.setLabsisfull(true);
					}
				}
				if(possTimes.size() == 0){
					return null;
				}
			}
		
		}
		System.out.print("You did something wrong");
		return null;
	}
	
	/**
	 * One parent. This method is use to do a mutate
	 * @return
	 */
	public Schedule fillSchedule(Schedule schedToFill, Schedule parent)
	{
		return null;
	}
	
	/**
	 * Two parents. This method is used to do a crossOver, if if only parent1 is a non-null schedule it will be a mutation
	 * and if both parents are null then it will create a new schedule
	 * @return
	 */
	public Schedule fillSchedule(Schedule schedToFill, Schedule parent1, Schedule parent2)
	{
		Random rand = new Random(System.currentTimeMillis());
		recursiveCount++;
		
		if(recursiveCount >200)
			kill = true;

		
		//System.out.println(schedToFill.getDepth()+"/"+courseSlots.size()+labSlots.size());
		
		if(kill == false)
		{
			if(schedToFill.getDepth() == schedToFill.size()){
				return schedToFill;
			}
			if(parent1 == null){
				// this means its creating a schedule
				parent1 = schedToFill.clone();
				parent2 = schedToFill.clone();
				// fills the courses while ignoring preassigned stuff;
				for(int i = 0; i< schedToFill.getCourses().size();i++){
					if(schedToFill.getCourses().get(i).getSlot() == null){
						parent1.getCourses().get(i).setSlot(courseSlots.get(rand.nextInt(courseSlots.size())));
						parent2.getCourses().get(i).setSlot(courseSlots.get(rand.nextInt(courseSlots.size())));
					}
				}// fills the labs like above;
				for(int i = 0; i< schedToFill.getLabs().size();i++){
					if(schedToFill.getLabs().get(i).getSlot() == null){
						parent1.getLabs().get(i).setSlot(labSlots.get(rand.nextInt(labSlots.size())));
						parent2.getLabs().get(i).setSlot(labSlots.get(rand.nextInt(labSlots.size())));
					}
				}
			}else if(parent2 == null){
				//clones parent one then randomly changes some slots
				parent2 = parent1.clone();
				// random amount of intrations
				int numOfMut;
				if(schedToFill.getCourses().size() <= 1)
				{
					 numOfMut = 0;
				}
				else
				{
					numOfMut = rand.nextInt(schedToFill.getCourses().size()-1);	
				} 
				for(int i = 0; i< numOfMut;i++){
					int index = rand.nextInt(schedToFill.getCourses().size());
					if(schedToFill.getCourses().get(i).getSlot() == null){
						parent2.getCourses().get(i).setSlot(courseSlots.get(rand.nextInt(courseSlots.size())));
					}
				}
				if(schedToFill.getLabs().size() <= 1   )
				{
				 numOfMut = 0;
				}
				else
				{
				 numOfMut = rand.nextInt(schedToFill.getLabs().size()-1);
				}
				for(int i = 0; i< numOfMut;i++){
					int index = rand.nextInt(schedToFill.getLabs().size());
					if(schedToFill.getLabs().get(i).getSlot() == null){
						parent2.getLabs().get(i).setSlot(labSlots.get(rand.nextInt(labSlots.size())));
					}
				}
			}
			
			//fills a tree from left to right
			if (schedToFill.getDepth() < schedToFill.getCourses().size()){
				ArrayList<TimeSlot> possTimes = new ArrayList<TimeSlot>();
				for(int i = 0;i <courseSlots.size();i++){
					possTimes.add(courseSlots.get(i));
				}
				boolean parentTry = true;
				while(possTimes.size() > 0 ){

					//select random timeslot to try
					
					if(schedToFill.getCourses().get(schedToFill.getDepth()).getSlot() == null){
						int timeIndex = rand.nextInt(possTimes.size());
						//System.out.println(timeIndex);
						TimeSlot tempSlot, tempSlot1, tempSlot2;
						if(parentTry){
											
							
							if(rand.nextBoolean()){
								
								tempSlot= parent1.getCourses().get(schedToFill.getDepth()).getSlot();
							}else{
								tempSlot =parent2.getCourses().get(schedToFill.getDepth()).getSlot();
							}
							
							
						}else{
							tempSlot = possTimes.get(timeIndex);
						}
						schedToFill.getCourses().get(schedToFill.getDepth()).setSlot(tempSlot);
						//checks if schedule is valid
						if(checker.checkHardConstr(schedToFill)){
							//Recursion
							schedToFill.setDepth(schedToFill.getDepth()+1);
							Schedule temp = fillSchedule(schedToFill,parent1,parent2);
							if( temp == null && kill == false){
								schedToFill.setDepth(schedToFill.getDepth()-1);
								schedToFill.getCourses().get(schedToFill.getDepth()).setSlot(null);
								possTimes.remove(tempSlot);
							}else{
								return temp;
							}
						}else{
							//throw away timeslot and try another one
							schedToFill.getCourses().get(schedToFill.getDepth()).setSlot(null);
							if(parentTry){
								parentTry = false;
								possTimes.remove(tempSlot);
							}else{
								possTimes.remove(timeIndex);
							}
						}
						
					}else{
						//course already has timeslot assigned then find the next null one
						schedToFill.setDepth(schedToFill.getDepth()+1);
						if(schedToFill.getDepth() == schedToFill.getCourses().size()){
							break;
						}
							
					}
					
				}
				if(possTimes.size() == 0){
					return null;
				}
			}
			
			int Index = schedToFill.getDepth() - schedToFill.getCourses().size();
			ArrayList<TimeSlot> possTimes = new ArrayList<TimeSlot>();
			for(int i = 0;i <labSlots.size();i++){
				possTimes.add(labSlots.get(i));
			}
			boolean parentTry = true;
			while(possTimes.size() > 0 ){
				//select random timeslot to try
				//System.out.println(Index+"."+schedToFill.getLabs().size());
				//got to the end of labsshort
				if(schedToFill.getLabs().get(Index).getSlot() == null){
					int timeIndex = rand.nextInt(possTimes.size());
					TimeSlot tempSlot, tempSlot1, tempSlot2;
					if(parentTry){
						
						/*
						tempSlot1 = parent1.getLabs().get(Index).getSlot();
						tempSlot2 = parent2.getLabs().get(Index).getSlot();
						
						if(tempSlot1 == tempSlot2)
						{
							tempSlot=tempSlot1;
						}
						else
						{
							tempSlot = possTimes.get(timeIndex);
						}
						*/
						
						
						if(rand.nextBoolean()){
							tempSlot = parent1.getLabs().get(Index).getSlot();
						}else{
							tempSlot = parent2.getLabs().get(Index).getSlot();
						}
						
					}else{
						tempSlot = possTimes.get(timeIndex);
					}
					schedToFill.getLabs().get(Index).setSlot(tempSlot);
					//checks if schedule is valid
					if(checker.checkHardConstr(schedToFill)){
						//Recursion
						schedToFill.setDepth(schedToFill.getDepth()+1);
						Schedule temp = fillSchedule(schedToFill,parent1,parent2);
						if( temp == null){
							schedToFill.setDepth(schedToFill.getDepth()-1);
							schedToFill.getLabs().get(Index).setSlot(null);
							possTimes.remove(timeIndex);
						}else{
							return temp;
						}
					}else{
						//throw away timeslot and try another one
						schedToFill.getLabs().get(Index).setSlot(null);
						
						if(parentTry){
							parentTry = false;
							possTimes.remove(tempSlot);
						}else{
							possTimes.remove(timeIndex);
						}
					}			
				}else{
					//course already has timeslot assigned then find the next null one
					schedToFill.setDepth(schedToFill.getDepth()+1);
					if(schedToFill.getDepth() == schedToFill.size()){
						return schedToFill;
					}			
				}
			}
			if(possTimes.size() == 0){
				return null;
			}
					
		}
	

	//System.out.print("You did something wrong");
	return null;
	}

	public int getRecursiveCount() {
		return recursiveCount;
	}

	public void setRecursiveCount(int recursiveCount) {
		this.recursiveCount = recursiveCount;
	}
	
	public boolean getKill()
	{
		return kill;
	}
	
	public void setKill(boolean kill)
	{
		this.kill = kill;
	}


}

/**
 * Tells the priority queue how to compare elements. The schedule
 * with the lowest depth
 * @author Brandon
 *
 */
class OrTreeDepthComparator implements Comparator<Schedule>
{
	 @Override
	 public int compare(Schedule x, Schedule y)
	 {
	     return y.getDepth() - x.getDepth();
	 }
}
