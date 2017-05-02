package uk.co.citybank.elevator.validator;

import uk.co.citybank.elevator.exception.ValidationException;
import uk.co.citybank.elevator.model.ElevatorDirection;
import uk.co.citybank.elevator.model.UserRequest;
import uk.co.citybank.elevator.utility.Constants;

/**
 * Implementation of @UserRequestValidator
 * @author anuragtripathi
 *
 */
public class UserRequestValidatorImpl implements UserRequestValidator {

	@Override
	public void validateUserRequest(UserRequest request) throws ValidationException {
		// The Current floor and Destination Floor should be between Lowest Floor and Highest Floor
		if (request.getCurrentFloor() < Constants.LOWEST_FLOOR || request.getCurrentFloor() > Constants.HIGHEST_FLOORS
				|| request.getFloorToGo() > Constants.HIGHEST_FLOORS
				|| request.getFloorToGo() < Constants.LOWEST_FLOOR) {
			throw new ValidationException(Constants.INVALID_REQUEST);
		}

		// If User is on the lowest floor, he/she can't select DOWN
		if (request.getCurrentFloor() == Constants.LOWEST_FLOOR
				&& ElevatorDirection.DOWN == request.getDirectionToGo()) {
			throw new ValidationException(Constants.INVALID_REQUEST);
		}

		// If User is on the highest floor, he/she can't select UP
		if (request.getCurrentFloor() == Constants.HIGHEST_FLOORS
				&& ElevatorDirection.UP == request.getDirectionToGo()) {
			throw new ValidationException(Constants.INVALID_REQUEST);
		}

		if (ElevatorDirection.UP == request.getDirectionToGo()
				&& (request.getCurrentFloor() >= request.getFloorToGo())) {
			throw new ValidationException(Constants.INVALID_REQUEST);
		}

		if (ElevatorDirection.DOWN == request.getDirectionToGo()
				&& (request.getCurrentFloor() <= request.getFloorToGo())) {
			throw new ValidationException(Constants.INVALID_REQUEST);
		}
	}
}
