package csx55.threads.wireformat;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;

import java.io.DataOutputStream;
import java.io.IOException;

import java.time.Instant;

//registers a node to the register
class Register implements Event, Protocol{

    private String [][] data = null;

    @Override
    public int getType(){
        //Registration is type 1, see protocol.java for more details
        return REGISTRATION_REQUEST;
    }

    

    @Override
    public void setData(String Message){
        //This allows us to turn the message, back into this
        //IP, then port of node we are registering
        String[] commandBroken = Message.split("\\s+");

        String[] newArray = deepCopyArray(commandBroken, 0, (commandBroken.length));
        
        String[][] newdata = new String[][]{newArray};
        this.data = newdata;
    }


    @Override
    public byte[] getBytes(){
        byte[] marshalledBytes = null;
        Event access = new Register();
        marshalledBytes = access.gatherData(data);
        return marshalledBytes;
    }


    //Method to simply give data out and process it. Has its own format, but is nicely usable, and generic across all built event classes. 
    @Override
    public String[][] giveData(){
        
        return this.data;
    };



      
}
