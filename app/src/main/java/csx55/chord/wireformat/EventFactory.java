package csx55.chord.wireformat;



//event factory for all the things
public class EventFactory implements Protocol{

    public static Event createEvent(int Protocol) {
        switch (Protocol) {
            
       

            
            // Add cases for other types as needed
            default:
                throw new IllegalArgumentException("Unknown event type: " + Protocol);
                //break;
        }
    }
    
   
}
