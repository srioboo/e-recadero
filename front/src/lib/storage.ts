/**
 * Storage utilities for persisting user preferences
 */

const STORAGE_KEYS = {
  LOCALE: 'user_locale',
  THEME: 'user_theme',
  PREFERENCES: 'user_preferences',
} as const;

/**
 * Get a value from localStorage
 * @param key Storage key
 * @param defaultValue Default value if key not found
 * @returns Stored value or default value
 */
export function getStorage<T>(key: string, defaultValue: T): T {
  if (typeof window === 'undefined') return defaultValue;

  try {
    const item = localStorage.getItem(key);
    return item ? JSON.parse(item) : defaultValue;
  } catch (error) {
    console.warn(`Failed to read from localStorage: ${key}`, error);
    return defaultValue;
  }
}

/**
 * Set a value in localStorage
 * @param key Storage key
 * @param value Value to store
 */
export function setStorage<T>(key: string, value: T): void {
  if (typeof window === 'undefined') return;

  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch (error) {
    console.warn(`Failed to write to localStorage: ${key}`, error);
  }
}

/**
 * Remove a value from localStorage
 * @param key Storage key
 */
export function removeStorage(key: string): void {
  if (typeof window === 'undefined') return;

  try {
    localStorage.removeItem(key);
  } catch (error) {
    console.warn(`Failed to remove from localStorage: ${key}`, error);
  }
}

/**
 * Clear all storage
 */
export function clearStorage(): void {
  if (typeof window === 'undefined') return;

  try {
    localStorage.clear();
  } catch (error) {
    console.warn('Failed to clear localStorage', error);
  }
}

export { STORAGE_KEYS };
