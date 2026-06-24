package com.example.thetunais4joteamproject.domain.user.controller;

import com.example.thetunais4joteamproject.domain.user.dto.CreateMemberRequest;
import com.example.thetunais4joteamproject.domain.user.dto.CreateMemberResponse;
import com.example.thetunais4joteamproject.domain.user.dto.GetMemberEmailCheckResponse;
import com.example.thetunais4joteamproject.domain.user.dto.LoginMemberRequest;
import com.example.thetunais4joteamproject.domain.user.dto.LoginMemberResponse;
import com.example.thetunais4joteamproject.domain.user.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/email-check")
    public ResponseEntity<GetMemberEmailCheckResponse> getEmailAvailability(
            @RequestParam String email
    ) {
        GetMemberEmailCheckResponse response = memberService.getEmailAvailability(email);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginMemberResponse> login(
            @Valid @RequestBody LoginMemberRequest request
    ) {
        LoginMemberResponse response = memberService.login(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<CreateMemberResponse> create(
            @Valid @RequestBody CreateMemberRequest request
    ) {
        CreateMemberResponse response = memberService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}