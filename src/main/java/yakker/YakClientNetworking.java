/**
 * @file YakClientNetworking.java
 * @brief Networking layer for the Yakker client.
 *        Manages the TCP connection, handshake, and a background read thread
 *        that parses incoming server messages and updates the GUI.
 */
package yakker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class YakClientNetworking {

    // constants
    private static final String SECRET      = "3c3c4ac618656ae32b7f3431e75f7b26b1a14a87";
    private static final String LIST_START  = "START_CLIENT_LIST";
    private static final String LIST_END    = "END_CLIENT_LIST";

    // state
    private YakClientFrame frame;
    private boolean        connected   = false;
    private String         membersList = "";
    private String         messages    = "";
    private ReadingThread  readingThread;

    // constructor
    public YakClientNetworking(YakClientFrame frame) {
        this.frame = frame;
    }

    // initiates connection and starts the background read thread
    public void connect(String name, String ip, int port) {
        connected     = true;
        readingThread = new ReadingThread(name, ip, port);
        readingThread.start();
    }

    // closes the socket and resets all state
    public void disconnect() {
        closeSocket();
        connected   = false;
        membersList = "";
        messages    = "";
        frame.reset();
    }

    // closes the socket if open
    public void closeSocket() {
        try {
            if (readingThread != null
                    && readingThread.sock != null
                    && !readingThread.sock.isClosed()) {
                readingThread.sock.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // returns whether the client is currently connected
    public boolean isConnected() {
        return connected;
    }

    // forwards an outgoing message to the read thread's writer
    public void sendMessage(String message) {
        if (readingThread != null) {
            readingThread.sendToServer(message);
        }
    }

    // pushes the current messages and members list to the GUI
    private void refreshGUI() {
        frame.updateAll(messages, membersList);
    }

    // background thread: connects, handshakes, and reads server messages
    private class ReadingThread extends Thread {

        // state
        private String         name;
        private String         ip;
        private int            port;
        private Socket         sock;
        private PrintWriter    writer;
        private BufferedReader in;
        private boolean        insideMembersList = false;

        // constructor
        public ReadingThread(String name, String ip, int port) {
            this.name = name;
            this.ip   = ip;
            this.port = port;
        }

        @Override
        public void run() {
            try {
                sock   = new Socket(ip, port);
                writer = new PrintWriter(sock.getOutputStream(), true);
                in     = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                // send handshake
                writer.println("SECRET");
                writer.println(SECRET);
                writer.println("NAME");
                writer.println(name);

                frame.setButtonState(true);
                readLoop();
            } catch (Exception e) {
                frame.errorMessage();
            }
        }

        // reads server messages continuously until disconnected
        private void readLoop() {
            try {
                while (connected) {
                    refreshGUI();
                    readNextLine();
                }
                writer.close();
                in.close();
                sock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // reads one line from the server and routes it appropriately
        private void readNextLine() {
            try {
                String line = in.readLine();
                if (line == null) return;

                if (line.equals(LIST_START)) {
                    membersList       = "";
                    insideMembersList = true;
                } else if (line.equals(LIST_END)) {
                    insideMembersList = false;
                } else if (insideMembersList) {
                    membersList += line + "\n";
                } else {
                    messages += line + "\n";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // sends a message to the server
        public void sendToServer(String message) {
            if (writer != null) {
                writer.println(message);
            }
        }
    }
}