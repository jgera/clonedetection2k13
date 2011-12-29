
public class IllegalValueException extends Exception {
	private static final long serialVersionUID = 1L;
	private String name;
	
	IllegalValueException(String n){
		name= n;
	}
	
	public String getError(){
		return "The value of the variable \""+name+"\" is not acceptable for the simulation";
	}
}
