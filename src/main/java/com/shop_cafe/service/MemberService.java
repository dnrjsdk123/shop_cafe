package com.shop_cafe.service;

import com.shop_cafe.entity.Member;
import com.shop_cafe.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository; //자동 주입됨
    public Member saveMember(Member member) {
        validateDuplicateMember(member);
        return memberRepository.save(member); // 데이터베이스에 저장을 하라는 명령
    }

    private void validateDuplicateMember(Member member) {
        if (member.getId() == null) { // 신규 회원 가입 시에만 중복 검사
            Member findMemberByEmail = memberRepository.findByEmail(member.getEmail());
            if (findMemberByEmail != null) {
                throw new IllegalStateException("이미 가입된 회원입니다."); // 예외 발생
            }

            Member findMemberByPhone = memberRepository.findByPhone(member.getPhone());
            if (findMemberByPhone != null) {
                throw new IllegalStateException("이미 가입된 전화번호입니다."); // 예외 발생
            }
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email);

        if(member == null){
            throw new UsernameNotFoundException(email);
        }
        //빌더패턴
        return User.builder().username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }

}
