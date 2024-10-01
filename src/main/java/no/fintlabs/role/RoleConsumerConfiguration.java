package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.member.Member;
import no.fintlabs.membership.Membership;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.List;
import java.util.stream.Collectors;

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
    ) {
        EntityTopicNameParameters entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("role")
                .build();

        return entityConsumerFactoryService.createFactory(
                        Role.class,
                        (ConsumerRecord<String, Role> consumerRecord) -> {

                            log.info("Role consumed from Kafka with offset {}, roleid: {}, members: {}, resourceid: {}",
                                    consumerRecord.offset() , consumerRecord.value().getRoleId(), memberIds.size(), consumerRecord.value().getResourceId());

                            Role savedRole = roleService.save(consumerRecord.value());

                            log.info("Role saved to database with roleid: {},  resourceid: {}"
                                    , savedRole.getRoleId(),  savedRole.getResourceId());
                        }
                )
                .createContainer(entityTopicNameParameters);
    }
}
