package com.proyecto.version1.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;


@Builder
public record AuthenticationResponse(
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("refresh_token")
        String refreshToken,
        @JsonProperty("token_type")
        String tokenType
) {
}
