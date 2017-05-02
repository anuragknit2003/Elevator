package uk.co.citybank.elevator.model;

/**
 * Model to hold User Request Properties
 * @author anuragtripathi
 *
 */
public class UserRequest {
	
   private final int currentFloor;
   private final ElevatorDirection directionToGo;
   private final int floorToGo;
   
   /**
    * ConStructor
    * @param currentFloor The Current Floor Location of the user 
    * @param floorToGo The Floor Number where User wants to go
    * @param directionToGo The Direction UP Or Down
    */
   public UserRequest(int currentFloor,int floorToGo, ElevatorDirection directionToGo) {
		this.currentFloor = currentFloor;
		this.floorToGo = floorToGo;
		this.directionToGo = directionToGo;
	}

	public int getCurrentFloor() {
		return currentFloor;
	}

	public ElevatorDirection getDirectionToGo() {
		return directionToGo;
	}
	public int getFloorToGo() {
		return floorToGo;
	}

	@Override
	public boolean equals(Object o){
	    if(o == null)                
	    	return false;
	    if(!(o instanceof UserRequest))
	    	return false;

	    UserRequest other = (UserRequest) o;
	    if( this.currentFloor==other.currentFloor && this.directionToGo == other.directionToGo && this.floorToGo == other.getFloorToGo() )  {
	    	return true;
	    }
	    return false;
    }
	
	@Override
	public int hashCode() {
		int result = 17;
        result = 31 * result + currentFloor;
        result = 31 * result + floorToGo;
        result = 31 * result + directionToGo.toString().hashCode();;
        return result;
	}
	
	@Override
	public String toString() {
			// TODO Auto-generated method stub
			return "["+currentFloor+","+floorToGo+","+directionToGo+"]";
	}
}
