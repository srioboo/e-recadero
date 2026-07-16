// Minimal in-memory mock of the backend Templates + Catalog REST APIs, used
// only to give the e2e suite a deterministic server to hit — no database,
// no Spring Boot process required. Started/stopped by playwright.config.ts's
// `webServer` list, ahead of the Astro dev server.
import { createServer } from 'node:http';

const PORT = process.env.MOCK_BACKEND_PORT || 4310;

const TEMPLATES = {
  promo: {
    template_id: 'tmpl-promo',
    name: 'Promo Landing',
    type: 'LANDING_PAGE',
    slug: 'promo',
    status: 'PUBLISHED',
    version: 2,
    published_version: 2,
    meta: {
      page_title: 'Big Spring Sale',
      page_description: 'Save big this spring',
      og_title: 'Spring Sale OG',
      og_description: 'OG description',
      canonical_url: 'https://example.com/promo',
      robots_directive: 'index,follow',
    },
    blocks: [
      {
        block_id: 'b1',
        block_type: 'HERO',
        block_order: 1,
        is_visible: true,
        content: {
          title: 'Spring Sale',
          subtitle: 'Up to 50% off',
          cta_text: 'Shop now',
          cta_link: '/catalog',
        },
      },
      {
        block_id: 'b2',
        block_type: 'CTA',
        block_order: 2,
        is_visible: true,
        content: { text: 'Browse deals', link: '/catalog' },
      },
    ],
  },
  'promo-hidden': {
    template_id: 'tmpl-promo-hidden',
    name: 'Promo Landing (hidden block)',
    type: 'LANDING_PAGE',
    slug: 'promo-hidden',
    status: 'PUBLISHED',
    version: 1,
    published_version: 1,
    meta: { page_title: 'Hidden Block Demo' },
    blocks: [
      {
        block_id: 'b1',
        block_type: 'HERO',
        block_order: 1,
        is_visible: true,
        content: { title: 'Visible Hero' },
      },
      {
        block_id: 'b2',
        block_type: 'RICH_TEXT',
        block_order: 2,
        is_visible: false,
        content: { html_content: '<p>Should not appear</p>' },
      },
    ],
  },
};

const CATEGORIES = [
  { id: 'cat-1', name: 'Widgets', slug: 'widgets', description: 'Assorted widgets' },
];

const PRODUCTS = [
  {
    id: 'prod-1',
    sku: 'W-1',
    name: 'Widget One',
    category_id: 'cat-1',
    base_price: 9.99,
    images: [],
  },
];

function sendJson(res, status, body) {
  res.writeHead(status, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify(body));
}

const server = createServer((req, res) => {
  const url = new URL(req.url, `http://localhost:${PORT}`);
  const { pathname, searchParams } = url;

  const templateMatch = pathname.match(/^\/api\/templates\/(.+)$/);
  if (templateMatch) {
    const slug = decodeURIComponent(templateMatch[1]);
    const template = TEMPLATES[slug];
    if (!template) return sendJson(res, 404, { message: 'Not found' });
    return sendJson(res, 200, template);
  }

  if (pathname === '/api/v1/categories') {
    const limit = Number(searchParams.get('limit') || 20);
    const offset = Number(searchParams.get('offset') || 0);
    return sendJson(res, 200, {
      data: CATEGORIES,
      pagination: { total: CATEGORIES.length, limit, offset, has_more: false },
    });
  }

  const categoryDetailMatch = pathname.match(/^\/api\/v1\/categories\/(.+)$/);
  if (categoryDetailMatch) {
    const category = CATEGORIES.find((c) => c.id === categoryDetailMatch[1]);
    if (!category) return sendJson(res, 404, { message: 'Not found' });
    return sendJson(res, 200, category);
  }

  if (pathname === '/api/v1/products') {
    const categoryId = searchParams.get('category_id');
    const limit = Number(searchParams.get('limit') || 20);
    const offset = Number(searchParams.get('offset') || 0);
    const data = categoryId ? PRODUCTS.filter((p) => p.category_id === categoryId) : PRODUCTS;
    return sendJson(res, 200, {
      data,
      pagination: { total: data.length, limit, offset, has_more: false },
    });
  }

  const productAvailabilityMatch = pathname.match(/^\/api\/v1\/products\/(.+)\/availability$/);
  if (productAvailabilityMatch) {
    return sendJson(res, 200, {
      product_id: productAvailabilityMatch[1],
      variants: [],
      timestamp: new Date().toISOString(),
    });
  }

  const productDetailMatch = pathname.match(/^\/api\/v1\/products\/(.+)$/);
  if (productDetailMatch) {
    const product = PRODUCTS.find((p) => p.id === productDetailMatch[1]);
    if (!product) return sendJson(res, 404, { message: 'Not found' });
    return sendJson(res, 200, product);
  }

  return sendJson(res, 404, { message: 'Not found' });
});

server.listen(PORT, () => {
  console.log(`Mock backend listening on http://localhost:${PORT}`);
});
