import axios from 'axios'

// Create axios instance with base configuration
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor
request.interceptors.request.use(
  config => {
    // Add authorization token if available
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// Response interceptor
request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      
      // Handle different error status codes
      switch (status) {
        case 401:
          // Unauthorized - redirect to login
          localStorage.removeItem('token')
          window.location.href = '/login'
          break
        case 403:
          console.error('Forbidden:', data.message)
          break
        case 404:
          console.error('Not Found:', data.message)
          break
        case 500:
          console.error('Server Error:', data.message)
          break
        default:
          console.error('HTTP Error:', data.message || error.message)
      }
    } else {
      console.error('Network Error:', error.message)
    }
    
    return Promise.reject(error)
  }
)

export default request