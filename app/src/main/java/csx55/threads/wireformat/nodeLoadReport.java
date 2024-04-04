package csx55.threads.wireformat;

//format for polltraffic is as follows
//ip then load
//etc etc move forward
//we check the first slot to see if its us, or pass along

class nodeLoadReport implements Event, Protocol{

    private String[][] data;

    @Override
    public int getType(){
        //Message is type 0, see protocol.java for more details
        return NODE_LOAD_REPORT;
    }

    @Override
    public byte[] getBytes(){
        byte[] marshalledBytes = null;
        Event access = new nodeLoadReport();
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

    //Method to simply give data out and process it. Has its own format, but is nicely usable, and generic across all built event classes. 
    @Override
    public String[][] giveData(){
        //Message will give data differently of course
        return this.data;
    };
    
    
}
