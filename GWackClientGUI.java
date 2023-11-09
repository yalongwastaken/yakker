import javax.swing.*;

public class GWackClientGUI extends JFrame {
    public static GWackClientFrame f;
    public static void main(String [] args) {
        f = new GWackClientFrame();
        f.setUp();
        f.setVisible(true);
    }
}
