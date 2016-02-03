/***********
 * The general scheme is: if a test for hard constraint fails, it will return false
 * if it passes it will return true
 * Some of the method names make this confusing, i will change them after
 * 
 */
package searchSystem;

import java.util.ArrayList;

import javax.naming.PartialResultException;

import javafx.util.Pair;
import types.Course;
import types.Lab;
import types.Schedule;
import types.ScheduleDay;
import types.TimeSlot;
import types.Triple;

public class ConstraintChecker {

	private ArrayList<TimeSlot> courseSlots;
	private ArrayList<TimeSlot> labSlots;
	private ArrayList<Pair<Course, Course>> notCompatible;
	private ArrayList<Pair<Course, TimeSlot>> unwanted;
	private ArrayList<Pair<Course, TimeSlot>> partials;
	private ArrayList<Triple<TimeSlot, Course, Integer>> preferences;	
	

	private ArrayList<Pair<Course, Course>> pairs;
	public static double count = 0;
	
	private double minFilledWeight = 1.00;
	private double prefWeight      = 1.00;
	private double pairWeight      = 1.00;
	private double secDiffWeight   = 1.00;
	private double 		   _penCoursemin = 1.00;
	private double 		   _penLabsmin   = 1.00;
	private double         _penNotPaired = 1.00;
	private double         _penSection   = 1.00;
	
	

	//Will need more stuff added to the constructor later
	public ConstraintChecker(
			ArrayList<TimeSlot> cSlots, 
			ArrayList<TimeSlot> lSlots, 
			ArrayList<Pair<Course, Course>> notCompat,
			ArrayList<Pair<Course, TimeSlot>> unWant,
			ArrayList<Pair<Course, TimeSlot>> parts,
			ArrayList<Triple<TimeSlot, Course, Integer>> pref,
			ArrayList<Pair<Course, Course>> pair)
	
	{
		courseSlots = cSlots;
		labSlots = lSlots;
		notCompatible = notCompat;
		unwanted = unWant;
		partials = parts;
		preferences = pref;
		pairs = pair;
	}
	
	/**
	 * Looks at a schedule and determines if any of the hard constraints are violated.
	 * This include the hard constraints for the CPSC department 
	 * @param s
	 * @return true if no hard constraints are violated, false if there is 1 or more violated
	 */
	public boolean checkHardConstr(Schedule sched)
	{	
		count++;
		//We don't want to continue if one of the on the hard constraints is violated
		//so if one fails we return false right after. No point in checking more
		if(!checkNotCompatible(sched))
			return false;
		else if(!checkCpscConstr(sched))
			return false;
		else if(!checkCourseLabConflict(sched))
			return false;
		else if(!checkCmaxLmax(sched,0))
			return false;
		else if(!checkCmaxLmax(sched, 1))
			return false;
		else if(!checkUnwanted(sched))
			return false;
		else if (!checkPartials(sched))
			return false;
			
		return true;
	}
	
	//I have ideas on how to make this faster later on if things are slow, 
	// just wanted to get something down in code first
	/**
	 * Checks that each course times slot doesn't have more than it's allowed courses
	 * @param s is a full schedule
	 * @param type is if we want to check course min or max min
	 * 	- 0: check courses
	 *  - 1: check labs
	 *  @return false if there is a conflict, true if there is no conflict
	 */
	private boolean checkCmaxLmax(Schedule sched, int type)
	{
		TimeSlot workingSlot = null;
		Course classOrLab = null;
		int counter = 0;
		int numOfTimeSlots = -1;
		int size = -1;
		
		//If we are working with a course or a lab
		if(type == 0)
		{
			numOfTimeSlots = courseSlots.size();
			size = sched.getCourses().size();
		}
		else if (type == 1)
		{
			numOfTimeSlots = labSlots.size();
			size = sched.getLabs().size();
		}
		
		//Traverse through all the available course/lab slots to the system
		for(int i = 0;i<numOfTimeSlots;i++)
		{
			counter = 0;
	
			//If we're working with labs or courses
			if(type == 0)
				workingSlot = courseSlots.get(i);
			else if(type == 1)
				workingSlot = labSlots.get(i);
					
			//Traverse through all the courses in the given schedule and check their time slot
			for(int j = 0;j<size;j++)
			{
				//If we're working with course or lab
				if(type == 0)
					classOrLab = sched.getCourses().get(j);
				else if(type == 1)
					classOrLab = sched.getLabs().get(j);
				
				if(classOrLab.getSlot() != null)
				{	
					//If they have the same day and start time, the are the same time slot
					if(workingSlot.getStartTime() == classOrLab.getSlot().getStartTime() &&
							workingSlot.getDay() == classOrLab.getSlot().getDay() &&
							workingSlot.getIsLab() == classOrLab.getSlot().getIsLab())
						counter++;
				}
			}	
			//For Testing
			//System.out.print("\nMax is: " + workingSlot.getMaxNum());
			//System.out.print("\nCounter is at: " + counter);
			
			//If we found more courses that course max that have this time slot, return false
			if(counter > workingSlot.getMaxNum())
				return false;
		}
		//If there have been no conflicts so far
		return true;
	}

	/**
	 * Checks that for every course, if it has labs, then they are not at the same time 
	 * @param s is a complete or partial schedule
	 * @return false if there IS a conflict, true if there is NO conflict
	 */
	private boolean checkCourseLabConflict(Schedule sched)
	{
		Schedule workingSchedule = sched;
		Course workingCourse;
		Lab workingLab, lab813 = null, lab913 = null;
			
		//Traverse through all the classes in the schedule and find their labs
		for(int i = 0; i<workingSchedule.getCourses().size();i++)
		{
			workingCourse = workingSchedule.getCourses().get(i);
			
			//Traverse through all the labs
			for(int j = 0; j<workingSchedule.getLabs().size();j++)
			{
				workingLab = workingSchedule.getLabs().get(j);
				
				if(workingLab.getNumber() == 813)
				{
					lab813 = workingLab;
				}
				else if(workingLab.getNumber() == 913)
				{
					lab913 = workingLab;
				}
				//If the lab is in fact a lab for this course and their time slots overlap
				//then we return false.
				if(isLabOf(workingCourse, workingLab) && !timeSlotConflict(workingCourse, workingLab))
					return false;
				else
				{
					//I need to making workingSchedule a deep copy of s, right now it's just a pointer to s
					//so if i remove something, we're deleting parts of the schedule
					//Have to override clone, just don't feel like doing it right now.
					//workingSchedule.getLabs().remove(j);
				}
			}
		}
		
		//Does special cpsc 813 and 913 checking
		for(int i = 0; i<workingSchedule.getLabs().size();i++)
		{
			workingLab = workingSchedule.getLabs().get(i);
			
			//A 313 lab
			if(workingLab.getDepartment().equals("CPSC") &&
					workingLab.getNumber() == 313 &&
					workingLab.getSlot() != null &&
					lab813.getSlot() != null)
			{
				if(workingLab.getSlot().getDay() == lab813.getSlot().getDay() &&
						workingLab.getSlot().getStartTime() == lab813.getSlot().getStartTime())
				{
					return false;
				}
			}
			//A 413 lab
			else if(workingLab.getDepartment().equals("CPSC") &&
					workingLab.getNumber() == 413 && 
					workingLab.getSlot() != null &&
					lab913.getSlot() != null)
			{
				if(workingLab.getSlot().getDay() == lab913.getSlot().getDay() &&
						workingLab.getSlot().getStartTime() == lab913.getSlot().getStartTime())
				{
					return false;
				}
			}
		}
		
		//We've now checked all the courses and labs and found no conflict, return false
		return true;
	}
	
	/**
	 * Checks that none of the not compatible pairs are violated
	 * TODO: This method is still being tested. it's fucking SLOW. One idea is to put everythying that has a 
	 * 		notcompatible entry at the start of the course/lab list
	 * @param sched partial or fulls schedule
	 * @return false if there is a violation, true if the check passes
	 */
	public boolean checkNotCompatible(Schedule sched)
	{ 
		int index1, index2;
		TimeSlot slot1, slot2;
		
		for(int i = 0;i<notCompatible.size();i++)
		{
			index1 = notCompatible.get(i).getKey().getIndex();
			index2 = notCompatible.get(i).getValue().getIndex();
			
			if(notCompatible.get(i).getKey().getClass() == Course.class) //Course-x
			{
				if(notCompatible.get(i).getValue().getClass() == Course.class) //Course-Course
				{
					slot1 = sched.getCourses().get(index1).getSlot();
					slot2 = sched.getCourses().get(index2).getSlot();
					
					if(slot1 == slot2 && slot1 != null && slot2 != null)
						return false;
					
				}
				else //Course-Lab
				{
					slot1 = sched.getCourses().get(index1).getSlot();
					slot2 = sched.getLabs().get(index2).getSlot();
					
					if(slot1 == slot2  && slot1 != null && slot2 != null)
						return false;
				}
			}
			else //Lab-x
			{
				if(notCompatible.get(i).getValue().getClass() == Course.class) //Lab-Course
				{
					slot1 = sched.getLabs().get(index1).getSlot();
					slot2 = sched.getCourses().get(index2).getSlot();
					
					if(slot1 == slot2 && slot1 != null && slot2 != null)
						return false;
					
				}
				else //Lab-Lab
				{
					slot1 = sched.getLabs().get(index1).getSlot();
					slot2 = sched.getLabs().get(index2).getSlot();
					
					if(slot1 == slot2 && slot1 != null && slot2 != null)
						return false;
				}
			}	
		}
		
		return true;
		
		/*
		Pair<Course, Course> workingPair;
		TimeSlot ts1, ts2;
		Course notCompat_x, notCompat_y;
		Course workingCourse1, workingCourse2;
		Lab workingLab1, workingLab2;
		
		//Traverse through the list of not compatibles
		for(int i = 0;i<notCompatible.size();i++)
		{
			notCompat_x = notCompatible.get(i).getKey();
			notCompat_y = notCompatible.get(i).getValue();
			
			if(notCompat_x.getClass() == Course.class)
			{
				//Find the notCompat _x in the course list
R:				for(int j = 0;j<sched.getCourses().size();j++)
				{
					workingCourse1 = sched.getCourses().get(j);
					
					//If we found it
					if(workingCourse1.equals(notCompat_x))
					{
						//Need to find the second one.
						if(notCompat_y.getClass() == Course.class) //If we're dealing with course-course
						{
							//Looking for the second one 
E:							for(int k = 0; k<sched.getCourses().size();k++)
							{
								workingCourse2 = sched.getCourses().get(k);
								
								//If we found the 2nd course in the course list
								if(workingCourse2.equals(notCompat_y))
								{
									ts1 = workingCourse1.getSlot();
									ts2 = workingCourse2.getSlot();
									//Now we have them both
									if(ts1 != null && ts2 != null)
									{
										if(ts1.getDay().equals(ts2.getDay()) &&
												ts1.getStartTime() == ts2.getStartTime())
										{
											return false;
										}
										
									}
									break E;
								}
							}
						}
						else //Must be a lab
						{
							//Dealing with course-lab
							//Looking for the second one 
E:							for(int k = 0; k<sched.getLabs().size();k++)
							{
								workingLab2 = sched.getLabs().get(k);
								
								//If we found the 2nd course in the course list
								if(workingLab2.equals(notCompat_y))
								{
									ts1 = workingCourse1.getSlot();
									ts2 = workingLab2.getSlot();
									//Now we have them both
									if(ts1 != null && ts2 != null)
									{
										if(ts1.getDay().equals(ts2.getDay()) &&
												ts1.getStartTime() == ts2.getStartTime())
										{
											return false;
										}	
										
										
										else if(timeSlotConflict(workingCourse1, workingLab2) ==false)
										{
											return false;
										}
										
									}
									break E;
								}
							}
						}
						break R;
					}

				}
			}
			else //Dealing with a lab first
			{
				
				//Find the notCompat _x in the course list
R:				for(int j = 0;j<sched.getLabs().size();j++)
				{
					workingLab1 = sched.getLabs().get(j);
					
					//If we found it
					if(workingLab1.equals(notCompat_x))
					{
						//Need to find the second one.
						if(notCompat_y.getClass() == Course.class) //If we're dealing with lab-course
						{
							//Looking for the second one 
							for(int k = 0; k<sched.getCourses().size();k++)
							{
								workingCourse2 = sched.getCourses().get(k);
								
								//If we found the 2nd course in the course list
E:								if(workingCourse2.equals(notCompat_y))
								{
									ts1 = workingLab1.getSlot();
									ts2 = workingCourse2.getSlot();
									//Now we have them both
									
									if(ts1 != null && ts2 != null)
									{
										if(ts1.getDay().equals(ts2.getDay()) &&
												ts1.getStartTime() == ts2.getStartTime())
										{
											return false;
										}
										
										if(timeSlotConflict(workingCourse2, workingLab1) == false)
										{
											return false;
										}
										
									}
									
									break E;
								}
							}
						}
						else //Must be a lab
						{
							//Dealing with lab-lab
							//Looking for the second one 
							for(int k = 0; k<sched.getLabs().size();k++)
							{
								workingLab2 = sched.getLabs().get(k);
								
								//If we found the 2nd course in the course list
E:								if(workingLab2.equals(notCompat_y))
								{
									ts1 = workingLab1.getSlot();
									ts2 = workingLab2.getSlot();
									//Now we have them both
									
									if(ts1 != null && ts2 != null)
									{
										if(ts1.getDay().equals(ts2.getDay()) &&
												ts1.getStartTime() == ts2.getStartTime())
										{
											return false;
										}
									}
									break E;
								}
							}
						}
						break R;
					}
				}
				
			}
			
		

		}
		return true;
		
			*/
	}
	
	/**
	 * Checks no class has an unwated timeslot assigned to it
	 * @param sched full or partial schedule
	 * @return false if there is a conflict, true if check passes
	 */
	private boolean checkUnwanted(Schedule sched)
	{
		Pair<Course, TimeSlot> workingPair;
		Course workingCourse;
		TimeSlot workingSlot;
		Lab workingLab;
		boolean done = false;
		
		//Courses 
		for(int i = 0;i<unwanted.size();i++)
		{	
			//If the unwanted we are checking is actually a course
			if(unwanted.get(i).getKey().getClass() == Course.class)
			{
T:				for(int j = 0; j<sched.getCourses().size();j++)
				{
					workingCourse = sched.getCourses().get(j);
					//If we found the course 
					if(workingCourse.equals(unwanted.get(i).getKey()) && workingCourse.getSlot() != null)
					{
						//And it has the same time slot as unwanted return false cause the check failsed
						workingSlot = workingCourse.getSlot();
						if(workingSlot.getDay() == unwanted.get(i).getValue().getDay() &&
								workingSlot.getStartTime() == unwanted.get(i).getValue().getStartTime() &&
								workingSlot.getIsLab() == unwanted.get(i).getValue().getIsLab())
						{
							return false;
						}
						else
						{
							//We found the course, but it didn't have the unwanted time slot so we 
							//break the loop that was trying to find the course
							break T;
						}
					}
				}
			}
			//If the unwanted we are checking is actually a Lab
			if(unwanted.get(i).getKey().getClass() == Lab.class)
			{
				Lab unwantedLab = (Lab) unwanted.get(i).getKey();
				
F:				for(int j = 0; j<sched.getLabs().size();j++)
				{
					workingLab = sched.getLabs().get(j);
					//If we found the Lab and the slot is not not null
					if(workingLab.getDepartment().equals(unwantedLab.getDepartment()) && 
							workingLab.getLectureNum() == unwantedLab.getLectureNum() &&
							workingLab.getNumber() == unwantedLab.getNumber() &&
							workingLab.getSection() == unwantedLab.getSection() &&
							workingLab.getSlot() != null)
					{
						//And it has the same time slot as unwanted return false cause the check failsed
						workingSlot = workingLab.getSlot();
						if(workingSlot.getDay() == unwanted.get(i).getValue().getDay() &&
								workingSlot.getStartTime() == unwanted.get(i).getValue().getStartTime() &&
								workingSlot.getIsLab() == unwanted.get(i).getValue().getIsLab())
						{
							return false;
						}
						else
						{
							//We found the course, but it didn't have the unwanted time slot so we 
							//break the loop that was trying to find the course
							break F;
						}
					}
				}
			}
		}
		
	
		return true;
	}
	
	
	/**
	 * Checks that all the partials assignment are fulfilled in the given schedule
	 * @param sched partial or full schedule
	 * @return false if there is a partial assignment that is not met, true if there is no conflicts
	 */
	private boolean checkPartials(Schedule sched)
	{
		ArrayList<Course> coursesToCheck = new ArrayList<Course>();
		Course workingPartialCourse, workingCourse;
		Lab workingPartialLab, workingLab;
		TimeSlot workingPartialSlot, workingSlot;
		
		for(int i = 0;i<partials.size();i++)
		{
			workingPartialCourse = partials.get(i).getKey();
			workingPartialSlot = partials.get(i).getValue();
			
			
			//If it's a lab...do some stuff
			if(workingPartialCourse.getClass() == Lab.class)
			{
				workingPartialLab = (Lab) workingPartialCourse;
				
				//System.out.println("found a lab");
				for(int j = 0; j<sched.getLabs().size();j++)
				{
					workingLab = sched.getLabs().get(j);
					workingSlot = workingLab.getSlot();
					
					if(workingSlot != null)
					{
						//If they are the same lab
						if(workingLab.getDepartment().equals(workingPartialLab.getDepartment()) &&
								workingLab.getLectureNum() == workingPartialLab.getLectureNum() &&
								workingLab.getNumber() == workingPartialLab.getNumber() &&
								workingLab.getSection() == workingPartialLab.getSection())
						{
							if(workingSlot.getIsLab() != workingPartialSlot.getIsLab() ||
									workingSlot.getDay() != workingPartialSlot.getDay() ||
									workingSlot.getStartTime() != workingPartialSlot.getStartTime())
							{
								return false;
							}
						}
					}
				}
			}
			else //It's a course..
			{
				for(int j = 0; j<sched.getCourses().size();j++)
				{
					workingCourse = sched.getCourses().get(j);
					workingSlot = workingCourse.getSlot();
					
					if(workingSlot != null)
					{
						//If they are the same course
						if(workingCourse.getDepartment().equals(workingPartialCourse.getDepartment()) &&
								workingCourse.getNumber() == workingPartialCourse.getNumber() &&
								workingCourse.getSection() == workingPartialCourse.getSection())
						{
							if(workingSlot.getIsLab() != workingPartialSlot.getIsLab() ||
									workingSlot.getDay() != workingPartialSlot.getDay() ||
									workingSlot.getStartTime() != workingPartialSlot.getStartTime())
							{
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Check to make sure that all the cpsc constraints outline in assignment 
	 * description are fulfilled. Only checks the CPSC courses
	 * TODO: when system is up and running, do testing to see which one is more frequent and put it first
	 * @param sched full or partial schedule to check
	 * @return returns false if they are not all fulfilled, true if they are all fulfilled
	 */
	private boolean checkCpscConstr(Schedule sched)
	{
		if(!checkEvenings(sched))
			return false;
		else if(!checkFifthYear(sched))
			return false;
		else if(!checkTuesdayEleven(sched))
			return false;
		else if(!check813_913(sched))
			return false;
		return true;
	}
	
	/**
	 * Checks that any CPSC class with a lecture section of 9 is schedule at or after 1800
	 * @param sched full or partial schedule
	 */
	private boolean checkEvenings(Schedule sched)
	{
		int tempSectionNumber; //In his example he has a course with lecture section 95
		//Traverse through all the courses and look at their time slot start time
		for(int i = 0; i<sched.getCourses().size(); i++)
		{
			if(sched.getCourses().get(i).getSlot() != null)
			{
				tempSectionNumber = sched.getCourses().get(i).getSection();
				
			    while (tempSectionNumber > 9) 
			    {
			        tempSectionNumber /= 10;
			    }
			       
				//If a CPSC course has section number that starts in 9 and 
			    //starts before 1800 the check failed
				if((tempSectionNumber == 9) &&
						(sched.getCourses().get(i).getSlot().getStartTime() <1800) )
					return false;
			}
		}
		
		//I commented this out because i don't think we need to for labs
		/*
		//Traverse through all the labs and look at their time slot start time
		for(int i = 0; i<sched.getLabs().size(); i++)
		{
			//If a CPSC course has section number 9 and starts before 1800 the check failed
			if((sched.getLabs().get(i).getLectureNum() == 9) &&
					(sched.getLabs().get(i).getSlot().getStartTime() <1800) &&
					(sched.getCourses().get(i).getDepartment().equals("CSPC")))
				return false;	
		}
		*/
		
		//If we made it here, there were no conflicts. Check passes
		return true;
	}
	
	/**
	 * Checks that none of the CSPC500 level courses do not have the same time slot
	 * TODO: Test more
	 * @param sched
	 * @return
	 */
	private boolean checkFifthYear(Schedule sched)
	{
		ArrayList<Course> fifthYearCourses = new ArrayList<Course>(); //Only 500CPSC courses
		//A list we build with time slots that 5th year courses were assigned 
		ArrayList<TimeSlot> fifthYearSlots = new ArrayList<TimeSlot>();  
		Course tempCourse;
		int tempCourseNumber;
		TimeSlot tempSlot;
		
		//Traverse through all the courses
		for(int i = 0; i<sched.getCourses().size();i++)
		{
			tempCourse = sched.getCourses().get(i);
			tempCourseNumber = tempCourse.getNumber();
			
			//If its course number is between 500 and 600 and it's in the cpsc department
			// append it to the list of fifth year courses
			if(
					(tempCourseNumber >= 500) && 
					(tempCourseNumber < 600))
			{
				fifthYearCourses.add(tempCourse);
			}
		}
		
		//At this point, fifthYearCourses contains all the 500 level CPSC class
		//THis loop checks if the time slot for the current 500 level course has 
		//already been added to our list. If it is, we know they are not all at different times.
		//
		for(int i = 0; i<fifthYearCourses.size();i++)
		{
			tempSlot = fifthYearCourses.get(i).getSlot();
			
			if(tempSlot != null)
			{
			
				if(fifthYearSlots.contains(tempSlot))
					return false;
				else
					fifthYearSlots.add(tempSlot);
			}
		}
		
		return true;
	}
	
	/**
	 * Checks that there is no lecture is assigned to tuesday at 11am
	 * TODO: Add this to another check method, takes up too much time for something so easily added
	 * to another check that goes through courses 
	 * @param sched
	 * @return
	 */
	private boolean checkTuesdayEleven(Schedule sched)
	{
		//Looks at all the course's timeslots and checks time start time and day
		for(int i = 0; i<sched.getCourses().size();i++)
		{
			if(sched.getCourses().get(i).getSlot() != null)
			{
				if((sched.getCourses().get(i).getSlot().getDay() == ScheduleDay.TU) && 
						(sched.getCourses().get(i).getSlot().getStartTime() == 1100))
					return false;
			}
		}
		
		//If we made it here, we found no conflict. Check passes 
		return true;
	}
	
	/**
	 * Checks that if we have CPSC 313 & 413 in the course section, then we have
	 * scheduled CPSC 813 and 913 into the proper section. CPSC 813 and 913 cannot overlap
	 * with anything that cannot overlap with cpsc 313 and 413. This means that if we have 
	 * them in a not-compatible pair, then the not-compatible class with 313/413 is also 
	 * uncompatible with cpsc 813/913.
	 * TODO: test the shit out of this method
	 * @param sched partial or complete schedule
	 * @return false if there is a conflict, true if there is no conflict
	 */
	private boolean check813_913(Schedule sched)
	{
		boolean has313 = false;
		boolean has413 = false;
		Course workingCourse;
		Lab workingLab;
		
		//Traverses the list and finds out if we have CPSC313 or CPSC413
		//COuld probably be added to something else and have has313/has412 a field.
		//of a schedule
C:		for(int i = 0;i<sched.getCourses().size();i++)
		{
			workingCourse = sched.getCourses().get(i);
			
			//If CPSC 313 exists in our courses list
			if((workingCourse.getDepartment().equals("CPSC")) && 
					(workingCourse.getNumber() == 313))
			{
				has313 = true;
			}
			
			//If CPSC 413 exists in our courses list
			if((workingCourse.getDepartment().equals("CPSC")) && 
					(workingCourse.getNumber() == 413))
			{
				has413 = true;
			}
			
			if(has313 && has413)
				break C;
		}//End of for-loop
		
		//At this point we know if we have cpsc313 or cpsc413 in the schedule
		if(has313)
		{
			//Checking that all the requirements for cpsc813 are met
			
			//Go through labs to find cpsc813
			for(int i = 0; i<sched.getLabs().size();i++)
			{
				workingLab = sched.getLabs().get(i);
				
				if((workingLab.getDepartment().equals("CPSC")) &&
						(workingLab.getNumber() == 813) &&
						(workingLab.getSlot() != null))
				{
					
					//If it's not schedule on tuesday at 1800 and it's not null 
					if((!workingLab.getSlot().getDay().equals(ScheduleDay.TU) || 
							workingLab.getSlot().getStartTime() != 1800) &&
							workingLab.getSlot() != null)
						return false;						
				}
			}
		}
		
		if(has413)
		{
			//Checking that all the requirements for cpsc913 are met
			
			//Go through labs to find cpsc913
			for(int i = 0; i<sched.getLabs().size();i++)
			{
				workingLab = sched.getLabs().get(i);
				
				if((workingLab.getDepartment().equals("CPSC")) &&
						(workingLab.getNumber() == 913) &&
						(workingLab.getSlot() != null))
				{
					
					//If it's not schedule on tuesday at 1800 and it's not null 
					if((!workingLab.getSlot().getDay().equals(ScheduleDay.TU) || 
							workingLab.getSlot().getStartTime() != 1800) &&
							workingLab.getSlot() != null)
						return false;
				}
			}
			
		}
		
		return true;
	}
	
	/**
	 * Takes a course and lab and finds out if they are in a time conflict. AKA
	 * they some any overlap. 
	 * @param c 
	 * 		course that has a time slot to compared to lab
	 * @param l 
	 * 		lab that has a time slot that gets compared to course
	 * @return 
	 * 		false if there is conflict, true if there is NO conflict
	 */
	private boolean timeSlotConflict(Course course, Lab lab)
	{
		boolean passed = true;
		TimeSlot cSlot = course.getSlot(), lSlot = lab.getSlot();
		
		//Obviously there is no conflict if they course + lab didn't have a time slot assigned
		if(cSlot != null && lSlot != null)
		{
			//If a course and lab are on the same day, or course is monday and lab is friday
			if(cSlot.getDay() == lSlot.getDay() || (cSlot.getDay()==ScheduleDay.MO && lSlot.getDay()==ScheduleDay.FR))
			{
				//If it's a Tuesday
				if(cSlot.getDay() == ScheduleDay.TU)
				{
					//If they have the same start they are a conflict
					//Lectures are 1.5 hours and labs are 1 hour. 
					//If a lecture starts 30 min after a lab, it's a conflict
					//If a lecture starts 30 min before (-70 becuase thats how we coded it for ints)
					// then it is a conflict
					//NOTE: I need to do more testing, but everything i tired is working so far
					if(cSlot.getStartTime() == lSlot.getStartTime())
						passed = false;
					else if(cSlot.getStartTime() == (lSlot.getStartTime()+30))
						passed = false;
					else if(cSlot.getStartTime() == (lSlot.getStartTime()-70))
						passed = false; 
					else if (cSlot.getStartTime() == (lSlot.getStartTime() -100))
						passed = false;
					//THIS IS WHERE WE NEED FIXING 
				}
				else if(cSlot.getDay() == ScheduleDay.MO && lSlot.getDay() == ScheduleDay.FR)
				{
					if(cSlot.getStartTime() == lSlot.getStartTime())
						passed = false;
					else if(cSlot.getStartTime() == (lSlot.getStartTime()+100))
						passed = false;
					//else if(sC.getStartTime() == (sL.getStartTime()-100))
						//conflict = true; 
				}
				else 
				{
					//If they are equaal, there is a time conflict
					passed = !(cSlot.getStartTime() == lSlot.getStartTime());
				}
			}
			else
				passed = true; //If they don't have the same day, they are obviously not in conflict
		}
		return passed;
	}
	
	
	/**
	 * 
	 * @param c 
	 * 		course that will be compared to a lab
	 * @param l 
	 * 		lab that we want to know if it belongs to course c
	 * @return true if the lab is a lab for that course
	 */
	private boolean isLabOf(Course c, Lab l )
	{
		//If the lab and course have the same department, course num and (section num (or -1))
		//then the lab must belong to that course
		return (l.getNumber() == c.getNumber()) && 
				(l.getDepartment().equals(c.getDepartment()) &&
				(l.getLectureNum() == c.getSection() || l.getLectureNum() == -1));
					
	}

	/**
	 * Evaluates how good a schedule is by looking at how many soft contraints that it breaks
	 * @param sched
	 * @return 
	 * 		an integer that represents how optimized the schedule is. The higher the number
	 * 		the worse the optimization. 0 is a perfect score and means the schedule cannot get any better
	 */
	public double eval(Schedule sched)
	{
		double totalEval = -1;
	
		totalEval = (evalMinFilled(sched)*minFilledWeight) + 
				(evalPref(sched)*prefWeight) + 
				(evalPair(sched)*pairWeight) + 
				(evalSecDiff(sched)*secDiffWeight) ;
		
		
		return totalEval;
	}
	
	public double evalTest(Schedule sched)
	{
		double totalEval = -1;
		
		totalEval = (evalMinFilled(sched)*minFilledWeight) + 
				(evalPrefTest(sched)*prefWeight) + 
				(evalPair(sched)*pairWeight) + 
				(evalSecDiff(sched)*secDiffWeight) ;
		
		System.out.println("Min weight:" + minFilledWeight);
		System.out.println("pref weight:" + prefWeight);
		System.out.println("pair weight:" + pairWeight);
		System.out.println("Sec weight:" + secDiffWeight);
		
		
		return totalEval;	
	}

	/**
	 * For soft constraints. Checks that each section of a lecture are a different times
	 * This is a CPSC only soft constraint. 
	 * How it works: 
	 * 		We have alreadyDone which is a list of courses that we've already dealt with.
	 * 		Traverse through the courses in the schedule and look at every element. If we haven't 
	 * 		looked at this course before, then we find all the lecture sections of it. 
	 * 		We add all the sections to alreadyDone so we don't look at any of them in the future. 
	 * 		Now we look at the time slot for every element in sectionsOfCourse, and see if any other
	 * 		sections have the same time slot. If they do we add 1 to our fail count.
	 *  
	 * @param sched 
	 * 		full or partial schedule
	 * @return 
	 * 		how many times this is violated
	 */
	public double evalSecDiff(Schedule sched) {
		
		int failCount = 0;
		ArrayList<Course> sectionsOfCourse = new ArrayList<Course>(); //A list of all the sections for a class
		ArrayList<Course> alreadyDone = new ArrayList<Course>(); //List of courses already looked
		Course workingCourse, tempCourse;
		TimeSlot workingTimeSlot;
		
		for(int i = 0;i<sched.getCourses().size();i++)
		{
			sectionsOfCourse = new ArrayList<Course>();
			workingCourse = sched.getCourses().get(i);
			
			//If we haven't already looked at this course
			if((!alreadyDone.contains(workingCourse)) && workingCourse.getDepartment().equals("CPSC"))
			{
				
				sectionsOfCourse.add(workingCourse);
				alreadyDone.add(workingCourse);
				
				//Traverse through the course list and find all the sections
				//of the selected working course
				for(int j = 0;j<sched.getCourses().size();j++)
				{
					tempCourse = sched.getCourses().get(j);
					
					//If they are the same course (with different sections)
					if(tempCourse.getDepartment().equals(workingCourse.getDepartment()) &&
							tempCourse.getNumber() == workingCourse.getNumber() &&
							tempCourse.getSection() != workingCourse.getSection())
					{
						sectionsOfCourse.add(tempCourse);
						alreadyDone.add(tempCourse);
					}
					
				}
				
				if(sectionsOfCourse.size() >1)
				{
					//System.out.println(sectionsOfCourse.size());
					//At this point, sections of course is all filled up with the sections of one course
					//Now we have to check that they all have different time slots
					while(sectionsOfCourse.size() > 0)
					{
						workingTimeSlot = sectionsOfCourse.get(0).getSlot();
						
						if(workingTimeSlot != null)
						{
							for(int j = 1;j<sectionsOfCourse.size();j++)
							{
								if(workingTimeSlot.getDay().equals(sectionsOfCourse.get(j).getSlot().getDay()) &&
										workingTimeSlot.getStartTime() == sectionsOfCourse.get(j).getSlot().getStartTime())
									failCount++;
							}
						}
						sectionsOfCourse.remove(0);
					}	
				}
			}
		}		
		return failCount * this.get_penSection();
	}

	/**
	 * For soft constraints. Looks at the pairings given in input file and checks how many 
	 * times they were not upheld.
	 * @param sched full or partial schedule
	 * @return returns an integer that is equal to the number of pairings not met
	 * 
	 */
	public double evalPair(Schedule sched) {
		
		int failCount = 0;
		Course pairCourse_x, pairCourse_y;
		Course workingCourse1, workingCourse2;
		
		//Go through all the pairs
		for(int i = 0;i<pairs.size();i++)
		{
			
			pairCourse_x = pairs.get(i).getKey();
			pairCourse_y = pairs.get(i).getValue();
			
			//Find courses in list
			for(int j = 0; j<sched.getCourses().size();j++)
			{
				workingCourse1 = sched.getCourses().get(j);
				
				if(workingCourse1.equals(pairCourse_x))
				{
					for(int k = 0;k<sched.getCourses().size();k++)
					{
						workingCourse2 = sched.getCourses().get(k);
						
						if(workingCourse2.equals(pairCourse_y))
						{
							if(! (workingCourse1.getSlot().getStartTime() == workingCourse2.getSlot().getStartTime() &&
									workingCourse1.getSlot().getDay().equals(workingCourse2.getSlot().getDay()) &&
									workingCourse1.getSlot().getMaxNum() == workingCourse2.getSlot().getMaxNum()))
							{
								failCount++;
							}
						}
					}
				}
			}

		}
		return failCount * this.get_penNotPaired();
	}

	
	public int evalPrefTest(Schedule sched)
	{
		int failCount = 0;
		int index = -1;
		Course workingCourse;
		Lab workingLab;
		int testCount = 0;
		
		
		for(int i = 0;i<preferences.size();i++)
		{
			index = preferences.get(i).getY().getIndex();
			
			if(preferences.get(i).getY().getClass() == Course.class) //is a course
			{
				workingCourse = sched.getCourses().get(index);
				
				if(workingCourse.getSlot() != preferences.get(i).getX())
				{
					failCount += preferences.get(i).getZ();

				}
				else
				{
					System.out.println(i);	
				}
			}
			else if(preferences.get(i).getY().getClass() == Lab.class)//is a lab
			{
				workingLab = sched.getLabs().get(index);
				
				if(workingLab.getSlot() != preferences.get(i).getX())
				{
					failCount += preferences.get(i).getZ();
				}
				else
				{
					System.out.println(i);	
				}
				
			}

			
			testCount += preferences.get(i).getZ();
			
		}
		
		//System.out.println(testCount);
		return failCount;
		
		
	}
	/**
	 * Checks how many of our preferences were met. For each preference, there is a value.
	 * @param sched
	 * @return return the total value of all the preferences that were not met.
	 */
	public int evalPref(Schedule sched) {
		
		int failCount = 0;
		int index = -1;
		Course workingCourse;
		Lab workingLab;
		int testCount = 0;
		
		
		for(int i = 0;i<preferences.size();i++)
		{
			index = preferences.get(i).getY().getIndex();
			
			if(preferences.get(i).getY().getClass() == Course.class) //is a course
			{
				workingCourse = sched.getCourses().get(index);
				
				if(workingCourse.getSlot() != preferences.get(i).getX())
				{
					failCount += preferences.get(i).getZ();
				}
			}
			else //is a lab
			{
				workingLab = sched.getLabs().get(index);
				
				if(workingLab.getSlot() != preferences.get(i).getX())
				{
					failCount += preferences.get(i).getZ();
				}
				
			}
			
			testCount += preferences.get(i).getZ();
			
		}
		
		//System.out.println(testCount);
		return failCount;
		
		/*
		ArrayList<Course> uniqueCourses = new ArrayList<Course>();
		ArrayList<Course> uniqueLabs = new ArrayList<Course>();
		
		ArrayList<Pair<Course, Integer>> cRunningTotals = new ArrayList<Pair<Course, Integer>>();
		ArrayList<Pair<Course, Integer>> lRunningTotals = new ArrayList<Pair<Course, Integer>>();
		
		ArrayList<Course> cHasPrefMet = new ArrayList<Course>();	
		ArrayList<Lab> lHasPrefMet = new ArrayList<Lab>();
		
		boolean continueChecking = true;
		Course prefCourse, workingCourse;
		Lab prefLab, workingLab;
		TimeSlot prefTimeSlot, workingTimeSlot;
		int prefValue;
		int totalToReturn = 0;
		
		//Traverse through preferences to find the unique courses and labs
		//because each lab and course can have more than one 
		for(int i = 0;i<preferences.size();i++)
		{
			if(preferences.get(i).getY().getClass() == Course.class)//working with a course
			{
				if(!uniqueCourses.contains(preferences.get(i).getY()))
				{
					uniqueCourses.add(preferences.get(i).getY());
				}
			}
			else //working with a class
			{
				if(!uniqueLabs.contains((Lab) preferences.get(i).getY()))
				{
					uniqueLabs.add((Lab)preferences.get(i).getY());
				}
			}
		}
		
		//Now have two lists that contains all the unique courses and labs
		//-----
		

		for(int i = 0; i<uniqueCourses.size();i++)
		{
			cRunningTotals.add(new Pair<Course, Integer>(uniqueCourses.get(i), 0));
		}
		

		for(int i = 0; i<uniqueLabs.size();i++)
		{
			lRunningTotals.add(new Pair<Course, Integer>(uniqueLabs.get(i), 0));
		}
		
		//Have a running total pair for each unique course
		//When we find one that meets a single preference
		//then set that total to zero and don't change it anymore 
		
		//Go through the preferences check if they are upheld
		for(int i = 0; i<preferences.size();i++)
		{
			prefTimeSlot = preferences.get(i).getX();
			prefValue = preferences.get(i).getZ();
			continueChecking = true;
			
			//If it's a lab, if not, must be a class
			if(preferences.get(i).getY().getClass() == Lab.class)
			{
				prefLab = (Lab) preferences.get(i).getY();
				
				//Want to check if we have met a preference for this course already
				//If we have, we don't need to do a check for this preference s
				for(int j = 0;j<lHasPrefMet.size();j++)
				{
					if(prefLab.equals(lHasPrefMet.get(j)))
						continueChecking = false;
				}
				
				if(continueChecking == true)
				{
					//Have to find this lab in the schedule and look to see if
					//it's in the correct time slot
A:					for(int j = 0;j<sched.getLabs().size();j++)
					{
						workingLab = sched.getLabs().get(j);
						workingTimeSlot = workingLab.getSlot();
						
						if(workingLab.equals(prefLab))
						{
							if((workingTimeSlot.getDay().equals(prefTimeSlot.getDay())) &&
									(workingTimeSlot.getStartTime() == prefTimeSlot.getStartTime()) &&
									(workingTimeSlot.getMaxNum() == prefTimeSlot.getMaxNum())
									)
							{
								//If we met the pref, we add it so this course won't be 
								//checked again cause a course can have more than 1 pref. and
								//Obviously not all can be met
								lHasPrefMet.add(workingLab);
								
								//Find this lab in lRunningTotals and set it to zero
								
X:								for(int k = 0; k<lRunningTotals.size();k++)
								{
									Lab temp;
									temp = (Lab) lRunningTotals.get(k).getKey();
									if(temp.equals(workingLab))
									{
										lRunningTotals.set(k, new Pair<Course, Integer>(prefLab, 0));
										break X;
									}
								}
								break A;
							}
							else
							{
								//The lab did not meet the preferences being tested
								//so we need to add it's prefValue to the total for 
								//that unique course
Q:								for(int k = 0; k<lRunningTotals.size();k++)
								{
									Lab temp;
									int tempTotal;
									
									tempTotal = lRunningTotals.get(k).getValue();
									temp = (Lab) lRunningTotals.get(k).getKey();
									if(temp.equals(workingLab))
									{
										lRunningTotals.set(k, new Pair<Course, Integer>(prefLab, tempTotal + prefValue));
										break Q;
									}
								}

								break A;
							}
							//SO, they are the same class, now check time slot and break out
						}
					}
				}
			}
			else //If the preference is talking about a course
			{

				prefCourse = preferences.get(i).getY();
				//Want to check if we have met a preference for this course already
				for(int j = 0;j<cHasPrefMet.size();j++)
				{
					if(prefCourse.equals(cHasPrefMet.get(j)))
						continueChecking = false;
				}
				
				if(continueChecking == true)
				{
					//Have to find this course in the schedule and look to see if
					//it's in the correct time slot
B:					for(int j = 0;j<sched.getCourses().size();j++)
					{
						workingCourse = sched.getCourses().get(j);
						workingTimeSlot = workingCourse.getSlot();
						
						if(workingCourse.equals(prefCourse))
						{
							/*
							System.out.print(workingCourse.getDepartment());
							System.out.print(" " + workingCourse.getNumber());
							System.out.print(" " + workingCourse.getSection());
							System.out.println();

							
							System.out.print(workingTimeSlot.getDay());
							System.out.print(" " + workingTimeSlot.getStartTime());
							System.out.println();
							

							System.out.print(prefTimeSlot.getDay());
							System.out.print(" " + prefTimeSlot.getStartTime());
							
	
							
							if((workingTimeSlot.getDay().equals(prefTimeSlot.getDay())) &&
									(workingTimeSlot.getStartTime() == prefTimeSlot.getStartTime()) &&
									(workingTimeSlot.getMaxNum() == prefTimeSlot.getMaxNum())
									)
							{

								//Same ideas as when doing labs
								cHasPrefMet.add(workingCourse);
Y:								for(int k = 0; k<cRunningTotals.size();k++)
								{
									Course temp;
									temp = cRunningTotals.get(k).getKey();
									if(temp.equals(workingCourse))
									{
										cRunningTotals.set(k, new Pair<Course, Integer>(prefCourse, 0));
	
										break Y;
									}
								}
								break B;
							}
							else
							{
P:								for(int k = 0; k<cRunningTotals.size();k++)
								{
									Course temp;
									int tempTotal;
									
									tempTotal = cRunningTotals.get(k).getValue();
									temp = cRunningTotals.get(k).getKey();
									if(temp.equals(workingCourse))
									{										
										cRunningTotals.set(k, new Pair<Course, Integer>(prefCourse, tempTotal + prefValue));
										
										break P;

									}
								}

								break B;
							}
						}
					}
				}
			}
		}
		
		//Add up our fail totals
		for(int i = 0;i<cRunningTotals.size();i++)
		{
			totalToReturn += cRunningTotals.get(i).getValue();
		}
		
		
		for(int i = 0;i<lRunningTotals.size();i++)
		{
			totalToReturn += lRunningTotals.get(i).getValue();
		}
		
		
		return totalToReturn;
		
		*/
	}

	/**
	 * Checks if the labs and time slots min requirements are filled
	 * @param sched full or partial schedule
	 * @return returns how many times a lab or course slot did NOT have it's minimum met
	 */
	public double evalMinFilled(Schedule sched) {
		
		double totalFailCount = 0;
		totalFailCount = this.evalMinCourse(sched) + this.evailMinLabs(sched);
		return totalFailCount;
	}
	
	private double evalMinCourse(Schedule sched)
	{
		int totalFailCount = 0;
		int timeSlotCounter = 0;
		TimeSlot workingCslot, workingLslot;
		Course workingCourse;
		Lab workingLab;
		
		//Check the min for all course slots first.
		for(int i = 0;i<courseSlots.size();i++)
		{
			timeSlotCounter = 0;
			workingCslot = courseSlots.get(i);
			
			//Traverse through all the courses and look at their time slots
			for(int j = 0;j<sched.getCourses().size();j++)
			{
				workingCourse = sched.getCourses().get(j);
				
				//If it's the same time slot, add to counter
				if(workingCourse.getSlot().getStartTime() == workingCslot.getStartTime() &&
						workingCourse.getSlot().getDay() == workingCslot.getDay() &&
						workingCourse.getSlot().getIsLab() == workingCslot.getIsLab())
				{
					timeSlotCounter++;
				}
			}

			
			if(timeSlotCounter < workingCslot.getMinNum())
				totalFailCount++;		
		}
		
		return totalFailCount * this.get_penCoursemin();
	}
	
	private double evailMinLabs(Schedule sched)
	{
		int totalFailCount = 0;
		int timeSlotCounter = 0;
		TimeSlot workingCslot, workingLslot;
		Course workingCourse;
		Lab workingLab;
		
		//Check the min for all labs slots first.
				for(int i = 0;i<labSlots.size();i++)
				{
					timeSlotCounter = 0;
					workingLslot = labSlots.get(i);
					
					//Traverse through all the Labs and look at their time slots
					for(int j = 0;j<sched.getLabs().size();j++)
					{
						workingLab = sched.getLabs().get(j);
						
						//If it's the same time slot, add to counter
						if(workingLab.getSlot().getStartTime() == workingLslot.getStartTime() &&
								workingLab.getSlot().getDay() == workingLslot.getDay() &&
								workingLab.getSlot().getIsLab() == workingLslot.getIsLab())
						{
							timeSlotCounter++;
						}
					}
					
					if(timeSlotCounter < workingLslot.getMinNum())
						totalFailCount++;		
				}		
		return totalFailCount * this.get_penLabsmin();
	}
	
	public double getMinFilledWeight() {
		return minFilledWeight;
	}

	public void setMinFilledWeight(double minFilledWeight) {
		this.minFilledWeight = minFilledWeight;
	}

	public double getPrefWeight() {
		return prefWeight;
	}

	public void setPrefWeight(double prefWeight) {
		this.prefWeight = prefWeight;
	}

	public double getPairWeight() {
		return pairWeight;
	}

	public void setPairWeight(double pairWeight) {
		this.pairWeight = pairWeight;
	}

	public double getSecDiffWeight() {
		return secDiffWeight;
	}

	public void setSecDiffWeight(double secDiffWeight) {
		this.secDiffWeight = secDiffWeight;
	}
	
	public double get_penCoursemin() {
		return _penCoursemin;
	}

	public void set_penCoursemin(double _penCoursemin) {
		this._penCoursemin = _penCoursemin;
	}

	public double get_penLabsmin() {
		return _penLabsmin;
	}

	public void set_penLabsmin(double _penLabsmin) {
		this._penLabsmin = _penLabsmin;
	}

	public double get_penNotPaired() {
		return _penNotPaired;
	}

	public void set_penNotPaired(double _penNotPaired) {
		this._penNotPaired = _penNotPaired;
	}

	public double get_penSection() {
		return _penSection;
	}

	public void set_penSection(double _penSection) {
		this._penSection = _penSection;
	}
}
