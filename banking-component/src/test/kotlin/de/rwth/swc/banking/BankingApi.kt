package de.rwth.swc.banking

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange

interface BankingApi {
    @PostExchange("/api/v1/transfer")
    fun transferV1(@RequestBody transfer: Transfer): ResponseEntity<TransferResponse>

    @PostExchange("/api/v2/transfer")
    fun transferV2(@RequestBody transfer: Transfer): ResponseEntity<TransferResponse>
}