package com.shop_cafe.repository;

import com.shop_cafe.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository <Member, Long>{
    Member findByEmail(String email);
    Member findByPhone(String phone);
}
