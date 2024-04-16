package csx55.chord.wireformat;

//our protocols for events
public interface Protocol {
    int REGISTRATION_REQUEST = 0;
    int REGISTRATION_RESPONSE = 1;
    int MESSAGE = 2;
    int DEREGISTRATION_REQUEST = 3;
    int DEREGISTRATION_RESPONSE = 4;
    int INFORM_NODES_MAP = 5;
    int OVERLAY_INFORMATION = 6;
    int START_ROUNDS = 7;
    int ANOTHER_NODE = 8;
    int FINISHED_ROUNDS = 9;
    int MESSAGE_NODES_LIST = 10;
    int RETRIEVE_TRAFFIC = 11;
    int TRAFFIC_SUMMARY = 12;
    int NODE_LOAD_REPORT = 13;

    int REQUEST_INFO = 18;
    int ACK = 99;

    int READY_WORK = 35;

    int contactPEERINITIAL = 56;
    int CONTACTPEERRESPONSE = 57;
    int NOTIFYNEIGHBOR = 58;
    int GIVEFINGERTABLE = 59;
    int REQUESTTABLEINFO = 60;
    int RESPONSETABLEINFO = 61;
    int VALIDATEFINGER = 62;
    int VALIDATEFORWARD = 63;
    int VALIDATEFINGERRESPONSE = 64;
    int VALIDATEFORWARDRESPONSE = 65;

    int LOAD_REQ_RESPOND = 66;

    int NEWPEER = 67;
    int REQUESTHASHlOCATION = 68;
    int REQUESTHASHRESPONSE = 69;

    


    int NODE_DONE = 34;

    int TASK_SEND = 14;
}


    