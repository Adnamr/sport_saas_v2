package com.sportsaas.auth.api;

import com.sportsaas.auth.api.dto.UserResponse;
import com.sportsaas.auth.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper MapStruct pour l'authentification.
 */
@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "invited", expression = "java(user.isInvited())")
    @Mapping(target = "invitationPending", expression = "java(user.isInvited() && !user.isEnabled())")
    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponseList(List<User> users);
}
