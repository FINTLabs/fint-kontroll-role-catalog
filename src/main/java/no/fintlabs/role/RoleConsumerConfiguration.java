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
    @Bean
    public ConcurrentMessageListenerContainer<String, Role> roleConsumer(
            FintCache<String, Role> roleCache,
            EntityConsumerFactoryService entityConsumerFactoryService
    ){
        EntityTopicNameParameters entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("role")
                .build();

        return entityConsumerFactoryService.createFactory(
                        Role.class,
                        (ConsumerRecord<String,Role> consumerRecord) -> {
                            log.info(" Role message from Kafka with key: {} is saved to role cache"
                                    ,consumerRecord.value().getRoleId());
                            roleCache.put(consumerRecord.value().getRoleId(),consumerRecord.value());}
                )
                .createContainer(entityTopicNameParameters);
    }
}
