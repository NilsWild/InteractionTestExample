package de.rwth.swc.banking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface IbanValidationApi {

    @PostExchange("/v1/validate/iban")
    ResponseEntity<Boolean> validateIbanV1(@RequestBody String iban);

    @PostExchange("/v2/validate/iban")
    ResponseEntity<Boolean> validateIbanV2(@RequestBody Transfer transfer);

}
