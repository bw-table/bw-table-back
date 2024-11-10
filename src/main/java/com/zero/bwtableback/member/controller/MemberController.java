package com.zero.bwtableback.member.controller;

import com.zero.bwtableback.common.service.ImageUploadService;
import com.zero.bwtableback.member.dto.MemberDto;
import com.zero.bwtableback.member.service.MemberService;
import com.zero.bwtableback.security.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final ImageUploadService imageUploadService;

    /**
     * 회원 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<MemberDto>> getMembers(Pageable pageable) {
        return ResponseEntity.ok(memberService.getMembers(pageable));
    }

    /**
     * 특정 회원 정보 조회
     */
    @GetMapping("/{memberId}")
    public MemberDto getMemberById(@PathVariable Long memberId) {
        return memberService.getMemberById(memberId);
    }

    /**
     * 본인 정보 조회
     *
     * JWT 토큰으로 현재 사용자 인증
     */
    @GetMapping("/me")
    public MemberDto getMyInfo(@AuthenticationPrincipal MemberDetails memberDetails) {
        String email = memberDetails.getUsername();
        return memberService.getMyInfo(email);
    }

    /**
     * 프로필 이미지 업로드 (S3)
     */
    @PostMapping("/profile-image")
    public ResponseEntity<Map<String, String>> uploadFile(@AuthenticationPrincipal MemberDetails memberDetails,
                                                          @RequestParam("file") MultipartFile file) {
        String email = memberDetails.getUsername();

        try {
            String fileUrl = imageUploadService.uploadProfileImage(file, email);

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", fileUrl);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // FIXME 가게, 리뷰 참고용 (여러 장)
//    @PostMapping("/upload-files")
//    public ResponseEntity<Map<String, List<String>>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
//        List<String> fileUrls = new ArrayList<>();
//
//        for (int i = 0; i < files.length; i++) {
//            MultipartFile file = files[i];
//            try {
//                // 파일 이름 생성
//                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
//
//                // S3에 파일 업로드
//                String fileUrl = memberService.uploadFile(fileName, file.getInputStream(), file.getSize());
//                fileUrls.add(fileUrl); // 업로드한 파일 URL 추가
//
//                // 파일 순서 확인
//                System.out.println("Uploaded file index: " + i + ", URL: " + fileUrl);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//            }
//        }
//
//        // JSON 응답 생성
//        Map<String, List<String>> response = new HashMap<>();
//        response.put("fileUrls", fileUrls);
//
//        return ResponseEntity.ok(response);
//    }

    /**
     * 이미지 수정
     */

    /**
     * TODO 이미지 삭제
     */
}
