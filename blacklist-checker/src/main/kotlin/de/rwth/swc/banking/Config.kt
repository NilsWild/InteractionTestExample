package de.rwth.swc.banking

import io.github.projectmapk.jackson.module.kogera.KotlinFeature
import io.github.projectmapk.jackson.module.kogera.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class Config {
    @Bean
    fun kotlinModule() = KotlinModule.Builder().configure(KotlinFeature.SingletonSupport, true).build()
}
