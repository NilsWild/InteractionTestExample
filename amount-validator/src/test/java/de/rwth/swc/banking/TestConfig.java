package de.rwth.swc.banking;

import de.rwth.swc.interact.rest.observer.WebClientObserver;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;


public class TestConfig {

    public static AmountValidationApi amountValidationApi(int port) {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .filter(new WebClientObserver(true))
                .build();
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient))
                .build();
        return httpServiceProxyFactory.createClient(AmountValidationApi.class);
    }

}
