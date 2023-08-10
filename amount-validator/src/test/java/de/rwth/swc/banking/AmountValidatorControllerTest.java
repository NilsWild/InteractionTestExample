package de.rwth.swc.banking;

import de.rwth.swc.interact.junit.jupiter.annotation.InterACtTest;
import de.rwth.swc.interact.observer.rest.SpringRestInterACtConfiguration;
import kotlin.Unit;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import static de.rwth.swc.interact.test.PropertyBasedAssertionsKt.inherently;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestConfig.class, SpringRestInterACtConfiguration.class})
class AmountValidatorControllerTest {

    @Autowired
    TestRestTemplate testRestTemplate;
    @LocalServerPort
    private int port;

    @InterACtTest
    @CsvSource({"500"})
    public void whenAmountIsBetween0And1000ShouldReturnTrue(int amount) {
        var result = testRestTemplate.postForEntity("http://localhost:" + port + "/v1/validate/amount", amount, Boolean.class);
        inherently(() -> {
            assertThat(result.getBody()).isEqualTo(true);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            return Unit.INSTANCE;
        });
    }

    @InterACtTest
    @CsvSource({"-500"})
    public void whenAmountIsNegativeShouldReturnFalse(int amount) {
        var result = testRestTemplate.postForEntity("http://localhost:" + port + "/v1/validate/amount", amount, Boolean.class);
        inherently(() -> {
            assertThat(result.getBody()).isEqualTo(false);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            return Unit.INSTANCE;
        });
    }

}