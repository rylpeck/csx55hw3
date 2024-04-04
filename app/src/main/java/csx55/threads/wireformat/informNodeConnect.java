package csx55.threads.wireformat;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;

import java.io.DataOutputStream;
import java.io.IOException;

import java.time.Instant;

//message informing what nodes to be connected to which other node, from register to node.
class informNodesConnect implements Event, Protocol{

    private String [][] data;
    @Override
    public int getType(){
        //Registration is type 1, see protocol.java for more details
        return INFORM_NODES_MAP;
    }

    
    @Override
    public void setData(String [][] message){
        //This allows us to turn the message, back into this
        //IP, then port of node we are registering
        this.data = new String[message.length][message[0].length];
        deepCopyDouble(message, this.data);            
       
    }

    

    //Method to simply give data out and process it. Has its own format, but is nicely usable, and generic across all built event classes. 
    @Override
    public String[][] giveData(){
        return this.data;
    }

    @Override
    public byte[] getBytes(){
        byte[] marshalledBytes = null;
        Event access = new informNodesConnect();
        marshalledBytes = access.gatherData(data);
        return marshalledBytes;
    }

    @Override
    public void setData(String arguments) {
        //First thing we do is allocate
        //This is for the other end when its already a string
        
        String[] Broken = arguments.split("\\s+");
        int lengthy = Broken.length/3;

        this.data = new String[lengthy][3];

        int index = 0;

        for (int i = 0; i < Broken.length; i += 3) {
            System.arraycopy(Broken, i, data[index], 0, 3);
            index++;
        }

        
        //this.data = new String[lengthy][3];

        // TODO 
        
    };
     
}
