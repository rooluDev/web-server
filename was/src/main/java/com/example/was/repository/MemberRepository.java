package com.example.was.repository;

import com.example.was.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,String> {

    Optional<Member> findByIdAndPassword(String id, String password);
}
