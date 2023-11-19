package no.fintlabs.roleCatalogMembership;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import org.springframework.stereotype.Service;

//import jakarta.annotation.PostConstruct;

@Service
@Slf4j
public class RoleCatalogMembershipEntityProducerService {
    private final EntityProducer<RoleCatalogMembership> entityProducer;
    private final EntityTopicNameParameters entityTopicNameParameters;
    private final EntityTopicService entityTopicService;

    public RoleCatalogMembershipEntityProducerService(
            EntityProducerFactory entityProducerFactory,
            EntityTopicService entityTopicService
    ) {
        entityProducer = entityProducerFactory.createProducer(RoleCatalogMembership.class);
        this.entityTopicService = entityTopicService;
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("role-catalog-membership")
                .build();
        entityTopicService.ensureTopic(entityTopicNameParameters, 0);
    }
//    @PostConstruct
//    public void init() {
//        entityTopicService.ensureTopic(entityTopicNameParameters, 0);
//    }

    public void publish(RoleCatalogMembership roleCatalogRole) {
        String key = roleCatalogRole.getId();
        log.info("Publish role-catalog-membership : " + key);
        entityProducer.send(
                EntityProducerRecord.<RoleCatalogMembership>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(roleCatalogRole)
                        .build()
        );
    }
}
