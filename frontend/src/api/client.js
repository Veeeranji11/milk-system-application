const API_BASE = '/api';

export async function apiRequest(endpoint, options = {}) {
  const token = localStorage.getItem('milk_jwt_token');
  const tenantId = localStorage.getItem('milk_tenant_id');

  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  if (tenantId) {
    headers['X-Tenant-ID'] = tenantId;
  }

  const config = {
    ...options,
    headers,
  };

  const response = await fetch(`${API_BASE}${endpoint}`, config);

  if (response.status === 401) {
    // If not on login/register page, clear session
    if (!window.location.pathname.includes('login') && !window.location.pathname.includes('register')) {
      localStorage.removeItem('milk_jwt_token');
      localStorage.removeItem('milk_user_data');
      window.dispatchEvent(new CustomEvent('auth:unauthorized'));
    }
  }

  if (response.status === 402) {
    // 402 Payment Required -> Dispatch global trial expired event to show upgrade dialog
    const errorData = await response.json().catch(() => ({}));
    window.dispatchEvent(new CustomEvent('subscription:expired', { detail: errorData }));
    throw new Error(errorData.message || 'Your subscription has expired. Please upgrade.');
  }

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({ message: response.statusText }));
    throw new Error(errorData.message || 'Request failed');
  }

  // Handle 204 No Content
  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export const api = {
  get: (url) => apiRequest(url, { method: 'GET' }),
  post: (url, data) => apiRequest(url, { method: 'POST', body: JSON.stringify(data) }),
  put: (url, data) => apiRequest(url, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (url) => apiRequest(url, { method: 'DELETE' }),
};
