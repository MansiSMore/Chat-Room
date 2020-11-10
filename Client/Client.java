import java.util.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;

public class Client {
    private String server, username;
    private int port = 1500;

    private Socket             clientsocket;
    private ObjectInputStream  sInput;
    private ObjectOutputStream sOutput;

    private ArrayList<String> chatList;
    private clientside1 cg;
    int temp=1;
    
    public Client(String server, int port, String username, clientside1 cg ) 
    {
        this.server   = server;
        this.port     = port;
        this.username = username;
        this.cg = cg;
        
        chatList = new ArrayList<String>();
    }

        private void displayEvent(String msg) 
    {
        chatList.add(msg);
        cg.appendEvent(msg);
    }

        private void displayMsg(String msg)
    {
        chatList.add(msg);
        cg.appendRoom(msg);     // append in the room window
    }

    private void chatSave() 
    {
        try
        {
            PrintStream print =
                new PrintStream(new FileOutputStream("client_log.txt", true));
            for (String str:chatList)
                print.println(str);
            print.close();
        }
        catch (IOException e) 
        {
            // do nothing
        }
    }

    public void disc()
    {
        sendMessage("LOGOUT");
        chatSave();
        
    }
    
    public boolean run() 
    {
        //Try to connect to server
        try {
            clientsocket = new Socket(server, port);
        } 
        // if it failed not much I can so
        catch(Exception ec) {
            displayEvent("Error connectiong to server:" + ec+"\n");
            return false;
        }
        String msg = "\nConnection accepted " + clientsocket.getInetAddress() + ":" + clientsocket.getPort() + "\n";
        displayEvent(msg);

        // Creating both Data Stream
        try {
            sInput  = new ObjectInputStream(clientsocket.getInputStream());
            sOutput = new ObjectOutputStream(clientsocket.getOutputStream());
        } catch (IOException eIO) {
            displayEvent("\nException creating new Input/output Streams: " + eIO+"\n");
            return false;
        }

        // creates the Thread to listen from the server 
        new RunClientThread().start();

        // Send our username to the server this is the only message that we
        // will send as a String. All other messages will be ChatMessage objects
        try 
        {
            sOutput.writeObject(username);
        }
        catch (IOException eIO)
        {
            displayEvent("\nException doing login : " + eIO+"\n");
            disconnect();
            return false;
        }
        // success we inform the caller that it worked
        return true;
    }
    
    // some problem over here.
    class RunClientThread extends Thread
    {
        public void run()
        {
                while(true)
                {
                    try
                    {
                        String msg = (String) sInput.readObject();
                        displayEvent(msg);
                    } 
                    catch(IOException e)
                    {
                        disconnect();
                        displayEvent("\nServer has closed the connection: \n");
                        break;
                     }
                    // can't happen with a String object but need the catch anyhow
                    catch(ClassNotFoundException e2){}
                }
        }
    }

    
    void sendMessage(String msg) 
    {
        try 
        {
            sOutput.writeObject(msg);
        } 
        catch(IOException e) 
        {
            displayEvent("\nException writing to server: " + e+"\n");
        }
    }

    /*
     * When something goes wrong
     * Close the Input/Output streams and disconnect
     * not much to do in the catch clause
     */
    private void disconnect() 
    {
        try
        { 
            if (sInput != null) 
                sInput.close();
        
            if (sOutput != null) 
                sOutput.close();
        
            if (clientsocket != null) 
                clientsocket.close();
        }
        catch(Exception e)
        {
            // do nothing
        } 

        //!!!Note - You may notify your GUI when closed.
        cg.appendEvent("Logged out Successfully \n");
        
    }
           
}
