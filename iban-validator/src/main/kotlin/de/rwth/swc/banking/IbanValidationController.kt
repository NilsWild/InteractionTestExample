package de.rwth.swc.banking

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

const val IBAN_REGEX = "^[A-Z]{2}[0-9]{2}(?:[ ]?[0-9]{4}){4}(?:[ ]?[0-9]{1,2})?$"

@RestController
class IbanValidationController(private val blacklistCheckerApi: BlacklistCheckerApi) {

    @PostMapping("/api/v1/validate/iban")
    fun validateIbanV1(@RequestBody iban: String): ResponseEntity<IbanValidationResponse> {
        return when (iban.matches(IBAN_REGEX.toRegex())) {
            true -> ResponseEntity.ok(IbanValidationResponse.Valid)
            false -> ResponseEntity.ok(IbanValidationResponse.Invalid("Invalid IBAN"))
        }
    }

    @PostMapping("/api/v2/validate/iban")
    fun validateIbanV2(@RequestBody iban: String): ResponseEntity<IbanValidationResponse> {
        return when (iban.matches(IBAN_REGEX.toRegex())) {
            true -> {
                when (val blacklistCheckResponse = blacklistCheckerApi.checkBlacklistV1(iban).body) {
                    is BlacklistCheckResponse.Blacklisted -> ResponseEntity.ok(
                        IbanValidationResponse.Invalid(
                            blacklistCheckResponse.reason
                        )
                    )

                    else -> ResponseEntity.ok(IbanValidationResponse.Valid)
                }
            }

            false -> ResponseEntity.ok(IbanValidationResponse.Invalid("Invalid IBAN"))
        }
    }
}