package de.rwth.swc.banking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rwth.swc.interact.junit.jupiter.annotation.InterACtTest;
import de.rwth.swc.interact.junit.jupiter.annotation.Offset;
import de.rwth.swc.interact.rest.RestMessage;
import kotlin.Unit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;

import static de.rwth.swc.interact.test.PropertyBasedAssertionsKt.inherently;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestConfig.class})
class IbanValidatorControllerTest {

    @LocalServerPort
    private int port;
    @Autowired
    private IbanValidatorController ibanValidatorController;
    private ObjectMapper mapper = new ObjectMapper();
    private IbanValidationApi ibanValidationApi;
    private static ClientAndServer mockServer;

    @BeforeAll
    public static void init() {
        mockServer = ClientAndServer.startClientAndServer(8082);
    }

    @AfterAll
    public static void stop() {
        mockServer.stop();
    }

    @BeforeEach
    public void setUp() {
        ibanValidationApi = TestConfig.ibanValidationApi(port);
        mockServer.reset();
    }

    @InterACtTest
    @CsvSource({"DE89370400440532013000"})
    public void v1WhenValidIbanIsReceivedShouldReturnTrue(
            @AggregateWith(StringAggregator.class) RestMessage<String> iban
    ) {
        ibanValidatorController.ibanList.add(iban.getBody().replace(" ", ""));

        var result = ibanValidationApi.validateIbanV1(iban.getBody());
        inherently(() -> {
            assertThat(result.getBody()).isEqualTo(true);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            return Unit.INSTANCE;
        });

        ibanValidatorController.ibanList = new ArrayList<>();
    }

    @InterACtTest
    @CsvSource({"DE89370400440532013000"})
    public void v1WhenInvalidIbanIsReceivedShouldReturnFalse(
            @AggregateWith(StringAggregator.class) RestMessage<String> iban
    ) {
        var result = ibanValidationApi.validateIbanV1(iban.getBody());
        inherently(() -> {
            assertThat(result.getBody()).isEqualTo(false);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            return Unit.INSTANCE;
        });
    }

    @InterACtTest
    @CsvSource({
            "300, DE33500105173249718433, EE441295895115123636, true",
            "300, DE33500105173249718433, EE441295895115123636, false"})
    public void v2WhenValidIbanIsReceivedShouldReturnDependingOnAmountValidation(
            @AggregateWith(TransferAggregator.class) RestMessage<Transfer> transfer,
            @Offset(3) @AggregateWith(BooleanAggregator.class) RestMessage<Boolean> amountValidationResponse
    ) throws JsonProcessingException {

        ibanValidatorController.ibanList.add(transfer.getBody().fromIban.replace(" ", ""));
        mockServer.when(
                request().withMethod(HttpMethod.POST.name())
                        .withPath("/v1/validate/amount")
                        .withBody(mapper.writeValueAsString(transfer.getBody())),
                Times.exactly(1)
        ).respond(
                response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(String.valueOf(amountValidationResponse.getBody()))
        );

        var result = ibanValidationApi.validateIbanV2(transfer.getBody());
        inherently(() -> {
            assertThat(result.getBody()).isEqualTo(amountValidationResponse.getBody());
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            return Unit.INSTANCE;
        });

        ibanValidatorController.ibanList = new ArrayList<>();
    }

    @InterACtTest
    @CsvSource({"300, DE33500105173249718433, EE441295895115123636"})
    void v2WhenInvalidIbanIsReceivedShouldReturnFalse(
            @AggregateWith(TransferAggregator.class) RestMessage<Transfer> transfer
    ) {

        var result = ibanValidationApi.validateIbanV2(transfer.getBody());
        inherently(() -> {
            assertThat(result.getBody()).isEqualTo(false);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            return Unit.INSTANCE;
        });

    }


}