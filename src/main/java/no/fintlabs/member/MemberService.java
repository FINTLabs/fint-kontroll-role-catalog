package no.fintlabs.member;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;
    public Member save (Member member) {
        Member savedMember = memberRepository.save(member);
        log.debug("saving member {}", member.getId());
        return savedMember;
    }
    public List<Member> saveAll(Set<Member> members) {
        List<Member> savedMembers = memberRepository.saveAllAndFlush(members);
        log.info("Saved {} members", savedMembers.size());
        return savedMembers;
    }
    public List<Member> getAllMembers() {
        return memberRepository.findAll().stream().collect(Collectors.toList());
    }

    public Member findMemberById(Long id) {
        return memberRepository.findById(id).orElse(new Member());
    }
//    public Mono<Member> findMemberByUserName(String userName) {
//        Member member = memberRepository.findByUserName(userName).orElse(new Member());
//        return Mono.just(member);
//    }
}
