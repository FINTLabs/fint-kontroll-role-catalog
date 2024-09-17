package no.fintlabs.membership;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.member.Member;
import no.fintlabs.member.MemberRepository;
import no.fintlabs.role.Role;
import no.fintlabs.role.RoleRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MembershipConsumer {

    private final MembershipRepository membershipRepository;

    private final RoleRepository roleRepository;

    private final MemberRepository memberRepository;

    public MembershipConsumer(MembershipRepository membershipRepository, RoleRepository roleRepository, MemberRepository memberRepository) {
        this.membershipRepository = membershipRepository;
        this.roleRepository = roleRepository;
        this.memberRepository = memberRepository;
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, KafkaMembership> membershipConsumerConfiguration(
            EntityConsumerFactoryService entityConsumerFactoryService
    ) {
        EntityTopicNameParameters entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("role-membership")
                .build();

        return entityConsumerFactoryService
                .createFactory(KafkaMembership.class, this::process)
                .createContainer(entityTopicNameParameters);
    }

    private void process(ConsumerRecord<String, KafkaMembership> membershipRecord) {

        KafkaMembership kafkaMembership = membershipRecord.value();
        log.info("Processing membership: {}", kafkaMembership.getRoleId());

        Role role = roleRepository.getReferenceById(kafkaMembership.getRoleId());
        Member member = memberRepository.getReferenceById(kafkaMembership.getMemberId());

        if(role == null || member == null) {
            log.error("Role or member not found for membership: {} {}", kafkaMembership.getRoleId(), kafkaMembership.getMemberId());
            return;
        }

        Membership membership = new Membership();
        membership.setRole(role);
        membership.setMember(member);
        membership.setMembershipStatus(kafkaMembership.getMemberStatus() == null ? "ACTIVE" : kafkaMembership.getMemberStatus());
        membership.setMembershipStatusChanged(kafkaMembership.getMemberStatusChanged());

        MembershipId membershipId = new MembershipId();
        membershipId.setMemberId(member.getId());
        membershipId.setRoleId(role.getId());
        membership.setId(membershipId);

        membershipRepository.save(membership);
    }
}