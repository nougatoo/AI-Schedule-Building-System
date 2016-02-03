package types;

import java.io.Serializable;

/**
 * A class that handles the courses
 * @author Shayne
 *
 */
public class Course implements Serializable{

	protected TimeSlot slot;
	protected int number;
	protected String department;
	protected int section;
	protected int index;
	
	public Course(TimeSlot s, int n, String d, int sec) {
		if (s.getIsLab()) {
			System.out.println("Lab slot tried to be assigned to course " +
		d + n + " section " + sec);
			throw (new IllegalArgumentException());
		}
		slot = s;
		number = n;
		department = d;
		section = sec;
	}
	
	public Course(int n, String d, int sec) {
		slot = null;
		number = n;
		department = d;
		section = sec;
	}
	
	public TimeSlot getSlot() {
		return slot;
	}
	
	public int getNumber() {
		return number;
	}
	
	public String getDepartment() {
		return department;
	}
	
	public int getSection() {
		return section;
	}
	
	public void setSlot(TimeSlot s) {
		if(s == null){}
		else if (s.getIsLab()) {
			System.out.println("Lab slot tried to be assigned to course " +
					department + number + " section " + section);
			throw (new IllegalArgumentException());
		}
		
		slot = s;
	}
	
	public int getIndex()
	{
		return index;
	}
	public void setIndex(int newIndex)
	{
		index = newIndex;
	}
	/**
	 * Determines if one course is equal to another.
	 * Does not take time slot into account
	 * @param c
	 * @return
	 */
	public boolean equals(Course c) {
		if (number == c.getNumber() && 
				department.equalsIgnoreCase(c.getDepartment()) && 
				section == c.getSection()) {
			return true;
		} else {
			return false;
		}
	}
	
	public Course clone()
	{
		Course c = new Course(number, department, section);	
		c.setIndex(this.index);
		
		return c;
	}

	public Course clone2()
	{
		Course tempC;
		if(slot != null)
		{
			tempC = new Course(slot, number, department, section);
			tempC.setIndex(this.index);
			return tempC;		
		}
		else
		{
			tempC = new Course(number, department, section);
			tempC.setIndex(this.index);
			return tempC;
		}

	}
}
