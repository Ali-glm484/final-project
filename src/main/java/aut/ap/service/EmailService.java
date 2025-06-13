package aut.ap.service;

import aut.ap.framework.SingletonSessionFactory;
import aut.ap.model.Email;
import aut.ap.model.EmailRecipient;
import aut.ap.model.User;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class EmailService {
    private EmailService() {}

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random RANDOM = new SecureRandom();
    private static final int CODE_LENGTH = 6;

    private static String generateUniqueCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
            }
            code = sb.toString();
        } while (isCodeInUse(code));

        return code;
    }

    private static boolean isCodeInUse(String code) {
        Email existingEmail = SingletonSessionFactory.get().fromTransaction(session -> {
            String sql = "SELECT * FROM emails WHERE code = :code_param";
            return session.createNativeQuery(sql, Email.class)
                    .setParameter("code_param", code)
                    .getSingleResultOrNull();
        });

        return existingEmail != null;
    }

    public static Email sendEmail(User sender, List<User> recipients, String subject, String body) throws IllegalArgumentException {
        if (sender == null || recipients == null || recipients.size() == 0 || subject == null || subject.isBlank() || body == null || body.isBlank())
            throw new IllegalArgumentException("Sender, recipients, subject and body can not be empty.");

        if (subject.length() > 75)
            throw new IllegalArgumentException("Subject must be at lest 75 characters long.");

        Email email = new Email(generateUniqueCode(), sender, null, subject, body, LocalDateTime.now());

        List<EmailRecipient> recipientList = new ArrayList<>();
        for (User user : recipients) {
            recipientList.add(new EmailRecipient(email, user));
        }

        email.setRecipients(recipientList);

        SingletonSessionFactory.get().inTransaction(session -> {
            session.persist(email);
        });

        return email;
    }

    public static List<Email> getSentEmails(User user) {
        return SingletonSessionFactory.get().fromTransaction(session -> {
            String sql = "select * from emails where sender_id = :userId order by sent_at desc";

            return session.createNativeQuery(sql, Email.class)
                    .setParameter("userId", user.getId())
                    .getResultList();
        });
    }

    public static List<Email> getReceivedEmails(User user, boolean unreadOnly) {
        String baseQuery = "select e.* from emails e " +
                "join email_recipients er on e.id = er.email_id " +
                "where er.recipient_id = :userId ";

        if (unreadOnly)
            baseQuery += " and er.is_read = false ";

        String finalQuery = baseQuery + " order by e.sent_at desc";

        return SingletonSessionFactory.get().fromTransaction(session -> {
            return session.createNativeQuery(finalQuery, Email.class)
                    .setParameter("userId", user.getId())
                    .getResultList();
        });
    }

    public static Email findEmailByCode(String code) throws IllegalArgumentException {
        if (code == null || code.isBlank())
            throw new IllegalArgumentException("The code can not be empty.");

        String sql = "select * from emails where code = :cd";

        Email email = SingletonSessionFactory.get().fromTransaction(session -> {
            return session.createNativeQuery(sql, Email.class)
                    .setParameter("cd", code)
                    .getSingleResultOrNull();
        });

        if (email == null) {
            throw new IllegalArgumentException("The code is Invalid.");
        }

        return email;
    }

    public static void markAsRead(Email email, User user) throws RuntimeException {
        if (email == null || user == null)
            throw new IllegalArgumentException("Email and user can ot be empty.");

        SingletonSessionFactory.get().inTransaction(session -> {
            String sql = "select * from email_recipients where recipient_id = :userId and email_id = :emailId";

            EmailRecipient emailRecipient = session.createNativeQuery(sql, EmailRecipient.class)
                    .setParameter("userId", user.getId())
                    .setParameter("emailId", email.getId())
                    .getSingleResultOrNull();

            if (emailRecipient == null)
                throw new RuntimeException("This email does not exist for the user.");

            emailRecipient.setRead(true);
        });
    }

    public static void validateCode(Email email, User user) throws IllegalArgumentException {
        if (email.getSender().getId() != user.getId()) {
            int flag1 = 0;

            for (EmailRecipient recipient : email.getRecipients())
                if (recipient.getUser().getId() == user.getId()) {
                    flag1 = 1;

                    break;
                }

            if (flag1 == 0)
                throw new IllegalArgumentException("You cannot read this email.");
        }
    }

    public static Email replyToEmail(Email originalEmail, User replierUser, String replyBody) {
        List<User> recipients = new ArrayList<>();

        recipients.add(originalEmail.getSender());
        for (EmailRecipient recipient : originalEmail.getRecipients())
            if (recipient.getUser().getId() != replierUser.getId())
                recipients.add(recipient.getUser());

        Email email = sendEmail(replierUser, recipients, "[Re] " + originalEmail.getSubject(), replyBody);

        return email;
    }

    public static Email forwardEmail(Email originalEmail, User forwarderUser, List<User> recipientUsers) {
        Email email = sendEmail(forwarderUser, recipientUsers, "[Fw] " + originalEmail.getSubject(), originalEmail.getBody());

        return email;
    }
}
