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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static de.rwth.swc.interact.test.ExampleBasedAssertionsKt.forExample;
import static de.rwth.swc.interact.test.PropertyBasedAssertionsKt.inherently;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestConfig.class})
class BankingControllerTest {

    @LocalServerPort
    private int port;
    private ObjectMapper mapper = new ObjectMapper();
    private BankingApi bankingApi;
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
        bankingApi = TestConfig.bankingApi(port);
        mockServer.reset();
    }

    @InterACtTest
    @CsvSource({"300, DE19 5001 0517 5326 8513 68, GE13617195993119486971, false"})
    public void v1WhenTransferIsSendAndIbanValidationFailsItFails(
            @AggregateWith(TransferAggregator.class) RestMessage<Transfer> transfer,
            @Offset(3) @AggregateWith(BooleanAggregator.class) RestMessage<Boolean> ibanValidatorResponse
    ) {

        mockServer.when(
                request().withMethod(HttpMethod.POST.name())
                        .withPath("/v1/validate/iban")
                        .withBody(transfer.getBody().fromIban),
                Times.exactly(1)
        ).respond(
                response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                        .withBody(String.valueOf(ibanValidatorResponse.getBody()))
        );

        var result = bankingApi.sendMoneyV1(transfer.getBody());

        inherently(() -> {
            assertThat(result.getBody()).isEqualTo(false);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            return Unit.INSTANCE;
        });
    }

    @InterACtTest
    @CsvSource({
            "500, DE93 5001 0517 6966 2689 58, GE13617195993119486971, true, true, true",
            "200, DE93 5001 0517 6966 2689 58, DE19 5001 0517 5326 8513 68, true, false, false"})
    public void v1WhenTransferIsSendAndIbanValidationSucceedsItRespondsAccordingToAmountValidation(
            @AggregateWith(TransferAggregator.class) RestMessage<Transfer> transfer,
            @Offset(3) @AggregateWith(BooleanAggregator.class) RestMessage<Boolean> ibanValidatorResponse,
            @Offset(4) @AggregateWith(BooleanAggregator.class) RestMessage<Boolean> amountValidatorResponse,
            @Offset(5) Boolean expectedResponse) throws JsonProcessingException {

        mockServer.when(
                request().withMethod(HttpMethod.POST.name())
                        .withPath("/v1/validate/iban")
                        .withBody(transfer.getBody().fromIban),
                Times.exactly(1)
        ).respond(
                response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                        .withBody(String.valueOf(ibanValidatorResponse.getBody()))
        );

        mockServer.when(
                request().withMethod(HttpMethod.POST.name())
                        .withPath("/v1/validate/amount")
                        .withBody(mapper.writeValueAsString(transfer.getBody())),
                Times.exactly(1)
        ).respond(
                response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                        .withBody(String.valueOf(amountValidatorResponse.getBody()))
        );

        var result = bankingApi.sendMoneyV1(transfer.getBody());

        forExample(() -> {
            assertThat(result.getBody()).isEqualTo(expectedResponse);
            return Unit.INSTANCE;
        });
        inherently(() -> {
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            return Unit.INSTANCE;
        });
    }

    @InterACtTest
    @CsvSource({
            "500, DE93 5001 0517 6966 2689 58, GE13617195993119486971, true, true",
            "200, GE13617195993119486971, DE19 5001 0517 5326 8513 68, false, false"})
    public void v2WhenTransferIsSendItIsHandled(
            @AggregateWith(TransferAggregator.class) RestMessage<Transfer> transfer,
            @Offset(3) @AggregateWith(BooleanAggregator.class) RestMessage<Boolean> validationResponse,
            @Offset(4) Boolean expectedResponse) throws JsonProcessingException {

        mockServer.when(
                request().withMethod(HttpMethod.POST.name())
                        .withPath("/v2/validate/iban")
                        .withBody(mapper.writeValueAsString(transfer.getBody())),
                Times.exactly(1)
        ).respond(
                response().withStatusCode(HttpStatus.OK.value())
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                        .withBody(String.valueOf(validationResponse.getBody()))
        );

        var result = bankingApi.sendMoneyV2(transfer.getBody());

        forExample(() -> {
            assertThat(result.getBody()).isEqualTo(expectedResponse);
            return Unit.INSTANCE;
        });

        inherently(() -> {
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            return Unit.INSTANCE;
        });
    }
}

