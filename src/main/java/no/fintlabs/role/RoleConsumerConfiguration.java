package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Slf4j
@Configuration
public class RoleConsumerConfiguration {
    private final RoleService roleService;

    public RoleConsumerConfiguration(RoleService roleService) {
        this.roleService = roleService;
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, Role> roleConsumer(
            EntityConsumerFactoryService entityConsumerFactoryService
    ){
        EntityTopicNameParameters entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("role")
                .build();

        return entityConsumerFactoryService.createFactory(
                        Role.class,
                        (ConsumerRecord<String,Role> consumerRecord) -> {
                            log.info("Role consumed from Kafka with roleid: {}, members: {}, resourceid: {}"
                                    ,consumerRecord.value().getRoleId(), consumerRecord.value().getMembers(), consumerRecord.value().getResourceId());

                            roleService.save(consumerRecord.value());

                            log.info("Role saved to database with roleid: {}, members: {}, resourceid: {}"
                                    ,consumerRecord.value().getRoleId(), consumerRecord.value().getMembers(), consumerRecord.value().getResourceId());
                        }
                )
                .createContainer(entityTopicNameParameters);
    }
}
