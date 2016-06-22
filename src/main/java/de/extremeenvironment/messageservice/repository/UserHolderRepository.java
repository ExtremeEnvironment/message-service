package de.extremeenvironment.messageservice.repository;

import de.extremeenvironment.messageservice.domain.UserHolder;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.method.P;

import java.util.List;

/**
 * Spring Data JPA repository for the UserHolder entity.
 */
@SuppressWarnings("unused")
public interface UserHolderRepository extends JpaRepository<UserHolder,Long> {

    @Query("select distinct userHolder from UserHolder userHolder left join fetch userHolder.conversations")
    List<UserHolder> findAllWithEagerRelationships();

    @Query("select userHolder from UserHolder userHolder left join fetch userHolder.conversations where userHolder.id =:id")
    UserHolder findOneWithEagerRelationships(@Param("id") Long id);

    @Query(
        "select userHolder " +
        "from UserHolder userHolder " +
        "left join userHolder.conversations conversations " +
        "where conversations.id = :conversationId"
    )
    List<UserHolder> findAllByConversationId(@Param("conversationId") Long conversationId);
}
