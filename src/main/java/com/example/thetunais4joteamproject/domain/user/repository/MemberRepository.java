package com.example.thetunais4joteamproject.domain.user.repository;

import com.example.thetunais4joteamproject.domain.user.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);
}