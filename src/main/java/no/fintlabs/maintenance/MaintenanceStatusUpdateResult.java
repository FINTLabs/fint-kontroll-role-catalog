package no.fintlabs.maintenance;

import java.util.Date;

public record MaintenanceStatusUpdateResult(
        boolean dryRun,
        Date referenceDate,
        String scope,
        int matchedRoles,
        int matchedMemberships,
        int updatedRoles,
        int updatedMemberships,
        int republishedRoles,
        int republishedMemberships,
        String message
) {
}
