import { v2ApiClient } from './Axios'

export default {
  getAlarms() {
    return v2ApiClient.get('/alarms', {
      params: { limit: 1000 }
    })
  }
}
