import java.util.ArrayList;


public class Hypervisor {
	private String host_rmi, protocol;
	private Integer n,g,e,e_send,e_rec,e_sign;
	private Float p,r;
	private ArrayList<Node> nodes = new ArrayList<Node>();

	public Hypervisor(String host, String prot, Integer g_in,
			Integer n_in, Integer e_in, Integer e_s, Integer e_r,
			Integer e_signat, Float p_in, Float r_in) {	//constructor from Simulation, data from ProjGUI -> Simulation
		host_rmi=host;	//host for the RMI server
		protocol=prot;	//protocol used for the simulation (LSM or RED)
		g=g_in;			//number of destination location
		n=n_in;			//number of nodes in the network
		e=e_in;			//Total energy for each nodes
		e_send=e_s;		//Energy spent for sending a message
		e_rec=e_r;		//Energy spent for receiving  a message
		e_sign= e_signat;	//Energy for the signature of a message
		p=p_in;			//Probability for a neighbor node to process a location claim
		r=r_in;			//Communication radius of a node
	}
	
	public Node getNode(int id){
		return nodes.get(id);
	}

	public void init_usa() {
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
				Node node= new Node(cont_id,coor, r,p,g,e,e_send,e_rec,e_sign);
				nodes.add(node);
				}
			else
				cont_id--;
		}
	}

}
