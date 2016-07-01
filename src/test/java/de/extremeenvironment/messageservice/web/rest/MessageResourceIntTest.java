package de.extremeenvironment.messageservice.web.rest;

import de.extremeenvironment.messageservice.MessageServiceApp;
import de.extremeenvironment.messageservice.client.UserClient;
import de.extremeenvironment.messageservice.domain.Conversation;
import de.extremeenvironment.messageservice.domain.Message;
import de.extremeenvironment.messageservice.domain.UserHolder;
import de.extremeenvironment.messageservice.repository.ConversationRepository;
import de.extremeenvironment.messageservice.repository.MessageRepository;
import de.extremeenvironment.messageservice.service.UserHolderService;
import de.extremeenvironment.messageservice.util.TestUtil;
import de.extremeenvironment.messageservice.util.WithMockOAuth2Authentication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the MessageResource REST controller.
 *
 * @see MessageResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MessageServiceApp.class)
@WebIntegrationTest({"server.port:0", "spring.profiles.active:test"})
public class MessageResourceIntTest{

    private static final String DEFAULT_MESSAGE_TEXT = "AAAAA";
    private static final String UPDATED_MESSAGE_TEXT = "BBBBB";
    private static final boolean USE_STANDALONE_MOCK = false;

    @Inject
    private MessageRepository messageRepository;

    @Inject
    ConversationResource conversationResource;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private ConversationRepository conversationRepository;

    @Inject
    private UserClient userClient;

    @Inject
    private UserHolderService userHolderService;

    @Inject
    private WebApplicationContext context;

    private MockMvc restMessageMockMvc;

    private Message message;
    private Conversation conversation;


    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        MessageResource messageResource = new MessageResource(
            messageRepository,
            conversationRepository,
            userClient,
            userHolderService
        );

        if (USE_STANDALONE_MOCK) {

            this.restMessageMockMvc = MockMvcBuilders
                .standaloneSetup(messageResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setMessageConverters(jacksonMessageConverter)
                .build();
        } else {
            this.restMessageMockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        }

    }


    @Before
    public void initTest() {
        conversation = new Conversation();
        conversation.addMessage(new Message("default message"));

        message = new Message();
        message.setMessageText(DEFAULT_MESSAGE_TEXT);

        message.setUser(new UserHolder(12L, "TestUser"));

        conversationRepository.save(conversation);

    }

    @Test
    //@Transactional
    @WithMockOAuth2Authentication
    public void createMessage() throws Exception {
        int databaseSizeBeforeCreate = messageRepository.findAll().size();

        // Create the Message


        restMessageMockMvc.perform(
            post("/api/conversations/{conversationId}/messages", conversation.getId())
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(message))
            ).andExpect(status().isCreated());

        // Validate the Message in the database
        List<Message> messages = messageRepository.findAll();
        assertThat(messages).hasSize(databaseSizeBeforeCreate + 1);
        Message testMessage = messages.get(messages.size() - 1);
        assertThat(testMessage.getMessageText()).isEqualTo(DEFAULT_MESSAGE_TEXT);
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication
    public void checkMessageTextIsRequired() throws Exception {
        int databaseSizeBeforeTest = messageRepository.findAll().size();
        // set the field null
        message.setMessageText(null);

        // Create the Message, which fails.

        restMessageMockMvc.perform(post("/api/conversations/{conversationId}/messages", conversation.getId())
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(message)))
                .andExpect(status().isBadRequest());

        List<Message> messages = messageRepository.findAll();
        assertThat(messages).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication
    public void getAllMessages() throws Exception {
        // Initialize the database
        conversation.addMessage(message);
        //conversationRepository.save(conversation);
        messageRepository.saveAndFlush(message);

        // Get all the messages
        restMessageMockMvc.perform(get("/api/conversations/{conversationId}/messages?sort=id,desc", conversation.getId())
                    .with(user("tester").roles("USER", "ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(message.getId().intValue())))
                .andExpect(jsonPath("$.[*].messageText").value(hasItem(DEFAULT_MESSAGE_TEXT.toString())))
                .andExpect(jsonPath("$.[*].messageUser").exists())
                .andExpect(jsonPath("$.[*].messageDate").exists());
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication
    public void getMessage() throws Exception {
        // Initialize the database
        conversation.addMessage(message);
        messageRepository.saveAndFlush(message);

        // Get the message
        restMessageMockMvc.perform(get("/api/conversations/{conversationId}/messages/{id}",
            conversation.getId(), message.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(message.getId().intValue()))
            .andExpect(jsonPath("$.messageText").value(DEFAULT_MESSAGE_TEXT.toString()));
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication
    public void getNonExistingMessage() throws Exception {
        // Get the message
        restMessageMockMvc.perform(get("/api/conversations/{conversationId}/messages/{id}", Long.MAX_VALUE, Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication
    public void updateMessage() throws Exception {
        // Initialize the database
        conversation.addMessage(message);
        messageRepository.saveAndFlush(message);
        int databaseSizeBeforeUpdate = messageRepository.findAll().size();

        // Update the message
        Message updatedMessage = new Message();
        updatedMessage.setId(message.getId());
        updatedMessage.setMessageText(UPDATED_MESSAGE_TEXT);

        restMessageMockMvc.perform(put("/api/conversations/{conversationId}/messages", conversation.getId())
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedMessage)))
                .andExpect(status().isOk());

        // Validate the Message in the database
        List<Message> messages = messageRepository.findAll();
        assertThat(messages).hasSize(databaseSizeBeforeUpdate);
        Message testMessage = messages.get(messages.size() - 1);
        assertThat(testMessage.getMessageText()).isEqualTo(UPDATED_MESSAGE_TEXT);
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication
    public void deleteMessage() throws Exception {
        // Initialize the database
        conversation.addMessage(message);
        messageRepository.saveAndFlush(message);
        int databaseSizeBeforeDelete = messageRepository.findAll().size();

        // Get the message
        restMessageMockMvc.perform(delete("/api/conversations/{conversationId}/messages/{id}", conversation.getId(), message.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Message> messages = messageRepository.findAll();
        assertThat(messages).hasSize(databaseSizeBeforeDelete - 1);
    }
}
