package com.example.model;

public class Slot {
    private int id;
    private String startTime;
    private String endTime;
    private String slotName;

    public Slot() {}

    public Slot(int id, String startTime, String endTime, String slotName) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotName = slotName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getSlotName() {
        return slotName;
    }

    public void setSlotName(String slotName) {
        this.slotName = slotName;
    }
}