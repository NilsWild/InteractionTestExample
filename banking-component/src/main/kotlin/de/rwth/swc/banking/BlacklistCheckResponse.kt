package de.rwth.swc.banking

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = BlacklistCheckResponse.Blacklisted::class, name = "Blacklisted"),
    JsonSubTypes.Type(value = BlacklistCheckResponse.NotBlacklisted::class, name = "NotBlacklisted")
)
sealed class BlacklistCheckResponse {
    data class Blacklisted(val reason: String) : BlacklistCheckResponse()
    object NotBlacklisted : BlacklistCheckResponse()
}