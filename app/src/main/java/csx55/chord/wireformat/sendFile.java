package csx55.chord.wireformat;

//registers a node to the register
class sendFile implements Event, Protocol{

    private String [][] data = null;

    @Override
    public int getType(){
        //Registration is type 1, see protocol.java for more details
        return SENDFILE;
    }

    

    @Override
    public void setData(String Message){
        //This allows us to turn the message, back into this
        //IP, then port of node we are registering
        String[] commandBroken = Message.split("\\s+");

        String[] newArray = deepCopyArray(commandBroken, 1, (commandBroken.length));
        
        String[][] newdata = new String[][]{ { commandBroken[0] }, newArray };
        this.data = newdata;
    }


    @Override
    public byte[] getBytes(){
        byte[] marshalledBytes = null;
        Event access = new sendFile();
        marshalledBytes = access.gatherData(data);
        return marshalledBytes;
    }


    //Method to simply give data out and process it. Has its own format, but is nicely usable, and generic across all built event classes. 
    @Override
    public String[][] giveData(){
        
        return this.data;
    };



      
}
