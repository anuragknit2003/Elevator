package uk.co.citybank.elevator.queue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.Assert;
import uk.co.citybank.elevator.model.ElevatorDirection;
import uk.co.citybank.elevator.model.UserRequest;

/**
 * JUNIT Class to test @UserRequestQueue 
 * @author anuragtripathi
 *
 */
public class UserRequestQueueTest {

	final UserRequestQueue queue = UserRequestQueue.getUserRequestQueue();
	final static Logger logger = LoggerFactory.getLogger(UserRequestQueueTest.class);
	
	/**
	 * Tests if thread blocks if it tries to pick the item from queue if there is nothing in the queue
	 * @throws InterruptedException
	 */
	@Test
    public void testIfThreadsAreBlockedIfThereIsNothinginQueue() throws InterruptedException {
    	
    	queue.addUserRequest(createUserRequest(1, 10, ElevatorDirection.UP));
    	queue.addUserRequest(createUserRequest(1, 8, ElevatorDirection.UP));
    	
        final Thread t = startTestThread( new TestRunnable() {
			@Override
			protected void runTestThread() throws Throwable {
				for (int i = 0; i < 2; ++i) {
					Assert.assertNotNull(queue.pickRequest(1, ElevatorDirection.UP));
					logger.debug("Picked Element Fron Queue");
                }
				Thread.currentThread().interrupt();
                try {
                	queue.pickRequest(1, ElevatorDirection.UP);
                    Assert.assertFalse(true);
                } catch (InterruptedException success) {
                	logger.debug("Thread Interrupted");
                }
                Assert.assertFalse(Thread.interrupted());
			}
		});
        t.start();
        
    }
	
	/**
	 * Tests If thread get the items from queue in the waiting order i.e. the thread which came first should get the item first
	 * @throws InterruptedException
	 */
	@Test
    public void testIfThreadsGetsTheItemsFromQueueInOrder() throws InterruptedException {
		
		final UserRequest req1 = createUserRequest(1, 5, ElevatorDirection.UP);
		final UserRequest req2 = createUserRequest(1, 8, ElevatorDirection.UP);
        
        final Thread  t1 = startTestThread( new TestRunnable() {
			@Override
			protected void runTestThread() throws Throwable {
				final UserRequest req = queue.pickRequest(1, ElevatorDirection.UP);
				Assert.assertNotNull(req);
				Assert.assertEquals(req1, req);
			}
		});
        
        final Thread  t2 = startTestThread( new TestRunnable() {
			@Override
			protected void runTestThread() throws Throwable {
				final UserRequest req = queue.pickRequest(1, ElevatorDirection.UP);
				Assert.assertNotNull(req);
				Assert.assertEquals(req2, req);
			}
		});
        t1.start();
        t2.start();
        queue.addUserRequest(req1);
        queue.addUserRequest(req2);
        
    }
    
    /**
     * Returns a new started daemon Thread running the given runnable.
     */
    private Thread startTestThread(Runnable runnable) {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        return t;
    }
    
    private UserRequest createUserRequest(int currentFloor,int floorToGo,ElevatorDirection directionToGo) {
   	    return new UserRequest(currentFloor, floorToGo, directionToGo);
    }

    public abstract class TestRunnable implements Runnable {
        protected abstract void runTestThread() throws Throwable;

        public final void run() {
            try {
            	runTestThread();
            } catch (Throwable fail) {
            }
        }
    }
}
