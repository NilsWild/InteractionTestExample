package de.rwth.swc.banking

import de.rwth.swc.interact.junit.jupiter.annotation.InterACtTest
import de.rwth.swc.interact.rest.RestMessage
import de.rwth.swc.interact.test.inherently
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.aggregator.AggregateWith
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfig::class)
internal class BlacklistCheckControllerTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var webClientBuilder: WebClient.Builder

    private lateinit var blacklistCheckerApi: BlacklistCheckerApi

    @Autowired
    private lateinit var blacklistStore: BlacklistStore

    @BeforeEach
    fun setUp() {
        blacklistCheckerApi = TestConfig.blacklistCheckApi(webClientBuilder, port)
    }

    @InterACtTest
    @CsvSource("DK6750511653371535")
    fun `when IBAN is on blacklist should report a match`(
        @AggregateWith(IbanAggregator::class) iban: RestMessage<String>
    ) {
        blacklistStore.addIban(iban.body, "IBAN is suspicious")
        val result = blacklistCheckerApi.checkBlacklistV1(iban.body)
        inherently {
            assertThat(result.body).isInstanceOf(BlacklistCheckResponse.Blacklisted::class.java)
            assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
        }
        blacklistStore.clear()
    }

    @InterACtTest
    @CsvSource("IS397193876714668732482789")
    fun `when IBAN is not on blacklist should report no match`(
        @AggregateWith(IbanAggregator::class) iban: RestMessage<String>
    ) {
        val result = blacklistCheckerApi.checkBlacklistV1(iban.body)
        inherently {
            assertThat(result.body).isInstanceOf(BlacklistCheckResponse.NotBlacklisted::class.java)
            assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
        }
    }
}