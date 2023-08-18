package de.rwth.swc.banking;

import de.rwth.swc.interact.rest.observer.WebClientObserver;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public AmountValidationApi testAmountValidationApi() {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8082")
                .filter(new WebClientObserver(false))
                .build();
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient))
                .build();
        return httpServiceProxyFactory.createClient(AmountValidationApi.class);
    }

    @Bean
    @Primary
    public IbanValidationApi testIbanValidationApi() {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8082")
                .filter(new WebClientObserver(false))
                .build();
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient))
                .build();
        return httpServiceProxyFactory.createClient(IbanValidationApi.class);
    }

    static BankingApi bankingApi(int port) {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .filter(new WebClientObserver(true))
                .build();
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient))
                .build();
        return httpServiceProxyFactory.createClient(BankingApi.class);
    }
}
