package de.rwth.swc.banking

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange


interface IbanValidationApi {
    @PostExchange("/api/v1/validate/iban")
    fun validateIbanV1(@RequestBody iban: String): ResponseEntity<IbanValidationResponse>

    @PostExchange("/api/v2/validate/iban")
    fun validateIbanV2(@RequestBody iban: String): ResponseEntity<IbanValidationResponse>
}
