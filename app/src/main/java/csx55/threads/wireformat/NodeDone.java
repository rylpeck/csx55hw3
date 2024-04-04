package csx55.threads.wireformat;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Instant;

//deregister request from node, to register
public class NodeDone implements Event, Protocol{

    private String [][] data;

    @Override
    public int getType(){
        //Dereg is type 3, see protocol.java for more details
        return NODE_DONE;
    }


    @Override
    public byte[] getBytes(){
        byte[] marshalledBytes = null;
        Event access = new NodeDone();
        marshalledBytes = access.gatherData(data);
        return marshalledBytes;
    }

   
    @Override
    public void setData(String Message){
        //This allows us to turn the message, back into this
        String[] commandBroken = Message.split("\\s+");

        //putting a cheeky 2 here allows us to sheer off the first entry, no matter what. 
        //edit nvm
        
        String[][] newdata = new String[][]{{commandBroken[0], commandBroken[1]}};
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
