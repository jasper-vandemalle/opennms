import { v2ApiClient } from './Axios'

export default {
  getNodesGraph() {
    return v2ApiClient.get('/graphs/nodes/nodes', {
    })
  }
}
