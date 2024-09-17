package no.fintlabs.member;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@Testcontainers
@SpringBootTest
public class MemberConsumerIntegrationTest {
    /*private static final String applicationId = "fint-kontroll-role-cat";
    private static final String topicOrgId = "testorg";
    private static final String topicDomainContext = "testdomain";

    @Container
    public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));

    @MockBean
    private OrgUnitRepository orgUnitRepositoryMock;

    @MockBean
    private SubOrgUnitRepository subOrgUnitRepositoryMock;

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> kafka.getBootstrapServers());
        registry.add("fint.kafka.topic.org-id", () -> topicOrgId);
        registry.add("fint.kafka.topic.domain-context", () -> topicDomainContext);
        registry.add("fint.kafka.application-id", () -> applicationId);
        registry.add("opa.jsonexport.filename", () -> "");
        registry.add("fint.kontroll.opa.url", () -> "");
    }

    @Test
    public void shouldConsumeMembersAndSave() {
        OrgUnitKafka orgUnitKafka = OrgUnitKafka.builder()
                .organisationUnitId("123")
                .allSubOrgUnitsRef(List.of())
                .build();

        KafkaTemplate<String, OrgUnitKafka> kafkaTemplate = createKafkaTemplate(kafka.getBootstrapServers());
        kafkaTemplate.send(topicOrgId + "." + topicDomainContext + ".entity.orgunit", "testKey", orgUnitKafka);

        verify(subOrgUnitRepositoryMock, timeout(5000)).saveAll(ArgumentMatchers.argThat(
                subOrgUnits -> !subOrgUnits.iterator().hasNext())
        );

        verify(orgUnitRepositoryMock, timeout(5000)).save(ArgumentMatchers.argThat(
                orgUnit -> orgUnit.getOrgUnitId().equals(orgUnitKafka.getOrganisationUnitId())
        ));
    }

    private KafkaTemplate<String, OrgUnitKafka> createKafkaTemplate(String bootstrapServers) {
        Map<String, Object> producerConfig = new HashMap<>();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        ProducerFactory<String, OrgUnitKafka> producerFactory = new DefaultKafkaProducerFactory<>(producerConfig);
        return new KafkaTemplate<>(producerFactory);
    }*/
}
