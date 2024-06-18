package com.mygdx.game.objects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.GameSettings;


public class BulletObject extends GameObject {

    public boolean wasHit;

    public BulletObject(int x, int y, int width, int height, String texturePath, World world, boolean kinematic) {
        super(texturePath, x, y, width, height, GameSettings.BULLET_BIT, world, kinematic);
        body.setLinearVelocity(new Vector2(0, GameSettings.BULLET_VELOCITY));
        body.setBullet(true);
        wasHit = false;
    }

    public boolean hasToBeDestroyed() {
        return wasHit || (getY() - height / 2 > GameSettings.SCREEN_HEIGHT);
    }

    @Override
    public void hit() {
        wasHit = true;
    }

    @Override
    public void conflictShip() {
        wasHit = true;
    }
}