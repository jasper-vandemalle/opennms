<template>
  <div class="map-alarms">
    <div class="button-group">
      <span class="map-alarm-buttons">
        <select name="alarmOptions" id="alarmOptions" v-model="currentAlarmOption">
          <option v-for="option in alarmOptions" :value="option" :key="option">{{ option }}</option>
        </select>
        <button @click="submit()">Submit</button>
        <button @click="clearFilters()">Clear Filters</button>
        <button @click="applyFilters()">Apply filter</button>
        <button @click="reset()">Reset</button>
      </span>
    </div>
    <div class="map-alarms-grid">
      <ag-grid-vue
        style="width: 100%; height: 700px"
        class="ag-theme-alpine"
        rowSelection="multiple"
        @grid-ready="onGridReady"
        :columnDefs="columnDefs"
        :rowData="rowData"
        :defaultColDef="defaultColDef"
        :gridOptions="gridOptions"
        :pagination="true"
      ></ag-grid-vue>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref } from "vue";
import "ag-grid-community/dist/styles/ag-grid.css";
import "ag-grid-community/dist/styles/ag-theme-alpine.css";
import { AgGridVue } from "ag-grid-vue3";
import { useStore } from "vuex";
import { computed, watch } from 'vue'
import { Alarm, Node, AlarmQueryParameters } from "@/types";
import SeverityFloatingFilter from "../../components/SeverityFloatingFilter.vue"

const store = useStore();

const gridOptions = ref({})

let interestedNodesID = computed(() => {
  return store.getters['mapModule/getInterestedNodesID'];
})

let rowData = ref(getAlarmsFromSelectedNodes());

let gridApi = ref({});

let gridColumnApi = ref({});

function onGridReady(params: any) {
  gridApi = params.api
  gridColumnApi = params.columnApi;
  sizeToFit()
  // autoSizeAll(false);
}

function sizeToFit() {
  gridApi.sizeColumnsToFit();
}

function autoSizeAll(skipHeader) {
  let allColumnIds = [];
  gridColumnApi.getAllColumns().forEach(function (column) {
    allColumnIds.push(column.colId);
  });
  gridColumnApi.autoSizeColumns(allColumnIds, skipHeader);
}

watch(
  () => interestedNodesID.value,
  () => {
    gridApi.setRowData(
      getAlarmsFromSelectedNodes()
    );
  }
)

function getAlarmsFromSelectedNodes() {
  let alarms = store.getters['mapModule/getAlarmsFromSelectedNodes'];
  return alarms.map((alarm: Alarm) => ({
    id: +alarm.id,
    severity: alarm.severity,
    node: alarm.nodeLabel,
    uei: alarm.uei,
    count: +alarm.count,
    lastEventTime: alarm.lastEvent.time,
    logMessage: alarm.logMessage,
  }));
}

let alarmOptions = ref(["Acknowledge", "Unacknowledge", "Escalate", "Clear"]);

let currentAlarmOption = ref(alarmOptions.value[0]);

function submit() {
  let selectedRows = gridApi.getSelectedNodes().map(node => node.data);
  let selectedAlarmIds: string[] = selectedRows.map(alarm => alarm.id);
  console.log("submitting: " + currentAlarmOption.value)
  console.log("selectedAlarmIds :  " + selectedAlarmIds)
  let alarmQueryParameters: AlarmQueryParameters;
  switch (currentAlarmOption.value) {
    case alarmOptions.value[0]: { // "Acknowledge"
      alarmQueryParameters = { ack: true };
      break;
    }
    case alarmOptions.value[1]: { // "Unacknowledge"
      alarmQueryParameters = { ack: false };
      break;
    }
    case alarmOptions.value[2]: { // "Escalate"
      alarmQueryParameters = { escalate: true };
      break;
    }
    case alarmOptions.value[3]: { // "Clear"
      alarmQueryParameters = { clear: true };
      break;
    }
    default:
      console.log("No such alarm option exists: " + currentAlarmOption.value);
      break;
  }

  selectedAlarmIds.forEach((alarmId: string) => store.dispatch("mapModule/modifyAlarm", {
    pathVariable: alarmId, queryParameters: alarmQueryParameters
  }))
}

function clearFilters() {
  gridApi.setFilterModel(null);
}

function applyFilters() {
  let nodesLable: any = [];
  gridApi.forEachNodeAfterFilter((node: any) => {
    nodesLable.push(node.data.node);
  });
  let distictNodesLable = [...new Set(nodesLable)];
  let ids = [];
  ids = store.getters['mapModule/getInterestedNodes']
    .filter((node: Node) => distictNodesLable.includes(node.label))
    .map((node: Node) => node.id);
  store.dispatch("mapModule/setInterestedNodesId", ids);
}

function reset() {
  store.dispatch("mapModule/resetInterestedNodesID");
}


const defaultColDef = ref({
  floatingFilter: true,
  resizable: true,
  enableBrowserTooltips: true,
  filter: "agTextColumnFilter",
  sortable: true,
})

const columnDefs = ref([
  {
    headerName: "ID",
    field: "id",
    headerTooltip: "ID",
    headerCheckboxSelection: true,
    checkboxSelection: true,
    headerCheckboxSelectionFilteredOnly: true,
    filter: "agNumberColumnFilter",
    comparator: (valueA: number, valueB: number) => {
      return valueA - valueB;
    },
  },
  {
    headerName: "SEVERITY",
    field: "severity",
    headerTooltip: "Severity",
    floatingFilterComponentFramework: SeverityFloatingFilter,
    floatingFilterComponentParams: {
      suppressFilterButton: true,
    },
    filterParams: {
      textCustomComparator: (filter, value, filterText) => {
        const filterTextUpperCase = filterText.toUpperCase();
        const valueUpperCase = value.toString().toUpperCase();
        enum ALARM_SEVERITY {
          'INDETERMINATE',
          'CLEARED',
          'NORMAL',
          'WARNING',
          'MINOR',
          'MAJOR',
          'CRITICAL'
        }
        if (filter === 'contains') {
          return ALARM_SEVERITY[valueUpperCase] >= ALARM_SEVERITY[filterTextUpperCase]
        }
        return true;
      }
    }
  },
  {
    headerName: "NODE",
    field: "node",
    headerTooltip: "Node",
  },
  {
    headerName: "UEI",
    field: "uei",
    headerTooltip: "UEI",
  },
  {
    headerName: "COUNT",
    field: "count",
    headerTooltip: "Count",
    comparator: (valueA: number, valueB: number) => {
      return valueA - valueB;
    },
  },
  {
    headerName: "LAST EVENT TIME",
    field: "lastEventTime",
    headerTooltip: "Last Event Time",
    filter: "agDateColumnFilter",
    cellRenderer: (data: any) => {
      return data.value ? new Date(data.value).toLocaleDateString() : "";
    },
  },
  {
    headerName: "LOG MESSAGE",
    field: "logMessage",
    headerTooltip: "Log Message",
  },
]
)
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
