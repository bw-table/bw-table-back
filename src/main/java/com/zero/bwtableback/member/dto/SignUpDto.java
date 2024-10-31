package com.zero.bwtableback.member.dto;

import com.zero.bwtableback.member.entity.LoginType;
import com.zero.bwtableback.member.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpDto {
    private LoginType loginType;

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
    private String nickname;

    @Pattern(regexp = "^\\d{10,15}$", message = "전화번호는 10~15자리 숫자로 입력하세요.")
    private String phone;

    private Role role;

    @Pattern(regexp = "^\\d{10,12}$", message = "사업자 번호는 10~12자리 숫자로 입력하세요.")
    private String businessNumber;
}
