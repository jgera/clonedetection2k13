
public class LocationClaim {
	private Integer id;
	private Coordinate coor;
	private boolean forw= false; //if this message has been red at least one time
	private Coordinate destination; //if the message has to be forwarded, this is the destination of it

	public LocationClaim(Integer _id, Coordinate _coor){
		id= _id;
		coor= _coor;
	}
	
	public LocationClaim() {}

	public void clone(LocationClaim toClone){
		id= toClone.id;
		coor= toClone.coor;
	}
	
	public Integer getID(){
		return id;
	}
	
	public Coordinate getCoord(){
		return coor;
	}
	
	public Coordinate getDestination(){
		return destination;
	}
	
	public void setDestination(Coordinate _cor){
		destination= _cor;
	}
	
	public void setForw(boolean _forw){
		forw= _forw;
	}
	
	public boolean getForw(){
		return forw;
	}
}
