<template>
  <div class="map-alarms">
    <div class="button-group">
      <span class="map-alarm-buttons">
        <select name="subject" id="subject">
          <option value="Acknowledge" selected="selected">Acknowledge</option>
          <option value="Unacknowledge">Unacknowledge</option>
          <option value="Escalate">Escalate</option>
          <option value="Clear">Clear</option>
        </select>
        <button v-on:click="submit()">Submit</button>
        <button v-on:click="clearFilters()">Clear Filters</button>
        <button v-on:click="syncMap()">Sync Map</button>
      </span>
    </div>
    <div class="map-alarms-grid">
      <ag-grid-vue
        style="width: 100%; height: 700px"
        class="ag-theme-alpine"
        rowSelection="multiple"
        :columnDefs="columnDefs"
        :rowData="rowData"
        :defaultColDef="defaultColDef"
        :gridOptions="gridOptions"
        :pagination="true"
      >
      </ag-grid-vue>
    </div>
  </div>
</template>

<script>
import "ag-grid-community/dist/styles/ag-grid.css";
import "ag-grid-community/dist/styles/ag-theme-alpine.css";
import { AgGridVue } from "ag-grid-vue3";

export default {
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

  components: {
    "ag-grid-vue": AgGridVue,
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
        headerCheckboxSelection: true,
        checkboxSelection: true,
        headerCheckboxSelectionFilteredOnly: true,
        filter: "agNumberColumnFilter",
        comparator: (valueA, valueB) => {
          return valueA - valueB;
        },
      },
      {
        headerName: "SEVERITY",
        field: "severity",
        sortable: true,
        headerTooltip: "Severity",
      },
      {
        headerName: "Node",
        field: "node",
        sortable: true,
        headerTooltip: "Node",
      },
      {
        headerName: "UEI",
        field: "lable",
        sortable: true,
        headerTooltip: "Lable",
      },
      {
        headerName: "LABLE SOURCE",
        field: "uei",
        sortable: true,
        headerTooltip: "UEI",
      },
      {
        headerName: "COUNT",
        field: "count",
        sortable: true,
        headerTooltip: "Count",
        filter: "agNumberColumnFilter",
        comparator: (valueA, valueB) => {
          return valueA - valueB;
        },
      },
      {
        headerName: "LAST EVENT TIME",
        field: "lastEventTime",
        sortable: true,
        headerTooltip: "Last Event Time",
        filter: "agDateColumnFilter",
        cellRenderer: (data) => {
          return data.value ? new Date(data.value).toLocaleDateString() : "";
        },
      },
      {
        headerName: "LOG MESSAGE",
        field: "logMessage",
        sortable: true,
        headerTooltip: "Log Message",
      },
    ];
  },

  mounted() {
    this.gridApi = this.gridOptions.api;
    this.gridColumnApi = this.gridOptions.columnApi;
    this.sizeToFit();
    // this.autoSizeAll(false);
  },

  created() {
    this.rowData = this.$store.state.alarms.map((alarm) => ({
      id: alarm.id,
      severity: alarm.severity,
      node: alarm.nodeLabel,
      uei: alarm.uei,
      count: alarm.count,
      lastEventTime: alarm.lastEvent.time,
      logMessage: alarm.logMessage,
    }));
  },
};
</script>

<style scoped>
.button-group {
  width: 100%;
  height: 25px;
}
.map-alarms-grid {
  width: 100%;
  height: 700px;
}
.map-alarm-buttons {
  float: right;
}
button {
  margin-left: 4px;
  margin-right: 6px;
}
</style>
