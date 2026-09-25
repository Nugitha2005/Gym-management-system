package com.example.GymManagement.equipment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EquipmentRequestDto {

    @NotBlank
    private String type;

    @NotBlank
    private String name;

    @NotBlank
    private String itemCondition;

    private String sportType;

    private String stringStatus;

    @Min(1)
    private Integer pieceCount;


    public EquipmentRequestDto() {
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getItemCondition() {
        return itemCondition;
    }

    public void setItemCondition(String itemCondition) {
        this.itemCondition = itemCondition;
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