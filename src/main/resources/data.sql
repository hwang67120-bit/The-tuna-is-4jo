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

ALTER TABLE category ALTER COLUMN id RESTART WITH 3;
ALTER TABLE product ALTER COLUMN id RESTART WITH 3;
ALTER TABLE product_option ALTER COLUMN id RESTART WITH 4;
