package de.rwth.swc.banking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class IbanValidatorController {

    private static final String IBAN_REGEX = "^[A-Z]{2}[0-9]{2}(?:[ ]?[0-9]{4}){4}(?:[ ]?[0-9]{1,2})?$";
    private final RestTemplate restTemplate;

    public IbanValidatorController(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/v1/validate/iban")
    public ResponseEntity<Boolean> sendMoneyV1(@RequestBody String iban) {
        return ResponseEntity.ok(iban.matches(IBAN_REGEX));
    }

    @PostMapping("/v2/validate/iban")
    public ResponseEntity<Boolean> sendMoneyV2(@RequestBody Transfer transfer) {
        if(transfer.iban.matches(IBAN_REGEX)) {
            var result = restTemplate.postForEntity("http://localhost:8082/v1/validate/amount", transfer.amount, Boolean.class);
            return ResponseEntity.ok(result.getBody());
        }else {
            return ResponseEntity.badRequest().body(false);
        }
    }
}
