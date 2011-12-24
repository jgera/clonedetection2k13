import java.rmi.ConnectException;
import java.rmi.Naming;
import java.util.Random;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;


public class Hypervisor {
	private String host_rmi, protocol;
	private Integer n,g,e,e_send,e_rec,e_sign,nsim;
	private Float p,r;
	private ArrayList<Node> nodes = new ArrayList<Node>();

	public Hypervisor(String host, String prot, Integer g_in,
			Integer n_in, Integer e_in, Integer e_s, Integer e_r,
			Integer e_signat, Float p_in, Float r_in, Integer n_sim) {	//constructor from Simulation, data from ProjGUI -> Simulation
		host_rmi=host;	//host for the RMI server
		//protocol=prot;	//protocol used for the simulation (LSM or RED)
		protocol= "LSM";	//TESTING
		g=g_in;			//number of destination location
		n=n_in;			//number of nodes in the network
		//n=5; 		//TESTING
		e=e_in;			//Total energy for each nodes
		e_send=e_s;		//Energy spent for sending a message
		e_rec=e_r;		//Energy spent for receiving  a message
		e_sign= e_signat;	//Energy for the signature of a message
		p=p_in;			//Probability for a neighbor node to process a location claim
		//p=(float) 0.5;
		r=r_in;			//Communication radius of a node
		//r=(float) 0.5;		//TESTING
		
		nsim= n_sim;    //useful only for the printing on the output txt file!
	}
	
	public Node getNode(int id){
		return nodes.get(id);
	}

	public void init_usa() {
		nodes.clear();	//clear the arraylist "nodes" before we start
		Node.setProtocol(protocol);
		int cont_id;
		for(cont_id=0;cont_id<n; cont_id++){	//cont_id is for the ID of a node (from 0 to n-1)
			Double x= Math.random();
			Double y= Math.random();
			Coordinate coor= new Coordinate(x,y);
			boolean found=false; //looking for another node with the same coordinates
			for(int i=0; i<cont_id & !found;i++){
				if(getNode(i).getCoord().equals(coor))	//if the coordinates of one of the nodes in the ArrayList is equals to the actual coordinates
					found=true;
			}
			if(!found){
				Node node= new Node(cont_id,coor, r,p,g,e,e_send,e_rec,e_sign,this);
				nodes.add(node);
				//build the neighborhood for this node
				for(int j=0; j<cont_id; j++){
					Node fromJ= getNode(j);
					if(node.getCoord().hasNeighbor(fromJ.getCoord(), r)){	//if a node is in the new node radius
						node.insertNeigh(fromJ);	//add this node to the list of the new node neighbors
						fromJ.insertNeigh(node);	//add the new node to the list of this node neighbors
					}
				}
				System.out.println("Nodo aggiunto" + cont_id);
				}
			else
				cont_id--;
		}
	}
	
	public String attack(){
		System.out.println("CREAZIONE CLONE");
		int clone_id=(int) ((Math.random())*100);
		System.out.println("Numero random 0-99 è "+clone_id);
		//create the clone
		Node clone= new Node();
		clone.clone(nodes.get(clone_id));
		boolean existCoord=false;
		while(!existCoord){
			System.out.println("provo coordinata");
			Double x= Math.random();
			Double y= Math.random();
			Coordinate coor= new Coordinate(x,y);
			boolean found=false;
			for(int i=0; i<nodes.size() & !found;i++){
				if(getNode(i).getCoord().equals(coor))	//if the coordinates of one of the nodes in the ArrayList is equals to the actual coordinates
					found=true;
			}
			if(!found){	//we can set this coordinates as new node's coordinates
				clone.setCoordinate(coor);
				existCoord=true;
			}
			//else we have to change coordinates
		}
		//let's assign neighbors to the clone node
		for(int j=0; j<nodes.size(); j++){
			Node fromJ= getNode(j);
			if(clone.getCoord().hasNeighbor(fromJ.getCoord(), r)){	//if a node is in the clone radius
				clone.insertNeigh(fromJ);	//add this node to the list of the new node neighbors
				fromJ.insertNeigh(clone);	//add the new node to the list of this node neighbors
			}
		}
		nodes.add(clone);
		
		//let's start the attack!!
		if(protocol=="LSM"){
			Node.setProtocol("LSM");
		}
		else{
		    Random generator = new Random();
		    int r=generator.nextInt(); //generation of randomic int for RED protocol
			for(int i=0; i<nodes.size();i++ ){
			    getNode(i).setRand(r);
			}
			Node.setProtocol("RED");
		}
		
		for(int i=0;i<nodes.size();i++){
			getNode(i).start();
		}
		
		while(!Node.getFoundClone() && !allAlive()){	//aggiungere controllo anche sul fatto che i thread sian tutti attivi
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return connect_RMI();
	}
	
	public boolean allAlive(){
		for(int i=0; i<nodes.size();i++){
			if(!nodes.get(i).isAlive()){
				this.notify();
				return false;
			}
		}
		return true;
	}
	
	@SuppressWarnings("finally")
	public String connect_RMI(){
		String echo="ERROR";
		try{
			txtPrint ref= (txtPrint) Naming.lookup("rmi://"+host_rmi+"/print");
			echo=protocol+ " "+ nsim + " "+ n + " "+ r+ " "+ p+ " "+g+" "+e+" "+e_send+" "+e_rec+" "+e_sign+"\n";
			ref.print_on_txt(echo);
		}
		catch(ConnectException e) {System.out.println("Problems with the Server Connection! Please try again later.");}
		catch(Exception exc) {exc.printStackTrace();}
		finally{
			return echo;
		}
	}
	
	public int clonitot(){
		return Node.cloni;
	}

}
