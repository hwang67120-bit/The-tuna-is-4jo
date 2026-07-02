import http from 'k6/http';
import {check, sleep} from 'k6';
import {Trend} from 'k6/metrics';

const recoveryDuration = new Trend('chat_message_recovery_duration');

export const options = {
    vus: Number(__ENV.VUS || 10),
    duration: __ENV.DURATION || '30s',
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<1000'],
        chat_message_recovery_duration: ['p(95)<1000'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const EMAIL = __ENV.EMAIL || 'indexuser@test.com';
const PASSWORD = __ENV.PASSWORD || 'Test!1234';
const ROOM_ID = __ENV.ROOM_ID || '1';
const AFTER_MESSAGE_ID = __ENV.AFTER_MESSAGE_ID || '49000';

export function setup() {
    const loginRes = http.post(
        `${BASE_URL}/api/members/login`,
        JSON.stringify({email: EMAIL, password: PASSWORD}),
        {headers: {'Content-Type': 'application/json'}}
    );

    check(loginRes, {
        'login status is 200': (res) => res.status === 200,
        'login has accessToken': (res) => Boolean(res.json('data.accessToken')),
    });

    return {
        token: loginRes.json('data.accessToken'),
    };
}

export default function (data) {
    const res = http.get(
        `${BASE_URL}/api/chats/${ROOM_ID}/messages?afterMessageId=${AFTER_MESSAGE_ID}`,
        {headers: {Authorization: `Bearer ${data.token}`}}
    );

    recoveryDuration.add(res.timings.duration);

    check(res, {
        'recovery status is 200': (response) => response.status === 200,
        'recovery returns messages': (response) => Array.isArray(response.json('data')),
    });

    sleep(1);
}