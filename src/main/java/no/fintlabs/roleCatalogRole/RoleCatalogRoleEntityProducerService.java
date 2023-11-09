package no.fintlabs.roleCatalogRole;

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
public class RoleCatalogRoleEntityProducerService {
    private final EntityProducer<RoleCatalogRole> entityProducer;
    private final EntityTopicNameParameters entityTopicNameParameters;
    private final EntityTopicService entityTopicService;

    public RoleCatalogRoleEntityProducerService(
            EntityProducerFactory entityProducerFactory,
            EntityTopicService entityTopicService
    ) {
        entityProducer = entityProducerFactory.createProducer(RoleCatalogRole.class);
        this.entityTopicService = entityTopicService;
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("role-catalog-role")
                .build();
        entityTopicService.ensureTopic(entityTopicNameParameters, 0);
    }
//    @PostConstruct
//    public void init() {
//        entityTopicService.ensureTopic(entityTopicNameParameters, 0);
//    }

    public void publish(RoleCatalogRole roleCatalogRole) {
        String key = roleCatalogRole.getRoleId();
        log.info("Publish role-catalog-role : " + key);
        entityProducer.send(
                EntityProducerRecord.<RoleCatalogRole>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(roleCatalogRole)
                        .build()
        );
    }
}
