import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Iterator;

public class GWackChannel {
    private final String SECRET = "3c3c4ac618656ae32b7f3431e75f7b26b1a14a87";
    private List<GWackConnectedClient> clients = new ArrayList<>();
    private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private ServerSocket serverSocket;
    private Thread sendMessageThread;

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        GWackChannel channel = new GWackChannel(port);
        channel.serve();
    }

    public GWackChannel(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } 
        catch (IOException e) {
            //e.printStackTrace();
        }
    }

    public void serve() {
        // Start the threads for sendMessageFromQueueToAll
        sendMessageThread = new Thread(this::runSendMessageFromQueueToAll);
        sendMessageThread.start();

        while (true) {
            //int activeThreadCount = Thread.activeCount();
            //System.out.println("Number of active threads: " + activeThreadCount);
            try {
                // accept incoming requesets
                Socket clientSocket = serverSocket.accept();
                //System.out.println("New connection: "+clientSocket.getRemoteSocketAddress());

                // read handshake from user
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String secret = reader.readLine();
                String secretText = reader.readLine();

                // create connectedClient and send clientlist to the new connection
                if (secret.equals("SECRET") && secretText.equals(SECRET) && reader.readLine().equals("NAME")) {
                    String username = reader.readLine();
                    GWackConnectedClient connectedClient = new GWackConnectedClient(clientSocket, username);
                    addClient(connectedClient);
                    connectedClient.start();
                    manageClientList();
                } 
                // wrong handshake -> close connection
                else {
                    clientSocket.close();
                }
            }
            catch (IOException e) {
            }
        }
    }

    // seperate thread for sending messages to all
    private void runSendMessageFromQueueToAll() {
        while (true) {
            sendMessageFromQueueToAll();
        }
    }

    // add client to all clients
    private void addClient(GWackConnectedClient client) {
        clients.add(client);
    }

    // add message to queue to be sent to all connected clients
    private void enqeueMessage(String message) {
        if (message != null) {
            try {
                messageQueue.put(message);
            } 
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void dequeueAll() {
        messageQueue.clear();
    }

    // gets messages from the queue and sends them to all connected clients
    private void sendMessageFromQueueToAll() {
        while (!messageQueue.isEmpty()) {
            try {
                String message = messageQueue.take();

                if (message != null) {
                    for (GWackConnectedClient client : clients) {
                        client.sendMessage(message);
                    }
                }
            } 
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } 
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // sesnding client list to all clients
    private void sendClientListToAll() {
        String clientList = getClientList();
        for (GWackConnectedClient client : clients) {
            client.sendMessage(clientList);
        }
    }

    // removes clients if they disconnect
    public void manageClientList() {
        Iterator<GWackConnectedClient> iterator = clients.iterator();
        while (iterator.hasNext()) {
            GWackConnectedClient client = iterator.next();
            if (!client.isConnected()) {
                iterator.remove();
            }
        }

        sendClientListToAll();
    }
    
    // returns the client list in the proper format
    public synchronized String getClientList() {
        String clientList = "START_CLIENT_LIST\n";
        for (GWackConnectedClient client : clients) {
            clientList += client.getClientName() + "\n";
        }
        clientList += "END_CLIENT_LIST";
        return clientList;
    }

    // class that manages the GWackConnectedClient and its functionality
    private class GWackConnectedClient extends Thread {
        private PrintWriter writer = null;
        private Socket sock = null;
        private BufferedReader in = null;
        private String name = "";
        private boolean isConnected = true;

        // constructor
        public GWackConnectedClient(Socket s, String n) {
            sock = s;
            name = n;
        }

        // sends message over the network connection
        public void sendMessage(String message) {
            try {
                writer = new PrintWriter(sock.getOutputStream(), true);
                writer.println(message);
            } 
            catch (IOException e) {
                // e.printStackTrace();
            }
        }

        // check if the client is connected
        public boolean isConnected() {
            return isConnected;
        }

        // get name of the client
        public String getClientName() {
            return name;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                while (true) {
                    // read a message from the input stream
                    String message = in.readLine();
                    if (message == null) {
                        break; 
                    }

                    // format message with client name
                    if (message != null) {
                        message = "[" + name + "] " + message;
                        enqeueMessage(message);
                    }
                }

                // client disconnects
                isConnected = false;
                writer.close();
                in.close();
                sock.close();

                // send client list when anyone disconnects
                manageClientList();
            } 
            catch (IOException e) {
                //e.printStackTrace();
            }
            //System.out.println("Connection lost: " + sock.getRemoteSocketAddress());
        }
    }
}
