package com.example.thetunais4joteamproject.domain.user.service;

import com.example.thetunais4joteamproject.domain.user.dto.CreateMemberRequest;
import com.example.thetunais4joteamproject.domain.user.dto.CreateMemberResponse;
import com.example.thetunais4joteamproject.domain.user.dto.GetMemberEmailCheckResponse;
import com.example.thetunais4joteamproject.domain.user.dto.LoginMemberRequest;
import com.example.thetunais4joteamproject.domain.user.dto.LoginMemberResponse;
import com.example.thetunais4joteamproject.domain.user.dto.LogoutMemberResponse;
import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.domain.user.repository.MemberRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;
import com.example.thetunais4joteamproject.global.util.JwtProvider;
import com.example.thetunais4joteamproject.global.util.PasswordEncryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncryptor passwordEncryptor;
    private final JwtProvider jwtProvider;

    public GetMemberEmailCheckResponse getEmailAvailability(String email) {
        boolean available = !memberRepository.existsByEmail(email);

        return GetMemberEmailCheckResponse.from(available);
    }

    public LoginMemberResponse login(LoginMemberRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> BusinessException.from(ErrorCode.UNAUTHORIZED));
        if (!passwordEncryptor.matches(request.password(), member.getPassword())) {
            throw BusinessException.from(ErrorCode.UNAUTHORIZED);
        }

        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getRole());

        return LoginMemberResponse.from(member, accessToken);
    }

    public LogoutMemberResponse logout(String authorizationHeader) {
        String accessToken = jwtProvider.extractToken(authorizationHeader);
        jwtProvider.validateToken(accessToken);

        return new LogoutMemberResponse(true);
    }

    /** 회원가입 **/
    @Transactional
    public CreateMemberResponse create(CreateMemberRequest request) {
        validateDuplicateEmail(request.email());

        String encryptedPassword = passwordEncryptor.encrypt(request.password());
        String normalizedPhoneNumber = normalizePhoneNumber(request.phoneNumber());
        Member savedMember = memberRepository.save(
                Member.create(request.email(), encryptedPassword, request.name(), normalizedPhoneNumber)
        );

        return CreateMemberResponse.from(savedMember);
    }

    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw BusinessException.from(ErrorCode.CONFLICT);
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.replace("-", "");
    }
}