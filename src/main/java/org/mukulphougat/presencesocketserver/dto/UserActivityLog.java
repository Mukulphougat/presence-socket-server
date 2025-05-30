package org.mukulphougat.presencesocketserver.dto;

import lombok.Data;

@Data
public class UserActivityLog {
    private String userId;
    private String status;
    private String timestamp;
}
