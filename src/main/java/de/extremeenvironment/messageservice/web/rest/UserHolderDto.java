package de.extremeenvironment.messageservice.web.rest;

import javax.validation.constraints.NotNull;

/**
 * Created by on 06.07.16.
 *
 * @author David Steiman
 */
public class UserHolderDto {
    @NotNull
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public UserHolderDto(Long userId) {
        this.userId = userId;
    }

    public UserHolderDto() {

    }

    @Override
    public String toString() {
        return "{userId= " + userId +
        "}";
    }
}
