package expiryTracker.client;

import expiryTracker.client.view.MenuGUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MenuGUI::new);
    }
}
