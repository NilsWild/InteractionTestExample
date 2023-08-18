package de.rwth.swc.banking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class IbanValidatorController {

    private static final String IBAN_REGEX = "^[A-Z]{2}[0-9]{2}(?:[ ]?[0-9]{4}){4}(?:[ ]?[0-9]{1,2})?$";
    private final AmountValidationApi amountValidationApi;

    public List<String> ibanList = new ArrayList();

    public IbanValidatorController(final AmountValidationApi amountValidationApi) {
        this.amountValidationApi = amountValidationApi;
    }

    @PostMapping("/v1/validate/iban")
    public ResponseEntity<Boolean> sendMoneyV1(@RequestBody String iban) {
        return ResponseEntity.ok(iban.matches(IBAN_REGEX) && ibanList.contains(iban.replace(" ", "")));
    }

    @PostMapping("/v2/validate/iban")
    public ResponseEntity<Boolean> sendMoneyV2(@RequestBody Transfer transfer) {
        if (transfer.fromIban.matches(IBAN_REGEX) && ibanList.contains(transfer.fromIban.replace(" ", ""))) {
            var result = amountValidationApi.sendMoneyV1(transfer);
            return ResponseEntity.ok(result.getBody());
        } else {
            return ResponseEntity.ok().body(false);
        }
    }
}
