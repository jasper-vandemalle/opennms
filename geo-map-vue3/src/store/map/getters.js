const getInterestedNodesID = state => {
    return state.interestedNodesID;
}

const getInterestedNodes = state => {
    return state.nodesWithCoordinates.filter(node => state.interestedNodesID.includes(node.id));
}

const getAlarmsFromSelectedNodes = (state, getters) => {
    let selectedNodesLable = getters.getInterestedNodes.map(
        (node) => node.label
    );
    return state.alarms.filter((alarm) =>
        selectedNodesLable.includes(alarm.nodeLabel)
    )
}

export default {
    getInterestedNodesID,
    getInterestedNodes,
    getAlarmsFromSelectedNodes
}