import java.math.BigDecimal;
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
	
	public static int clones=0;	//counting the clones founded in a simulation

	public Hypervisor(String host, String prot, Integer g_in,
			Integer n_in, Integer e_in, Integer e_s, Integer e_r,
			Integer e_signat, Float p_in, Float r_in, Integer n_sim) {	//constructor from Simulation, data from ProjGUI -> Simulation
		host_rmi=host;	//host for the RMI server
		protocol=prot;	//protocol used for the simulation (LSM or RED)
		//protocol= "LSM";	//TESTING
		g=g_in;			//number of destination location
		n=n_in;			//number of nodes in the network
		e=e_in;			//Total energy for each nodes
		e_send=e_s;		//Energy spent for sending a message
		e_rec=e_r;		//Energy spent for receiving  a message
		e_sign= e_signat;	//Energy for the signature of a message
		p=p_in;			//Probability for a neighbor node to process a location claim
		//p=(float) 1;		//FOR TESTING
		r=r_in;			//Communication radius of a node
		//r=(float) 0.5;		//FOR TESTING
		
		nsim= n_sim;    //useful only for the printing on the output txt file!
	}
	
	public Node getNode(int id){
		return nodes.get(id);
	}
	
	public void allDead(){
		allDead=true;
	}

	public void init_usa() {
		//restoring of the static fields for the new iterations
		nodes.clear();	//clear the arraylist "nodes" before we start
		allDead=false;
		Node.setFoundClone(false);
		
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
			}
			else	//found=true, a node with this coordinates already exists
				cont_id--;
		}
	}
	
	public String attack(){
		//choose the ID of the node to clone
		int clone_id=(int) ((Math.random())*100);
		//create the clone
		Node clone= new Node();
		clone.clone(nodes.get(clone_id));
		boolean existCoord=false;
		while(!existCoord){
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
			}	//else we have to change coordinates
		}
		//let's assign neighbors to the clone node
		for(int j=0; j<nodes.size(); j++){
			Node fromJ= getNode(j);
			if(clone.getCoord().hasNeighbor(fromJ.getCoord(), r)){	//if a node is in the clone radius
				clone.insertNeigh(fromJ);	//add this node to the list of the new node neighbors
				fromJ.insertNeigh(clone);	//add the new node to the list of this node neighbors
			}
		}
		//add the clone node to the hypervisor list of all the nodes
		nodes.add(clone);
		
		//let's start the attack!!
		if(protocol=="RED"){
		    Random generator = new Random();
		    int r=generator.nextInt(); //generation of randomic int for RED protocol
			for(int i=0; i<nodes.size();i++ )
			    getNode(i).setRand(r);
		}
		Node.setProtocol(protocol);
		
		for(int i=0;i<nodes.size();i++)	//starts the thread of each node
			getNode(i).start();
		
		NoDeamon nd= new NoDeamon(nodes, this);	//thread to control if every Thread is still alive
		nd.start();
		
		while(!Node.getFoundClone() && !allDead){
			try {
				synchronized(this){
					this.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//Node founded or all Threads are dead
		//interrupt all the threads!
		//if we stopped because of the founding of the clone node, they still finish what they were doing 
		//(so it's possible they spend energy for receiving,sending or forwarding a message), then they interrupt!
		nd.setStop(true);	//indispensable if the simulation stopped because of the founding of the clone
		for(int i=0; i<nodes.size();i++)
			synchronized(nodes.get(i)){
				nodes.get(i).interrupt();
			}
		
		return connect_RMI();
	}
	
	@SuppressWarnings("finally")
	public String connect_RMI(){
		String echo="ERROR\n";	//at the end, it'll still be "ERROR" if there are problems with the connection to the RMI Server
		try{
			txtPrint ref= (txtPrint) Naming.lookup("rmi://"+host_rmi+"/print");
			int found=0;
			if(Node.getFoundClone()){
				found=1;
				clones++;
			}
			
			//sum_x= E(xi)
			//sum_xq= E(xi^2)
			
			//min, max, average, standard deviation of sent messages (s)
			int min_s=nodes.get(0).getSent();
			int max_s= min_s;
			double sum_s= max_s;	//sum of the xi
			double sum_sq= max_s*max_s;	//sum of the xi^2
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
			double avr_s_a=new BigDecimal(avr_s).setScale(4 , BigDecimal.ROUND_UP).doubleValue();	//approximate to the 4th digit
			double stand_devs_a=new BigDecimal(stand_devs).setScale(4 , BigDecimal.ROUND_UP).doubleValue();	//approximate to the 4th digit
			
			//min, max, average, standard deviation of received messages (r)
			int min_r=nodes.get(0).getRec();
			int max_r= min_r;
			double sum_r= max_r;	//sum of the xi
			double sum_rq= max_r*max_r;	//sum of the xi^2
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
			double avr_r_a=new BigDecimal(avr_r).setScale(4 , BigDecimal.ROUND_UP).doubleValue();	//approximate to the 4th digit
			double stand_devr_a=new BigDecimal(stand_devr).setScale(4 , BigDecimal.ROUND_UP).doubleValue();	//approximate to the 4th digit
			
			//min, max, average, standard deviation of verified messages (v)
			int min_v=nodes.get(0).getSign();
			int max_v= min_v;
			double sum_v= max_v;	//sum of the xi
			double sum_vq= max_v*max_v;	//sum of the xi^2
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
			double avr_v_a=new BigDecimal(avr_v).setScale(4 , BigDecimal.ROUND_UP).doubleValue();	//approximate to the 4th digit
			double stand_devv_a=new BigDecimal(stand_devv).setScale(4 , BigDecimal.ROUND_UP).doubleValue();	//approximate to the 4th digit
			
			//min, max, average, standard deviation of consumed energy (e)
			int min_e=e-nodes.get(0).final_energy();	//total energy- final energy
			int max_e= min_e;
			double sum_e= max_e;	//sum of the xi
			double sum_eq= max_e*max_e;	//sum of the xi^2
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
			double avr_e_a=new BigDecimal(avr_e).setScale(4 , BigDecimal.ROUND_UP).doubleValue();	//approximate to the 4th digit
			double stand_deve_a=new BigDecimal(stand_deve).setScale(4 , BigDecimal.ROUND_UP).doubleValue();	//approximate to the 4th digit
			
			//min, max, average, standard deviation of memorized messages (m)
			int min_m=nodes.get(0).getM().size();
			int max_m= min_m;
			double sum_m= max_m;	//sum of the xi
			double sum_mq= max_m*max_m;	//sum of the xi^2
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
			double avr_m_a=new BigDecimal(avr_m).setScale(4 , BigDecimal.ROUND_UP).doubleValue();	//approximate to the 4th digit
			double stand_devm_a=new BigDecimal(stand_devm).setScale(4 , BigDecimal.ROUND_UP).doubleValue();	//approximate to the 4th digit
			
			echo=protocol+ " "+ nsim + " "+ n + " "+ r+ " "+ p+ " "+g+" "+e+" "+e_send+" "+e_rec+" "+e_sign;
			echo+=" "+min_s+" "+max_s+" "+avr_s_a+" "+stand_devs_a;	//sent messages
			echo+=" "+min_r+" "+max_r+" "+avr_r_a+" "+stand_devr_a;	//received messages
			echo+=" "+min_v+" "+max_v+" "+avr_v_a+" "+stand_devv_a;	//verified messages
			echo+=" "+min_e+" "+max_e+" "+avr_e_a+" "+stand_deve_a;	//spent energy
			echo+=" "+min_m+" "+max_m+" "+avr_m_a+" "+stand_devm_a;	//message memorized
			echo+=" "+found+"\n";
			ref.print_on_txt(echo);
		}
		catch(ConnectException e) {System.out.println("Problems with the Server Connection! Please try again later.");}
		catch(Exception exc) {exc.printStackTrace();}
		finally{
			return echo;
		}
	}
	
	public double stdDev(double sum, double sum_2){	//INPUT: sum=sum_x=E(xi); sum_2=sum_xq=E(xi^2);
		//SD = Math.sqrt((sum_2 - sum*sum/n)/(n-1))
		return Math.sqrt((sum_2-(sum*sum/nodes.size()))/(nodes.size()-1));
		
	}
}
