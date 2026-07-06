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

    public List<RoleCatalogMembership> publishChangedCatalogMemberships(List<RoleCatalogMembership> allCatalogMemberships) {
        return allCatalogMemberships
                .stream()
                .filter(catalogMembership -> roleCatalogMembershipCache
                        .getOptional(catalogMembership.getId())
                        .map(publishedCatalogMembership -> !catalogMembership.equals(publishedCatalogMembership))
                        .orElse(true)
                )
                .peek(this::publishChangedCatalogMemberships)
                .toList();
    }

    public void publishChangedCatalogMemberships(RoleCatalogMembership roleCatalogMembership) {
        String key = roleCatalogMembership.getId();
        log.debug("Publishing role catalog membership. key={}", key);
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
        log.info("Publishing role catalog membership tombstone. key={}", membershipId);
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
