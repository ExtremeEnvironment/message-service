package de.extremeenvironment.messageservice.repository;

import de.extremeenvironment.messageservice.domain.Conversation;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Conversation entity.
 */
@SuppressWarnings("unused")
public interface ConversationRepository extends JpaRepository<Conversation,Long> {
    @Query(
        "select conversation " +
        "from Conversation conversation " +
        "inner join conversation.users users " +
        "where users.userId = :userId"
    )

    List<Conversation> readConversationsByUserId(@Param("userId") Long userId);
}
