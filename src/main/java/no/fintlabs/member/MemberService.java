package no.fintlabs.member;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;
    public Member save (Member member) {
        Member savedMember = memberRepository.save(member);
        log.info("saving member {}", member.getId());
        return savedMember;
    }
    public Flux<Member> getAllMembers() {
        List<Member> allMembers  = memberRepository.findAll().stream().collect(Collectors.toList());
        return Flux.fromIterable(allMembers);
    }
    public Mono<Member> findMemberById(Long id) {
        Member member = memberRepository.findById(id).orElse(new Member());
        return Mono.just(member);
    }
//    public Mono<Member> findMemberByUserName(String userName) {
//        Member member = memberRepository.findByUserName(userName).orElse(new Member());
//        return Mono.just(member);
//    }
}
