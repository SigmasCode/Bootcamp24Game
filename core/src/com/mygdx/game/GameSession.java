package com.mygdx.game;

import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.objects.ShipObject;

import java.util.ArrayList;

import managers.MemoryManager;

public class GameSession {

    public GameState state;
    long nextTrashSpawnTime;
    long sessionStartTime;
    long pauseStartTime;
    private int score;
    int destructedTrashNumber;
    int diamonds;
    long nextMeteoriteSpawnTime;
    long nextDiamondSpawnTime;

    public GameSession() {
    }

    public void startGame() {
        state = GameState.PLAYING;
        score = 0;
        //
        diamonds = 0;
        //
        destructedTrashNumber = 0;
        sessionStartTime = TimeUtils.millis();
        nextTrashSpawnTime = sessionStartTime + (long) (GameSettings.STARTING_TRASH_APPEARANCE_COOL_DOWN
                * getTrashPeriodCoolDown());
        nextMeteoriteSpawnTime = sessionStartTime + (long) (GameSettings.METEORITE_COOL_DOWN
        * getTrashPeriodCoolDown());
        nextDiamondSpawnTime = sessionStartTime + (long) (GameSettings.DIAMOND_COOL_DOWN
        * getTrashPeriodCoolDown());
    }

    public void pauseGame() {
        state = GameState.PAUSED;
        pauseStartTime = TimeUtils.millis();
    }

    public void resumeGame() {
        state = GameState.PLAYING;
        sessionStartTime += TimeUtils.millis() - pauseStartTime;
    }

    public void endGame() {
        updateScore();
        state = GameState.ENDED;
        ArrayList<Integer> recordsTable = MemoryManager.loadRecordsTable();
        if (recordsTable == null) {
            recordsTable = new ArrayList<>();
        }
        int foundIdx = 0;
        for (; foundIdx < recordsTable.size(); foundIdx++) {
            if (recordsTable.get(foundIdx) < getScore()) break;
        }
        recordsTable.add(foundIdx, getScore());
        MemoryManager.saveTableOfRecords(recordsTable);
    }

    public void destructionRegistration() {
        destructedTrashNumber += 1;
    }

    public void updateScore() {
        score = (int) (TimeUtils.millis() - sessionStartTime) / 100 + destructedTrashNumber * 100 + diamonds * GameSettings.DIAMOND_PRICE;
    }

    public void gotDiamond() {
        diamonds += 1;
    }

    public int getScore() {
        return score;
    }

    public boolean shouldSpawnTrash() {
        if (nextTrashSpawnTime <= TimeUtils.millis()) {
            nextTrashSpawnTime = TimeUtils.millis() + (long) (GameSettings.STARTING_TRASH_APPEARANCE_COOL_DOWN
                    * getTrashPeriodCoolDown());
            return true;
        }
        return false;
    }

    public boolean shouldSpawnMeteorite() {
        if (nextMeteoriteSpawnTime <= TimeUtils.millis()) {
            nextMeteoriteSpawnTime = TimeUtils.millis() + (long) (GameSettings.METEORITE_COOL_DOWN
            * getTrashPeriodCoolDown());
            return true;
        }
        return false;
    }

    public boolean shouldSpawnDiamond() {
        if (nextDiamondSpawnTime <= TimeUtils.millis()) {
            nextDiamondSpawnTime = TimeUtils.millis() + (long) (GameSettings.DIAMOND_COOL_DOWN
            * getTrashPeriodCoolDown());
            return true;
        }
        return false;
    }

    private float getTrashPeriodCoolDown() {
        return (float) Math.exp(-0.001 * (TimeUtils.millis() - sessionStartTime + 1) / 1000);
    }
}