package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class CustomerMenu {

    private final Scanner scanner;
    private final UserDao userDao;
    private final User user;

    public CustomerMenu(Scanner scanner, UserDao userDao, User user) {
        this.scanner = scanner;
        this.userDao = userDao;
        this.user = user;
    }

    public void show() {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- CUSTOMER Dashboard ---");
            System.out.println("Username : " + user.username());
            System.out.println("Role     : " + user.roles());
            System.out.println("Status   : " + user.status());
            System.out.println("[1] Logout");
            System.out.print("Choose: ");

            if (scanner.nextLine().trim().equals("1")) {
                System.out.println("You have been logged out.");
                loggedIn = false;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }
}