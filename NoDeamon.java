import java.util.ArrayList;

public class NoDeamon extends Thread{
	ArrayList<Node> nodes= new ArrayList<Node>();
	Hypervisor parent;
	boolean stop=false;
	
	NoDeamon(ArrayList<Node> _nodes, Hypervisor _hypervisor){
		nodes= _nodes;
		parent= _hypervisor;
	}
	
	public void setStop(boolean b) {
		// TODO Auto-generated method stub
		stop=b;
	}
	
	public void run(){
		System.out.println("DAEMON!");
		//boolean found=false;
		while(!stop){
			int numDead=0;
			for(int i=0; i<nodes.size();i++){
				synchronized(nodes.get(i)){ 
					if(nodes.get(i).getState()==State.WAITING || nodes.get(i).getState()==State.TERMINATED)
						numDead++;
					}
				}
			if(numDead==nodes.size()){
				stop=true;
				parent.dimAlive();
				synchronized(parent){ 	//notify to the hypervisor
					parent.notify();
				}
			}
		}
	}
}
