package csx55.chord.wireformat;



//event factory for all the things
public class EventFactory implements Protocol{

    public static Event createEvent(int Protocol) {
        switch (Protocol) {
            case REGISTRATION_REQUEST:
                return new Register();
            case REGISTRATION_RESPONSE:
                return new RegisterResponse();
       
            case ACK:
                return new Ack();

            
            // Add cases for other types as needed
            default:
                throw new IllegalArgumentException("Unknown event type: " + Protocol);
                //break;
        }
    }
    
   
}
