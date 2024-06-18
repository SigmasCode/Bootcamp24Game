package com.mygdx.game.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.GameResources;
import com.mygdx.game.GameSettings;

import managers.MemoryManager;

public class ShipObject extends GameObject {

    long lastShotTime;
    int livesLeft;
    int coolDown;

    public ShipObject(int x, int y, int width, int height, String texturePath, World world, boolean kinematic) {
        super(texturePath, x, y, width, height, GameSettings.SHIP_BIT, world, kinematic);
        body.setLinearDamping(10);
        livesLeft = 3;
        coolDown = GameSettings.SHOOTING_COOL_DOWN;
    }


    public int getLiveLeft() {
        return livesLeft;
    }

    @Override
    public void draw(SpriteBatch batch) {
        putInFrame();
        super.draw(batch);
    }

    public void move(Vector3 vector3) {
        if (MemoryManager.saveSpaceShipOn()) {
            body.applyForceToCenter(new Vector2(
                            (vector3.x - getX()) * GameSettings.SPACESHIP_FORCE_RATIO,
                            (vector3.y - getY()) * GameSettings.SPACESHIP_FORCE_RATIO),
                    true
            );
        } else {
            body.applyForceToCenter(new Vector2(
                            (vector3.x - getX()) * GameSettings.SHIP_FORCE_RATIO,
                            (vector3.y - getY()) * GameSettings.SHIP_FORCE_RATIO),
                    true
            );
        }
    }

    private void putInFrame() {
        if (getY() > (GameSettings.SCREEN_HEIGHT / 2f - height / 2f)) {
            setY((int) (GameSettings.SCREEN_HEIGHT / 2f - height / 2f));
        }
        if (getY() <= (height / 2f)) {
            setY(height / 2);
        }
        if (getX() < (-width / 2f)) {
            setX(GameSettings.SCREEN_WIDTH);
        }
        if (getX() > (GameSettings.SCREEN_WIDTH + width / 2f)) {
            setX(0);
        }
    }

    public boolean needToShoot() {
        if (MemoryManager.saveSpaceShipOn()) {
            coolDown = GameSettings.SPACE_SHOOTING_COOL_DOWN;
        }
        if (TimeUtils.millis() - lastShotTime >= coolDown) {
            lastShotTime = TimeUtils.millis();
            return true;
        }
        return false;
    }

    @Override
    public void hit() {
        livesLeft -= 1;
    }

    public void endHit() { livesLeft = 0; }

    public boolean isAlive() {
        return livesLeft > 0;
    }

    @Override
    public void conflictShip() {}
}