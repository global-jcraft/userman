import http from 'k6/http';
import {check, sleep} from 'k6';

// Test configuration
export let options = {
    insecureSkipTLSVerify: true,
    stages: [
        {duration: '1m', target: 100}, // Ramp up to 100 users over 1 minute
        {duration: '2m', target: 500}, // Ramp up to 500 users over 2 minutes
        {duration: '1m', target: 0}, // Ramp down to 0 users
    ],
    thresholds: {
        http_req_duration: ['p(95)<2000'], // 95% of requests must complete below 3s
        http_req_failed: ['rate<0.1'], // Error rate should be less than 10%
    },
};

// Function to generate random string of specified length with given character set
function generateRandomString(length, charset) {
    let result = '';
    for (let i = 0; i < length; i++) {
        result += charset.charAt(Math.floor(Math.random() * charset.length));
    }
    return result;
}

// Function to generate random name (first or last)
function generateName(length) {
    const nameChars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
    let name = nameChars.charAt(Math.floor(Math.random() * 26)).toUpperCase(); // First letter capital
    name += generateRandomString(length - 1, nameChars.toLowerCase());
    return name;
}

// Function to generate random password matching the regex pattern
function generatePassword() {
    const lowercase = 'abcdefghijklmnopqrstuvwxyz';
    const uppercase = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    const numbers = '0123456789';
    const special = '@#!$%^&+=';

    // Ensure at least one character from each required set
    let password = generateRandomString(1, lowercase) +
        generateRandomString(1, uppercase) +
        generateRandomString(1, numbers) +
        generateRandomString(1, special);

    // Add random characters to meet minimum length of 8
    const remainingLength = 4; // 8 - 4 required characters
    const allChars = lowercase + uppercase + numbers + special;
    password += generateRandomString(remainingLength, allChars);

    // Shuffle the password
    return password.split('').sort(() => Math.random() - 0.5).join('');
}

// Function to generate random phone number
function generatePhoneNumber() {
    const countryCode = '+' + (Math.floor(Math.random() * 98) + 1); // Random country code between 1-99
    const length = Math.floor(Math.random() * 7) + 7; // Random length between 8-14 digits
    const number = generateRandomString(length, '0123456789');
    return countryCode + number;
}

// Generate random company name
function generateCompanyName() {
    const prefixes = ['Tech', 'Global', 'Smart', 'Next', 'Digital', 'Innovative', 'Future', 'Advanced'];
    const suffixes = ['Solutions', 'Systems', 'Technologies', 'Innovations', 'Group', 'Industries', 'Services'];
    return prefixes[Math.floor(Math.random() * prefixes.length)] +
        ' ' +
        suffixes[Math.floor(Math.random() * suffixes.length)];
}

// Generate random email with google domain
function generateEmail(firstName) {
    const randomNum = Math.floor(Math.random() * 1000);
    return `${firstName.toLowerCase()}${randomNum}@google.com`;
}

// Function to generate user data
function generateUserData() {
    const firstName = generateName(Math.floor(Math.random() * 5) + 4); // 4-8 characters
    const lastName = generateName(Math.floor(Math.random() * 5) + 4);  // 4-8 characters
    const companyName = generateCompanyName();
    const phoneNumber = generatePhoneNumber();
    const password = generatePassword();
    const email = generateEmail(firstName);

    return {
        firstName,
        lastName,
        companyName,
        phoneNumber,
        password,
        email
    };
}

export default function () {
    // Generate random user data for each virtual user iteration
    const userData = generateUserData();

    // Log generated values for verification
    console.log('Generated user data:', userData.email);

    // Prepare registration payload
    const registrationPayload = {
        firstName: userData.firstName,
        lastName: userData.lastName,
        email: userData.email,
        password: userData.password,
        phoneNumber: userData.phoneNumber,
        companyName: userData.companyName
    };

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
        },
    };

    // Make registration request (replace with your actual registration endpoint)
    const registrationResponse = http.post(
        'https://localhost:8080/huddey/api/v1/auth/basic-auth',
        JSON.stringify(registrationPayload),
        params
    );

    // Check registration response
    // Replace the existing checks with these
    check(registrationResponse, {
        'registration status is 201 or 200': (r) => r.status === 201 || r.status === 200,
        'registration response has valid body': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body !== null && typeof body === 'object';
            } catch (e) {
                return false;
            }
        }
    });

    if (registrationResponse.status === 500) {
        try {
            const errorBody = JSON.parse(registrationResponse.body);
            console.log('Server error details:', JSON.stringify(errorBody, null, 2));
        } catch (e) {
            console.log('Failed to parse error response');
        }
    }

    // Log response for debugging
    console.log(`Registration response status: ${registrationResponse.status}`);
    console.log(`Registration response body: ${registrationResponse.body}`);

    // If registration was successful, you can perform additional actions
    if (registrationResponse.status === 200 || registrationResponse.status === 201) {
        // Optional: Test login with the newly created account
        const loginPayload = {
            email: userData.email,
            password: userData.password
        };

        const loginResponse = http.post(
            'https://localhost:8080/huddey/api/v1/auth/login',
            JSON.stringify(loginPayload),
            params
        );

        check(loginResponse, {
            'login after registration successful': (r) => r.status === 200,
            'login response contains token': (r) => r.body.includes('token') || r.cookies.access_token,
        });

        console.log(`Login response status: ${loginResponse.status}`);
    }
}

// Setup function - runs once before the test starts
export function setup() {
    console.log('Starting registration load test...');
    console.log('Test will create random users with unique emails and data');
    return {};
}

// Teardown function - runs once after the test ends
export function teardown(data) {
    console.log('Registration load test completed');
}