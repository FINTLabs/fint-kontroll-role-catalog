package no.fintlabs.member;


import no.fintlabs.model.Member;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
