
public class Coordinate {
	private double x, y;
    
    public Coordinate(double x_in, double y_in){
        this.x = x_in;
        this.y = y_in;
    }
    
    public boolean equals(Coordinate c){
        return (x==c.getX() && y==c.getY());
    }
    
    public double getX(){
        return x;
    }    
    
    public double getY(){
        return y;
    }
    
    public boolean hasNeighbor(Coordinate seekneigh, float radius){	//it controls if this node has the node with coordinate "seekneigh" in its neighborhood
    	Double diff_x= x-seekneigh.getX();
    	Double diff_y= y-seekneigh.getY();
    	//distance between two points, calculated with the Pythagorean Theorem
    	Float pita= (float) Math.sqrt((diff_x*diff_x)+(diff_y*diff_y));
    	if(pita<=radius)
    		return true;
    	else return false;
    }
    
    public Double distance(Coordinate dest){	//distance between two points
    	//useful for the research of the closer node to a destination
    	Double diff_x= x-dest.getX();
    	Double diff_y= y-dest.getY();
    	return Math.sqrt((diff_x*diff_x)+(diff_y*diff_y));
    }
}
