package inferno.cube_game.common.entities;

import com.badlogic.gdx.math.Vector3;

import java.io.Serializable;

public abstract class Entity implements Serializable {
    protected Vector3 position;
    protected Vector3 velocity;
    protected float width, height;

    public Entity(Vector3 position, float width, float height) {
        this.position = position;
        this.width = width;
        this.height = height;
        this.velocity = new Vector3();
    }

    // Abstract method to update the entity's state
    public abstract void update(float deltaTime);

    // Getters and setters
    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 position) {
        this.position = position;
    }

    public Vector3 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3 velocity) {
        this.velocity = velocity;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}


