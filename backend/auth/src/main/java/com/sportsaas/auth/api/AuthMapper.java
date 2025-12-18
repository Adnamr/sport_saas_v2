package com.sportsaas.auth.api;

import com.sportsaas.auth.api.dto.UserResponse;
import com.sportsaas.auth.domain.User;
import org.mapstruct.Mapper;

/**
 * Mapper MapStruct pour l'authentification.
 */
@Mapper(componentModel = "spring")
public interface AuthMapper {

    UserResponse toUserResponse(User user);
}
