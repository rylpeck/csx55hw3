package csx55.chord;

import csx55.chord.Node.peerNode;
import csx55.chord.Node.Node;


import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;


public class peer {

    private peerNode myNode;
    private Thread nodeThread = null;

    public static void main(String[] args) throws IOException, InterruptedException {
        
        if (args.length < 1){
            //ln("Error, no args specified");
            System.exit(0);
        }
        if (args.length < 2){
            //System.out.println("Error, no port selected");
            System.exit(0);
        }

        peer myCompute  = new peer();
        //System.out.println("Peer starting");

        //make a mess, to use it later
        
        //parse arguments
        int regPortNumber = Integer.parseInt(args[1]); 
        String regHostname = (args[0]); 

        

        String name = "MessagingNode"; 
        try {

            //This starts the listening node
            int myPortNumber = myPort();
            myCompute.myNode = new peerNode(name, myPortNumber, regHostname, regPortNumber);
           
            myCompute.nodeThread = new Thread(myCompute.myNode);
            myCompute.nodeThread.start();
            
            //cmdlineThread.start();
            Scanner scanner = new Scanner(System.in);
            Boolean scan = true;

            //System.out.print("Command Awaiting: ");
            while(scan){
                try{
                    String cmd = scanner.nextLine();
                    //System.out.println("Command Recieved: " + cmd);
                    clientCommandHandler(cmd, myCompute.myNode);
                    if (cmd.equals("exit")){
                        scan = false;
                    }
                    //System.out.print("Command Awaiting: ");
                    
                }
                catch (Exception e){
                    
                }
                
            }
            //System.out.println("Past loop");

            //node.shutdown();
            //nodeThread.join();            
            scanner.close();

            //System.out.println("Closing");
           


            //TODO make this its own thread, lets do that tomorrow

            //node.run();
            //Todo clean shut down

            
        } catch (Error e) {
            e.printStackTrace();
            //serverSocket.close();
        }

        
    }

    public static int myPort(){
        //point of this method is to onstartup, determine what our port will be. Our low bar will be 22600, arbitrary choice

            try (ServerSocket serverSocket = new ServerSocket(0)) {
                return serverSocket.getLocalPort();
            } catch (IOException e) {
                
            }
        //}
        throw new IllegalStateException("No free port found within the range, contact your admin");
        

    }

    public static void clientCommandHandler(String command, peerNode node){
        String[] commandBroken = command.split("\\s+");
        
        switch (commandBroken[0].toString()){
            case "Cm":
                //System.out.println("CM");
                node.showConnections();
                break;
            case "finger-table":
                node.printFingerTable();
                break;
            case "validate":
                node.doValidation();
                break;
            case "neighbors":
                node.printNeighbors();
                break;
            case "upload":
                node.sendFile(commandBroken[1]);
                break;
            case "download":
                node.downloadFile(commandBroken[1]);
                break;
            case "files":
                node.printFiles();
                break;

            case "exit":
                node.exitGracefully();
                System.exit(3);
                //deregister(mess);
                break;
        }

    }








    
}
