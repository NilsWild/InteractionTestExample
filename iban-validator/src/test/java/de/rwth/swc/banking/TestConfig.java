package de.rwth.swc.banking;

import de.rwth.swc.interact.integrator.rest.RestTemplateIntegrationInterceptor;
import de.rwth.swc.interact.integrator.rest.TestRestTemplateIntegrationInterceptor;
import de.rwth.swc.interact.observer.rest.RestTemplateObservationInterceptor;
import de.rwth.swc.interact.observer.rest.TestRestTemplateObservationInterceptor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    @Bean
    public TestRestTemplate testRestTemplate(
            RestTemplateBuilder restTemplateBuilder,
            TestRestTemplateObservationInterceptor interceptor1,
            TestRestTemplateIntegrationInterceptor interceptor2
    ) {
        var testRestTemplate = new TestRestTemplate(restTemplateBuilder);
        testRestTemplate.getRestTemplate().getInterceptors()
                .removeIf(it -> it instanceof RestTemplateObservationInterceptor || it instanceof RestTemplateIntegrationInterceptor);


        testRestTemplate.getRestTemplate().getInterceptors().add(interceptor2);
        testRestTemplate.getRestTemplate().getInterceptors().add(interceptor1);
        return testRestTemplate;
    }
}