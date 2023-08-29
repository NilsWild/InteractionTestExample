package de.rwth.swc.banking

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = TransferResponse.Success::class, name = "Success"),
    JsonSubTypes.Type(value = TransferResponse.Failure::class, name = "Failure")
)
sealed class TransferResponse {
    data class Success(val newAmount: Int) : TransferResponse()
    data class Failure(val reason: String) : TransferResponse()
}