package uk.co.citybank.elevator.queue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.citybank.elevator.model.ElevatorDirection;
import uk.co.citybank.elevator.model.UserRequest;
import uk.co.citybank.elevator.utility.Constants;

/**
 * A Singleton Class which holds all the User Requests
 * All the running Elevator threads will monitor this queue to find out unassigned user requests.
 * Once a request is assigned to any Elevator thread, it will deleted from this queue.
 * @author anuragtripathi
 *
 */
public class UserRequestQueue {
	
	private static UserRequestQueue queue = null;
	final static Logger logger = LoggerFactory.getLogger(UserRequestQueue.class);
	
	// Comparator to sort the elements in TreeSet
	private Comparator<UserRequest> byFloor = Comparator.comparing((UserRequest p)->p.getCurrentFloor()).thenComparing(p->p.getFloorToGo());
	
	/**
	 * The two sorted set one to hold the requests to go up and one to go down
	 */
	private TreeSet<UserRequest> requestTreeForGoingUp; 
	private TreeSet<UserRequest> requestTreeForGoingDown; 
	
	/** Main lock guarding all access */
    final ReentrantLock lock;

    // The threads needs to be blocked if there is nothing available in the queue
    private final Condition queueNotEmpty;
	
	private UserRequestQueue() {
		requestTreeForGoingUp = new TreeSet<UserRequest>(byFloor);
		requestTreeForGoingDown = new TreeSet<UserRequest>(byFloor);
		
		lock = new ReentrantLock(true);
		queueNotEmpty = lock.newCondition();
	}
	
	/**
	 * Returns a instance of UserRequestQueue
	 * @return UserRequestQueue
	 */
	public static UserRequestQueue getUserRequestQueue() {
	    if (queue == null) {
	      synchronized(UserRequestQueue.class) {
	        if (queue == null) 
	        	queue = new UserRequestQueue();
	      }
	    }
	    return queue;
	}
	
	/**
	 * Adds an User Request to the Queue
	 * @param userRequest User Request
	 */
	public void addUserRequest(final UserRequest userRequest ) {
		if(ElevatorDirection.UP == userRequest.getDirectionToGo()) {
			addRequest(requestTreeForGoingUp,userRequest);
		} else {
			addRequest(requestTreeForGoingDown,userRequest);
		}
	}
	
	/**
	 * Returns a Single Request To be picked by Elevators
	 * @param currentFloor The Current Floor of the Elevators
	 * @param direction Direction of the Elevator
	 * @return User Request
	 * @throws InterruptedException Exception Thrown if this thread needs to interrupted
	 */
	public UserRequest pickRequest(int currentFloor, final ElevatorDirection direction) throws InterruptedException {
		// Acquire the lock
		lock.lock();
		try {
			
			// Block the requesting thread if both queues are empty
            while (requestTreeForGoingUp.size() == 0 && requestTreeForGoingDown.size() == 0) {
            	queueNotEmpty.await();
            }
            /**
             * If Elevator was going UP then it will pick the closet UP request if exists in the queue
             * If There is no request found then it will try to find out if there is any request for going down closet
             * if none of them is true then it will pick any of from these two queues
             * 
             */
            if(direction == ElevatorDirection.UP) {
                UserRequest req = requestTreeForGoingUp.higher(new UserRequest(currentFloor, Constants.HIGHEST_FLOORS+1, ElevatorDirection.UP));
            	if(req!=null) {
            		requestTreeForGoingUp.remove(req);
            		return req;
            	} else {
                	req = requestTreeForGoingDown.lower(new UserRequest(Constants.LOWEST_FLOOR, currentFloor+1, ElevatorDirection.DOWN));
                	if(req!=null) {
                		requestTreeForGoingDown.remove(req);
                		return req;
                	} else {
                		// Pick any request from both queues
                		
                		req = requestTreeForGoingDown.pollFirst();
                		if(req !=null) {
                		    return req;
                		} else {
                			req = requestTreeForGoingUp.pollFirst();
                		}
                		return req;
                	}
            	}
            } else {
            	UserRequest req = requestTreeForGoingDown.lower(new UserRequest( Constants.LOWEST_FLOOR, currentFloor, ElevatorDirection.DOWN));
            	if(req!=null) {
            		requestTreeForGoingDown.remove(req);
            		return req;
            	} else {
                	req = requestTreeForGoingUp.higher(new UserRequest(currentFloor, Constants.HIGHEST_FLOORS+1, ElevatorDirection.UP));
                	if(req!=null) {
                		requestTreeForGoingUp.remove(req);
                		return req;
                	} else {
                		
                        // Pick any request from both queues
                		req = requestTreeForGoingUp.pollFirst();
                		if(req !=null) {
                			return req;
                		} else {
                			req = requestTreeForGoingUp.pollFirst();
                		}
                		return req;
                	}
            	}
            }

        } finally {
        	// Release the lock
            lock.unlock();
        }
	}
	
	private void addRequest(final Set<UserRequest> set , final UserRequest e ) {
		//Add the Request in the set and send the signal to waiting threads to pick the request from the Queue
		lock.lock();
		try {
			set.add(e);
			queueNotEmpty.signal();
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Determines if any User willing to go UP from the current Floor is still waiting
	 * @param currentFloor Current Floor
	 * @return true/false
	 */
	public boolean isUserWillingToGoUpWaitingAtCurrentFloor(int currentFloor) {
		 final UserRequest request = requestTreeForGoingUp.higher(new UserRequest(currentFloor, currentFloor+1, ElevatorDirection.UP));
		 if(request!=null && (request.getCurrentFloor() == currentFloor)) {
				 return true;
		 }
		 return false;
	}
	
	/**
	 * Determines if any request to go Down whose pick up floor is same as the current Floor is still Pending
	 * @param currentFloor Current Floor
	 * @return true/false
	 */
	public boolean isRequestToGoDownPendingFromCurrentFloor(int currentFloor) {
		final UserRequest request = requestTreeForGoingDown.lower(new UserRequest(currentFloor, currentFloor, ElevatorDirection.DOWN));
		if(request !=null && (request.getCurrentFloor() == currentFloor)) {
			return true;
		 }
		return false;
	}
	
	/**
	 * Returns Users needs to be picked at current floor willing to go UP 
	 * @param currentFloor Current Floor
	 * @return Users
	 */
	public List<UserRequest> pickUsersWantToGoUpWaitingAtCurrentFloor(int currentFloor) {
		    final List<UserRequest> requestList = new ArrayList<>();
			boolean keepIterating = true;
			final UserRequest tempRequest = new UserRequest(currentFloor, currentFloor, ElevatorDirection.UP);
			while(keepIterating) {
				final UserRequest request = requestTreeForGoingUp.higher(tempRequest);
				if(request == null) {
					break;
				}
				if(request.getCurrentFloor() == currentFloor) {
					lock.lock();
					try {
						final boolean isDeleted = requestTreeForGoingUp.remove(request);
						if(isDeleted) {
							requestList.add(request);
						}
					} finally {
						lock.unlock();
					}
				} else {
					keepIterating =false;
				}
			}
		    return requestList;
	}
	
	/**
	 * Returns Users needs to be picked at current floor willing to go DOWN 
	 * @param currentFloor Current Floor
	 * @return Users
	 */
	public List<UserRequest> pickUsersWantToGoDownWaitingAtCurrentFloor(int currentFloor) {
            final List<UserRequest> requestList = new ArrayList<>();
		    boolean keepIterating = true;
			final UserRequest tempRequest = new UserRequest(currentFloor, currentFloor-1, ElevatorDirection.DOWN);
			while(keepIterating) {
				final UserRequest request = requestTreeForGoingDown.lower(tempRequest);
				if(request == null) {
					break;
				}
				if(request.getCurrentFloor() == currentFloor) {
					lock.lock();
					try {
						final boolean isDeleted = requestTreeForGoingDown.remove(request);
						if(isDeleted) {
							requestList.add(request);
						}
					} finally {
						lock.unlock();
					}
				} else {
					keepIterating =false;
				}
			}
		    return requestList;
	}
}
