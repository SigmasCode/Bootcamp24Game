package com.mygdx.game;


public class GameSettings {

    // Device settings

    public static final int SCREEN_WIDTH = 720;
    public static final int SCREEN_HEIGHT = 1280;

    // Physics settings

    public static final float STEP_TIME = 1f / 60f;
    public static final int VELOCITY_ITERATIONS = 6;
    public static final int POSITION_ITERATIONS = 6;
    public static final float SCALE = 0.05f;

    public static float SHIP_FORCE_RATIO = 10;
    public static float TRASH_VELOCITY = 20;
    public static long STARTING_TRASH_APPEARANCE_COOL_DOWN = 2000; // in [ms] - milliseconds
    public static int BULLET_VELOCITY = 200; // in [m/s] - meter per second
    public static int SHOOTING_COOL_DOWN = 1000; // in [ms] - milliseconds
    public static long METEORITE_COOL_DOWN = 5000;
    public static long DIAMOND_COOL_DOWN = 7000;

    public static final short TRASH_BIT = 2;
    public static final short SHIP_BIT = 4;
    public static final short BULLET_BIT = 8;
    public static final short METEORITE_BIT = 16;
    public static final short DIAMOND_BIT = 32;

    // Object sizes

    public static final int SHIP_WIDTH = 150;
    public static final int SHIP_HEIGHT = 150;
    public static final int TRASH_WIDTH = 140;
    public static final int TRASH_HEIGHT = 100;
    public static final int BULLET_WIDTH = 15;
    public static final int BULLET_HEIGHT = 45;
    public static final int METEORITE_WIDTH = 60;
    public static final int METEORITE_HEIGHT = 60;
    public static final int METEORITE_VELOCITY = 30;
    public static final int DIAMOND_WIDTH = 48;
    public static final int DIAMOND_HEIGHT = 48;
    public static final int DIAMOND_VELOCITY = 35;
    public static final int DIAMOND_PRICE = 500;
    public static final int SPACESHIP_WIDTH = 120;
    public static final int SPACESHIP_HEIGHT = 120;
    public static float SPACESHIP_FORCE_RATIO = 11;
    public static int SPACE_SHOOTING_COOL_DOWN = 500;


}