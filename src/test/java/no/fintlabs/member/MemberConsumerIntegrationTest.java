package no.fintlabs.member;

import no.fintlabs.DatabaseIntegrationTest;
import no.fintlabs.membership.MembershipRepository;
import no.fintlabs.role.RoleRepository;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembershipEntityProducerService;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembershipPublishingComponent;
import no.fintlabs.roleCatalogRole.RoleCatalogPublishingComponent;
import no.fintlabs.roleCatalogRole.RoleCatalogRoleEntityProducerService;
import no.fintlabs.securityconfig.FintKontrollSecurityConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest
@EmbeddedKafka(partitions = 1)
@TestPropertySource(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
public class MemberConsumerIntegrationTest extends DatabaseIntegrationTest {

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

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

    private static final String applicationId = "fint-kontroll-role-cat";
    private static final String topicOrgId = "testorg";
    private static final String topicDomainContext = "testdomain";

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("fint.kafka.topic.org-id", () -> topicOrgId);
        registry.add("fint.kafka.topic.domain-context", () -> topicDomainContext);
        registry.add("fint.kafka.application-id", () -> applicationId);
    }

    @Test
    public void testMembershipConsumer() {
        String kafkaTopic = topicOrgId + "." + topicDomainContext + ".entity.kontrolluser";

        // Define kafka message object
        KontrollUser kontrollUser = KontrollUser.builder()
                .id(1L)
                .build();

        // Send message to Kafka
        KafkaTemplate<String, KontrollUser> kafkaTemplate = createKafkaProducerForTest();
        kafkaTemplate.setDefaultTopic(kafkaTopic);
        kafkaTemplate.sendDefault(kontrollUser.getId().toString(), kontrollUser);

        // Verify that the message was consumed and the member was saved
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<Member> dbMember = memberRepository.findById(kontrollUser.getId());
                    assertTrue(dbMember.isPresent(), "Membership should be present in the database");
                });
    }

    @NotNull
    private KafkaTemplate<String, KontrollUser> createKafkaProducerForTest() {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        producerProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        DefaultKafkaProducerFactory<String, KontrollUser> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        return new KafkaTemplate<>(producerFactory, true);
    }
}
