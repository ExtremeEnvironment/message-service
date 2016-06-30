package de.extremeenvironment.messageservice.web.rest;

import com.codahale.metrics.annotation.Timed;
import de.extremeenvironment.messageservice.client.Account;
import de.extremeenvironment.messageservice.client.UserClient;
import de.extremeenvironment.messageservice.domain.Conversation;
import de.extremeenvironment.messageservice.domain.Message;
import de.extremeenvironment.messageservice.repository.ConversationRepository;
import de.extremeenvironment.messageservice.repository.MessageRepository;
import de.extremeenvironment.messageservice.service.UserHolderService;
import de.extremeenvironment.messageservice.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Message.
 */
@RestController
@RequestMapping("/api")
public class MessageResource {

    private final Logger log = LoggerFactory.getLogger(MessageResource.class);

    private MessageRepository messageRepository;

    private ConversationRepository conversationRepository;

    private UserClient userClient;

    private UserHolderService userHolderService;

    @Inject
    public MessageResource(
        MessageRepository messageRepository,
        ConversationRepository conversationRepository,
        UserClient userClient,
        UserHolderService userHolderService
    ) {

        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userClient = userClient;
        this.userHolderService = userHolderService;
    }

    /**
     * POST  /messages : Create a new message.
     *
     * @param message the message to create
     * @return the ResponseEntity with status 201 (Created) and with body the new message, or with status 400 (Bad Request) if the message has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/conversations/{conversationId}/messages",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional
    public ResponseEntity<Message> createMessage(
        @Valid @RequestBody Message message,
        @PathVariable("conversationId") Long conversationId,
        Principal currentUser
    ) throws URISyntaxException {
        log.debug("REST request to save Message : {}", message);
        if (message.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("message", "idexists", "A new message cannot already have an ID")).body(null);
        }
        Conversation conversation = conversationRepository.findOne(conversationId);
        if (conversation == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("message", "noconversation", "conversation not found")).body(null);
        }


        //fetch user
        if (currentUser != null) {
            Account userAccount = userClient.getAccount(currentUser.getName());

            if (userAccount != null) {
                message.setUser(userHolderService.findOrCreateByUserId(userAccount.getId()));
            }
        }

        //conversation.addMessage(message);
        conversationRepository.save(conversation);
        Message result = messageRepository.save(message);
        return ResponseEntity.created(new URI("/api/messages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("message", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /messages : Updates an existing message.
     *
     * @param message the message to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated message,
     * or with status 400 (Bad Request) if the message is not valid,
     * or with status 500 (Internal Server Error) if the message couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/conversations/{conversationId}/messages",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Message> updateMessage(
        @Valid @RequestBody Message message,
        @PathVariable("conversationId") Long conversationId,
        Principal currentUser
    ) throws URISyntaxException {
        log.debug("REST request to update Message : {}", message);
        Conversation conversation = conversationRepository.findOne(conversationId);
        if (conversation == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("message", "noconversation", "conversation not found")).body(null);
        }
        if (message.getId() == null) {
            return createMessage(message, conversationId, currentUser);
        }
        Message result = messageRepository.save(message);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("message", message.getId().toString()))
            .body(result);
    }

    /**
     * GET  /messages : get all the messages.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of messages in body
     */
    @RequestMapping(value = "/conversations/{conversationId}/messages",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Message> getAllMessages(@PathVariable("conversationId") Long conversationId) {
        log.debug("REST request to get all Messages");

        Conversation conversation = conversationRepository.findOne(conversationId);
        /*if (conversation == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("message", "noconversation", "conversation not found")).body(new LinkedList<>());
        }*/

        return messageRepository.findAllByConversationId(conversationId);
    }

    /**
     * GET  /messages/:id : get the "id" message.
     *
     * @param id the id of the message to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the message, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/conversations/{conversationId}/messages/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Message> getMessage(@PathVariable Long id, @PathVariable("conversationId") Long conversationId) {
        log.debug("REST request to get Message : {}", id);
        Message message = messageRepository.findOne(id);
        return Optional.ofNullable(message)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /messages/:id : delete the "id" message.
     *
     * @param id the id of the message to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/conversations/{conversationId}/messages/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id, @PathVariable("conversationId") Long conversationId) {
        log.debug("REST request to delete Message : {}", id);
        Conversation conversation = conversationRepository.findOne(conversationId);
        if (conversation == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("message", "noconversation", "conversation not found")).body(null);
        }

        messageRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("message", id.toString())).build();
    }

}
