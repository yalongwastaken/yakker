/**
 * @file YakClientGUI.java
 * @brief Entry point for the Yakker client application.
 *        Initializes and displays the main Swing frame.
 */
package yakker;

import javax.swing.*;

public class YakClientGUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            YakClientFrame frame = new YakClientFrame();
            frame.setUp();
            frame.setVisible(true);
        });
    }
}