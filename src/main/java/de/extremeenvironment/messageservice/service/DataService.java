package de.extremeenvironment.messageservice.service;

import de.extremeenvironment.messageservice.domain.Conversation;
import de.extremeenvironment.messageservice.domain.Message;
import de.extremeenvironment.messageservice.domain.UserHolder;
import de.extremeenvironment.messageservice.repository.ConversationRepository;
import de.extremeenvironment.messageservice.repository.MessageRepository;
import de.extremeenvironment.messageservice.repository.UserHolderRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Jonathan on 08.07.2016.
 */
@Service
public class DataService {


    @Inject
    ConversationRepository conversationRepository;

    @Inject
    UserHolderRepository userHolderRepository;

    @Inject
    MessageRepository messageRepository;

    @PostConstruct
    public void createDate() {

        UserHolder userHolder= new UserHolder();
        userHolder.setUserId(3L);
        userHolder.setUsername("admin");
        userHolderRepository.saveAndFlush(userHolder);

        UserHolder userHolder2= new UserHolder();
        userHolder2.setUserId(4L);
        userHolder2.setUsername("user");
        userHolderRepository.saveAndFlush(userHolder2);

        Conversation conversation= new Conversation();
        conversation.addMember(userHolder);
        conversation.addMember(userHolder2);
        conversation.setActive(true);
        conversation.setTitle("coole Conversation");
        conversationRepository.saveAndFlush(conversation);

        Message message= new Message();
        message.setMessageText("looll");
        message.setUser(userHolder);

        messageRepository.saveAndFlush(message);

        conversation.addMessage(message);

        conversationRepository.saveAndFlush(conversation);

    }

}
