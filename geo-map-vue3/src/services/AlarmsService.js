import { v2ApiClient } from './Axios'

export default {
  getAlarms() {
    return v2ApiClient.get('/alarms', {
      auth: {
        username: 'admin',
        password: 'admin'
      }
    })
  }
}
