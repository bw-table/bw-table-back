-- 회원 데이터
INSERT INTO member (login_type, email, password, name, nickname, phone, role,
                    business_number, profile_image_url, provider, provider_id)
VALUES ('EMAIL', 'john.doe@example.com', 'password123', '홍길동', '홍길이',
        '01011112222', 'GUEST', NULL, NULL, NULL, NULL),
       ('EMAIL', 'jane.smith@example.com', 'password123', '김영희', '영희',
        '01033334444', 'GUEST', NULL, NULL, NULL, NULL),
       ('SOCIAL', 'kakao_user@example.com', 'password123', '카카오 사용자',
        '카카오유저', '01055556666', 'GUEST', NULL, NULL, 'Kakao', 'kakao123'),
       ('SOCIAL', 'google_user@example.com', 'password123', '구글 사용자',
        '구글유저', '01077778888', 'GUEST', NULL, NULL, 'Google',
        'google123'),
       ('EMAIL', 'owner@example.com', 'ownerpassword123', '사장님 이름',
        '사장님닉네임', '01099990000', 'OWNER', '1234567890', NULL, NULL,
        NULL);

-- 레스토랑 데이터
INSERT INTO restaurant (name, description, address, contact, closed_day)
VALUES ('고급 레스토랑', '고급 요리를 제공하는 레스토랑입니다.',
        '서울특별시 강남구 테헤란로 123', '0212345678', '월요일'),
       ('매운 주방', '전 세계의 정통 매운 요리를 제공합니다.',
        '서울특별시 종로구 인사동 456', '0223456789', '화요일'),
       ('초밥 천국', '매일 신선한 초밥과 사시미를 제공합니다.',
        '서울특별시 마포구 홍대 789', '0234567890', NULL),
       ('비건 맛집', '모두를 위한 맛있는 식물 기반 식사를 제공합니다.',
        '서울특별시 용산구 이태원 1011', '0245678901', NULL),
       ('버거 천국', '육즙 가득한 버거와 바삭한 감자튀김을 제공합니다.',
        '서울특별시 서초구 양재동 1213', 0245678931, NULL);