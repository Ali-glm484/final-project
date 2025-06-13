package aut.ap;

import aut.ap.model.Email;
import aut.ap.model.User;
import aut.ap.service.EmailService;
import aut.ap.service.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static String normalizeEmail(String emailInput) {
        if (emailInput == null || emailInput.isBlank())
            return null;

        if (!emailInput.contains("@"))
            return emailInput + "@milou.com";

        return emailInput;
    }

    private static List<User> createListOfRecipientUsers() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Recipient(s): ");
        String recipientInput = scanner.nextLine();

        List<String> emailStrings = Arrays.asList(recipientInput.split("\\s*,\\s*"));

        List<String> normalizedEmailStrings = new ArrayList<>();
        for (String e : emailStrings)
            normalizedEmailStrings.add(normalizeEmail(e));

        List<User> recipientUsers = UserService.findUsersByEmails(normalizedEmailStrings);

        return recipientUsers;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("[L]og in, [S]ign up: ");
            switch (scanner.nextLine()) {
                case "S", "Sign up", "s", "sign up":
                    while (true) {
                        System.out.print("Name: ");
                        String name = scanner.nextLine();
                        System.out.print("Email: ");
                        String email = normalizeEmail(scanner.nextLine());
                        System.out.print("Password: ");
                        String password = scanner.nextLine();

                        try {
                            UserService.createUser(name, email, password);

                            break;

                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                            System.out.println("Try again.");
                        }
                    }

                    System.out.println("Your new account is created.");
                    System.out.println("Go ahead and login!");

                    break;

                case "L", "Log in", "l", "log in":
                    User user;

                    while (true) {
                        System.out.print("Email: ");
                        String email = normalizeEmail(scanner.nextLine());
                        System.out.print("Password: ");
                        String password = scanner.nextLine();

                        try {
                            user = UserService.authenticate(email, password);

                            break;

                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                            System.out.println("Try again.");
                        }
                    }

                    System.out.println("Welcome back, " + user.getName() + "!");
                    System.out.println();

                    while (true) {
                        int flag = 0;
                        System.out.print("[S]end, [V]iew, [R]eply, [F]orward, [L]og out: ");

                        switch (scanner.nextLine()) {
                            case "S", "Send", "s", "send":
                                Email email;

                                while (true) {
                                    List<User> recipientUsers = createListOfRecipientUsers();

                                    System.out.print("Subject: ");
                                    String subject = scanner.nextLine();
                                    System.out.print("Body: ");
                                    String body = scanner.nextLine();

                                    try {
                                        email = EmailService.sendEmail(user, recipientUsers, subject, body);

                                        break;

                                    } catch (IllegalArgumentException e) {
                                        System.err.println(e.getMessage());
                                        System.out.println("Try again.");
                                    }
                                }

                                System.out.println("Successfully sent your email.");
                                System.out.println("Code: " + email.getCode());

                                break;

                            case "V", "View", "v", "view":
                                while (true) {
                                    int flag2 = 0;

                                    System.out.print("[A]ll emails, [U]nread emails, [S]ent emails, Read by [C]ode, [B]ack: ");

                                    switch (scanner.nextLine()) {
                                        case "A", "a":
                                            List<Email> allReceivedEmails = EmailService.getReceivedEmails(user, false);

                                            System.out.println("All Emails:");
                                            for (Email e : allReceivedEmails)
                                                System.out.println("+ " + e.getSender().getEmail() + " - " + e.getSubject() + " (" + e.getCode() + ")");
                                            System.out.println();

                                            break;

                                        case "U", "u":
                                            List<Email> unreadEmails = EmailService.getReceivedEmails(user, true);

                                            System.out.println("Unread Emails:");
                                            for (Email e : unreadEmails)
                                                System.out.println("+ " + e.getSender().getEmail() + " - " + e.getSubject() + " (" + e.getCode() + ")");
                                            System.out.println();

                                            break;

                                        case "s", "S":
                                            List<Email> sentEmails = EmailService.getSentEmails(user);

                                            System.out.println("Sent Emails:");
                                            for (Email e : sentEmails) {
                                                System.out.print("+ ");
                                                for (int i = 0; i < e.getRecipients().size(); i++) {
                                                    if (i < e.getRecipients().size() - 1)
                                                        System.out.print(e.getRecipients().get(i).getUser().getEmail() + ", ");

                                                    else
                                                        System.out.print(e.getRecipients().get(i).getUser().getEmail() + " ");

                                                }
                                                System.out.println("- " + e.getSubject() + " (" + e.getCode() + ")");
                                            }

                                            System.out.println();

                                            break;

                                        case "C", "c":
                                            System.out.print("Code: ");
                                            String code = scanner.nextLine();

                                            try {
                                                Email email1 = EmailService.findEmailByCode(code);

                                                EmailService.validateCode(email1, user);

                                                System.out.println("Code: " + email1.getCode());
                                                System.out.print("Recipient(s): ");

                                                for (int i = 0; i < email1.getRecipients().size(); i++) {
                                                    if (i < email1.getRecipients().size() - 1)
                                                        System.out.print(email1.getRecipients().get(i).getUser().getEmail() + ", ");

                                                    else
                                                        System.out.println(email1.getRecipients().get(i).getUser().getEmail());

                                                }

                                                System.out.println("Subject: " + email1.getSubject());
                                                System.out.println("Date: " + email1.getSentAt());
                                                System.out.println();
                                                System.out.println(email1.getBody());

                                                EmailService.markAsRead(email1, user);


                                            } catch (IllegalArgumentException e) {
                                                System.err.println(e.getMessage());
                                            }

                                            break;

                                        case "B", "b":
                                            flag2 = 1;

                                            System.out.println("Return to menu...");
                                            System.out.println();

                                            break;


                                        default:
                                            System.out.println("Invalid command.");

                                            break;
                                    }

                                    if (flag2 == 1)
                                        break;
                                }

                                break;

                            case "R", "Reply", "r", "reply":
                                System.out.print("Code: ");
                                String code = scanner.nextLine();
                                System.out.print("Body: ");
                                String body = scanner.nextLine();

                                try {
                                    Email originalEmail = EmailService.findEmailByCode(code);

                                    EmailService.validateCode(originalEmail, user);


                                    Email replyEmail = EmailService.replyToEmail(originalEmail, user, body);

                                    System.out.println("Successfully sent your reply to email " + originalEmail.getCode() + ".");
                                    System.out.println("Code: " + replyEmail.getCode());

                                } catch (IllegalArgumentException e) {
                                    System.err.println(e.getMessage());
                                }

                                break;

                            case "F", "Forward", "f", "forward":
                                System.out.print("Code: ");
                                String code1 = scanner.nextLine();

                                List<User> recipientUsers = createListOfRecipientUsers();

                                try {
                                    Email originalEmail = EmailService.findEmailByCode(code1);

                                    EmailService.validateCode(originalEmail, user);

                                    Email forwardEmail = EmailService.forwardEmail(originalEmail, user, recipientUsers);

                                    System.out.println("Successfully forwarded your email.");
                                    System.out.println("Code: " + forwardEmail.getCode());

                                } catch (IllegalArgumentException e) {
                                    System.err.println(e.getMessage());
                                }

                                break;

                            case "L", "Log out", "l", "log out":
                                flag = 1;

                                System.out.println("By Tehran :)");
                                System.out.println();

                                break;

                            default:
                                System.out.println("Invalid command.");

                                break;
                        }

                        if (flag == 1)
                            break;
                    }

                    break;

                default:
                    System.out.println("Invalid command.");
                    break;
            }
        }
    }
}