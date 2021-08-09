import {
  getNodes,
  getNodeById,
  getNodeOutages,
  getNodeIpInterfaces,
  getNodeSnmpInterfaces,
  getNodeAvailabilityPercentage
} from './nodesService'

import { getEvents } from './eventsService'
import { getNodeIfServices } from './ifService'
import { getLocations } from './locationsService'
import { scanIPRanges, detectSNMPAvailable, provision } from './inventoryService'

export default {
  getNodes,
  getEvents,
  getNodeById,
  getLocations,
  getNodeOutages,
  getNodeIfServices,
  getNodeIpInterfaces,
  getNodeSnmpInterfaces,
  getNodeAvailabilityPercentage,
  scanIPRanges,
  detectSNMPAvailable,
  provision
}
