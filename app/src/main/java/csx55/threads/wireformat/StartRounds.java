package csx55.threads.wireformat;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.time.Instant;

//from register to nodes, tells them to start sending
class StartRounds implements Event, Protocol{

    private String [][] data;
    @Override
    public int getType(){
        //Registration is type 1, see protocol.java for more details
        return START_ROUNDS;
    }

    


    @Override
    public void setData(String Message){
        //This allows us to turn the message, back into this
        //IP, then port of node we are registering
       
        String[][] newdata = new String[][]{{Message}};
        this.data = newdata;
    }

    @Override
    public byte[] getBytes(){
        byte[] marshalledBytes = null;
        Event access = new StartRounds();
        marshalledBytes = access.gatherData(data);
        return marshalledBytes;
    }


    //Method to simply give data out and process it. Has its own format, but is nicely usable, and generic across all built event classes. 
    @Override
    public String[][] giveData(){
        
        return this.data;
    };



      
}
