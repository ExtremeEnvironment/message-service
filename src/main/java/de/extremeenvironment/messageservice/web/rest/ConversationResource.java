package de.extremeenvironment.messageservice.web.rest;

import com.codahale.metrics.annotation.Timed;
import de.extremeenvironment.messageservice.domain.Conversation;
import de.extremeenvironment.messageservice.domain.Message;
import de.extremeenvironment.messageservice.repository.ConversationRepository;
import de.extremeenvironment.messageservice.repository.MessageRepository;
import de.extremeenvironment.messageservice.web.rest.dto.ConversationDto;
import de.extremeenvironment.messageservice.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Conversation.
 */
@RestController
@RequestMapping("/api")
public class ConversationResource {

    private final Logger log = LoggerFactory.getLogger(ConversationResource.class);

    private ConversationRepository conversationRepository;

    private MessageRepository messageRepository;

    @Inject
    public ConversationResource(ConversationRepository conversationRepository, MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * POST  /conversations : Create a new conversation.
     *
     * @param conversation the conversation to create
     * @return the ResponseEntity with status 201 (Created) and with body the new conversation, or with status 400 (Bad Request) if the conversation has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/conversations",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Conversation> createConversation(@Valid @RequestBody ConversationDto conversation) throws URISyntaxException {
        log.debug("REST request to save Conversation : {}", conversation);
        if (conversation.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("conversation", "idexists", "A new conversation cannot already have an ID")).body(null);
        }
        Conversation result = conversationRepository.save(conversation.toConversation());
        return ResponseEntity.created(new URI("/api/conversations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("conversation", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /conversations : Updates an existing conversation.
     *
     * @param conversation the conversation to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated conversation,
     * or with status 400 (Bad Request) if the conversation is not valid,
     * or with status 500 (Internal Server Error) if the conversation couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/conversations",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Conversation> updateConversation(@Valid @RequestBody Conversation conversation) throws URISyntaxException {
        log.debug("REST request to update Conversation : {}", conversation);
        if (conversation.getId() == null) {
            return createConversation(new ConversationDto(conversation));
        }
        Conversation result = conversationRepository.save(conversation);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("conversation", conversation.getId().toString()))
            .body(result);
    }

    /**
     * GET  /conversations : get all the conversations.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of conversations in body
     */
    @RequestMapping(value = "/conversations",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Conversation> getAllConversations(Principal currentUser) {
        log.debug("REST request to get all Conversations");
        List<Conversation> conversations = conversationRepository.findAll();
        return conversations;
    }

    /**
     * GET  /conversations/:id : get the "id" conversation.
     *
     * @param id the id of the conversation to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the conversation, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/conversations/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Conversation> getConversation(@PathVariable Long id) {
        log.debug("REST request to get Conversation : {}", id);
        Conversation conversation = conversationRepository.findOne(id);
        return Optional.ofNullable(conversation)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /conversations/:id : delete the "id" conversation.
     *
     * @param id the id of the conversation to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/conversations/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteConversation(@PathVariable Long id) {
        log.debug("REST request to delete Conversation : {}", id);
        conversationRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("conversation", id.toString())).build();
    }



}
