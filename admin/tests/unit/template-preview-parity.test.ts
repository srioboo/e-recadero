import { readFileSync } from 'node:fs';
import path from 'node:path';
import { describe, it, expect } from 'vitest';

const FRONT_BLOCKS_DIR = path.resolve(__dirname, '../../../front/src/components/templates');
const ADMIN_PREVIEW_DIR = path.resolve(__dirname, '../../src/components/templates/preview');

// Every block type supported by TemplateRenderer (front) must have a matching
// preview counterpart in admin — SC-003 requires their rendered markup to
// stay structurally equivalent (see T029/T048).
const BLOCK_FILES = [
  'HeroBlock.astro',
  'ProductGridBlock.astro',
  'CategoryListBlock.astro',
  'FeaturedProductsBlock.astro',
  'RichTextBlock.astro',
  'ImageBannerBlock.astro',
  'TestimonialsBlock.astro',
  'CtaBlock.astro',
  'HeaderBlock.astro',
  'FooterBlock.astro',
];

/** Strips the Astro frontmatter (between the two leading `---` fences), leaving template + style markup. */
function templateBody(source: string): string {
  const parts = source.split(/^---$/m);
  // parts[0] is empty (before the first ---), parts[1] is frontmatter, parts[2+] is the rest
  return parts.slice(2).join('---').trim();
}

function readBlock(dir: string, file: string): string {
  return readFileSync(path.join(dir, file), 'utf-8');
}

/** First `<section class="...">` (or `<header>`/`<footer>`) root wrapper opening tag, ignoring dynamic attributes like `style`. */
function rootWrapperClass(markup: string): string | null {
  const match = markup.match(/<(section|header|footer)\s+class="([\w-]+)"/);
  return match ? `${match[1]}.${match[2]}` : null;
}

/** The collection expression a block iterates over, e.g. `products.map(` or `testimonials.map(`. */
function iterationSources(markup: string): string[] {
  return [...markup.matchAll(/(\w+)\.map\(/g)].map((m) => m[1]).sort();
}

/** Class of the "no content" empty-state message, when the block has one. */
function emptyStateClass(markup: string): string | null {
  const match = markup.match(/<p class="([\w-]+__empty)">/);
  return match ? match[1] : null;
}

describe('admin preview / front block markup parity (SC-003)', () => {
  it.each(BLOCK_FILES)('%s exists in both front and admin preview', (file) => {
    expect(() => readBlock(FRONT_BLOCKS_DIR, file)).not.toThrow();
    expect(() => readBlock(ADMIN_PREVIEW_DIR, file)).not.toThrow();
  });

  it.each(BLOCK_FILES)('%s: root wrapper element/class matches', (file) => {
    const front = templateBody(readBlock(FRONT_BLOCKS_DIR, file));
    const admin = templateBody(readBlock(ADMIN_PREVIEW_DIR, file));

    const frontRoot = rootWrapperClass(front);
    const adminRoot = rootWrapperClass(admin);

    expect(frontRoot).not.toBeNull();
    expect(adminRoot).toBe(frontRoot);
  });

  it.each(BLOCK_FILES)('%s: empty-state markup matches when present', (file) => {
    const front = templateBody(readBlock(FRONT_BLOCKS_DIR, file));
    const admin = templateBody(readBlock(ADMIN_PREVIEW_DIR, file));

    expect(emptyStateClass(admin)).toBe(emptyStateClass(front));
  });

  // ProductGridBlock and CategoryListBlock get dedicated iteration checks below —
  // front's ProductGridBlock delegates iteration to a shared `<ProductGrid>`
  // component (no `.map` of its own), and CategoryListBlock's admin preview
  // intentionally omits the nested subcategory list.
  const GENERIC_ITERATION_BLOCKS = BLOCK_FILES.filter(
    (file) => file !== 'ProductGridBlock.astro' && file !== 'CategoryListBlock.astro'
  );

  it.each(GENERIC_ITERATION_BLOCKS)('%s: iterates the same resolved collection(s)', (file) => {
    const front = templateBody(readBlock(FRONT_BLOCKS_DIR, file));
    const admin = templateBody(readBlock(ADMIN_PREVIEW_DIR, file));

    expect(iterationSources(admin)).toEqual(iterationSources(front));
  });

  it('ProductGridBlock.astro: front delegates to the shared ProductGrid component, admin inlines the same collection', () => {
    const front = templateBody(readBlock(FRONT_BLOCKS_DIR, 'ProductGridBlock.astro'));
    const admin = templateBody(readBlock(ADMIN_PREVIEW_DIR, 'ProductGridBlock.astro'));

    expect(front).toContain('<ProductGrid products={products}');
    expect(iterationSources(admin)).toEqual(['products']);
  });

  it('CategoryListBlock.astro: admin preview omits the nested subcategory list front renders', () => {
    const front = templateBody(readBlock(FRONT_BLOCKS_DIR, 'CategoryListBlock.astro'));
    const admin = templateBody(readBlock(ADMIN_PREVIEW_DIR, 'CategoryListBlock.astro'));

    expect(iterationSources(front)).toEqual(['categories', 'children']);
    expect(iterationSources(admin)).toEqual(['categories']);
  });

  // Blocks with no catalog-backed card rendering: admin's preview reuses the
  // exact same markup as front's (verified byte-for-byte), since neither side
  // needs a simplified stand-in for a shared catalog component.
  const IDENTICAL_MARKUP_BLOCKS = [
    'HeroBlock.astro',
    'RichTextBlock.astro',
    'ImageBannerBlock.astro',
    'TestimonialsBlock.astro',
    'CtaBlock.astro',
    'HeaderBlock.astro',
    'FooterBlock.astro',
  ];

  it.each(IDENTICAL_MARKUP_BLOCKS)('%s: template + style markup is identical', (file) => {
    const front = templateBody(readBlock(FRONT_BLOCKS_DIR, file));
    const admin = templateBody(readBlock(ADMIN_PREVIEW_DIR, file));

    expect(admin).toBe(front);
  });

  // These three delegate per-item card rendering to shared `front`-only catalog
  // components (ProductGrid/ProductCard) that admin doesn't have access to, so
  // admin's preview inlines a simplified equivalent card instead of importing
  // them — the outer block/empty/iteration structure above still stays in
  // parity, but the per-item content is intentionally simplified.
  const SIMPLIFIED_CARD_BLOCKS: Record<string, string[]> = {
    'ProductGridBlock.astro': ['product.name', 'product.base_price'],
    'CategoryListBlock.astro': ['category.name'],
    'FeaturedProductsBlock.astro': ['product.name'],
  };

  it.each(Object.entries(SIMPLIFIED_CARD_BLOCKS))(
    '%s: per-item card still surfaces the same key content fields',
    (file, fields) => {
      const admin = templateBody(readBlock(ADMIN_PREVIEW_DIR, file));

      for (const field of fields) {
        expect(admin).toContain(`{${field}}`);
      }
    }
  );
});
