package de.extremeenvironment.messageservice.web.rest;

import de.extremeenvironment.messageservice.MessageServiceApp;
import de.extremeenvironment.messageservice.client.Account;
import de.extremeenvironment.messageservice.service.UserHolderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by on 13.07.16.
 *
 * @author David Steiman
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MessageServiceApp.class)
@WebIntegrationTest({"server.port:0", "spring.profiles.active:test"})
public class UserUnitTest {

    @Inject
    private WebApplicationContext context;

    @Inject
    private UserHolderService userHolderService;

    @PostConstruct
    private void setUp() {

    }

    @Test
    public void testUniqueFailureNotOccurring() throws Exception {

        Account first = new Account(1L, "test", "test", "test", "test@test.com", true, "ts");
        Account second = new Account(1L, "test", "test", "test", "test@test.com", true, "ts");

        List<Account> accounts = new LinkedList<>();
        accounts.add(first);
        accounts.add(second);

        accounts.parallelStream().forEach(account -> userHolderService.findOrCreateByAccount(account));

        //userHolderService.findOrCreateByAccount(first);
        userHolderService.findOrCreateByAccount(second);
    }
}
