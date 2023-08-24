package de.rwth.swc.banking;

import de.rwth.swc.interact.junit.jupiter.annotation.InterACtTest;
import de.rwth.swc.interact.rest.RestMessage;
import kotlin.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.util.HashMap;

import static de.rwth.swc.interact.test.PropertyBasedAssertionsKt.inherently;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AmountValidatorControllerTest {

    @LocalServerPort
    private int port;
    @Autowired
    private AmountValidatorController amountValidatorController;
    private AmountValidationApi amountValidationApi;

    @BeforeEach
    void beforeEach() {
        amountValidationApi = TestConfig.amountValidationApi(port);
    }

    @InterACtTest
    @CsvSource({"600, LI3008800671232812246, DK6750511653371535"})
    public void whenAmountIsBetween0AndBalanceShouldReturnTrue(
            @AggregateWith(TransferAggregator.class) RestMessage<Transfer> transfer
    ) {

        amountValidatorController.accounts.put(
                transfer.getBody().fromIban.replace(" ", ""),
                transfer.getBody().amount
        );
        var result = amountValidationApi.sendMoneyV1(transfer.getBody());
        inherently(() -> {
            assertThat(result.getBody()).isEqualTo(true);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            return Unit.INSTANCE;
        });

        amountValidatorController.accounts = new HashMap<>();
    }

    @InterACtTest
    @CsvSource({"-600, DK2950519923344578, IS397193876714668732482789"})
    public void whenAmountIsNegativeShouldReturnFalse(
            @AggregateWith(TransferAggregator.class) RestMessage<Transfer> transfer
    ) {
        var result = amountValidationApi.sendMoneyV1(transfer.getBody());
        inherently(() -> {
            assertThat(result.getBody()).isEqualTo(false);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            return Unit.INSTANCE;
        });
    }

}