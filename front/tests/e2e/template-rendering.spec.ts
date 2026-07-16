import { test, expect } from '@playwright/test';

test.describe('Template rendering (US1)', () => {
  test('renders a published template in block order with SEO metadata', async ({ page }) => {
    await page.goto('/landing/promo');

    await expect(page).toHaveTitle('Big Spring Sale');
    await expect(page.locator('meta[name="description"]')).toHaveAttribute(
      'content',
      'Save big this spring'
    );
    await expect(page.locator('link[rel="canonical"]')).toHaveAttribute(
      'href',
      'https://example.com/promo'
    );
    await expect(page.locator('meta[name="robots"]')).toHaveAttribute('content', 'index,follow');

    const blocks = page.locator('.template-renderer > *');
    await expect(blocks).toHaveCount(2);
    await expect(blocks.nth(0)).toHaveClass(/hero-block/);
    await expect(blocks.nth(1)).toHaveClass(/cta-block/);

    await expect(page.locator('.hero-block__title')).toHaveText('Spring Sale');
    await expect(page.locator('.cta-block__button')).toHaveText('Browse deals');
  });

  test('falls back to the default category layout when no template is mapped', async ({
    page,
  }) => {
    await page.goto('/catalog/widgets');

    await expect(page.locator('.hero-block')).toHaveCount(0);
    await expect(page.locator('h1')).toHaveText('Widgets');
    await expect(page.getByText('Widget One')).toBeVisible();
  });

  test('excludes blocks marked as not visible', async ({ page }) => {
    await page.goto('/landing/promo-hidden');

    await expect(page.locator('.hero-block__title')).toHaveText('Visible Hero');
    await expect(page.locator('.rich-text-block')).toHaveCount(0);
    await expect(page.getByText('Should not appear')).toHaveCount(0);
  });

  test('returns a 404 for an unpublished/missing landing slug', async ({ page }) => {
    const response = await page.goto('/landing/does-not-exist');

    expect(response?.status()).toBe(404);
    await expect(page.locator('h1')).toHaveText('Page not found');
  });
});
