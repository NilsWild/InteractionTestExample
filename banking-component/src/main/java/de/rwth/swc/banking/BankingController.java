package de.rwth.swc.banking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BankingController {

    private final AmountValidationApi amountValidationApi;
    private final IbanValidationApi ibanValidationApi;

    public BankingController(AmountValidationApi amountValidationApi, IbanValidationApi ibanValidationApi) {
        this.amountValidationApi = amountValidationApi;
        this.ibanValidationApi = ibanValidationApi;
    }

    @PostMapping("/v1/transfer")
    public ResponseEntity<Boolean> sendMoneyV1(@RequestBody Transfer transfer) {
        var result = ibanValidationApi.validateIbanV1(transfer.fromIban);
        if (Boolean.TRUE.equals(result.getBody())) {
            result = amountValidationApi.sendMoneyV1(transfer);
        }
        if (Boolean.TRUE.equals(result.getBody())) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.ok().body(false);
        }
    }

    @PostMapping("/v2/transfer")
    public ResponseEntity<Boolean> sendMoneyV2(@RequestBody Transfer transfer) {
        var result = ibanValidationApi.validateIbanV2(transfer);
        if (Boolean.TRUE.equals(result.getBody())) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.ok().body(false);
        }
    }
}
