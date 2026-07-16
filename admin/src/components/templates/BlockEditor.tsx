/** @jsxImportSource react */
import { useState } from 'react';
import type { TemplateBlock, BlockType } from '@/lib/types';
import { removeBlock, setBlockVisibility, reorderBlocks } from '@/lib/api';
import BlockForm from './BlockForm';
import type { BlockFormValue } from './BlockForm';

const BLOCK_TYPES: BlockType[] = [
  'HERO',
  'PRODUCT_GRID',
  'CATEGORY_LIST',
  'FEATURED_PRODUCTS',
  'RICH_TEXT',
  'IMAGE_BANNER',
  'TESTIMONIALS',
  'CTA',
  'HEADER',
  'FOOTER',
];

interface Props {
  templateId: string;
  initialBlocks: TemplateBlock[];
}

type EditingState = { mode: 'new'; blockType: BlockType } | { mode: 'edit'; block: TemplateBlock } | null;

function sortByOrder(blocks: TemplateBlock[]): TemplateBlock[] {
  return [...blocks].sort((a, b) => a.block_order - b.block_order);
}

export default function BlockEditor({ templateId, initialBlocks }: Props) {
  const [blocks, setBlocks] = useState<TemplateBlock[]>(sortByOrder(initialBlocks));
  const [editing, setEditing] = useState<EditingState>(null);
  const [addType, setAddType] = useState<BlockType>('HERO');
  const [conflictWarning, setConflictWarning] = useState(false);
  const [busyBlockId, setBusyBlockId] = useState<string | null>(null);

  function handleConflict() {
    setConflictWarning(true);
  }

  async function handleToggleVisibility(block: TemplateBlock) {
    setBusyBlockId(block.block_id);
    try {
      const result = await setBlockVisibility(templateId, block.block_id, !block.is_visible);
      setBlocks((prev) =>
        prev.map((b) => (b.block_id === block.block_id ? { ...b, is_visible: result.is_visible } : b))
      );
    } catch {
      handleConflict();
    } finally {
      setBusyBlockId(null);
    }
  }

  async function handleRemove(block: TemplateBlock) {
    if (!window.confirm(`Remove block "${block.block_name || block.block_type}"?`)) return;
    setBusyBlockId(block.block_id);
    try {
      await removeBlock(templateId, block.block_id);
      setBlocks((prev) => prev.filter((b) => b.block_id !== block.block_id));
    } catch {
      handleConflict();
    } finally {
      setBusyBlockId(null);
    }
  }

  async function persistOrder(nextBlocks: TemplateBlock[]) {
    setBlocks(nextBlocks);
    try {
      await reorderBlocks(
        templateId,
        nextBlocks.map((b) => ({ blockId: b.block_id, blockOrder: b.block_order }))
      );
    } catch {
      handleConflict();
    }
  }

  // Move-up/move-down buttons are the accessible, keyboard-operable primary
  // interaction (spec.md Assumptions allow this as an equally valid fallback
  // to drag-and-drop for reordering).
  function moveBlock(index: number, direction: -1 | 1) {
    const targetIndex = index + direction;
    if (targetIndex < 0 || targetIndex >= blocks.length) return;

    const reordered = [...blocks];
    [reordered[index], reordered[targetIndex]] = [reordered[targetIndex], reordered[index]];
    const withOrders = reordered.map((b, i) => ({ ...b, block_order: i + 1 }));
    void persistOrder(withOrders);
  }

  function handleSaved(saved: {
    block_id: string;
    block_type: BlockType;
    block_name?: string;
    block_order: number;
    is_visible: boolean;
    content: TemplateBlock['content'];
  }) {
    setBlocks((prev) => {
      const exists = prev.some((b) => b.block_id === saved.block_id);
      const next = exists
        ? prev.map((b) => (b.block_id === saved.block_id ? { ...b, ...saved } : b))
        : sortByOrder([...prev, { ...saved, template_id: templateId }]);
      return sortByOrder(next);
    });
    setEditing(null);
  }

  return (
    <div className="block-editor">
      {conflictWarning && (
        <div className="block-editor__conflict" role="alert">
          Your changes may be out of sync with another admin's edits. Reload the page before
          continuing to avoid overwriting their changes.
          <button type="button" onClick={() => setConflictWarning(false)}>Dismiss</button>
        </div>
      )}

      <ul className="block-editor__list">
        {blocks.map((block, index) => (
          <li key={block.block_id} className="block-editor__item">
            <div className="block-editor__item-header">
              <span className="block-editor__item-type">{block.block_type}</span>
              <span className="block-editor__item-name">{block.block_name}</span>
              {!block.is_visible && <span className="block-editor__hidden-badge">Hidden</span>}
            </div>
            <div className="block-editor__item-actions">
              <button type="button" onClick={() => moveBlock(index, -1)} disabled={index === 0} aria-label="Move up">↑</button>
              <button type="button" onClick={() => moveBlock(index, 1)} disabled={index === blocks.length - 1} aria-label="Move down">↓</button>
              <button
                type="button"
                onClick={() => handleToggleVisibility(block)}
                disabled={busyBlockId === block.block_id}
                aria-label={block.is_visible ? 'Hide block' : 'Show block'}
              >
                {block.is_visible ? 'Hide' : 'Show'}
              </button>
              <button type="button" onClick={() => setEditing({ mode: 'edit', block })}>Edit</button>
              <button
                type="button"
                onClick={() => handleRemove(block)}
                disabled={busyBlockId === block.block_id}
                className="block-editor__remove"
              >
                Remove
              </button>
            </div>
          </li>
        ))}
      </ul>

      {editing && editing.mode === 'edit' && (
        <BlockForm
          templateId={templateId}
          initialValue={{
            blockId: editing.block.block_id,
            blockType: editing.block.block_type,
            blockName: editing.block.block_name || '',
            blockOrder: editing.block.block_order,
            content: editing.block.content as Record<string, unknown>,
          }}
          onSaved={handleSaved}
          onCancel={() => setEditing(null)}
          onConflict={handleConflict}
        />
      )}

      {editing && editing.mode === 'new' && (
        <BlockForm
          templateId={templateId}
          initialValue={{
            blockType: editing.blockType,
            blockName: '',
            blockOrder: blocks.length + 1,
            content: {},
          }}
          onSaved={handleSaved}
          onCancel={() => setEditing(null)}
          onConflict={handleConflict}
        />
      )}

      {!editing && (
        <div className="block-editor__add">
          <select value={addType} onChange={(e) => setAddType(e.target.value as BlockType)}>
            {BLOCK_TYPES.map((type) => (
              <option key={type} value={type}>{type}</option>
            ))}
          </select>
          <button type="button" onClick={() => setEditing({ mode: 'new', blockType: addType })}>
            + Add block
          </button>
        </div>
      )}

      <style>{`
        .block-editor { display: flex; flex-direction: column; gap: 1rem; }
        .block-editor__conflict { background: #fff7ed; border: 1px solid #fdba74; color: #9a3412; border-radius: 6px; padding: 0.75rem 1rem; display: flex; justify-content: space-between; align-items: center; gap: 1rem; }
        .block-editor__conflict button { background: transparent; border: 1px solid #9a3412; border-radius: 4px; padding: 0.25rem 0.6rem; cursor: pointer; }
        .block-editor__list { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 0.5rem; }
        .block-editor__item { display: flex; justify-content: space-between; align-items: center; background: #fff; border: 1px solid #ddd; border-radius: 6px; padding: 0.6rem 0.9rem; gap: 1rem; flex-wrap: wrap; }
        .block-editor__item-header { display: flex; align-items: center; gap: 0.6rem; }
        .block-editor__item-type { font-weight: 700; font-size: 0.75rem; color: #2563eb; text-transform: uppercase; }
        .block-editor__hidden-badge { font-size: 0.7rem; background: #e5e7eb; padding: 0.15rem 0.5rem; border-radius: 999px; }
        .block-editor__item-actions { display: flex; gap: 0.4rem; }
        .block-editor__item-actions button { border: 1px solid #ccc; background: #f9fafb; border-radius: 4px; padding: 0.3rem 0.6rem; cursor: pointer; font-size: 0.85rem; }
        .block-editor__remove { color: #b91c1c; }
        .block-editor__add { display: flex; gap: 0.6rem; align-items: center; }
        .block-editor__add select { padding: 0.4rem 0.6rem; border-radius: 6px; border: 1px solid #ccc; }
        .block-editor__add button { background: #2563eb; color: #fff; border: none; border-radius: 6px; padding: 0.5rem 1rem; font-weight: 600; cursor: pointer; }
      `}</style>
    </div>
  );
}
