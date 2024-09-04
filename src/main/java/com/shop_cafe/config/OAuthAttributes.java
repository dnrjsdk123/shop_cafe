package com.shop_cafe.config;

import com.shop_cafe.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;
    private String socialId; // 소셜 제공자의 고유 ID를 저장할 필드 추가

    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name,
                           String email, String picture, String socialId) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.socialId = socialId;
    }

    public OAuthAttributes() {
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName,
                                     Map<String, Object> attributes) {
        if(registrationId.equals("kakao")){
            return ofKakao(userNameAttributeName, attributes);
        }
        if(registrationId.equals("naver")){
            return ofNaver(userNameAttributeName, attributes);
        }
        if(registrationId.equals("google")){
            return ofGoogle(userNameAttributeName, attributes);
        }
        return null;
    }

    private static OAuthAttributes ofNaver(String userNameAttributeName,
                                           Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return new OAuthAttributes(
                attributes,
                userNameAttributeName,
                (String) response.get("name"),
                (String) response.get("email"),
                (String) response.get("profile_image"),
                (String) response.get("id") // 네이버의 고유 ID를 추가
        );
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName,
                                           Map<String, Object> attributes) {
        Map<String, Object> kakao_account = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakao_account.get("profile");

        return new OAuthAttributes(
                attributes,
                userNameAttributeName,
                (String) profile.get("nickname"),
                (String) kakao_account.get("email"),
                (String) profile.get("profile_image_url"),
                attributes.get("id").toString() // 카카오의 고유 ID를 추가
        );
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName,
                                            Map<String, Object> attributes) {

        return new OAuthAttributes(
                attributes,
                userNameAttributeName,
                (String) attributes.get("name"),
                (String) attributes.get("email"),
                (String) attributes.get("picture"),
                (String) attributes.get("sub") // 구글의 고유 ID를 추가
        );
    }

    public User toEntity() {
        return new User(name, email, picture, socialId); // 소셜 ID 포함하여 엔티티 생성
    }
}
