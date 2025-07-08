package no.fintlabs.role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RoleConsumerConfiguration {

    private final RoleService roleService;

    @Bean
    public ConcurrentMessageListenerContainer<String, Role> roleConsumer(
            EntityConsumerFactoryService entityConsumerFactoryService
    ) {
        EntityTopicNameParameters topicParams = EntityTopicNameParameters.builder()
                .resource("role")
                .build();

        return entityConsumerFactoryService.createFactory(
                Role.class,
                this::processRecord
        ).createContainer(topicParams);
    }

    private void processRecord(ConsumerRecord<String, Role> record) {
        Role role = record.value();
        log.info("Consumed Role from Kafka. offset={}, roleId={}, name={}, status={}, resourceId={}",
                record.offset(), role.getRoleId(), role.getRoleName(), role.getRoleStatus(), role.getResourceId());

        roleService.save(role);

        log.info("Saved Role to DB. roleId={}, name={}, status={}, resourceId={}",
                role.getRoleId(), role.getRoleName(), role.getRoleStatus(), role.getResourceId());
    }
}
