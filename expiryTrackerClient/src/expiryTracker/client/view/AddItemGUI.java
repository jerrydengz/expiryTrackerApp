package expiryTracker.client.view;

import expiryTracker.client.control.ConsumableManager;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Handles displaying of a <code>JDialog</code> for the purpose of
 * getting user input to create a <code>Consumable</code> object
 */
public class AddItemGUI extends JDialog {
    private final ConsumableManager consumableManager = ConsumableManager.getInstance();
    private final String[] CONSUMABLES_TYPE = {"Food", "Drink"};
    private final int COLUMN_LENGTH = 32;
    private final int ENTRY_FIELD_WIDTH = 300;
    private final int RIGID_AREA_WIDTH = 20;
    private final int RIGID_AREA_HEIGHT = 20;
    private final int PANEL_DIMENSION = 500;
    private final int DIMENSION_OFFSET = 50;
    private String type = CONSUMABLES_TYPE[0]; // initialize type to be "Food" by default

    // Components of UI
    private JPanel mainPanel;
    private final JTextField nameTextField = new JTextField(COLUMN_LENGTH);
    private final JTextField notesTextField = new JTextField(COLUMN_LENGTH);
    private final JTextField priceTextField = new JTextField(COLUMN_LENGTH);
    private final JTextField matterTextField = new JTextField(COLUMN_LENGTH);
    private DatePicker expiryDatePicker;
    private JLabel matterLabel;

    /**
     * Constructs a <code>AddItemGUI</code> object.
     * <p>
     * Calls methods that set up components to the <code>JDialog</code>
     * for the program to run creating a new <code>Consumable</code> object
     *
     * @param parent a <code>JFrame</code> object from the main program.
     */
    public AddItemGUI(JFrame parent) {
        super(parent, "Add Item", true);

        // http://www2.hawaii.edu/~takebaya/ics111/jdialog/jdialog.html
        // set position of the JDialog being created
        Point location = parent.getLocation();
        final int LOCATION_OFFSET = 80;
        this.setLocation(location.x + LOCATION_OFFSET, location.y + LOCATION_OFFSET);

        this.setMinimumSize(new Dimension(PANEL_DIMENSION - DIMENSION_OFFSET, PANEL_DIMENSION));

        this.setUpMainPanel();
        this.createEntryFields();
        this.setUpStateButtons();
        mainPanel.add(Box.createRigidArea(new Dimension(RIGID_AREA_WIDTH, RIGID_AREA_HEIGHT)));

        this.add(mainPanel);
        // https://stackoverflow.com/questions/20293220/swing-set-a-fixed-window-size-for-jdialog
        this.setResizable(false);
        this.setVisible(true);
    }

    /**
     * Creates and initializes the main <code>JPanel</code> where all the
     * GUI components are contained in.
     */
    private void setUpMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setMinimumSize(new Dimension(PANEL_DIMENSION, PANEL_DIMENSION + DIMENSION_OFFSET));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(Box.createRigidArea(new Dimension(RIGID_AREA_WIDTH, RIGID_AREA_HEIGHT)));

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * Creates and Initializes <code>JTextFields</code> required for handling user input.
     */
    private void createEntryFields() {
        // Type of Consumable
        JPanel typePanel = new JPanel();
        JLabel typeLabel = new JLabel("Type: ");
        JComboBox<String> typeComboBox = new JComboBox<>(CONSUMABLES_TYPE);
        typeComboBox.setPreferredSize(new Dimension(ENTRY_FIELD_WIDTH + DIMENSION_OFFSET,
                typeLabel.getPreferredSize().height));

        // https://docs.oracle.com/javase/tutorial/uiswing/components/combobox.html
        typeComboBox.addActionListener(event -> {
            var item = (JComboBox<?>) event.getSource();
            type = (String) item.getSelectedItem();
            matterLabel.setText((Objects.equals(type, CONSUMABLES_TYPE[0])) ? "Weight: " : "Volume: ");
        });
        typePanel.add(typeLabel);
        typePanel.add(typeComboBox);
        mainPanel.add(typePanel);

        // TextFields for Name, Notes, Price
        textFieldInitialize(nameTextField, "Name: ");
        textFieldInitialize(notesTextField, "Notes: ");
        textFieldInitialize(priceTextField, "Price: ");

        // Mass (Weight/Volume) TextField
        JPanel matterPanel = new JPanel();
        matterLabel = new JLabel("Weight: ");
        matterTextField.setPreferredSize(new Dimension(ENTRY_FIELD_WIDTH, matterTextField.getPreferredSize().height));
        matterPanel.add(matterLabel);
        matterPanel.add(matterTextField);
        mainPanel.add(matterPanel);

        // Expiry Date Picker Field
        JPanel expiryPanel = new JPanel();
        //http://javadox.com/com.github.lgooddatepicker/LGoodDatePicker/10.3.1/com/github/lgooddatepicker/components/DatePickerSettings.html
        DatePickerSettings disableEditing = new DatePickerSettings();
        disableEditing.setAllowKeyboardEditing(false);
        expiryDatePicker = new DatePicker(disableEditing);
        expiryDatePicker.setPreferredSize(new Dimension(ENTRY_FIELD_WIDTH, expiryDatePicker.getPreferredSize().height));
        expiryPanel.add(new JLabel("Expiry Date: "));
        expiryPanel.add(expiryDatePicker);
        mainPanel.add(expiryPanel);
    }

    /**
     * Helper method to initialize the <code>JTextFields</code> of context Name, Notes, and Price.
     *
     * @param entryTextField the <code>JTextField</code> object to be initialized
     * @param entryLabelName a <code>String</code> representing the title of the passed in <code>JTextField</code>
     */
    private void textFieldInitialize(JTextField entryTextField, String entryLabelName) {
        JPanel entryPanel = new JPanel();
        JLabel entryLabel = new JLabel(entryLabelName);
        entryTextField.setPreferredSize(new Dimension(ENTRY_FIELD_WIDTH, entryTextField.getPreferredSize().height));
        entryPanel.add(entryLabel);
        entryPanel.add(entryTextField);
        mainPanel.add(entryPanel);
    }

    /**
     * Creates and Initializes the buttons required for Creating or Canceling the process of
     * creating a <code>Consumable</code> object
     */
    private void setUpStateButtons() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        // Set up the Create Button
        JButton createBtn = new JButton("Create");
        createBtn.addActionListener(event -> {

            // notes can be empty, but JTextField .getText() will return null if nothing is entered
            String notes = (notesTextField.getText() == null) ? "" : notesTextField.getText().trim();
            String name;
            double price;
            double matter;

            if (!this.isEmptyEntries()) {
                name = nameTextField.getText().trim();

                if (this.areValidEntries()) {
                    price = Double.parseDouble(priceTextField.getText());
                    matter = Double.parseDouble(matterTextField.getText());

                    // convert to LocalDateTime to handle day of comparisons in model
                    // https://beginnersbook.com/2017/10/java-convert-localdate-to-localdatetime/
                    final int hour = 23;
                    final int minute = 59;
                    consumableManager.addConsumableItemRequest(type, name, notes, price, matter,
                            expiryDatePicker.getDate().atTime(hour, minute));
                    dispose();
                }
            }
        });

        // Set up the Cancel Button
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(event -> dispose());

        buttonPanel.add(createBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(RIGID_AREA_WIDTH, RIGID_AREA_HEIGHT)));
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel);
    }

    /**
     * Validates if the <code>JTextFields</code> of the <code>JDialog</code>
     * contain any empty or null values.
     *
     * @return a <code>boolean</code> confirming if the <code>JTextFields</code> of the <code>JDialog</code>
     * contain any empty or null values
     */
    private boolean isEmptyEntries() {
        // https://stackoverflow.com/questions/23419087/stringutils-isblank-vs-string-isempty
        if (nameTextField.getText() == null || nameTextField.getText().isBlank()) {
            generateJOptionPane("The \"Name\" field can't be empty. Please enter a non-empty name.",
                    "Empty Field");
            return true;
        } else if (priceTextField.getText() == null || priceTextField.getText().isBlank()) {
            generateJOptionPane("The \"Price\" field can't be empty. Please enter a non-empty price.",
                    "Empty Field");
            return true;
        } else if (matterTextField.getText() == null || matterTextField.getText().isBlank()) {
            String mass = (Objects.equals(type, "Food")) ? "Weight" : "Volume";
            generateJOptionPane("The \"" + mass + "\" field can't be empty. Please enter a non-empty "
                    + mass + ".", "Empty Field");
            return true;
        } else if (expiryDatePicker.getDate() == null) {
            generateJOptionPane("The \"Expiry Date\" field can't be empty. Please enter a expiry date",
                    "Empty Field");
            return true;
        }
        return false;
    }

    /**
     * Validates if the <code>JTextFields</code> of the <code>JDialog</code>
     * contain any invalid values.
     *
     * @return a <code>boolean</code> confirming if the <code>JTextFields</code> of the <code>JDialog</code>
     * contain any invalid values
     */
    private boolean areValidEntries() {
        double price;
        double matter;
        String mass = (Objects.equals(type, "Food")) ? "Weight" : "Volume";

        // check for values of not type double
        try {
            price = Double.parseDouble(priceTextField.getText());
        } catch (NumberFormatException e) {
            generateJOptionPane("The \"Price\" field has to be a valid number. Please enter a valid number",
                    "Invalid Entry");
            return false;
        }

        try {
            matter = Double.parseDouble(matterTextField.getText());
        } catch (NumberFormatException e) {
            generateJOptionPane("The \" " + mass + " \" field has to be a valid number. " +
                    "Please enter a valid number", "Invalid Entry");
            return false;
        }

        if (price < 0) {
            generateJOptionPane("Price can't be negative. Please enter a non-negative price.",
                    "Invalid Entry");
            return false;
        } else if (matter < 0) {
            generateJOptionPane(mass + " can't be negative. Please enter a non-negative " + mass + ".",
                    "Invalid Entry");
            return false;
        }
        return true;
    }

    /**
     * Creates a <code>JOptionPane</code> to be displayed.
     *
     * @param message a <code>String</code> message to be displayed to the <code>JOptionPane</code> window.
     * @param title   a <code>String</code> title to be displayed to the <code>JOptionPane</code> window.
     */
    private void generateJOptionPane(String message, String title) {
        // https://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
}