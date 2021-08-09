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

      <l-marker :lat-lng="marker1latlng">
        <!-- <l-icon :icon-url="iconUrl" :icon-size="iconSize" /> -->
        <l-popup> popup1 </l-popup>
        <l-tooltip> tooltip1 </l-tooltip>
      </l-marker>

      <l-marker :lat-lng="marker2">
        <!-- <l-icon :icon-url="iconUrl" :icon-size="iconSize" /> -->
        <l-popup> popup2 </l-popup>
        <l-tooltip> tooltip2 </l-tooltip>
      </l-marker>

      <l-polyline
        :lat-lngs="[marker1, marker2]"
        color="blue"
        :weight="3"
      ></l-polyline>
    </l-map>
  </div>
</template>
<script>
import {
  LMap,
  LTileLayer,
  LMarker,
  // LIcon,
  LControlLayers,
  LTooltip,
  LPopup,
  LPolyline,
} from "@vue-leaflet/vue-leaflet";
import "leaflet/dist/leaflet.css";
// import NodesService from "@/services/NodesService.js";

export default {
  components: {
    LMap,
    LTileLayer,
    LMarker,
    // LIcon,
    LControlLayers,
    LTooltip,
    LPopup,
    LPolyline,
  },
  data() {
    return {
      zoom: 4,
      openNMSHeadQuarter: [35.849613, -78.794882],
      // iconWidth: 25,
      // iconHeight: 25,
      nodes: null,
      marker1: [35.849613, -78.794882],
      marker2: [45.448795, -75.688529],
    };
  },
  computed: {
    // iconUrl: function () {
    //   return require("../assets/node.png");
    // },
    // iconSize: function () {
    //   return [this.iconWidth, this.iconHeight];
    // },
    marker1latlng: function () {
      return [35.849613, -78.794882];
    },
    marker2latlng: function () {
      return [40.714847, -74.048383];
    },
  },
  methods: {},

  created() {
console.log("I'm in Leaflet page")
  },
};
</script>

<style scoped>
.geo-map {
  height: 80vh;
}
</style>

