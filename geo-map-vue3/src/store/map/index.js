import AlarmsService from "@/services/AlarmsService.js";
import NodesService from "@/services/NodesService.js";
import GraphService from "@/services/GraphService.js";

export default {
    state: {
        nodesWithCoordinates: [],
        alarms: [],
        interestedNodesID: [],
        edges: []
    },
    mutations: {
        SET_NODES(state, nodes) {
            state.nodesWithCoordinates = nodes
        },
        SET_ALARMS(state, alarms) {
            state.alarms = alarms
        },
        SET_INTERESTED_NODES_ID(state, ids) {
            state.interestedNodesID = ids
        },
        SET_NODE_EDGES(state, edges) {
            state.edges = edges
        }
    },
    actions: {
        fetchNodes({ commit }) {
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
        },
        fetchAlarms({ commit }) {
            return AlarmsService.getAlarms()
                .then(response => {
                    commit("SET_ALARMS", response.data.alarm)
                })
                .catch(error => {
                    throw (error)
                })
        },
        resetInterestedNodesID({ commit, state }) {
            commit("SET_INTERESTED_NODES_ID", state.nodesWithCoordinates.map(node => node.id))
        },
        fetchNodesGraph({ commit }) {
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
    },
    getters: {
        getInterestedNodesID: state => {
            return state.interestedNodesID;
        },
        getInterestedNodes: state => {
            return state.nodesWithCoordinates.filter(node => state.interestedNodesID.includes(node.id));
        },
        getAlarmsFromSelectedNodes: (state, getters) => {
            let selectedNodesLable = getters.getInterestedNodes.map(
                (node) => node.label
            );
            return state.alarms.filter((alarm) =>
                selectedNodesLable.includes(alarm.nodeLabel)
            )
        }
    },
}