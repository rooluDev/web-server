package com.example.was.controller;
import com.example.was.entity.Member;
import org.springframework.ui.Model;

import com.example.was.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/member/list")
    public String memberList(Model model) {

        List<Member> members = memberService.findAll();

        model.addAttribute("members", members);

        return "index";
    }
}
