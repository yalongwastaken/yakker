/**
 * @file YakChannel.java
 * @brief Server-side channel for Yakker.
 *        Accepts client connections, validates handshakes, and broadcasts
 *        messages to all connected clients via a blocking message queue.
 */
package yakker;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class YakChannel {

    // constants
    private static final String SECRET = "3c3c4ac618656ae32b7f3431e75f7b26b1a14a87";

    // state
    private List<YakConnectedClient> clients      = new ArrayList<>();
    private BlockingQueue<String>    messageQueue = new LinkedBlockingQueue<>();
    private ServerSocket             serverSocket;
    private Thread                   sendMessageThread;

    // entry point
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        YakChannel channel = new YakChannel(port);
        channel.serve();
    }

    // constructor
    public YakChannel(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // accepts incoming connections and validates handshake
    public void serve() {
        sendMessageThread = new Thread(this::runSendMessageFromQueueToAll);
        sendMessageThread.start();

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();

                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String secret     = reader.readLine();
                String secretText = reader.readLine();

                boolean validHandshake = secret.equals("SECRET")
                        && secretText.equals(SECRET)
                        && reader.readLine().equals("NAME");

                if (validHandshake) {
                    String username = reader.readLine();
                    YakConnectedClient client = new YakConnectedClient(clientSocket, username);
                    addClient(client);
                    client.start();
                    manageClientList();
                } else {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // runs continuously on a background thread, draining the message queue
    private void runSendMessageFromQueueToAll() {
        while (true) {
            sendMessageFromQueueToAll();
        }
    }

    // adds a client to the active client list
    private void addClient(YakConnectedClient client) {
        clients.add(client);
    }

    // enqueues a message to be broadcast to all clients
    private void enqueueMessage(String message) {
        if (message == null) return;
        try {
            messageQueue.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // drains the message queue and sends each message to all connected clients
    private void sendMessageFromQueueToAll() {
        while (!messageQueue.isEmpty()) {
            try {
                String message = messageQueue.take();
                for (YakConnectedClient client : clients) {
                    client.sendMessage(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // broadcasts the current client list to all connected clients
    private void sendClientListToAll() {
        String clientList = getClientList();
        for (YakConnectedClient client : clients) {
            client.sendMessage(clientList);
        }
    }

    // removes disconnected clients and broadcasts the updated list
    public void manageClientList() {
        Iterator<YakConnectedClient> iterator = clients.iterator();
        while (iterator.hasNext()) {
            YakConnectedClient client = iterator.next();
            if (!client.isConnected()) {
                iterator.remove();
            }
        }
        sendClientListToAll();
    }

    // returns the client list as a formatted string
    public synchronized String getClientList() {
        StringBuilder clientList = new StringBuilder("START_CLIENT_LIST\n");
        for (YakConnectedClient client : clients) {
            clientList.append(client.getClientName()).append("\n");
        }
        clientList.append("END_CLIENT_LIST");
        return clientList.toString();
    }

    // manages an individual connected client and its read loop
    private class YakConnectedClient extends Thread {

        // state
        private Socket        sock        = null;
        private PrintWriter   writer      = null;
        private BufferedReader in         = null;
        private String        name        = "";
        private boolean       isConnected = true;

        // constructor
        public YakConnectedClient(Socket s, String n) {
            sock = s;
            name = n;
        }

        // sends a message over the network connection
        public void sendMessage(String message) {
            try {
                writer = new PrintWriter(sock.getOutputStream(), true);
                writer.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // returns whether the client is still connected
        public boolean isConnected() {
            return isConnected;
        }

        // returns the client's username
        public String getClientName() {
            return name;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                while (true) {
                    String message = in.readLine();
                    if (message == null) break;
                    enqueueMessage("[" + name + "] " + message);
                }

                // client disconnected — clean up
                isConnected = false;
                writer.close();
                in.close();
                sock.close();
                manageClientList();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}