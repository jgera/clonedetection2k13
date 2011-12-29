import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.StringTokenizer;
import javax.swing.JScrollBar;

public class Simulation extends Thread{
	private String config_file="www.math.unipd.it/~conti/teaching/PCD1112/project_config.txt";
	private String host_rmi="localhost";
	private String host_conf, file="";
	private ProjGUI parent;
	private BufferedReader in;
	private BufferedWriter out;
	private String proto;
	private Integer nsim,g,n,e,e_send,e_receive,e_sign;
	private Float p,r;
	private static int cont_sim; 	//counter of the simulations
	private boolean stop=false;

	public Simulation(String conf_file, String host, ProjGUI par){
		if(!conf_file.isEmpty())
			config_file= conf_file;
		if(!host.isEmpty())
			host_rmi= host;
		parent= par;
	}
	
	public boolean strtknize(){
		//the URL address must begin with "www." and not with "http://" or "https://"
		if(config_file.startsWith("http")){
			config_file=config_file.replace("http://", "");
			config_file=config_file.replace("https://", "");
		}
		//split the config_file URL in
		//host_conf= the website for the socket connection, and
		//file= the source of the file in the website
		StringTokenizer strtok = new StringTokenizer(config_file, "/");
		if(config_file.startsWith("local"))	//there was a problem during the holiday due to a server exchange: it needed the local before the "www" begin
			host_conf= "local"+strtok.nextToken();
		else
			host_conf=strtok.nextToken();
		file+="/";
		while(strtok.hasMoreTokens()){
			file+=strtok.nextToken();
			if(strtok.hasMoreTokens())
				file+="/";
		}
		//
		file=file.trim();
		if(file.endsWith(".txt"))
			return true;
		else
			return false;
		
	}
	
	public void connect(){
		try {
			Socket socket = new Socket(host_conf,80);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (UnknownHostException i){
			i.printStackTrace();
			parent.getResultArea().append("\nUnhable to connect to the host "+config_file);
			parent.getResultArea().append("\nInsert a valid configuration link file");
			parent.resetInitialState();
		}
		catch(IOException e){
			e.printStackTrace();
			parent.getResultArea().append("\nErrore in input");
		}
	}
	
	public void sendGetRequest() throws IOException{
		out.write("GET " + file + " HTTP/1.0\r\n\r\n");
		out.flush();
	}
	
	public void receiveGetResponse() throws IOException, IllegalValueException{
		Boolean firstEmptyLine= false, stop= false;
		
		while(!stop){
			String line = in.readLine();
			if(line.isEmpty() && !firstEmptyLine){
				//We reached the end of the header
				firstEmptyLine= true;
			}
			if(firstEmptyLine){
				stop=true;
				//Start reading payload
				while((line=in.readLine()) != null){
					//decode
					line=cleanstring(line);
					if(!line.isEmpty())
						tokenize(line);
				}
			}
		}
	}
	
	public String cleanstring(String str){
		str=str.replaceAll("\t", "");
		str=str.replaceAll("\r","");
		str=str.replaceAll(" ","");
		return str;
	}
	
	public void tokenize(String line) throws IllegalValueException {
		//tokenize each line from the configuration file
		if(line.startsWith("%")){
			return;
		}
		StringTokenizer strtok = new StringTokenizer(line,"=");
		String next= strtok.nextToken();
	
		//check PROTO,string
		if(next.startsWith("PROTO")){
			//it must be LSM or RED, other tokens are not allowed
			String testproto= strtok.nextToken();
			if(testproto.startsWith("LSM") || testproto.startsWith("RED")){
				proto= testproto;
				return;
			}
			else throw new IllegalValueException("proto");
		}
		//ckeck NSIM || g || n || E (E+E_send+E_receive+E_signature), ints
		if(next.startsWith("NSIM")||next.startsWith("g")||next.startsWith("n")||next.startsWith("E")){
			Scanner scanner= new Scanner(strtok.nextToken());
			Integer x=scanner.nextInt();
			if(x>0){
				if(next.equals("NSIM")){	
					nsim=x;
					return;
				}
				
				if(next.equals("g")){
					g=x;
					return;
				}
				
				if(next.equals("n")){
					n=x;
					return;
				}
				
				if(next.equals("E")){
					e=x;
					return;
				}
				
				if(next.equals("E_send")){
					e_send=x;
					return;
				}
				
				if(next.equals("E_receive")){
					e_receive=x;
					return;
				}
				
				if(next.equals("E_signature")){
					e_sign=x;
					return;
				}
			}
			else throw new IllegalValueException(next);
		}
		
		//check p || r, floats
		if(next.startsWith("p")||next.startsWith("r")){
			String s=strtok.nextToken();
			Float x=Float.parseFloat(s);
			if(next.equals("p")){	
			    if(x>0.0 && x<1.0){
					p=x;
					return;
				}
			    else throw new IllegalValueException("p");
			}
			if(next.equals("r")){
				if(x>0.0){
					r=x;
					return;
				}
				else throw new IllegalValueException("r");
			}
		}
		
		else{	//not valid character for "next"
			throw new IllegalValueException("document returned");
		}
	}
	
	public void setStop(boolean flag){
		stop=flag;
	}
	
	public void run(){
		if(strtknize())
			//connection with the config_file URL
			connect();
		else{
			//the configuration file link is not a .txt
			parent.getResultArea().append("Configuration file link not valid");
			parent.resetInitialState();
			return;
		}
		try {
			sendGetRequest();
			receiveGetResponse();
		}catch (IOException e) {
			e.printStackTrace();
			parent.getResultArea().append("\nCommunication error with the inserted host");
			parent.resetInitialState();
			return;
		} catch (IllegalValueException i) {
			parent.getResultArea().append("\n"+i.getError());
			parent.resetInitialState();
			return;
		} catch(NumberFormatException nfe){	//if the parsed string in Tokenize() does not contain a parsable float
			parent.getResultArea().append("\nFormat of a FLOAT field not acceptable");
			parent.resetInitialState();
			return;
		} catch(InputMismatchException ime){	//if the parsed int in Tokenize() isn't actually an int!
			parent.getResultArea().append("\nFormat of a INT field not acceptable");
			parent.resetInitialState();
			return;
		}
		//let's the REAL simulation start...
		//creation of the Hypervisor
		Hypervisor hyp= new Hypervisor(host_rmi,proto,g,n,e,e_send,e_receive,e_sign,p,r,nsim);
		while(!stop && cont_sim<nsim){
				hyp.init_usa(); //initialization of the unite-square area
				//Clone attack!
				String result=hyp.attack();
				cont_sim++;
				parent.getResultArea().append(result);
				//set the bar to follow the printing in the textArea
				JScrollBar vbar = parent.getBar().getVerticalScrollBar();
				vbar.setValue(vbar.getMaximum());
		}
		//if the user pressed the STOP button or all the simulations end : disable the Stop Button
		parent.setBStop(false);
		parent.getResultArea().append("\n"+Hypervisor.cloni);	//PROVA
	}
}
