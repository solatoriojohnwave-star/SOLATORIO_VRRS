package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Optional;
import java.util.Scanner;

public class Authentication {

    private final Scanner scanner;
    private final UserDao userDao;

    public Authentication(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n=====================================");
            System.out.println("  JWSVideoke: Videoke Room Reservation");
            System.out.println("=====================================");
            System.out.println("[1] Register Account");
            System.out.println("[2] Login");
            System.out.println("[3] Exit");
            System.out.print("Choose: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> register();
                case "2" -> login();
                case "3" -> {
                    System.out.println("Thank you for using JWSVideoke. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }

    private void register() {
        System.out.println("\n--- Register Account ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }
        if (userDao.usernameExists(username)) {
            System.out.println("Username already taken. Try another one.");
            return;
        }

        String role = chooseRole();
        if (role == null) {
            System.out.println("Invalid role. Registration cancelled.");
            return;
        }

        if (userDao.register(username, password, role)) {
            System.out.println("Account registered! Role: " + role + " | Status: PENDING");
            System.out.println("Please wait for an admin to approve your account.");
        }
    }

    private String chooseRole() {
        System.out.println("Select role:");
        System.out.println("[1] ADMIN");
        System.out.println("[2] RECEPTIONISTS");
        System.out.println("[3] CUSTOMER");
        System.out.print("Choose: ");
        return switch (scanner.nextLine().trim()) {
            case "1" -> "ADMIN";
            case "2" -> "RECEPTIONISTS";
            case "3" -> "CUSTOMER";
            default -> null;
        };
    }

    private void login() {
        System.out.println("\n--- Login ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        Optional<User> result = userDao.login(username, password);
        if (result.isEmpty()) {
            System.out.println("Invalid username or password.");
            return;
        }

        User user = result.get();
        if (!user.status().equals("APPROVED")) {
            System.out.println("Your account is still " + user.status()
                    + ". Please wait for an admin to approve it.");
            return;
        }
        routeByRole(user);
    }

    // Role-based routing: sends the user straight to their own menu.
    private void routeByRole(User user) {
        System.out.println("\nLogin successful! Welcome, " + user.username() + ".");
        switch (user.roles()) {
            case "ADMIN"         -> new AdminMenu(scanner, userDao, user).show();
            case "RECEPTIONISTS" -> new ReceptionistMenu(scanner, userDao, user).show();
            case "CUSTOMER"      -> new CustomerMenu(scanner, userDao, user).show();
            default -> System.out.println("Unknown role: " + user.roles() + ". Access denied.");
        }
    }
}