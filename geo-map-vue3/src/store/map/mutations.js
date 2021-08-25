const SET_NODES = (state, nodes) =>{
    state.nodesWithCoordinates = nodes
}

const SET_ALARMS = (state, alarms) => {
    state.alarms = alarms
}

const SET_INTERESTED_NODES_ID = (state, ids) => {
    state.interestedNodesID = ids
}

const SET_NODE_EDGES = (state, edges) => {
    state.edges = edges
}

export default {
    SET_NODES,
    SET_ALARMS,
    SET_INTERESTED_NODES_ID,
    SET_NODE_EDGES
}