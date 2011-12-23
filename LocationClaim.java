
public class LocationClaim {
	private Integer id;
	private Coordinate coor;
	private int locations;

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
	
	public void setNumLoc(int loc){
		locations=loc;
	}
}
