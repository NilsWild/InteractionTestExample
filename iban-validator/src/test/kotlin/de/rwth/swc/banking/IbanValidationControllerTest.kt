package de.rwth.swc.banking

import com.fasterxml.jackson.databind.ObjectMapper
import de.rwth.swc.interact.junit.jupiter.annotation.InterACtTest
import de.rwth.swc.interact.junit.jupiter.annotation.Offset
import de.rwth.swc.interact.rest.RestMessage
import de.rwth.swc.interact.test.PropertyBasedAssertionError
import de.rwth.swc.interact.test.inherently
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.aggregator.AggregateWith
import org.junit.jupiter.params.provider.CsvSource
import org.mockserver.configuration.Configuration
import org.mockserver.integration.ClientAndServer
import org.mockserver.matchers.Times
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.MediaType
import org.slf4j.event.Level
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfig::class)
internal class IbanValidationControllerTest {
    @Autowired
    private lateinit var mapper: ObjectMapper

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var webClientBuilder: WebClient.Builder

    private lateinit var ibanValidationApi: IbanValidationApi

    @BeforeEach
    fun setUp() {
        mockServer.reset()
        ibanValidationApi = TestConfig.ibanValidatorApi(webClientBuilder, port)
    }

    @Nested
    inner class ApiV1 {
        @InterACtTest
        @CsvSource("DE89370400440532013000")
        fun `when a valid IBAN is provided the validator should respond that the IBAN is valid`(
            @AggregateWith(IbanAggregator::class) iban: RestMessage<String>
        ) {
            val result = ibanValidationApi.validateIbanV1(iban.body)
            inherently {
                Assertions.assertThat(result.body).isInstanceOf(IbanValidationResponse.Valid::class.java)
                Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
            }
        }

        @InterACtTest
        @CsvSource("MyIban")
        fun `when an invalid IBAN is provided the validator should respond that the IBAN is invalid`(
            @AggregateWith(IbanAggregator::class) iban: RestMessage<String>
        ) {
            val result = ibanValidationApi.validateIbanV1(iban.body)
            inherently {
                Assertions.assertThat(result.body).isInstanceOf(IbanValidationResponse.Invalid::class.java)
                Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
            }
        }
    }

    @Nested
    inner class ApiV2 {
        @InterACtTest
        @CsvSource(
            "DE33500105173249718433, false"
        )
        fun `when a valid IBAN is provided it and the blacklist check fails it should respond with the reason of the blacklist check`(
            @AggregateWith(IbanAggregator::class) iban: RestMessage<String>,
            @Offset(1) @AggregateWith(BlacklistCheckResponseAggregator::class) blacklistCheckResponse: RestMessage<BlacklistCheckResponse>
        ) {
            mockServer.`when`(
                HttpRequest.request().withMethod(HttpMethod.POST.name())
                    .withPath("/api/v1/check/blacklist")
                    .withBody(iban.body),
                Times.exactly(1)
            ).respond(
                HttpResponse.response().withStatusCode(HttpStatus.OK.value())
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody(mapper.writeValueAsString(blacklistCheckResponse.body))
            )
            val result = ibanValidationApi.validateIbanV2(iban.body)
            inherently {
                Assertions.assertThat(result.body).isInstanceOf(IbanValidationResponse.Invalid::class.java)
                Assertions
                    .assertThat((result.body as IbanValidationResponse.Invalid).errorMessage)
                    .isEqualTo((blacklistCheckResponse.body as BlacklistCheckResponse.Blacklisted).reason)
                Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
            }
        }

        @InterACtTest
        @CsvSource(
            "EE441295895115123636, true",
        )
        fun `when a valid IBAN is provided and the blacklist check succeeds it should respond that the iban is valid`(
            @AggregateWith(IbanAggregator::class) iban: RestMessage<String>,
            @Offset(1) @AggregateWith(BlacklistCheckResponseAggregator::class) blacklistCheckResponse: RestMessage<BlacklistCheckResponse>
        ) {
            mockServer.`when`(
                HttpRequest.request().withMethod(HttpMethod.POST.name())
                    .withPath("/api/v1/check/blacklist")
                    .withBody(iban.body),
                Times.exactly(1)
            ).respond(
                HttpResponse.response().withStatusCode(HttpStatus.OK.value())
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody(mapper.writeValueAsString(blacklistCheckResponse.body))
            )

            val result = ibanValidationApi.validateIbanV2(iban.body)

            inherently {
                Assertions.assertThat(result.body).isInstanceOf(IbanValidationResponse.Valid::class.java)
                Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
            }
        }

        @InterACtTest
        @CsvSource("300, DE33500105173249718433, DestinationIBAN")
        fun `when an invalid IBAN is provided the validator should respond that the IBAN is invalid`(
            @AggregateWith(IbanAggregator::class) iban: RestMessage<String>
        ) {
            val result = ibanValidationApi.validateIbanV2(iban.body)
            inherently {
                Assertions.assertThat(result.body).isInstanceOf(IbanValidationResponse.Invalid::class.java)
                Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
            }
        }
    }

    companion object {
        private lateinit var mockServer: ClientAndServer

        @JvmStatic
        @BeforeAll
        fun init() {
            mockServer = ClientAndServer.startClientAndServer(Configuration.configuration().logLevel(Level.ERROR),8082)
        }

        @JvmStatic
        @AfterAll
        fun stop() {
            mockServer.stop()
        }
    }
}