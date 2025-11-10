package com.example.was.controller;

import com.example.was.entity.Member;
import com.example.was.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<Member> login(@ModelAttribute Member member) {

        Member result = memberService.login(member.getId(), member.getPassword());
        if(result != null){
            return  ResponseEntity.ok(result);
        }

        return  ResponseEntity.status(401).build();
    }

    @GetMapping("/members")
    public List<Member> findAll() {
        return memberService.findAll();
    }

    @PutMapping("/member")
    public Member update(@RequestBody Member member) {
        return memberService.update(member.getId(), member);
    }

    @DeleteMapping("/member/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        memberService.delete(id);
        return ResponseEntity.ok("Deleted");
    }
}
