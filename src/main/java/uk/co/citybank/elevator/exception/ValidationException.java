package uk.co.citybank.elevator.exception;

/**
 * Custom Exception If User Request is not valid
 * @author anuragtripathi
 *
 */
public class ValidationException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	String message;
	
	/**
	 * Constructor
	 * @param message Error Message
	 */
	public ValidationException(String message) {
	       super(message);
	}
}
