import { v2ApiClient } from './Axios'

export default {
  getNodes() {
    return v2ApiClient.get('/nodes', {
      auth: {
        username: 'admin',
        password: 'admin'
      },
      params: { limit: 500 }
    })
  }
}
