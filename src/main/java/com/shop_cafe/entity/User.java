package com.shop_cafe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String picture;

    private String role = "ROLE_USER";
    @Column(unique = true)
    private String socialId; // 카카오 또는 구글의 고유 ID를 저장

    public User(String name, String email, String picture, String socialId) {
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.socialId = socialId;
    }

    public User update(String name, String picture) {
        this.name = name;
        this.picture = picture;
        return this;
    }
}
