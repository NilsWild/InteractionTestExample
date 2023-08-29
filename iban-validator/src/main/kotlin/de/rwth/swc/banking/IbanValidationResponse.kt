package de.rwth.swc.banking

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = IbanValidationResponse.Valid::class, name = "Valid"),
    JsonSubTypes.Type(value = IbanValidationResponse.Invalid::class, name = "Invalid")
)
sealed class IbanValidationResponse {
    object Valid : IbanValidationResponse()
    data class Invalid(var errorMessage: String) : IbanValidationResponse()
}