package no.fintlabs.member;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.model.Member;
import no.fintlabs.repository.MemberRepository;
//import no.vigoiks.resourceserver.security.FintJwtEndRolePrincipal;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;

    public Member save(Member member) {
        return memberRepository.save(member);
    }

}
