package de.extremeenvironment.messageservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A Conversation.
 */
@Entity
@Table(name = "conversation")
public class Conversation extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 46757L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "title")
    private String title;

    @Column
    private String type = "default";

    @Column
    private Long matchedActionId;

    @OneToMany(
        mappedBy = "conversation",
        orphanRemoval = true,
        fetch = FetchType.EAGER,
        cascade = CascadeType.REMOVE
    )
    @JsonIgnore
    private Set<Message> messages = new HashSet<>();

    @ManyToMany(mappedBy = "conversations", cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<UserHolder> users = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<Message> getMessages() {
        return messages;
    }

    public void setMessages(Set<Message> messages) {
        this.messages = messages;
    }

    public Set<UserHolder> getUsers() {
        return users;
    }

    public void setUsers(Set<UserHolder> userHolders) {
        this.users = userHolders;
    }

    public void addMessage(Message message) {
        message.setConversation(this);
        messages.add(message);
    }

    public void addMember(UserHolder userHolder) {
        userHolder.getConversations().add(this);
        users.add(userHolder);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getMatchedActionId() {
        return matchedActionId;
    }

    public void setMatchedActionId(Long matchedActionId) {
        this.matchedActionId = matchedActionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Conversation conversation = (Conversation) o;
        if(conversation.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, conversation.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Conversation{" +
            "id=" + id +
            ", active='" + active + "'" +
            ", title='" + title + "'" +
            '}';
    }
}
