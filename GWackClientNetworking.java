import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GWackClientNetworking {
    private GWackClientFrame GUI;
    private boolean connection;
    private String membersList = "";
    private String messages = "";
    private ReadingThread readingThread;

    // constructor
    public GWackClientNetworking(GWackClientFrame GUI) {
        this.GUI = GUI;
        connection = true;
    }

    // disconnect logic (close socket and clear data)
    public void disconnect() {
        closeSocket();

        connection = false;
        membersList = "";
        messages = "";
        GUI.reset();
    }

    // ensures the socket closes
    public void closeSocket() {
        try {
            if (readingThread != null && readingThread.sock != null && !readingThread.sock.isClosed()) {
                readingThread.sock.close();
                //System.out.println(readingThread.sock.isClosed());
            }
        } catch (IOException e) {
        }
    }

    // connect method for handling networking operations
    public void connect(String name, String ip, int port) {
        connection = true;
        readingThread = new ReadingThread(name, ip, port);
        readingThread.start();
    }

    public boolean isConnected() {
        return connection;
    }

    // sends text to readingThread where it is properly managed
    public void sendMessage(String message) {
        readingThread.newMessage(message);
    }

    // updates members list with information from server
    private void updateMembersList(String message, String membersList) {
        GUI.updateAll(messages, membersList);
    }

    private class ReadingThread extends Thread {
        boolean insideMembersList = false;
        String name;
        String ip;
        int port;
        PrintWriter writer;
        Socket sock;
        BufferedReader in;

        // constructor
        public ReadingThread(String name, String ip, int port) {
            this.name = name;
            this.ip = ip;
            this.port = port;
        }

        public void run() {
            try {
                // Create a socket and connect to the server
                sock = new Socket(ip, port);

                // Handshake
                writer = new PrintWriter(sock.getOutputStream(), true);
                writer.println("SECRET");
                writer.println("3c3c4ac618656ae32b7f3431e75f7b26b1a14a87");
                writer.println("NAME");
                writer.println(name);
                GUI.setButtonState(true);

                in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                startReading();
            } 
            catch (Exception e) {
                GUI.errorMessage();
            }
        }

        // method that constantly reads text from the server while the user is connected
        private void startReading() {
            try {
                while (connection) {
                    updateMembersList(messages, membersList);
                    getServerText();
                }

                // Close the connections
                writer.close();
                in.close();
                sock.close();
            } 
            catch (IOException e) {
            }
        }
        
        // get text from the server and update appropriate fields accordingly
        private void getServerText() {
            try {

                String line;
                line = in.readLine();

                if (line != null) {
                    if (line.equals("START_CLIENT_LIST")) {
                        membersList = "";
                        insideMembersList = true;
                    }
                    else if (line.equals("END_CLIENT_LIST")) {
                        insideMembersList = false;
                    }
                    else if (insideMembersList) {
                        membersListText(line);
                    }
                    else {
                        messageText(line);
                    }
                }
            }
            catch (Exception e) {
            }
        }

        // if text is client list
        private void membersListText(String message) {
            if (!message.equals("START_CLIENT_LIST") && !message.equals("END_CLIENT_LIST") && !message.equals(null)) {
                membersList += message + "\n";
            }
        }

        // if text is not client list
        private void messageText(String message) {
            messages += message + "\n";
        }

        // user sends message to server
        private void newMessage(String message) {
            if (writer != null) {
                writer.println(message);
            }
        }
    }
}
