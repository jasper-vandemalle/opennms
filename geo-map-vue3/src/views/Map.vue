<template>
  <splitpanes class="default-theme" horizontal style="height: 1000px">
    <pane min-size="1" max-size="60">
      <div class="leaflet-map">
        <LeafletMap />
      </div>
    </pane>
    <pane id="map-pane-under">
      <button @click="currentTabComponent = 'Alarms'">Alarms View</button>
      <button @click="currentTabComponent = 'Nodes'">Nodes View</button>
      <keep-alive>
        <component :is="currentTabComponent"></component>
      </keep-alive>
    </pane>
  </splitpanes>
</template>

<script>
import LeafletMap from "@/components/LeafletMap.vue";
import Alarms from "@/components/MapAlarms.vue";
import Nodes from "@/components/MapNodes.vue";
import { Splitpanes, Pane } from "splitpanes";
import "splitpanes/dist/splitpanes.css";

export default {
  name: "Map",
  components: {
    LeafletMap,
    Splitpanes,
    Pane,
    Alarms,
    Nodes,
  },
  data() {
    return {
      currentTabComponent: Nodes,
    };
  },
};
</script>

<style scoped>
#map-pane-under {
  text-align: left;
}

#map-pane-under a {
  font-family: Arial;
  font-size: 15px;
  /* font-weight: bold; */
  color: #7e8198;
}

#map-pane-under a.router-link-exact-active {
  color: #131736;
}
</style>