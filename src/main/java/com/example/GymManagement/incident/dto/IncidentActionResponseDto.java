package com.example.GymManagement.incident.dto;

import com.example.GymManagement.incident.entity.IncidentAction;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// One action, flattened. Deliberately has NO reference back to the Incident,
// which is what stops the Incident <-> IncidentAction infinite JSON loop.
@Getter
@Setter
@NoArgsConstructor
public class IncidentActionResponseDto {

    private Long actionId;
    private String actionTaken;
    private String outcome;
    private LocalDateTime actionDate;
    private Long handledByUserId;
    private String handledByName;
    private String handledByRole;

    public IncidentActionResponseDto(IncidentAction action) {

        // 1. Copy the action's own fields
        this.actionId = action.getActionId();
        this.actionTaken = action.getActionTaken();
        this.outcome = action.getOutcome();
        this.actionDate = action.getActionDate();

        // 2. Only safe details about the handler, never the whole User
        this.handledByUserId = action.getHandledBy().getUserId();
        this.handledByName = action.getHandledBy().getName();
        this.handledByRole = action.getHandledBy().getRole().getRoleName();
    }
}