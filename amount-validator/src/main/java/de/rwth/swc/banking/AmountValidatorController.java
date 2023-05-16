package de.rwth.swc.banking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class AmountValidatorController {

    @PostMapping("/v1/validate/amount")
    public ResponseEntity<Boolean> sendMoneyV1(@RequestBody Integer amount) {
        return ResponseEntity.ok(amount > 0 && amount < 1000);
    }

}
