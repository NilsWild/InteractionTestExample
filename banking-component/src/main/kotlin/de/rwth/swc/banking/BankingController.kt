package de.rwth.swc.banking

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class BankingController(
    private val ibanValidationApi: IbanValidationApi,
    private val blacklistCheckerApi: BlacklistCheckerApi,
    private val bankAccountStore: BankAccountStore
) {

    @PostMapping("/api/v1/transfer")
    fun transferV1(@RequestBody transfer: Transfer): ResponseEntity<TransferResponse> {
        return when (val ibanValidationResponse = ibanValidationApi.validateIbanV1(transfer.toIban).body) {
            is IbanValidationResponse.Valid -> {
                when (val blacklistCheckResponse = blacklistCheckerApi.checkBlacklistV1(transfer.toIban).body) {
                    is BlacklistCheckResponse.NotBlacklisted -> ResponseEntity.ok().body(handleTransfer(transfer))
                    is BlacklistCheckResponse.Blacklisted -> ResponseEntity.ok()
                        .body(TransferResponse.Failure(blacklistCheckResponse.reason))

                    else -> ResponseEntity.ok().body(TransferResponse.Failure("An unexpected Error occurred"))
                }
            }

            is IbanValidationResponse.Invalid -> ResponseEntity.ok()
                .body(TransferResponse.Failure(ibanValidationResponse.errorMessage))

            else -> ResponseEntity.ok().body(TransferResponse.Failure("An unexpected Error occurred"))
        }
    }

    @PostMapping("/api/v2/transfer")
    fun transferV2(@RequestBody transfer: Transfer): ResponseEntity<TransferResponse> {
        return when (val ibanValidationResponse = ibanValidationApi.validateIbanV2(transfer.toIban).body) {
            is IbanValidationResponse.Valid -> ResponseEntity.ok().body(handleTransfer(transfer))
            is IbanValidationResponse.Invalid -> ResponseEntity.ok()
                .body(TransferResponse.Failure(ibanValidationResponse.errorMessage))

            else -> ResponseEntity.ok().body(TransferResponse.Failure("An unexpected Error occurred"))
        }
    }

    private fun handleTransfer(transfer: Transfer): TransferResponse {
        try {
            val fromBalance = bankAccountStore.getBalance(transfer.fromIban)
            if (fromBalance >= transfer.amount) {
                val newBalance = fromBalance - transfer.amount
                bankAccountStore.setBalance(transfer.fromIban, newBalance)
                return TransferResponse.Success(newBalance)
            }
            return TransferResponse.Failure("Insufficient funds")
        } catch (e: IllegalArgumentException) {
            return TransferResponse.Failure("No bank account with IBAN ${transfer.fromIban}")
        }
    }
}