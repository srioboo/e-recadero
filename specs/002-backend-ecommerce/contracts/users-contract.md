# Users Module Contract

**Module**: Users (Accounts & Profiles)  
**Status**: API Contract  
**Version**: 1.0.0

## Overview

The Users Module manages user authentication, account information, profiles, and address management. It handles user registration, email verification, and role-based access control.

### Responsibilities
- User authentication (JWT token generation)
- User profile management
- Address management (billing/shipping)
- User roles and permissions
- Email verification

---

## REST API Endpoints

### Authentication

#### POST /api/v1/auth/register
Register new user.

**Request**:
```json
{
  "email": "user@example.com",
  "username": "johndoe",
  "password": "SecurePassword123!",
  "first_name": "John",
  "last_name": "Doe"
}
```

**Response** (201 Created):
```json
{
  "user_id": "uuid",
  "email": "user@example.com",
  "username": "johndoe",
  "email_verified": false,
  "message": "Registration successful. Please verify your email."
}
```

#### POST /api/v1/auth/verify-email
Verify email with token.

**Request**:
```json
{
  "token": "email-verification-token-from-email"
}
```

**Response** (200 OK):
```json
{
  "user_id": "uuid",
  "message": "Email verified successfully"
}
```

#### POST /api/v1/auth/login
Authenticate user and get tokens.

**Request**:
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
```

**Response** (200 OK):
```json
{
  "access_token": "eyJhbGc...",
  "refresh_token": "eyJhbGc...",
  "expires_in": 900,
  "user": {
    "user_id": "uuid",
    "email": "user@example.com",
    "username": "johndoe",
    "roles": ["CUSTOMER"],
    "profile": {
      "first_name": "John",
      "last_name": "Doe",
      "avatar_url": "...",
      "preferred_locale": "en"
    }
  }
}
```

#### POST /api/v1/auth/refresh-token
Refresh access token using refresh token.

**Request**:
```json
{
  "refresh_token": "eyJhbGc..."
}
```

**Response** (200 OK):
```json
{
  "access_token": "eyJhbGc...",
  "expires_in": 900
}
```

#### POST /api/v1/auth/logout
Revoke refresh token (invalidate session).

**Response** (200 OK):
```json
{
  "message": "Logged out successfully"
}
```

#### POST /api/v1/auth/forgot-password
Request password reset.

**Request**:
```json
{
  "email": "user@example.com"
}
```

**Response** (200 OK):
```json
{
  "message": "Password reset link sent to your email"
}
```

#### POST /api/v1/auth/reset-password
Reset password with token.

**Request**:
```json
{
  "token": "password-reset-token",
  "new_password": "NewSecurePassword456!"
}
```

**Response** (200 OK):
```json
{
  "message": "Password reset successfully"
}
```

---

### User Profile

#### GET /api/v1/users/me
Get current logged-in user profile.

**Response** (200 OK):
```json
{
  "user": {
    "user_id": "uuid",
    "email": "user@example.com",
    "username": "johndoe",
    "status": "ACTIVE",
    "email_verified": true,
    "phone_verified": false,
    "created_at": "2026-05-01T10:00:00Z"
  },
  "profile": {
    "first_name": "John",
    "last_name": "Doe",
    "phone": "+1-234-567-8900",
    "avatar_url": "http://cdn.example.com/avatars/uuid",
    "bio": "E-commerce enthusiast",
    "preferred_locale": "en",
    "preferred_currency": "USD",
    "newsletter_subscribed": true
  }
}
```

#### PUT /api/v1/users/me
Update user profile.

**Request**:
```json
{
  "profile": {
    "first_name": "John",
    "last_name": "Doe",
    "phone": "+1-234-567-8900",
    "bio": "Updated bio",
    "avatar_url": "http://cdn.example.com/avatars/new-uuid",
    "preferred_locale": "es",
    "newsletter_subscribed": false
  }
}
```

**Response** (200 OK): Updated user profile object

#### PUT /api/v1/users/me/password
Change password (authenticated user).

**Request**:
```json
{
  "current_password": "OldPassword123!",
  "new_password": "NewPassword456!"
}
```

**Response** (200 OK):
```json
{
  "message": "Password changed successfully"
}
```

#### DELETE /api/v1/users/me
Deactivate user account.

**Request**:
```json
{
  "password": "ConfirmPassword123!",
  "reason": "Taking a break"
}
```

**Response** (204 No Content)

---

### User Addresses

#### GET /api/v1/users/me/addresses
List user addresses.

**Query Parameters**:
```
- type: BILLING|SHIPPING|OTHER (optional, filter by type)
```

**Response** (200 OK):
```json
{
  "data": [
    {
      "address_id": "uuid-1",
      "type": "BILLING",
      "street_address": "123 Main St",
      "street_address_2": "Apt 4B",
      "city": "New York",
      "state_province": "NY",
      "postal_code": "10001",
      "country_code": "US",
      "is_primary": true,
      "created_at": "2026-05-01T10:00:00Z"
    },
    {
      "address_id": "uuid-2",
      "type": "SHIPPING",
      "street_address": "456 Oak Ave",
      "city": "Los Angeles",
      "state_province": "CA",
      "postal_code": "90001",
      "country_code": "US",
      "is_primary": true,
      "created_at": "2026-05-02T10:00:00Z"
    }
  ]
}
```

#### POST /api/v1/users/me/addresses
Create new address.

**Request**:
```json
{
  "type": "SHIPPING",
  "street_address": "789 Pine Rd",
  "city": "Chicago",
  "state_province": "IL",
  "postal_code": "60601",
  "country_code": "US",
  "is_primary": false
}
```

**Response** (201 Created):
```json
{
  "address_id": "uuid-3",
  "type": "SHIPPING",
  "street_address": "789 Pine Rd",
  "city": "Chicago",
  "state_province": "IL",
  "postal_code": "60601",
  "country_code": "US",
  "is_primary": false,
  "created_at": "2026-05-09T15:00:00Z"
}
```

#### PUT /api/v1/users/me/addresses/{address_id}
Update address.

**Response** (200 OK): Updated address object

#### DELETE /api/v1/users/me/addresses/{address_id}
Delete address.

**Response** (204 No Content)

#### PUT /api/v1/users/me/addresses/{address_id}/set-primary
Set address as primary for its type.

**Response** (200 OK):
```json
{
  "message": "Address set as primary"
}
```

---

### Admin User Management

#### GET /api/v1/admin/users
List users (admin only).

**Query Parameters**:
```
- status: ACTIVE|INACTIVE|SUSPENDED|DELETED (optional)
- role: CUSTOMER|ADMIN|VENDOR (optional)
- created_from: ISO date (optional)
- created_to: ISO date (optional)
- limit: int (1-100, default 20)
- offset: int (default 0)
```

**Response** (200 OK):
```json
{
  "data": [
    {
      "user_id": "uuid",
      "email": "user@example.com",
      "username": "johndoe",
      "status": "ACTIVE",
      "roles": ["CUSTOMER"],
      "created_at": "2026-05-01T10:00:00Z",
      "last_login_at": "2026-05-09T08:30:00Z"
    }
  ],
  "pagination": { "total": 5000, "limit": 20, "offset": 0 }
}
```

#### GET /api/v1/admin/users/{user_id}
Get user details (admin only).

**Response** (200 OK): User object with profile and addresses

#### PATCH /api/v1/admin/users/{user_id}/status
Change user status (admin only).

**Request**:
```json
{
  "status": "SUSPENDED",
  "reason": "Violation of terms"
}
```

**Response** (200 OK): Updated user object

#### POST /api/v1/admin/users/{user_id}/roles
Add role to user (admin only).

**Request**:
```json
{
  "role_name": "VENDOR"
}
```

**Response** (201 Created):
```json
{
  "user_id": "uuid",
  "roles": ["CUSTOMER", "VENDOR"]
}
```

#### DELETE /api/v1/admin/users/{user_id}/roles/{role_name}
Remove role from user (admin only).

**Response** (204 No Content)

---

## Domain Events

### UserRegistered
Emitted when user completes registration.

```json
{
  "event_type": "UserRegistered",
  "event_id": "uuid",
  "aggregate_id": "user-uuid",
  "timestamp": "2026-05-09T15:00:00Z",
  "data": {
    "user_id": "uuid",
    "email": "user@example.com",
    "username": "johndoe"
  }
}
```

### UserEmailVerified
Emitted when user verifies email.

```json
{
  "event_type": "UserEmailVerified",
  "event_id": "uuid",
  "aggregate_id": "user-uuid",
  "timestamp": "2026-05-09T15:00:00Z",
  "data": {
    "user_id": "uuid",
    "email": "user@example.com"
  }
}
```

### UserProfileUpdated
Emitted when user updates profile.

```json
{
  "event_type": "UserProfileUpdated",
  "event_id": "uuid",
  "aggregate_id": "user-uuid",
  "timestamp": "2026-05-09T15:00:00Z",
  "data": {
    "user_id": "uuid",
    "fields_changed": ["first_name", "preferred_locale"]
  }
}
```

---

## Error Responses

### 400 Bad Request - User Already Exists
```json
{
  "error_code": "USER_ALREADY_EXISTS",
  "message": "Email already registered",
  "details": {
    "field": "email"
  }
}
```

### 401 Unauthorized - Invalid Credentials
```json
{
  "error_code": "INVALID_CREDENTIALS",
  "message": "Email or password incorrect"
}
```

### 409 Conflict - Invalid State Transition
```json
{
  "error_code": "INVALID_STATUS_TRANSITION",
  "message": "Cannot change password for SUSPENDED user",
  "details": {
    "current_status": "SUSPENDED"
  }
}
```

---

## Authentication & Authorization

- **Registration**: Public (no auth required)
- **Login/Refresh**: Public (no auth required)
- **Profile endpoints**: Requires CUSTOMER role
- **Admin endpoints**: Requires ADMIN role
- **Token expiration**: 15 minutes (access), 7 days (refresh)

---

## Performance SLAs

- POST /api/v1/auth/login: < 200ms
- GET /api/v1/users/me: < 100ms
- GET /api/v1/users/me/addresses: < 100ms

---

## Cross-Module Dependencies

- **Cart Module**: Queries to verify user_id validity
- **Orders Module**: User email/profile for order confirmations
- **Email Service**: Subscribes to UserRegistered, UserEmailVerified events
