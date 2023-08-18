package de.rwth.swc.banking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface BankingApi {

    @PostExchange("/v1/transfer")
    ResponseEntity<Boolean> sendMoneyV1(@RequestBody Transfer transfer);

    @PostExchange("/v2/transfer")
    ResponseEntity<Boolean> sendMoneyV2(@RequestBody Transfer transfer);
}
