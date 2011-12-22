
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
        boolean inX = false, inY = false;
        if( x >= (seekneigh.getX()-radius) && x <= (seekneigh.getX()+radius) ){
            inX = true;
        }
        if( y >= (seekneigh.getY()-radius) && y <= (seekneigh.getY()+radius) ){
            inY = true;
        }
        return (inX && inY);
    }

}
