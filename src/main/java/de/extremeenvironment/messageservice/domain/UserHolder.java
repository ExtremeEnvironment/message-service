package de.extremeenvironment.messageservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A UserHolder.
 */
@Entity
@Table(name = "user_holder")
public class UserHolder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private Set<Message> messages = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "user_holder_conversation",
               joinColumns = @JoinColumn(name="user_holders_id", referencedColumnName="ID"),
               inverseJoinColumns = @JoinColumn(name="conversations_id", referencedColumnName="ID"))
    private Set<Conversation> conversations = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Set<Message> getMessages() {
        return messages;
    }

    public void setMessages(Set<Message> messages) {
        this.messages = messages;
    }

    public Set<Conversation> getConversations() {
        return conversations;
    }

    public void setConversations(Set<Conversation> conversations) {
        this.conversations = conversations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserHolder userHolder = (UserHolder) o;
        if(userHolder.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, userHolder.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "UserHolder{" +
            "id=" + id +
            ", userId='" + userId + "'" +
            '}';
    }
}
