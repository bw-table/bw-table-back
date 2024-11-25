package com.zero.bwtableback.member.servcie;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.dto.MemberDto;
import com.zero.bwtableback.member.dto.MemberPrivateDto;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach; // JUnit 5의 어노테이션
import org.junit.jupiter.api.DisplayName; // JUnit 5의 어노테이션
import org.junit.jupiter.api.Test; // JUnit 5의 어노테이션
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setId(1L);
        member.setEmail("test@example.com");
        member.setName("홍길동");
        member.setNickname("길동");
        member.setPhone("01012345678");
        member.setRole(Role.GUEST);
        member.setProfileImage("profile.jpg");
        member.setBusinessNumber(null);
    }

    @Test
    @DisplayName("회원 ID로 회원 정보 조회 성공")
    void testGetMemberById_Success() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // when
        MemberDto result = memberService.getMemberById(1L);
        System.out.println(result.getName());

        // then
        assertNotNull(result);
        assertEquals(member.getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 조회 시 예외 발생")
    public void testGetMemberById_UserNotFound() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            memberService.getMemberById(1L);
        });
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("본인 정보 조회 성공")
    public void testGetMyInfo_Success() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // when
        MemberPrivateDto result = memberService.getMyInfo(member.getId());

        // then
        assertNotNull(result);
        assertEquals(member.getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("회원 목록 조회 성공")
    public void testGetMembers_Success() {
        // given
        List<Member> members = new ArrayList<>();
        members.add(member);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Member> pageMembers = new PageImpl<>(members, pageable, members.size());

        when(memberRepository.findAll(pageable)).thenReturn(pageMembers);

        // when
        Page<MemberDto> result = memberService.getMembers(pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
}
