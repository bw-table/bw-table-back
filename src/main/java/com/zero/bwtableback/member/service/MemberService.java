package com.zero.bwtableback.member.service;

import com.zero.bwtableback.member.dto.SignupForm;
import com.zero.bwtableback.member.entity.Member;

public interface MemberService {
    Member signupMember(SignupForm form);
}