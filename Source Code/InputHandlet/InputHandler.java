package InputHandlet;

import helpers.Helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.util.Pair;

import types.*;

public class InputHandler {
	
	private String 		   _fileName = "";
	private File 		   _fileObj  = null;
	private BufferedReader _reader   = null;
	
	// soft constraints
	private double 		   _minField     = 1.00;
	private double 		   _pref         = 1.00;
	private double 		   _pair         = 1.00;
	private double 		   _secdiff      = 1.00;
	private double 		   _penCoursemin = 1.00;
	private double 		   _penLabsmin   = 1.00;
	private double         _penNotPaired = 1.00;
	private double         _penSection   = 1.00;
	

	//will be displayed to the user if they haven't provided command line arguments at all
	//or if only (presumably filename) was provided.
	private static final String _USAGE = "filename minfield pref pair secdiff";
	
	private Scanner scanner = new Scanner(new InputStreamReader(System.in));
	
	private ArrayList<TimeSlot> 						 courseSlots = new ArrayList<TimeSlot>();
	private ArrayList<TimeSlot> 						 labSlots = new ArrayList<TimeSlot>();
	private ArrayList<Course> 							 courses = new ArrayList<Course>();
	private ArrayList<Lab> 							     labs = new ArrayList<Lab>();
	private ArrayList<Pair<Course, Course>> 			 notCompatible = new ArrayList<Pair<Course, Course>>();
	private ArrayList<Pair<Course, TimeSlot>> 			 unwanted = new ArrayList<Pair<Course, TimeSlot>>();
	private ArrayList<Triple<TimeSlot, Course, Integer>> preferences = new ArrayList<Triple<TimeSlot, Course, Integer>>();
	private ArrayList<Pair<Course, Course>> 			 pairs = new ArrayList<Pair<Course, Course>>();
	private ArrayList<Pair<Course, TimeSlot>>			 partials = new ArrayList<Pair<Course, TimeSlot>>();
	
	private boolean added813 = false;
	private boolean added913 = false;
	
	//String values corresponding to FileSection the way the appear in the input file
	private static final String NAME = "Name:";
	private static final String CS = "Course slots:";
	private static final String LS = "Lab slots:";
	private static final String C = "Courses:";
	private static final String L = "Labs:";
	private static final String NC = "Not compatible:";
	private static final String UW = "Unwanted:";
	private static final String P = "Preferences:";
	private static final String PAIR = "Pair:";
	private static final String PAR = "Partial Assignments:";

	
	
	

	
	public InputHandler(String[] args)
	{
		
		parseCommandLineArgs(args);
	}
	
	public InputHandler(String fileName)
	{
		set_fileName(fileName);
	}

	/**TODO: redesign.
	* Might need different return type
	* Main worker method. Calls variety of other methods
	*/
	public void ReadAndPopulate()
	{	
		_fileObj = new File(get_fileName());
		try {
			_fileObj = new File(get_fileName());
			_reader = new BufferedReader(new FileReader(_fileObj));
			doTheRead(_reader);
			
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("IO exception");
		} 
		
	}
	
	public boolean isAdded813() {
		return added813;
	}

	public void setAdded813(boolean added813) {
		this.added813 = added813;
	}

	public boolean isAdded913() {
		return added913;
	}

	public void setAdded913(boolean added913) {
		this.added913 = added913;
	}

	/**
	 * shit name, m8. Rename
	 * */
	private void doTheRead(BufferedReader bufferedReader)
	{
		FileSections section = null;
		String text;
		try {
			/**
			 * Look at how example input fields are structured. We read file line by line
			 * Each section will be headed by the it's proper name( contents of filesection enum)
			 * Once we know what section the lines are from handle them apropriately
			 * 
			 * */
			while((text = bufferedReader.readLine()) != null)
			{
				//Determine what section we're in
				if (text.equalsIgnoreCase(NAME)) {
					section = FileSections.NAME;
				} else if (text.equalsIgnoreCase(CS)) {
					section = FileSections.COURSESLOTS;
				} else if (text.equalsIgnoreCase(LS)) {
					section = FileSections.LABSLOTS;
				} else if (text.equalsIgnoreCase(C)) {
					section = FileSections.COURSES;
				} else if (text.equalsIgnoreCase(L)) {
					section = FileSections.LABS;
				} else if (text.equalsIgnoreCase(NC)) {
					section = FileSections.NOTCOMPATIBLE;
				} else if (text.equalsIgnoreCase(UW)) {
					section = FileSections.UNWANTED;
				} else if (text.equalsIgnoreCase(P)) {
					section = FileSections.PREFERENCES;
				} else if (text.equalsIgnoreCase(PAIR)) {
					section = FileSections.PAIR;
				} else if (text.equalsIgnoreCase(PAR)) {
					section = FileSections.PARTIALASSIGNMENTS;
				} else if (text.equalsIgnoreCase("")) {
					// Do nothing on empty line
				}
				//depending on the section add the data appropriately
				else {
					switch(section)
					{
					case NAME:
						//do nothing with Name
						break;
					case COURSESLOTS:	
						addTimeSlot(text, false, courseSlots);
						break;
					case LABSLOTS:
						addTimeSlot(text, true, labSlots);
						break;
					case COURSES:
						Course c = parseCourse(text);
						courses.add(c);
						// Adds CPSC 813 or 913 to the labs list.
						// This is needed as requirement has said that 813 and 913 will never be in the courses list and need to be added in this way
						// These are listed as labs as they use lab slots instead of course slots
						// No time slot is assigned at this point
						// booleans used to prevent adding duplicate course if there are multiple sections of 313 or 413
						if (c.getDepartment().equals("CPSC") && c.getNumber() == 313 && !added813) {
							labs.add(new Lab(813, "CPSC", 1));
							added813 = true;
						} else if (c.getDepartment().equals("CPSC") && c.getNumber() == 413 && !added913) {
							labs.add(new Lab(913, "CPSC", 1));
							added913 = true;
						}
						
						
						c.setIndex(courses.size()-1);
						
						break;
					case LABS:
						Lab tempLab = (Lab)parseCourse(text);
						labs.add(tempLab);
						
						tempLab.setIndex(labs.size()-1);
						
						break;	
					case NOTCOMPATIBLE:
						
						
						
						// TODO: add matching notCompatible pair for 813 or 913 if course is 313 or 413
						// search through notCompatible for a pair that already contains 813/913 and the other course
						// before creating a new pair to prevent duplicates
						Pair<Course, Course> p = parseClassPair(text, courses, labs);
						notCompatible.add(p);
						Course c1 = (Course)p.getKey();
						Course c2 = (Course)p.getValue();
						// if the key is 313
						if (c1.getDepartment().equalsIgnoreCase("CPSC") && c1.getNumber() == 313) {
							// Find 813 in labs
							for (Lab lab : labs) {
								if (lab.getDepartment().equalsIgnoreCase("CPSC") && lab.getNumber() == 813) {
									c1 = lab;
								}
							}
							
							boolean pairMatch = false;
							// Search to find if a not compatible pair for 813 and the other class already exists
							for (Pair<Course, Course> pair : notCompatible) {
								Course x = (Course)pair.getKey();
								Course y = (Course)pair.getValue();
								// This checks if the pair exists as (813, x) or (x, 813)
								if ((x.equals(c1) && y.equals(c2)) || (x.equals(c2) & y.equals(c1))) {
									pairMatch = true;
								}
							}
							
							// If not found, create and add the pair
							if (!pairMatch) {
								notCompatible.add(new Pair<Course, Course>(c1, c2));
							}
							// If the value is 313
						} else if (c2.getDepartment().equalsIgnoreCase("CPSC") && c2.getNumber() == 313) {
							// Find 813 in labs
							for (Lab lab : labs) {
								if (lab.getDepartment().equalsIgnoreCase("CPSC") && lab.getNumber() == 813) {
									c2 = lab;
								}
							}
							
							boolean pairMatch = false;
							// Search to find if a not compatible pair for 813 and the other class already exists
							for (Pair<Course, Course> pair : notCompatible) {
								Course x = (Course)pair.getKey();
								Course y = (Course)pair.getValue();
								// This checks if the pair exists as (813, x) or (x, 813)
								if ((x.equals(c1) && y.equals(c2)) || (x.equals(c2) & y.equals(c1))) {
									pairMatch = true;
								}
							}
							
							// If not found, create and add the pair
							if (!pairMatch) {
								notCompatible.add(new Pair<Course, Course>(c1, c2));
							}
							// If key is 413
						} else if (c1.getDepartment().equalsIgnoreCase("CPSC") && c1.getNumber() == 413) {
							// Find 913 in labs
							for (Lab lab : labs) {
								if (lab.getDepartment().equalsIgnoreCase("CPSC") && lab.getNumber() == 913) {
									c1 = lab;
								}
							}
							
							boolean pairMatch = false;
							// Search to find if a not compatible pair for 913 and the other class already exists
							for (Pair<Course, Course> pair : notCompatible) {
								Course x = (Course)pair.getKey();
								Course y = (Course)pair.getValue();
								// This checks if the pair exists as (913, x) or (x, 913)
								if ((x.equals(c1) && y.equals(c2)) || (x.equals(c2) & y.equals(c1))) {
									pairMatch = true;
								}
							}
							
							// If not found, create and add the pair
							if (!pairMatch) {
								notCompatible.add(new Pair<Course, Course>(c1, c2));
							}
							// If the value is 413
						} else if (c2.getDepartment().equalsIgnoreCase("CPSC") && c2.getNumber() == 413) {
							// Find 913 in labs
							for (Lab lab : labs) {
								if (lab.getDepartment().equalsIgnoreCase("CPSC") && lab.getNumber() == 913) {
									c2 = lab;
								}
							}
							
							boolean pairMatch = false;
							// Search to find if a not compatible pair for 913 and the other class already exists
							for (Pair<Course, Course> pair : notCompatible) {
								Course x = (Course)pair.getKey();
								Course y = (Course)pair.getValue();
								// This checks if the pair exists as (913, x) or (x, 913)
								if ((x.equals(c1) && y.equals(c2)) || (x.equals(c2) & y.equals(c1))) {
									pairMatch = true;
								}
							}
							
							// If not found, create and add the pair
							if (!pairMatch) {
								notCompatible.add(new Pair<Course, Course>(c1, c2));
							}
						}
						break;
					case UNWANTED:
						unwanted.add(parseUW(text, courses, labs, courseSlots, labSlots));
						break;
					case PREFERENCES:
						Triple<TimeSlot, Course, Integer> t = parsePreference(text, courses, labs, courseSlots,
								labSlots);
						if (t != null) {
							preferences.add(t);
						}
						break;
					case PAIR:
						pairs.add(parseClassPair(text, courses, labs));
						break;
					case PARTIALASSIGNMENTS:
						Pair<Course, TimeSlot> p3 = (parsePartialAssignment(text, courses, labs, courseSlots, labSlots));
						
						if (p3 != null) {
							partials.add(parsePartialAssignment(text, courses, labs, courseSlots, labSlots));
						}
						break;
					}
				}
			}
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}
	
	
	

	public void addTimeSlot(String info, boolean isLab, ArrayList<TimeSlot> slots) {
		String[] courseInfo = info.split(",");

		// Removes all spaces
		for (int i = 0; i < courseInfo.length; i++) {
			courseInfo[i] = courseInfo[i].replace(" ", "");
		}

		ScheduleDay day = ScheduleDay.fromString(courseInfo[0]);
		int time = Integer.parseInt(courseInfo[1].replace(":", ""));
		int max = Integer.parseInt(courseInfo[2]);
		int min = Integer.parseInt(courseInfo[3]);

		slots.add(new TimeSlot(day, time, max, min, isLab));
	}
	
	/**
	 * Creates and returns a course parsed from the input string Handles and
	 * returns both lectures and labs
	 * 
	 * @param courseInfo
	 *            is the string of text read from the input file that contains
	 *            information about the class
	 * @return
	 */
	public Course parseCourse(String courseInfo) {
		// Splits the info by spaces
		String[] badFormat = courseInfo.split(" ");

		// Removes "" from list which are caused by extra spaces
		ArrayList<String> formattedInfo = new ArrayList<String>();
		for (String s : badFormat) {
			if (!s.equalsIgnoreCase("")) {
				formattedInfo.add(s);
			}
		}

		String name = formattedInfo.get(0);

		int number = -1;
		try {
			number = Integer.parseInt(formattedInfo.get(1));
		} catch (NumberFormatException e) {
			System.err.println("Number not found for class.");
		}

		boolean isLab = false;
		if (formattedInfo.get(2).equalsIgnoreCase("TUT") || formattedInfo.get(2).equalsIgnoreCase("LAB")) {
			isLab = true;
		}

		int section = -1;
		// if lab, create lab and return
		try {
			section = Integer.parseInt(formattedInfo.get(3));
		} catch (NumberFormatException e) {
			System.err.println("Section number not found for course.");
		}

		// If lab, create lab and return
		if (isLab) {
			return (new Lab(number, name, section));
		} else if (formattedInfo.size() == 4) {
			// if there are only 4 parts to the info and it's not
			// a lab, create a course and return
			return (new Course(number, name, section));
		}

		// No else needed as returns will exit the method

		isLab = true;
		int lecSection = section;
		section = -1;

		try {
			section = Integer.parseInt(formattedInfo.get(5));
		} catch (NumberFormatException e) {
			System.err.println("Section not found for lab.");
		}

		return (new Lab(number, name, section, lecSection));
	}
	
	/**
	 * Returns a pair of courses parsed from the input string. Used for both not
	 * compatible and pair
	 * 
	 * @param info
	 *            is the input string read from the file
	 * @param cs
	 *            is the listed of courses used to add a reference to an
	 *            existing course rather than create a new one
	 * @param ls,
	 *            same idea as cs, but with labs
	 * @return
	 */
	public Pair<Course, Course> parseClassPair(String info, ArrayList<Course> cs, ArrayList<Lab> ls) {
		String[] coursesInfo = info.split(",");

		Course class1 = parseCourse(coursesInfo[0]);
		Course class2 = parseCourse(coursesInfo[1]);

		if (class1 instanceof Lab) {
			for (Lab l : ls) {
				if (class1.equals(l)) {
					class1 = l;
				}
			}
		} else {
			for (Course c : cs) {
				if (class1.equals(c)) {
					class1 = c;
				}
			}
		}

		if (class2 instanceof Lab) {
			for (Lab l : ls) {
				if (class2.equals(l)) {
					class2 = l;
				}
			}
		} else {
			for (Course c : cs) {
				if (class2.equals(c)) {
					class2 = c;
				}
			}
		}

		return (new Pair<Course, Course>(class1, class2));
	}
	
	
	/**
	 * Parses text in the Unwanted section and returns a pair of a course and a
	 * timeslot
	 * 
	 * @param text
	 *            is the text that was read from the Unwanted section
	 * @param courses
	 *            is the list of courses already created. May be used to find a
	 *            reference to an already existing class rather than creating a
	 *            new one
	 * @param labs
	 *            is the same as courses, but for labs
	 * @param courseSlots,
	 *            same idea as courses, but with course time slots
	 * @param labSlots,
	 *            same idea as courseSlots, but with lab slots
	 * @return
	 */
	public Pair<Course, TimeSlot> parseUW(String text, ArrayList<Course> courses, ArrayList<Lab> labs,
			ArrayList<TimeSlot> courseSlots, ArrayList<TimeSlot> labSlots) {
		String[] splitInfo = text.split(",");

		int courseIndex;
		int slotIndex;
		
		// If the first part is of length 2 it cannot be course info as that is much longer
		// Assumes all time slots will be given in the same format as shown in the ShortExample
		if (splitInfo[0].replace(" ", "").length() == 2) {
			slotIndex = 0;
			// courseIndex set to 2 as a timeslot has 2 parts in splitInfo
			courseIndex = 2;
		} else {
			//Otherwise, course must be first
			courseIndex = 0;
			slotIndex = 1;
		}
		
		Course c = parseCourse(splitInfo[courseIndex]);
		
		TimeSlot slot = null;
		ScheduleDay day = ScheduleDay.fromString(splitInfo[slotIndex].replace(" ", ""));

		splitInfo[slotIndex + 1] = (splitInfo[slotIndex + 1].replace(" ", "")).replace(":", "");
		int time = Integer.parseInt(splitInfo[slotIndex + 1]);

		// Searches for existing references
		if (c instanceof Lab) {
			for (Lab l : labs) {
				if (l.equals((Lab) c)) {
					c = l;
				}
			}

			boolean slotFound = false;

			for (TimeSlot s : labSlots) {
				if (s.equals(day, time)) {
					slot = s;
					slotFound = true;
				}
			}

			if (!slotFound) {
				slot = new TimeSlot(day, time, true);
			}

			return (new Pair<Course, TimeSlot>(c, slot));
		} else {
			for (Course c2 : courses) {
				if (c2.equals(c)) {
					c = c2;
				}
			}

			boolean slotFound = false;
			for (TimeSlot s : courseSlots) {
				if (s.equals(day, time)) {
					slot = s;
					slotFound = true;
				}
			}

			if (!slotFound) {
				slot = new TimeSlot(day, time, false);
			}

			return (new Pair<Course, TimeSlot>(c, slot));
		}
	}
	
	
	/**
	 * Parses a pair containing a Course and a TimeSlot from the given string.
	 * If a course does not have an existing match in a list, a new object is
	 * created, but not added to the list. if an exisiting timeslot does not
	 * match the parsed timeslot, null is returned. DOES NOT assign the time
	 * slot to the class, just returns the pair
	 * 
	 * @param info
	 * @param cs
	 * @param ls
	 * @param cSlots
	 * @param lSlots
	 * @return
	 */
	public Pair<Course, TimeSlot> parsePartialAssignment(String info, ArrayList<Course> cs, ArrayList<Lab> ls,
			ArrayList<TimeSlot> cSlots, ArrayList<TimeSlot> lSlots) {
		String[] splitInfo = info.split(",");

		int courseIndex;
		int slotIndex;
		
		// If the first part is of length 2 it cannot be course info as that is much longer
		// Assumes all time slots will be given in the same format as shown in the ShortExample
		if (splitInfo[0].replace(" ", "").length() == 2) {
			slotIndex = 0;
			// courseIndex set to 2 as a timeslot has 2 parts in splitInfo
			courseIndex = 2;
		} else {
			//Otherwise, course must be first
			courseIndex = 0;
			slotIndex = 1;
		}
		
		ScheduleDay day = ScheduleDay.fromString(splitInfo[slotIndex].replace(" ", ""));
		int time = Integer.parseInt((splitInfo[slotIndex + 1].replace(" ", "")).replace(":", ""));
		TimeSlot slot = null;

		Course course = parseCourse(splitInfo[courseIndex]);

		// Searches for existing references
		if (course instanceof Lab) {
			for (Lab l : ls) {
				if (l.equals((Lab) course)) {
					course = l;
				}
			}

			boolean slotFound = false;
			for (TimeSlot s : lSlots) {
				if (s.equals(day, time)) {
					slot = s;
					slotFound = true;
				}
			}

			// Returns null if no slot is found as per Jorg's instructions
			if (!slotFound) {
				return null;
			}

		} else {
			for (Course c2 : cs) {
				if (c2.equals(course)) {
					course = c2;
				}
			}

			boolean slotFound = false;
			for (TimeSlot s : cSlots) {
				if (s.equals(day, time)) {
					slot = s;
					slotFound = true;
				}
			}

			if (!slotFound) {
				return null;
			}
		}

		return (new Pair<Course, TimeSlot>(course, slot));
	}
	
	
	/**
	 * Returns a Triple containing a TimeSlot, a Course and an Integer parsed
	 * from the input string. If a parsed course does not have a
	 * match in the existing lists, a new object is created, but not added to
	 * the lists. If a parsed timeslot does not have a match, null is returned
	 * 
	 * @param text
	 * @param courses
	 * @param labs
	 * @param courseSlots
	 * @param labSlots
	 * @return
	 */
	public Triple<TimeSlot, Course, Integer> parsePreference(String text, ArrayList<Course> courses,
			ArrayList<Lab> labs, ArrayList<TimeSlot> courseSlots, ArrayList<TimeSlot> labSlots) {
		String[] splitInfo = text.split(",");
		
		int slotIndex;
		int courseIndex;
		int numIndex;
		
		// If first string is only 2 long and is not a number, then the first two strings form a timeslot
		if (splitInfo[0].replace(" ", "").length() == 2 && !Helper.isNumeric(splitInfo[0].replace(" ", ""))) {
			slotIndex = 0;
			// if third string is numeric, it is the preference number and the 4th must be the course
			if (Helper.isNumeric(splitInfo[2].replace(" ", ""))) {
				numIndex = 2;
				courseIndex = 3;
			} else {
				// Otherwise, the course must be the 3rd and the num must be the 4th
				courseIndex = 2;
				numIndex = 3;
			}
			// If the first string is a number, it must be the num
		} else if(Helper.isNumeric(splitInfo[0].replace(" ", ""))) {
			numIndex = 0;
			// If 2nd string is only 2 long and the num has already been found, then the 2nd and 3rd strings must be the timeslot and the 4th must be the course
			if (splitInfo[0].replace(" ", "").length() == 2) {
				slotIndex = 1;
				courseIndex = 3;
			} else {
				courseIndex = 1;
				slotIndex = 2;
			}
			// If first string not num or slot, it must be course
		} else {
			courseIndex = 0;
			// If second string is a number, it must be num and the slot is the last 2 strings
			if (Helper.isNumeric(splitInfo[0].replace(" ", ""))) {
				numIndex = 1;
				slotIndex = 2;
			} else {
				slotIndex = 1;
				numIndex = 3;
			}
		}

		ScheduleDay day = ScheduleDay.fromString(splitInfo[slotIndex]);
		int time = Integer.parseInt((splitInfo[slotIndex + 1].replace(" ", "")).replace(":", ""));
		TimeSlot slot = null;

		Course course = parseCourse(splitInfo[courseIndex]);

		// Searches for existing references
		if (course instanceof Lab) {
			for (Lab l : labs) {
				if (l.equals((Lab) course)) {
					course = l;
				}
			}

			boolean slotFound = false;
			for (TimeSlot s : labSlots) {
				if (s.equals(day, time)) {
					slot = s;
					slotFound = true;
				}
			}

			// If an existing matching slot is not found null is returned as per Jorg's instructions
			if (!slotFound) {
				return null;
			}

		} else {
			for (Course c2 : courses) {
				if (c2.equals(course)) {
					course = c2;
				}
			}

			boolean slotFound = false;
			for (TimeSlot s : courseSlots) {
				if (s.equals(day, time)) {
					slot = s;
					slotFound = true;
				}
			}

			if (!slotFound) {
				return null;
			}
		}

		int num = Integer.parseInt(splitInfo[numIndex].replace(" ", ""));

		return (new Triple<TimeSlot, Course, Integer>(slot, course, num));
	}
	
	
	private void parseCommandLineArgs(String[] args)
	{
		if(args.length == 0)
		{
			try
			{
				this.promptUserForValues();
			}
			catch(Exception e){}
		}
		else if(args.length == 1)//Only provided filename, set soft constraints to defaults
		{
			if(parseFilename(args[0]))
			{
				this.set_fileName(args[0]);
			}
			else{
				System.out.println(_USAGE);
				System.exit(0);
			}
		}
		else if(args.length == 5)//gave us everything 
		{
			if(parseFilename(args[0]))
			{
				this.set_fileName(args[0]);
			}
			else{
				System.out.println(_USAGE);
				System.exit(0);
			}
		
			if(parseSoftConstraint(args[1]))
			{
				this.set_minField(Double.parseDouble(args[1]));
			}
			else{
				System.out.println(_USAGE);
				System.exit(0);
			}
			
			if(parseSoftConstraint(args[1]))
			{
				this.set_pref(Double.parseDouble(args[1]));
			}
			else
			{
				System.out.println(_USAGE);
				System.exit(0);
			}
			if(parseSoftConstraint(args[3]))
			{
				this.set_pair(Double.parseDouble(args[3]));
			}
			else
			{
				System.out.println(_USAGE);
				System.exit(0);
			}
			if(parseSoftConstraint(args[4]))
			{
				this.set_secdiff(Double.parseDouble(args[4]));
			}
			else
			{
				System.out.println(_USAGE);
				System.exit(0);
			}
		}
		else
		{
				System.out.println(_USAGE);
				System.exit(0);
		}
			
		//"filename minfield pref pair secdiff";
	}
	
	/**
	 * prompts user for input
	 */
	public void promptUserForValues() 
	{
		//Handle filename input and check if file exists
		System.out.println("Enter file name: ");
		String filename = scanner.nextLine();
		while(!parseFilename(filename))
		{
			System.out.println("File doesn't exist.");
			System.out.println("Enter file name: ");
			filename = scanner.nextLine();
		}
		this.set_fileName(filename);
		set_fileObj(new File(this.get_fileName()));
		
		
		//Soft constraints
		String input;
		System.out.print("\nEnter minfield weight value: ");
		String softConstr = rePromptuser("minfield");
		this._minField = Double.parseDouble(softConstr);
	
		System.out.print("\nEnter pref weight value: ");
		softConstr = rePromptuser("pref");
		this._pref = Double.parseDouble(softConstr);
		
		System.out.print("\nEnter pair weight value: ");
		softConstr = rePromptuser("pair");
		this._pair = Double.parseDouble(softConstr);
		
		System.out.print("\nEnter secdiff weight value: ");
		softConstr = rePromptuser("secdiff");
		this._secdiff = Double.parseDouble(softConstr);
		
		System.out.print("\nEnter penalty coursemin constraint value: ");
		softConstr = rePromptuser("penalty coursemin");
		this._penCoursemin = Double.parseDouble(softConstr);
		
		System.out.print("\nEnter penalty labsmin constraint value: ");
		softConstr = rePromptuser("penalty labsmin");
		this._penLabsmin = Double.parseDouble(softConstr);
		
		System.out.print("\nEnter penalty notpaired constraint value: ");
		softConstr = rePromptuser("penalty notpaired");
		this._penNotPaired= Double.parseDouble(softConstr);
		
		System.out.print("\nEnter penalty section constraint value: ");
		softConstr = rePromptuser("penalty section");
		this._penSection = Double.parseDouble(softConstr);
		
		System.out.println();
	}
	
	
	/**
	 * This method is here only to reduce repetition of code in promptUserForValues() method.
	 * If user enters value that isn't a double that we ask them to reenter it.
	 * @param constraint
	 * @return string representation of user's input
	 */
	private String rePromptuser(String constraint)
	{
		String input = scanner.nextLine();
		while(!(input.isEmpty() || this.parseSoftConstraint(input)))
		{
			System.out.println("Enter valid(empty space or a double) "+ constraint + " constraint value: ");
			System.out.println("Enter " + constraint + " constraint value: ");
			input = scanner.nextLine();
		}
		return input;
	}
	
	
	/**
	 * checks if srting provided can at all be converted into double.
	 * @param constraint
	 * @return true or false
	 */
	private boolean parseSoftConstraint(String constraint)
	{
		try
		{
			Double d = Double.parseDouble(constraint);
			return true;
		}
		catch(Exception e) // can't be converted into double
		{
			return false;
		}
	}
	
	
	/**
	 * check if given file exists at all
	 * @param fileName
	 * @return
	 */
	private boolean parseFilename(String fileName)
	{
		File f = new File(fileName);
		if(f.exists() && f.isFile())
			return true;
		else
			return false;
	}
	
	/**
	 * GETERS AND SETERS
	 * 
	 * */
	public ArrayList<TimeSlot> getCourseSlots() {
		return courseSlots;
	}

	public void setCourseSlots(ArrayList<TimeSlot> courseSlots) {
		this.courseSlots = courseSlots;
	}

	public ArrayList<TimeSlot> getLabSlots() {
		return labSlots;
	}

	public void setLabSlots(ArrayList<TimeSlot> labSlots) {
		this.labSlots = labSlots;
	}

	public ArrayList<Course> getCourses() {
		return courses;
	}

	public void setCourses(ArrayList<Course> courses) {
		this.courses = courses;
	}

	public ArrayList<Lab> getLabs() {
		return labs;
	}

	public void setLabs(ArrayList<Lab> labs) {
		this.labs = labs;
	}

	public ArrayList<Pair<Course, Course>> getNotCompatible() {
		return notCompatible;
	}

	public void setNotCompatible(ArrayList<Pair<Course, Course>> notCompatible) {
		this.notCompatible = notCompatible;
	}

	public ArrayList<Pair<Course, TimeSlot>> getUnwanted() {
		return unwanted;
	}

	public void setUnwanted(ArrayList<Pair<Course, TimeSlot>> unwanted) {
		this.unwanted = unwanted;
	}

	public ArrayList<Triple<TimeSlot, Course, Integer>> getPreferences() {
		return preferences;
	}

	public void setPreferences(
			ArrayList<Triple<TimeSlot, Course, Integer>> preferences) {
		this.preferences = preferences;
	}

	public ArrayList<Pair<Course, Course>> getPairs() {
		return pairs;
	}

	public void setPairs(ArrayList<Pair<Course, Course>> pairs) {
		this.pairs = pairs;
	}

	public ArrayList<Pair<Course, TimeSlot>> getPartials() {
		return partials;
	}

	public void setPartials(ArrayList<Pair<Course, TimeSlot>> partials) {
		this.partials = partials;
	}

	public BufferedReader get_reader() {
		return _reader;
	}

	public void set_reader(BufferedReader _reader) {
		this._reader = _reader;
	}

	public String get_fileName() {
		return _fileName;
	}

	public void set_fileName(String _fileName) {
		this._fileName = _fileName;
	}
	
	public File get_fileObj()
	{
		return _fileObj;
	}
	
	public void set_fileObj(File file)
	{
		_fileObj = file;
	}
	
	public double get_minField() {
		return _minField;
	}

	public void set_minField(double _minField) {
		this._minField = _minField;
	}

	public double get_pref() {
		return _pref;
	}

	public void set_pref(double _pref) {
		this._pref = _pref;
	}

	public double get_pair() {
		return _pair;
	}

	public void set_pair(double _pair) {
		this._pair = _pair;
	}

	public double get_secdiff() {
		return _secdiff;
	}

	public void set_secdiff(double _secdiff) {
		this._secdiff = _secdiff;
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
