package uk.co.citybank.elevator.validator;

import org.junit.Test;

import org.junit.Assert;
import uk.co.citybank.elevator.exception.ValidationException;
import uk.co.citybank.elevator.model.ElevatorDirection;
import uk.co.citybank.elevator.model.UserRequest;
import uk.co.citybank.elevator.utility.Constants;

/**
 * JUNIT Class having UNIT Tests to validate the functionality of @UserRequestValidator
 * @author anuragtripathi
 *
 */
public class UserRequestValidatorTest {
	
  final UserRequestValidator validator = new UserRequestValidatorImpl();
  
  @Test
  public void validatorShouldThrowExceptionCurrentFloorIsGreaterThanHIghestFloor() {
	  try {
		validator.validateUserRequest(createUserRequest(11, 1, ElevatorDirection.UP));
	} catch (ValidationException e) {
		Assert.assertEquals(Constants.INVALID_REQUEST, e.getMessage());
	}
  }
  
  @Test
  public void validatorShouldThrowExceptionCurrentFloorIsLowerThanLowestFloor() {
	  try {
		validator.validateUserRequest(createUserRequest(-1, -8, ElevatorDirection.DOWN));
	} catch (ValidationException e) {
		Assert.assertEquals(Constants.INVALID_REQUEST, e.getMessage());
	}
  }
  
  @Test
  public void validatorShouldThrowExcIfDestFloorIsGreaterThanHIghestFloor() {
	  try {
		validator.validateUserRequest(createUserRequest(10, -1, ElevatorDirection.UP));
	} catch (ValidationException e) {
		Assert.assertEquals(Constants.INVALID_REQUEST, e.getMessage());
	}
  }
  
  @Test
  public void validatorShouldThrowExcIfDestFloorIsLowerThanHIghestFloor() {
	  try {
		validator.validateUserRequest(createUserRequest(11, -1, ElevatorDirection.DOWN));
	} catch (ValidationException e) {
		Assert.assertEquals(Constants.INVALID_REQUEST, e.getMessage());
	}
  }
  
  
  @Test
  public void validatorShouldThrowExcIfCurrentFloorIsGreaterThanDestFllorForGoingUp() {
	  try {
		validator.validateUserRequest(createUserRequest(11, 8, ElevatorDirection.UP));
	} catch (ValidationException e) {
		Assert.assertEquals(Constants.INVALID_REQUEST, e.getMessage());
	}
  }
  
  @Test
  public void validatorShouldThrowExcIfCurrentFloorIsLessThanDestFllorForGoingDown() {
	  try {
		validator.validateUserRequest(createUserRequest(11, 12, ElevatorDirection.DOWN));
	} catch (ValidationException e) {
		Assert.assertEquals(Constants.INVALID_REQUEST, e.getMessage());
	}
  }
  
  private UserRequest createUserRequest(int currentFloor,int floorToGo,ElevatorDirection directionToGo) {
	 return new UserRequest(currentFloor, floorToGo, directionToGo);
  }
  
}
