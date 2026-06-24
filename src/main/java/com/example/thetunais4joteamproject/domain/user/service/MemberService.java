package com.example.thetunais4joteamproject.domain.user.service;

import com.example.thetunais4joteamproject.domain.user.dto.CreateMemberRequest;
import com.example.thetunais4joteamproject.domain.user.dto.CreateMemberResponse;
import com.example.thetunais4joteamproject.domain.user.dto.GetMemberEmailCheckResponse;
import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.domain.user.repository.MemberRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;
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

    public GetMemberEmailCheckResponse getEmailAvailability(String email) {
        boolean available = !memberRepository.existsByEmail(email);

        return GetMemberEmailCheckResponse.from(available);
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