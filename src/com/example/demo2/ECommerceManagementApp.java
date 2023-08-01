package com.example.demo2;
import java.sql.*;
import java.util.Scanner;

public class ECommerceManagementApp {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ecommerce";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Sai@gmail9";

    private Connection conn;
    private Scanner scanner;

    public ECommerceManagementApp() {
        scanner = new Scanner(System.in);
    }

    public void connectDatabase() {
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connected to the database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeDatabase() {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Disconnected from the database.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Customer Management

    public void addCustomer() {
        try {
            System.out.print("Enter first name: ");
            String firstName = scanner.nextLine();
            System.out.print("Enter last name: ");
            String lastName = scanner.nextLine();
            System.out.print("Enter email: ");
            String email = scanner.nextLine();
            System.out.print("Enter address: ");
            String address = scanner.nextLine();
            System.out.print("Enter phone: ");
            String phone = scanner.nextLine();

            String insertQuery = "INSERT INTO Cust (customer_id, first_name, last_name, email, address, phone) VALUES (NULL, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);

            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, email);
            pstmt.setString(4, address);
            pstmt.setString(5, phone);
            pstmt.executeUpdate();

            System.out.println("Customer added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void displayCustomers() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Cust");

            System.out.println("Customers:");
            System.out.println("ID | First Name | Last Name | Email | Address | Phone");
            System.out.println("------------------------------------------");
            while (rs.next()) {
                int customerId = rs.getInt("customer_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                String address = rs.getString("address");
                String phone = rs.getString("phone");
                System.out.println(customerId + " | " + firstName + " | " + lastName + " | " + email + " | " + address + " | " + phone);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Product Management

    public void addProduct() {
        try {
            System.out.print("Enter product name: ");
            String name = scanner.nextLine();
            System.out.print("Enter product description: ");
            String description = scanner.nextLine();
            System.out.print("Enter product price: ");
            double price = scanner.nextDouble();
            System.out.print("Enter stock quantity: ");
            int stockQuantity = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            String insertQuery = "INSERT INTO Prod (product_id,name, description, price, stock_quantity) VALUES (NULL,?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, stockQuantity);
            pstmt.executeUpdate();

            System.out.println("Product added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void displayProducts() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Prod");

            System.out.println("Products:");
            System.out.println("ID | Name | Description | Price | Stock Quantity");
            System.out.println("------------------------------------------");
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");
                int stockQuantity = rs.getInt("stock_quantity");
                System.out.println(productId + " | " + name + " | " + description + " | " + price + " | " + stockQuantity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Order Management

    public void placeOrder() {
        try {
            displayCustomers();
            System.out.print("Enter customer ID: ");
            int customerId = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            displayProducts();
            System.out.print("Enter product ID: ");
            int productId = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character
            System.out.print("Enter quantity: ");
            int quantity = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            double itemPrice = getProductPrice(productId);
            double totalAmount = itemPrice * quantity;

            String insertOrderQuery = "INSERT INTO Orders (customer_id, order_date, total_amount, status) VALUES (?, CURRENT_TIMESTAMP, ?, 'Pending')";
            PreparedStatement pstmt = conn.prepareStatement(insertOrderQuery, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, customerId);
            pstmt.setDouble(2, totalAmount);
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            int orderId = -1;
            if (generatedKeys.next()) {
                orderId = generatedKeys.getInt(1);
            }

            String insertOrderItemQuery = "INSERT INTO Order_Items (order_id, product_id, quantity, item_price, total_price) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertOrderItemQuery);
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);
            pstmt.setDouble(4, itemPrice);
            pstmt.setDouble(5, totalAmount);
            pstmt.executeUpdate();

            updateProductStock(productId, quantity);

            System.out.println("Order placed successfully! Order ID: " + orderId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getProductPrice(int productId) {
        try {
            String query = "SELECT price FROM Products WHERE product_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("price");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public void updateProductStock(int productId, int quantity) {
        try {
            String query = "UPDATE Products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, productId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Main Menu

    public void displayMainMenu() {
        System.out.println("\nWelcome to E-Commerce Management App");
        System.out.println("1. Add Customer");
        System.out.println("2. Display Customers");
        System.out.println("3. Add Product");
        System.out.println("4. Display Products");
        System.out.println("5. Place Order");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

    public static void main(String[] args) {
        ECommerceManagementApp app = new ECommerceManagementApp();
        app.connectDatabase();

        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            app.displayMainMenu();
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    app.addCustomer();
                    break;
                case 2:
                    app.displayCustomers();
                    break;
                case 3:
                    app.addProduct();
                    break;
                case 4:
                    app.displayProducts();
                    break;
                case 5:
                    app.placeOrder();
                    break;
                case 0:
                    System.out.println("Exiting the application. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 0);

        scanner.close();
        app.closeDatabase();
    }

}