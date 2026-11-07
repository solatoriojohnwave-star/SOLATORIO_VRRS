package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.RoomDao;
import com.scc.dao.UserDao;
import com.scc.model.ActivityLog;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private static final List<String> VALID_ROLES = List.of("ADMIN", "RECEPTIONISTS", "CUSTOMER");
    private static final List<String> VALID_STATUSES = List.of("APPROVED", "PENDING");
    private static final List<String> ROOM_STATUSES = List.of("AVAILABLE", "OCCUPIED", "UNDER MAINTENANCE");

    private final Scanner scanner;
    private final UserDao userDao;
    private final RoomDao roomDao;
    private final ActivityLogDao logDao;
    private final User user;

    public AdminMenu(Scanner scanner, UserDao userDao, User user) {
        this.scanner = scanner;
        this.userDao = userDao;
        this.roomDao = new RoomDao();
        this.logDao = new ActivityLogDao();
        this.user = user;
    }

    public void show() {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n========================================================");
            System.out.println("                     ADMIN DASHBOARD                    ");
            System.out.println("========================================================");
            System.out.println("Logged in as: " + user.username() + " (" + user.roles() + ")");
            System.out.println("--------------------------------------------------------");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User");
            System.out.println("6. Delete User");
            System.out.println("7. Manage Videoke Rooms & Types");
            System.out.println("8. View System Audit Logs");
            System.out.println("9. Logout");
            System.out.println("========================================================");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUserAccount();
                case "4" -> addNewUserPrompt();
                case "5" -> updateUserRoleAndStatus();
                case "6" -> System.out.println("\n[Feature placeholder for Delete User]");
                case "7" -> manageRoomsAndTypes();
                case "8" -> viewSystemAuditLogs();
                case "9" -> {
                    System.out.println("\nYou have been logged out.");
                    loggedIn = false;
                }
                default -> System.out.println("\nInvalid choice. Please enter a number between 1 and 9.");
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

        if (pending.isEmpty()) return;

        while (true) {
            int targetId = readIntInput("Enter Target User ID to approve (0 to cancel): ");
            if (targetId == 0) {
                System.out.println("Approval cancelled.");
                return;
            }

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

            if (userDao.approveUser(user.id(), targetId, target.username())) {
                System.out.println("Success! Account '" + target.username() + "' has been APPROVED.");
            } else {
                System.out.println("Approval failed. Please try again.");
            }
            return;
        }
    }

    private void addNewUserPrompt() {
        System.out.println("\n--- Add New User ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();
        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }
        String role = readValidRole();
        if (role == null) return;

        if (userDao.register(username, password, role)) {
            System.out.println("User successfully added as PENDING!");
        } else {
            System.out.println("Failed to add user. Username may already exist.");
        }
    }

    private void updateUserRoleAndStatus() {
        System.out.println("\n--- Update User Role and Status ---");
        List<User> users = userDao.getAllUsers();
        printUserTable(users);

        if (users.isEmpty()) return;

        User target = null;
        while (target == null) {
            int targetUserId = readIntInput("Enter Target User ID to update (0 to cancel): ");
            if (targetUserId == 0) {
                System.out.println("Update cancelled.");
                return;
            }
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

        String newRole = readValidRole();
        if (newRole == null) return;
        String newStatus = readValidStatus();
        if (newStatus == null) return;

        if (userDao.updateUserRoleAndStatus(user.id(), target.id(), newRole, newStatus)) {
            System.out.println("Success! Account '" + target.username() + "' is now " + newRole + " with status " + newStatus + ".");
        } else {
            System.out.println("Update failed. Please try again.");
        }
    }

    private void manageRoomsAndTypes() {
        boolean managing = true;
        while (managing) {
            System.out.println("\n========================================================");
            System.out.println("              MANAGE VIDEOKE ROOMS & TYPES              ");
            System.out.println("========================================================");
            System.out.println("[1] View All Room Types & Rates");
            System.out.println("[2] Add New Room Type");
            System.out.println("[3] View All Videoke Rooms");
            System.out.println("[4] Add New Videoke Room");
            System.out.println("[5] Update Room Status (Available / Occupied / Maintenance)");
            System.out.println("[6] Delete Closed Room");
            System.out.println("[7] Return to Admin Dashboard");
            System.out.println("========================================================");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewRoomTypes();
                case "2" -> addRoomTypePrompt();
                case "3" -> viewRooms();
                case "4" -> addRoomPrompt();
                case "5" -> updateRoomStatusPrompt();
                case "6" -> deleteRoomPrompt();
                case "7" -> managing = false;
                default -> System.out.println("Invalid choice. Please enter 1-7.");
            }
        }
    }

    private void viewRoomTypes() {
        List<RoomDao.RoomType> types = roomDao.getAllRoomTypes();
        String border = "+-------+---------------------+-------------------+------------+";
        String rowFormat = "| %-5s | %-19s | %-17s | %-10s |%n";

        System.out.println("\n" + border);
        System.out.printf(rowFormat, "ID", "TYPE NAME", "RATE / HOUR", "CAPACITY");
        System.out.println(border);

        if (types.isEmpty()) {
            System.out.printf("| %-59s |%n", "No room types found.");
        } else {
            for (RoomDao.RoomType rt : types) {
                System.out.printf(rowFormat, rt.id(), rt.typeName(), String.format("P%.2f", rt.ratePerHour()), rt.capacity());
            }
        }
        System.out.println(border + "\n");
    }

    private void addRoomTypePrompt() {
        System.out.println("\n--- Add New Room Type ---");
        System.out.print("Enter Type Name (e.g., Small, VIP, Party): ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Type name cannot be empty.");
            return;
        }
        double rate = readDoubleInput("Enter Rate Per Hour (PHP): ");
        int capacity = readIntInput("Enter Room Capacity (number of persons): ");

        if (roomDao.addRoomType(user.id(), name, rate, capacity)) {
            System.out.println("Successfully added room type: " + name);
        } else {
            System.out.println("Failed to add room type. Name might already exist.");
        }
    }

    private void viewRooms() {
        List<RoomDao.Room> rooms = roomDao.getAllRooms();
        String border = "+-------+---------------------+---------------------+-------------------+----------+----------------------+";
        String rowFormat = "| %-5s | %-19s | %-19s | %-17s | %-8s | %-20s |%n";

        System.out.println("\n" + border);
        System.out.printf(rowFormat, "ID", "ROOM NAME", "TYPE", "RATE / HR", "CAPACITY", "STATUS");
        System.out.println(border);

        if (rooms.isEmpty()) {
            System.out.printf("| %-95s |%n", "No rooms found.");
        } else {
            for (RoomDao.Room r : rooms) {
                System.out.printf(rowFormat, r.id(), r.roomName(), r.typeName(), String.format("P%.2f", r.ratePerHour()), r.capacity(), r.status());
            }
        }
        System.out.println(border + "\n");
    }

    private void addRoomPrompt() {
        System.out.println("\n--- Add New Videoke Room ---");
        viewRoomTypes();
        List<RoomDao.RoomType> types = roomDao.getAllRoomTypes();
        if (types.isEmpty()) {
            System.out.println("Please add a room type first.");
            return;
        }

        int typeId = readIntInput("Enter Room Type ID to assign: ");
        boolean valid = types.stream().anyMatch(rt -> rt.id() == typeId);
        if (!valid) {
            System.out.println("Invalid Room Type ID.");
            return;
        }

        System.out.print("Enter Room Name/Number (e.g., Room 01, Diamond Suite): ");
        String roomName = scanner.nextLine().trim();
        if (roomName.isEmpty()) {
            System.out.println("Room name cannot be empty.");
            return;
        }

        if (roomDao.addRoom(user.id(), roomName, typeId)) {
            System.out.println("Successfully added videoke room: " + roomName);
        } else {
            System.out.println("Failed to add room. Room name might already exist.");
        }
    }

    private void updateRoomStatusPrompt() {
        System.out.println("\n--- Update Room Status ---");
        viewRooms();
        List<RoomDao.Room> rooms = roomDao.getAllRooms();
        if (rooms.isEmpty()) return;

        int roomId = readIntInput("Enter Room ID to update (0 to cancel): ");
        if (roomId == 0) return;

        boolean exists = rooms.stream().anyMatch(r -> r.id() == roomId);
        if (!exists) {
            System.out.println("Room ID not found.");
            return;
        }

        System.out.println("Select new status: " + ROOM_STATUSES);
        System.out.print("Enter status (AVAILABLE / OCCUPIED / UNDER MAINTENANCE): ");
        String status = scanner.nextLine().trim().toUpperCase();

        if (!ROOM_STATUSES.contains(status)) {
            System.out.println("Invalid status choice.");
            return;
        }

        if (roomDao.updateRoomStatus(user.id(), roomId, status)) {
            System.out.println("Room status successfully updated to " + status + "!");
        } else {
            System.out.println("Update failed.");
        }
    }

    private void deleteRoomPrompt() {
        System.out.println("\n--- Delete Closed Room ---");
        viewRooms();
        List<RoomDao.Room> rooms = roomDao.getAllRooms();
        if (rooms.isEmpty()) return;

        int roomId = readIntInput("Enter Room ID to delete (0 to cancel): ");
        if (roomId == 0) return;

        if (roomDao.deleteRoom(user.id(), roomId)) {
            System.out.println("Room successfully deleted.");
        } else {
            System.out.println("Deletion failed.");
        }
    }

    private void viewSystemAuditLogs() {
        boolean viewingLogs = true;
        while (viewingLogs) {
            System.out.println("\n========================================================");
            System.out.println("                   SYSTEM AUDIT LOGS                    ");
            System.out.println("========================================================");
            System.out.println("[1] View All System Logs");
            System.out.println("[2] Filter Logs by User ID");
            System.out.println("[3] Return to Admin Dashboard");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> printLogTable(logDao.getAllLogs());
                case "2" -> {
                    int uid = readIntInput("Enter User ID to filter by: ");
                    printLogTable(logDao.getLogsByUserId(uid));
                }
                case "3" -> viewingLogs = false;
                default -> System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }

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

    private double readDoubleInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid amount.");
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.print("Enter new role " + VALID_ROLES + " (0 to cancel): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("0")) return null;
            if (VALID_ROLES.contains(input)) return input;
            System.out.println("Invalid role. Valid roles are: " + VALID_ROLES);
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new status " + VALID_STATUSES + " (0 to cancel): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("0")) return null;
            if (VALID_STATUSES.contains(input)) return input;
            System.out.println("Invalid status. Valid statuses are: " + VALID_STATUSES);
        }
    }

    private void printUserTable(List<User> users) {
        String border = "+-------+---------------------+---------------------+------------+";
        String rowFormat = "| %-5s | %-19s | %-19s | %-10s |%n";

        System.out.println("\n" + border);
        System.out.printf(rowFormat, "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println(border);

        if (users.isEmpty()) {
            System.out.printf("| %-59s |%n", "No records found.");
        } else {
            for (User u : users) {
                System.out.printf(rowFormat, u.id(), u.username(), u.roles(), u.status());
            }
        }
        System.out.println(border + "\n");
    }

    private void printLogTable(List<ActivityLog> logs) {
        // Expanded Description column width to 65 to ensure description never overflows
        String border = "+-------+---------+----------------------+-----------------------------------------------------------------+---------------------+";
        String rowFormat = "| %-5s | %-7s | %-20s | %-63s | %-19s |%n";

        System.out.println("\n" + border);
        System.out.printf(rowFormat, "ID", "USER ID", "ACTION TYPE", "DESCRIPTION", "TIMESTAMP");
        System.out.println(border);

        if (logs.isEmpty()) {
            System.out.printf("| %-125s |%n", "No audit logs found.");
        } else {
            for (ActivityLog log : logs) {
                System.out.printf(rowFormat, log.id(), log.userId(), log.actionType(), log.description(), log.timestamp());
            }
        }
        System.out.println(border + "\n");
    }
}