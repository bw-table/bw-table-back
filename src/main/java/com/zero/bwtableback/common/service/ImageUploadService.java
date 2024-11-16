package com.zero.bwtableback.common.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
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
     * TODO 가게 아이디로 받을 수 있게 설정
     * TODO 순서는 어떻게 보장할지 생각해보기
     */
    public List<String> uploadRestaurantImages(MultipartFile[] files) throws IOException {
        validateImageFiles(files, 5);
        List<String> fileUrls = new ArrayList<>();

//        for (MultipartFile file : files) {
//            String fileUrl = uploadFile(file,RESTAURANT_BUCKET_NAME,filepath);
//            fileUrls.add(fileUrl);
//        }
//        return fileUrls;
        return null;
    }

    /**
     * 리뷰 이미지
     * - 최대 5장
     * TODO 가게 아이디로 받을 수 있게 설정
     * TODO 리뷰 아이디로 필요한지 생각
     * TODO 순서는 어떻게 보장할지 생각해보기
     */
    public List<String> uploadReviewImages(MultipartFile[] files) throws IOException {
        validateImageFiles(files, 5); // 최대 5개 검증
        List<String> fileUrls = new ArrayList<>();

//        for (MultipartFile file : files) {
//            String fileUrl = uploadFile(file,RESTAURANT_BUCKET_NAME);
//            fileUrls.add(fileUrl);
//        }
//        return fileUrls;
        return null;
    }

    /**
     * 메뉴 이미지
     * - 최대 1장
     *  TODO 가게 아이디로 받을 수 있게 설정
     *  TODO 메뉴 아이디 받아야하는지 생각
     */
    public String uploadMenuImage(MultipartFile file) throws IOException {
//        validateSingleImageFile(file);
//        return uploadFile(file,RESTAURANT_BUCKET_NAME);
        return null;
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
