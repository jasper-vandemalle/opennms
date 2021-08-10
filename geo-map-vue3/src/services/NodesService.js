import { v2ApiClient } from './Axios'

export default {
  getNodes() {
    return v2ApiClient.get('/nodes', {
      params: { limit: 500 }
    })
  }
}
