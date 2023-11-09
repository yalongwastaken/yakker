import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Iterator;

public class GWackChannel {
    private static String SECRET = "3c3c4ac618656ae32b7f3431e75f7b26b1a14a87";
    private static List<GWackConnectedClient> clients = new ArrayList<>();
    private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    private ServerSocket serverSocket;
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
            e.printStackTrace();
        }
    }

    public void serve() {
        while (true) {
            try {
                // Accept incoming client connections
                Socket clientSocket = serverSocket.accept();
        
                // Create a reader to receive data from the client
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String secret = reader.readLine();
                String secretText = reader.readLine();
        
                // Check if the client's request is valid
                if (secret.equals("SECRET") && secretText.equals(SECRET) && reader.readLine().equals("NAME")) {
                    String username = reader.readLine();
        
                    // Start a new thread to handle the client's interactions
                    GWackConnectedClient connectedClient = new GWackConnectedClient(clientSocket, username);
                    Thread clientHandler = new Thread(connectedClient);
                    addClient(connectedClient);
                    clientHandler.start();

                    // Get the updated client list
                    String clientList = getClientList();
        
                    // Create a writer to send the client list back to the client
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                    writer.println(clientList);
                } 
                else {
                    // If the request is not valid, close the client socket
                    System.out.println("something went wrong");
                    clientSocket.close();
                }
            }
            catch (IOException e) {
            }

            // sending and interacting with all the clients
            try {
                manageClientList();
                sendMessageFromQueueToAll();
            }
            catch (Exception e) {
            }
        }
    }

    private void addClient(GWackConnectedClient client) {
        clients.add(client);
    }

     // Enqueue a message to be sent to all clients
     private void enqeueMessage(String message) {
        try {
            // Enqueue the message to the BlockingQueue
            messageQueue.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void dequeueAll() {
        messageQueue.clear();
    }

     // A separate thread to handle sending messages from the queue to all clients
     private void sendMessageFromQueueToAll() {
        while (true) {
            try {
                // Dequeue a message from the BlockingQueue
                String message = messageQueue.take();

                // Send the message to all connected clients
                if (message != null) {
                    for (GWackConnectedClient client : clients) {
                        client.sendMessage(message);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void manageClientList() {
        Iterator<GWackConnectedClient> iterator = clients.iterator();
        while (iterator.hasNext()) {
            GWackConnectedClient client = iterator.next();
            if (!client.getConnection()) {
                iterator.remove();
            }
        }
    }

    // return a list of the currently connected clients
    public synchronized String getClientList() {
        String clientList = "START_CLIENT_LIST\n";
        for (GWackConnectedClient client: clients) {
            clientList += client.getClientName() + "\n";
        }
        clientList += "END_CLIENT_LIST";

        return clientList;
    }

    private class GWackConnectedClient extends Thread {
        private Socket sock = null;
        private String name = "";
        private boolean connected = true;

        public GWackConnectedClient(Socket s, String n) {
            sock = s;
            name = n;
        }

        public void sendMessage(String message) {
            try {
                PrintWriter writer = new PrintWriter(sock.getOutputStream(), true);
                writer.println(message);
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean isValid() {
            return !sock.isClosed();
        }

        public String getClientName() {
            return name;
        }

        private void clientDisconnect() {
            connected = false;
        }

        public boolean getConnection() {
            return connected;
        }

        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
    
                // Add code to continuously read messages from the client and process them
                while (true) {
                    String message = reader.readLine();
                    enqeueMessage(message);

                    // user disconnects
                    if (!isValid()) {
                        break;
                    }
                }
    
                // If the client disconnects, remove them from the clients map
                clientDisconnect();
                sock.close();
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
