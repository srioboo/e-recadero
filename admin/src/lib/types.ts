// Admin user information
export interface AdminUser {
  id: string;
  name: string;
  email: string;
  role: 'admin' | 'editor' | 'viewer';
  permissions: string[];
}

// Form field definition
export interface FormField {
  name: string;
  label: string;
  type: 'text' | 'email' | 'password' | 'select' | 'checkbox' | 'textarea';
  required?: boolean;
  validation?: ValidationRule;
}

// Form validation rules
export interface ValidationRule {
  pattern?: RegExp;
  minLength?: number;
  maxLength?: number;
  custom?: (value: any) => boolean | string;
}

// Navigation configuration
export interface NavItem {
  label: string;
  href: string;
  icon?: string;
  children?: NavItem[];
}

// API Response wrapper
export interface ApiResponse<T> {
  data: T;
  status: 'success' | 'error';
  message?: string;
  timestamp: string;
}

// Dashboard statistics
export interface DashboardStats {
  totalUsers: number;
  activeUsers: number;
  lastUpdated: string;
}

// Templates module types
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
  blocks_count?: number;
  blocks?: TemplateBlock[];
  meta?: TemplateMeta;
  created_by?: string;
  created_at?: string;
  published_at?: string | null;
}

export interface TemplateVersion {
  version_id: string;
  template_id: string;
  version_number: number;
  published_at: string;
  created_by?: string;
  change_note?: string | null;
  blocks?: TemplateBlock[];
  meta?: TemplateMeta;
}

export type EntityType = 'PRODUCT' | 'CATEGORY' | 'LANDING_PAGE' | 'USER';
export type MappingStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface PageContentMapping {
  page_content_id: string;
  template_id: string;
  entity_id: string;
  entity_type: EntityType;
  entity_name?: string;
  status: MappingStatus;
  published_at?: string | null;
}

export interface Pagination {
  total: number;
  limit: number;
  offset: number;
}

export interface PaginatedResponse<T> {
  data: T[];
  pagination: Pagination;
}
