<template>
  <div class="geo-map">
    <l-map
      v-model:zoom="zoom"
      :zoomAnimation="true"
      :center="openNMSHeadQuarter"
    >
      <l-tile-layer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution='&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
      ></l-tile-layer>
      <l-control-layers />

      <l-marker v-for="(coordinate, index) in nodesWithCoordinate" :key="index" :lat-lng="coordinate">
        <l-popup> popup1 </l-popup>
        <l-tooltip> tooltip1 </l-tooltip>
      </l-marker>

      <!-- <l-polyline
        :lat-lngs="[marker1, marker2]"
        color="blue"
        :weight="3"
      ></l-polyline> -->
    </l-map>
  </div>
</template>
<script>
import {
  LMap,
  LTileLayer,
  LMarker,
  LControlLayers,
  LTooltip,
  LPopup,
  // LPolyline,
} from "@vue-leaflet/vue-leaflet";
import "leaflet/dist/leaflet.css";
import { mapState } from "vuex";

export default {
  components: {
    LMap,
    LTileLayer,
    LMarker,
    LControlLayers,
    LTooltip,
    LPopup,
    // LPolyline,
  },
  data() {
    return {
      zoom: 4,
      openNMSHeadQuarter: [35.849613, -78.794882],
    };
  },
  computed: {
    ...mapState(["monitoredNodesID"]),
    nodesWithCoordinate(){
      return this.$store.getters.getMonitoredNodes.filter(
        (node) =>
          !(
            node.assetRecord.latitude == null ||
            node.assetRecord.latitude.length === 0
          ) &&
          !(
            node.assetRecord.longitude == null ||
            node.assetRecord.longitude.length === 0
          )
      ).map((node) => {
        let coordinate = [];
        coordinate.push(node.assetRecord.latitude);
        coordinate.push(node.assetRecord.longitude);
        return coordinate
      });
    },
  },
  watch: {
    monitoredNodesID() {
    },
  },
  methods: {
  },

  created() {},
};
</script>

<style scoped>
.geo-map {
  height: 80vh;
}
</style>

