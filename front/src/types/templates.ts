// Templates TypeScript interfaces
// Based on specs/002-backend-ecommerce/contracts/templates-contract.md

export type TemplateType = 'LANDING_PAGE' | 'CATEGORY_PAGE' | 'PRODUCT_PAGE' | 'CUSTOM';
export type TemplateStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export type BlockType =
  | 'HERO'
  | 'PRODUCT_GRID'
  | 'CATEGORY_LIST'
  | 'FEATURED_PRODUCTS'
  | 'RICH_TEXT'
  | 'IMAGE_BANNER'
  | 'TESTIMONIALS'
  | 'CTA'
  | 'HEADER'
  | 'FOOTER';

export interface HeroContent {
  title: string;
  subtitle?: string;
  background_image_url?: string;
  cta_text?: string;
  cta_link?: string;
}

export interface ProductGridContent {
  products: string[];
  layout?: '2-columns' | '3-columns' | '4-columns';
  show_prices?: boolean;
  show_ratings?: boolean;
}

export interface CategoryListContent {
  category_ids: string[];
  items_per_row?: number;
  show_subcategories?: boolean;
}

export interface FeaturedProductsContent {
  products: string[];
  title?: string;
  layout?: 'carousel' | 'grid';
}

export interface RichTextContent {
  html_content: string;
  text_alignment?: 'left' | 'center' | 'right';
  background_color?: string;
}

export interface ImageBannerContent {
  image_url: string;
  alt_text?: string;
  link_url?: string;
  overlay_color?: string;
  overlay_opacity?: number;
}

export interface Testimonial {
  author: string;
  content: string;
  rating?: number;
}

export interface TestimonialsContent {
  testimonials: Testimonial[];
  layout?: 'carousel' | 'grid';
}

export interface CtaContent {
  text: string;
  link: string;
  button_style?: 'primary' | 'secondary';
  button_size?: 'small' | 'medium' | 'large';
}

export interface NavigationLink {
  label: string;
  link: string;
}

export interface HeaderContent {
  logo_url?: string;
  navigation_links?: NavigationLink[];
}

export interface FooterContent {
  company_info?: string;
  links?: NavigationLink[];
  social_links?: { platform: string; url: string }[];
}

export type BlockContent =
  | HeroContent
  | ProductGridContent
  | CategoryListContent
  | FeaturedProductsContent
  | RichTextContent
  | ImageBannerContent
  | TestimonialsContent
  | CtaContent
  | HeaderContent
  | FooterContent;

export interface TemplateBlock {
  block_id: string;
  template_id?: string;
  block_type: BlockType;
  block_name?: string;
  block_order: number;
  is_visible: boolean;
  content: BlockContent;
  created_at?: string;
}

export interface TemplateMeta {
  page_title?: string;
  page_description?: string;
  og_title?: string;
  og_description?: string;
  og_image_url?: string;
  keywords?: string;
  canonical_url?: string;
  robots_directive?: string;
  structured_data_json?: Record<string, unknown>;
}

export interface Template {
  template_id: string;
  name: string;
  type: TemplateType;
  slug: string;
  status: TemplateStatus;
  version: number;
  published_version: number | null;
  blocks?: TemplateBlock[];
  meta?: TemplateMeta;
  created_by?: string;
  created_at?: string;
  published_at?: string | null;
}
