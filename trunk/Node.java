
public class Node extends Thread{
	private int id;
	private Coordinate coord; //coordinate of the node in the Unite-Square Area
	private Float radius,prob;
	private int locations;
	private int energy, en_send, en_rec, en_sign;
	
	public Node(int cont_id, Coordinate coo_in, Float r, Float p, Integer g, Integer e,
			Integer e_send, Integer e_rec, Integer e_sign) {
		id= cont_id;		//the ID for the node
		coord= coo_in;		//coordinates of the node
		radius= r;			//communication radius of the node
		prob=p;				//Probability for a neighbour node to process a location claim
		locations=g;		//number of destination location
		energy=e;			//Total energy for the node
		en_send= e_send;	//Energy spent for sending a message
		en_rec= e_rec;		//Energy spent for receiving a message
		en_sign= e_sign;	//Energy for the signature of a message
	}
	
	public int getNodeId(){
		return id;
	}
	
	public Coordinate getCoord(){
		return coord;
	}

}
