package com.shop_cafe.config;

import com.shop_cafe.dto.SessionUser;
import com.shop_cafe.entity.Member;
import com.shop_cafe.entity.User;
import com.shop_cafe.repository.MemberRepository;
import com.shop_cafe.repository.UserRepository;
import com.shop_cafe.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HttpSession httpSession;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    private int oauthNum = 1;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(oAuth2UserRequest);

        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = oAuth2UserRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // 카카오와 구글의 고유 ID를 가져옴
        String socialId;
        if ("kakao".equals(registrationId)) {
            socialId = attributes.getAttributes().get("id").toString(); // 카카오는 'id' 필드
        } else if ("google".equals(registrationId)) {
            socialId = attributes.getAttributes().get("sub").toString(); // 구글은 'sub' 필드
        } else {
            throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        }

        // 디버깅을 위해 로그 추가
        System.out.println("OAuthAttributes: " + attributes);

        User user = saveOrUpdate(attributes, socialId);

        httpSession.setAttribute("user", new SessionUser(user));

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes.getAttributes(), attributes.getNameAttributeKey());
    }

    private User saveOrUpdate(OAuthAttributes attributes, String socialId) {
        // User 조회 및 저장
        User user = userRepository.findByEmail(attributes.getEmail())
                .map(entity -> {
                    entity.update(attributes.getName(), attributes.getPicture());
                    entity.setSocialId(socialId); // 소셜 ID 업데이트
                    return userRepository.save(entity); // User 업데이트
                })
                .orElseGet(() -> {
                    User newUser = attributes.toEntity();
                    newUser.setSocialId(socialId);
                    return userRepository.save(newUser); // 새로운 User 저장
                });

        // Member 조회
        Member existingMember = memberRepository.findByEmail(attributes.getEmail());

        if (existingMember == null) {
            // 기존 Member가 없으면 새로운 Member 생성 및 저장
            Member member = new Member();
            member.setName(user.getName());
            member.setEmail(user.getEmail());
            member.setPassword("Oauth2"); // 기본 비밀번호 설정
            member.setAddress("Oauth2"); // 기본 주소 설정
            member.setPhone("Oauth" + oauthNum); // 기본 전화번호 설정
            member.setSocialId(socialId);

            // 디버깅을 위해 로그 추가
            System.out.println("Attempting to save new member: " + member);
            memberService.saveMember(member);
            System.out.println("회원가입 완료: " + member);
            oauthNum++;
        } else {
            // 기존 Member의 소셜 ID 업데이트
            existingMember.setSocialId(socialId);
            memberService.saveMember(existingMember);
        }

        return user;
    }


}

