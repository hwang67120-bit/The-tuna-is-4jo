package com.example.thetunais4joteamproject.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;

import com.example.thetunais4joteamproject.domain.user.dto.LoginMemberRequest;
import com.example.thetunais4joteamproject.domain.user.dto.LoginMemberResponse;
import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.domain.user.entity.MemberRole;
import com.example.thetunais4joteamproject.domain.user.repository.MemberRepository;
import com.example.thetunais4joteamproject.global.util.JwtProvider;
import com.example.thetunais4joteamproject.global.util.PasswordEncryptor;

import java.util.Base64;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MemberServiceLoginJwtTest {

	private static final long MEMBER_ID = 1L;
	private static final String EMAIL = "jwtuser@test.com";
	private static final String NAME = "JWT User";
	private static final String PHONE_NUMBER = "01012345678";
	private static final String PASSWORD = "Test!1234";
	private static final long ACCESS_TOKEN_EXPIRATION = 3_600_000L;

	@Mock
	private MemberRepository memberRepository;

	private PasswordEncryptor passwordEncryptor;
	private JwtProvider jwtProvider;
	private MemberService memberService;

	@BeforeEach
	void setUp() {
		passwordEncryptor = new PasswordEncryptor();
		jwtProvider = new JwtProvider(createJwtSecret(), ACCESS_TOKEN_EXPIRATION);
		memberService = new MemberService(memberRepository, passwordEncryptor, jwtProvider);
	}

	@Test
	@DisplayName("로그인 성공 시 JWT가 발급되고 발급된 토큰으로 인증 정보를 조회할 수 있다.")
	void loginSuccess_CreatesValidJwtToken() {
		// given
		String encryptedPassword = passwordEncryptor.encrypt(PASSWORD);
		Member member = Member.create(EMAIL, encryptedPassword, NAME, PHONE_NUMBER, MemberRole.USER);
		ReflectionTestUtils.setField(member, "id", MEMBER_ID);

		given(memberRepository.findByEmail(EMAIL)).willReturn(Optional.of(member));
		LoginMemberRequest request = new LoginMemberRequest(EMAIL, PASSWORD);

		// when
		LoginMemberResponse response = memberService.login(request);

		// then
		assertThat(response.memberId()).isEqualTo(MEMBER_ID);
		assertThat(response.email()).isEqualTo(EMAIL);
		assertThat(response.name()).isEqualTo(NAME);
		assertThat(response.phoneNumber()).isEqualTo(PHONE_NUMBER);
		assertThat(response.role()).isEqualTo(MemberRole.USER);
		assertThat(response.accessToken()).isNotBlank();
		assertThatCode(() -> jwtProvider.validateToken(response.accessToken())).doesNotThrowAnyException();
		assertThat(jwtProvider.getMemberId(response.accessToken())).isEqualTo(MEMBER_ID);
		assertThat(jwtProvider.getRole(response.accessToken())).isEqualTo(MemberRole.USER);

		Authentication authentication = jwtProvider.getAuthentication(response.accessToken());
		assertThat(authentication.getPrincipal()).isEqualTo(MEMBER_ID);
		assertThat(authentication.getAuthorities())
			.extracting("authority")
			.containsExactly("ROLE_USER");
	}

	private String createJwtSecret() {
		byte[] secret = "jwt-test-secret-key-must-be-long-enough-for-hmac-signing-1234".getBytes();

		return Base64.getEncoder().encodeToString(secret);
	}
}
