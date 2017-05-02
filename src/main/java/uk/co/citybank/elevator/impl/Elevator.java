package uk.co.citybank.elevator.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.citybank.elevator.model.ElevatorDirection;
import uk.co.citybank.elevator.model.ElevatorStatus;
import uk.co.citybank.elevator.queue.UserRequestQueue;
import uk.co.citybank.elevator.utility.Constants;

/**
 * Class representing A Running Elevator.
 * Elevator will try to find the first Request from UserRequestQueue.
 * Once its got an request from the Queue , it will start working on that request.
 * When moving between floors , if it finds any user waiting at the current floor willing to go in the same direction as of elevator
 * it will allow to get the user in and will continue in the same direction. Once its done , it will try again to find a new request from queue
 * 
 * @author anuragtripathi
 *
 */
public class Elevator implements Runnable {
	
	final static Logger logger = LoggerFactory.getLogger(Elevator.class);
	
	// Every Elevator will have its own copy of status 
	private final ThreadLocal<ElevatorStatus> status;
	
	// This variable will indicated whether threads has been requested to shut down
    private volatile boolean working = true;
	
    public void stop() {
    	working = false;
    }
	
	public Elevator(final String name,final UserRequestQueue queue) {
		/**
		 * At Start, Elevator should be on lowest floor and will go to UP direction only.
		 */
        status = new ThreadLocal<ElevatorStatus>() {
				@Override
		        protected ElevatorStatus initialValue() {
		            return new ElevatorStatus(name,Constants.LOWEST_FLOOR,ElevatorDirection.UP, queue);
		        }
		};
	}

	/**
	 * Starts Running An Elevator Thread
	 */
	public void run() {
		System.out.println("Elevator Started Running");
		
		while(working || status.get().areUsersStillInElevator()) {
			
		 try {	
			
			// if Elevator is going up, then keep going UP until it has pending requests(either pick or drop) to go up
			if(status.get().getCurrentFloor() < Constants.HIGHEST_FLOORS 
					&& status.get().getDirection() == ElevatorDirection.UP
					&& status.get().isElevatorNeedsToGoUp()) {
				
				status.get().moveOneFloorUp();
				
				// Check If Elevator needs to be stopped at the current floor
				if(status.get().isElevatorNeedsToStopAtCurrentFloor()) {
					status.get().waitOnTheCurrentFloor(true);
				}
				
			}
			// if Elevator is going down, then keep going down until it has pending requests(either pick or drop) to go down
			else if(status.get().getCurrentFloor() > Constants.LOWEST_FLOOR 
					&& status.get().getDirection() == ElevatorDirection.DOWN
					&& status.get().isElevatorNeedsToGoDown()) {
				
				status.get().moveOneFloorDown();
				
				// Check If Elevator needs to be stopped at the current floor
				if(status.get().isElevatorNeedsToStopAtCurrentFloor()) {
					status.get().waitOnTheCurrentFloor(working);
				}
			} else {
				// If the Elevator doesn't have any thing to work on, then try to look into the queue
				// The Thread will be blocked if there is no request available in the queue 
				// The thread will be resumed once anything becomes available in the queue
				status.get().pickRequestFromQueue();
			}
		 } catch (InterruptedException e) {
			 log(" Request to close down the Elevator Received");
			 status.remove();
			 working = false;
		} 
	  }
	  log(" Closing Down");
	}
	
	private void log(String message) {
		logger.debug(status.get().getElevatorName()+message);
	}
}
