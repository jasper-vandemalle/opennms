<template>
  <div class="nodes">
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
import NodesService from "@/services/NodesService.js";
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
      filter: "agTextColumnFilter",
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
    NodesService.getNodes()
      .then((response) => {
        this.rowData = response.data.node.map(node => ({
          id: node.id,
          foreignSource: node.foreignSource,
          foreignId: node.foreignId,
          lable: node.label,
          lableSource: node.labelSource,
          lastCapabilitiesScan: node.lastCapsdPoll,
        }));
      })
      .catch((error) => {
        console.log(error);
      });
  },
};
</script>

<style scoped></style>
