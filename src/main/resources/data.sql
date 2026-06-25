INSERT INTO category (id, name, created_at, updated_at)
VALUES (1, '상의', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO product (id, member_id, category_id, name, price, description, status, created_at, updated_at)
VALUES (1, 1, 1, '테스트 상품', 10000, '카트 테스트용 상품', 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO product_option (id, product_id, option_name, option_stock, additional_price, status, created_at, updated_at)
VALUES (1, 1, '기본 옵션', 100, 0, 'ON_SALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
