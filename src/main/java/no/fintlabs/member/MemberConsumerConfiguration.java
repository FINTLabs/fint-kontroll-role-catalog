package no.fintlabs.member;

import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.member.MemberService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import no.fintlabs.model.Member;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class MemberConsumerConfiguration {
    @Bean
    public ConcurrentMessageListenerContainer<String, Member> memberConsumer(
            MemberService memberService,
            EntityConsumerFactoryService entityConsumerFactoryService
    ){
        EntityTopicNameParameters entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("member")
                .build();

        ConcurrentMessageListenerContainer container = entityConsumerFactoryService.createFactory(
                        Member.class,
                        (ConsumerRecord<String,Member> consumerRecord)
                                -> memberService.save(consumerRecord.value()))
                .createContainer(entityTopicNameParameters);

        return container;
    }
}