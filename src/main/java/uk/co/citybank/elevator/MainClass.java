package uk.co.citybank.elevator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringUtils;

import uk.co.citybank.elevator.exception.ValidationException;
import uk.co.citybank.elevator.manager.ElevatorManager;
import uk.co.citybank.elevator.model.ElevatorDirection;

/**
 * Main Class to Start the Application
 * @author anuragtripathi
 *
 */
public class MainClass {
	 public static void main( String[] args ) throws NumberFormatException  {
		 int elevatorsToBeStarted = 2;
		 if(args.length ==1 && StringUtils.isNumeric(args[0])) {
			 elevatorsToBeStarted = Integer.parseInt(args[0]);
		 }
		 System.out.println("Number of the Elevators needs to be running :"+elevatorsToBeStarted);

    	 final ElevatorManager manager = ElevatorManager.getElevatorManager();
    	 manager.startElevators(elevatorsToBeStarted);
    	 
    	 InputStreamReader isr = null;
    	 BufferedReader br =null;
	     try {
	         isr = new InputStreamReader(System.in);
	         br = new BufferedReader(isr);
	         String line = "";
	         while ((line = br.readLine()) != null) {
	        	 if("SHUTDOWN".equalsIgnoreCase(line)) {
		    		 System.out.println("Shutting Down All the Elevators");
		    		 manager.shutDownElevators();
		    		 break;
		    	 } else {
		    		 final String[] array = line.split("\\s+");
		    		 if(array==null || array.length!=3 ||
		    				 !StringUtils.isNumeric(array[0]) || !StringUtils.isNumeric(array[1]) 
		    				 || !(ElevatorDirection.DOWN.toString().equals(array[2]) || ElevatorDirection.UP.toString().equals(array[2]))) {
		    			 System.out.println("Invalid Request");
		    			 continue;
		    		 }
		    		 try {
			    		 manager.raiseUserRequest(Integer.parseInt(array[0]), 
			    				 Integer.parseInt(array[1]), ElevatorDirection.valueOf(array[2]));
		    		 } catch (ValidationException e) {
						System.out.println("Invalid Request");
						continue;
					}
		    	 }
	         }
	         isr.close();
	     } catch (IOException ioe) {
	         ioe.printStackTrace();
	     } finally {
	    	 manager.shutDownElevators();
	    	 try {
		    	 if(br!=null) {  br.close(); }
		    	 if(isr!=null) { isr.close(); }
	    	 } catch (Exception e) { }
			 System.out.println("Exit");
			 System.exit(1);
		}
	 }
}
