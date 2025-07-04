import http from 'k6/http';
import { check, sleep, group } from 'k6';

/*
| **Phase** | **Purpose**                                                                   |
| --------- | ----------------------------------------------------------------------------- |
| Warm-up   | Bring system to operating temperature                                         |
| Spike     | Simulate a sudden flood of users to test autoscaling, caching, resilience     |
| Stress    | Gradually increase pressure to find system limits (throughput, latency, etc.) |
| Soak      | Test system stability, memory leaks, token/session handling under load        |
| Ramp-down | Observe graceful recovery                                                     |
*/

// Parameterized URLs
const BASE_URL = __ENV.BASE_URL || 'https://localhost:8080/huddey';
const REGISTRATION_ENDPOINT = `${BASE_URL}/api/v1/auth/basic-auth`;
const LOGIN_ENDPOINT = `${BASE_URL}/api/v1/auth/login`;
const ME_ENDPOINT = `${BASE_URL}/api/v1/auth/me`;

// Test configuration
export let options = {
    insecureSkipTLSVerify: true,
    stages: [
        // 🧪 Warm-up
        { duration: '1m', target: 20 },

        // 🚀 Spike
        { duration: '15s', target: 100 },
        { duration: '30s', target: 100 },

        // 📈 Stress ramp-up to failure point
        { duration: '1m', target: 200 },
        { duration: '1m', target: 300 },
        { duration: '1m', target: 400 },

        // 🛡️ Soak test: sustain high load
        { duration: '5m', target: 400 },

        // 🧯 Ramp-down
        { duration: '1m', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<2000'],     // 95% of requests must complete < 2s
        http_req_failed: ['rate<0.1'],         // < 10% failures allowed
        checks: ['rate>0.95'],                 // At least 95% of checks must pass
    },
};

// --- Data Generation Functions ---

function generateRandomString(length, charset) {
    let result = '';
    for (let i = 0; i < length; i++) {
        result += charset.charAt(Math.floor(Math.random() * charset.length));
    }
    return result;
}

function generateName(length) {
    const nameChars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
    let name = nameChars.charAt(Math.floor(Math.random() * 26)).toUpperCase(); // First letter capital
    name += generateRandomString(length - 1, nameChars.toLowerCase());
    return name;
}

function generatePassword() {
    const lowercase = 'abcdefghijklmnopqrstuvwxyz';
    const uppercase = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    const numbers = '0123456789';
    const special = '@#!$%^&+=';
    let password = generateRandomString(1, lowercase) +
        generateRandomString(1, uppercase) +
        generateRandomString(1, numbers) +
        generateRandomString(1, special);
    const remainingLength = 4;
    const allChars = lowercase + uppercase + numbers + special;
    password += generateRandomString(remainingLength, allChars);
    return password.split('').sort(() => Math.random() - 0.5).join('');
}

function generatePhoneNumber() {
    const countryCode = '+' + (Math.floor(Math.random() * 98) + 1);
    const length = Math.floor(Math.random() * 7) + 7;
    const number = generateRandomString(length, '0123456789');
    return countryCode + number;
}

function generateCompanyName() {
    const prefixes = ['Tech', 'Global', 'Smart', 'Next', 'Digital', 'Innovative', 'Future', 'Advanced'];
    const suffixes = ['Solutions', 'Systems', 'Technologies', 'Innovations', 'Group', 'Industries', 'Services'];
    return prefixes[Math.floor(Math.random() * prefixes.length)] +
        ' ' +
        suffixes[Math.floor(Math.random() * suffixes.length)];
}

/**
 * Generates a unique email for every iteration of every virtual user.
 * Prevents registration failures due to data collisions during the test.
 * __VU is the virtual user ID, __ITER is the iteration number for that user.
 */
function generateEmail(firstName) {
    return `${firstName.toLowerCase()}.${__VU}.${__ITER}@test.com`;
}

function generateUserData() {
    const firstName = generateName(Math.floor(Math.random() * 5) + 4);
    const lastName = generateName(Math.floor(Math.random() * 5) + 4);
    const companyName = generateCompanyName();
    const phoneNumber = generatePhoneNumber();
    const password = generatePassword();
    const email = generateEmail(firstName);

    return { firstName, lastName, companyName, phoneNumber, password, email };
}

// --- Main Test Logic ---
export default function () {
    const userData = generateUserData();
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
        },
    };

    let loginResponse;
    let registrationFailed = false;

    // Group 1: User Registration
    group('1. User Registration', function () {
        const registrationPayload = {
            firstName: userData.firstName,
            lastName: userData.lastName,
            email: userData.email,
            password: userData.password,
            phoneNumber: userData.phoneNumber,
            companyName: userData.companyName
        };

        const registrationResponse = http.post(REGISTRATION_ENDPOINT, JSON.stringify(registrationPayload), params);

        check(registrationResponse, {
            'registration status is 201 or 200': (r) => [200, 201].includes(r.status),
            'registration response has valid body': (r) => {
                try {
                    const body = JSON.parse(r.body);
                    return body !== null && typeof body === 'object';
                } catch (e) {
                    return false;
                }
            }
        });

        if (registrationResponse.status >= 400) {
            console.log(`Registration failed for ${userData.email} with status ${registrationResponse.status}. Response: ${registrationResponse.body}`);
            registrationFailed = true;
        }
    });

    if (registrationFailed) {
        // Skip login and profile fetch if registration failed
        return;
    }

    // Simulate user think time between registration and login
    sleep(Math.random() * 2 + 1);

    // Group 2: User Login
    group('2. User Login', function () {
        const loginPayload = { email: userData.email, password: userData.password };
        loginResponse = http.post(LOGIN_ENDPOINT, JSON.stringify(loginPayload), params);

        check(loginResponse, {
            'login successful (status 200)': (r) => r.status === 200,
            'login response contains token': (r) => {
                if (r.cookies.access_token && r.cookies.access_token.length > 0) return true;
                try {
                    return JSON.parse(r.body).hasOwnProperty('token');
                } catch (e) {
                    return false;
                }
            },
        });
    });

    // Simulate user think time between login and fetching profile
    sleep(Math.random() * 2 + 1);

    // Group 3: Fetch User Profile
    group('3. Fetch User Profile', function () {
        if (!loginResponse || loginResponse.status !== 200) {
            console.warn(`Skipping /me for ${userData.email} due to login failure`);
            return;
        }

        const token = extractToken(loginResponse);
        if (!token) {
            console.warn(`Token not found in login response for ${userData.email}`);
            return;
        }

        const meResponse = http.get(ME_ENDPOINT, {
            headers: { 'Accept': 'application/json' },
            cookies: loginResponse.cookies,
        });

        check(meResponse, {
            'profile fetch successful (status 200)': (r) => r.status === 200,
            'profile data matches registered user': (r) => {
                try {
                    const profile = JSON.parse(r.body);
                    const actualEmail = profile?.data?.email;
                    if (!actualEmail) {
                        console.warn(`No email found in profile for ${userData.email}`);
                        return false;
                    }
                    return actualEmail.toLowerCase() === userData.email.toLowerCase();
                } catch (e) {
                    console.error(`Failed to parse /me response for ${userData.email}`);
                    return false;
                }
            }
        });
    });

    // Final sleep before next iteration starts
    sleep(Math.random() * 2 + 1);
}

function extractToken(response) {
    const cookieToken = response.cookies?.access_token?.[0]?.value;
    if (cookieToken) return cookieToken;
    console.warn('Token not found in cookies');
    return null;
}

// --- Setup and Teardown Functions ---
export function setup() {
    console.log('Starting registration load test...');
    console.log('Test will create random users with unique emails and data.');
    return {};
}

export function teardown(data) {
    console.log('Registration load test completed.');
}
