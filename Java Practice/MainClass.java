import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

class Item implements Serializable {
    String id;
    String name;
    int quantity;
    double price;

    public Item(String id, String name, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public Object[] toTableRow() {
        return new Object[]{id, name, quantity, String.format("K%.2f", price)};
    }
}

public class MainClass extends JFrame {
    private static final String FILE_NAME = "inventory.txt";
    private static List<Item> inventory = new ArrayList<>();
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JLabel welcomeLabel;

    public MainClass() {
        loadInventory();
        createUI();
    }

    private void createUI() {
        setTitle("INVENTORY MANAGEMENT SYSTEM");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Welcome Message
        welcomeLabel = new JLabel("WELCOME TO INVENTORY MANAGEMENT SYSTEM", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(welcomeLabel, BorderLayout.NORTH);

        // Table for displaying inventory
        String[] columnNames = {"ID", "Name", "Quantity (KG)", "Price (MWK)"};
        tableModel = new DefaultTableModel(columnNames, 0);
        inventoryTable = new JTable(tableModel);
        add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 7));
        String[] buttons = {
            "View Inventory", "Add New Item", "Update Item",
            "Remove Item", "Search for Item", "View Reports", "Exit"
        };

        for (String button : buttons) {
            JButton btn = new JButton(button);
            btn.addActionListener(new ButtonClickListener());
            buttonPanel.add(btn);
        }

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private class ButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            welcomeLabel.setVisible(false); // Hide welcome message when any button is clicked

            switch (command) {
                case "View Inventory":
                    viewInventory();
                    break;
                case "Add New Item":
                    addItem();
                    break;
                case "Update Item":
                    updateItem();
                    break;
                case "Remove Item":
                    removeItem();
                    break;
                case "Search for Item":
                    searchItem();
                    break;
                case "View Reports":
                    viewReports();
                    break;
                case "Exit":
                    saveInventory();
                    JOptionPane.showMessageDialog(MainClass.this, "Exiting The System... Data saved!");
                    System.exit(0);
                    break;
            }
        }
    }

    private void viewInventory() {
        tableModel.setRowCount(0); // Clear table before displaying new data
        if (inventory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Oops! Inventory is empty!");
            return;
        }

        for (Item item : inventory) {
            tableModel.addRow(item.toTableRow());
        }
    }

    private void addItem() {
        String id = JOptionPane.showInputDialog(this, "Enter Item ID:");
        String name = JOptionPane.showInputDialog(this, "Enter Item Name:");
        int quantity;
        double price;

        try {
            quantity = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter Quantity (KG):"));
            price = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter Price (MWK):"));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input! Please enter numeric values.");
            return;
        }

        inventory.add(new Item(id, name, quantity, price));
        saveInventory();
        JOptionPane.showMessageDialog(this, "Item added successfully!");
        viewInventory(); // Update inventory after adding item
    }

    private void updateItem() {
        String id = JOptionPane.showInputDialog(this, "Enter Item ID to update:");
        for (Item item : inventory) {
            if (item.id.equals(id)) {
                try {
                    int newQuantity = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter new quantity:"));
                    double newPrice = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter new price:"));
                    item.quantity = newQuantity;
                    item.price = newPrice;
                    saveInventory();
                    JOptionPane.showMessageDialog(this, "Item updated successfully!");
                    viewInventory(); // Update inventory after updating item
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid input! Please enter numeric values.");
                }
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Sorry: Item not found!");
    }

    private void removeItem() {
        String id = JOptionPane.showInputDialog(this, "Enter Item ID to remove:");
        boolean removed = inventory.removeIf(item -> item.id.equals(id));
        if (removed) {
            saveInventory();
            JOptionPane.showMessageDialog(this, "Item removed successfully!");
            viewInventory(); // Update inventory after removal
        } else {
            JOptionPane.showMessageDialog(this, "Sorry: Item not found!");
        }
    }

    private void searchItem() {
        String input = JOptionPane.showInputDialog(this, "Enter Item Name or ID:");
        for (Item item : inventory) {
            if (item.id.equals(input) || item.name.equalsIgnoreCase(input)) {
                JOptionPane.showMessageDialog(this, "Item Found!\nID: " + item.id + "\nName: " + item.name +
                        "\nQuantity: " + item.quantity + " KG\nPrice: K" + item.price);
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Sorry: Item not found!");
    }

    private void viewReports() {
        tableModel.setRowCount(0); // Clear table
        for (Item item : inventory) {
            if (item.quantity < 5) {
                tableModel.addRow(item.toTableRow());
            }
        }

        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No low stock items.");
        }
    }

    private static void saveInventory() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(inventory);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving inventory: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadInventory() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            inventory = (List<Item>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            inventory = new ArrayList<>(); // Start with empty inventory if file is missing
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainClass().setVisible(true));
    }
}
