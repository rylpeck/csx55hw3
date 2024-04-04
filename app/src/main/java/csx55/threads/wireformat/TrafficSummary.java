package csx55.threads.wireformat;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;

import java.io.DataOutputStream;
import java.io.IOException;

import java.time.Instant;


//last thing sent, traffic sent from node to register, its happy
public class TrafficSummary implements Event, Protocol{


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
        //Message is type 0, see protocol.java for more details
        return TRAFFIC_SUMMARY;
    }


   
    @Override
    public void setData(String Message){
        //This allows us to turn the message, back into this
        String[] commandBroken = Message.split("\\s+");

        //putting a cheeky 2 here allows us to sheer off the first entry, no matter what. 
        //edit nvm
        String[] newArray = deepCopyArray(commandBroken, 1, (commandBroken.length));
        
        String[][] newdata = new String[][]{{commandBroken[0]}, newArray};
        this.data = newdata;
    }

    @Override
    public byte[] getBytes(){
        byte[] marshalledBytes = null;
        Event access = new TrafficSummary();
        marshalledBytes = access.gatherData(data);
        return marshalledBytes;
    }


    //Method to simply give data out and process it. Has its own format, but is nicely usable, and generic across all built event classes. 
    @Override
    public String[][] giveData(){
        //Message will give data differently of course
        return this.data;
    };
      
    
}
