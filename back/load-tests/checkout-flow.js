// T183: checkout flow load test.
//
// Simulates 100 concurrent users each adding 5 items to their cart, with 50%
// applying a coupon and 30% completing checkout — per specs/002-backend-ecommerce/tasks.md.
//
// Not run in this environment (no live server/staging deployment available).
// Run against a real deployment with:
//   BASE_URL=https://staging.example.com k6 run checkout-flow.js
//
// Requires a seeded product catalog and a way to obtain a JWT per virtual
// user; TEST_USER_EMAIL/TEST_USER_PASSWORD below assume a pre-provisioned
// load-test account, or adapt registerAndLogin() to register fresh users.

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api/v1';
const TEST_USER_EMAIL = __ENV.TEST_USER_EMAIL || 'loadtest@example.com';
const TEST_USER_PASSWORD = __ENV.TEST_USER_PASSWORD || 'LoadTest123!';
const COUPON_CODE = __ENV.COUPON_CODE || 'SUMMER20';

const checkoutDuration = new Trend('checkout_duration', true);

export const options = {
  scenarios: {
    checkout_flow: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 100 },
        { duration: '2m', target: 100 },
        { duration: '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    // GET endpoints: p95 < 200ms
    'http_req_duration{endpoint:get}': ['p(95)<200'],
    // POST /orders (checkout confirm): p95 < 1s
    'checkout_duration': ['p(95)<1000'],
    'http_req_failed': ['rate<0.01'],
  },
};

function authHeaders(token) {
  return { headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' } };
}

function login() {
  const res = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({ email: TEST_USER_EMAIL, password: TEST_USER_PASSWORD }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  check(res, { 'login succeeded': (r) => r.status === 200 });
  return res.json('access_token');
}

export default function () {
  const token = login();
  if (!token) {
    return;
  }
  const opts = authHeaders(token);

  // Browse products (GET, tagged for the p95<200ms threshold).
  const products = http.get(`${BASE_URL}/products?page=0&size=20`, {
    ...opts,
    tags: { endpoint: 'get' },
  });
  check(products, { 'list products 200': (r) => r.status === 200 });

  const variantIds = (products.json('content') || [])
    .flatMap((p) => (p.variants || []).map((v) => v.id))
    .filter(Boolean);

  // Add up to 5 items to the cart.
  const itemsToAdd = variantIds.slice(0, 5);
  for (const variantId of itemsToAdd) {
    const res = http.post(
      `${BASE_URL}/cart/items`,
      JSON.stringify({ productVariantId: variantId, quantity: 1 }),
      opts,
    );
    check(res, { 'add item 200/201': (r) => r.status === 200 || r.status === 201 });
    sleep(0.2);
  }

  // 50% apply a coupon.
  if (Math.random() < 0.5) {
    const res = http.post(
      `${BASE_URL}/cart/apply-coupon`,
      JSON.stringify({ couponCode: COUPON_CODE }),
      opts,
    );
    check(res, { 'apply coupon 200 or expected failure': (r) => r.status < 500 });
  }

  // 30% complete checkout.
  if (Math.random() < 0.3) {
    const prepare = http.post(`${BASE_URL}/cart/prepare-checkout`, null, opts);
    check(prepare, { 'prepare checkout 200': (r) => r.status === 200 });

    const start = Date.now();
    const confirm = http.post(
      `${BASE_URL}/cart/confirm-checkout`,
      JSON.stringify({ paymentMethod: 'CREDIT_CARD' }),
      opts,
    );
    checkoutDuration.add(Date.now() - start);
    check(confirm, { 'confirm checkout 200/201': (r) => r.status === 200 || r.status === 201 });
  }

  sleep(1);
}
