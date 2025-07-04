import http from 'k6/http';
// Import 'group' for better reporting
import { check, sleep, group } from 'k6';


// Parameterized URLs
const BASE_URL = __ENV.BASE_URL || 'https://localhost:8080/huddey';
const REGISTRATION_ENDPOINT = `${BASE_URL}/api/v1/auth/basic-auth`;
const LOGIN_ENDPOINT = `${BASE_URL}/api/v1/auth/login`;
const ME_ENDPOINT = `${BASE_URL}/api/v1/auth/me`;

// Test configuration
export let options = {
    insecureSkipTLSVerify: true,
    stages: [
        {duration: '1m', target: 50},
        {duration: '2m', target: 100},
        {duration: '1m', target: 0},
    ],
    thresholds: {
        // Corrected comment to match the value
        http_req_duration: ['p(95)<2000'], // 95% of requests must complete below 2s
        http_req_failed: ['rate<0.1'], // Error rate should be less than 10%
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
 * IMPROVED: Generates a unique email for every iteration of every virtual user.
 * This prevents registration failures due to data collisions during the test.
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

    let loginResponse; // Declare here to be accessible in later groups

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
            // Stop this iteration if registration fails
            return;
        }
    });

    // Simulate user think time between registration and login
    sleep(Math.random() * 2 + 1);

    // Group 2: User Login
    group('2. User Login', function () {
        const loginPayload = { email: userData.email, password: userData.password };
        loginResponse = http.post(LOGIN_ENDPOINT, JSON.stringify(loginPayload), params);

        check(loginResponse, {
            'login successful (status 200)': (r) => r.status === 200,
            // IMPROVED: Robustly check for token in body or as a cookie
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
            // Skip this group if the login request failed
            return;
        }

        let token;
        try {
            token = JSON.parse(loginResponse.body).token;
        } catch (e) {
            token = null;
        }

        // IMPROVED: Fail gracefully if login was 200 but token is missing
        if (!token && (!loginResponse.cookies.access_token || loginResponse.cookies.access_token.length === 0)) {
            console.log(`Login succeeded for ${userData.email} but no token was found in response body or cookies.`);
            return;
        }

        const meHeaders = {
            'Accept': 'application/json',
            'Authorization': `Bearer ${token}`,
        };

        const meResponse = http.get(ME_ENDPOINT, { headers: meHeaders, cookies: loginResponse.cookies });

        check(meResponse, {
            'profile fetch successful (status 200)': (r) => r.status === 200,
            'profile data matches registered user': (r) => {
                try {
                    const profile = JSON.parse(r.body);
                    const actualEmail = profile?.data?.email;
                    if (!actualEmail) {
                        console.warn(`No email found in /me response: ${r.body}`);
                        return false;
                    }
                    return actualEmail.toLowerCase() === userData.email.toLowerCase();
                } catch (e) {
                    console.error(`Failed to parse /me response: ${r.body}`);
                    return false;
                }
            }

        });

    });

    // Final sleep before the next iteration starts
    sleep(Math.random() * 2 + 1);
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