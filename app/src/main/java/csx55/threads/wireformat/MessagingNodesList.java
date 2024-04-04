package csx55.threads.wireformat;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;

import java.io.DataOutputStream;
import java.io.IOException;

import java.time.Instant;

//messages nodes the list of things todo
class MessagingNodesList implements Event, Protocol{

    private String [][] data;
    @Override
    public int getType(){
        //Registration is type 1, see protocol.java for more details
        return MESSAGE_NODES_LIST;
    }

    @Override
    public void setData(String [][] message){
        //This allows us to turn the message, back into this
        //IP, then port of node we are registering
        this.data = new String[message.length][message[0].length];
        deepCopyDouble(message, this.data);            
       
    }


    @Override
    public byte[] getBytes(){
        byte[] marshalledBytes = null;
        Event access = new MessagingNodesList();
        marshalledBytes = access.gatherData(data);
        return marshalledBytes;
    }


    

    //Method to simply give data out and process it. Has its own format, but is nicely usable, and generic across all built event classes. 
    @Override
    public String[][] giveData(){
        return this.data;
    }

    @Override
    public void setData(String Message){
        //This allows us to turn the message, back into this
        //number of nodes, then connections
        String[] commandBroken = Message.split("\\s+");
       
        
        String[][] newdata = new String[][]{{commandBroken[0]}, {commandBroken[1]}, {commandBroken[2]}, {commandBroken[3]}};
        this.data = newdata;
    }
     
}
