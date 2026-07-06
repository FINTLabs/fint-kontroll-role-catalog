package no.fintlabs.role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.KafkaConsumerConfigurationDefaults;
import no.novari.kafka.consuming.*;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RoleConsumerConfiguration {

    private final RoleService roleService;
    private final KafkaConsumerConfigurationDefaults kafkaConsumerConfigurationDefaults;

    @Bean
    public ConcurrentMessageListenerContainer<String, Role> roleConsumer(
            ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService
    ) {
        ParameterizedListenerContainerFactory<Role> recordListenerFactory =
                parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                        Role.class,
                        this::processRecord,
                        kafkaConsumerConfigurationDefaults.continueFromPreviousListenerConfiguration(),
                        kafkaConsumerConfigurationDefaults.defaultErrorHandler()
                );

        EntityTopicNameParameters topicParams =
                kafkaConsumerConfigurationDefaults.defaultEntityTopic("role");

        return recordListenerFactory.createContainer(topicParams);
    }

    private void processRecord(ConsumerRecord<String, Role> record) {
        Role role = record.value();

        if (role.getRoleStatus() == null) {
            log.warn("Not saving because role has a status null. roleId={}, resourceId={}", role.getRoleId(), role.getResourceId());
            return;
        }

        log.debug("Processing role event. offset={}, roleId={}, name={}, status={}, resourceId={}",
                record.offset(), role.getRoleId(), role.getRoleName(), role.getRoleStatus(), role.getResourceId());

        roleService.save(role);

        log.debug("Processed role event. roleId={}, name={}, status={}, resourceId={}",
                role.getRoleId(), role.getRoleName(), role.getRoleStatus(), role.getResourceId());
    }
}
