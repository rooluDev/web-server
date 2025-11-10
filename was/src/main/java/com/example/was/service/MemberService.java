package com.example.was.service;

import com.example.was.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.was.entity.Member;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Member login(String id, String password) {
        return memberRepository.findByIdAndPassword(id, password)
                .orElse(null);
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member update(String id, Member newData) {
        return memberRepository.findById(id).map(m -> {
            m.setEmail(newData.getEmail());
            m.setPassword(newData.getPassword());
            return memberRepository.save(m);
        }).orElseThrow();
    }

    public void delete(String id) {
        memberRepository.deleteById(id);
    }
}
