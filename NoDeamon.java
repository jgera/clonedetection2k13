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
		//boolean found=false;
		while(true){
			int numDead=0;
			for(int i=0; i<nodes.size();i++){
				//System.out.println(nodes.get(i)+" "+nodes.get(i).getState());
				//if(nodes.get(i).getMessages().isEmpty())
				//if(nodes.get(i).getState()==State.BLOCKED || nodes.get(i).getState()==State.WAITING)
				if(nodes.get(i).getState()==State.TERMINATED)
					numDead++;
			}
			if(numDead==nodes.size()){
				System.out.println("Chiamiamo la merdaccia");
				//found=true;
				parent.dimAlive();
				synchronized(parent){
					parent.notify();
				}
			}
			/*try {
				sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
	}
}
