package csx55.threads.wireformat;



//event factory for all the things
public class EventFactory implements Protocol{

    public static Event createEvent(int Protocol) {
        switch (Protocol) {
            case REGISTRATION_REQUEST:
                return new Register();
            case REGISTRATION_RESPONSE:
                return new RegisterResponse();
            case START_ROUNDS:
                return new StartRounds();
            case ANOTHER_NODE:
                return new AnotherNode();
            case MESSAGE_NODES_LIST:
                return new MessagingNodesList();
            case NODE_LOAD_REPORT:
                return new nodeLoadReport();
            case TASK_SEND:
                return new SendTask();

            case FINISHED_ROUNDS:
                return new TaskComplete();

            case TRAFFIC_SUMMARY:
                return new TrafficSummary();
            case RETRIEVE_TRAFFIC:
                return new RetrieveTraffic();

            case ACK:
                return new Ack();
            case REQUEST_INFO:
                return new RequestInfo();

            case NODE_DONE:
                return new NodeDone();
            case LOAD_REQ_RESPOND:
                return new LoadReqRespond();

            case READY_WORK:
                return new ReadyWork();
       

            
            // Add cases for other types as needed
            default:
                throw new IllegalArgumentException("Unknown event type: " + Protocol);
                //break;
        }
    }
    
   
}
