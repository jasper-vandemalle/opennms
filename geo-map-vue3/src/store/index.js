import { createStore } from "vuex";
import mapModule from './map/index.js'


export default createStore({
  modules: {mapModule},
});
