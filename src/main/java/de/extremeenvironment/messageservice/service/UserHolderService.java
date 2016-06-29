package de.extremeenvironment.messageservice.service;

import de.extremeenvironment.messageservice.domain.UserHolder;
import de.extremeenvironment.messageservice.repository.UserHolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by on 29.06.16.
 *
 * @author David Steiman
 */
@Service
public class UserHolderService {
    private UserHolderRepository userHolderRepository;

    @Autowired
    public UserHolderService(UserHolderRepository userHolderRepository) {
        this.userHolderRepository = userHolderRepository;
    }

    public UserHolder findOrCreateByUserId(Long id) {
        return userHolderRepository
            .findOneByUserId(id)
            .orElse(userHolderRepository.save(new UserHolder(id)));
    }
}
