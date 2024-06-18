package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import components.ButtonView;
import components.ImageView;
import components.LiveView;
import components.MovingBackgroundView;
import components.RecordsListView;
import components.TextView;
import managers.ContactManager;
import managers.MemoryManager;

import com.mygdx.game.GameResources;
import com.mygdx.game.GameSession;
import com.mygdx.game.GameSettings;
import com.mygdx.game.GameState;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.objects.BulletObject;
import com.mygdx.game.objects.DiamondObject;
import com.mygdx.game.objects.MeteoriteObject;
import com.mygdx.game.objects.ShipObject;
import com.mygdx.game.objects.TrashObject;

import java.util.ArrayList;
import java.util.Iterator;


public class GameScreen extends ScreenAdapter {

    MyGdxGame myGdxGame;
    GameSession gameSession;
    ShipObject shipObject;

    ArrayList<TrashObject> trashArray;
    ArrayList<BulletObject> bulletArray;
    ArrayList<MeteoriteObject> meteoriteArray;
    ArrayList<DiamondObject> diamondArray;

    ContactManager contactManager;

    // PLAY state UI
    MovingBackgroundView backgroundView;
    ImageView topBlackoutView;
    LiveView liveView;
    TextView scoreTextView;
    ButtonView pauseButton;

    // PAUSED state UI
    ImageView fullBlackoutView;
    TextView pauseTextView;
    ButtonView homeButton;
    ButtonView continueButton;

    // ENDED state UI
    TextView recordsTextView;
    RecordsListView recordsListView;
    ButtonView homeButton2;
    String texture;
    int width;
    int height;

    public GameScreen(MyGdxGame myGdxGame) {
        this.myGdxGame = myGdxGame;
        gameSession = new GameSession();

        contactManager = new ContactManager(myGdxGame.world);

        trashArray = new ArrayList<>();
        bulletArray = new ArrayList<>();
        meteoriteArray = new ArrayList<>();
        diamondArray = new ArrayList<>();

        updateShip();

        shipObject = new ShipObject(
                GameSettings.SCREEN_WIDTH / 2, 150,
                width, height,
                texture,
                myGdxGame.world,
                false
        );

        backgroundView = new MovingBackgroundView(GameResources.BACKGROUND_IMG_PATH);
        topBlackoutView = new ImageView(0, 1180, GameResources.BLACKOUT_TOP_IMG_PATH);
        liveView = new LiveView(305, 1215);
        scoreTextView = new TextView(myGdxGame.commonWhiteFont, 50, 1215);
        pauseButton = new ButtonView(
                605, 1200,
                46, 54,
                GameResources.PAUSE_IMG_PATH
        );

        fullBlackoutView = new ImageView(0, 0, GameResources.BLACKOUT_FULL_IMG_PATH);
        pauseTextView = new TextView(myGdxGame.largeWhiteFont, 282, 842, "Pause");
        homeButton = new ButtonView(
                138, 695,
                200, 70,
                myGdxGame.commonBlackFont,
                GameResources.BUTTON_SHORT_BG_IMG_PATH,
                "Home"
        );
        continueButton = new ButtonView(
                393, 695,
                200, 70,
                myGdxGame.commonBlackFont,
                GameResources.BUTTON_SHORT_BG_IMG_PATH,
                "Continue"
        );

        recordsListView = new RecordsListView(myGdxGame.commonWhiteFont, 690);
        recordsTextView = new TextView(myGdxGame.largeWhiteFont, 206, 842, "Last records");
        homeButton2 = new ButtonView(
                280, 365,
                160, 70,
                myGdxGame.commonBlackFont,
                GameResources.BUTTON_SHORT_BG_IMG_PATH,
                "Home"
        );

    }

    @Override
    public void show() {
        restartGame();
    }

    @Override
    public void render(float delta) {

        handleInput();

        if (gameSession.state == GameState.PLAYING) {
            if (gameSession.shouldSpawnTrash()) {
                TrashObject trashObject = new TrashObject(
                        GameSettings.TRASH_WIDTH, GameSettings.TRASH_HEIGHT,
                        GameResources.TRASH_IMG_PATH,
                        myGdxGame.world,
                        false
                );
                trashArray.add(trashObject);
            }

            if (gameSession.shouldSpawnMeteorite() && MemoryManager.saveModeGameOn()) {
                MeteoriteObject meteoriteObject = new MeteoriteObject(
                        GameSettings.METEORITE_WIDTH, GameSettings.METEORITE_HEIGHT,
                        GameResources.METEORITE_IMG_PATH,
                        myGdxGame.world,
                        true
                );
                meteoriteArray.add(meteoriteObject);
            }

            if (gameSession.shouldSpawnDiamond() && !MemoryManager.saveModeGameOn()) {
                DiamondObject diamondObject = new DiamondObject(
                        GameSettings.DIAMOND_WIDTH, GameSettings.DIAMOND_HEIGHT,
                        GameResources.DIAMOND_IMG_PATH,
                        myGdxGame.world,
                        true
                );
                diamondArray.add(diamondObject);
            }

            if (shipObject.needToShoot()) {
                BulletObject laserBullet = new BulletObject(
                        shipObject.getX(), shipObject.getY() + shipObject.height / 2,
                        GameSettings.BULLET_WIDTH, GameSettings.BULLET_HEIGHT,
                        GameResources.BULLET_IMG_PATH,
                        myGdxGame.world,
                        false
                );
                bulletArray.add(laserBullet);
                if (myGdxGame.audioManager.isSoundOn) myGdxGame.audioManager.shootSound.play();
            }

            if (!shipObject.isAlive()) {
                gameSession.endGame();
                recordsListView.setRecords(MemoryManager.loadRecordsTable());
            }

            updateTrash();

            updateMeteorites();
            updateDiamonds();

            updateBullets();
            backgroundView.move();
            gameSession.updateScore();

            scoreTextView.setText("Score: " + gameSession.getScore());
            liveView.setLeftLives(shipObject.getLiveLeft());

            myGdxGame.stepWorld();
        }

        draw();
    }

    private void handleInput() {
        if (Gdx.input.isTouched()) {
            myGdxGame.touch = myGdxGame.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

            switch (gameSession.state) {
                case PLAYING:
                    if (pauseButton.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                        gameSession.pauseGame();
                    }
                    shipObject.move(myGdxGame.touch);
                    break;

                case PAUSED:
                    if (continueButton.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                        gameSession.resumeGame();
                    }
                    if (homeButton.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                        myGdxGame.setScreen(myGdxGame.menuScreen);
                    }
                    break;

                case ENDED:

                    if (homeButton2.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                        myGdxGame.setScreen(myGdxGame.menuScreen);
                    }
                    break;
            }

        }
    }

    private void draw() {

        myGdxGame.camera.update();
        myGdxGame.batch.setProjectionMatrix(myGdxGame.camera.combined);
        ScreenUtils.clear(Color.CLEAR);

        myGdxGame.batch.begin();
        backgroundView.draw(myGdxGame.batch);
        for (TrashObject trash : trashArray) trash.draw(myGdxGame.batch);

        for (MeteoriteObject meteorite : meteoriteArray) meteorite.draw(myGdxGame.batch);
        for (DiamondObject diamond : diamondArray) diamond.draw(myGdxGame.batch);

        shipObject.draw(myGdxGame.batch);
        for (BulletObject bullet : bulletArray) bullet.draw(myGdxGame.batch);
        topBlackoutView.draw(myGdxGame.batch);
        scoreTextView.draw(myGdxGame.batch);
        liveView.draw(myGdxGame.batch);
        pauseButton.draw(myGdxGame.batch);

        if (gameSession.state == GameState.PAUSED) {
            fullBlackoutView.draw(myGdxGame.batch);
            pauseTextView.draw(myGdxGame.batch);
            homeButton.draw(myGdxGame.batch);
            continueButton.draw(myGdxGame.batch);
        } else if (gameSession.state == GameState.ENDED) {
            fullBlackoutView.draw(myGdxGame.batch);
            recordsTextView.draw(myGdxGame.batch);
            recordsListView.draw(myGdxGame.batch);
            homeButton2.draw(myGdxGame.batch);
        }

        myGdxGame.batch.end();

    }
// check update
private void updateTrash() {
    Iterator<TrashObject> iterator = trashArray.iterator();
    while (iterator.hasNext()) {
        TrashObject trash = iterator.next();
        boolean hasToBeDestroyed = !trash.isAlive() || !trash.isInFrame();

        if (!trash.isAlive()) {
            gameSession.destructionRegistration();
            if (myGdxGame.audioManager.isSoundOn) myGdxGame.audioManager.explosionSound.play(0.2f);
        }

        if (hasToBeDestroyed) {
            myGdxGame.world.destroyBody(trash.body);
            iterator.remove();
        }
    }
}

    private void updateMeteorites() {
        Iterator<MeteoriteObject> iterator = meteoriteArray.iterator();
        while (iterator.hasNext()) {
            MeteoriteObject meteorite = iterator.next();
            boolean hasToBeDestroyed = !meteorite.isAlive() || !meteorite.isInFrame();

            if (!meteorite.isAlive()) {
                if (myGdxGame.audioManager.isSoundOn) myGdxGame.audioManager.explosionSound.play(0.2f);
            }

            if (hasToBeDestroyed) {
                myGdxGame.world.destroyBody(meteorite.body);
                iterator.remove();
            }
        }
    }

    private void updateDiamonds() {
        Iterator<DiamondObject> iterator = diamondArray.iterator();
        while (iterator.hasNext()) {
            DiamondObject diamond = iterator.next();
            boolean hasToBeDestroyed = !diamond.isAlive() || !diamond.isInFrame();

            if (!diamond.isAlive()) {
                gameSession.gotDiamond();
                if (myGdxGame.audioManager.isSoundOn) myGdxGame.audioManager.explosionSound.play(0.2f);
            }

            if (hasToBeDestroyed) {
                myGdxGame.world.destroyBody(diamond.body);
                iterator.remove();
            }
        }
    }

    public void updateShip() {
        if (MemoryManager.saveSpaceShipOn()) {
            texture = GameResources.SPACESHIP_IMG_PATH;
            width = GameSettings.SPACESHIP_WIDTH;
            height = GameSettings.SPACESHIP_HEIGHT;
        } else {
            texture = GameResources.SHIP_IMG_PATH;
            width = GameSettings.SHIP_WIDTH;
            height = GameSettings.SHIP_HEIGHT;
        }
    }

    private void updateBullets() {
        Iterator<BulletObject> iterator = bulletArray.iterator();
        while (iterator.hasNext()) {
            BulletObject bullet = iterator.next();
            if (bullet.hasToBeDestroyed()) {
                myGdxGame.world.destroyBody(bullet.body);
                iterator.remove();
            }
        }
    }
//check update
    private void restartGame() {

        for (int i = 0; i < trashArray.size(); i++) {
            myGdxGame.world.destroyBody(trashArray.get(i).body);
            trashArray.remove(i--);
        }

        if (shipObject != null) {
            myGdxGame.world.destroyBody(shipObject.body);
        }

        updateShip();

        shipObject = new ShipObject(
                GameSettings.SCREEN_WIDTH / 2, 150,
                width, height,
                texture,
                myGdxGame.world,
                false
        );

        bulletArray.clear();
        gameSession.startGame();
    }

}