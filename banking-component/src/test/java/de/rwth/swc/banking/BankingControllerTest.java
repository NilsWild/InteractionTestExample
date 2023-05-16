package de.rwth.swc.banking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rwth.swc.interact.integrator.rest.SpringRestInterACtIntegrationProxyConfiguration;
import de.rwth.swc.interact.junit.jupiter.InterACt;
import de.rwth.swc.interact.observer.rest.SpringRestInterACtConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestConfig.class, SpringRestInterACtConfiguration.class, SpringRestInterACtIntegrationProxyConfiguration.class})
@ExtendWith(InterACt.class)
class BankingControllerTest {

    @Autowired
    TestRestTemplate testRestTemplate;
    @LocalServerPort
    private int port;
    @Autowired
    private RestTemplate restTemplate;
    private ObjectMapper mapper = new ObjectMapper();

    private MockRestServiceServer mockServer;

    @BeforeEach
    public void init() {
        mockServer = MockRestServiceServer.bindTo(restTemplate).bufferContent().build();
    }

    @Test
    public void v1WhenValidTransferIsReceivedShouldReturnTrue() throws URISyntaxException {
        var transfer = new Transfer(500, "DE93 5001 0517 6966 2689 58");

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("http://localhost:8081/v1/validate/iban")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(transfer.iban))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(String.valueOf(true))
                );
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("http://localhost:8082/v1/validate/amount")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("500"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(String.valueOf(true))
                );
        var result = testRestTemplate.postForEntity("http://localhost:" + port + "/v1/transfer", transfer,
                Boolean.class);
        assertThat(result.getBody()).isEqualTo(true);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void v2WhenValidTransferIsReceivedShouldReturnTrue() throws URISyntaxException, JsonProcessingException {
        var transfer = new Transfer(500, "DE19 5001 0517 5326 8513 68");

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("http://localhost:8081/v2/validate/iban")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(mapper.writeValueAsString(transfer)))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(String.valueOf(true))
                );
        var result = testRestTemplate.postForEntity("http://localhost:" + port + "/v2/transfer", transfer,
                Boolean.class);
        assertThat(result.getBody()).isEqualTo(true);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}