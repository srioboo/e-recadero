import { test, expect } from '@playwright/test';

test('admin authors a template end-to-end: create -> add/reorder/hide blocks -> publish -> revert', async ({
  page,
}) => {
  const slug = `e2e-landing-${Date.now()}`;

  await test.step('create a template', async () => {
    await page.goto('/templates/new');
    await page.getByLabel('Name').fill('E2E Test Landing');
    await page.getByLabel('Type').selectOption('LANDING_PAGE');
    await page.getByLabel('Slug').fill(slug);
    await page.getByRole('button', { name: 'Create template' }).click();

    await expect(page).toHaveURL(/\/templates\/[^/]+\/edit$/);
    await expect(page.locator('.edit-template__badge')).toHaveText('DRAFT');

    // BlockEditor is a `client:load` React island — wait for its JS bundle to
    // load and hydrate before interacting, otherwise clicks land on inert
    // server-rendered markup.
    await page.waitForLoadState('networkidle');
  });

  await test.step('add three blocks of different types', async () => {
    // Block 1: HERO (default selection in the "add block" dropdown)
    await page.getByRole('button', { name: '+ Add block' }).click();
    await page.getByLabel('Block name').fill('Main hero');
    await page.getByLabel('Title', { exact: true }).fill('Welcome');
    await page.getByRole('button', { name: 'Save block' }).click();
    await expect(page.locator('.block-editor__item')).toHaveCount(1);

    // Block 2: CTA
    await page.locator('.block-editor__add select').selectOption('CTA');
    await page.getByRole('button', { name: '+ Add block' }).click();
    await page.getByLabel('Block name').fill('Bottom CTA');
    await page.getByLabel('Button text').fill('Shop now');
    await page.getByLabel('Link').fill('/catalog');
    await page.getByRole('button', { name: 'Save block' }).click();
    await expect(page.locator('.block-editor__item')).toHaveCount(2);

    // Block 3: RICH_TEXT
    await page.locator('.block-editor__add select').selectOption('RICH_TEXT');
    await page.getByRole('button', { name: '+ Add block' }).click();
    await page.getByLabel('Block name').fill('Details');
    await page.getByLabel('HTML content').fill('<p>More info</p>');
    await page.getByRole('button', { name: 'Save block' }).click();
    await expect(page.locator('.block-editor__item')).toHaveCount(3);

    await expect(page.locator('.block-editor__item-type')).toHaveText(['HERO', 'CTA', 'RICH_TEXT']);
  });

  await test.step('reorder blocks (move RICH_TEXT up above CTA)', async () => {
    const items = page.locator('.block-editor__item');
    await items.nth(2).getByRole('button', { name: 'Move up' }).click();

    await expect(page.locator('.block-editor__item-type')).toHaveText(['HERO', 'RICH_TEXT', 'CTA']);
  });

  await test.step('hide the CTA block', async () => {
    const ctaItem = page.locator('.block-editor__item').nth(2);
    await expect(ctaItem.locator('.block-editor__item-type')).toHaveText('CTA');
    await ctaItem.getByRole('button', { name: 'Hide block' }).click();

    await expect(ctaItem.locator('.block-editor__hidden-badge')).toHaveText('Hidden');
  });

  await test.step('reloading confirms the block changes persisted server-side', async () => {
    await page.reload();

    await expect(page.locator('.block-editor__item-type')).toHaveText(['HERO', 'RICH_TEXT', 'CTA']);
    await expect(page.locator('.block-editor__item').nth(2).locator('.block-editor__hidden-badge')).toHaveText(
      'Hidden'
    );
  });

  await test.step('publish the template', async () => {
    await page.getByRole('button', { name: 'Publish' }).click();

    await expect(page.locator('.edit-template__badge')).toHaveText('PUBLISHED');
    await expect(page.getByText('Template published.')).toBeVisible();
  });

  await test.step('view version history and revert to v1', async () => {
    await page.getByRole('link', { name: 'Version history →' }).click();
    await expect(page).toHaveURL(/\/versions$/);

    const versionItem = page.locator('.version-history__item').first();
    await expect(versionItem.locator('strong')).toHaveText('v1');

    await versionItem.getByPlaceholder('Why revert to this version?').fill('Rolling back for e2e test');
    await versionItem.getByRole('button', { name: 'Revert to this version' }).click();

    await expect(page).toHaveURL(/\/templates\/[^/]+\/edit$/);
    await expect(page.locator('.edit-template__badge')).toHaveText('DRAFT');
    await expect(page.locator('.block-editor__item-type')).toHaveText(['HERO', 'RICH_TEXT', 'CTA']);
  });
});
