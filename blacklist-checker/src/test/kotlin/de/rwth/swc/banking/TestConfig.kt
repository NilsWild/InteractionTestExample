package de.rwth.swc.banking

import de.rwth.swc.interact.rest.observer.WebClientObserver
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

class TestConfig {

    companion object {
        fun blacklistCheckApi(
            builder: WebClient.Builder,
            port: Int
        ): BlacklistCheckerApi {
            val webClient = builder
                .baseUrl("http://localhost:$port")
                .filter(WebClientObserver(true))
                .build()
            val httpServiceProxyFactory = HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient))
                .build()
            return httpServiceProxyFactory.createClient(BlacklistCheckerApi::class.java)
        }
    }
}
