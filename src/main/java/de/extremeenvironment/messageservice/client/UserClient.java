package de.extremeenvironment.messageservice.client;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * Created by on 29.06.16.
 *
 * @author David Steiman
 */
@FeignClient("http://userservice/api")
public interface UserClient {
    @RequestMapping(method = RequestMethod.GET, value = "/users")
    List<User> getUsers();

    @RequestMapping(method = RequestMethod.GET, value = "/users/{userName}")
    Account getAccount(@PathVariable("userName") String userName);
}
