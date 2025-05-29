package com.BookMyEvent.entity.Enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public enum EventType {
    @Schema(description = "Stand-up comedy event")
    STAND_UP("Stand-up"),
    @Schema(description = "Event under the house")
    UNDER_HOUSE("Під домом"),
    @Schema(description = "Concert events")
    CONCERTS("Концерти"),
    @Schema(description = "Master classes")
    MASTER_CLASS("Майстер класи"),
    @Schema(description = "Business networking events")
    BUSINESS_NETWORKING("Бізнес та нетворкінг"),
    @Schema(description = "Sports events")
    SPORTS_EVENTS("Спортивні заходи"),
    @Schema(description = "Other events")
    OTHER("Інше");

    private final String ukrainianName;

    EventType(String ukrainianName) {
        this.ukrainianName = ukrainianName;
    }
}