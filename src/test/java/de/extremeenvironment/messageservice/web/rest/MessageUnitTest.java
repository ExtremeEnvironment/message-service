package de.extremeenvironment.messageservice.web.rest;

import de.extremeenvironment.messageservice.MessageServiceApp;
import de.extremeenvironment.messageservice.client.Account;
import de.extremeenvironment.messageservice.client.UserClient;
import de.extremeenvironment.messageservice.domain.Conversation;
import de.extremeenvironment.messageservice.domain.Message;
import de.extremeenvironment.messageservice.domain.UserHolder;
import de.extremeenvironment.messageservice.repository.ConversationRepository;
import de.extremeenvironment.messageservice.repository.MessageRepository;
import de.extremeenvironment.messageservice.repository.UserHolderRepository;
import de.extremeenvironment.messageservice.service.UserHolderService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

/**
 * Created by on 05.07.16.
 *
 * @author David Steiman
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MessageServiceApp.class)
@WebIntegrationTest({"server.port:0", "spring.profiles.active:test"})
public class MessageUnitTest {

    Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private UserHolderService userHolderService;

    @Inject
    private MessageRepository messageRepository;

    @Inject
    private ConversationRepository conversationRepository;

    @Inject
    private UserHolderRepository userHolderRepository;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void create3Messages() throws Exception {
        Conversation conversation = new Conversation();
        conversation.setTitle("test");
        conversation = conversationRepository.save(conversation);

        Account account = new Account(1, "tester", "Tes", "Ter", "tes@ter.com", true, "de");
        UserHolder userHolder = userHolderService.findOrCreateByAccount(account);
        conversation.addMember(userHolder);

        Message[] messages = new Message[]{
            new Message("hello"),
            new Message("anybody here?"),
            new Message("HELLO?!?!")
        };

        for(int i = 0; i < 3; i++) {
            userHolder = userHolderService.findOrCreateByAccount(account);
            log.info("findOrCreate user {}", userHolder);

            messages[i].setUser(userHolder);
            messages[i].setConversation(conversation);

            messageRepository.save(messages[i]);
        }
    }

    @Test
    public void createMessageAndNotMemberOfConversation() throws Exception {
        expectedEx.expect(ConstraintViolationException.class);
        expectedEx.expectMessage("user must be part of conversations members");


        Conversation conversation = new Conversation();
        conversation.setTitle("test");
        conversation = conversationRepository.save(conversation);

        Account account = new Account(1, "tester", "Tes", "Ter", "tes@ter.com", true, "de");
        UserHolder userHolder = userHolderService.findOrCreateByAccount(account);

        Message message = new Message("hello");
        message.setUser(userHolder);
        message.setConversation(conversation);

        messageRepository.save(message);
    }
}
