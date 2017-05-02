package uk.co.citybank.elevator.validator;

import uk.co.citybank.elevator.exception.ValidationException;
import uk.co.citybank.elevator.model.UserRequest;

/**
 * Validator to validate the User Request
 * @author anuragtripathi
 *
 */
public interface UserRequestValidator {
	
	/**
	 * Validates if the User Request is valid and can be proceesed
	 * @param request User Request
	 * @throws ValidationException Validation Exception
	 */
	public void validateUserRequest(UserRequest request) throws ValidationException ;
}
