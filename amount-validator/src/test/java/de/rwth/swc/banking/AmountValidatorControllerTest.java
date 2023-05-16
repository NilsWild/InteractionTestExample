package de.rwth.swc.banking;

import de.rwth.swc.interact.integrator.rest.SpringRestInterACtIntegrationProxyConfiguration;
import de.rwth.swc.interact.junit.jupiter.InterACt;
import de.rwth.swc.interact.observer.rest.SpringRestInterACtConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestConfig.class, SpringRestInterACtConfiguration.class, SpringRestInterACtIntegrationProxyConfiguration.class})
@ExtendWith(InterACt.class)
class AmountValidatorControllerTest {

    @Autowired
    TestRestTemplate testRestTemplate;
    @LocalServerPort
    private int port;

    @Test
    public void whenAmountIsBetween0And1000ShouldReturnTrue() {
        var result = testRestTemplate.postForEntity("http://localhost:" + port + "/v1/validate/amount", 500, Boolean.class);
        assertThat(result.getBody()).isEqualTo(true);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void whenAmountIsNegativeShouldReturnFalse() {
        var result = testRestTemplate.postForEntity("http://localhost:" + port + "/v1/validate/amount", -500, Boolean.class);
        assertThat(result.getBody()).isEqualTo(false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}