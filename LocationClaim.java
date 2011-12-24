
public class LocationClaim {
	private final Integer id;
	private Coordinate coor;
	private int locations;
	private boolean firsttime= false; //if this message has been red at least one time
	private Coordinate destination; //if the message has to be forwarded, this is the destination of it

	public LocationClaim(Integer _id, Coordinate _coor, int _locations){
		id= _id;
		coor= _coor;
		locations=_locations;
	}
	
	public Integer getID(){
		return id;
	}
	
	public Coordinate getCoord(){
		return coor;
	}
	
	public int getNumLoc(){
		return locations;
	}
	
	public void setDestination(Coordinate _cor){
		destination= _cor;
	}
	
	public void setNumLoc(int loc){
		locations=loc;
	}
	
	public void setForw(boolean forw){
		firsttime= forw;
	}
	
	public boolean toForw(){
		return firsttime;
	}
}
