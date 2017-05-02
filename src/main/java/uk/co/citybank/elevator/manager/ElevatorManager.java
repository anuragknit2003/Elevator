package uk.co.citybank.elevator.manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.citybank.elevator.exception.ValidationException;
import uk.co.citybank.elevator.impl.Elevator;
import uk.co.citybank.elevator.model.ElevatorDirection;
import uk.co.citybank.elevator.model.UserRequest;
import uk.co.citybank.elevator.queue.UserRequestQueue;
import uk.co.citybank.elevator.validator.UserRequestValidator;
import uk.co.citybank.elevator.validator.UserRequestValidatorImpl;

/**
 * Singleton Class to Take User Request and Start & Stop the Elevators
 * @author anuragtripathi
 *
 */
public class ElevatorManager {
   
	final static Logger logger = LoggerFactory.getLogger(ElevatorManager.class);

	private static ElevatorManager manager;
	private ExecutorService executor;
	private UserRequestQueue queue;
	private final UserRequestValidator validator;
	
	private ElevatorManager() {
		validator = new UserRequestValidatorImpl();
	}
	
	/**
	 * Returns a instance of ElevatorManager
	 * @return UserRequestQueue
	 */
	public static ElevatorManager getElevatorManager() {
	    if (manager == null) {
	      synchronized(ElevatorManager.class) {
	        if (manager == null) 
	        	manager = new ElevatorManager();
	      }
	    }
	    return manager;
	}
	
	/**
	 * Starts the elevators
	 * @param number Number of Elevators needs to be started
	 */
	public void startElevators(int number) {
		if(executor != null) {
			logger.debug("EleVators are already Running");
			return;
		}
		
		executor = Executors.newFixedThreadPool(number);
		queue = UserRequestQueue.getUserRequestQueue();
		
		 for (int i = 1; i <= number; i++) {
		     final Runnable elevator = new Elevator("Elevator-"+i, queue);
		     executor.execute(elevator);
		 }
	 }
	
	/**
	 * Shut Down all the Running Elevators
	 */
	public void shutDownElevators() {
		if(!executor.isShutdown()) {
			executor.shutdownNow();
		}
	}
	
	/**
	 * Adds User Request in the Queue
	 * @param currentFloor User's Current Floor
	 * @param floorToGo The Floor User wants to go
	 * @param direction Direction
	 * @throws ValidationException Validation Exception if the request is invalid
	 */
	public void raiseUserRequest(int currentFloor, int floorToGo, ElevatorDirection direction) throws ValidationException {
		final UserRequest request = new UserRequest(currentFloor,floorToGo, direction);
		validator.validateUserRequest(request);
	    queue.addUserRequest(request);
	}
}
