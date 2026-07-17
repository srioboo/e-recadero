package org.sirantar.recadero.templates.domain;

/**
 * The supported content block types, each with its own content JSON shape.
 * See specs/002-backend-ecommerce/contracts/templates-contract.md.
 */
public enum BlockType {
  HERO,
  PRODUCT_GRID,
  CATEGORY_LIST,
  FEATURED_PRODUCTS,
  RICH_TEXT,
  IMAGE_BANNER,
  TESTIMONIALS,
  CTA,
  HEADER,
  FOOTER
}
