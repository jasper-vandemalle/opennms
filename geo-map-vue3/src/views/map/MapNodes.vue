<template>
  <div class="map-nodes">
    <div class="button-group">
      <span class="buttons">
        <button v-on:click="clearFilters()">Clear Filters</button>
        <!-- <button v-on:click="showTopology()">Show Topology</button> -->
        <button v-on:click="confirmFilters()">Apply filter</button>
        <button v-on:click="reset()">Reset</button>
      </span>
    </div>
    <div class="map-nodes-grid">
      <ag-grid-vue
        style="width: 100%; height: 600px"
        class="ag-theme-alpine"
        rowSelection="multiple"
        :columnDefs="columnDefs"
        :rowData="rowData"
        :defaultColDef="defaultColDef"
        :gridOptions="gridOptions"
        :pagination="true">
      </ag-grid-vue>
    </div>
  </div>
</template>

<script>
import "ag-grid-community/dist/styles/ag-grid.css";
import "ag-grid-community/dist/styles/ag-theme-alpine.css";
import { AgGridVue } from "ag-grid-vue3";
export default {
  components: {
    "ag-grid-vue": AgGridVue,
  },
  data() {
    return {
      gridOptions: null,
      gridApi: null,
      columnDefs: null,
      rowData: null,
      defaultColDef: null,
      gridColumnApi: null,
    };
  },
  computed: {
    interestedNodesID () {
      return this.$store.getters.getInterestedNodesID
    } 
  },
  watch: {
    interestedNodesID(newValue, oldValue) {
      console.log(`InterestedNodesID updating from ${oldValue} to ${newValue}`);
      this.gridApi.setRowData(
        this.getGridRowDataFromInterestedNodes()
      );
    },
  },

  methods: {
    sizeToFit() {
      this.gridApi.sizeColumnsToFit();
    },
    autoSizeAll(skipHeader) {
      var allColumnIds = [];
      this.gridColumnApi.getAllColumns().forEach(function (column) {
        allColumnIds.push(column.colId);
      });
      this.gridColumnApi.autoSizeColumns(allColumnIds, skipHeader);
    },
    //TODO: make this smarter
    clearFilters() {
      this.gridApi.getFilterInstance("id").setModel(null);
      this.gridApi.getFilterInstance("foreignSource").setModel(null);
      this.gridApi.getFilterInstance("lable").setModel(null);
      this.gridApi.getFilterInstance("lableSource").setModel(null);
      this.gridApi.getFilterInstance("foreignId").setModel(null);
      this.gridApi.getFilterInstance("lastCapabilitiesScan").setModel(null);
      this.gridApi.onFilterChanged();
    },
    confirmFilters() {
      let ids = [];
      this.gridApi.forEachNodeAfterFilter((node) => ids.push(node.data.id));
      this.$store.commit("SET_INTERESTED_NODES_ID", ids);
    },
    showTopology() {},
    reset() {
      this.$store.dispatch("resetInterestedNodesID");
    },
  getGridRowDataFromInterestedNodes() {
      return this.$store.getters.getInterestedNodes.map((node) => ({
        id: node.id,
        foreignSource: node.foreignSource,
        foreignId: node.foreignId,
        lable: node.label,
        lableSource: node.labelSource,
        lastCapabilitiesScan: node.lastCapsdPoll,
        // primaryInterface: ,
        sysObjectid: node.sysObjectId,
        sysName: node.sysName,
        sysDescription:  node.sysDescription,
        sysContact: node.sysContact,
        sysLocation: node.sysLocation
      }));
    },
  },

  beforeMount() {
    this.gridOptions = {};
    this.defaultColDef = {
      floatingFilter: true,
      resizable: true,
      enableBrowserTooltips: true,
      filter: "agTextColumnFilter",
    };
    this.columnDefs = [
      {
        headerName: "ID",
        field: "id",
        sortable: true,
        headerTooltip: "ID",
        floatingFilter: false,
        // filter: "agNumberColumnFilter",
        comparator: (valueA, valueB) => {
          return valueA - valueB;
        },
      },
      {
        headerName: "FOREIGN SOURCE",
        field: "foreignSource",
        sortable: true,
        headerTooltip: "Foreign Source",
      },
      {
        headerName: "FOREIGN ID",
        field: "foreignId",
        sortable: true,
        headerTooltip: "Foreign ID",
      },
      {
        headerName: "LABLE",
        field: "lable",
        sortable: true,
        headerTooltip: "Lable",
      },
      {
        headerName: "LABLE SOURCE",
        field: "lableSource",
        sortable: true,
        headerTooltip: "Lable Source",
      },
      {
        headerName: "LAST CAPABILITIES SCAN",
        field: "lastCapabilitiesScan",
        sortable: true,
        headerTooltip: "Last Capabilities Scan",
        filter: "agDateColumnFilter",
        cellRenderer: (data) => {
          return data.value ? new Date(data.value).toLocaleDateString() : "";
        },
      },
      {
        headerName: "PRIMARY INTERFACE",
        field: "primaryInterface",
        sortable: true,
        headerTooltip: "Primary Interface",
      },
      {
        headerName: "SYSOBJECTID",
        field: "sysObjectid",
        sortable: true,
        headerTooltip: "Sys Object ID",
      },
      {
        headerName: "SYSNAME",
        field: "sysName",
        sortable: true,
        headerTooltip: "Sys Name",
      },
      {
        headerName: "SYSDESCRIPTION",
        field: "sysDescription",
        sortable: true,
        headerTooltip: "Sys Description",
      },
      {
        headerName: "SYSCONTACT",
        field: "sysContact",
        sortable: true,
        headerTooltip: "Sys Contact",
      },
      {
        headerName: "SYSLOCATION",
        field: "sysLocation",
        sortable: true,
        headerTooltip: "Sys Location",
      },
    ];
  },

  mounted() {
    this.gridApi = this.gridOptions.api;
    this.gridColumnApi = this.gridOptions.columnApi;
    // this.sizeToFit();
    this.autoSizeAll(false);
  },

  created() {
    this.rowData = this.getGridRowDataFromInterestedNodes();
  },
};
</script>

<style scoped>
.button-group {
  width: 100%;
  height: 25px;
}
.map-nodes-grid {
  width: 100%;
  height: 700px;
}
.buttons {
  float: right;
}
button {
  margin-right: 5px;
}
</style>
