import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add request interceptor for debugging
api.interceptors.request.use(
  (config) => {
    console.log('API Request:', config.method?.toUpperCase(), config.url);
    return config;
  },
  (error) => {
    console.error('API Request Error:', error);
    return Promise.reject(error);
  }
);

// Add response interceptor for error handling
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    console.error('API Response Error:', error.response?.status, error.response?.data);
    if (error.response?.status === 401 || error.response?.status === 403) {
      console.error('Authentication/Authorization error. User might not be logged in or lacks permissions.');
    }
    return Promise.reject(error);
  }
);

export const patientService = {
  getAll: () => api.get('/patients'),
  getById: (id) => api.get(`/patients/${id}`),
  create: (patient) => api.post('/patients', patient),
  update: (id, patient) => api.put(`/patients/${id}`, patient),
  delete: (id) => api.delete(`/patients/${id}`),
};

export const doctorService = {
  getAll: () => api.get('/doctors'),
  getById: (id) => api.get(`/doctors/${id}`),
  create: (doctor) => api.post('/doctors', doctor),
  update: (id, doctor) => api.put(`/doctors/${id}`, doctor),
  delete: (id) => api.delete(`/doctors/${id}`),
};

export const appointmentService = {
  create: (data) => api.post('/appointments', null, { params: data }),
  getAvailableSlots: (medicalServiceId, date) => 
    api.get('/appointments/available-times', { params: { medicalServiceId, date } }),
  getByPatientId: (patientId) => api.get(`/appointments/patient/${patientId}`),
  submitFeedback: (appointmentId, rating) => 
    api.post(`/appointments/${appointmentId}/feedback`, null, { params: { rating } }),
  delete: (id) => api.delete(`/appointments/${id}`),
  update: (id, appointmentFrom) => api.put(`/appointments/${id}`, null, { params: { appointmentFrom } }),
};

export const propagationService = {
  sync: () => api.post('/propagation/sync'),
  getStats: () => api.get('/propagation/stats'),
};

export const medicalServiceService = {
  getAll: () => api.get('/medical-services'),
};

export const subscriptionPlanService = {
  getAll: () => api.get('/subscription-plans'),
};

export const insuranceProviderService = {
  getAll: () => api.get('/insurance-providers'),
  getById: (id) => api.get(`/insurance-providers/${id}`),
  create: (provider) => api.post('/insurance-providers', provider),
  update: (id, provider) => api.put(`/insurance-providers/${id}`, provider),
  delete: (id) => api.delete(`/insurance-providers/${id}`),
};

export default api;

