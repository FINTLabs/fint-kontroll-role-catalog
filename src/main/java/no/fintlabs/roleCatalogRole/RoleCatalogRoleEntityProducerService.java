package no.fintlabs.roleCatalogRole;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import org.springframework.stereotype.Service;

import java.util.Optional;

//import jakarta.annotation.PostConstruct;

@Service
@Slf4j
public class RoleCatalogRoleEntityProducerService {
    private final EntityProducer<RoleCatalogRole> entityProducer;
    private final EntityTopicNameParameters entityTopicNameParameters;
    private final EntityTopicService entityTopicService;
    private final FintCache<String, RoleCatalogRole> roleCatalogRoleCache;

    public RoleCatalogRoleEntityProducerService(
            EntityProducerFactory entityProducerFactory,
            EntityTopicService entityTopicService,
            FintCache<String, RoleCatalogRole> roleCatalogRoleCache) {
        entityProducer = entityProducerFactory.createProducer(RoleCatalogRole.class);
        this.entityTopicService = entityTopicService;
        this.roleCatalogRoleCache = roleCatalogRoleCache;
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
        Optional<RoleCatalogRole> roleCatalogRoleOptional = roleCatalogRoleCache.getOptional(key);

        if (roleCatalogRoleOptional.isEmpty() && !roleCatalogRole.equals(roleCatalogRoleOptional)) {
            log.info("Publish role-catalog-role : " + key);
            entityProducer.send(
                    EntityProducerRecord.<RoleCatalogRole>builder()
                            .topicNameParameters(entityTopicNameParameters)
                            .key(key)
                            .value(roleCatalogRoleOptional.get())
                            .build()
            );
            roleCatalogRoleCache.put(key, roleCatalogRoleOptional.get());
        }
        else {
            log.info("role-catalog-role : " + key +" already published");
        }
    }
}
