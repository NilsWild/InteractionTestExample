package de.rwth.swc.banking

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange

interface BlacklistCheckerApi {

    @PostExchange("/api/v1/check/blacklist")
    fun checkBlacklistV1(@RequestBody iban: String): ResponseEntity<BlacklistCheckResponse>

}