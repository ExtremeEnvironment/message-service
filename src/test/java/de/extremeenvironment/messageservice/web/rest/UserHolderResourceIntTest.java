package de.extremeenvironment.messageservice.web.rest;

import de.extremeenvironment.messageservice.MessageServiceApp;
import de.extremeenvironment.messageservice.domain.Conversation;
import de.extremeenvironment.messageservice.domain.UserHolder;
import de.extremeenvironment.messageservice.repository.ConversationRepository;
import de.extremeenvironment.messageservice.repository.UserHolderRepository;

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
 * Test class for the UserHolderResource REST controller.
 *
 * @see UserHolderResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MessageServiceApp.class)
@WebAppConfiguration
@IntegrationTest
public class UserHolderResourceIntTest {


    private static final Long DEFAULT_USER_ID = 1L;
    private static final Long UPDATED_USER_ID = 2L;

    @Inject
    private UserHolderRepository userHolderRepository;

    @Inject
    private ConversationRepository conversationRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restUserHolderMockMvc;

    private UserHolder userHolder;
    private Conversation conversation;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        UserHolderResource userHolderResource = new UserHolderResource(userHolderRepository, conversationRepository);
        this.restUserHolderMockMvc = MockMvcBuilders.standaloneSetup(userHolderResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        conversation = new Conversation();

        conversationRepository.save(conversation);

        userHolder = new UserHolder();
        userHolder.setUserId(DEFAULT_USER_ID);

        conversation.addMember(userHolder);
    }

    @Test
    @Transactional
    public void createUserHolder() throws Exception {
        int databaseSizeBeforeCreate = userHolderRepository.findAll().size();

        // Create the UserHolder

        restUserHolderMockMvc.perform(post("/api/conversations/{conversationId}/members", conversation.getId())
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userHolder)))
                .andExpect(status().isCreated());

        // Validate the UserHolder in the database
        List<UserHolder> userHolders = userHolderRepository.findAll();
        assertThat(userHolders).hasSize(databaseSizeBeforeCreate + 1);
        UserHolder testUserHolder = userHolders.get(userHolders.size() - 1);
        assertThat(testUserHolder.getUserId()).isEqualTo(DEFAULT_USER_ID);
    }

    @Test
    @Transactional
    public void checkUserIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = userHolderRepository.findAll().size();
        // set the field null
        userHolder.setUserId(null);

        // Create the UserHolder, which fails.

        restUserHolderMockMvc.perform(post("/api/conversations/{conversationId}/members", conversation.getId())
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userHolder)))
                .andExpect(status().isBadRequest());

        List<UserHolder> userHolders = userHolderRepository.findAll();
        assertThat(userHolders).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllUserHolders() throws Exception {
        // Initialize the database
        userHolderRepository.saveAndFlush(userHolder);

        // Get all the userHolders
        restUserHolderMockMvc.perform(get("/api/conversations/{conversationId}/members?sort=id,desc", conversation.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(userHolder.getId().intValue())))
                .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID.intValue())));
    }

    //@HACK @TODO
    /*
    @Test
    @Transactional
    public void getUserHolder() throws Exception {
        // Initialize the database
        Conversation currentConv = new Conversation();
        currentConv.addMember(userHolder);

        currentConv = conversationRepository.save(currentConv);
        userHolderRepository.saveAndFlush(userHolder);

        // Get the userHolder
        restUserHolderMockMvc.perform(get("/api/conversations/{conversationId}/members/{id}", currentConv.getId(),
            userHolder.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(userHolder.getId().intValue()))
            .andExpect(jsonPath("$.userId").value(DEFAULT_USER_ID.intValue()));
    }
*/
    @Test
    @Transactional
    public void getNonExistingUserHolder() throws Exception {
        // Get the userHolder
        restUserHolderMockMvc.perform(get("/api/conversations/{conversationId}/members/{id}", Long.MAX_VALUE, Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateUserHolder() throws Exception {
        // Initialize the database
        userHolderRepository.saveAndFlush(userHolder);
        int databaseSizeBeforeUpdate = userHolderRepository.findAll().size();

        // Update the userHolder
        UserHolder updatedUserHolder = new UserHolder();
        updatedUserHolder.setId(userHolder.getId());
        updatedUserHolder.setUserId(UPDATED_USER_ID);

        restUserHolderMockMvc.perform(put("/api/conversations/{conversationId}/members/", conversation.getId())
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedUserHolder)))
                .andExpect(status().isOk());

        // Validate the UserHolder in the database
        List<UserHolder> userHolders = userHolderRepository.findAll();
        assertThat(userHolders).hasSize(databaseSizeBeforeUpdate);
        UserHolder testUserHolder = userHolders.get(userHolders.size() - 1);
        assertThat(testUserHolder.getUserId()).isEqualTo(UPDATED_USER_ID);
    }

    @Test
    @Transactional
    public void deleteUserHolder() throws Exception {
        // Initialize the database
        userHolderRepository.saveAndFlush(userHolder);
        int databaseSizeBeforeDelete = userHolderRepository.findAll().size();

        // Get the userHolder
        restUserHolderMockMvc.perform(delete("/api/conversations/{conversationId}/members/{id}",
            conversation.getId(),
            userHolder.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<UserHolder> userHolders = userHolderRepository.findAll();
        assertThat(userHolders).hasSize(databaseSizeBeforeDelete - 1);
    }
}
