package types;

/**
 * This class handles timeslots for both courses and labs
 * @author Shayne
 *
 */
public class TimeSlot {

	private ScheduleDay day;
	private int startTime;
	private int maxNum;
	private int minNum;
	private boolean isLab;
	
	public TimeSlot(ScheduleDay d, int s, int max, int min, boolean lab) {
		
		if (!lab && d == ScheduleDay.FR) {
			System.out.println("Timeslot for lecture given a day of Friday");
			throw (new IllegalArgumentException());
		}
		
		day = d;
		startTime = s;
		maxNum = max;
		minNum = min;
		isLab = lab;
	}
	
	public TimeSlot(ScheduleDay d, int s, boolean b) {
		day = d;
		startTime = s;
		isLab = b;
	}
	
	public ScheduleDay getDay() {
		return day;
	}
	
	public int getStartTime() {
		return startTime;
	}
	
	public int getMaxNum() {
		return maxNum;
	}
	
	public int getMinNum() {
		return minNum;
	}
	
	public boolean getIsLab() {
		return isLab;
	}
	
	public boolean equals(ScheduleDay d, int t) {
		if (d == day && t == startTime) {
			return true;
		} else {
			return false;
		}
	}
	
}
