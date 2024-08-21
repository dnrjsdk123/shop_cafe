package com.shop_cafe.entity;

import com.shop_cafe.constant.Role;
import com.shop_cafe.dto.MemberFormDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.tags.form.PasswordInputTag;

@Entity
@Table(name = "member")
@Getter
@Setter
@ToString
public class Member extends BaseEntity {
    //기본키 1개씩 자동증가
    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    //알아서 생성
    private String name;
    //이메일 고유
    @Column(unique = true)
    private String email;
    //폰번호 고유
    @Column(unique = true)
    private String phone;
    //알아서 생성
    private String password;
    //알아서 설정
    private String address;
    //Enum -> 컴 : 숫지 우리 : 문자
    //데이터베이스 문자그대로 -> USER, ADMIN ,GUEST
    @Enumerated(EnumType.STRING)
    private Role role;

    public static Member createMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder) {
        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        member.setPhone(memberFormDto.getPhone());
        member.setAddress(memberFormDto.getAddress());
        String password = passwordEncoder.encode(memberFormDto.getPassword());
        member.setPassword(password);
        member.setRole(Role.ADMIN);
        return member;
    }
}
