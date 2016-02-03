package types;


/**
 * This class is for labs
 * Since labs have many similarities to courses, lab extends course
 * In this case, section is the section of the lab, and lectureNum
 * is the section of the lecture, if there is one
 * @author Shayne
 *
 */
public class Lab extends Course {

	private int lectureNum = -1;
	
	public Lab(TimeSlot s, int n, String d, int sec, int l) {
		super(n, d, sec);
		if (!s.getIsLab()) {
			System.out.println("Course slot tried to be assigned to course " +
		d + n + " section " + sec);
			throw (new IllegalArgumentException());
		}
		slot = s;
		lectureNum = l;
	}
	
	public Lab(TimeSlot s, int n, String d, int sec) {
		super(n, d, sec);
		if (!s.getIsLab()) {
			System.out.println("Course slot tried to be assigned to course " +
		d + n + " section " + sec);
			throw (new IllegalArgumentException());
		}
		slot = s;
	}
	
	public Lab(int n, String d, int sec, int l) {
		super(n, d, sec);
		lectureNum = l;
	}
	
	public Lab(int n, String d, int sec) {
		super(n, d, sec);
	}
	
	public int getLectureNum() {
		return lectureNum;
	}
	
	public void setSlot(TimeSlot s) {
		if(s == null){}
		else if (!s.getIsLab()) {
			System.out.println("Course slot tried to be assigned to lab " +
					department + number + " section " + section);
			throw (new IllegalArgumentException());
		}
		
		slot = s;
	}
	
	/**
	 * Determines if one lab is equal to another.
	 * Does not take time slot into account
	 * @param l
	 * @return
	 */
	public boolean equals(Lab l) {
		if (number == l.getNumber() && 
				department.equalsIgnoreCase(l.getDepartment()) && 
				section == l.getSection() && 
				lectureNum == l.getLectureNum()) {
			return true;
		} else {
			return false;
		}
	}

	public Lab clone()
	{
		Lab l = new Lab(number, department, section, lectureNum);
		l.setIndex(this.index);
		return l;
	}
	public Lab clone2()
	{
		Lab tempL;
		if(slot != null)
		{
			tempL = new Lab(slot, number, department, section, lectureNum);
			tempL.setIndex(this.index);
			return tempL;		
		}
		else
		{
			tempL =  new Lab(number, department, section, lectureNum);
			tempL.setIndex(this.index);
			return tempL;
		}
	}

}
