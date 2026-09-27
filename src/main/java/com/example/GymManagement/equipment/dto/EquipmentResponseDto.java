package com.example.GymManagement.equipment.dto;

public class EquipmentResponseDto {

    private Long equipmentId;
    private String name;
    private String itemCondition;
    private boolean available;

    // Which subclass this item is: BALL, RACKET or BOARD_GAME
    private String type;

    // Subclass-specific details. Only the fields that apply to the type are filled; the rest stay null.
    private String sportType;     // Ball, Racket
    private String stringStatus;  // Racket
    private Integer pieceCount;   // BoardGame

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSportType() {
        return sportType;
    }

    public void setSportType(String sportType) {
        this.sportType = sportType;
    }

    public String getStringStatus() {
        return stringStatus;
    }

    public void setStringStatus(String stringStatus) {
        this.stringStatus = stringStatus;
    }

    public Integer getPieceCount() {
        return pieceCount;
    }

    public void setPieceCount(Integer pieceCount) {
        this.pieceCount = pieceCount;
    }
}
