package com.BookMyEvent.entity.Enums;

public enum EventType {
    STAND_UP("Stand-up"),
    UNDER_HOUSE("Під домом"),
    CONCERTS("Концерт"),
    MASTER_CLASS("Майстер клас"),
    BUSINESS_NETWORKING("Бізнес та нетворкінг"),
    SPORTS_EVENTS("Спортивні заходи");

    private final String ukrainianName;

    EventType(String ukrainianName) {
        this.ukrainianName = ukrainianName;
    }

    public String getUkrainianName() {
        return ukrainianName;
    }
}