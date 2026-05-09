import en from '../../messages/en.json';
import es from '../../messages/es.json';

export type Locale = 'en' | 'es';

const messages: Record<Locale, typeof en> = {
  en,
  es,
};

const DEFAULT_LOCALE: Locale = 'en';
const SUPPORTED_LOCALES: Locale[] = ['en', 'es'];

/**
 * Get the message for a given key and locale
 * @param key Dot-notation key (e.g., "common.language", "admin.dashboard")
 * @param locale Locale code (e.g., "en", "es")
 * @returns Translated string or the key if not found
 */
export function getMessage(key: string, locale: Locale = DEFAULT_LOCALE): string {
  const localeMessages = messages[locale] || messages[DEFAULT_LOCALE];
  const keys = key.split('.');
  let value: any = localeMessages;

  for (const k of keys) {
    if (value && typeof value === 'object' && k in value) {
      value = value[k];
    } else {
      // Fallback: return key if not found
      return key;
    }
  }

  return typeof value === 'string' ? value : key;
}

/**
 * Get all messages for a locale
 * @param locale Locale code
 * @returns Complete messages object
 */
export function getMessages(locale: Locale = DEFAULT_LOCALE): typeof en {
  return messages[locale] || messages[DEFAULT_LOCALE];
}

/**
 * Get the user's preferred locale from localStorage
 * Requires client-side execution (use in client scripts or events)
 * @returns User's preferred locale or default
 */
export function getUserLocale(): Locale {
  if (typeof window === 'undefined') return DEFAULT_LOCALE;
  
  const stored = localStorage.getItem('user_locale');
  if (stored && SUPPORTED_LOCALES.includes(stored as Locale)) {
    return stored as Locale;
  }
  
  // Try browser language
  const browserLang = navigator.language.split('-')[0];
  if (SUPPORTED_LOCALES.includes(browserLang as Locale)) {
    return browserLang as Locale;
  }
  
  return DEFAULT_LOCALE;
}

/**
 * Set the user's preferred locale
 * Requires client-side execution
 * @param locale Locale code
 */
export function setUserLocale(locale: Locale): void {
  if (typeof window !== 'undefined') {
    localStorage.setItem('user_locale', locale);
  }
}

/**
 * Get all supported locales
 * @returns Array of locale codes
 */
export function getSupportedLocales(): Locale[] {
  return SUPPORTED_LOCALES;
}

/**
 * Check if a locale is supported
 * @param locale Locale code to check
 * @returns true if locale is supported
 */
export function isLocaleSupported(locale: string): locale is Locale {
  return SUPPORTED_LOCALES.includes(locale as Locale);
}

export { DEFAULT_LOCALE, SUPPORTED_LOCALES };
