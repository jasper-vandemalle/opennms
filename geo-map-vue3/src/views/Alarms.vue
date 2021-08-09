<template>
  <div class="alarms">
    <ag-grid-vue
      style="width: 100%; height: 700px"
      class="ag-theme-alpine"
      rowSelection="multiple"
      :columnDefs="columnDefs"
      :rowData="rowData"
      :defaultColDef="defaultColDef"
      :gridOptions="gridOptions"
    >
    </ag-grid-vue>
  </div>
</template>

<script>
import "ag-grid-community/dist/styles/ag-grid.css";
import "ag-grid-community/dist/styles/ag-theme-alpine.css";
import { AgGridVue } from "ag-grid-vue3";
import AlarmsService from "@/services/AlarmsService.js";
export default {
  data() {
    return {
      gridOptions: null,
      gridApi: null,
      columnDefs: null,
      rowData: [],
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
        filter: "agTextColumnFilter",
      },
      {
        headerName: "Node",
        field: "node",
        sortable: true,
        headerTooltip: "Node",
        filter: "agTextColumnFilter",
      },
      {
        headerName: "UEI",
        field: "uei",
        sortable: true,
        headerTooltip: "uei",
      },
      {
        headerName: "COUNT",
        field: "count",
        sortable: true,
        headerTooltip: "Count",
        filter: "agTextColumnFilter",
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
        filter: "agTextColumnFilter",
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
    AlarmsService.getAlarms()
      .then((response) => {
        console.log(response.data);
        this.rowData = response.data.alarm.map((alarm) => ({
          id: alarm.id,
          severity: alarm.severity,
          node: alarm.nodeLabel,
          uei: alarm.uei,
          count: alarm.count,
          lastEventTime: alarm.lastEvent.time,
          logMessage: alarm.logMessage,
        }));
      })
      .catch((error) => {
        console.log(error);
      });
  },
};
</script>

<style scoped></style>
