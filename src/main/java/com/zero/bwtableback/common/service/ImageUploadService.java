package com.zero.bwtableback.common.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.restaurant.entity.*;
import com.zero.bwtableback.restaurant.repository.MenuRepository;
import com.zero.bwtableback.restaurant.repository.RestaurantImageRepository;
import com.zero.bwtableback.restaurant.repository.ReviewImageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageUploadService {

    private final AmazonS3 amazonS3Client;
    private final MemberRepository memberRepository;
    private final RestaurantImageRepository restaurantImageRepository;
    private final MenuRepository menuRepository;
    private final ReviewImageRepository reviewImageRepository;


    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET_NAME;

    /**
     * 회원 프로필 이미지
     * - 최대 1장
     */
    public String uploadProfileImage(MultipartFile file, Long memberId) throws IOException {
        validateSingleImageFile(file);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String fileUrl = uploadFile(file, "member/" + member.getId() + "/profile/");

        member.setProfileImage(fileUrl);
        memberRepository.save(member);

        return fileUrl;
    }

    /**
     * 이미지 수정 1장
     */
    public String updateProfileImage(MultipartFile newFile, Long memberId) throws IOException {
        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            String oldFileUrl = member.getProfileImage();
            if (oldFileUrl != null) {
                deleteFileFromS3(member);
            }

            String newFileUrl = uploadProfileImage(newFile, memberId);
            return newFileUrl;

        } catch (IOException e) {
            log.error("회원 ID: {}에 대한 파일 업로드 오류", memberId, e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 이미지 삭제
     */
    public void deleteFileFromDB(Long memberId) {
        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            // S3에서 파일 삭제
            deleteFileFromS3(member);

            // 프로필 이미지 URL을 null로 설정
            member.setProfileImage(null);

            // 데이터베이스에 변경 사항 저장
            memberRepository.save(member);

        } catch (Exception e) {
            // 일반적인 예외 처리
            log.error("회원 ID: {}에 대한 파일 삭제 오류", memberId, e);
            throw new CustomException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    /**
     * 이미지 삭제 S3에서
     */
    private void deleteFileFromS3(Member member) {
        String fileUrl = member.getProfileImage();
        String baseUrl = "https://bwtable.s3.amazonaws.com/";
        String fileName = fileUrl.replace(baseUrl, "");

        amazonS3Client.deleteObject(BUCKET_NAME, fileName);
    }

    /**
     * 가게 이미지
     * - 최대 5장
     */
    public List<String> uploadRestaurantImages(Long restaurantId, MultipartFile[] files) throws IOException {
        validateImageFiles(files, 5);

        List<String> fileUrls = new ArrayList<>();

        for (MultipartFile file: files) {
            String fileUrl = uploadFile(file, "restaurant/" + restaurantId + "/main/");
            fileUrls.add(fileUrl);
        }
        return fileUrls;
    }

    /**
     * 메뉴 이미지
     * - 최대 1장
     */
    public String uploadMenuImage(Long restaurantId, Long menuId, MultipartFile file) throws IOException {
        validateSingleImageFile(file);

        String fileUrl = uploadFile(file, "restaurant/" + restaurantId + "/menu/" + menuId + "/");

        return fileUrl;
    }

    /**
     * 리뷰 이미지
     * - 최대 5장
     */
    public List<String> uploadReviewImages(Long restaurantId,
                                           Long reviewId,
                                           MultipartFile[] files) throws IOException {

        validateImageFiles(files, 5);

        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file: files) {
            String fileUrl = uploadFile(file, "restaurant/" + restaurantId + "/review/" + reviewId + "/");
            fileUrls.add(fileUrl);
        }

        return fileUrls;
    }

    // S3에 이미지 업로드
    private String uploadFile(MultipartFile file, String folderPath) throws IOException {
        String fileName = Instant.now().toEpochMilli() + "_" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        // S3에 파일 업로드
        amazonS3Client.putObject(BUCKET_NAME, folderPath + fileName, file.getInputStream(), metadata);

        return "https://" + BUCKET_NAME + ".s3.amazonaws.com/" + folderPath + fileName;
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

    // FIXME 삭제 검토
//    public String updateProfileImage(MultipartFile newFile, String email) throws IOException {
//        // 기존 프로필 이미지 URL을 가져옵니다.
//        Member member = memberRepository.findByEmail(email)
//                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
//
//        String oldFileUrl = member.getProfileImage(); // 기존 URL 가져오기
//        if (oldFileUrl != null) {
//            deleteFileFromS3(oldFileUrl); // 기존 파일 삭제
//        }
//
//        // 새로운 이미지를 S3에 업로드
//        String newFileUrl = uploadFile(newFile, "member/" + member.getId() + "/profile/");
//        member.setProfileImage(newFileUrl);
//        memberRepository.save(member);
//
//        return newFileUrl;
//    }

    // FIXME 삭제검토 :  기존 프로필 이미지 URL을 가져오는 메서드
    private String getOldProfileImageUrl(String email) {
        // 기존 프로필 이미지 URL 반환
        // return member.getProfileImage();

        return null;
    }

    // S3에서 이미지 삭제
    private void deleteFileFromS3(String fileUrl) {
        // S3 URL에서 파일 이름 추출
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
//        amazonS3Client.deleteObject(fileName);
    }

    // 식당 이미지 삭제
    public void deleteRestaurantImage(Long imageId) {
        RestaurantImage image = restaurantImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant image not found"));

        deleteFileFromS3(image.getImageUrl());

        restaurantImageRepository.delete(image);
    }

    // 메뉴 이미지 삭제
    public void deleteMenuImage(Long restaurantId, Long menuId) throws IOException {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new EntityNotFoundException("Menu not found"));

        deleteFileFromS3(menu.getImageUrl());

        menuRepository.deleteMenuImageByRestaurantAndMenuId(restaurantId, menuId);
    }

    // 리뷰 이미지 삭제
    public void deleteReviewImageFile(Long imageId) {
        ReviewImage image = reviewImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Review image not found"));

        deleteFileFromS3(image.getImageUrl());

        reviewImageRepository.delete(image);
    }

    // FIXME 삭제검토  URL에서 파일 경로 추출
    public String getImagePathFromUrl(String imageUrl) {
        String baseUrl = "https://" + BUCKET_NAME + ".s3.amazonaws.com/";
        if (imageUrl.startsWith(baseUrl)) {
            return imageUrl.substring(baseUrl.length());
        }
        return imageUrl;
    }

    // FIXME 삭제검토  URL에서 파일 이름 추출
    private String getFileNameFromUrl(String imageUrl) {
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }
}
