package no.fintlabs.role;

import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import no.fintlabs.model.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class RoleConsumerConfiguration {
    @Bean
    public ConcurrentMessageListenerContainer<String, Role> roleConsumer(
            RoleService roleService,
            EntityConsumerFactoryService entityConsumerFactoryService
    ){
        EntityTopicNameParameters entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("entitytorole")
                .build();

        ConcurrentMessageListenerContainer container = entityConsumerFactoryService.createFactory(
                        Role.class,
                        (ConsumerRecord<String,Role> consumerRecord)
                                -> roleService.save(consumerRecord.value()))
                .createContainer(entityTopicNameParameters);

        return container;
    }
}
