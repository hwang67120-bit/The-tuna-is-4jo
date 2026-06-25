package com.example.thetunais4joteamproject.domain.user.controller;

import com.example.thetunais4joteamproject.domain.user.dto.CreateMemberRequest;
import com.example.thetunais4joteamproject.domain.user.dto.CreateMemberResponse;
import com.example.thetunais4joteamproject.domain.user.dto.GetMemberEmailCheckResponse;
import com.example.thetunais4joteamproject.domain.user.dto.GetMemberInfoResponse;
import com.example.thetunais4joteamproject.domain.user.dto.LoginMemberRequest;
import com.example.thetunais4joteamproject.domain.user.dto.LoginMemberResponse;
import com.example.thetunais4joteamproject.domain.user.dto.LogoutMemberResponse;
import com.example.thetunais4joteamproject.domain.user.dto.UpdateMemberInfoRequest;
import com.example.thetunais4joteamproject.domain.user.dto.UpdateMemberInfoResponse;
import com.example.thetunais4joteamproject.domain.user.service.MemberService;
import com.example.thetunais4joteamproject.global.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    public ResponseEntity<ApiResponse<GetMemberEmailCheckResponse>> getEmailAvailability(
            @RequestParam String email
    ) {
        GetMemberEmailCheckResponse response = memberService.getEmailAvailability(email);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginMemberResponse>> login(
            @Valid @RequestBody LoginMemberRequest request
    ) {
        LoginMemberResponse response = memberService.login(request);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<LogoutMemberResponse>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        LogoutMemberResponse response = memberService.logout(authorizationHeader);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<GetMemberInfoResponse>> getInfo(
            @AuthenticationPrincipal Long memberId
    ) {
        GetMemberInfoResponse response = memberService.getInfo(memberId);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/info")
    public ResponseEntity<ApiResponse<UpdateMemberInfoResponse>> updateInfo(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody UpdateMemberInfoRequest request
    ) {
        UpdateMemberInfoResponse response = memberService.updateInfo(memberId, request);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<CreateMemberResponse>> create(
            @Valid @RequestBody CreateMemberRequest request
    ) {
        CreateMemberResponse response = memberService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }
}