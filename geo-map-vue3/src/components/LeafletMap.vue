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

      <l-marker
        v-for="(node, index) in interestedNodes"
        :key="index"
        :lat-lng="getCoordinateFromNode(node)"
      >
        <l-popup> {{ node.label }} </l-popup>
        <!-- <l-tooltip> tooltip1 </l-tooltip> -->
      </l-marker>

      <l-polyline
        v-for="(coordinatePair, index) in edges"
        :key="index"
        :lat-lngs="[coordinatePair[0], coordinatePair[1]]"
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
  LControlLayers,
  // LTooltip,
  LPopup,
  LPolyline,
} from "@vue-leaflet/vue-leaflet";
import "leaflet/dist/leaflet.css";
import { mapState } from "vuex";

export default {
  components: {
    LMap,
    LTileLayer,
    LMarker,
    LControlLayers,
    // LTooltip,
    LPopup,
    LPolyline,
  },
  data() {
    return {
      zoom: 4,
      openNMSHeadQuarter: [35.849613, -78.794882],
    };
  },
  computed: {
    ...mapState(["mapModule/interestedNodesID"]),
    interestedNodes(){
      return this.$store.getters['mapModule/getInterestedNodes']
    },

    edges() {
      
      let ids = this.$store.getters['mapModule/getInterestedNodesID'];
      console.log("ids = "+ ids)
      let interestedNodesIDCoordinateMap = this.getInterestedNodesIDCoordinateMap();

      this.$store.getters['mapModule/getEdges'].forEach(e=> console.log("edge: "+ e))
      this.$store.getters['mapModule/getEdges'].filter(edge => ids.includes(edge[0]) && ids.includes(edge[1]))
      .map((edge) => {
        let edgeCoordinatesPair = [];
        edgeCoordinatesPair.push(interestedNodesIDCoordinateMap.get(edge[0]));
        edgeCoordinatesPair.push(interestedNodesIDCoordinateMap.get(edge[1]));
        return edgeCoordinatesPair
      }).forEach(e=> console.log("result edge: "+ e))
      return this.$store.getters['mapModule/getEdges'].filter(edge => ids.includes(edge[0]) && ids.includes(edge[1]))
      .map((edge) => {
        let edgeCoordinatesPair = [];
        edgeCoordinatesPair.push(interestedNodesIDCoordinateMap.get(edge[0]));
        edgeCoordinatesPair.push(interestedNodesIDCoordinateMap.get(edge[1]));
        return edgeCoordinatesPair
      });
    },
  },
  watch: {
    interestedNodesID() {},
  },
  methods: {
    getCoordinateFromNode(node) {
      let coordinate = [];
      coordinate.push(node.assetRecord.latitude);
      coordinate.push(node.assetRecord.longitude);
      return coordinate;
    },
    getInterestedNodesIDCoordinateMap() {
      var map = new Map();
      this.interestedNodes.forEach((node) => {
        map.set(node.id, this.getCoordinateFromNode(node));
      });
      return map;
    },
  },

  created() {},
};
</script>

<style scoped>
.geo-map {
  height: 80vh;
}
</style>

