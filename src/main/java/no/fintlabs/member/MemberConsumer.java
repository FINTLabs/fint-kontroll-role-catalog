package no.fintlabs.member;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MemberConsumer {

    private final FintCache<Long, Member> memberCache;

    private final MemberRepository memberRepository;

    public MemberConsumer(MemberRepository memberRepository, FintCache<Long, Member> memberCache) {
        this.memberCache = memberCache;
        this.memberRepository = memberRepository;
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, KontrollUser> memberConsumerConfiguration(
            EntityConsumerFactoryService entityConsumerFactoryService
    ) {
        EntityTopicNameParameters kontrolluser = EntityTopicNameParameters
                .builder()
                .resource("kontrolluser")
                .build();

        return entityConsumerFactoryService
                .createFactory(KontrollUser.class, this::process)
                .createContainer(kontrolluser);
    }

    void process(ConsumerRecord<String, KontrollUser> consumerRecord) {
        KontrollUser kontrollUser = consumerRecord.value();
        log.info("Processing member: {}, username: {}, status: {}, identityProviderUserObjectId: {}", kontrollUser.getId(), kontrollUser.getUserName(), kontrollUser.getStatus(),
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
            updateMemberInCache(convertedMember, "Member found in cache, but not equal, updating member: {}");
        } else {
            log.debug("Member in cache is up-to-date: {}", cachedMember.getId());
        }
    }

    private void handleMember(Member member) {
        updateMemberInCache(member, "Member not found in cache, saving member: {}");
    }

    void updateMemberInCache(Member member, String logMessage) {
        log.info(logMessage, member.getId());

        memberRepository.findById(member.getId())
                .ifPresentOrElse(
                        existingMember -> updateMember(existingMember, member),
                        () -> saveNewMember(member)
                );
    }

    private void updateMember(Member member, Member updatedMember) {
        if (!member.equals(updatedMember)) {
            Member savedmember = memberRepository.save(updatedMember);
            memberCache.put(savedmember.getId(), savedmember);
        }
    }

    private void saveNewMember(Member member) {
        Member savedmember = memberRepository.save(member);
        memberCache.put(savedmember.getId(), savedmember);
    }
}
