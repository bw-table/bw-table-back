package com.zero.bwtableback.common.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.restaurant.entity.Menu;
import com.zero.bwtableback.restaurant.entity.ReviewImage;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final AmazonS3 amazonS3Client;
    private final MemberRepository memberRepository;
    private final RestaurantRepository restaurantRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET_NAME;

    /**
     * 회원 프로필 이미지
     * - 최대 1장
     */
    public String uploadProfileImage(MultipartFile file, String email) throws IOException {
        validateSingleImageFile(file);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String fileUrl = uploadFile(file, "member/" + member.getId() + "/profile/");

        member.setProfileImage(fileUrl);
        memberRepository.save(member);

        return fileUrl;
    }

    /**
     * 가게 이미지
     * - 최대 5장
     * TODO 가게 아이디로 받을 수 있게 설정
     * TODO 순서는 어떻게 보장할지 생각해보기
     */
    public Set<String> uploadRestaurantImages(MultipartFile[] files) throws IOException {
        validateImageFiles(files, 5);

        Set<String> fileUrls = new HashSet<>();

        for (MultipartFile file: files) {
            String fileUrl = uploadFile(file, "temp/restaurant/");
            fileUrls.add(fileUrl);
        }
        return fileUrls;
    }

    // 식당 등록 후 식당 이미지 이동
    public void moveRestaurantImagesToFinalPath(Long restaurantId, Set<String> tempFileUrls) throws IOException {
        String tempFolderPath = "temp/restaurant/";
        String finalFolderPath = "restaurant/" + restaurantId + "/main/";

        for (String tempFileUrl: tempFileUrls) {
            // 임시 URL에서 파일명 추출
            String tempFileName = tempFileUrl.substring(tempFileUrl.lastIndexOf("/") + 1);

            // 최종 경로로 복사
            amazonS3Client.copyObject(BUCKET_NAME, tempFolderPath + tempFileName, BUCKET_NAME, finalFolderPath + tempFileName);
            // 원본 삭제
            amazonS3Client.deleteObject(BUCKET_NAME, tempFolderPath + tempFileName);
        }
    }

    /**
     * 메뉴 이미지
     * - 최대 1장
     *  TODO 가게 아이디로 받을 수 있게 설정
     *  TODO 메뉴 아이디 받아야하는지 생각
     */
    public String uploadMenuImages(MultipartFile file) throws IOException {
        return uploadFile(file, "temp/restaurant/menu/");
    }

    // 식당 등록 후 메뉴 이미지 이동
    public void moveMenuImagesToFinalPath(Long restaurantId, List<Menu> menus) throws IOException {
        String tempFolderPath = "temp/restaurant/menu/";
        String finalFolderPathTemplate = "restaurant/" + restaurantId + "/menu/";

        for (Menu menu: menus) {
            if (menu.getImageUrl() != null) {
                String tempFileName = menu.getImageUrl().substring(menu.getImageUrl().lastIndexOf("/") + 1);
                String finalFolderPath = finalFolderPathTemplate + menu.getId() + "/";

                amazonS3Client.copyObject(BUCKET_NAME, tempFolderPath + tempFileName, BUCKET_NAME, finalFolderPath + tempFileName);
                amazonS3Client.deleteObject(BUCKET_NAME, tempFolderPath + tempFileName);

                String finalImageUrl = "https://" + BUCKET_NAME + ".s3.amazonaws.com/" + finalFolderPath + tempFileName;
                menu.setImageUrl(finalImageUrl);
            }
        }
    }

    /**
     * 리뷰 이미지
     * - 최대 5장
     * TODO 가게 아이디로 받을 수 있게 설정
     * TODO 리뷰 아이디로 필요한지 생각
     * TODO 순서는 어떻게 보장할지 생각해보기
     */
    public List<String> uploadReviewImages(Long restaurantId,
                                           Long reviewId,
                                           MultipartFile[] files) throws IOException {
//        String tempFolderPath = "temp/restaurant/" + restaurantId + "/review/";

        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file: files) {
            String fileUrl = uploadFile(file, "restaurant/" + restaurantId + "/review/" + reviewId + "/");
            fileUrls.add(fileUrl);
        }

        return fileUrls;
    }

    // 리뷰 등록 후 리뷰 이미지 이동
    public void moveReviewImagesToFinalPath(Long restaurantId,
                                            Long reviewId,
                                            List<ReviewImage> reviewImages) throws IOException {

        String tempFolderPath = "temp/restaurant/" + restaurantId + "/review/";
        String finalFolderPath = "restaurant/" + restaurantId + "/review/" + reviewId + "/";

        for (ReviewImage reviewImage: reviewImages) {
            String tempFileName = reviewImage.getImageUrl().substring(reviewImage.getImageUrl().lastIndexOf("/") + 1);

            amazonS3Client.copyObject(BUCKET_NAME, tempFolderPath + tempFileName, BUCKET_NAME, finalFolderPath + tempFileName);
            amazonS3Client.deleteObject(BUCKET_NAME, tempFolderPath + tempFileName);

            String finalImageUrl = "https://" + BUCKET_NAME + ".s3.amazonaws.com/" + finalFolderPath + tempFileName;
            reviewImage.setImageUrl(finalImageUrl);
        }
    }

    // S3에 이미지 업로드
    private String uploadFile(MultipartFile file, String folderPath) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        // S3에 파일 업로드
        amazonS3Client.putObject(BUCKET_NAME, folderPath + fileName, file.getInputStream(), metadata);

        return "https://" + BUCKET_NAME + ".s3.amazonaws.com/" + folderPath + fileName;
    }

    // s3 임시 경로에 이미지 업로드
//    private String uploadTempFile(MultipartFile file, String folderPath) throws IOException {
//        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
//
//        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentType(file.getContentType());
//        metadata.setContentLength(file.getSize());
//
//        amazonS3Client.putObject(BUCKET_NAME, folderPath + fileName, file.getInputStream(), metadata);
//
//        return "https://" + BUCKET_NAME + ".s3.amazonaws.com/" + folderPath + fileName;
//    }

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

    /**
     * TODO 이미지 수정 1장
     */
    public String updateProfileImage(MultipartFile newFile, String email) throws IOException {
        // 기존 프로필 이미지 URL을 가져옵니다.
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String oldFileUrl = member.getProfileImage(); // 기존 URL 가져오기
        if (oldFileUrl != null) {
            deleteFileFromS3(oldFileUrl); // 기존 파일 삭제
        }

        // 새로운 이미지를 S3에 업로드
        String newFileUrl = uploadFile(newFile, "member/" + member.getId() + "/profile/");
        member.setProfileImage(newFileUrl);
        memberRepository.save(member);

        return newFileUrl;
    }

    // TODO 기존 프로필 이미지 URL을 가져오는 메서드
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
}
