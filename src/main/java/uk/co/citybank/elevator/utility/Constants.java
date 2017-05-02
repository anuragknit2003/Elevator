package uk.co.citybank.elevator.utility;


/**
 * Constants File
 * @author anuragtripathi
 *
 */
public final class Constants {
	
	/**
	 * No Object Required Since It is an Utility Class therefore making the constructor private
	 */
	private Constants() {
	}
	
	public final static int TIME_MOVE_ONE_FLOOR = 3;
	public final static int TIME_USER_PICKDROP = 2;
	public final static int HIGHEST_FLOORS = 10;
	public final static int LOWEST_FLOOR = 0;
	
	// Validation Error Message
	public final static String INVALID_REQUEST = "Invalid Current Floor/ Destination Floor Selection";

}
