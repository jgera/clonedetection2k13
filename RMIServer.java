import java.io.*;
import java.rmi.*;

public class RMIServer {
	private static final String HOST= "localhost";
	
	public static void main(String[] args) throws RemoteException, IOException{
		txtPrintImpl ref= new txtPrintImpl();
		String rmiObjName= "rmi://"+HOST+"/print";
		Naming.rebind(rmiObjName, ref);
		System.out.println("Server ready");
	}
}