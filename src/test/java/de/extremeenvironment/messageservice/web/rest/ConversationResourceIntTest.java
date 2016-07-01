package de.extremeenvironment.messageservice.web.rest;

import de.extremeenvironment.messageservice.MessageServiceApp;
import de.extremeenvironment.messageservice.client.UserClient;
import de.extremeenvironment.messageservice.domain.Conversation;
import de.extremeenvironment.messageservice.domain.Message;
import de.extremeenvironment.messageservice.repository.ConversationRepository;

import de.extremeenvironment.messageservice.repository.MessageRepository;
import de.extremeenvironment.messageservice.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
public class ConversationResourceIntTest {


    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;
    private static final String DEFAULT_TITLE = "AAAAA";
    private static final String UPDATED_TITLE = "BBBBB";

    @Inject
    private ConversationRepository conversationRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private UserClient userClient;

    private MockMvc restConversationMockMvc;

    private Conversation conversation;

    @Inject
    private MessageRepository messageRepository;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);

        ConversationResource conversationResource = new ConversationResource(
            conversationRepository,
            messageRepository,
            userClient
        );

        this.restConversationMockMvc = MockMvcBuilders.standaloneSetup(conversationResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        conversation = new Conversation();
        conversation.setActive(DEFAULT_ACTIVE);
        conversation.setTitle(DEFAULT_TITLE);
        conversation.addMessage(new Message("Hi"));
        conversation.addMessage(new Message("Goodbye"));


    }

    @Test
    @Transactional
    public void createConversation() throws Exception {
        int databaseSizeBeforeCreate = conversationRepository.findAll().size();

        // Create the Conversation

        restConversationMockMvc.perform(post("/api/conversations")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(conversation)))
                .andExpect(status().isCreated());

        // Validate the Conversation in the database
        List<Conversation> conversations = conversationRepository.findAll();
        assertThat(conversations).hasSize(databaseSizeBeforeCreate + 1);
        Conversation testConversation = conversations.get(conversations.size() - 1);
        assertThat(testConversation.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testConversation.getTitle()).isEqualTo(DEFAULT_TITLE);
    }


    @Test
    @Transactional
    public void getAllConversations() throws Exception {
        // Initialize the database
        conversation.getMessages().stream().forEach(message -> messageRepository.save(message));
        conversationRepository.saveAndFlush(conversation);

        // Get all the conversations
        restConversationMockMvc.perform(get("/api/conversations?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(conversation.getId().intValue())))
                .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
                .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())));
    }

    @Test
    @Transactional
    public void getConversation() throws Exception {
        // Initialize the database
        conversation.getMessages().stream().forEach(message -> messageRepository.save(message));
        conversationRepository.saveAndFlush(conversation);

        // Get the conversation
        restConversationMockMvc.perform(get("/api/conversations/{id}", conversation.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(conversation.getId().intValue()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingConversation() throws Exception {
        // Get the conversation
        restConversationMockMvc.perform(get("/api/conversations/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateConversation() throws Exception {
        // Initialize the database
        conversation.getMessages().stream().forEach(message -> messageRepository.save(message));
        conversationRepository.saveAndFlush(conversation);
        int databaseSizeBeforeUpdate = conversationRepository.findAll().size();

        // Update the conversation
        Conversation updatedConversation = new Conversation();
        updatedConversation.setId(conversation.getId());
        updatedConversation.setActive(UPDATED_ACTIVE);
        updatedConversation.setTitle(UPDATED_TITLE);

        restConversationMockMvc.perform(put("/api/conversations")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedConversation)))
                .andExpect(status().isOk());

        // Validate the Conversation in the database
        List<Conversation> conversations = conversationRepository.findAll();
        assertThat(conversations).hasSize(databaseSizeBeforeUpdate);
        Conversation testConversation = conversations.get(conversations.size() - 1);
        assertThat(testConversation.isActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testConversation.getTitle()).isEqualTo(UPDATED_TITLE);
    }

    @Test
    @Transactional
    public void deleteConversation() throws Exception {
        // Initialize the database
        conversation.getMessages().stream().forEach(message -> messageRepository.save(message));
        conversationRepository.saveAndFlush(conversation);
        int databaseSizeBeforeDelete = conversationRepository.findAll().size();

        // Get the conversation
        restConversationMockMvc.perform(delete("/api/conversations/{id}", conversation.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Conversation> conversations = conversationRepository.findAll();
        assertThat(conversations).hasSize(databaseSizeBeforeDelete - 1);
    }

}
