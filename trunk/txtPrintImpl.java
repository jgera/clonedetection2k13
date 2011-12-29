import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class txtPrintImpl extends UnicastRemoteObject implements txtPrint{
	private static final long serialVersionUID = 1L;
	PrintWriter write;
	
	public txtPrintImpl() throws RemoteException, FileNotFoundException {
		write= new PrintWriter(new File("output.txt"));
	}
	
	public void print_on_txt(String values){
		System.out.println("RMI Server writing on the .txt output file");
		write.println(values);
		write.flush();
	}
}
