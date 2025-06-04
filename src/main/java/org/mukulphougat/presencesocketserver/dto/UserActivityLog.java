package org.mukulphougat.presencesocketserver.dto;

import lombok.Data;
import org.mukulphougat.presencesocketserver.constants.PresenceStatus;

@Data
public class UserActivityLog {
    private String userId;
    private PresenceStatus status;
    private String timestamp;
}
