package no.fintlabs.roleCatalogMembership;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import no.fintlabs.roleCatalogRole.RoleCatalogRole;
import org.springframework.stereotype.Service;

import java.util.Optional;

//import jakarta.annotation.PostConstruct;

@Service
@Slf4j
public class RoleCatalogMembershipEntityProducerService {
    private final EntityProducer<RoleCatalogMembership> entityProducer;
    private final EntityTopicNameParameters entityTopicNameParameters;
    private final EntityTopicService entityTopicService;
    private final FintCache<String, RoleCatalogMembership> roleCatalogMembershipCache;
    public RoleCatalogMembershipEntityProducerService(
            EntityProducerFactory entityProducerFactory,
            EntityTopicService entityTopicService,
            FintCache<String, RoleCatalogMembership> roleCatalogMembershipCache) {
        entityProducer = entityProducerFactory.createProducer(RoleCatalogMembership.class);
        this.entityTopicService = entityTopicService;
        this.roleCatalogMembershipCache = roleCatalogMembershipCache;
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

    public void publish(RoleCatalogMembership roleCatalogMembership) {
        String key = roleCatalogMembership.getId();
        Optional<RoleCatalogMembership> roleCatalogMembershipOptional = roleCatalogMembershipCache.getOptional(key);

        if (roleCatalogMembershipOptional.isEmpty() || !roleCatalogMembership.equals(roleCatalogMembershipOptional)) {
            log.info("Publish role-catalog-membership : " + key);
            entityProducer.send(
                    EntityProducerRecord.<RoleCatalogMembership>builder()
                            .topicNameParameters(entityTopicNameParameters)
                            .key(key)
                            .value(roleCatalogMembership)
                            .build()
            );
            roleCatalogMembershipCache.put(key, roleCatalogMembership);
        }
        else {
            log.info("role-catalog-membership : " + key +" already published");
        }
    }
}
