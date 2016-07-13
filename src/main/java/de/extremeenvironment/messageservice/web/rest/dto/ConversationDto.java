package de.extremeenvironment.messageservice.web.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.extremeenvironment.messageservice.domain.Conversation;
import de.extremeenvironment.messageservice.domain.UserHolder;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by on 22.06.16.
 *
 * @author David Steiman
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConversationDto {
    private Long id;
    private boolean active = true;
    private String title;
    private String type;
    private Long matchedActionId;
    private Set<UserHolder> users = new HashSet<>();

    public ConversationDto() {
    }

    public ConversationDto(Conversation conversation) {
        id = conversation.getId();
        active = conversation.isActive();
        title = conversation.getTitle();
        users = conversation.getUsers();
    }



    public Conversation toConversation() {
        Conversation conversation = new Conversation();

        conversation.setId(id);
        conversation.setActive(active);
        conversation.setTitle(title);
        conversation.setUsers(users);
        conversation.setType(type);
        conversation.setMatchedActionId(matchedActionId);

        return conversation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Set<UserHolder> getUsers() {
        return users;
    }

    public void setUsers(Set<UserHolder> users) {
        this.users = users;
    }
}
