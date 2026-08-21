package com.example.GymManagement.equipment;

import jakarta.persistence.Entity;

@Entity
public class BoardGame extends Equipment {

    private int pieceCount; // expected number of pieces for the set

    protected BoardGame() {
        super();
    }

    public BoardGame(String name, String condition, int pieceCount) {
        super(name, condition);
        this.pieceCount = pieceCount;
    }

    public int getPieceCount() {
        return pieceCount;
    }

    public void setPieceCount(int pieceCount) {
        this.pieceCount = pieceCount;
    }

    /**
     * Checks whether the board game currently has its full set of pieces.
     * For now this is a simple placeholder — later this could be tied to
     * a real piece-tracking mechanism if the team wants more detail.
     */
    public boolean checkPiecesComplete() {
        return pieceCount > 0;
    }
}