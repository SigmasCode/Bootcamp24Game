package com.mygdx.game.objects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.GameSettings;

import java.util.Random;

public class DiamondObject extends GameObject{
    private static final int paddingHorizontal = 10;
    private int livesLeft;

    public DiamondObject(int width, int height, String texturePath, World world, boolean kinematic) {
        super(
                texturePath,
                width / 2 + paddingHorizontal + (new Random()).nextInt((GameSettings.SCREEN_WIDTH - 2 * paddingHorizontal - width)),
                GameSettings.SCREEN_HEIGHT + height / 2,
                width, height,
                GameSettings.DIAMOND_BIT,
                world,
                kinematic
        );
        body.setLinearVelocity(new Vector2(0, -GameSettings.DIAMOND_VELOCITY));
        livesLeft = 1;
    }

    public boolean isAlive() {
        return livesLeft > 0;
    }

    public boolean isInFrame() {
        return getY() + height / 2 > 0;
    }

    @Override
    public void conflictShip() {
        livesLeft -= 1;
    }

    @Override
    public void hit() {
    }
}
