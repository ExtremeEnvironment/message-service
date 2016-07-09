package de.extremeenvironment.messageservice.config;



import de.extremeenvironment.messageservice.security.AuthoritiesConstants;

import feign.RequestInterceptor;

import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;


import javax.inject.Inject;
import java.io.IOException;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class MicroserviceSecurityConfiguration extends ResourceServerConfigurerAdapter {

    @Inject
    JHipsterProperties jHipsterProperties;

    @Inject
    LoadBalancedResourceDetails loadBalancedResourceDetails;


    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .csrf()
            .disable()
            .headers()
            .frameOptions()
            .disable()
        .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.POST, "/api/conversations")
                .access("#oauth2.hasScope('internal.write')")
            .antMatchers(HttpMethod.POST, "/api/conversations/*/members")
                .access("#oauth2.hasScope('internal.write')")
            .antMatchers(HttpMethod.GET, "/api/conversations/*/messages")
                .access("#oauth2.hasScope('web-app')")
            .antMatchers(HttpMethod.POST, "/api/conversations/*/messages")
                .access("#oauth2.hasScope('web-app')")
            .antMatchers(HttpMethod.GET, "/api/conversations")
                .access("#oauth2.hasScope('internal.write') or #oauth2.hasScope('web-app')")
            .antMatchers("/api/**").authenticated()
            .antMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/configuration/ui").permitAll();

    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.stateless(false);
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey(jHipsterProperties.getSecurity().getAuthentication().getJwt().getSecret());

        return converter;
    }

    @Bean
    public RequestInterceptor getOAuth2RequestInterceptor() throws IOException {
        return new OAuth2FeignRequestInterceptor(new DefaultOAuth2ClientContext(), loadBalancedResourceDetails);
    }


}

