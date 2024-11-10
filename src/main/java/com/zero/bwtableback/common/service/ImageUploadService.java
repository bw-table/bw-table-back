package com.zero.bwtableback.common.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final AmazonS3 amazonS3Client;

    private final MemberRepository memberRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET_NAME;

    /**
     * 회원 프로필 이미지
     * - 최대 1장
     */
    public String uploadProfileImage(MultipartFile file, String email) throws IOException {
        validateSingleImageFile(file);
        String fileUrl = uploadFile(file);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        member.setProfileImage(fileUrl);
        memberRepository.save(member);

        return fileUrl;
    }

    /**
     * 리뷰 이미지
     * - 최대 5장
     */
    public List<String> uploadReviewImages(MultipartFile[] files) throws IOException {
        validateImageFiles(files, 5); // 최대 5개 검증
        List<String> fileUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            String fileUrl = uploadFile(file);
            fileUrls.add(fileUrl);
        }
        return fileUrls;
    }

    /**
     * 가게 이미지
     * - 최대 5장
     */
    public List<String> uploadRestaurantImages(MultipartFile[] files) throws IOException {
        validateImageFiles(files, 5);
        List<String> fileUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            String fileUrl = uploadFile(file);
            fileUrls.add(fileUrl);
        }
        return fileUrls;
    }

    /**
     * 메뉴 이미지
     * - 최대 1장
     */
    public String uploadMenuImage(MultipartFile file) throws IOException {
        validateSingleImageFile(file);
        return uploadFile(file);
    }

    // S3에 이미지 업로드
    private String uploadFile(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        amazonS3Client.putObject(BUCKET_NAME, fileName, file.getInputStream(), metadata);

        return "https://" + BUCKET_NAME + ".s3.amazonaws.com/" + fileName; // S3 URL 반환
    }

    // 이미지 1장 유효성 검사
    private void validateSingleImageFile(MultipartFile file) {
        if (!isValidImageFileType(file)) {
            throw new IllegalArgumentException("JPG, JPEG, PNG 형식만 가능합니다.");
        }
    }

    // 이미지 여러 장 유효성 검사
    private void validateImageFiles(MultipartFile[] files, int maxCount) {
        if (files.length > maxCount) {
            throw new IllegalArgumentException("최대 " + maxCount + "개의 이미지만 업로들 할 수 있습니다.");
        }

        for (MultipartFile file : files) {
            if (!isValidImageFileType(file)) {
                throw new IllegalArgumentException("JPG, JPEG, PNG 형식만 가능합니다.");
            }
        }
    }

    // 이미지 파일 타입 유효성 검사
    private boolean isValidImageFileType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null &&
                (contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/png"));
    }
}
