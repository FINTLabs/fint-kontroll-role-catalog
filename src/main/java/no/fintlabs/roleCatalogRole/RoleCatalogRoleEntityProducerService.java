package no.fintlabs.roleCatalogRole;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.novari.kafka.producing.ParameterizedProducerRecord;
import no.novari.kafka.producing.ParameterizedTemplate;
import no.novari.kafka.producing.ParameterizedTemplateFactory;
import no.novari.kafka.topic.EntityTopicService;
import no.novari.kafka.topic.configuration.EntityCleanupFrequency;
import no.novari.kafka.topic.configuration.EntityTopicConfiguration;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class RoleCatalogRoleEntityProducerService {
    private final ParameterizedTemplate<RoleCatalogRole> parameterizedTemplate;
    private final EntityTopicNameParameters entityTopicNameParameters;
    private final FintCache<String, Integer> roleCatalogRoleCache;

    public RoleCatalogRoleEntityProducerService(
            ParameterizedTemplateFactory parameterizedTemplateFactory,
            EntityTopicService entityTopicService,
            FintCache<String, Integer> roleCatalogRoleCache
    ) {
        this.parameterizedTemplate = parameterizedTemplateFactory.createTemplate(RoleCatalogRole.class);
        this.roleCatalogRoleCache = roleCatalogRoleCache;
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build())
                .resourceName("role-catalog-role")
                .build();
        entityTopicService.createOrModifyTopic(entityTopicNameParameters, EntityTopicConfiguration.stepBuilder()
                .partitions(1)
                .lastValueRetainedForever()
                .nullValueRetentionTime(Duration.ofDays(7))
                .cleanupFrequency(EntityCleanupFrequency.NORMAL)
                .build()
        );
    }


    public List<RoleCatalogRole> publishChangedCatalogRoles(List<RoleCatalogRole> catalogRoles) {
        return catalogRoles
                .stream()
                .filter(catalogRole -> roleCatalogRoleCache
                        .getOptional(catalogRole.getRoleId())
                        .map(publishedCatalogRoleHash -> !(catalogRole.hashCode()==(publishedCatalogRoleHash)))
                        .orElse(true)
                )
                .peek(this::publishCatalogRole)
                .toList();
    }

    public void publishCatalogRole(RoleCatalogRole roleCatalogRole) {
        String key = roleCatalogRole.getRoleId();
        log.debug("Publishing role catalog role. key={}", key);
        parameterizedTemplate.send(
                ParameterizedProducerRecord.<RoleCatalogRole>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(roleCatalogRole)
                        .build()
        );
        roleCatalogRoleCache.put(key, roleCatalogRole.hashCode());
    }
}
