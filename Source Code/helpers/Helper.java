package helpers;

import java.text.NumberFormat;
import java.text.ParsePosition;

public abstract class Helper {

	
	/**
	 * Determines if a string is numeric
	 * Taken from http://stackoverflow.com/questions/1102891/how-to-check-if-a-string-is-a-numeric-type-in-java
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str)
	{
	  NumberFormat formatter = NumberFormat.getInstance();
	  ParsePosition pos = new ParsePosition(0);
	  formatter.parse(str, pos);
	  return str.length() == pos.getIndex();
	}
	
}
