import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.function.Consumer;

public class GWackClientInteractions extends JFrame {
    public JButton connectButtonPress (JButton c) {
        c.addActionListener((e) -> {
            try {
                if (c.getText().equals("Connect")) {
                    changeText("Disconnect", c);
                }
                else {
                    changeText("Connect", c);
                }
            } catch (Exception error) {
                errorMessage();
            }
        });

        return c;
    }

    private void changeText(String type, JButton c) {
        c.setText(type);
    }

    private void errorMessage() {
        JOptionPane.showMessageDialog(this, "Error! Invalid Values!\nPlease Enter Again.",
               "Project 2", JOptionPane.ERROR_MESSAGE);
    }
}
