package no.fintlabs.membership;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityManager;
import no.fintlabs.DatabaseIntegrationTest;
import no.fintlabs.member.Member;
import no.fintlabs.member.MemberConsumer;
import no.fintlabs.member.MemberRepository;
import no.fintlabs.role.Role;
import no.fintlabs.role.RoleConsumerConfiguration;
import no.fintlabs.role.RoleRepository;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembershipEntityProducerService;
import no.fintlabs.roleCatalogRole.RoleCatalogPublishingComponent;
import no.fintlabs.roleCatalogRole.RoleCatalogRoleEntityProducerService;
import no.fintlabs.securityconfig.FintKontrollSecurityConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.condition.EmbeddedKafkaCondition;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest
@EmbeddedKafka(partitions = 1)
@TestPropertySource(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
public class MembershipConsumerIntegrationTest extends DatabaseIntegrationTest {

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
    private MemberConsumer memberConsumer;

    @MockBean
    private RoleConsumerConfiguration roleConsumerConfiguration;

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
        String kafkaTopic = topicOrgId + "." + topicDomainContext + ".entity.role-membership";

        // Define existing role and member
        Role role = Role.builder()
                .roleId("123")
                .resourceId("http://test.no")
                .build();

        role = roleRepository.save(role);

        Member member = Member.builder().id(976L).build();
        member = memberRepository.save(member);

        ZonedDateTime zonedDateTime = ZonedDateTime.parse("2024-07-31T01:00:00.000+00:00", DateTimeFormatter.ISO_ZONED_DATE_TIME);
        Date date = Date.from(zonedDateTime.toInstant());

        // Define kafka message object
        KafkaMembership kafkaMembership = KafkaMembership.builder()
                .roleId(role.getId())
                .memberId(member.getId())
                .memberStatus("INACTIVE")
                .memberStatusChanged(date)
                .build();

        // Send message to Kafka
        KafkaTemplate<String, KafkaMembership> kafkaTemplate = createKafkaProducerForTest();
        kafkaTemplate.setDefaultTopic(kafkaTopic);
        kafkaTemplate.sendDefault(kafkaMembership.getRoleId().toString(), kafkaMembership);

        MembershipId membershipId = new MembershipId();
        membershipId.setRoleId(role.getId());
        membershipId.setMemberId(member.getId());

        // Verify that the message was consumed and the membership was saved
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<Membership> optionalMembership = membershipRepository.findById(membershipId);
                    assertTrue(optionalMembership.isPresent(), "Membership should be present in the database");
                    Membership membership = optionalMembership.get();
                    assertEquals("INACTIVE", membership.getMembershipStatus(), "Membership status should be INACTIVE");
                    assertThat(membership.getRole().getMemberships().size()).isEqualTo(1);
                });

        kafkaMembership.setMemberStatus("ACTIVE");
        kafkaTemplate.sendDefault(kafkaMembership.getRoleId().toString(), kafkaMembership);

        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<Membership> optionalMembership = membershipRepository.findById(membershipId);
                    assertTrue(optionalMembership.isPresent(), "Membership should be present in the database");
                    Membership membership = optionalMembership.get();
                    assertEquals("ACTIVE", membership.getMembershipStatus(), "Membership status should be ACTIVE");
                    assertThat(membership.getRole().getMemberships().size()).isEqualTo(1);
                });

    }

    @NotNull
    private KafkaTemplate<String, KafkaMembership> createKafkaProducerForTest() {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        producerProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        DefaultKafkaProducerFactory<String, KafkaMembership> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        return new KafkaTemplate<>(producerFactory, true);
    }
}
