package expiryTracker.client.view;

import expiryTracker.client.control.ConsumableManager;
import expiryTracker.client.model.Consumable;
import expiryTracker.client.model.Food;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

/**
 * Handles the displaying of the menu GUI and different
 * views of <code>Consumable</code> objects
 */
public class MenuGUI {
    // Singleton Instance
    private final ConsumableManager consumableManager = ConsumableManager.getInstance();

    // Container of the application
    private JFrame appFrame;
    private JPanel mainPanel;

    // Components & Values of the List Display
    private String listMode;
    private JPanel itemPanel;
    private JPanel buttonPanel;
    private JScrollPane itemScrollPane;
    private final JButton listAllBtn = new JButton("All");
    private final JButton listExpiredBtn = new JButton("Expired");
    private final JButton listNotExpiredBtn = new JButton("Not Expired");
    private final JButton list7DaysExpiringBtn = new JButton("Expiring in 7 Days");

    // Dimension Constants
    private final int MAIN_WIDTH = 600;
    private final int MAIN_HEIGHT = 700;
    private final int RIGID_WIDTH = 20;
    private final int RIGID_HEIGHT = 20;

    /**
     * Constructs a <code>MenuGUI</code> object.
     * <p>
     * Calls methods that set up the components to the frame
     * for the program to run.
     */
    public MenuGUI() {

        if (!consumableManager.isServerUp()) {
            System.out.println("Server is Down!");
            return;
        }

        this.setUpFrame();
        this.setUpMainPanel();
        mainPanel.add(Box.createRigidArea(new Dimension(RIGID_WIDTH, RIGID_HEIGHT)));

        this.setUpListButtons();
        this.setUpListPanels();

        this.populateScrollPane(consumableManager.getFilteredList("All"));
        mainPanel.add(itemScrollPane);
        mainPanel.add(Box.createRigidArea(new Dimension(RIGID_WIDTH, RIGID_HEIGHT)));

        this.createAddBtn();
        mainPanel.add(Box.createRigidArea(new Dimension(RIGID_WIDTH, RIGID_HEIGHT)));

        // piece together mainPanel to the frame
        appFrame.add(mainPanel);
        appFrame.setVisible(true);
    }

    /**
     * Creates and initializes the parent <code>JFrame</code> where the GUI is displayed.
     */
    private void setUpFrame() {
        // https://docs.oracle.com/javase/6/docs/api/java/awt/Window.html#setMinimumSize%28java.awt.Dimension%29
        appFrame = new JFrame("Totally !Functional Consumables Expiry Date Tracker");
        appFrame.setMinimumSize(new Dimension(MAIN_WIDTH, MAIN_HEIGHT));
        appFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        appFrame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                consumableManager.exitRequest();
                appFrame.dispose();
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });
    }

    /**
     * Creates and initializes the main <code>JPanel</code> where all the
     * GUI components are contained in.
     */
    private void setUpMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setPreferredSize(new Dimension(MAIN_WIDTH, MAIN_HEIGHT));
        mainPanel.setBorder(new EmptyBorder(0, RIGID_WIDTH, 0, RIGID_WIDTH));
        mainPanel.setBackground(Color.WHITE);
    }

    /**
     * Initializes the buttons required for switching between different
     * list views of <code>Consumable</code> items.
     */
    private void setUpListButtons() {
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBackground(Color.WHITE);
        listMode = listAllBtn.getText();
        listAllBtn.setBackground(Color.BLACK);
        listAllBtn.setForeground(Color.WHITE);

        createListButtons(listAllBtn);
        createListButtons(listExpiredBtn);
        createListButtons(listNotExpiredBtn);
        createListButtons(list7DaysExpiringBtn);

        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(RIGID_WIDTH, RIGID_HEIGHT)));
    }

    /**
     * Creates buttons required for switching between different
     * list views of <code>Consumable</code>
     *
     * @param button a <code>JButton</code> corresponding to different list views
     *               to be constructed
     */
    private void createListButtons(JButton button) {
        // https://stackoverflow.com/questions/9361658/disable-jbutton-focus-border
        button.setFocusPainted(false);
        button.addActionListener(event -> {
            selectedButton(button);
            listMode = button.getText();
            populateScrollPane(consumableManager.getFilteredList(listMode));
            itemPanel.revalidate();
            itemPanel.repaint();
        });

        buttonPanel.add(button);
        buttonPanel.add(Box.createRigidArea(new Dimension(RIGID_WIDTH, 0)));
    }

    /**
     * Updates the currently selected Button's background
     * and the previously selected Button's background.
     *
     * @param button a <code>JButton</code> used for displaying different
     *               list views of different <code>Consumable</code> objects
     */
    private void selectedButton(JButton button) {
        // turn off previously selected button
        // https://stackoverflow.com/questions/1358398/how-to-get-jbutton-default-background-color/13957911
        Color defaultButtonColour = new JButton().getBackground();
        switch (listMode) {
            case "All" -> {
                listAllBtn.setBackground(defaultButtonColour);
                listAllBtn.setForeground(Color.BLACK);
            }
            case "Expired" -> {
                listExpiredBtn.setBackground(defaultButtonColour);
                listExpiredBtn.setForeground(Color.BLACK);
            }
            case "Not Expired" -> {
                listNotExpiredBtn.setBackground(defaultButtonColour);
                listNotExpiredBtn.setForeground(Color.BLACK);
            }
            case "Expiring in 7 Days" -> {
                list7DaysExpiringBtn.setBackground(defaultButtonColour);
                list7DaysExpiringBtn.setForeground(Color.BLACK);
            }
        }

        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
    }

    /**
     * Initializes the <code>JPanel</code> and <code>JScrollPane</code>
     * needed for displaying the different list views of <code>Consumable</code> objects
     */
    private void setUpListPanels() {
        // create panel to hold items
        itemPanel = new JPanel();
        itemPanel.setLayout(new GridBagLayout());
        itemPanel.setBackground(Color.WHITE);

        // create scrollPane to hold panel
        itemScrollPane = new JScrollPane(itemPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    /**
     * Populates a <code>JPanel itemPanel</code> with <code>Consumable</code> objects
     * to be displayed from the parameter's <code>List<Consumable></code>
     *
     * @param itemList a <code>List<Consumable></code>
     *                 containing specific <code>Consumable</code> objects based on their expiry date.
     */
    private void populateScrollPane(List<Consumable> itemList) {
        itemPanel.removeAll();

        GridBagConstraints constraint = new GridBagConstraints();

        // populate the itemListPanel
        for (int i = 0; i < itemList.size(); i++) {
            JPanel item = createPanelItem(itemList, i, constraint);
            itemPanel.add(item, constraint);
        }

        if (itemList.isEmpty()) {
            switch (listMode) {
                case "All" -> itemPanel.add(new JLabel("No items to show."));
                case "Expired" -> itemPanel.add(new JLabel("No expired items to show."));
                case "Not Expired" -> itemPanel.add(new JLabel("No non-expired items to show."));
                case "Expiring in 7 Days" -> itemPanel.add(
                        new JLabel("No items expiring in 7 days to show."));
            }
        }

        itemScrollPane.getViewport().add(itemPanel);
    }

    /**
     * Constructs a <code>JPanel</code> object containing a <code>JTextArea</code> of
     * <code>Consumable</code> object's <code>toString()</code> text and a <code>JButton</code>.
     *
     * @param itemList   a <code>List<Consumable></code> containing specific <code>Consumable</code> objects to
     *                   be displayed.
     * @param index      an <code>int</code> representing the index of the
     *                   <code>Consumable</code> object in <code>itemList</code>
     * @param constraint a <code>GridBagConstraints</code> to fixate the constructed
     *                   <code>JPanel</code> when displayed
     * @return a <code>JPanel</code> object to be displayed
     */
    private JPanel createPanelItem(List<Consumable> itemList, int index, GridBagConstraints constraint) {
        Consumable itemObject = itemList.get(index);
        String itemType = (itemObject instanceof Food) ? "Food" : "Drink";

        // Create the item panel and initialize
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.X_AXIS));
        item.add(Box.createRigidArea(new Dimension(RIGID_WIDTH, RIGID_HEIGHT)));
        item.setBackground(Color.WHITE);

        // https://docs.oracle.com/javase/tutorial/uiswing/components/border.html
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
        Border outline = BorderFactory.createTitledBorder(blackBorder,
                "Item #" + (index + 1) + " " + "(" + itemType + ")");

        final int BORDER_SPACE_LARGE = 10;
        final int BORDER_SPACE_SMALL = 5;
        item.setBorder(new CompoundBorder(
                new EmptyBorder(BORDER_SPACE_SMALL, BORDER_SPACE_LARGE,
                        BORDER_SPACE_SMALL, BORDER_SPACE_LARGE), outline));

        // Create textArea to display object info
        // https://coderanch.com/t/338648/java/Multiple-lines-JLabel
        JTextArea consumableItemTextArea = new JTextArea(itemList.get(index).toString());
        consumableItemTextArea.setOpaque(false);
        consumableItemTextArea.setEditable(false);
        consumableItemTextArea.setBorder(new EmptyBorder(BORDER_SPACE_SMALL, BORDER_SPACE_SMALL,
                BORDER_SPACE_SMALL, BORDER_SPACE_SMALL));
        item.add(consumableItemTextArea);

        // Create delete button
        item.add(createDeleteBtn(itemObject));
        item.add(Box.createRigidArea(new Dimension(RIGID_WIDTH, RIGID_HEIGHT)));

        // Set up constraints to each item in the itemPanel
        // https://stackoverflow.com/questions/6364280/starting-gridbaglayout-from-top-left-corner-in-java-swing
        // https://stackoverflow.com/questions/24723998/can-components-of-a-gridbaglayout-fill-parent-frame-upon-resize
        constraint.gridx = 0;
        constraint.gridy = index;
        constraint.gridheight = 1;
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.anchor = GridBagConstraints.NORTH;
        constraint.weightx = 1.0;

        if (index == itemList.size() - 1) {
            constraint.weighty = 1.0;
        } else {
            constraint.weighty = 0;
        }
        return item;
    }

    /**
     * Creates a <code>JButton</code> corresponding to the passed in <code>Consumable</code> object.
     *
     * @param item a <code>Consumable</code> object used to match the constructed <code><JButton</code>
     * @return <code>JButton</code> corresponding to the passed in <code>Consumable</code> object
     */
    private JButton createDeleteBtn(Consumable item) {
        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(event -> {
            consumableManager.removeConsumableItemRequest(item.getItemId());
            this.populateScrollPane(consumableManager.getFilteredList(listMode));
            itemPanel.revalidate();
            itemPanel.repaint();
        });
        return deleteBtn;
    }

    /**
     * Creates a <code>JButton</code> used to open a <code>JDialog</code> window
     * for the construction of a new <code>Consumable</code> object.
     */
    private void createAddBtn() {
        JButton addBtn = new JButton("Add");
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.addActionListener(event -> {
            new AddItemGUI(appFrame);
            this.populateScrollPane(consumableManager.getFilteredList(listMode));
            itemPanel.revalidate();
            itemPanel.repaint();
        });

        mainPanel.add(addBtn);
    }
}

