package com.example.GymManagement.equipment.service;

import com.example.GymManagement.config.ResourceNotFoundException;
import com.example.GymManagement.equipment.dto.EquipmentRequestDto;
import com.example.GymManagement.equipment.dto.EquipmentResponseDto;
import com.example.GymManagement.equipment.entity.Ball;
import com.example.GymManagement.equipment.entity.BoardGame;
import com.example.GymManagement.equipment.entity.Equipment;
import com.example.GymManagement.equipment.entity.Racket;
import com.example.GymManagement.equipment.repository.EquipmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(
            EquipmentRepository equipmentRepository) {

        this.equipmentRepository = equipmentRepository;
    }

    // =========================================================
    // GET ALL
    // =========================================================

    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    public Equipment getEquipmentById(Long equipmentId) {

        return equipmentRepository.findById(equipmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Equipment not found with ID: "
                                        + equipmentId
                        )
                );
    }

    // =========================================================
    // GET AVAILABLE
    // =========================================================

    public List<Equipment> getAvailableEquipment() {
        return equipmentRepository.findByAvailable(true);
    }

    // =========================================================
    // SEARCH
    // =========================================================

    public List<Equipment> searchEquipment(String name) {

        return equipmentRepository
                .findByNameContainingIgnoreCase(name);
    }

    // =========================================================
    // CREATE
    // =========================================================

    public Equipment createEquipment(
            EquipmentRequestDto request) {

        Equipment equipment;

        String type = request.getType()
                .trim()
                .toUpperCase();

        switch (type) {

            case "BALL":

                if (request.getSportType() == null
                        || request.getSportType().isBlank()) {

                    throw new IllegalArgumentException(
                            "Sport type is required for a ball."
                    );
                }

                equipment = new Ball(
                        request.getName(),
                        request.getItemCondition(),
                        request.getSportType()
                );

                break;

            case "RACKET":

                if (request.getSportType() == null
                        || request.getSportType().isBlank()) {

                    throw new IllegalArgumentException(
                            "Sport type is required for a racket."
                    );
                }

                equipment = new Racket(
                        request.getName(),
                        request.getItemCondition(),
                        request.getSportType(),
                        request.getStringStatus()
                );

                break;

            case "BOARD_GAME":

                if (request.getPieceCount() == null
                        || request.getPieceCount() <= 0) {

                    throw new IllegalArgumentException(
                            "Piece count must be greater than zero."
                    );
                }

                equipment = new BoardGame(
                        request.getName(),
                        request.getItemCondition(),
                        request.getPieceCount()
                );

                break;

            default:

                throw new IllegalArgumentException(
                        "Invalid equipment type: "
                                + request.getType()
                );
        }

        equipment.markAvailable();

        return equipmentRepository.save(equipment);
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public Equipment updateEquipment(
            Long equipmentId,
            EquipmentRequestDto request) {

        Equipment existingEquipment =
                getEquipmentById(equipmentId);

        String type = request.getType()
                .trim()
                .toUpperCase();

        /*
         * We don't allow changing the actual subclass type
         * of an existing equipment item.
         *
         * Example:
         * Ball -> Racket
         *
         * should not happen through an UPDATE.
         */

        if (existingEquipment instanceof Ball) {

            if (!type.equals("BALL")) {
                throw new IllegalArgumentException(
                        "Equipment type cannot be changed."
                );
            }

            Ball ball = (Ball) existingEquipment;

            if (request.getSportType() == null
                    || request.getSportType().isBlank()) {

                throw new IllegalArgumentException(
                        "Sport type is required for a ball."
                );
            }

            ball.setName(request.getName());
            ball.setItemCondition(
                    request.getItemCondition()
            );
            ball.setSportType(
                    request.getSportType()
            );

        } else if (existingEquipment instanceof Racket) {

            if (!type.equals("RACKET")) {
                throw new IllegalArgumentException(
                        "Equipment type cannot be changed."
                );
            }

            Racket racket =
                    (Racket) existingEquipment;

            if (request.getSportType() == null
                    || request.getSportType().isBlank()) {

                throw new IllegalArgumentException(
                        "Sport type is required for a racket."
                );
            }

            racket.setName(request.getName());
            racket.setItemCondition(
                    request.getItemCondition()
            );
            racket.setSportType(
                    request.getSportType()
            );
            racket.setStringStatus(
                    request.getStringStatus()
            );

        } else if (existingEquipment instanceof BoardGame) {

            if (!type.equals("BOARD_GAME")) {
                throw new IllegalArgumentException(
                        "Equipment type cannot be changed."
                );
            }

            BoardGame boardGame =
                    (BoardGame) existingEquipment;

            if (request.getPieceCount() == null
                    || request.getPieceCount() <= 0) {

                throw new IllegalArgumentException(
                        "Piece count must be greater than zero."
                );
            }

            boardGame.setName(request.getName());
            boardGame.setItemCondition(
                    request.getItemCondition()
            );
            boardGame.setPieceCount(
                    request.getPieceCount()
            );

        } else {

            throw new IllegalArgumentException(
                    "Unsupported equipment type."
            );
        }

        return equipmentRepository.save(
                existingEquipment
        );
    }

    // =========================================================
    // DELETE
    // =========================================================

    public void deleteEquipment(Long equipmentId) {

        Equipment equipment =
                getEquipmentById(equipmentId);

        equipmentRepository.delete(equipment);
    }

    // =========================================================
    // MARK UNAVAILABLE
    // =========================================================

    public Equipment markUnavailable(Long equipmentId) {

        Equipment equipment =
                getEquipmentById(equipmentId);

        equipment.markUnavailable();

        return equipmentRepository.save(equipment);
    }

    // =========================================================
    // MARK AVAILABLE
    // =========================================================

    public Equipment markAvailable(Long equipmentId) {

        Equipment equipment =
                getEquipmentById(equipmentId);

        equipment.markAvailable();

        return equipmentRepository.save(equipment);
    }

    // =========================================================
    // ENTITY -> DTO
    // =========================================================

    public EquipmentResponseDto toResponseDto(
            Equipment equipment) {

        return new EquipmentResponseDto(
                equipment.getEquipmentId(),
                equipment.getName(),
                equipment.getItemCondition(),
                equipment.isAvailable()
        );
    }
}