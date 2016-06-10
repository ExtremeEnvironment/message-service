package de.extremeenvironment.messageservice.repository;

import de.extremeenvironment.messageservice.domain.Conversation;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Conversation entity.
 */
@SuppressWarnings("unused")
public interface ConversationRepository extends JpaRepository<Conversation,Long> {

}
