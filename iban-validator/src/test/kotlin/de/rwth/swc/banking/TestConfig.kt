package de.rwth.swc.banking

import de.rwth.swc.interact.rest.observer.WebClientObserver
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory


@TestConfiguration
class TestConfig {

    @Bean
    @Primary
    fun blacklistCheckTestApi(builder: WebClient.Builder): BlacklistCheckerApi {
        val webClient = builder
            .baseUrl("http://localhost:8082")
            .filter(WebClientObserver(false))
            .build()
        val httpServiceProxyFactory = HttpServiceProxyFactory
            .builder(WebClientAdapter.forClient(webClient))
            .build()
        return httpServiceProxyFactory.createClient(BlacklistCheckerApi::class.java)
    }

    companion object {
        fun ibanValidatorApi(
            builder: WebClient.Builder,
            port: Int
        ): IbanValidationApi {
            val webClient = builder
                .baseUrl("http://localhost:$port")
                .filter(WebClientObserver(true))
                .build()
            val httpServiceProxyFactory = HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient))
                .build()
            return httpServiceProxyFactory.createClient(IbanValidationApi::class.java)
        }
    }
}
