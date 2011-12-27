import java.util.ArrayList;

public class NoDeamon extends Thread{
	ArrayList<Node> nodes= new ArrayList<Node>();
	Hypervisor parent;
	
	NoDeamon(ArrayList<Node> _nodes, Hypervisor _hypervisor){
		nodes= _nodes;
		parent= _hypervisor;
		this.setDaemon(true);
	}
	
	public void run(){
		System.out.println("DAEMON!");
		boolean found=false;
		while(!found){
			/*int numDead=0;
			for(int i=0; i<nodes.size();i++){
				//System.out.println(nodes.get(i)+" "+nodes.get(i).getState());
				//if(nodes.get(i).getMessages().isEmpty())
				if(nodes.get(i).getState()==State.BLOCKED || nodes.get(i).getState()==State.WAITING)
					numDead++;
			}
			System.out.println("numDead è "+numDead);
			if(numDead==nodes.size()){
				System.out.println("Chiamiamo la merdaccia");
				found=true;
				parent.dimAlive();
				synchronized(parent){
					parent.notify();
				}
			}*/
			//System.out.println("Message proceeded "+Node.getmessagepro());
			if(Node.getmessagepro()==0){ //all messages proceeded
				System.out.println("Chiamiamo la merdaccia");
				found=true;
				parent.dimAlive();
				synchronized(parent){
					parent.notify();
				}
			}	
		}
	}
}
