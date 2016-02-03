package types;

/**
 * An enumerated type to keep track of the day(s) of a timeslot
 * @author Shayne
 *
 */

public enum ScheduleDay {
		MO, TU, FR;
		
		public static ScheduleDay fromString(String s) {
			if (s.equalsIgnoreCase("MO")) {
				return MO;
			} else if (s.equalsIgnoreCase("TU")) {
				return TU;
			} else if (s.equalsIgnoreCase("FR")) {
				return FR;
			} else {
				return null;
			}
		}
}
