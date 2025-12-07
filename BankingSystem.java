import java.io.*;
import java.util.*;

class Transaction implements Serializable {
    private String type;
    private double amount;
    private Date date;
    
    public Transaction(String type, double amount) {
        this.type = type;
        this.amount = amount;
        this.date = new Date();
    }
    
    @Override
    public String toString() {
        return date + " | " + type + " | $" + String.format("%.2f", amount);
    }
}

class User implements Serializable {
    private String username;
    private String passwordHash;
    private long accountNumber;
    private double balance;
    private List<Transaction> transactions = new ArrayList<>();
    
    public User(String username, String password) {
        this.username = username;
        this.passwordHash = hashPassword(password);
        this.accountNumber = (long)(Math.random() * 9000000000L) + 1000000000L;
        this.balance = 0.0;
    }
    
    public boolean authenticate(String password) {
        return hashPassword(password).equals(passwordHash);
    }
    
    private String hashPassword(String password) {
        // Simple hash for demo (use BCrypt in production)
        return String.valueOf(password.hashCode());
    }
    
    // Getters
    public String getUsername() { return username; }
    public long getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }
    public List<Transaction> getTransactions() { return transactions; }
    
    // Account operations
    public boolean deposit(double amount) {
        if (amount <= 0) return false;
        balance += amount;
        transactions.add(new Transaction("DEPOSIT", amount));
        return true;
    }
    
    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance) return false;
        balance -= amount;
        transactions.add(new Transaction("WITHDRAWAL", -amount));
        return true;
    }
    
    public String getStatement() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ACCOUNT STATEMENT ===\n");
        sb.append("Account: ").append(accountNumber).append("\n");
        sb.append("Balance: $").append(String.format("%.2f", balance)).append("\n\n");
        sb.append("Transactions:\n");
        for (Transaction t : transactions) {
            sb.append(t).append("\n");
        }
        return sb.toString();
    }
}

public class BankingSystem {
    private static Map<String, User> users = new HashMap<>();
    private static final String DATA_FILE = "banking_data.dat";
    
    public static void main(String[] args) {
        loadData();
        Scanner sc = new Scanner(System.in);
        
        while (true) {
            System.out.println("\n=== BANKING SYSTEM ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose option: ");
            
            int choice = getIntInput(sc, 1, 3);
            
            switch (choice) {
                case 1 -> register(sc);
                case 2 -> login(sc);
                case 3 -> {
                    saveData();
                    System.out.println("Thank you for banking with us!");
                    return;
                }
            }
        }
    }
    
    private static void register(Scanner sc) {
        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();
        
        if (users.containsKey(username)) {
            System.out.println("❌ Username already exists!");
            return;
        }
        
        System.out.print("Enter password: ");
        String password = sc.nextLine();
        
        User user = new User(username, password);
        users.put(username, user);
        saveData();
        System.out.println("✅ Registration successful! Account: " + user.getAccountNumber());
    }
    
    private static void login(Scanner sc) {
        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine();
        
        User user = users.get(username);
        if (user != null && user.authenticate(password)) {
            System.out.println("✅ Login successful! Welcome, " + username);
            showAccountMenu(sc, user);
        } else {
            System.out.println("❌ Invalid credentials!");
        }
    }
    
    private static void showAccountMenu(Scanner sc, User user) {
        while (true) {
            System.out.println("\n=== ACCOUNT MENU ===");
            System.out.printf("Account: %d | Balance: $%.2f%n", user.getAccountNumber(), user.getBalance());
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Transfer Funds");
            System.out.println("4. Account Statement");
            System.out.println("5. Logout");
            System.out.print("Choose option: ");
            
            int choice = getIntInput(sc, 1, 5);
            
            switch (choice) {
                case 1 -> deposit(sc, user);
                case 2 -> withdraw(sc, user);
                case 3 -> transferFunds(sc, user);
                case 4 -> System.out.println(user.getStatement());
                case 5 -> {
                    System.out.println("Logged out successfully.");
                    return;
                }
            }
        }
    }
    
    private static void deposit(Scanner sc, User user) {
        System.out.print("Enter deposit amount: $");
        double amount = getDoubleInput(sc);
        if (user.deposit(amount)) {
            System.out.println("✅ Deposit successful! New balance: $" + user.getBalance());
        } else {
            System.out.println("❌ Invalid deposit amount!");
        }
    }
    
    private static void withdraw(Scanner sc, User user) {
        System.out.print("Enter withdrawal amount: $");
        double amount = getDoubleInput(sc);
        if (user.withdraw(amount)) {
            System.out.println("✅ Withdrawal successful! New balance: $" + user.getBalance());
        } else {
            System.out.println("❌ Insufficient funds or invalid amount!");
        }
    }
    
    private static void transferFunds(Scanner sc, User fromUser) {
        System.out.print("Enter recipient account number: ");
        long toAccount = sc.nextLong();
        sc.nextLine();
        
        User toUser = null;
        for (User user : users.values()) {
            if (user.getAccountNumber() == toAccount) {
                toUser = user;
                break;
            }
        }
        
        if (toUser == null) {
            System.out.println("❌ Recipient account not found!");
            return;
        }
        
        System.out.print("Enter transfer amount: $");
        double amount = getDoubleInput(sc);
        
        if (fromUser.withdraw(amount)) {
            toUser.deposit(amount);
            saveData();
            System.out.println("✅ Transfer successful!");
            System.out.printf("New balance: $%.2f%n", fromUser.getBalance());
        } else {
            System.out.println("❌ Transfer failed - insufficient funds!");
        }
    }
    
    // Utility methods for safe input
    private static int getIntInput(Scanner sc, int min, int max) {
        while (true) {
            try {
                int input = Integer.parseInt(sc.nextLine().trim());
                if (input >= min && input <= max) return input;
                System.out.print("Please enter a number between " + min + "-" + max + ": ");
            } catch (NumberFormatException e) {
                System.out.print("Invalid input! Please enter a number: ");
            }
        }
    }
    
    private static double getDoubleInput(Scanner sc) {
        while (true) {
            try {
                double input = Double.parseDouble(sc.nextLine().trim());
                if (input > 0) return input;
                System.out.print("Please enter a positive amount: $");
            } catch (NumberFormatException e) {
                System.out.print("Invalid amount! Please enter a number: $");
            }
        }
    }
    
    // Persistence methods
    @SuppressWarnings("unchecked")
    private static void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            users = (Map<String, User>) ois.readObject();
            System.out.println("Data loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("No existing data found. Starting fresh.");
            users = new HashMap<>();
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            users = new HashMap<>();
        }
    }
    
    private static void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
}
