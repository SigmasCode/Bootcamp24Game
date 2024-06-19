(1)Первый пункт ТЗ:
Реализовал в игре метеорит, который двигается намного быстрее мусора, при столкновении с кораблём разом отнимает все жизни и игра на этом заканчивается. 
Этот метеорит можно уничтожить 3 попаданиями в него.

Его реализация:

Внёс изменения в класс GameObject(String texturePath, int x, int y, int width, int height, short cBits, World world, boolean kinematic), добавил новый параметр 
при создании boolean kinematic для определения какое тело надо создать(кинематическое или динамическое(true - значит надо, false - не надо), 
кинематическое тело не реагирует на силы). 
Внёс соответственно этот же параметр и в метод private Body createBody(float x, float y, World world, boolean kinematic).
При создании тела использовал if для определения => if (kinematic) def.type = BodyDef.BodyType.KinematicBody; else def.type = BodyDef.BodyType.DynamicBody;

Зашёл в GameSettings и добавил нужные для него характеристики:

public static long METEORITE_COOL_DOWN = 5000;

public static final short METEORITE_BIT = 16;

public static final int METEORITE_WIDTH = 60;
    
public static final int METEORITE_HEIGHT = 60;
    
public static final int METEORITE_VELOCITY = 30;

Затем добавил в папку assets текстурку для него и прописал к ней путь в GameResources:

public static final String METEORITE_IMG_PATH = "textures/meteorite.png";

После создал новый класс для метеорита MeteoriteObject и унаследовал его от GameObject.

public class MeteoriteObject extends GameObject {
    private static final int paddingHorizontal = 10;
    private int livesLeft;

    public MeteoriteObject(int width, int height, String texturePath, World world, boolean kinematic) {
        super(
                texturePath,
                width / 2 + paddingHorizontal + (new Random()).nextInt((GameSettings.SCREEN_WIDTH - 2 * paddingHorizontal - width)),
                GameSettings.SCREEN_HEIGHT + height / 2,
                width, height,
                GameSettings.METEORITE_BIT,
                world,
                kinematic
        );
        body.setLinearVelocity(new Vector2(0, -GameSettings.METEORITE_VELOCITY));
        livesLeft = 3;
    }

    public boolean isAlive() {
        return livesLeft > 0;
    }

    public boolean isInFrame() {
        return getY() + height / 2 > 0;
    }

    public void endHit() {
        livesLeft = 0;
    }

    @Override
    public void hit() {
        livesLeft -= 1;
    }
}

Написал конструкор, методы isAlive(), isInFrame(), hit() по аналогии с другими объектами. Также написал метод endHit() для полного обнуления жизней 
метеорита и корабля при их столкновении.

Затем пошёл в класс ContactManager в метод beginContact(Contact contact) и прописал там следующее:

if (cDef == GameSettings.METEORITE_BIT && cDef2 == GameSettings.SHIP_BIT
   || cDef2 == GameSettings.METEORITE_BIT && cDef == GameSettings.SHIP_BIT) {
    ((GameObject) fixA.getUserData()).endHit();
    ((GameObject) fixB.getUserData()).endHit();
}

if (cDef == GameSettings.METEORITE_BIT && cDef2 == GameSettings.BULLET_BIT
   || cDef2 == GameSettings.METEORITE_BIT && cDef == GameSettings.BULLET_BIT) {
    ((GameObject) fixA.getUserData()).hit();
    ((GameObject) fixB.getUserData()).hit();
}

Здесь обработал случаи, когда метеорит сталкивается с кораблём(в этом случае вызывается у обоих метод endHit() для полного обнуления их жизней) и 
когда метеорит сталкивается с пулей(в этом случае вызывается у обоих метод hit(), который отнимает одну жизнь у метеорита(у него их 3), а у 
пули ставит пометку о том, что было столкновение => wasHit = true;

Затем пошёл в класс GameSession и прописал в нём переменную для вычисления времени спамна нового метеорита long nextMeteoriteSpawnTime;
В методе startGame() прописал:
nextMeteoriteSpawnTime = sessionStartTime + (long) (GameSettings.METEORITE_COOL_DOWN
        * getTrashPeriodCoolDown());

Воспользовался математическим выражением как и у мусора для спамна новых метеоритов.
Написал метод shouldSpawnMeteorite() по аналогии с методом shouldSpawnTrash():
public boolean shouldSpawnMeteorite() {
        if (nextMeteoriteSpawnTime <= TimeUtils.millis()) {
            nextMeteoriteSpawnTime = TimeUtils.millis() + (long) (GameSettings.METEORITE_COOL_DOWN
            * getTrashPeriodCoolDown());
            return true;
        }
        return false;
    }

В нём возврашается true, если надо делать новый метеорит и false, если его делать не надо.

После я пошёл в класс GameScreen и прописал по аналогии с мусором массив для хранения всех объектов метеоритов => ArrayList<MeteoriteObject> meteoriteArray;

В конструкторе GameScreen(MyGdxGame myGdxGame) выделил память под массив meteoriteArray = new ArrayList<>();

В методе render(float delta) после условия начала игровой сессии if (gameSession.state == GameState.PLAYING) прописал условие для спамна метеоритов:

if (gameSession.shouldSpawnMeteorite() && MemoryManager.saveModeGameOn()) {
                MeteoriteObject meteoriteObject = new MeteoriteObject(
                        GameSettings.METEORITE_WIDTH, GameSettings.METEORITE_HEIGHT,
                        GameResources.METEORITE_IMG_PATH,
                        myGdxGame.world,
                        true
                );
                meteoriteArray.add(meteoriteObject);
            }

Для параметра kinematic передаю true, чтобы сделать тело метеорита кинематическим.
Если надо делать новый метеорит и включен режим хардкора MemoryManager.saveModeGameOn() - делаю метеорит. В обычном режиме метеоритов нет.
Ниже в этом же методе прописываю => updateMeteorites(); для их обновления.

Метод updateMeteorites() написал по аналогии с методом updateTrash() с использованием итераторов:

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


Если метеорит вышел за границы экрана - уничтожаем его тело. Если у него кончились жизни(у него их 3) - включаем звук уничтожения.

В методе draw() отрисовываю метеориты => for (MeteoriteObject meteorite : meteoriteArray) meteorite.draw(myGdxGame.batch);





(2)Второй пункт ТЗ:
Реализовал в обычном режиме бриллианты. Их надо ловить самими кораблём. Каждый пойманный бриллиант прибавляет к счёту +500 очков.

Его реализация:

Зашёл в GameSettings и добавил нужные для него характеристики:

public static long DIAMOND_COOL_DOWN = 7000;

public static final short DIAMOND_BIT = 32;

public static final int DIAMOND_WIDTH = 48;
    
public static final int DIAMOND_HEIGHT = 48;
    
public static final int DIAMOND_VELOCITY = 35;
    
public static final int DIAMOND_PRICE = 500;

Затем добавил в папку assets текстурку для него и прописал к ней путь в GameResources:

public static final String DIAMOND_IMG_PATH = "textures/diamond.png";

После создал новый класс для метеорита DiamondObject и унаследовал его от GameObject.

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

В классе GameObject прописал новый метод conflictShip(), он нужен в случае столкновения корабля и бриллианта.
У бриллианта одна жизнь, при столкновении она отнимается и он удаляется.

В классе ContactManager в методе beginContact(Contact contact) прописал условия столкновения бриллианта и корабля, и бриллианта и пули.
if (cDef == GameSettings.DIAMOND_BIT && cDef2 == GameSettings.SHIP_BIT
   || cDef2 == GameSettings.DIAMOND_BIT && cDef == GameSettings.SHIP_BIT) {
    ((GameObject) fixA.getUserData()).conflictShip();
    ((GameObject) fixB.getUserData()).conflictShip();
}

if (cDef == GameSettings.DIAMOND_BIT && cDef2 == GameSettings.BULLET_BIT
    || cDef2 == GameSettings.DIAMOND_BIT && cDef == GameSettings.BULLET_BIT) {
    ((GameObject) fixA.getUserData()).hit();
    ((GameObject) fixB.getUserData()).hit();
}


При столкновении бриллианта и корабля: вызывается у обоих объектов метод conflictShip(), он снимает единственную жизнь бриллианта, а с кораблём ничего не делает.
При столкновении бриллианта и пули: вызывается у обоих объектов метод hit(), с бриллиантов ничего не происходит, а у пули ставится метка wasHit = true; и далее 
она удаляется.

В классе GameSession прописал переменную для определения времени спамна нового бриллианта => long nextDiamondSpawnTime; и прописал переменную для подсчёта
количества собранных бриллиантов => int diamonds;

В методе startGame() поставил начальное количество бриллиантов => diamonds = 0;
По аналогии с мусором и метеоритом прописал:
nextDiamondSpawnTime = sessionStartTime + (long) (GameSettings.DIAMOND_COOL_DOWN
        * getTrashPeriodCoolDown());


По аналогии с мусором и метеоритом прописал метод для определения надобности спамна нового бриллианта:
public boolean shouldSpawnDiamond() {
        if (nextDiamondSpawnTime <= TimeUtils.millis()) {
            nextDiamondSpawnTime = TimeUtils.millis() + (long) (GameSettings.DIAMOND_COOL_DOWN
            * getTrashPeriodCoolDown());
            return true;
        }
        return false;
    }


Прописал метод для увеличения количества собранных бриллиантов:
public void gotDiamond() {
        diamonds += 1;
    }


В методе updateScore() изменил подсчёт очков:
public void updateScore() {
        score = (int) (TimeUtils.millis() - sessionStartTime) / 100 + destructedTrashNumber * 100 + diamonds * GameSettings.DIAMOND_PRICE;
    }

Теперь к счёту прибавляется ещё и количество собранных бриллиантов помноженное на стоимость одного(500).


В классе GameScreen по аналогии с мусором, метеоритом и пулей прописал новый массив для хранения всех 
объектов бриллианта => ArrayList<DiamondObject> diamondArray;

В конструкторе GameScreen(MyGdxGame myGdxGame) выделил память для массива diamondArray => diamondArray = new ArrayList<>();

В методе render(float delta) после условия активной игровой сессии if (gameSession.state == GameState.PLAYING) по аналогии с мусором и метеоритом прописал условие
создания бриллианта:
if (gameSession.shouldSpawnDiamond() && !MemoryManager.saveModeGameOn()) {
                DiamondObject diamondObject = new DiamondObject(
                        GameSettings.DIAMOND_WIDTH, GameSettings.DIAMOND_HEIGHT,
                        GameResources.DIAMOND_IMG_PATH,
                        myGdxGame.world,
                        true
                );
                diamondArray.add(diamondObject);
            }

При условии надобности спамна бриллианта и простого режима(не хардкора) !MemoryManager.saveModeGameOn() - делаем новый бриллиант.
Для параметра kinematic передаю true, чтобы сделать тело бриллианта кинематическим.

Ниже в этом методе прописываю updateDiamonds(); для обновления бриллиантов.
Метод updateDiamonds(); написал по аналогии с мусором и метеоритом с использованием итераторов:

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

Здесь при условии, что бриллиант не живой if (!diamond.isAlive()) вызываю метод gameSession.gotDiamond(); для увеличения количества собранных игроком бриллиантов.

В методе draw() рисую бриллианты => for (DiamondObject diamond : diamondArray) diamond.draw(myGdxGame.batch);





(3)Третий пункт ТЗ:
Сделал два режима игры: простой и хардкор. В простом режиме нет метеоритов и падают иногда бриллианты. В режиме хардкора нет бриллиантов и летят быстрые метеориты,
которые можно уничтожить только тремя точными попаданиями. Если корабль столкнётся с метеоритом, то игра сразу закончится:(

Реализация:

В классе SettingsScreen прописал переменную TextView hardcoreGameView; для вывода на экран режима игры.

В конструкторе SettingsScreen(MyGdxGame myGdxGame) делаю надпись для выбора режима по аналогии с другими:
hardcoreGameView = new TextView(
                myGdxGame.commonWhiteFont,
                173, 540,
                "hardcore: " + translateStateToText(MemoryManager.saveModeGameOn())
        );

В методе render(float delta) отрисовываю => hardcoreGameView.draw(myGdxGame.batch);

В методе handleInput() обрабатываю нажатие:
if (hardcoreGameView.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                MemoryManager.saveModeGame(!MemoryManager.saveModeGameOn());
                hardcoreGameView.setText("hardcore: " + translateStateToText(MemoryManager.saveModeGameOn()));
            }

Сохраняется новое значение выбора, противоположное тому, что было уже установлено. 
В классе MemoryManager прописал метод для сохранения и получения данных о выборе сложности игры:
public static void saveModeGame(boolean isOn) {
        preferences.putBoolean("hardcoreGame", isOn);
        preferences.flush();
    }

Метод сохраняет выбор сложности, если true - хардкор, false - простой.

public static boolean saveModeGameOn() { return preferences.getBoolean("hardcoreGame", false); }

Метод получает значение выбора сложности. По умлочанию стоит false - простой режим.





(4)Четвёртый пункт ТЗ:
Реализовал возможность выбрать пользователю корабль. Их два вида: стандартный и ускоренный(SpaceX). Ускоренный по размерам меньше, быстрее двигается и быстрее
стреляет. Оба корабля доступны во всех режимах.

В классе GameSettings поставил для корабля SpaceX улучшенные характеристики:

public static float SPACESHIP_FORCE_RATIO = 11;
    
public static int SPACE_SHOOTING_COOL_DOWN = 500;

SpaceX стреляет в два раза быстрее и скорость передвижения чуть больше.

В классе GameResources добавил новую текстурку для SpaceX:

public static final String SPACESHIP_IMG_PATH = "textures/spacex.png";


В классе MemoryManager написал метод для сохранения выбранного корабля:
public static void saveSpaceShip(boolean isOn) {
        preferences.putBoolean("SpaceX", isOn);
        preferences.flush();
    }

true - выбран SpaceX, false - стандартный.

И написал метод для получения выбора пользоватлея:
public static boolean saveSpaceShipOn() { return preferences.getBoolean("SpaceX", false); }

По умолчанию стоит false - стандартный корабль.


В классе SettingsScreen прописал переменную TextView spaceshipView; для вывода на экран выбора корабля.

В конструкторе SettingsScreen(MyGdxGame myGdxGame) делаю надпись для выбора корабля:
spaceshipView = new TextView(
                myGdxGame.commonWhiteFont,
                173, 480,
                "SpaceX: " + translateStateToText(MemoryManager.saveSpaceShipOn())
        );

ON - выбран корабль SpaceX, OFF - выбран стандартный корабль.

В методе render(float delta) вывожу информацию о выборе => spaceshipView.draw(myGdxGame.batch);

В методе handleInput() обрабатываю нажатие:
if (spaceshipView.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                MemoryManager.saveSpaceShip(!MemoryManager.saveSpaceShipOn());
                spaceshipView.setText("SpaceX: " + translateStateToText(MemoryManager.saveSpaceShipOn()));
            }

Сохраняется новое значение выбора, противоположное тому, что было уже установлено !MemoryManager.saveSpaceShipOn().

В классе GameScreen делаю три новых переменных для хранения конфигурации корабля:
String texture;
  
int width;
    
int height;


В конструкторе GameScreen(MyGdxGame myGdxGame) перед созданием объекта корабля вызываю метод updateShip();

Метод updateShip():

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

Он изменяет конфигурацию корабля в зависимости от выбора пользователя.
Переменные с текстурой, шириной и высотой корабля обновляются в зависимости от выбора.
Если MemoryManager.saveSpaceShipOn() возвращает true - выбран SpaceX, иначе выбран стандартный.

После вызова updateShip() создаётся объект выбранного корабля:
shipObject = new ShipObject(
                GameSettings.SCREEN_WIDTH / 2, 150,
                width, height,
                texture,
                myGdxGame.world,
                false
        );

Для параметра kinematic передаём false для создания динамического тела.

В классе ShipObject добавил новую переменную для хранения времени перезарядки => int coolDown;
Добавил переменную float forceRatio; 

Поправил конструктор ShipObject:
public ShipObject(int x, int y, int width, int height, String texturePath, World world, boolean kinematic) {
        super(texturePath, x, y, width, height, GameSettings.SHIP_BIT, world, kinematic);
        body.setLinearDamping(10);
        livesLeft = 3;
        updateCoolDown();
        updateForceRatio();
    }

В нём в конце вызываются методы обновления характеристик корабля.


Написал метод updateCoolDown():
public void updateCoolDown() {
        if (MemoryManager.saveSpaceShipOn()) {
            coolDown = GameSettings.SPACE_SHOOTING_COOL_DOWN;
        } else {
            coolDown = GameSettings.SHOOTING_COOL_DOWN;
        }
    }

В нём меняется время перезарядки корабля в зависимости от выбранного.
Если MemoryManager.saveSpaceShipOn() возвращает true - выбран SpaceX, иначе выбран стандартный.

Написал метод updateForceRatio():
public void updateForceRatio() {
        if (MemoryManager.saveSpaceShipOn()) {
            forceRatio = GameSettings.SPACESHIP_FORCE_RATIO;
        } else {
            forceRatio = GameSettings.SHIP_FORCE_RATIO;
        }
    }

В нём также меняется сила в зависимости от выбранного корабля.

Поправил метод needToShoot(): 
public boolean needToShoot() {
        if (TimeUtils.millis() - lastShotTime >= coolDown) {
            lastShotTime = TimeUtils.millis();
            return true;
        }
        return false;
    }

Поправил метод move(Vector3 vector3):
public void move(Vector3 vector3) {
        body.applyForceToCenter(new Vector2(
                        (vector3.x - getX()) * forceRatio,
                        (vector3.y - getY()) * forceRatio),
                true
        );
    }





(5)Пятый пункт ТЗ:


Методы updateTrash() и updateBullets() выглядели так:

private void updateTrash() {
        for (int i = 0; i < trashArray.size(); i++) {

            boolean hasToBeDestroyed = !trashArray.get(i).isAlive() || !trashArray.get(i).isInFrame();

            if (!trashArray.get(i).isAlive()) {
                gameSession.destructionRegistration();
                if (myGdxGame.audioManager.isSoundOn) myGdxGame.audioManager.explosionSound.play(0.2f);
            }

            if (hasToBeDestroyed) {
                myGdxGame.world.destroyBody(trashArray.get(i).body);
                trashArray.remove(i--);
            }
        }
    }

    private void updateBullets() {
        for (int i = 0; i < bulletArray.size(); i++) {
            if (bulletArray.get(i).hasToBeDestroyed()) {
                myGdxGame.world.destroyBody(bulletArray.get(i).body);
                bulletArray.remove(i--);
            }
        }
    }


Переписал их вот так:

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



Также при реализации фич в методах updateMeteorites() и updateDiamonds() использовал итераторы:

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


Почему исходная реализация была плохой:

В исходном коде происходит итерация по массиву, с использованием цикла for, и модификация массива, удалением элементов во время итерации. 
Когда мы удаляем элемент из массива, индексы оставшихся элементов смещаются, но счётчик циклов i не учитывает этого. 
Это может привести к пропуску элементов или возникновению исключения IndexOutOfBoundsException.

Использование i-- для компенсации удаления.

В исходном коде мы пытаемся компенсировать удаление, уменьшая счетчик циклов i с помощью i--. 
Однако это ненадёжное решение, поскольку оно всё равно может привести к проблемам, если потребуется удалить несколько элементов подряд.

Использование итераторов - лучший подход.

Итераторы предназначены для обработки изменений в коллекции во время выполнения итерации. 
Когда мы вызываем iterator.remove(), итератор обновляет свое внутреннее состояние, чтобы отразить удаление. 
Это гарантирует, что итерация будет выполняться корректно, без пропуска элементов или проблем с индексом.
Используя итераторы, мы можем безопасно удалять элементы из коллекции во время итерации по ней, 
не беспокоясь о сложностях изменения коллекции во время итерации.




































