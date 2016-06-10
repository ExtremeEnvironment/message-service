package de.extremeenvironment.messageservice.web.rest;

import de.extremeenvironment.messageservice.MessageServiceApp;
import de.extremeenvironment.messageservice.domain.Message;
import de.extremeenvironment.messageservice.repository.MessageRepository;

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
import org.springframework.test.util.ReflectionTestUtils;
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
 * Test class for the MessageResource REST controller.
 *
 * @see MessageResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MessageServiceApp.class)
@WebAppConfiguration
@IntegrationTest
public class MessageResourceIntTest {

    private static final String DEFAULT_MESSAGE_TEXT = "AAAAA";
    private static final String UPDATED_MESSAGE_TEXT = "BBBBB";

    @Inject
    private MessageRepository messageRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restMessageMockMvc;

    private Message message;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        MessageResource messageResource = new MessageResource();
        ReflectionTestUtils.setField(messageResource, "messageRepository", messageRepository);
        this.restMessageMockMvc = MockMvcBuilders.standaloneSetup(messageResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        message = new Message();
        message.setMessageText(DEFAULT_MESSAGE_TEXT);
    }

    @Test
    @Transactional
    public void createMessage() throws Exception {
        int databaseSizeBeforeCreate = messageRepository.findAll().size();

        // Create the Message

        restMessageMockMvc.perform(post("/api/messages")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(message)))
                .andExpect(status().isCreated());

        // Validate the Message in the database
        List<Message> messages = messageRepository.findAll();
        assertThat(messages).hasSize(databaseSizeBeforeCreate + 1);
        Message testMessage = messages.get(messages.size() - 1);
        assertThat(testMessage.getMessageText()).isEqualTo(DEFAULT_MESSAGE_TEXT);
    }

    @Test
    @Transactional
    public void checkMessageTextIsRequired() throws Exception {
        int databaseSizeBeforeTest = messageRepository.findAll().size();
        // set the field null
        message.setMessageText(null);

        // Create the Message, which fails.

        restMessageMockMvc.perform(post("/api/messages")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(message)))
                .andExpect(status().isBadRequest());

        List<Message> messages = messageRepository.findAll();
        assertThat(messages).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllMessages() throws Exception {
        // Initialize the database
        messageRepository.saveAndFlush(message);

        // Get all the messages
        restMessageMockMvc.perform(get("/api/messages?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(message.getId().intValue())))
                .andExpect(jsonPath("$.[*].messageText").value(hasItem(DEFAULT_MESSAGE_TEXT.toString())));
    }

    @Test
    @Transactional
    public void getMessage() throws Exception {
        // Initialize the database
        messageRepository.saveAndFlush(message);

        // Get the message
        restMessageMockMvc.perform(get("/api/messages/{id}", message.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(message.getId().intValue()))
            .andExpect(jsonPath("$.messageText").value(DEFAULT_MESSAGE_TEXT.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingMessage() throws Exception {
        // Get the message
        restMessageMockMvc.perform(get("/api/messages/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateMessage() throws Exception {
        // Initialize the database
        messageRepository.saveAndFlush(message);
        int databaseSizeBeforeUpdate = messageRepository.findAll().size();

        // Update the message
        Message updatedMessage = new Message();
        updatedMessage.setId(message.getId());
        updatedMessage.setMessageText(UPDATED_MESSAGE_TEXT);

        restMessageMockMvc.perform(put("/api/messages")
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
    public void deleteMessage() throws Exception {
        // Initialize the database
        messageRepository.saveAndFlush(message);
        int databaseSizeBeforeDelete = messageRepository.findAll().size();

        // Get the message
        restMessageMockMvc.perform(delete("/api/messages/{id}", message.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Message> messages = messageRepository.findAll();
        assertThat(messages).hasSize(databaseSizeBeforeDelete - 1);
    }
}
