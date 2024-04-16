package csx55.chord.wireformat;



//event factory for all the things
public class EventFactory implements Protocol{

    public static Event createEvent(int Protocol) {
        switch (Protocol) {
            case REGISTRATION_REQUEST:
                return new Register();
            case REGISTRATION_RESPONSE:
                return new RegisterResponse();
            case contactPEERINITIAL:
                return new contactPeerInitial();
            case CONTACTPEERRESPONSE:
                return new contactPeerResponse();
            case NOTIFYNEIGHBOR:
                return new notifyNeighbor();
            case REQUESTTABLEINFO:
                return new RequestTableInfo();
            case RESPONSETABLEINFO:
                return new ResponseTableInfo();
            case VALIDATEFINGER:
                return new validateFinger();
            case VALIDATEFORWARD:
                return new validateForward();
            case VALIDATEFINGERRESPONSE:
                return new validateFingerResponse();
            case VALIDATEFORWARDRESPONSE:
                return new validateForwardResponse();

            case NEWPEER:
                return new newPeer();

            case REQUESTHASHlOCATION:
                return new requestHashLocation();
            case REQUESTHASHRESPONSE:
                return new requestHashResponse();
               

            case GIVEFINGERTABLE:
                return new giveFingerTable();
       
            case ACK:
                return new Ack();

            
            // Add cases for other types as needed
            default:
                throw new IllegalArgumentException("Unknown event type: " + Protocol);
                //break;
        }
    }
    
   
}
