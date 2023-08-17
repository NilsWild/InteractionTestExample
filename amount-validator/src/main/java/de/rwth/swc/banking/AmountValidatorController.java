package de.rwth.swc.banking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AmountValidatorController {

    public Map<String,Integer> accounts = new HashMap<>();

    @PostMapping("/v1/validate/amount")
    public ResponseEntity<Boolean> sendMoneyV1(@RequestBody Transfer transfer) {
        return ResponseEntity.ok(transfer.amount > 0 && transfer.amount <= accounts.getOrDefault(transfer.fromIban.replace(" ",""),-1));
    }

}
