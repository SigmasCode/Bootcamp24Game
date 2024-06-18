package managers;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.GameSession;
import com.mygdx.game.GameSettings;
import com.mygdx.game.objects.GameObject;

public class ContactManager {

    World world;
    GameSession gameSession;

    public ContactManager(World world) {
        this.world = world;

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {

                Fixture fixA = contact.getFixtureA();
                Fixture fixB = contact.getFixtureB();

                int cDef = fixA.getFilterData().categoryBits;
                int cDef2 = fixB.getFilterData().categoryBits;

                if (cDef == GameSettings.TRASH_BIT && cDef2 == GameSettings.BULLET_BIT
                        || cDef2 == GameSettings.TRASH_BIT && cDef == GameSettings.BULLET_BIT
                        || cDef == GameSettings.TRASH_BIT && cDef2 == GameSettings.SHIP_BIT
                        || cDef2 == GameSettings.TRASH_BIT && cDef == GameSettings.SHIP_BIT) {

                    ((GameObject) fixA.getUserData()).hit();
                    ((GameObject) fixB.getUserData()).hit();

                }

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
            }

            @Override
            public void endContact(Contact contact) {
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });

    }

}
