package de.rwth.swc.banking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface AmountValidationApi {

    @PostExchange("/v1/validate/amount")
    ResponseEntity<Boolean> sendMoneyV1(@RequestBody Transfer transfer);
}
