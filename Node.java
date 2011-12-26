import java.util.*;

public class Node extends Thread{
	private int id;
	private Coordinate coord; //coordinate of the node in the Unite-Square Area
	private Float radius, prob;
	private int locations;
	private int energy, en_send, en_rec, en_sign;
	private int rand; //randomic int generated by hypervisor for RED protocol
	private Hypervisor parent;
	private ArrayList<Node> neigh = new ArrayList<Node>();
	private ArrayList<LocationClaim> messages =new ArrayList<LocationClaim>();
    private Map<Integer, Coordinate> m = new HashMap<Integer, Coordinate>(); //contains the map of the routed nodes coordinate
	private static String protocol;
	private static boolean foundClone=false;
	
	public static int cloni=0;
	public Node(){}
	
	public Node(int cont_id, Coordinate coo_in, Float r, Float p, Integer g, Integer e,
			Integer e_send, Integer e_rec, Integer e_sign, Hypervisor par) {
		id= cont_id;		//the ID for the node
		coord= coo_in;		//coordinates of the node
		radius= r;			//communication radius of the node
		prob=p;				//Probability for a neighbour node to process a location claim
		locations=g;		//number of destination location
		energy=e;			//Total energy for the node
		en_send= e_send;	//Energy spent for sending a message
		en_rec= e_rec;		//Energy spent for receiving a message
		en_sign= e_sign;	//Energy for the signature of a message
		parent=par; 		//refer to hypervisor
	}
	
	public int getNodeId(){
		return id;
	}
	
	public boolean setRand(int x){
		rand=x;
		return true;
	}
	
	public Coordinate getCoord(){
		return coord;
	}
	
	public static void setProtocol(String s){
			protocol=s;
	}
	
	public static boolean getFoundClone(){
		return foundClone;
	}
	
	public static void setFoundClone(boolean fc){
		foundClone=fc;
	}
	
	public void insertNeigh(Node n){
		neigh.add(n);
		//System.out.println(this.getNodeId()+ " ha come vicino il nodo "+n.getNodeId());
	}
	
	public void clone(Node fromClone){
		id= fromClone.id;
		radius= fromClone.radius;
		prob= fromClone.prob;
		locations= fromClone.locations;
		energy= fromClone.energy;
		en_send= fromClone.en_send;
		en_rec= fromClone.en_rec;
		en_sign= fromClone.en_sign;
		parent= fromClone.parent;
	}
	
	public void setCoordinate(Coordinate cor){
		coord=cor;
	}
	
	public synchronized ArrayList<LocationClaim> getMessages(){
		return messages;
	}
	
	public void sendLC(LocationClaim mess){
		//System.out.println(this.getNodeId()+ "send message to" + n.getNodeId());
		synchronized(messages){
			messages.add(mess);
			messages.notify();
		}
	}
	
	public synchronized void receiveLC(LocationClaim mess){	//receive the location claim for the first time
		System.out.println(this.getNodeId()+ " receive message of claim location from "+ mess.getID());
		if(Math.random()<=(1-prob))
			System.out.println("ignore"); //ignore the message
		else{ //forward the message
			Double x= ((Math.random()*99)/100);
			Double y= ((Math.random()*99)/100);
			System.out.println(this.getCoord().getX()+ " "+ this.getCoord().getY());
			System.out.println(x+ " "+ y);
			Coordinate dest= new Coordinate(x,y);	//destination
			mess.setDestination(dest);
			forward(mess);
		}
	}
	
	public synchronized void receiveLCForw(LocationClaim mess){	//receive the location claim in order to forwarding it, LSM
		//aggiungere controllo su energy finita
		energy-=en_rec;
		System.out.println(this.getNodeId()+ " receive message forwarded");
		//inserire conto su locations!! (g)
		Coordinate test=m.get(mess.getID());
		if(test!=null){	//already present
			System.out.println("Gi� presente");
			energy-=en_sign;
			//control if this id is already present
			//verify: spent energy for signature
			//controllo energia sufficiente
			//MODIFICHE QUI PER VERIFICARE SE ESISTE CLONE: NON CONVINTO DEL FUNZIONAMENTO DEL GET SOTTOSTANTE
			if(!test.equals(mess.getCoord())){	//same id, different coordinates = CLONE!
				System.out.println("CLONE" + mess.getID());
				cloni++;	//test per contare i cloni trovati in 100 cicli
				foundClone=true;	//static field, flag for the hypervisor
				synchronized(parent){
					parent.notify();
				}
				synchronized(messages){
					messages.notifyAll();
				}
			}
			else forward(mess);
		}
		else{//not present
			System.out.println("Nodo "+this.getNodeId()+ "salva in hash "+ mess.getID()+ ", coordinate "+mess.getCoord().getX()+ " "+mess.getCoord().getY());
			m.put(mess.getID(), mess.getCoord());
			forward(mess);
		}
	}
	
	public synchronized void forward(LocationClaim message){
		//the forwarding is different according to the protocol implemented
		Coordinate dest= message.getCoord();
		if(protocol=="LSM"){
			System.out.println("LSM");
			Node closer= this;
			System.out.println("closer before= "+this.getNodeId());
			Double distance_min= closer.getCoord().distance(dest);
			for(int i=0;i<neigh.size();i++){
				Double newdistance=neigh.get(i).getCoord().distance(dest);
				if(newdistance<distance_min){
					closer= neigh.get(i);
					distance_min= newdistance;
				}
			}
			System.out.println("closer after= "+closer.getNodeId());
			
			if(closer!=this && message.getNumLoc()!=0){ //there is a node closer to the destination and we can still forward
				message.setForw(true);
				System.out.println("Forwarding");
				energy-=en_send;
				message.setNumLoc(message.getNumLoc()-1);
				System.out.println(this.getNodeId()+" forward to " +closer.getNodeId());
				closer.sendLC(message);
				//aggiungere controllo su energy finita
				 
			}
			if(closer==this){	//this is the closest node to the destination
				System.out.println("Save here!");
				if(m.get(message.getID()).equals(message.getCoord())){	//this id+coordinate is already present
					System.out.println("Gi� presente");
				}
				else{
					System.out.println("Nodo "+this.getNodeId()+ "salva in hash "+ message.getID()+ ", coordinate "+message.getCoord().getX()+ " "+message.getCoord().getY());
					m.put(message.getID(), message.getCoord());
					//control if this id is already present
					//verify: spent energy for signature
					//controllo energia sufficiente
					//MODIFICHE QUI PER VERIFICARE SE ESISTE CLONE: NON CONVINTO DEL FUNZIONAMENTO DEL GET SOTTOSTANTE
					energy-=en_sign;
					if(!m.get(message.getID()).equals(message.getCoord())){	//same id, different coordinates = CLONE!
						System.out.println("CLONE" + message.getID());
						cloni++;	//test per contare i cloni trovati in 100 cicli
						foundClone=true;	//static field, flag for the hypervisor
						synchronized(parent){
							parent.notify();
						}
						synchronized(messages){
							messages.notifyAll();
						}
						
					}
				}
			}
		//if(protocol=="RED"){}	
		}
	}
		
	public void run(){
		LocationClaim message= new LocationClaim(getNodeId(), getCoord(), locations);	//create a LCMessage with id and coordinates of the node
		//the node send broadcast to its neighbors the locationclaim message
		for(int i=0;i<neigh.size();i++){
			neigh.get(i).sendLC(message);
		}
		LocationClaim mex=null;
		boolean foundclone=false;
		while(!foundclone){
			synchronized(messages){
				while(messages.isEmpty()){
					System.out.println("Empty LOLOLOLOLOLOLOLOLOL?");
					try {
							messages.wait();
						} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						System.out.println("Interrotto");
						messages.clear();
						return;
						}	
				}
				mex= messages.remove(0);
				if(!mex.toForw())
					receiveLC(mex);
				else
					receiveLCForw(mex);
			}
		}
		//if(protocol=="RED");
		//if(protocol=="LSM");
	}
}
