package de.rwth.swc.banking;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class Config {

    @Bean
    public AmountValidationApi amountValidationApi(RestTemplateBuilder restTemplateBuilder) {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8082")
                .build();
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient))
                .build();
        return httpServiceProxyFactory.createClient(AmountValidationApi.class);
    }

    @Bean
    public IbanValidationApi ibanValidationApi(RestTemplateBuilder restTemplateBuilder) {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient))
                .build();
        return httpServiceProxyFactory.createClient(IbanValidationApi.class);
    }
}
