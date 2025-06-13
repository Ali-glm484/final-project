package aut.ap.service;

import aut.ap.framework.SingletonSessionFactory;
import aut.ap.model.User;

import java.util.ArrayList;
import java.util.List;

public final class UserService {
    private UserService() {}

    public static User createUser(String name, String email, String password) throws Exception {
        if (name == null || name.isEmpty() || password == null || password.isBlank() || email.isBlank() || email == null)
            throw new IllegalArgumentException("Name, email, and password cannot be empty.");

        if (findUserByEmail(email) != null)
            throw new Exception("This email is already registered in the system.");

        if (password.length() < 8 )
            throw new Exception("Password must be at least 8 characters long.");

        return SingletonSessionFactory.get().fromTransaction(session -> {
            User user = new User(name, email, password);
            session.persist(user);
            return user;
        });
    }

    public static User findUserByEmail(String email) throws IllegalArgumentException {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email can not be empty.");

        return SingletonSessionFactory.get().fromTransaction(session -> {
            String sql = "select * from users where email = :email_param";

            return session.createNativeQuery(sql, User.class)
                    .setParameter("email_param", email)
                    .getSingleResultOrNull();
        });
    }

    public static User authenticate(String email, String password) throws Exception{
        User user = findUserByEmail(email);

        if (user == null)
            throw new Exception("There is no user with this email.");

        if (!user.getPassword().equals(password))
            throw new Exception("The password is not correct.");

        return user;
    }

    public static List<User> findUsersByEmails(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return new ArrayList<>();
        }

        return SingletonSessionFactory.get().fromTransaction(session -> {
            String sql = "SELECT * FROM users WHERE email IN (:emailList)";

            return session.createNativeQuery(sql, User.class)
                    .setParameter("emailList", emails)
                    .getResultList();
        });
    }
}
