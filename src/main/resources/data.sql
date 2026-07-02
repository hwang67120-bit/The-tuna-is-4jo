INSERT INTO category (id, name, created_at, updated_at)
VALUES (1, '상의', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO category (id, name, created_at, updated_at)
VALUES (2, '하의', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (1, 1, 1, '오버핏 후드티', 39000, '상세설명...', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (2, 1, 2, '와이드 데님 팬츠', 45000, '카테고리 조회 테스트용 상품', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (1, 1, '블랙 / L', 12, 0, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (2, 1, '화이트 / M', 0, 0, 'SOLDOUT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (3, 2, '청색 / M', 8, 1000, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

ALTER TABLE category
    AUTO_INCREMENT = 3;
INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (3, 1, 1, '베이직 반팔 티셔츠', 19000, '데일리로 입기 좋은 기본 반팔 티셔츠', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (4, 1, 1, '린넨 셔츠', 34000, '가볍고 시원한 여름 린넨 셔츠', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (5, 1, 1, '크롭 맨투맨', 42000, '캐주얼한 핏의 크롭 맨투맨', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (6, 1, 1, '니트 가디건', 52000, '부드러운 터치감의 니트 가디건', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (7, 1, 1, '후드 집업', 59000, '간절기에 입기 좋은 후드 집업', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (8, 1, 1, '스트라이프 셔츠', 36000, '깔끔한 스트라이프 패턴 셔츠', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (9, 1, 1, '오버핏 자켓', 89000, '넉넉한 실루엣의 데일리 자켓', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (10, 1, 1, '카라 니트', 47000, '단정한 카라 디자인 니트', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (11, 1, 1, '피그먼트 티셔츠', 28000, '빈티지한 색감의 피그먼트 티셔츠', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (12, 1, 1, '라운드 스웨터', 49000, '겨울철 활용도 높은 라운드 스웨터', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (13, 1, 2, '슬림 슬랙스', 43000, '깔끔한 핏의 데일리 슬랙스', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (14, 1, 2, '조거 팬츠', 39000, '편하게 입기 좋은 조거 팬츠', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (15, 1, 2, '코튼 쇼츠', 26000, '여름용 코튼 반바지', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (16, 1, 2, '카고 팬츠', 57000, '포켓 디테일이 있는 카고 팬츠', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (17, 1, 2, '부츠컷 데님', 48000, '라인이 예쁜 부츠컷 데님', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (18, 1, 2, '와이드 슬랙스', 46000, '편안한 와이드 핏 슬랙스', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (19, 1, 2, '트레이닝 팬츠', 33000, '운동과 일상 모두 가능한 팬츠', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (20, 1, 2, '핀턱 팬츠', 52000, '핀턱 디테일의 고급스러운 팬츠', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (21, 1, 2, '밴딩 데님', 41000, '허리 밴딩으로 편안한 데님 팬츠', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (22, 1, 2, '치노 팬츠', 37000, '기본으로 활용하기 좋은 치노 팬츠', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (4, 3, '화이트 / M', 15, 0, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (5, 4, '베이지 / L', 9, 1000, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (6, 5, '그레이 / FREE', 7, 0, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (7, 6, '아이보리 / M', 11, 2000, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (8, 7, '블랙 / XL', 6, 3000, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (9, 8, '블루 / L', 13, 0, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (10, 9, '차콜 / FREE', 5, 5000, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (11, 10, '네이비 / M', 8, 1000, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (12, 11, '카키 / L', 10, 0, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (13, 12, '브라운 / FREE', 4, 2000, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (14, 13, '블랙 / M', 14, 0, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (15, 14, '멜란지 / L', 12, 0, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (16, 15, '베이지 / M', 16, 0, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (17, 16, '카키 / L', 6, 2000, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (18, 17, '중청 / M', 9, 1000, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (19, 18, '그레이 / L', 10, 0, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (20, 19, '블랙 / FREE', 18, 0, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (21, 20, '네이비 / M', 7, 3000, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (22, 21, '진청 / L', 11, 1000, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (23, 22, '베이지 / M', 13, 0, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
ALTER TABLE product
    AUTO_INCREMENT = 23;
ALTER TABLE product_option
    AUTO_INCREMENT = 24;
