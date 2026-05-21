package no.fintlabs.roleCatalogMembership;

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
import java.util.Optional;


@Service
@Slf4j
public class RoleCatalogMembershipEntityProducerService {
    private final ParameterizedTemplate<RoleCatalogMembership> parameterizedTemplate;
    private final EntityTopicNameParameters entityTopicNameParameters;
    private final FintCache<String, RoleCatalogMembership> roleCatalogMembershipCache;

    public RoleCatalogMembershipEntityProducerService(
            ParameterizedTemplateFactory parameterizedTemplateFactory,
            EntityTopicService entityTopicService,
            FintCache<String, RoleCatalogMembership> roleCatalogMembershipCache
    ) {
        this.parameterizedTemplate = parameterizedTemplateFactory.createTemplate(RoleCatalogMembership.class);
        this.roleCatalogMembershipCache = roleCatalogMembershipCache;
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build())
                .resourceName("role-catalog-membership")
                .build();
        entityTopicService.createOrModifyTopic(entityTopicNameParameters,EntityTopicConfiguration.stepBuilder()
                .partitions(1)
                .lastValueRetainedForever()
                .nullValueRetentionTime(Duration.ofDays(7))
                .cleanupFrequency(EntityCleanupFrequency.NORMAL)
                .build()
        );
    }

    public void publish(RoleCatalogMembership roleCatalogMembership) {
        String key = roleCatalogMembership.getId();
        Optional<RoleCatalogMembership> roleCatalogMembershipOptional = roleCatalogMembershipCache.getOptional(key);

        if (roleCatalogMembershipOptional.isEmpty() || !roleCatalogMembership.equals(roleCatalogMembershipOptional.get())) {
            log.debug("Publish role-catalog-membership: {}", key);
            parameterizedTemplate.send(
                    ParameterizedProducerRecord.<RoleCatalogMembership>builder()
                            .topicNameParameters(entityTopicNameParameters)
                            .key(key)
                            .value(roleCatalogMembership)
                            .build()
            );
            roleCatalogMembershipCache.put(key, roleCatalogMembership);
        }
        else {
            log.info("role-catalog-membership: {} already published", key);
        }
    }

    public List<RoleCatalogMembership> publishChangedCatalogMemberships(List<RoleCatalogMembership> allCatalogMemberships) {
        return allCatalogMemberships
                .stream()
                .filter(catalogMembership -> roleCatalogMembershipCache
                        .getOptional(catalogMembership.getId())
                        .map(publishedCatalogMembership -> !catalogMembership.equals(publishedCatalogMembership))
                        .orElse(true)
                )
                .peek(catalogMembership -> log.debug("Publish role-catalog-membership: {}", catalogMembership.getId()))
                .peek(this::publishChangedCatalogMemberships)
                .toList();
    }

    private void publishChangedCatalogMemberships(RoleCatalogMembership roleCatalogMembership) {
        String key = roleCatalogMembership.getId();
        log.info("Publish role-catalog-membership: {}", key);
        parameterizedTemplate.send(
                ParameterizedProducerRecord.<RoleCatalogMembership>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(key)
                        .value(roleCatalogMembership)
                        .build()
        );
        roleCatalogMembershipCache.put(key, roleCatalogMembership);
    }

    public void publishTombstone(String membershipId) {
        log.info("Publish tombstone for role-catalog-membership: {}", membershipId);
        parameterizedTemplate.send(
                ParameterizedProducerRecord.<RoleCatalogMembership>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(membershipId)
                        .value(null)
                        .build()
        );
        roleCatalogMembershipCache.remove(membershipId);
    }
}
