package de.extremeenvironment.messageservice.service;

import de.extremeenvironment.messageservice.domain.Conversation;
import de.extremeenvironment.messageservice.domain.Message;
import de.extremeenvironment.messageservice.repository.ConversationRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by on 21.06.16.
 *
 * @author David Steiman
 */
@Service
public class ConversationService {
    private ConversationRepository conversationRepository;

    @Inject
    public void setConversationRepository(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    public void addMessageToConversation(Conversation conversation, Message message) {
        if (conversation == null || message == null) {
            throw new IllegalArgumentException("neither conversation nor message should be null");
        }

        conversation.getMessages().add(message);
        message.setConversation(conversation);
    }
}
