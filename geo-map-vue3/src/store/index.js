import { createStore } from "vuex";
import AlarmsService from "@/services/AlarmsService.js";
import NodesService from "@/services/NodesService.js";

export default createStore({
  state: {
    nodes: [],
    alarms: [],
    selectedNodesID: []
  },
  mutations: {
    SET_NODES(state, nodes) {
      state.nodes = nodes
    },
    SET_ALARMS(state, alarms) {
      state.alarms = alarms
    },
    SET_SELECTED_NODES_ID(state, ids){
      state.selectedNodesID = ids
    }
  },
  actions: {
    fetchNodes({ commit }) {
      return NodesService.getNodes()
        .then(response => {
          commit("SET_NODES", response.data.node),
          commit("SET_SELECTED_NODES_ID", response.data.node.map(node => node.id))
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
    }
  },
  getters: {
    getSelectedNodesFromIDs: state => {
      return state.nodes.filter(node => state.selectedNodesID.includes(node.id));
    }
  },
  modules: {},
});
