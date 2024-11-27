package com.zero.bwtableback.member.controller;

import com.zero.bwtableback.member.dto.MemberDto;
import com.zero.bwtableback.member.service.MemberService;
import com.zero.bwtableback.security.MemberDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    private MemberDto memberDto;

    @BeforeEach
    void setUp() {
        memberDto = MemberDto.builder()
                .id(1L)
                .email("test@example.com")
                .name("홍길동")
                .build();
    }

    // TODO Member Controller 테스트 작성
//    @Test
//    @DisplayName("특정 회원 정보 조회 성공")
//    void testGetMemberById_Success() throws Exception {
//        // given
//        when(memberService.getMemberById(anyLong())).thenReturn(memberDto);
//
//        // when & then
//        mockMvc.perform(get("/api/members/{memberId}", 1L)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.email").value("test@example.com"))
//                .andExpect(jsonPath("$.name").value("홍길동"));
//    }

//    @Test
//    @DisplayName("본인 정보 조회 성공")
//    void testGetMyInfo_Success() throws Exception {
//        // given
//        MemberDetails memberDetails = MemberDetails.builder()
//                ..setId(1L)
//                .build();
//
//        when(memberService.getMyInfo(anyLong())).thenReturn(memberDto);
//
//        // when & then
//        mockMvc.perform(get("/api/members/me")
//                        .principal(() -> "test@example.com") // 인증된 사용자로 설정
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.email").value("test@example.com"))
//                .andExpect(jsonPath("$.name").value("홍길동"));
//    }
}
