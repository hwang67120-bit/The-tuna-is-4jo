INSERT INTO member (id, email, password, name, phone_number, role, created_at, updated_at)
VALUES (
    1,
    'indexuser@test.com',
    '120000:MDEyMzQ1Njc4OWFiY2RlZg==:jQt1BL66YZzsGhES7ZJ9vQ84E4sPjlQKWEpXCoqfs68=',
    'Index Test User',
    '01012345678',
    'USER',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO chat_room (id, member_id, admin_id, title, status, completed_at, created_at, updated_at)
SELECT
    seq,
    1,
    NULL,
    CONCAT('index test room ', seq),
    'WAITING',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM SYSTEM_RANGE(1, 5) AS dummy(seq);

INSERT INTO chat_message (id, chat_room_id, sender_id, content, message_type, created_at, updated_at)
SELECT
    seq,
    MOD(seq - 1, 5) + 1,
    1,
    CONCAT('dummy message ', seq),
    'USER',
    DATEADD('SECOND', seq, TIMESTAMP '2026-01-01 00:00:00'),
    DATEADD('SECOND', seq, TIMESTAMP '2026-01-01 00:00:00')
FROM SYSTEM_RANGE(1, 50000) AS dummy(seq);

ALTER TABLE member ALTER COLUMN id RESTART WITH 2;
ALTER TABLE chat_room ALTER COLUMN id RESTART WITH 6;
ALTER TABLE chat_message ALTER COLUMN id RESTART WITH 50001;