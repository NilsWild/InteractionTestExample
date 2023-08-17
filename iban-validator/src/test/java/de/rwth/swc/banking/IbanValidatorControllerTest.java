package de.rwth.swc.banking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rwth.swc.interact.junit.jupiter.annotation.InterACtTest;
import de.rwth.swc.interact.junit.jupiter.annotation.Offset;
import de.rwth.swc.interact.observer.rest.SpringRestInterACtConfiguration;
import kotlin.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static de.rwth.swc.interact.test.PropertyBasedAssertionsKt.inherently;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestConfig.class, SpringRestInterACtConfiguration.class})
class IbanValidatorControllerTest {

    @Autowired
    TestRestTemplate testRestTemplate;
    @LocalServerPort
    private int port;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private IbanValidatorController ibanValidatorController;
    private ObjectMapper mapper = new ObjectMapper();

    private MockRestServiceServer mockServer;

    @BeforeEach
    public void init() {
        mockServer = MockRestServiceServer.bindTo(restTemplate).bufferContent().build();
    }

    @InterACtTest
    @CsvSource({"DE89370400440532013000"})
    public void v1WhenValidIbanIsReceivedShouldReturnTrue(String iban) {
        ibanValidatorController.ibanList.add(iban.replace(" ",""));

        var result = testRestTemplate.postForEntity("http://localhost:" + port + "/v1/validate/iban", iban,
                Boolean.class);
        inherently(() -> {
            assertThat(result.getBody()).isEqualTo(true);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            return Unit.INSTANCE;
        });

        ibanValidatorController.ibanList = new ArrayList<>();
    }

    @InterACtTest
    @CsvSource({"DE89370400440532013000"})
    public void v1WhenInvalidIbanIsReceivedShouldReturnFalse(String iban) {
        var result = testRestTemplate.postForEntity("http://localhost:" + port + "/v1/validate/iban", iban,
                Boolean.class);
        inherently(() -> {
            assertThat(result.getBody()).isEqualTo(false);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            return Unit.INSTANCE;
        });
    }

    @InterACtTest
    @CsvSource({"300, DE33500105173249718433, EE441295895115123636, true"})
    public void v2WhenValidTransferIsReceivedShouldReturnTrue(
            @AggregateWith(TransferAggregator.class) Transfer transfer,
            @Offset(3) boolean amountValidationResponse) throws URISyntaxException, JsonProcessingException {

        ibanValidatorController.ibanList.add(transfer.fromIban.replace(" ",""));

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("http://localhost:8082/v1/validate/amount")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(mapper.writeValueAsString(transfer)))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(String.valueOf(amountValidationResponse))
                );

        var result = testRestTemplate.postForEntity("http://localhost:" + port + "/v2/validate/iban", transfer,
                Boolean.class);
        inherently(() -> {
            assertThat(result.getBody()).isEqualTo(true);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            return Unit.INSTANCE;
        });

        ibanValidatorController.ibanList = new ArrayList<>();
    }

    @InterACtTest
    @CsvSource({"300, DE33500105173249718433, EE441295895115123636, false"})
    void v2WhenTransferWithValidIbanButInvalidAmountIsReceivedShouldReturnFalse(
            @AggregateWith(TransferAggregator.class) Transfer transfer,
            @Offset(3) boolean amountValidationResponse) throws URISyntaxException, JsonProcessingException {

        ibanValidatorController.ibanList.add(transfer.fromIban.replace(" ",""));

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("http://localhost:8082/v1/validate/amount")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(mapper.writeValueAsString(transfer)))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(String.valueOf(amountValidationResponse))
                );

        var result = testRestTemplate.postForEntity("http://localhost:" + port + "/v2/validate/iban", transfer,
                Boolean.class);
        inherently(() -> {
            assertThat(result.getBody()).isEqualTo(false);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            return Unit.INSTANCE;
        });

        ibanValidatorController.ibanList = new ArrayList<>();
    }

    @InterACtTest
    @CsvSource({"300, DE33500105173249718433, EE441295895115123636"})
    void v2WhenTransferWithInValidIbanIsReceivedShouldReturnFalse(
            @AggregateWith(TransferAggregator.class) Transfer transfer
    ) throws URISyntaxException {

        var result = testRestTemplate.postForEntity("http://localhost:" + port + "/v2/validate/iban", transfer,
                Boolean.class);
        inherently(() -> {
            assertThat(result.getBody()).isEqualTo(false);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            return Unit.INSTANCE;
        });

    }


}