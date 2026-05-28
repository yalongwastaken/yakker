/**
 * @file YakClientFrame.java
 * @brief Swing frame for Yakker client.
 *        Builds the GUI layout, wires action listeners for connect/disconnect
 *        and send, and delegates networking to YakClientNetworking.
 */
package yakker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class YakClientFrame extends JFrame {

    // constants
    private static final String TITLE_CONNECTED    = "Yakker (connected)";
    private static final String TITLE_DISCONNECTED = "Yakker (disconnected)";
    private static final int    FRAME_WIDTH        = 750;
    private static final int    FRAME_HEIGHT       = 400;

    // input fields
    private JTextArea nameText;
    private JTextArea ipText;
    private JTextArea portText;

    // display areas
    private JTextArea membersText;
    private JTextArea messagesText;
    private JTextArea composeText;

    // controls
    private JButton connect;
    private boolean connected = false;

    // networking
    private YakClientNetworking clientNetworking;

    // constructor
    public YakClientFrame() {
        this.setTitle(TITLE_DISCONNECTED);
        this.setLayout(new GridBagLayout());
        this.setResizable(true);
        this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // center on screen
        GraphicsEnvironment ge            = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice      defaultScreen = ge.getDefaultScreenDevice();
        int screenWidth  = defaultScreen.getDisplayMode().getWidth();
        int screenHeight = defaultScreen.getDisplayMode().getHeight();
        this.setLocation((screenWidth - FRAME_WIDTH) / 2, (screenHeight - FRAME_HEIGHT) / 2);

        clientNetworking = new YakClientNetworking(this);
    }

    // builds all panels and adds them to the frame
    public void setUp() {
        setUpHeader();
        setUpActiveUsers();
        setUpTextInterface();
        setUpBottom();
    }

    // header row: name, IP, port inputs and connect button
    private void setUpHeader() {
        JPanel header = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx   = 1;
        c.gridy   = 0;
        c.anchor  = GridBagConstraints.WEST;
        this.add(header, c);

        JLabel name = new JLabel("Name:",       JLabel.CENTER);
        JLabel ip   = new JLabel("IP Address:", JLabel.CENTER);
        JLabel port = new JLabel("Port:",       JLabel.CENTER);

        nameText = new JTextArea();
        nameText.setPreferredSize(new Dimension(100, 20));
        nameText.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        ipText = new JTextArea();
        ipText.setPreferredSize(new Dimension(100, 20));
        ipText.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        portText = new JTextArea();
        portText.setPreferredSize(new Dimension(75, 20));
        portText.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        connect = new JButton("Connect");
        connect.addActionListener(new ButtonActionListener());

        header.add(name);
        header.add(nameText);
        header.add(ip);
        header.add(ipText);
        header.add(port);
        header.add(portText);
        header.add(Box.createHorizontalStrut(10));
        header.add(connect);
    }

    // left panel: scrollable list of active members
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

        JLabel label = new JLabel("Active Members:");
        c = new GridBagConstraints();
        c.gridx  = 0;
        c.gridy  = 0;
        c.anchor = GridBagConstraints.WEST;
        membersPanel.add(label, c);

        membersText = new JTextArea();
        membersText.setLineWrap(true);
        membersText.setWrapStyleWord(true);
        membersText.setEditable(false);

        JScrollPane membersScrollPane = new JScrollPane(membersText);
        membersScrollPane.setPreferredSize(new Dimension(150, 255));
        membersScrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        membersScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        c = new GridBagConstraints();
        c.gridx  = 0;
        c.gridy  = 1;
        c.anchor = GridBagConstraints.EAST;
        membersPanel.add(membersScrollPane, c);
    }

    // center panel: messages display and compose area
    private void setUpTextInterface() {
        JPanel text = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        this.add(text, c);

        // messages panel
        JPanel messagesPanel = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        text.add(messagesPanel, c);

        JLabel messagesLabel = new JLabel("Messages:");
        c = new GridBagConstraints();
        c.gridx  = 0;
        c.gridy  = 0;
        c.anchor = GridBagConstraints.WEST;
        messagesPanel.add(messagesLabel, c);

        messagesText = new JTextArea();
        messagesText.setLineWrap(true);
        messagesText.setWrapStyleWord(true);
        messagesText.setEditable(false);

        JScrollPane messagesScrollPane = new JScrollPane(messagesText);
        messagesScrollPane.setPreferredSize(new Dimension(520, 145));
        messagesScrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        messagesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        c = new GridBagConstraints();
        c.gridx  = 0;
        c.gridy  = 1;
        c.anchor = GridBagConstraints.EAST;
        messagesPanel.add(messagesScrollPane, c);

        // vertical buffer
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        text.add(Box.createVerticalStrut(10), c);

        // compose panel
        JPanel composePanel = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        text.add(composePanel, c);

        JLabel composeLabel = new JLabel("Compose:");
        c = new GridBagConstraints();
        c.gridx  = 0;
        c.gridy  = 0;
        c.anchor = GridBagConstraints.WEST;
        composePanel.add(composeLabel, c);

        composeText = new JTextArea();
        composeText.setLineWrap(true);
        composeText.setWrapStyleWord(true);
        composeText.addKeyListener(new SendActionListener());

        JScrollPane composeScrollPane = new JScrollPane(composeText);
        composeScrollPane.setPreferredSize(new Dimension(520, 84));
        composeScrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        composeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        c = new GridBagConstraints();
        c.gridx  = 0;
        c.gridy  = 1;
        c.anchor = GridBagConstraints.EAST;
        composePanel.add(composeScrollPane, c);
    }

    // bottom row: send button
    private void setUpBottom() {
        JPanel bottom = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx  = 1;
        c.gridy  = 2;
        c.anchor = GridBagConstraints.EAST;
        this.add(bottom, c);

        JButton send = new JButton("Send");
        send.addActionListener(new SendActionListener());
        bottom.add(send);
    }

    // updates messages and client list displays
    public void updateAll(String message, String clients) {
        newMessage(message);
        updateClients(clients);
    }

    // clears messages and client list displays
    public void reset() {
        newMessage("");
        updateClients("");
    }

    // replaces the messages display content
    private void newMessage(String message) {
        messagesText.setText(message);
    }

    // replaces the members display content
    private void updateClients(String clients) {
        membersText.setText(clients);
    }

    // reads compose field, sends message, and clears the field
    private void sendMessage() {
        String message = composeText.getText();
        clientNetworking.sendMessage(message);
        composeText.setText("");
    }

    // returns the connect/disconnect button
    public JButton getConnect() {
        return connect;
    }

    // returns whether the client is currently connected
    public boolean getConnectionStatus() {
        return connected;
    }

    // shows a connection error dialog
    public void errorMessage() {
        JOptionPane.showMessageDialog(this,
            "Cannot Connect\nPlease Try Again",
            "Connection Error", JOptionPane.ERROR_MESSAGE);
    }

    // shows a warning dialog when attempting to send while disconnected
    public void warningMessage() {
        JOptionPane.showMessageDialog(this,
            "Not Connected\nPlease Connect to Send Messages",
            "Connection Warning", JOptionPane.ERROR_MESSAGE);
    }

    // updates button label and window title to reflect connection state
    public void setButtonState(boolean isConnected) {
        connected = isConnected;
        if (connected) {
            connect.setText("Disconnect");
            this.setTitle(TITLE_CONNECTED);
        } else {
            connect.setText("Connect");
            this.setTitle(TITLE_DISCONNECTED);
        }
    }

    // toggles between connect and disconnect actions
    private class ButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!connected) {
                new ConnectActionListener().performConnectAction();
            } else {
                new DisconnectActionListener().performDisconnectAction();
            }
        }
    }

    // reads input fields and initiates connection on a background thread
    private class ConnectActionListener {
        public void performConnectAction() {
            String n  = nameText.getText();
            String ip = ipText.getText();

            try {
                int p = Integer.parseInt(portText.getText());

                // resolve localhost to actual address
                if (ip.equalsIgnoreCase("localhost")) {
                    try {
                        ip = InetAddress.getLocalHost().getHostAddress();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }

                final String resolvedIp = ip;
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        clientNetworking.connect(n, resolvedIp, p);
                        return null;
                    }

                    @Override
                    protected void done() {}
                }.execute();
            } catch (Exception e) {
                errorMessage();
            }
        }
    }

    // initiates disconnection on a background thread
    private class DisconnectActionListener {
        public void performDisconnectAction() {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    clientNetworking.disconnect();
                    setButtonState(false);
                    return null;
                }

                @Override
                protected void done() {}
            }.execute();
        }
    }

    // handles send button click and Enter key press
    private class SendActionListener implements ActionListener, KeyListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            sendAction();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                e.consume();
                sendAction();
            }
        }

        @Override public void keyTyped(KeyEvent e)    {}
        @Override public void keyReleased(KeyEvent e) {}

        // dispatches send on a background thread, warns if disconnected
        private void sendAction() {
            if (getConnectionStatus()) {
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        sendMessage();
                        return null;
                    }

                    @Override
                    protected void done() {}
                }.execute();
            } else {
                warningMessage();
            }
        }
    }
}