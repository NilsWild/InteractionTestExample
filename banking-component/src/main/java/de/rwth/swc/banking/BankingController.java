package de.rwth.swc.banking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class BankingController {

    private final RestTemplate restTemplate;

    public BankingController(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/v1/transfer")
    public ResponseEntity<Boolean> sendMoneyV1(@RequestBody Transfer transfer) {
        var result = restTemplate.postForEntity("http://localhost:8081/v1/validate/iban", transfer.iban, Boolean.class);
        if(Boolean.TRUE.equals(result.getBody())) {
            result = restTemplate.postForEntity("http://localhost:8082/v1/validate/amount", transfer.amount, Boolean.class);
        }
        if(Boolean.TRUE.equals(result.getBody())) {
            return ResponseEntity.ok(true);
        }else {
            return ResponseEntity.badRequest().body(false);
        }
    }

    @PostMapping("/v2/transfer")
    public ResponseEntity<Boolean> sendMoneyV2(@RequestBody Transfer transfer) {
        var result = restTemplate.postForEntity("http://localhost:8081/v2/validate/iban", transfer, Boolean.class);
        if(Boolean.TRUE.equals(result.getBody())) {
            return ResponseEntity.ok(true);
        }else {
            return ResponseEntity.badRequest().body(false);
        }
    }
}
