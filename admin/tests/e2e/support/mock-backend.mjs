// Minimal in-memory mock of the backend Templates admin REST API, used only
// to give the e2e suite a deterministic server to hit — no database, no
// Spring Boot process, no auth (the Users/Auth module isn't implemented yet;
// see front/src/lib/storage.ts's getAccessToken note). Started/stopped by
// playwright.config.ts's `webServer` list, ahead of the Astro dev server.
import { createServer } from 'node:http';
import { randomUUID } from 'node:crypto';

const PORT = process.env.MOCK_BACKEND_PORT || 4320;

/** @type {Map<string, any>} */
const templates = new Map();

const CORS_HEADERS = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, POST, PUT, PATCH, DELETE, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type, Authorization',
};

function sendJson(res, status, body) {
  res.writeHead(status, { 'Content-Type': 'application/json', ...CORS_HEADERS });
  res.end(body === undefined ? '' : JSON.stringify(body));
}

function readBody(req) {
  return new Promise((resolve) => {
    let raw = '';
    req.on('data', (chunk) => (raw += chunk));
    req.on('end', () => {
      if (!raw) return resolve({});
      try {
        resolve(JSON.parse(raw));
      } catch {
        resolve({});
      }
    });
  });
}

function serializeTemplate(t) {
  return {
    template_id: t.template_id,
    name: t.name,
    type: t.type,
    slug: t.slug,
    status: t.status,
    version: t.version,
    published_version: t.published_version,
    blocks: t.blocks,
    meta: t.meta,
    created_by: 'e2e-admin',
    created_at: t.created_at,
    published_at: t.published_at,
  };
}

const server = createServer(async (req, res) => {
  const url = new URL(req.url, `http://localhost:${PORT}`);
  const { pathname } = url;
  const method = req.method || 'GET';

  if (method === 'OPTIONS') {
    res.writeHead(204, CORS_HEADERS);
    return res.end();
  }

  // POST /api/v1/templates
  if (method === 'POST' && pathname === '/api/v1/templates') {
    const body = await readBody(req);
    const existing = [...templates.values()].find((t) => t.slug === body.slug);
    if (existing) {
      return sendJson(res, 409, {
        error_code: 'DUPLICATE_SLUG',
        message: 'Template slug must be unique',
        details: { field: 'slug', provided_slug: body.slug },
      });
    }
    const template = {
      template_id: randomUUID(),
      name: body.name,
      type: body.type,
      slug: body.slug,
      status: 'DRAFT',
      version: 1,
      published_version: null,
      blocks: [],
      meta: {},
      versions: [],
      created_at: new Date().toISOString(),
      published_at: null,
    };
    templates.set(template.template_id, template);
    return sendJson(res, 201, serializeTemplate(template));
  }

  const templateMatch = pathname.match(/^\/api\/v1\/templates\/([^/]+)$/);
  if (templateMatch) {
    const template = templates.get(templateMatch[1]);
    if (!template) return sendJson(res, 404, { message: 'Not found' });

    if (method === 'GET') return sendJson(res, 200, serializeTemplate(template));

    if (method === 'PUT') {
      const body = await readBody(req);
      if (body.name) template.name = body.name;
      if (body.meta) template.meta = { ...template.meta, ...body.meta };
      return sendJson(res, 200, serializeTemplate(template));
    }
  }

  const blocksMatch = pathname.match(/^\/api\/v1\/templates\/([^/]+)\/blocks$/);
  if (blocksMatch && method === 'POST') {
    const template = templates.get(blocksMatch[1]);
    if (!template) return sendJson(res, 404, { message: 'Not found' });
    const body = await readBody(req);
    const block = {
      block_id: randomUUID(),
      template_id: template.template_id,
      block_type: body.block_type,
      block_name: body.block_name,
      block_order: body.block_order,
      is_visible: true,
      content: body.content,
      created_at: new Date().toISOString(),
    };
    template.blocks.push(block);
    return sendJson(res, 201, block);
  }

  const reorderMatch = pathname.match(/^\/api\/v1\/templates\/([^/]+)\/blocks\/reorder$/);
  if (reorderMatch && method === 'POST') {
    const template = templates.get(reorderMatch[1]);
    if (!template) return sendJson(res, 404, { message: 'Not found' });
    const body = await readBody(req);
    for (const entry of body.blocks || []) {
      const block = template.blocks.find((b) => b.block_id === entry.block_id);
      if (block) block.block_order = entry.block_order;
    }
    return sendJson(res, 200, { message: 'Reordered' });
  }

  // Note: this generic /blocks/:blockId route must come after the more
  // specific /blocks/reorder route above, since "reorder" would otherwise
  // match :blockId here and shadow it.
  const blockMatch = pathname.match(/^\/api\/v1\/templates\/([^/]+)\/blocks\/([^/]+)$/);
  if (blockMatch) {
    const template = templates.get(blockMatch[1]);
    if (!template) return sendJson(res, 404, { message: 'Not found' });
    const block = template.blocks.find((b) => b.block_id === blockMatch[2]);
    if (!block) return sendJson(res, 404, { message: 'Not found' });

    if (method === 'PUT') {
      const body = await readBody(req);
      if (body.content) block.content = { ...block.content, ...body.content };
      if (body.block_order !== undefined && body.block_order !== null) block.block_order = body.block_order;
      return sendJson(res, 200, block);
    }

    if (method === 'DELETE') {
      template.blocks = template.blocks.filter((b) => b.block_id !== blockMatch[2]);
      return sendJson(res, 204);
    }
  }

  const visibilityMatch = pathname.match(/^\/api\/v1\/templates\/([^/]+)\/blocks\/([^/]+)\/visibility$/);
  if (visibilityMatch && method === 'PATCH') {
    const template = templates.get(visibilityMatch[1]);
    if (!template) return sendJson(res, 404, { message: 'Not found' });
    const block = template.blocks.find((b) => b.block_id === visibilityMatch[2]);
    if (!block) return sendJson(res, 404, { message: 'Not found' });
    const body = await readBody(req);
    block.is_visible = Boolean(body.is_visible);
    return sendJson(res, 200, { block_id: block.block_id, is_visible: block.is_visible });
  }

  const entitiesMatch = pathname.match(/^\/api\/v1\/templates\/([^/]+)\/entities$/);
  if (entitiesMatch && method === 'GET') {
    return sendJson(res, 200, { data: [], pagination: { total: 0, limit: 20, offset: 0 } });
  }

  const publishMatch = pathname.match(/^\/api\/v1\/templates\/([^/]+)\/publish$/);
  if (publishMatch && method === 'POST') {
    const template = templates.get(publishMatch[1]);
    if (!template) return sendJson(res, 404, { message: 'Not found' });
    const body = await readBody(req);
    const versionNumber = template.versions.length + 1;
    template.versions.push({
      version_number: versionNumber,
      blocks: JSON.parse(JSON.stringify(template.blocks)),
      meta: JSON.parse(JSON.stringify(template.meta)),
      published_at: new Date().toISOString(),
      change_note: body.change_note || null,
    });
    template.status = 'PUBLISHED';
    template.version = versionNumber;
    template.published_version = versionNumber;
    template.published_at = new Date().toISOString();
    return sendJson(res, 200, {
      status: 'PUBLISHED',
      version: template.version,
      published_version: template.published_version,
      published_at: template.published_at,
    });
  }

  const unpublishMatch = pathname.match(/^\/api\/v1\/templates\/([^/]+)\/unpublish$/);
  if (unpublishMatch && method === 'POST') {
    const template = templates.get(unpublishMatch[1]);
    if (!template) return sendJson(res, 404, { message: 'Not found' });
    template.status = 'DRAFT';
    return sendJson(res, 200, { status: 'DRAFT', message: 'Template unpublished.' });
  }

  const versionsMatch = pathname.match(/^\/api\/v1\/templates\/([^/]+)\/versions$/);
  if (versionsMatch && method === 'GET') {
    const template = templates.get(versionsMatch[1]);
    if (!template) return sendJson(res, 404, { message: 'Not found' });
    return sendJson(res, 200, {
      data: [...template.versions].reverse(),
      pagination: { total: template.versions.length, limit: 10, offset: 0 },
    });
  }

  const versionMatch = pathname.match(/^\/api\/v1\/templates\/([^/]+)\/versions\/(\d+)$/);
  if (versionMatch && method === 'GET') {
    const template = templates.get(versionMatch[1]);
    if (!template) return sendJson(res, 404, { message: 'Not found' });
    const version = template.versions.find((v) => v.version_number === Number(versionMatch[2]));
    if (!version) return sendJson(res, 404, { message: 'Not found' });
    return sendJson(res, 200, version);
  }

  const revertMatch = pathname.match(/^\/api\/v1\/templates\/([^/]+)\/revert\/(\d+)$/);
  if (revertMatch && method === 'POST') {
    const template = templates.get(revertMatch[1]);
    if (!template) return sendJson(res, 404, { message: 'Not found' });
    const version = template.versions.find((v) => v.version_number === Number(revertMatch[2]));
    if (!version) return sendJson(res, 404, { message: 'Not found' });
    template.blocks = JSON.parse(JSON.stringify(version.blocks));
    template.status = 'DRAFT';
    template.version = template.version + 1;
    return sendJson(res, 200, { status: 'DRAFT', version: template.version, blocks: template.blocks });
  }

  return sendJson(res, 404, { message: 'Not found' });
});

server.listen(PORT, () => {
  console.log(`Admin mock backend listening on http://localhost:${PORT}`);
});
