
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
    
    /*public boolean compresaInArea(Coordinata centro, double raggio){
        boolean compresaX = false, compresaY = false;
        if( x >= (centro.getX()-raggio) && x <= (centro.getX()+raggio) ){
            compresaX = true;
        }
        if( y >= (centro.getY()-raggio) && y <= (centro.getY()+raggio) ){
            compresaY = true;
        }
        return (compresaY && compresaX);
    }*/

}
