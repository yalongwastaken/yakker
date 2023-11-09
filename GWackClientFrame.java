import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class GWackClientFrame extends JFrame {
    // text areas
    private JTextArea nameText;
    private JTextArea ipText;
    private JTextArea portText;
    private JTextArea membersText;
    private JTextArea messagesText;
    private JTextArea composeText;

    // connect button
    private JButton connect;
    private boolean connected = false;
    
    // for handling networking aspects and updating the GUI
    private GWackClientNetworking clientNetworking;

    public GWackClientFrame() {
        // Set up the main frame
        this.setTitle("GWack -- GW Slack Simulator (disconnected)");
        this.setLayout(new GridBagLayout());
        this.setResizable(true);
        this.pack();
        this.setSize(750, 400);

        // Center the frame on the screen
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        int screenWidth = defaultScreen.getDisplayMode().getWidth();
        int screenHeight = defaultScreen.getDisplayMode().getHeight();
        int frameWidth = this.getWidth();
        int frameHeight = this.getHeight();
        int x = (screenWidth - frameWidth) / 2;
        int y = (screenHeight - frameHeight) / 2;
        this.setLocation(x, y);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        clientNetworking = new GWackClientNetworking(this);
    }
    
    public void setUp() {
        setUpHeader();
        setUpActiveUsers();
        setUpTextInterface();
        setUpBot();
    }

    // Set up the header
    private void setUpHeader() {
        JPanel header = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        this.add(header, c);

         // Labels and input fields
        JLabel name = new JLabel("Name:", JLabel.CENTER);
        JLabel ip = new JLabel("IP Address:", JLabel.CENTER);
        JLabel port = new JLabel("Port:", JLabel.CENTER);;

        nameText = new JTextArea();
        nameText.setPreferredSize(new Dimension(100, 20));
        nameText.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        ipText = new JTextArea();
        ipText.setPreferredSize(new Dimension(100, 20));
        ipText.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        portText = new JTextArea();
        portText.setPreferredSize(new Dimension(75, 20));
        portText.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        // connect button
        connect = new JButton("Connect");
        ButtonActionListener buttonListener = new ButtonActionListener();
        connect.addActionListener(buttonListener);

        header.add(name);
        header.add(nameText);
        header.add(ip);
        header.add(ipText);
        header.add(port);
        header.add(portText);
        header.add(Box.createHorizontalStrut(10));
        header.add(connect);
    }

    // Set up the panel displaying active users
    private void setUpActiveUsers() {
        JPanel connectedMembers = new JPanel();
        connectedMembers.setLayout(new BoxLayout(connectedMembers, BoxLayout.Y_AXIS));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        this.add(connectedMembers, c);

        JPanel membersPanel = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        connectedMembers.add(membersPanel, c);
    
        JLabel messages = new JLabel("Active Members:");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        membersPanel.add(messages, c);
    
        membersText = new JTextArea();
        membersText.setLineWrap(true);
        membersText.setWrapStyleWord(true);
        membersText.setEditable(false);
        JScrollPane membersScrollPane = new JScrollPane(membersText);
        membersScrollPane.setPreferredSize(new Dimension(150, 255));
        membersScrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        membersScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.EAST;
        membersPanel.add(membersScrollPane, c);
    }

    // Set up the panel for text messages and composition
    private void setUpTextInterface() {
        JPanel text = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        this.add(text, c);
    
        // Create a panel for Messages
        JPanel messagesPanel = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        text.add(messagesPanel, c);
    
        JLabel messages = new JLabel("Messages:");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        messagesPanel.add(messages, c);
    
        messagesText = new JTextArea();
        messagesText.setLineWrap(true);
        messagesText.setWrapStyleWord(true);
        messagesText.setEditable(false);
        JScrollPane messagesScrollPane = new JScrollPane(messagesText);
        messagesScrollPane.setPreferredSize(new Dimension(520, 145));
        messagesScrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        messagesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.EAST;
        messagesPanel.add(messagesScrollPane, c);

        // add a buffer
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        text.add(Box.createVerticalStrut(10), c);
    
        // Create a panel for Compose
        JPanel composePanel = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        text.add(composePanel, c);
    
        JLabel compose = new JLabel("Compose:");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        composePanel.add(compose, c);
    
        composeText = new JTextArea();
        composeText.setLineWrap(true);
        composeText.setWrapStyleWord(true);
        composeText.addKeyListener(new SendActionListener());
        JScrollPane composeScrollPane = new JScrollPane(composeText);
        composeScrollPane.setPreferredSize(new Dimension(520, 84));
        composeScrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        composeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.EAST;
        composePanel.add(composeScrollPane, c);
    }
    
    // Set up the panel for the send button
    private void setUpBot() {
        JPanel bottom = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.anchor = GridBagConstraints.EAST;
        this.add(bottom, c);

        JButton send = new JButton("Send");
        SendActionListener buttonListener = new SendActionListener();
        send.addActionListener(buttonListener);

        bottom.add(send);
    }

    // update text areas appropriately
    public void updateAll(String message, String clients) {
        newMessage(message);
        updateClients(clients);
    }

    // clear text
    public void reset() {
        newMessage("");
        updateClients("");
    }

    // update the message text area appropriately
    private void newMessage(String message) {
        messagesText.setText(message);
    }

    // update the client text area appropriately
    private void updateClients(String clients) {
        membersText.setText(clients);
    }

    // update the compose text appropriately and send to clientnetworking
    private void sendMessage() {
        String message = composeText.getText();
        clientNetworking.sendMessage(message);
        composeText.setText("");
    }

    // return value of connect button
    public JButton getConnect() {
        return connect;
    }

    public boolean getConnectionStatus() {
        return connected;
    }

    // error message when user enters wrong information for connecting
    public void errorMessage() {
        JOptionPane.showMessageDialog(this, "Cannot Connect\nPlease Try Again",
            "Connection Error", JOptionPane.ERROR_MESSAGE);
    }

    // warns user when they try to do an action when not connected
    public void warningMessage() {
        JOptionPane.showMessageDialog(this, "Not Connected\nPlease Connect to Send Messages",
            "Connection Warning", JOptionPane.ERROR_MESSAGE);
    }

    // for changing text of the connect button
    public void setButtonState(boolean isConnected) {
        connected = isConnected;
        if (connected) {
            connect.setText("Disconnect");
            this.setTitle("GWack -- GW Slack Simulator (connected)");
        } 
        else {
            connect.setText("Connect");
            this.setTitle("GWack -- GW Slack Simulator (disconnected)");
        }
    }

    // Handling the connect and disconnect button action
    private class ButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // if button is disconnected -> connect
            if (!connected) {
                ConnectActionListener connectListener = new ConnectActionListener();
                connectListener.performConnectAction();
            } 
            // if button is connected -> disconnect
            else {
                DisconnectActionListener disconnectListener = new DisconnectActionListener();
                disconnectListener.performDisconnectAction();
            }
        }
    }

    // logic relatede to when the user connects
    private class ConnectActionListener {
        private String n;
        private String ip;
        private int p;

        public void performConnectAction() {
            n = nameText.getText();
            ip = ipText.getText();
            try {
                p = Integer.parseInt(portText.getText());

                // Check if the IP address is "localhost"
                if (ip.equalsIgnoreCase("localhost")) {
                    try {
                        ip = InetAddress.getLocalHost().getHostAddress();
                    } 
                    catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }

                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        // Connection logic
                        clientNetworking.connect(n, ip, p);
                        // update button text
                        return null;

                    }

                    @Override
                    protected void done() {
                    }
                };
                worker.execute();
            }
            catch (Exception e) {
                errorMessage();
                
            }
        }
    }

    // Handling the disconnect button action
    private class DisconnectActionListener {
        public void performDisconnectAction() {
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    // Disconnect logic
                    clientNetworking.disconnect();
                    setButtonState(false);
                    return null;
                }

                @Override
                protected void done() {
                }
            };
            worker.execute();
        }
    }

    // Handling the send button action
    private class SendActionListener implements ActionListener, KeyListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // button click action
            sendAction();
        }
    
        @Override
        public void keyTyped(KeyEvent e) {
        }
    
        @Override
        public void keyPressed(KeyEvent e) {
            // Enter key press action
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                e.consume();
                sendAction();
            }
        }
    
        @Override
        public void keyReleased(KeyEvent e) {
        }
    
        private void sendAction() {
            if (getConnectionStatus()) {
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        // Send logic
                        sendMessage();
                        return null;
                    }
        
                    @Override
                    protected void done() {
                    }
                };
                worker.execute();
            }
            else {
                warningMessage();
            }
        }
    }
}