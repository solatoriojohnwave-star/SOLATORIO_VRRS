package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    // Valid values used by the validation wrappers
    private static final List<String> VALID_ROLES = List.of("ADMIN", "RECEPTIONISTS", "CUSTOMER");
    private static final List<String> VALID_STATUSES = List.of("APPROVED", "PENDING");

    private final Scanner scanner;
    private final UserDao userDao;
    private final User user;

    public AdminMenu(Scanner scanner, UserDao userDao, User user) {
        this.scanner = scanner;
        this.userDao = userDao;
        this.user = user;
    }

    public void show() {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- ADMIN Dashboard ---");
            System.out.println("Username : " + user.username());
            System.out.println("Role     : " + user.roles());
            System.out.println("Status   : " + user.status());
            System.out.println("[1] View All System Users");
            System.out.println("[2] View Pending Registrations");
            System.out.println("[3] Approve User Account");
            System.out.println("[4] Update User Role and Status");
            System.out.println("[5] Logout");
            System.out.print("Choose: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUserAccount();
                case "4" -> updateUserRoleAndStatus();
                case "5" -> {
                    System.out.println("You have been logged out.");
                    loggedIn = false;
                }
                default -> System.out.println("Invalid choice. Please enter 1-5.");
            }
        }
    }

    private void viewAllUsers() {
        System.out.println("\n--- All System Users ---");
        printUserTable(userDao.getAllUsers());
    }

    private void viewPendingUsers() {
        System.out.println("\n--- Pending Registrations ---");
        printUserTable(userDao.getPendingUsers());
    }

    private void approveUserAccount() {
        System.out.println("\n--- Approve User Account ---");
        List<User> pending = userDao.getPendingUsers();
        printUserTable(pending);

        if (pending.isEmpty()) {
            return;
        }

        while (true) {
            int targetId = readIntInput("Enter Target User ID to approve (0 to cancel): ");

            if (targetId == 0) {
                System.out.println("Approval cancelled.");
                return;
            }

            // Verify the ID exists in the pending list
            User target = null;
            for (User u : pending) {
                if (u.id() == targetId) {
                    target = u;
                    break;
                }
            }

            if (target == null) {
                System.out.println("User ID " + targetId + " is not in the pending list. Try again.");
                continue;
            }

            if (userDao.approveUser(targetId)) {
                System.out.println("Success! Account '" + target.username()
                        + "' (" + target.roles() + ") has been APPROVED.");
            } else {
                System.out.println("Approval failed. Please try again.");
            }
            return;
        }
    }

    // NEW: update the role and status of another user
    private void updateUserRoleAndStatus() {
        System.out.println("\n--- Update User Role and Status ---");
        List<User> users = userDao.getAllUsers();
        printUserTable(users);

        if (users.isEmpty()) {
            return;
        }

        // Step 1: ask for the target User ID
        User target = null;
        while (target == null) {
            int targetUserId = readIntInput("Enter Target User ID to update (0 to cancel): ");

            if (targetUserId == 0) {
                System.out.println("Update cancelled.");
                return;
            }

            // Self-edit restriction
            if (targetUserId == user.id()) {
                System.out.println("WARNING: Modifying the currently active admin account is restricted.");
                continue;
            }

            for (User u : users) {
                if (u.id() == targetUserId) {
                    target = u;
                    break;
                }
            }

            if (target == null) {
                System.out.println("User ID " + targetUserId + " does not exist. Try again.");
            }
        }

        // Step 2: ask for the new role (null means the admin typed 0 to cancel)
        String newRole = readValidRole();
        if (newRole == null) {
            System.out.println("Update cancelled.");
            return;
        }

        // Step 3: ask for the new status (null means the admin typed 0 to cancel)
        String newStatus = readValidStatus();
        if (newStatus == null) {
            System.out.println("Update cancelled.");
            return;
        }

        if (userDao.updateUserRoleAndStatus(target.id(), newRole, newStatus)) {
            System.out.println("Success! Account '" + target.username()
                    + "' is now " + newRole + " with status " + newStatus + ".");
        } else {
            System.out.println("Update failed. Please try again.");
        }
    }

    // ---------------- Input validation wrappers ----------------

    // Keeps asking until the admin enters a whole number. Letters/symbols never crash the app.
    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric value.");
            }
        }
    }

    // Keeps asking until a valid role is entered. Returns null if the admin enters 0 (cancel).
    private String readValidRole() {
        while (true) {
            System.out.print("Enter new role " + VALID_ROLES + " (0 to cancel): ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("0")) {
                return null;
            }
            if (VALID_ROLES.contains(input)) {
                return input;
            }
            System.out.println("Invalid role. Valid roles are: " + VALID_ROLES);
        }
    }

    // Keeps asking until a valid status is entered. Returns null if the admin enters 0 (cancel).
    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new status " + VALID_STATUSES + " (0 to cancel): ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("0")) {
                return null;
            }
            if (VALID_STATUSES.contains(input)) {
                return input;
            }
            System.out.println("Invalid status. Valid statuses are: " + VALID_STATUSES);
        }
    }

    // Reusable table renderer for any list of users
    private void printUserTable(List<User> users) {
        String border = "+-------+-----------------+-----------------+------------+";
        String rowFormat = "| %-5s | %-15s | %-15s | %-10s |%n";

        System.out.println(border);
        System.out.printf(rowFormat, "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println(border);

        if (users.isEmpty()) {
            System.out.printf("| %-47s |%n", "No records found.");
        } else {
            for (User u : users) {
                System.out.printf(rowFormat, u.id(), u.username(), u.roles(), u.status());
            }
        }
        System.out.println(border);
    }
}