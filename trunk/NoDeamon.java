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
				//System.out.println(nodes.get(i)+" "+nodes.get(i).getState());
				//if(nodes.get(i).getMessages().isEmpty())
				synchronized(nodes.get(i)){
					//if(nodes.get(i).getState()==State.BLOCKED || 
					if(nodes.get(i).getState()==State.WAITING)
					//if(nodes.get(i).getState()==State.TERMINATED)
						numDead++;
					}
				}
			if(numDead==nodes.size()){
				System.out.println("Chiamiamo la merdaccia");
				stop=true;
				parent.dimAlive();
				synchronized(parent){
					parent.notify();
				}
			}
		}
	}
}
