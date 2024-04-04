package csx55.threads;

import java.util.Scanner;
import csx55.threads.Node.*;

//This is our registry class, it calls commands to the registryNode. Its pretty light

public class Registry {

    //private String hostname = null;
    //our port
    private Integer port = null;
    //our name, always register, not used
    private String name = "Register";
    //my node so i can do stuff
    RegisterNode myNode = null;
    //thread of the node, to keep track
    Thread nodeThread = null;

    //main is main, starts on start
    public static void main(String[] args) {
        //System.out.println(args);
        if (args.length < 1){
            System.out.println("Error, no args specified");
            System.exit(0);
        }
        //System.out.println("Registry Started");
        

        //Registry will run a reciever node
        Registry reg = new Registry();

        reg.port = Integer.parseInt(args[0]);

        try {
            RegisterNode node = new RegisterNode(reg.port, reg.name);
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
                    registerCommandHandler(cmd, reg);
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


    //register command handler, so our messages and such get through
    public static void registerCommandHandler(String command, Registry reg){
        String[] commandBroken = command.split("\\s+");
        
        switch (commandBroken[0].toString()){
            case "list-messaging-nodes":
                reg.myNode.listNodes();
                break;
            
            case "send-overlay-link-weights":
                reg.myNode.sendOverlay();
                break;

            case "list-weights":
                reg.myNode.showWeights();
                break;

            case "setup-overlay":
                if (commandBroken.length != 2){
                    //System.out.println("Error, no number given for connection limit");
                }
                else{
                    reg.myNode.setupOverlay(Integer.parseInt(commandBroken[1]));
                }
                break;

            case "start":
                if (commandBroken.length != 2){
                    //System.out.println("Error, no number given for rounds");
                }
                else{
                    reg.myNode.startRounds(Integer.parseInt(commandBroken[1]));
                }
                break;
            
            default:
                //System.out.println("Unknown command");
        }


    }


    
}
