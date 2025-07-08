package no.fintlabs.membership;

import no.fintlabs.member.Member;
import no.fintlabs.member.MemberRepository;
import no.fintlabs.role.Role;
import no.fintlabs.role.RoleRepository;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembershipEntityProducerService;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembershipPublishingComponent;
import no.fintlabs.roleCatalogRole.RoleCatalogPublishingComponent;
import no.fintlabs.roleCatalogRole.RoleCatalogRoleEntityProducerService;
import no.fintlabs.securityconfig.FintKontrollSecurityConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest
public class MembershipKafkaConsumerIntegrationTest {
    private static final String applicationId = "fint-kontroll-role-cat";
    private static final String topicOrgId = "testorgmembership";
    private static final String topicDomainContext = "testdomainmembership";

    @MockBean
    private RoleCatalogMembershipEntityProducerService roleCatalogMembershipEntityProducerService;

    @MockBean
    private RoleCatalogRoleEntityProducerService roleCatalogRoleEntityProducerService;

    @MockBean
    private RoleCatalogPublishingComponent roleCatalogPublishingComponent;

    @MockBean
    private RoleCatalogMembershipPublishingComponent roleCatalogMembershipPublishingComponent;

    @MockBean
    private FintKontrollSecurityConfig fintKontrollSecurityConfig;

    @MockBean
    private MembershipRepository membershipRepositoryMock;

    @MockBean
    private RoleRepository roleRepositoryMock;

    @MockBean
    private MemberRepository memberRepositoryMock;

    @Container
    public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.9.1"));


    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> kafka.getBootstrapServers());
        registry.add("fint.kafka.topic.org-id", () -> topicOrgId);
        registry.add("fint.kafka.topic.domain-context", () -> topicDomainContext);
        registry.add("fint.kafka.application-id", () -> applicationId);
        registry.add("fint.cache.defaultCacheEntryTimeToLiveMillis", () -> 518400000);
        registry.add("fint.cache.defaultCacheHeapSize", () -> 1000000);
    }

    @Test
    public void shouldConsumeMembershipsAndSave() {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse("2024-07-31T01:00:00.000+00:00", DateTimeFormatter.ISO_ZONED_DATE_TIME);
        Date date = Date.from(zonedDateTime.toInstant());

        KafkaMembership kafkaMembership = KafkaMembership.builder()
                .roleId(1775L)
                .memberId(976L)
                .memberStatus("INACTIVE")
                .memberStatusChanged(date)
                .build();

        Role role = Role.builder()
                .id(1775L)
                .resourceId("http://test.no")
                .build();

        Member member = Member.builder().id(976L).build();

        when(roleRepositoryMock.findById(anyLong())).thenReturn(Optional.of(role));
        when(memberRepositoryMock.findById(anyLong())).thenReturn(Optional.of(member));

        KafkaTemplate<String, KafkaMembership> kafkaTemplate = createKafkaTemplate();
        kafkaTemplate.send(topicOrgId + "." + topicDomainContext + ".entity.role-membership", "testKey", kafkaMembership);

        verify(roleRepositoryMock, timeout(5000)).findById(1775L);
        verify(memberRepositoryMock, timeout(5000)).findById(976L);
        verify(membershipRepositoryMock, timeout(5000)).save(isA(Membership.class));
    }

    private KafkaTemplate<String, KafkaMembership> createKafkaTemplate() {
        Map<String, Object> producerConfig = new HashMap<>();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        ProducerFactory<String, KafkaMembership> producerFactory = new DefaultKafkaProducerFactory<>(producerConfig);
        return new KafkaTemplate<>(producerFactory);
    }
}
