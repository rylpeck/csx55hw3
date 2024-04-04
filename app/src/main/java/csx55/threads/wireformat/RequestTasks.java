package csx55.threads.wireformat;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;

import java.io.DataOutputStream;
import java.io.IOException;

import java.time.Instant;


//is a message betwene nodes, holds the little data
class RequestTask implements Event, Protocol{


    //Whats in message
    //Message = random int
    //Routing plan[] = Route made on intiialization.
    //All data is stored in data[][]
    //format as followed
    //data[0][] = Message
    //data[1][] = each spot is a different node int he route (pre discovered)
    //Note, route wil be counted donw step by step

    //For ease of access, data can be taken out of the object iwth getData[] all in one string.

    private String[][] data;

    @Override
    public int getType(){
        
        return REQUEST_INFO;
    }

    @Override
    public byte[] getBytes(){
        byte[] marshalledBytes = null;
        Event access = new RequestTask();
        marshalledBytes = access.gatherData(data);
        return marshalledBytes;
    }

  

    @Override
    public void setData(String Message){
        //This allows us to turn the message, back into this
        String[] commandBroken = Message.split("\\s+");

        String[] newArray = deepCopyArray(commandBroken, 1, (commandBroken.length));
        
        String[][] newdata = new String[][]{{commandBroken[0]}, newArray};
        this.data = newdata;
    }

    public void setData(String[][] data){

        this.data = data;


    }



    //Method to simply give data out and process it. Has its own format, but is nicely usable, and generic across all built event classes. 
    @Override
    public String[][] giveData(){
        //Message will give data differently of course
        return this.data;
    };
      
    
}
