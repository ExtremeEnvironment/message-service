package de.extremeenvironment.messageservice.web.rest;

import com.codahale.metrics.annotation.Timed;
import de.extremeenvironment.messageservice.client.Account;
import de.extremeenvironment.messageservice.client.UserClient;
import de.extremeenvironment.messageservice.domain.Conversation;
import de.extremeenvironment.messageservice.domain.UserHolder;
import de.extremeenvironment.messageservice.repository.ConversationRepository;
import de.extremeenvironment.messageservice.repository.UserHolderRepository;
import de.extremeenvironment.messageservice.service.UserHolderService;
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
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing UserHolder.
 */
@RestController
@RequestMapping("/api")
public class UserHolderResource {

    private final Logger log = LoggerFactory.getLogger(UserHolderResource.class);

    private UserHolderRepository userHolderRepository;

    private ConversationRepository conversationRepository;

    private UserClient userClient;

    private UserHolderService userHolderService;

    @Inject
    public UserHolderResource(
        UserHolderRepository userHolderRepository,
        ConversationRepository conversationRepository,
        UserClient userClient,
        UserHolderService userHolderService
        ) {
        this.userHolderRepository = userHolderRepository;
        this.conversationRepository = conversationRepository;
        this.userClient = userClient;
        this.userHolderService = userHolderService;
    }

    /**
     * POST  /user-holders : Create a new userHolder.
     *
     * @param userHolder the userHolder to create
     * @return the ResponseEntity with status 201 (Created) and with body the new userHolder, or with status 400 (Bad Request) if the userHolder has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/conversations/{conversationId}/members",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<UserHolder> createUserHolder(@Valid @RequestBody UserHolderDto userHolder,
           @PathVariable("conversationId") Long conversationId) throws URISyntaxException {
        log.debug("REST request to save UserHolder : {}", userHolder);

        Conversation conversation = conversationRepository.findOne(conversationId);
        if (conversation == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("userHolder", "noconversation", "conversation not found")).body(null);
        }
        Account account = userClient.getAccount(userHolder.getUserId());
        UserHolder user = userHolderService.findOrCreateByAccount(account);
        conversation.addMember(user);

        UserHolder result = userHolderRepository.save(user);

        String resourceUri = String.format("/api/conversations/%d/members/%d", conversationId, result.getId());
        return ResponseEntity.created(new URI(resourceUri))
            .headers(HeaderUtil.createEntityCreationAlert("userHolder", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /user-holders : Updates an existing userHolder.
     *
     * @param userHolder the userHolder to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated userHolder,
     * or with status 400 (Bad Request) if the userHolder is not valid,
     * or with status 500 (Internal Server Error) if the userHolder couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/conversations/{conversationId}/members",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<UserHolder> updateUserHolder(@Valid @RequestBody UserHolder userHolder,
               @PathVariable("conversationId") Long conversationId) throws URISyntaxException {
        log.debug("REST request to update UserHolder : {}", userHolder);

        Conversation conversation = conversationRepository.findOne(conversationId);
        if (conversation == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("userHolder", "noconversation", "conversation not found")).body(null);
        }
        if (userHolder.getId() == null) {
            return createUserHolder(new UserHolderDto(userHolder.getId()), conversationId);
        }
        UserHolder result = userHolderRepository.save(userHolder);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("userHolder", userHolder.getId().toString()))
            .body(result);
    }

    /**
     * GET  /user-holders : get all the userHolders.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of userHolders in body
     */
    @RequestMapping(value = "/conversations/{conversationId}/members",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<UserHolder> getAllUserHolders(@PathVariable("conversationId") Long conversationId) {
        log.debug("REST request to get all UserHolders");
        return userHolderRepository.findAllByConversationId(conversationId);
    }

    /**
     * GET  /user-holders/:id : get the "id" userHolder.
     *
     * @param id the id of the userHolder to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the userHolder, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/conversations/{conversationId}/members/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<UserHolder> getUserHolder(@PathVariable Long id,
            @PathVariable("conversationId") Long conversationId) {
        log.debug("REST request to get UserHolder : {}", id);
        List<UserHolder> userHolders = userHolderRepository.findAllByConversationId(id);
        Conversation conversation = conversationRepository.findOne(id);
        Optional<UserHolder> userHolder = userHolders.stream()
            .filter(user -> user.getId().equals(id))
            .findFirst();

        return userHolder
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /user-holders/:id : delete the "id" userHolder.
     *
     * @param id the id of the userHolder to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/conversations/{conversationId}/members/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteUserHolder(@PathVariable Long id,
             @PathVariable("conversationId") Long conversationId) {
        log.debug("REST request to delete UserHolder : {}", id);
        userHolderRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("userHolder", id.toString())).build();
    }

}
