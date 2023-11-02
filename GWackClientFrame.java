import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.function.Consumer;


public class GWackClientFrame extends JFrame {
    GWackClientInteractions interact = new GWackClientInteractions();

    public GWackClientFrame() {
        this.setTitle("GWack -- GW Slack Simulator (disconnected)");
        this.setLayout(new GridBagLayout());
        this.setResizable(false);
        this.pack();
        this.setSize(750, 400);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();

        // Determine the screen size
        int screenWidth = defaultScreen.getDisplayMode().getWidth();
        int screenHeight = defaultScreen.getDisplayMode().getHeight();

        // Calculate the position to center the frame
        int frameWidth = this.getWidth();
        int frameHeight = this.getHeight();
        int x = (screenWidth - frameWidth) / 2;
        int y = (screenHeight - frameHeight) / 2;

        // Set the frame's location
        this.setLocation(x, y);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public void setUp() {
        setUpHeader();
        setUpActiveUsers();
        setUpTextInterface();
        setUpBot();
    }

    private void setUpHeader() {
        JPanel header = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        this.add(header, c);

        JLabel name = new JLabel("Name:", JLabel.CENTER);
        JLabel ip = new JLabel("IP Address:", JLabel.CENTER);
        JLabel port = new JLabel("Port:", JLabel.CENTER);;

        JTextArea nameText = new JTextArea();
        nameText.setPreferredSize(new Dimension(100, 20));
        nameText.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        JTextArea ipText = new JTextArea();
        ipText.setPreferredSize(new Dimension(100, 20));
        ipText.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        JTextArea portText = new JTextArea();
        portText.setPreferredSize(new Dimension(75, 20));
        portText.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        JButton connect = new JButton("Connect");
        connect = interact.connectButtonPress(connect);

        header.add(name);
        header.add(nameText);
        header.add(ip);
        header.add(ipText);
        header.add(port);
        header.add(portText);
        header.add(Box.createHorizontalStrut(10));
        header.add(connect);
    }

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
    
        JTextArea membersText = new JTextArea();
        membersText.setPreferredSize(new Dimension(150, 255));
        membersText.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        membersText.setLineWrap(true);
        membersText.setWrapStyleWord(true);
        membersText.setEditable(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.EAST;
        membersPanel.add(membersText, c);
    }

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
    
        JTextArea messagesText = new JTextArea();
        messagesText.setPreferredSize(new Dimension(520, 145));
        messagesText.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        messagesText.setLineWrap(true);
        messagesText.setWrapStyleWord(true);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.EAST;
        messagesPanel.add(messagesText, c);

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
    
        JTextArea composeText = new JTextArea();
        composeText.setPreferredSize(new Dimension(520, 84));
        composeText.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        composeText.setLineWrap(true);
        composeText.setWrapStyleWord(true);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.EAST;
        composePanel.add(composeText, c);
    }
    
    private void setUpBot() {
        JPanel bottom = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.anchor = GridBagConstraints.EAST;
        this.add(bottom, c);

        JButton send = new JButton("Send");

        bottom.add(send);
    }
}