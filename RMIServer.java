import java.io.*;
import java.rmi.*;

public class RMIServer {
	private static final String HOST= "localhost";
	
	public static void main(String[] args) throws RemoteException, IOException{
		//crea istanza di un oggetto remoto
		txtPrintImpl ref= new txtPrintImpl();
		//genera un nome con cui pubblicizzare l'oggetto
		String rmiObjName= "rmi://"+HOST+"/print";
		Naming.rebind(rmiObjName, ref);
		System.out.println("Server pronto!");
	}
}