package types;

import java.util.ArrayList;

public class Schedule {
	
	private ArrayList<Course> courses;
	private ArrayList<Lab> labs;
	private double evalRating;
	private int depth;
	private boolean coursesisfull = false;
	private boolean labsisfull = false;

	public Schedule(ArrayList<Course> c, ArrayList<Lab> l )
	{
		setCourses(c);
		setLabs(l);
		setEvalRating(-1);
		setDepth(0);
	}

	public ArrayList<Lab> getLabs() {
		return labs;
	}

	public void setLabs(ArrayList<Lab> labs) {
		this.labs = labs;
	}
	public ArrayList<Course> getCourses() {
		return courses;
	}
	public boolean checkSlotAvail(int index){
		if (index >= this.size())
			return false;
		if (index > courses.size()){
			if ( labs.get(index-courses.size() ).getSlot() == null ){
				return true;
			}else{
				return false;
			}
		}else{
			if (labs.get(index).getSlot() == null){
				return true;
			}else{
				return false;
				}
		}
	}
	public void setCourses(ArrayList<Course> courses) {
		this.courses = courses;
	}
	public double getEvalRating() {
		return evalRating;
	}
	public int size(){
		return courses.size() + labs.size();
	}
	public void setEvalRating(double evalRating) {
		this.evalRating = evalRating;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	public Schedule clone()
	{
		Schedule newSched;
		Course tempCourse;
		Lab tempLab;
		
		ArrayList<Course> newCourseList = new ArrayList<Course>();
		ArrayList<Lab> newLabList = new ArrayList<Lab>();
		
		
		for(int i = 0;i<courses.size();i++)
		{
			tempCourse = courses.get(i).clone2();
			newCourseList.add(tempCourse);
		}
		
		for(int i = 0;i<labs.size();i++)
		{
			tempLab = labs.get(i).clone2();
			newLabList.add(tempLab);
		}
		
		newSched = new Schedule(newCourseList, newLabList);
		newSched.setDepth(depth);
		newSched.setEvalRating(this.evalRating);
		return newSched;
	}

	public boolean isLabsisfull() {
		return labsisfull;
	}

	public void setLabsisfull(boolean labsisfull) {
		this.labsisfull = labsisfull;
	}

	public boolean isCoursesisfull() {
		return coursesisfull;
	}

	public void setCoursesisfull(boolean coursesisfull) {
		this.coursesisfull = coursesisfull;
	}
}
