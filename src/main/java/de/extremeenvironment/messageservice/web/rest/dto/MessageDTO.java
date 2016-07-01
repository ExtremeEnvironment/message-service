package de.extremeenvironment.messageservice.web.rest.dto;

import de.extremeenvironment.messageservice.domain.Message;

import java.time.ZonedDateTime;

/**
 * Created by on 01.07.16.
 *
 * @author David Steiman
 */
public class MessageDTO {
    private Long id;
    private String messageText;
    private String messageUser;
    private ZonedDateTime messageDate;

    public MessageDTO() {
    }

    public MessageDTO(Message message) {
        this.id = message.getId();
        this.messageText = message.getMessageText();
        this.messageUser = message.getUser().getUsername();
        this.messageDate = message.getCreatedDate();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public ZonedDateTime getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(ZonedDateTime messageDate) {
        this.messageDate = messageDate;
    }
}
