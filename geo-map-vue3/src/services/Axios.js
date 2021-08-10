import axios from 'axios'

const v2ApiClient = axios.create({
  baseURL: 'http://localhost:8980/opennms/api/v2/',
  withCredentials: true,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json'
  },
  auth: {
    username: 'admin',
    password: 'admin'
  },
})

export {
  v2ApiClient
}