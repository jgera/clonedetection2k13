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
		//Sarebbe giusto usare metodo append() perchè così richiede il professore. Ma con append si dovrebbe poi far uso del carattere "\n" che viene
		//mal interpretato da BloccoNote (se vogliamo aprire .txt in Windows con questo programma,se lo si apre con Oo.Writer è tutto ok!).
		//Alternativa: println(), che fa gli a capo giusti anche aprendo il file con BloccoNote!
		//Se si usa println(), comunque, per ogni iterazione bisogna chiamare una sola volta la connessione al ServerRMI!
		System.out.println("SERVER");
		write.println(values);
		write.flush();
	}
}
