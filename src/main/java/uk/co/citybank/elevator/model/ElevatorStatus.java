package uk.co.citybank.elevator.model;

import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.citybank.elevator.queue.UserRequestQueue;
import uk.co.citybank.elevator.utility.Constants;

/**
 * This class holds the state of each Elevator Thread. A thread will have its own copy
 * @author anuragtripathi
 *
 */
public class ElevatorStatus {
	
	final static Logger logger = LoggerFactory.getLogger(ElevatorStatus.class);
	
	private final String elevatorName;
	
    private int currentFloor;
    private ElevatorDirection direction;
    private final UserRequestQueue queue;
   
    /**
     * Holds the Unique Ordered List of Floors where Elevator needs to stop for Pick and Drop.
     * For Example, If Elevator picks an user which is currently at floor 2 and wants 
     * to go at floor 5 , then it needs to stop at both floors. Will need to stop at floor 2 to pick the user and
     * at floor 5 to drop the user. 
     */
    private SortedSet<Integer> floorsToStop = new TreeSet<Integer>();
    
    /**
     * Constructor to Initialise Elevator Thread 
     * @param elevatorName Elevator Name
     * @param currentFloor Current Floor of Elevator
     * @param direction The direction of Elevator
     * @param queue The User Request Queue
     */
	public ElevatorStatus(String elevatorName,int currentFloor, ElevatorDirection direction,final UserRequestQueue queue) {
		super();
		this.elevatorName=elevatorName;
		this.currentFloor = currentFloor;
		this.direction = direction;
		this.queue = queue;
	}
	
	/**
	 * Returns the current floor
	 * @return Current Floor
	 */
	public int getCurrentFloor() {
		return currentFloor;
	}

	/**
	 * Returns the Direction Elevator is going
	 * @return UP/DOWN
	 */
	public ElevatorDirection getDirection() {
		return direction;
	}
	
	/**
	 * Returns Elevator Name
	 * @return Elevator
	 */
	public String getElevatorName() {
		return elevatorName;
	}
	
	/**
	 * Moves One Floor Up
	 * @throws InterruptedException Exception Thrown if this thread needs to interrupted
	 */
	public void moveOneFloorUp() throws InterruptedException   {
		if(direction != ElevatorDirection.UP) {
			this.direction = ElevatorDirection.UP;
		}
		if(currentFloor<Constants.HIGHEST_FLOORS) {
			// Remove the entry from pick and drop set if exists
		   floorsToStop.remove(currentFloor++);
		    
		   Thread.sleep(Constants.TIME_MOVE_ONE_FLOOR*1000);
		   logMessage("Moved from Floor "+ (currentFloor-1) +" to Floor "+currentFloor + " - Floors To Stop :" +floorsToStop);
		}
	}
	
	/**
	 * Waits on the current floor to allow to get in or out and pick the request if anything is available
	 * @param canPickNewRequests Boolean to indicate of Elevator can pick the new requests from queue or not
	 * @throws InterruptedException Exception Thrown if this thread needs to interrupted
	 */
	public void waitOnTheCurrentFloor(boolean canPickNewRequests) throws InterruptedException  {
		logMessage("Opening at "+ currentFloor +" : Floors To Stop "+floorsToStop);
		
		// If thread has not been requested to shut down , Elevator can take new User
		if(canPickNewRequests) {
			/**
			 * If Elevator is going down and if there are some users waiting at the current floor and want to go down, then let them in
			 * and If Elevator is going UP and if there are some users waiting at the current floor and want to go UP, then let them in
			 */
			
			if(direction == ElevatorDirection.UP) {
				queue.pickUsersWantToGoUpWaitingAtCurrentFloor(currentFloor).forEach(p->addRequestToWorkOn(p));
			}  else {
				queue.pickUsersWantToGoDownWaitingAtCurrentFloor(currentFloor).forEach(p->addRequestToWorkOn(p));
			}
		}
		
		// Remove the Current Floor From the Elevator Set which is used to find if it needs to stop at particular floors
		floorsToStop.remove(currentFloor);
		
		// Once Elevator Stops at any floor, Sleeping the Current Elevator to show User In And Out Events
		Thread.sleep(Constants.TIME_USER_PICKDROP*1000);
		
		logMessage("Closing at "+ currentFloor+" : Floors To Stop "+floorsToStop);
	}
	
	/**
	 * Moves one Floor Down
	 * @throws InterruptedException Exception Thrown if this thread needs to interrupted
	 */
	public void moveOneFloorDown() throws InterruptedException  {
		if(direction != ElevatorDirection.DOWN) {
			this.direction = ElevatorDirection.DOWN;
		}
		if(currentFloor>Constants.LOWEST_FLOOR) {
			// Remove the entry from pick and drop set if exists
			floorsToStop.remove(currentFloor--);
			// Sleep the thread for certain time occurring in one floor move
			Thread.sleep(Constants.TIME_MOVE_ONE_FLOOR*1000);
		}
		logMessage("Moved from Floor "+ (currentFloor+1) +" to Floor "+currentFloor +" - Floors To Stop :" +floorsToStop);
	}
	
	/**
	 * Determines if the an elevator needs to stop at current floor
	 * If There is an entry for the current floor in pick and drop 
	 * or there is any request pending from the current floor which needs to be picked up then it should stop
	 * @return true/false
	 */
	public boolean isElevatorNeedsToStopAtCurrentFloor() {
		boolean isElevatorHasRequest = false;
		if(direction == ElevatorDirection.UP) {
			isElevatorHasRequest = floorsToStop.subSet(currentFloor, currentFloor+1).size() >= 1;
		} else if(direction == ElevatorDirection.DOWN) {
			isElevatorHasRequest = floorsToStop.subSet(currentFloor, currentFloor+1).size() >= 1;;
		}
		
		boolean isAnyRequestCanbePickedFromCurrentFloor =false;
		if(direction == ElevatorDirection.UP) {
			isAnyRequestCanbePickedFromCurrentFloor = queue.isUserWillingToGoUpWaitingAtCurrentFloor(currentFloor);
		} else {
			isAnyRequestCanbePickedFromCurrentFloor = queue.isRequestToGoDownPendingFromCurrentFloor(currentFloor);
		}
		return isElevatorHasRequest || isAnyRequestCanbePickedFromCurrentFloor;
	}
	
	/**
	 * Finds Out if an elevator needs to go Up
	 * Check if it has any entries in Set greater than current floor
	 * @return
	 */
	public boolean isElevatorNeedsToGoUp() {
		return floorsToStop.subSet(currentFloor, Constants.HIGHEST_FLOORS+1).size() > 0;
	}
	
	/**
	 * Finds Out if an elevator needs to go DOWN
	 * Check if it has any entries in Set lower than current floor
	 * @return
	 */
	public boolean isElevatorNeedsToGoDown() {
		return floorsToStop.subSet(Constants.LOWEST_FLOOR ,currentFloor).size() > 0;
	}
	
	/**
	 * Adds a request to Elevator to work on
	 * @param userRequest
	 */
	public void addRequestToWorkOn(final UserRequest userRequest) {
		floorsToStop.add(userRequest.getCurrentFloor());
		floorsToStop.add(userRequest.getFloorToGo());
		logMessage(" Picked Request "+userRequest);
	}
	
	/**
	 * Picks a User Request from Queue and adds it to Set to Elevator to work on
	 * This method will be invoked only when Elevator doesn't have any user request to work on
	 * @throws InterruptedException 
	 */
	public void pickRequestFromQueue() throws InterruptedException {
		final UserRequest request = queue.pickRequest(currentFloor, direction);
		logMessage(" Picked Request "+request +" Current Floor "+currentFloor);
		
		// Needs to go first at the user's current floor
		if(currentFloor>request.getCurrentFloor()) {
			while(currentFloor != request.getCurrentFloor()) {
				moveOneFloorDown();
			}
		} else {
			while(currentFloor != request.getCurrentFloor()) {
				moveOneFloorUp();
			}
		}
		
		if(currentFloor>request.getFloorToGo()) {
			direction = ElevatorDirection.DOWN;
		} else {
			direction = ElevatorDirection.UP;
		}
		
		floorsToStop.add(request.getFloorToGo());
		if(isElevatorNeedsToStopAtCurrentFloor()) {
			waitOnTheCurrentFloor(true);
		}
	}
	
	/**
	 * Determines Elevator is still busy in serving allocated user requests
	 * @return true/false
	 */
	public boolean areUsersStillInElevator() {
		return floorsToStop.size()>0;
	}
	
	private void logMessage(String message) {
		logger.debug(elevatorName + "- "+message);
	}
	
}
