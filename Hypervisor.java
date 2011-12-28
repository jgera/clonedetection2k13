import java.rmi.ConnectException;
import java.rmi.Naming;
import java.util.Random;
import java.util.ArrayList;


public class Hypervisor {
	private String host_rmi, protocol;
	private Integer n,g,e,e_send,e_rec,e_sign,nsim;
	private Float p,r;
	private ArrayList<Node> nodes = new ArrayList<Node>();
	private static boolean allDead;

	public Hypervisor(String host, String prot, Integer g_in,
			Integer n_in, Integer e_in, Integer e_s, Integer e_r,
			Integer e_signat, Float p_in, Float r_in, Integer n_sim) {	//constructor from Simulation, data from ProjGUI -> Simulation
		host_rmi=host;	//host for the RMI server
		//protocol=prot;	//protocol used for the simulation (LSM or RED)
		protocol= "LSM";	//TESTING
		g=g_in;			//number of destination location
		n=n_in;			//number of nodes in the network
		//n=3;		//TESTING
		//n=5; 		//TESTING
		e=e_in;			//Total energy for each nodes
		e_send=e_s;		//Energy spent for sending a message
		e_rec=e_r;		//Energy spent for receiving  a message
		e_sign= e_signat;	//Energy for the signature of a message
		p=p_in;			//Probability for a neighbor node to process a location claim
		//p=(float) 1;
		//r=r_in;			//Communication radius of a node
		r=(float) 0.4;		//TESTING
		
		nsim= n_sim;    //useful only for the printing on the output txt file!
	}
	
	public Node getNode(int id){
		return nodes.get(id);
	}
	
	public void dimAlive(){
		allDead=true;
		//Node.setFoundClone(true);
		System.out.println("All dead!");
		/*synchronized(this){
			this.notify();
		}*/
	}

	public void init_usa() {
		System.out.println("ITERAZIONE SUCCESSIVA");
		//restoring of the static fields for the new iterations
		nodes.clear();	//clear the arraylist "nodes" before we start
		allDead=false;
		Node.setFoundClone(false);
		Node.setProtocol(protocol);
		//Node.setMessPro(0);
		
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
		
		NoDeamon nd= new NoDeamon(nodes, this);	//daemon thread to control if every Thread is still alive
		nd.start();
		
		while(!Node.getFoundClone() && !allDead){	//aggiungere controllo anche sul fatto che i thread sian tutti attivi
			try {
				synchronized(this){
					System.out.println("HYP IN WAIT");
					this.wait();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(Node.getFoundClone())
			nd.setStop(true);
		
		System.out.println("AVVISATO!");
		for(int i=0; i<nodes.size();i++)
			nodes.get(i).interrupt();
		
		return connect_RMI();
	}
	
	@SuppressWarnings("finally")
	public String connect_RMI(){
		System.out.println("connect RMI");
		String echo="ERROR\n";
		try{
			txtPrint ref= (txtPrint) Naming.lookup("rmi://"+host_rmi+"/print");
			int found=0;
			System.out.println(Node.getFoundClone());
			if(Node.getFoundClone())
				found=1;
			
			//min, max, average, standard deviation of sent messages (s)
			int min_s=nodes.get(0).getSent();
			int max_s= min_s;
			int sum_s= max_s;	//sum of the xi
			int sum_sq= max_s*max_s;	//sum of the xi^2
			for(int i=1; i<nodes.size();i++){
				int calc= nodes.get(i).getSent();
				if(calc<min_s)
					min_s=calc;
				else
					if(calc>max_s)
						max_s=calc;
				sum_s+=calc;
				sum_sq+=(calc*calc);
			}
			double avr_s= sum_s/(nodes.size());
			double stand_devs= stdDev(sum_s,sum_sq);
			
			//min, max, average, standard deviation of received messages (r)
			int min_r=nodes.get(0).getRec();
			int max_r= min_r;
			int sum_r= max_r;	//sum of the xi
			int sum_rq= max_r*max_r;	//sum of the xi^2
			for(int i=1; i<nodes.size();i++){
				int calc= nodes.get(i).getRec();
				if(calc<min_r)
					min_r=calc;
				else
					if(calc>max_r)
						max_r=calc;
				sum_r+=calc;
				sum_rq+=(calc*calc);
			}
			double avr_r= sum_r/(nodes.size());
			double stand_devr= stdDev(sum_r, sum_rq);
			
			//min, max, average, standard deviation of verified messages (v)
			int min_v=nodes.get(0).getSign();
			int max_v= min_v;
			int sum_v= max_v;	//sum of the xi
			int sum_vq= max_v*max_v;	//sum of the xi^2
			for(int i=1; i<nodes.size();i++){
				int calc= nodes.get(i).getSign();
				if(calc<min_v)
					min_v=calc;
				else
					if(calc>max_v)
						max_v=calc;
				sum_v+=calc;
				sum_vq+=(calc*calc);
			}
			double avr_v= sum_v/(nodes.size());
			double stand_devv= stdDev(sum_v, sum_vq);
			
			//min, max, average, standard deviation of consumed energy (e)
			int min_e=e-nodes.get(0).final_energy();	//energy tot- final energy
			int max_e= min_e;
			int sum_e= max_e;	//sum of the xi
			int sum_eq= max_e*max_e;	//sum of the xi^2
			for(int i=1; i<nodes.size();i++){
				int calc= e-nodes.get(i).final_energy();
				if(calc<min_e)
					min_e=calc;
				else
					if(calc>max_e)
						max_e=calc;
				sum_e+=calc;
				sum_eq+=(calc*calc);
			}
			double avr_e= sum_e/(nodes.size());
			double stand_deve= stdDev(sum_e, sum_eq);
			
			//min, max, average, standard deviation of memorized messages (m)
			int min_m=nodes.get(0).getM().size();
			int max_m= min_m;
			int sum_m= max_m;	//sum of the xi
			int sum_mq= max_m*max_m;	//sum of the xi^2
			for(int i=1; i<nodes.size();i++){
				int calc= nodes.get(i).getM().size();
				if(calc<min_m)
					min_m=calc;
				else
					if(calc>max_m)
						max_m=calc;
				sum_m+=calc;
				sum_mq+=(calc*calc);
			}
			double avr_m= sum_m/(nodes.size());
			double stand_devm= stdDev(sum_m, sum_mq);
			
			echo=protocol+ " "+ nsim + " "+ n + " "+ r+ " "+ p+ " "+g+" "+e+" "+e_send+" "+e_rec+" "+e_sign;
			echo+=" "+min_s+" "+max_s+" "+avr_s+" "+stand_devs;	//sent messages
			echo+=" "+min_r+" "+max_r+" "+avr_r+" "+stand_devr;	//received messages
			echo+=" "+min_v+" "+max_v+" "+avr_v+" "+stand_devv;	//verified messages
			echo+=" "+min_e+" "+max_e+" "+avr_e+" "+stand_deve;	//spent energy
			echo+=" "+min_m+" "+max_m+" "+avr_m+" "+stand_devm;	//message memorized
			echo+=" "+found+"\n";
			ref.print_on_txt(echo);
		}
		catch(ConnectException e) {System.out.println("Problems with the Server Connection! Please try again later.");}
		catch(Exception exc) {exc.printStackTrace();}
		finally{
			System.out.println("FINE CONNECT");
			return echo;
		}
	}
	
	public double stdDev(int sum, int sum_2){
		//D = Math.sqrt((sum_2 - sum*sum/n)/(n-1))
		return Math.sqrt((sum_2-(sum*sum/nodes.size()))/(nodes.size()-1));
		
	}
	
	public int clonitot(){
		return Node.cloni;
	}

}
