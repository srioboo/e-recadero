/** @jsxImportSource react */
import { useState } from 'react';
import type { FormEvent } from 'react';
import type { BlockType, BlockContent } from '@/lib/types';
import { addBlock, updateBlock, InvalidBlockContentError } from '@/lib/api';
import type { BlockContentFieldError } from '@/lib/api';

interface FieldDef {
  key: string;
  label: string;
  kind: 'text' | 'textarea' | 'number' | 'checkbox' | 'select' | 'json';
  options?: string[];
}

// One field set per block type, matching the content shapes in
// specs/003-templates-frontend-admin/data-model.md. List/array fields use a
// JSON textarea to keep the form generic without a bespoke nested-list editor.
const FIELD_DEFS: Record<BlockType, FieldDef[]> = {
  HERO: [
    { key: 'title', label: 'Title', kind: 'text' },
    { key: 'subtitle', label: 'Subtitle', kind: 'text' },
    { key: 'background_image_url', label: 'Background image URL', kind: 'text' },
    { key: 'cta_text', label: 'CTA text', kind: 'text' },
    { key: 'cta_link', label: 'CTA link', kind: 'text' },
  ],
  PRODUCT_GRID: [
    { key: 'products', label: 'Product IDs (JSON array)', kind: 'json' },
    { key: 'layout', label: 'Layout', kind: 'select', options: ['2-columns', '3-columns', '4-columns'] },
    { key: 'show_prices', label: 'Show prices', kind: 'checkbox' },
    { key: 'show_ratings', label: 'Show ratings', kind: 'checkbox' },
  ],
  CATEGORY_LIST: [
    { key: 'category_ids', label: 'Category IDs (JSON array)', kind: 'json' },
    { key: 'items_per_row', label: 'Items per row', kind: 'number' },
    { key: 'show_subcategories', label: 'Show subcategories', kind: 'checkbox' },
  ],
  FEATURED_PRODUCTS: [
    { key: 'products', label: 'Product IDs (JSON array)', kind: 'json' },
    { key: 'title', label: 'Title', kind: 'text' },
    { key: 'layout', label: 'Layout', kind: 'select', options: ['carousel', 'grid'] },
  ],
  RICH_TEXT: [
    { key: 'html_content', label: 'HTML content', kind: 'textarea' },
    { key: 'text_alignment', label: 'Text alignment', kind: 'select', options: ['left', 'center', 'right'] },
    { key: 'background_color', label: 'Background color', kind: 'text' },
  ],
  IMAGE_BANNER: [
    { key: 'image_url', label: 'Image URL', kind: 'text' },
    { key: 'alt_text', label: 'Alt text', kind: 'text' },
    { key: 'link_url', label: 'Link URL', kind: 'text' },
    { key: 'overlay_color', label: 'Overlay color', kind: 'text' },
    { key: 'overlay_opacity', label: 'Overlay opacity (0-1)', kind: 'number' },
  ],
  TESTIMONIALS: [
    { key: 'testimonials', label: 'Testimonials (JSON array of {author, content, rating})', kind: 'json' },
    { key: 'layout', label: 'Layout', kind: 'select', options: ['carousel', 'grid'] },
  ],
  CTA: [
    { key: 'text', label: 'Button text', kind: 'text' },
    { key: 'link', label: 'Link', kind: 'text' },
    { key: 'button_style', label: 'Button style', kind: 'select', options: ['primary', 'secondary'] },
    { key: 'button_size', label: 'Button size', kind: 'select', options: ['small', 'medium', 'large'] },
  ],
  HEADER: [
    { key: 'logo_url', label: 'Logo URL', kind: 'text' },
    { key: 'navigation_links', label: 'Navigation links (JSON array of {label, link})', kind: 'json' },
  ],
  FOOTER: [
    { key: 'company_info', label: 'Company info', kind: 'text' },
    { key: 'links', label: 'Links (JSON array of {label, link})', kind: 'json' },
    { key: 'social_links', label: 'Social links (JSON array of {platform, url})', kind: 'json' },
  ],
};

export interface BlockFormValue {
  blockId?: string;
  blockType: BlockType;
  blockName: string;
  blockOrder: number;
  content: Record<string, unknown>;
}

interface Props {
  templateId: string;
  initialValue: BlockFormValue;
  onSaved: (block: { block_id: string; block_type: BlockType; block_name?: string; block_order: number; is_visible: boolean; content: BlockContent }) => void;
  onCancel: () => void;
  onConflict: () => void;
}

function jsonFieldToString(value: unknown): string {
  if (value === undefined) return '';
  return JSON.stringify(value, null, 2);
}

export default function BlockForm({ templateId, initialValue, onSaved, onCancel, onConflict }: Props) {
  const [blockName, setBlockName] = useState(initialValue.blockName);
  const [fields, setFields] = useState<Record<string, string>>(() => {
    const defs = FIELD_DEFS[initialValue.blockType];
    const initial: Record<string, string> = {};
    for (const def of defs) {
      const raw = (initialValue.content as Record<string, unknown>)[def.key];
      if (def.kind === 'json') {
        initial[def.key] = jsonFieldToString(raw ?? (def.key.endsWith('s') ? [] : {}));
      } else if (def.kind === 'checkbox') {
        initial[def.key] = raw ? 'true' : 'false';
      } else {
        initial[def.key] = raw !== undefined && raw !== null ? String(raw) : '';
      }
    }
    return initial;
  });
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const defs = FIELD_DEFS[initialValue.blockType];

  function updateField(key: string, value: string) {
    setFields((prev) => ({ ...prev, [key]: value }));
  }

  function buildContent(): Record<string, unknown> | null {
    const content: Record<string, unknown> = {};
    const errors: Record<string, string> = {};

    for (const def of defs) {
      const raw = fields[def.key];
      if (def.kind === 'json') {
        if (raw.trim() === '') continue;
        try {
          content[def.key] = JSON.parse(raw);
        } catch {
          errors[def.key] = 'Must be valid JSON';
        }
      } else if (def.kind === 'number') {
        if (raw.trim() === '') continue;
        const num = Number(raw);
        if (Number.isNaN(num)) {
          errors[def.key] = 'Must be a number';
        } else {
          content[def.key] = num;
        }
      } else if (def.kind === 'checkbox') {
        content[def.key] = raw === 'true';
      } else if (raw.trim() !== '') {
        content[def.key] = raw;
      }
    }

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return null;
    }
    setFieldErrors({});
    return content;
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setFormError(null);

    const content = buildContent();
    if (!content) return;

    setSaving(true);
    try {
      const saved = initialValue.blockId
        ? await updateBlock(templateId, initialValue.blockId, {
            content: content as never,
            blockOrder: initialValue.blockOrder,
          })
        : await addBlock(templateId, {
            blockType: initialValue.blockType,
            blockName,
            blockOrder: initialValue.blockOrder,
            content: content as never,
          });
      onSaved(saved);
    } catch (err) {
      if (err instanceof InvalidBlockContentError) {
        const errors: Record<string, string> = {};
        for (const fieldError of err.errors as BlockContentFieldError[]) {
          errors[fieldError.field] = fieldError.issue;
        }
        setFieldErrors(errors);
        setFormError(err.message);
      } else if (err && (err as Error).name === 'UnauthorizedError') {
        setFormError('You are not authorized to edit this template. Please log in as an admin.');
      } else {
        // No documented conflict error code exists for concurrent block edits;
        // treat any other unexpected save failure as a possible stale-data
        // conflict per the Edge Case in spec.md / last-write-wins in research.md.
        onConflict();
        setFormError('Could not save this block. Someone else may have changed it — reload and try again.');
      }
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="block-form">
      {formError && <div className="block-form__error">{formError}</div>}

      {!initialValue.blockId && (
        <label className="block-form__field">
          <span>Block name</span>
          <input
            type="text"
            value={blockName}
            onChange={(e) => setBlockName(e.target.value)}
            required
          />
        </label>
      )}

      {defs.map((def) => (
        <label key={def.key} className="block-form__field">
          <span>{def.label}</span>
          {def.kind === 'textarea' || def.kind === 'json' ? (
            <textarea
              rows={def.kind === 'json' ? 4 : 6}
              value={fields[def.key] ?? ''}
              onChange={(e) => updateField(def.key, e.target.value)}
            />
          ) : def.kind === 'select' ? (
            <select value={fields[def.key] ?? ''} onChange={(e) => updateField(def.key, e.target.value)}>
              <option value="">--</option>
              {def.options?.map((opt) => (
                <option key={opt} value={opt}>{opt}</option>
              ))}
            </select>
          ) : def.kind === 'checkbox' ? (
            <input
              type="checkbox"
              checked={fields[def.key] === 'true'}
              onChange={(e) => updateField(def.key, e.target.checked ? 'true' : 'false')}
            />
          ) : (
            <input
              type={def.kind === 'number' ? 'number' : 'text'}
              value={fields[def.key] ?? ''}
              onChange={(e) => updateField(def.key, e.target.value)}
            />
          )}
          {fieldErrors[def.key] && <span className="block-form__field-error">{fieldErrors[def.key]}</span>}
        </label>
      ))}

      <div className="block-form__actions">
        <button type="submit" disabled={saving}>{saving ? 'Saving…' : 'Save block'}</button>
        <button type="button" onClick={onCancel} disabled={saving}>Cancel</button>
      </div>

      <style>{`
        .block-form { display: flex; flex-direction: column; gap: 1rem; background: #fff; border: 1px solid #ddd; border-radius: 8px; padding: 1.25rem; margin-bottom: 1rem; }
        .block-form__field { display: flex; flex-direction: column; gap: 0.35rem; font-weight: 500; font-size: 0.9rem; }
        .block-form__field input[type='text'], .block-form__field input[type='number'], .block-form__field select, .block-form__field textarea { font: inherit; padding: 0.45rem 0.6rem; border: 1px solid #ccc; border-radius: 6px; font-family: inherit; }
        .block-form__field-error { color: #b91c1c; font-size: 0.8rem; font-weight: normal; }
        .block-form__error { background: #fdecea; color: #b91c1c; border: 1px solid #f5b5b0; border-radius: 6px; padding: 0.6rem 0.8rem; }
        .block-form__actions { display: flex; gap: 0.75rem; }
        .block-form__actions button[type='submit'] { background: #2563eb; color: #fff; border: none; border-radius: 6px; padding: 0.5rem 1.1rem; font-weight: 600; cursor: pointer; }
        .block-form__actions button[type='button'] { background: #e5e7eb; border: none; border-radius: 6px; padding: 0.5rem 1.1rem; cursor: pointer; }
      `}</style>
    </form>
  );
}
