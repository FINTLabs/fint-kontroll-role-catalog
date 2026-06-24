package no.fintlabs.membership;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.KafkaConsumerConfigurationDefaults;
import no.novari.kafka.consuming.*;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
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
    private final KafkaConsumerConfigurationDefaults kafkaConsumerConfigurationDefaults;


    @Bean
    public ConcurrentMessageListenerContainer<String, KafkaMembership> membershipConsumer(
            ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService
    ) {
        ParameterizedListenerContainerFactory<KafkaMembership> recordListenerFactory =
                parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                        KafkaMembership.class,
                        this::processWithRetry,
                        kafkaConsumerConfigurationDefaults.continueFromPreviousListenerConfiguration(),
                        kafkaConsumerConfigurationDefaults.defaultErrorHandler()
                );

        EntityTopicNameParameters entityTopicNameParameters =
                kafkaConsumerConfigurationDefaults.defaultEntityTopic("role-membership");


        return recordListenerFactory.createContainer(entityTopicNameParameters);
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
