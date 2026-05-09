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
