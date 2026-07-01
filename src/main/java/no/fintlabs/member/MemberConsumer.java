package no.fintlabs.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.KafkaConsumerConfigurationDefaults;
import no.fintlabs.cache.FintCache;
import no.novari.kafka.consuming.*;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import no.fintlabs.membership.MembershipService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberConsumer {

    private final FintCache<Long, Member> memberCache;
    private final MemberRepository memberRepository;
    private final MembershipService membershipService;
    private final KafkaConsumerConfigurationDefaults kafkaConsumerConfigurationDefaults;

    @Bean
    public ConcurrentMessageListenerContainer<String, KontrollUser> memberConsumerConfiguration(
            ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService
    ) {
        ParameterizedListenerContainerFactory<KontrollUser> recordListenerFactory =
                parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                        KontrollUser.class,
                        this::process,
                        kafkaConsumerConfigurationDefaults.seekToBeginningListenerConfiguration(),
                        kafkaConsumerConfigurationDefaults.defaultErrorHandler()
                );
        EntityTopicNameParameters entityTopicNameParameters =
                kafkaConsumerConfigurationDefaults.defaultEntityTopic("kontrolluser");

        return recordListenerFactory.createContainer(entityTopicNameParameters);
    }


    void process(ConsumerRecord<String, KontrollUser> consumerRecord) {
        KontrollUser kontrollUser = consumerRecord.value();
        log.debug("Processing member event. memberId={}, username={}, status={}, identityProviderUserObjectId={}", kontrollUser.getId(), kontrollUser.getUserName(), kontrollUser.getStatus(),
                 kontrollUser.getIdentityProviderUserObjectId());

        Member convertedMember = MemberMapper.fromKontrollUser(kontrollUser);

        memberCache.getOptional(convertedMember.getId())
                .ifPresentOrElse(
                        cachedMember -> handleCachedMember(cachedMember, convertedMember),
                        () -> handleMember(convertedMember)
                );
    }

    private void handleCachedMember(Member cachedMember, Member convertedMember) {
        if (!cachedMember.equals(convertedMember)) {
            updateMemberInCache(convertedMember, "Member changed; saving update. memberId={}");
        } else {
            log.debug("Member in cache is up-to-date: {}", cachedMember.getId());
        }
    }

    private void handleMember(Member member) {
        updateMemberInCache(member, "New member; saving. memberId={}");
    }

    void updateMemberInCache(Member member, String logMessage) {
        log.debug(logMessage, member.getId());

        memberRepository.findById(member.getId())
                .ifPresentOrElse(
                        existingMember -> updateMember(existingMember, member),
                        () -> saveNewMember(member)
                );
    }

    private void updateMember(Member existingMember, Member incomingMember) {
        if (!existingMember.equals(incomingMember)) {
            if("DELETED".equals(incomingMember.getStatus())) {
                log.info("Deleting member from catalog. memberId={}", incomingMember.getId());
                membershipService.removeAllMembershipsForUser(existingMember);
                memberRepository.deleteById(existingMember.getId());
                memberCache.remove(existingMember.getId());
                return;
            }
            Member savedmember = memberRepository.save(incomingMember);
            memberCache.put(savedmember.getId(), savedmember);
        }
    }

    private void saveNewMember(Member member) {
        if(!"DELETED".equals(member.getStatus())) {
            Member savedmember = memberRepository.save(member);
            memberCache.put(savedmember.getId(), savedmember);
        }
    }
}
