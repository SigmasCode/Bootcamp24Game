package components;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

public class View implements Disposable {

    float x;
    float y;

    float width;
    float height;

    public View(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public View(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public boolean isHit(float tx, float ty) {
        return (tx >= x && tx <= x + width && ty >= y && ty <= y + height);
    }

    public void draw(SpriteBatch batch) {
    }

    @Override
    public void dispose() {
    }
}