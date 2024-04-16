package csx55.chord.util;

import csx55.chord.Node.peerNode;

public class fingerUpdater implements Runnable{

    private int waittime = 0;
    private peerNode parent = null;

    public fingerUpdater(int waitTime, peerNode parent){
        this.waittime = waitTime;
        this.parent = parent;
    }

    public void run(){
       
        while (true) {
            
            try {
                // Sleep for 1 minute
                Thread.sleep(this.waittime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //this.parent.validateForward();
            //this.parent.validateRandomFinger();
            
        }
        

    
       // do stuff
    }

    
}
