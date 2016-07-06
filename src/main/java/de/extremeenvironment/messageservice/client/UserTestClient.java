package de.extremeenvironment.messageservice.client;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by on 29.06.16.
 *
 * @author David Steiman
 */
@Component
@Profile("test")
@Primary
public class UserTestClient implements UserClient {

    @Override
    public List<User> getUsers() {
        return new LinkedList<>();
    }

    @Override
    public Account getAccount(@PathVariable("userName") String userName) {
        return new Account(
            42,
            userName,
            userName,
            userName,
            userName + "@example.com",
            true,
            "de"
        );
    }

    @Override
    public Account getAccount(@PathVariable("id") Long id) {
        return new Account(
            42,
            "John Doe",
            "John",
            "Doe",
            "john@example.com",
            true,
            "de"
        );
    }
}
