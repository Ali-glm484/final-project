package aut.ap.model;

import jakarta.persistence.*;

@Entity
@Table(name = "email_recipients")
public class EmailRecipient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "email_id", nullable = false)
    private Email email;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    public EmailRecipient () {}

    public EmailRecipient (Email email, User recipient) {
        this.email = email;
        this.recipient = recipient;
        this.isRead = false;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public void setUser(User recipient) {
        this.recipient = recipient;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public int getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public User getUser() {
        return recipient;
    }

    public boolean getIsRead() {
        return isRead;
    }
}
