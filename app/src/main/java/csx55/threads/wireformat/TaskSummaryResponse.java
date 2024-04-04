package csx55.threads.wireformat;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;

import java.io.DataOutputStream;
import java.io.IOException;

import java.time.Instant;
//request from reg, to nodes, to give summarization
class taskSummaryRequest implements Event, Protocol{

    private String [][] data;
    @Override
    public int getType(){
        
        return TRAFFIC_SUMMARY;
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
        Event access = new taskSummaryRequest();
        marshalledBytes = access.gatherData(data);
        return marshalledBytes;
    }

    @Override
    public void setData(String Message){
        //This allows us to turn the message, back into this
        //number of nodes, then connections
        String[] commandBroken = Message.split("\\s+");

        String[] newArray = deepCopyArray(commandBroken, 1, (commandBroken.length));
        
        String[][] newdata = new String[][]{{commandBroken[0]}, newArray};
        this.data = newdata;
    }
     
}
