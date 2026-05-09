// Public page metadata
export interface PublicPage {
  slug: string;
  title: string;
  description: string;
  content: string;
  metadata: PageMetadata;
}

// SEO metadata
export interface PageMetadata {
  ogTitle?: string;
  ogDescription?: string;
  ogImage?: string;
  twitterCard?: string;
  twitterTitle?: string;
  twitterDescription?: string;
  canonicalUrl?: string;
}

// i18n content wrapper
export interface I18nContent {
  locale: 'en' | 'es';
  content: Record<string, string>;
  fallback: 'en' | 'es';
}

// Navigation context
export interface NavigationItem {
  label: string;
  href: string;
  children?: NavigationItem[];
}

// Content section (hero, features, testimonials, cta, etc.)
export interface ContentSection {
  type:
    | 'hero'
    | 'features'
    | 'testimonials'
    | 'cta'
    | 'custom'
    | 'faq'
    | 'pricing';
  title: string;
  content: string;
  backgroundColor?: string;
  ctas?: CallToActionButton[];
}

// Call-to-action button
export interface CallToActionButton {
  label: string;
  href: string;
  type?: 'primary' | 'secondary' | 'tertiary';
  icon?: string;
}

// Locale preference
export interface LocaleContext {
  current: 'en' | 'es';
  available: ['en', 'es'];
  default: 'en';
}

// API response from backend
export interface ApiResponse<T = any> {
  data: T;
  status: 'success' | 'error';
  message?: string;
  timestamp: string;
}

// Theme configuration
export interface UITheme {
  primary: string;
  secondary: string;
  spacing: Record<string, string>;
  typography: TypographyConfig;
}

// Typography configuration
export interface TypographyConfig {
  fontFamily: string;
  fontSize: Record<string, string>;
  lineHeight: Record<string, number>;
  fontWeight: Record<string, number>;
}
