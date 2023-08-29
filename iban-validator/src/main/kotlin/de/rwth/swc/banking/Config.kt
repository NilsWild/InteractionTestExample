package de.rwth.swc.banking

import io.github.projectmapk.jackson.module.kogera.KotlinFeature
import io.github.projectmapk.jackson.module.kogera.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory


@Configuration
class Config {
    @Bean
    fun blacklistCheckApi(builder: WebClient.Builder): BlacklistCheckerApi {
        val webClient = builder
            .baseUrl("http://localhost:8082")
            .build()
        val httpServiceProxyFactory = HttpServiceProxyFactory
            .builder(WebClientAdapter.forClient(webClient))
            .build()
        return httpServiceProxyFactory.createClient(BlacklistCheckerApi::class.java)
    }

    @Bean
    fun kotlinModule() = KotlinModule.Builder().configure(KotlinFeature.SingletonSupport, true).build()
}
