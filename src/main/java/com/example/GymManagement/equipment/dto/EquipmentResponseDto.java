package com.example.GymManagement.equipment.dto;

public class EquipmentResponseDto {

    private Long equipmentId;
    private String name;
    private String itemCondition;
    private boolean available;

    public EquipmentResponseDto() {
    }

    public EquipmentResponseDto(
            Long equipmentId,
            String name,
            String itemCondition,
            boolean available) {

        this.equipmentId = equipmentId;
        this.name = name;
        this.itemCondition = itemCondition;
        this.available = available;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public String getName() {
        return name;
    }

    public String getItemCondition() {
        return itemCondition;
    }

    public boolean isAvailable() {
        return available;
    }
}