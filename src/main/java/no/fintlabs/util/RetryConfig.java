package no.fintlabs.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RetryConfig {

    @Bean
    public RetryTemplate kafkaRetryTemplate() {
        return RetryTemplate.builder()
                .exponentialBackoff(1000, 2.0, 10000)
                .maxAttempts(5)
                .build();
    }
}
