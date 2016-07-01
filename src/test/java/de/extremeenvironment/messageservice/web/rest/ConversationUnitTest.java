package de.extremeenvironment.messageservice.web.rest;

import de.extremeenvironment.messageservice.MessageServiceApp;
import de.extremeenvironment.messageservice.domain.Conversation;
import de.extremeenvironment.messageservice.domain.Message;
import de.extremeenvironment.messageservice.repository.ConversationRepository;
import de.extremeenvironment.messageservice.repository.MessageRepository;
import de.extremeenvironment.messageservice.service.ConversationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the ConversationResource REST controller.
 *
 * @see ConversationResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MessageServiceApp.class)
@WebAppConfiguration
@IntegrationTest
public class ConversationUnitTest {

    @Inject
    private MessageRepository messageRepository;

    @Inject
    private ConversationRepository conversationRepository;

    @Inject
    private ConversationService conversationService;

    @Test
    public void findByConversationIdWorks() throws Exception {
        Conversation cOne = new Conversation();
        Conversation cTwo = new Conversation();

        Message mOne = new Message("test1");
        Message mTwo = new Message("test2");


        conversationService.addMessageToConversation(cOne,mOne);
        conversationService.addMessageToConversation(cTwo,mTwo);

        cOne = conversationRepository.save(cOne);
        cTwo =conversationRepository.save(cTwo);

        List<Message> listOne = messageRepository.findAllByConversationId(cOne.getId());
        List<Message> listTwo = messageRepository.findAllByConversationId(cTwo.getId());

        assertThat(listOne.contains(mOne));
        assertThat(listTwo.contains(mTwo));


    }
}
