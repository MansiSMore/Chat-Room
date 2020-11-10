
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {
    // a unique ID for each connection
    private static int uniqueId;
    // an ArrayList to keep the list of the Client
    private ArrayList<ClientThread> al;
    // to display time
    private SimpleDateFormat sdf;
    // the port number to listen for connection
    private int port;
    // the boolean that will be turned off to stop the server
    private boolean keepGoing;

    // if GUI
    private myGUI mg;
    // log chat list
    private ArrayList<String> chatList;

    /*
     *  server constructor that receives the port to listen to for connection
     *  as parameter in console
     */

    public Server(int port, myGUI mg) {
        // the port
        this.port = port;
        this.mg   = mg;
        // to display hh:mm:ss
        sdf = new SimpleDateFormat("HH:mm:ss");
        // ArrayList for the Client list
        al = new ArrayList<ClientThread>();
        //chat list
        chatList = new ArrayList<String>();
    }

    public void start() {
        keepGoing = true;
        // create socket server and wait for connection requests
        try {
            // the socket used by the server
            ServerSocket serverSocket = new ServerSocket(port);

            // infinite loop to wait for connections
            while (keepGoing) {
                // format message saying we are waiting
                displayEvent("Server waiting for Clients on port " + port + ".\n");

                Socket clientsocket = serverSocket.accept();    // accept connection
                // if I was asked to stop
                if (!keepGoing)
                    break;
                ClientThread t = new ClientThread(clientsocket);  // make a thread of it
                al.add(t);                                  // save it in the ArrayList
                t.start();
            }
            // I was asked to stop
            try {
                serverSocket.close();
                for (int i = 0; i < al.size(); ++i) {
                    ClientThread tc = al.get(i);
                    try {
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    } catch(IOException ioE) {
                        // not much I can do
                    }
                }
            } catch(Exception e) {
                displayEvent("Exception closing the server and clients: " + e);
            }
        }
        // something went bad
        catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: "
                + e + "\n";
            displayEvent(msg);
        }
    }

    /*
     * For the GUI to stop the server
     */

    protected void stop() {
        keepGoing = false;
        // connect to myself as Client to exit statement 
        try {
            new Socket("localhost", port);
        } catch(Exception e) {
            // nothing I can really do
        }
    }

    /*
     * Display an event (not a message) to the console or the GUI
     */

    private void displayEvent(String msg) {
        String e = sdf.format(new Date()) + " " + msg;
        //System.out.println(e);
        chatList.add(e);
        //display events in GUI
        mg.appendEvent(e + "\n");
    }

    /*
     * Display an event (not a message) to the console or the GUI
     */

    private void displayMsg(String msg) {
        //display events in GUI
        mg.appendRoom(msg);     // append in the room window
    }

    private void chatSave()
    {
        try{
            PrintStream print =
                new PrintStream(new FileOutputStream("log.txt", true));
            for (String str:chatList)
                print.println(str);
            print.close();
        } catch (IOException e) {
            // do nothing
        }
    }

    /*
     *  to broadcast a message to all Clients
     */

    private synchronized void broadcast(String message) {
        // add HH:mm:ss and \n to the message
        String time = sdf.format(new Date());
        String messageLf = time + " " + message + "\n";
        // display message on console or GUI
        displayMsg(messageLf);

        // we loop in reverse order in case we would have to remove a Client
        // because it has disconnected
        for (int i = al.size(); --i >= 0;) {
            ClientThread ct = al.get(i);
            // try to write to the Client if it fails remove it from the list
            if (!ct.writeMsg(messageLf)) {
                al.remove(i);
                displayEvent("Disconnected Client " + ct.username +
                    " removed from list.\n");
            }
        }
    }

    // for a client who logoff using the LOGOUT message
    synchronized void remove(int id) {
        // scan the array list until we found the Id
        for (int i = 0; i < al.size(); ++i) {
            ClientThread ct = al.get(i);
            // found it
            if (ct.id == id) {
                al.remove(i);
                return;
            }
        }
    }

    /*
     *  To run as a console application just open a console window and: 
     * > java Server
     * > java Server portNumber
     * If the port number is not specified 1500 is used
     */

    public static void main(String[] args) throws Exception {
        // start server on port 1500 unless a PortNumber is specified 
        int portNumber = 1500;
        switch(args.length) {
            case 1:
            try {
                portNumber = Integer.parseInt(args[0]);
            } catch(Exception e) {
                System.out.println("Invalid port number.");
                System.out.println("Usage is: > java Server [portNumber]");
                return;
            }
            case 0:
            break;
            default:
            System.out.println("Usage is: > java Server [portNumber]");
            return;           
        }
        // create a server object and start it
        Server server = new Server(portNumber, null);
        server.start();
    }

    // One instance of this thread will run for each client
    class ClientThread extends Thread {
        // the socket where to listen/talk
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        // my unique id (easier for deconnection)
        int id;
        // the Username of the Client
        String username;
        // the only type of message a will receive
        String msg;
        // the date I connect
        String date;

        // Constructor
        ClientThread(Socket socket) {
            // a unique id
            id = ++uniqueId;
            this.socket = socket;
            /* Creating both Data Stream */
          //  System.out.println("Thread trying to create Object Input/Output Streams");
            try {
                // create output first
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput  = new ObjectInputStream(socket.getInputStream());
                // read the username
                username = (String) sInput.readObject();
                displayEvent(username + " just connected.\n");
            } catch (IOException e) {
                displayEvent("Exception creating new Input/output Streams: " + e);
                return;
            }
            // have to catch ClassNotFoundException
            // but I read a String, I am sure it will work
            catch (ClassNotFoundException e) {
                // do nothing
            }
            date = new Date().toString() + "\n";
        }

        // what will run forever
        public void run() {
            // to loop until LOGOUT
            boolean keepGoing = true;
            while (keepGoing) {
                // read a String (which is an object)
                try {
                    msg = (String) sInput.readObject();
                } catch (IOException e) {
                    displayEvent(username + " Exception reading Streams: " + e);
                    break;              
                } catch(ClassNotFoundException e2) {
                    break;
                }

                // Switch on the type of message receive
                if (msg.equals("LOGOUT")) {
                    displayEvent(username + " disconnected with a LOGOUT message.");
                    keepGoing = false;
                } else if (msg.equals("WHOISIN")) { 
                    writeMsg("List of the users connected at " + sdf.format(new Date()) +
                        "\n");
                    // scan al the users connected
                    for (int i = 0; i < al.size(); ++i) {
                        ClientThread ct = al.get(i);
                        writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
                    }
                } else {
                    broadcast(username + ": " + msg);
                }
            }

            // remove myself from the arrayList containing the list of the
            // connected Clients
            remove(id);
            close();
        }

        // try to close everything
        private void close() {
            // try to close the connection
            try {
                if (sOutput != null)
                    sOutput.close();
            } catch(Exception e) {
                // do nothing
            }
            try {
                if (sInput != null)
                    sInput.close();
            } catch(Exception e) {
                // do nothing
            }
            try {
                if (socket != null)
                    socket.close();
            } catch (Exception e) {
                // do nothing
            }
            chatSave();
        }

        /*
         * Write a String to the Client output stream
         */

        private boolean writeMsg(String msg) {
            // if Client is still connected send the message to it
            if (!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream
            try {
                sOutput.writeObject(msg);
            }
            // if an error occurs, do not abort just inform the user
            catch(IOException e) {
                displayEvent("Error sending message to " + username);
                displayEvent(e.toString());
            }
            return true;
        }
    }
}
