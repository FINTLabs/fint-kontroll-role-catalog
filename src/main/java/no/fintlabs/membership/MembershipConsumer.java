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

import java.util.Optional;

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

        Optional<Role> roleOptional = roleRepository.findById(kafkaMembership.getRoleId());
        Optional<Member> memberOptional = memberRepository.findById(kafkaMembership.getMemberId());

        if (roleOptional.isEmpty() || memberOptional.isEmpty()) {
            //TODO: What to do if role or member is not found? Retry?
            log.error("Role or member not found for membership: {} {}", kafkaMembership.getRoleId(), kafkaMembership.getMemberId());
            return;
        }

        Role role = roleOptional.get();
        Member member = memberOptional.get();

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
