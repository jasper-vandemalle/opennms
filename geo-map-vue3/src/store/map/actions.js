import AlarmsService from "@/services/AlarmsService.js";
import NodesService from "@/services/NodesService.js";
import GraphService from "@/services/GraphService.js";

const fetchNodes = ({ commit }) => {
    return NodesService.getNodes()
        .then(response => {
            commit("SET_NODES", response.data.node.filter(
                (node) =>
                  !(
                    node.assetRecord.latitude == null ||
                    node.assetRecord.latitude.length === 0
                  ) &&
                  !(
                    node.assetRecord.longitude == null ||
                    node.assetRecord.longitude.length === 0
                  )
              )),
              commit("SET_INTERESTED_NODES_ID", response.data.node.map(node => node.id))
        })
        .catch(error => {
            throw (error)
        })
}

const fetchAlarms = ({ commit }) => {
    return AlarmsService.getAlarms()
        .then(response => {
            commit("SET_ALARMS", response.data.alarm)
        })
        .catch(error => {
            throw (error)
        })
}

const resetInterestedNodesID = ({ commit, state }) =>{
    commit("SET_INTERESTED_NODES_ID", state.nodesWithCoordinates.map(node => node.id))
}

const fetchNodesGraph = ({ commit }) =>{
    return GraphService.getNodesGraph()
        .then(response => {
            let edges = []
            response.data.edges.forEach((e) => {
                let edge = []
                edge.push(e.source.id)
                edge.push(e.target.id)
                edges.push(edge)
            });
            commit("SET_NODE_EDGES", edges)
        })
        .catch(error => {
            throw (error)
        })
}


export default {
    fetchNodes,
    fetchAlarms,
    resetInterestedNodesID,
    fetchNodesGraph
}