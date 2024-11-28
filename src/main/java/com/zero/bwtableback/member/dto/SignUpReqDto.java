package com.zero.bwtableback.member.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor // JSON 역직렬화(JSON 문자열 -> Java 객체)를 위해 필요
public class SignUpReqDto {
    @NotNull(message = "로그인 타입을 선택하세요.")
    private String loginType; // Enum 대신 String으로 변경

    @NotNull(message = "역할을 선택하세요.")
    private String role; // Enum 대신 String으로 변경

    @NotBlank(message = "이메일을 입력하세요.")
    @Email(message = "유효한 이메일 주소를 입력하세요.")
    private String email;

    @NotBlank(message = "이름을 입력하세요.")
    private String name;

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$",
            message = "비밀번호는 최소 8자 이상이며 대문자, 소문자, 숫자 및 특수문자를 포함해야 합니다.")
    private String password;

    @NotBlank(message = "닉네임을 입력하세요.")
    @Size(min = 2, max = 15, message = "닉네임은 최소 2자 이상, 최대 15자 이하이어야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "닉네임은 한글, 영문자, 숫자만 허용됩니다.")
    private String nickname;

    @Pattern(regexp = "^\\d{10,15}$", message = "전화번호는 10~15자리 숫자로 입력하세요.")
    private String phone;

    @Pattern(regexp = "^\\d{10,12}$", message = "사업자 번호는 10~12자리 숫자로 입력하세요.")
    private String businessNumber;
}
