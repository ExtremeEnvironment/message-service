package de.extremeenvironment.messageservice.service;

import de.extremeenvironment.messageservice.client.Account;
import de.extremeenvironment.messageservice.domain.UserHolder;
import de.extremeenvironment.messageservice.repository.UserHolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    public UserHolder findOrCreateByAccount(Account source) {
        Optional<UserHolder> user = userHolderRepository.findOneByUserId(source.getId());
        return user.isPresent()
            ? user.get()
            : userHolderRepository.save(new UserHolder(source.getId(), source.getLogin()));
/*
        UserHolder userHolder = userHolderRepository
            .findOneByUserId(source.getId())
            .orElse(userHolderRepository.save(new UserHolder(source.getId(), source.getLogin())));

        return userHolder;*/
    }
}
