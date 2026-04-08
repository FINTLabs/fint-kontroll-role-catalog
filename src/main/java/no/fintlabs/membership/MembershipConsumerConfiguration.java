package no.fintlabs.membership;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.util.MissingReferenceException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipConsumerConfiguration {

    private final MembershipService membershipService;
    private final RetryTemplate kafkaRetryTemplate;


    @Bean
    public ConcurrentMessageListenerContainer<String, KafkaMembership> membershipConsumer(
            EntityConsumerFactoryService entityConsumerFactoryService
    ) {
        EntityTopicNameParameters entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("role-membership")
                .build();


        return entityConsumerFactoryService
                .createFactory(KafkaMembership.class, this::processWithRetry)
                .createContainer(entityTopicNameParameters);
    }

    private void processWithRetry(ConsumerRecord<String, KafkaMembership> record) {
        kafkaRetryTemplate.execute(context -> {
            process(record);
            return null;
        });
    }

    private void process(ConsumerRecord<String, KafkaMembership> record) {
        try {
            membershipService.processMembership(record.value());
        } catch (MissingReferenceException ex) {
            log.warn("Missing data for Kafka membership event: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to process membership", ex);
            throw ex;
        }
    }

}
