import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('role')
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
      return Promise.reject(new Error(res.message || '未授权'))
    }
    return res
  },
  error => {
    if (error.response) {
      const status = error.response.status
      if (status === 429) {
        ElMessage.error('请求过于频繁，请稍后再试')
      } else if (status === 503) {
        ElMessage.error('服务暂不可用，请稍后重试')
      } else if (status === 500) {
        ElMessage.error('服务器内部错误，请稍后重试')
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else {
        ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络后重试')
    } else {
      ElMessage.error('网络异常，请检查网络连接是否正常')
    }
    return Promise.reject(error)
  }
)

export default request
