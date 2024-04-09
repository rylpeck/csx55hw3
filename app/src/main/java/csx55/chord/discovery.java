package csx55.chord;

import java.util.Scanner;
import csx55.chord.Node.*;

public class discovery {

    private Integer port = null;
    //our name, always register, not used
    private String name = "Register";
    //my node so i can do stuff
    discoveryNode myNode = null;
    //thread of the node, to keep track
    Thread nodeThread = null;

    public static void main(String[] args) {
        

        //args are as follows
        //give the port so we can run and have it and be happy
        if (args.length < 1){
            System.out.println("Error, no args specified");
            System.exit(0);
        }
        //System.out.println("Registry Started");

        //Registry will run a reciever node
        discovery reg = new discovery();

        reg.port = Integer.parseInt(args[0]);

        try {
            discoveryNode node = new discoveryNode(reg.port, reg.name);
            reg.myNode = node;
            //our register node has started, and we will now get connections
            reg.nodeThread = new Thread(node);
            reg.nodeThread.start();

            Scanner scanner = new Scanner(System.in);
            Boolean scan = true;

            //System.out.print("Command Awaiting: ");
            while(scan){
                try{
                    String cmd = scanner.nextLine();
                    //System.out.println("Command Recieved: " + cmd);
                    discoveryCommandHandler(cmd, reg);
                    if (cmd.equals("exit")){
                        scan = false;
                    }
                    //System.out.print("Command Awaiting: ");
                    
                }
                catch (Exception e){
                    
                }
                
            }
            //System.out.println("Past loop");

        }
        catch (Exception e){

        }

    }

    public static void discoveryCommandHandler(String command, discovery reg){
        String[] commandBroken = command.split("\\s+");
        
        switch (commandBroken[0].toString()){
            case "list-messaging-nodes":
                reg.myNode.listNodes();
                break;
            
            case "send-overlay-link-weights":
                //reg.myNode.sendOverlay();
                break;

            case "list-weights":
                //reg.myNode.showWeights();
                break;

            case "setup-overlay":
                
                break;

            case "start":
                
                break;
            
            default:
                //System.out.println("Unknown command");
        }


    }
    
}
