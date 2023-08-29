package de.rwth.swc.banking


import com.fasterxml.jackson.databind.ObjectMapper
import de.rwth.swc.interact.domain.serialization.SerializationConstants
import de.rwth.swc.interact.junit.jupiter.annotation.InterACtTest
import de.rwth.swc.interact.junit.jupiter.annotation.Offset
import de.rwth.swc.interact.rest.RestMessage
import de.rwth.swc.interact.test.forExample
import de.rwth.swc.interact.test.inherently
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.aggregator.AggregateWith
import org.junit.jupiter.params.provider.CsvSource
import org.mockserver.integration.ClientAndServer
import org.mockserver.matchers.Times
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.MediaType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(
    TestConfig::class
)
internal class BankingControllerTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var webClientBuilder: WebClient.Builder

    private lateinit var bankingApi: BankingApi

    @Autowired
    private lateinit var mapper: ObjectMapper

    @Autowired
    private lateinit var bankAccountStore: BankAccountStore

    @BeforeEach
    fun setUp() {
        mockServer.reset()
        bankingApi = TestConfig.bankingApi(webClientBuilder, port)
    }

    @Nested
    inner class ApiV1 {
        @InterACtTest
        @CsvSource("DE19 5001 0517 5326 8513 68, invalidIban, 300, false")
        fun `when IBAN validation fails the transfer fails`(
            @AggregateWith(TransferAggregator::class) transfer: RestMessage<Transfer>,
            @Offset(3) @AggregateWith(IbanValidationResponseAggregator::class) ibanValidatorResponse: RestMessage<IbanValidationResponse>
        ) {
            mockServer.`when`(
                HttpRequest.request().withMethod(HttpMethod.POST.name())
                    .withPath("/api/v1/validate/iban")
                    .withBody(transfer.body.toIban),
                Times.exactly(1)
            ).respond(
                HttpResponse.response().withStatusCode(HttpStatus.OK.value())
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody(mapper.writeValueAsString(ibanValidatorResponse.body))
            )
            val result = bankingApi.transferV1(transfer.body)
            inherently {
                assertThat(result.body).isInstanceOf(TransferResponse.Failure::class.java)
                assertThat((result.body as TransferResponse.Failure).reason)
                    .isEqualTo((ibanValidatorResponse.body as IbanValidationResponse.Invalid).errorMessage)
                assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
            }
        }

        @InterACtTest
        @CsvSource(
            "DE93 5001 0517 6966 2689 58, GE13617195993119486971, 500, true, true"
        )
        fun `when IBAN validation succeeds and IBAN is not on blacklist and the account has sufficient funds the transfer is successful`(
            @AggregateWith(TransferAggregator::class) transfer: RestMessage<Transfer>,
            @Offset(3) @AggregateWith(IbanValidationResponseAggregator::class) ibanValidatorResponse: RestMessage<IbanValidationResponse>,
            @Offset(4) @AggregateWith(BlacklistCheckResponseAggregator::class) blacklistCheckResponse: RestMessage<BlacklistCheckResponse>
        ) {
            bankAccountStore.addBankAccount(transfer.body.fromIban, transfer.body.amount)
            mockServer.`when`(
                HttpRequest.request().withMethod(HttpMethod.POST.name())
                    .withPath("/api/v1/validate/iban")
                    .withBody(transfer.body.toIban),
                Times.exactly(1)
            ).respond(
                HttpResponse.response().withStatusCode(HttpStatus.OK.value())
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody(mapper.writeValueAsString(ibanValidatorResponse.body))
            )
            mockServer.`when`(
                HttpRequest.request().withMethod(HttpMethod.POST.name())
                    .withPath("/api/v1/check/blacklist")
                    .withBody(transfer.body.toIban),
                Times.exactly(1)
            ).respond(
                HttpResponse.response().withStatusCode(HttpStatus.OK.value())
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody(mapper.writeValueAsString(blacklistCheckResponse.body))
            )
            val result = bankingApi.transferV1(transfer.body)
            inherently {
                assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
                assertThat(result.body).isInstanceOf(TransferResponse.Success::class.java)
                assertThat(bankAccountStore.getBalance(transfer.body.fromIban)).isEqualTo(0)
            }
            bankAccountStore.clear()
        }

        @InterACtTest
        @CsvSource(
            "DE93 5001 0517 6966 2689 58, GE13617195993119486971, 500, true, true"
        )
        fun `when IBAN validation succeeds and IBAN is not on blacklist but the account has insufficient funds the transfer fails`(
            @AggregateWith(TransferAggregator::class) transfer: RestMessage<Transfer>,
            @Offset(3) @AggregateWith(IbanValidationResponseAggregator::class) ibanValidatorResponse: RestMessage<IbanValidationResponse>,
            @Offset(4) @AggregateWith(BlacklistCheckResponseAggregator::class) blacklistCheckResponse: RestMessage<BlacklistCheckResponse>
        ) {
            bankAccountStore.addBankAccount(transfer.body.fromIban, transfer.body.amount - 1)
            mockServer.`when`(
                HttpRequest.request().withMethod(HttpMethod.POST.name())
                    .withPath("/api/v1/validate/iban")
                    .withBody(transfer.body.toIban),
                Times.exactly(1)
            ).respond(
                HttpResponse.response().withStatusCode(HttpStatus.OK.value())
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody(mapper.writeValueAsString(ibanValidatorResponse.body))
            )
            mockServer.`when`(
                HttpRequest.request().withMethod(HttpMethod.POST.name())
                    .withPath("/api/v1/check/blacklist")
                    .withBody(transfer.body.toIban),
                Times.exactly(1)
            ).respond(
                HttpResponse.response().withStatusCode(HttpStatus.OK.value())
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody(mapper.writeValueAsString(blacklistCheckResponse.body))
            )
            val result = bankingApi.transferV1(transfer.body)
            inherently {
                assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
                assertThat(result.body).isInstanceOf(TransferResponse.Failure::class.java)
                assertThat((result.body as TransferResponse.Failure).reason)
                    .isEqualTo("Insufficient funds")
            }
            bankAccountStore.clear()
        }

        @InterACtTest
        @CsvSource(
            "DE93 5001 0517 6966 2689 58, DE19 5001 0517 5326 8513 68, 200, true, false"
        )
        fun `when IBAN validation succeeds but IBAN is on blacklist the transfer fails`(
            @AggregateWith(TransferAggregator::class) transfer: RestMessage<Transfer>,
            @Offset(3) @AggregateWith(IbanValidationResponseAggregator::class) ibanValidatorResponse: RestMessage<IbanValidationResponse>,
            @Offset(4) @AggregateWith(BlacklistCheckResponseAggregator::class) blacklistCheckResponse: RestMessage<BlacklistCheckResponse>
        ) {
            mockServer.`when`(
                HttpRequest.request().withMethod(HttpMethod.POST.name())
                    .withPath("/api/v1/validate/iban")
                    .withBody(transfer.body.toIban),
                Times.exactly(1)
            ).respond(
                HttpResponse.response().withStatusCode(HttpStatus.OK.value())
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody(mapper.writeValueAsString(ibanValidatorResponse.body))
            )
            mockServer.`when`(
                HttpRequest.request().withMethod(HttpMethod.POST.name())
                    .withPath("/api/v1/check/blacklist")
                    .withBody(transfer.body.toIban),
                Times.exactly(1)
            ).respond(
                HttpResponse.response().withStatusCode(HttpStatus.OK.value())
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody(mapper.writeValueAsString(blacklistCheckResponse.body))
            )
            val result = bankingApi.transferV1(transfer.body)
            inherently {
                assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
                assertThat(result.body).isInstanceOf(TransferResponse.Failure::class.java)
                assertThat((result.body as TransferResponse.Failure).reason)
                    .isEqualTo((blacklistCheckResponse.body as BlacklistCheckResponse.Blacklisted).reason)
            }
        }
    }

    @Nested
    inner class ApiV2 {
        @InterACtTest
        @CsvSource(
            "DE93 5001 0517 6966 2689 58, GE13617195993119486971, 500, true"
        )
        fun `when IBAN validation succeeds but the account has insufficient funds the transfer fails`(
            @AggregateWith(TransferAggregator::class) transfer: RestMessage<Transfer>,
            @Offset(3) @AggregateWith(IbanValidationResponseAggregator::class) validationResponse: RestMessage<IbanValidationResponse>
        ) {
            bankAccountStore.addBankAccount(transfer.body.fromIban, transfer.body.amount - 1)
            mockServer.`when`(
                HttpRequest.request().withMethod(HttpMethod.POST.name())
                    .withPath("/api/v2/validate/iban")
                    .withBody(transfer.body.toIban),
                Times.exactly(1)
            ).respond(
                HttpResponse.response().withStatusCode(HttpStatus.OK.value())
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody(mapper.writeValueAsString(validationResponse.body))
            )
            val result = bankingApi.transferV2(transfer.body)
            inherently {
                assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
                assertThat(result.body).isInstanceOf(TransferResponse.Failure::class.java)
                assertThat((result.body as TransferResponse.Failure).reason)
                    .isEqualTo("Insufficient funds")
            }
            bankAccountStore.clear()
        }

        @InterACtTest
        @CsvSource(
            "DE19 5001 0517 5326 8513 68, invalidIban, 200, false"
        )
        fun `when IBAN validation fails the transfer should fail`(
            @AggregateWith(TransferAggregator::class) transfer: RestMessage<Transfer>,
            @Offset(3) @AggregateWith(IbanValidationResponseAggregator::class) validationResponse: RestMessage<IbanValidationResponse>
        ) {
            mockServer.`when`(
                HttpRequest.request().withMethod(HttpMethod.POST.name())
                    .withPath("/api/v2/validate/iban")
                    .withBody(transfer.body.toIban),
                Times.exactly(1)
            ).respond(
                HttpResponse.response().withStatusCode(HttpStatus.OK.value())
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody(mapper.writeValueAsString(validationResponse.body))
            )
            val result = bankingApi.transferV2(transfer.body)
            inherently {
                assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
                assertThat(result.body).isInstanceOf(TransferResponse.Failure::class.java)
                assertThat((result.body as TransferResponse.Failure).reason)
                    .isEqualTo((validationResponse.body as IbanValidationResponse.Invalid).errorMessage)
            }
        }

        @InterACtTest
        @CsvSource(
            "DE93 5001 0517 6966 2689 58, GE13617195993119486971, 500, true"
        )
        fun `when IBAN validation succeeds and the account has sufficient funds the transfer succeeds`(
            @AggregateWith(TransferAggregator::class) transfer: RestMessage<Transfer>,
            @Offset(3) @AggregateWith(IbanValidationResponseAggregator::class) validationResponse: RestMessage<IbanValidationResponse>
        ) {
            bankAccountStore.addBankAccount(transfer.body.fromIban, transfer.body.amount)
            mockServer.`when`(
                HttpRequest.request().withMethod(HttpMethod.POST.name())
                    .withPath("/api/v2/validate/iban")
                    .withBody(transfer.body.toIban),
                Times.exactly(1)
            ).respond(
                HttpResponse.response().withStatusCode(HttpStatus.OK.value())
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody(mapper.writeValueAsString(validationResponse.body))
            )
            val result = bankingApi.transferV2(transfer.body)
            inherently {
                assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
                assertThat(result.body).isInstanceOf(TransferResponse.Success::class.java)
                assertThat(bankAccountStore.getBalance(transfer.body.fromIban)).isEqualTo(0)
            }
            bankAccountStore.clear()
        }
    }

    companion object {

        private lateinit var mockServer: ClientAndServer

        @JvmStatic
        @BeforeAll
        fun init() {
            mockServer = ClientAndServer.startClientAndServer(8082)
        }

        @JvmStatic
        @AfterAll
        fun stop() {
            mockServer.stop()
        }
    }
}
