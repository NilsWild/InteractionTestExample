package de.rwth.swc.banking

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class BlacklistCheckController(private val blacklistStore: BlacklistStore) {

    @PostMapping("/api/v1/check/blacklist")
    fun checkBlacklistV1(@RequestBody iban: String): ResponseEntity<BlacklistCheckResponse> {
        return when (blacklistStore.isBlacklisted(iban)) {
            true -> ResponseEntity.ok(BlacklistCheckResponse.Blacklisted(blacklistStore.getReason(iban)))
            false -> ResponseEntity.ok(BlacklistCheckResponse.NotBlacklisted)
        }
    }

}