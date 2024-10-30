package com.zero.bwtableback.member.service;

import com.zero.bwtableback.member.dto.SignupFormDto;
import com.zero.bwtableback.member.entity.Member;

public interface MemberService {
    Member signupMember(SignupFormDto form);
}