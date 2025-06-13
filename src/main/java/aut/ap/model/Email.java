package aut.ap.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "emails")
public class Email {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 6)
    private String code;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @OneToMany(mappedBy = "email", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<EmailRecipient> recipients;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    @Lob
    private String body;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    public Email () {}

    public Email (String code, User sender, List<EmailRecipient> recipients, String subject, String body, LocalDateTime sentAt) {
        this.code = code;
        this.sender = sender;
        this.recipients = recipients;
        this.subject = subject;
        this.body = body;
        this.sentAt = sentAt;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setRecipients(List<EmailRecipient> recipients) {
        this.recipients = recipients;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public User getSender() {
        return sender;
    }

    public List<EmailRecipient> getRecipients() {
        return recipients;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}
