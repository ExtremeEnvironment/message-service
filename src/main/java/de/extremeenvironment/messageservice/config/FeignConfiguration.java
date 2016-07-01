package de.extremeenvironment.messageservice.config;

import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Created by on 29.06.16.
 *
 * @author David Steiman
 */
@Configuration
@Profile("!test")
@EnableFeignClients(basePackages = "de.extremeenvironment.messageservice.client")
public class FeignConfiguration {
}
