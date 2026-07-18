// T184: product search load test.
//
// Simulates 500 concurrent product searches with varying queries/filters —
// per specs/002-backend-ecommerce/tasks.md. Target: p95 < 500ms.
//
// Not run in this environment (no live server/staging deployment available).
// Run against a real deployment with:
//   BASE_URL=https://staging.example.com k6 run product-search.js

import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api/v1';

const SEARCH_TERMS = ['shirt', 'shoes', 'jacket', 'bag', 'watch', 'hat', 'socks', 'jeans'];
const CATEGORY_IDS = (__ENV.CATEGORY_IDS || '').split(',').filter(Boolean);

export const options = {
  scenarios: {
    product_search: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 500 },
        { duration: '2m', target: 500 },
        { duration: '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
};

function randomItem(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

export default function () {
  const term = randomItem(SEARCH_TERMS);
  const params = new URLSearchParams({ q: term, page: '0', size: '20' });

  if (CATEGORY_IDS.length > 0 && Math.random() < 0.4) {
    params.set('categoryId', randomItem(CATEGORY_IDS));
  }
  if (Math.random() < 0.3) {
    params.set('minPrice', '10');
    params.set('maxPrice', '100');
  }

  const res = http.get(`${BASE_URL}/products?${params.toString()}`);
  check(res, {
    'search 200': (r) => r.status === 200,
    'has content array': (r) => Array.isArray(r.json('content')),
  });
}
