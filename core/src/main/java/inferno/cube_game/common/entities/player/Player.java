package inferno.cube_game.common.entities.player;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Player {
    private Vector3 position;
    private Vector3 velocity;  // Added to store movement speed
    private BoundingBox boundingBox;
    private float gravity = -9.8f;  // Adjust this for desired gravity strength
    private boolean isOnGround = false;  // Track if the player is on solid ground
    private float jumpStrength = 10f;  // Set a jump strength


    public Player(Vector3 initialPosition, float width, float height) {
        this.position = initialPosition;
        this.velocity = new Vector3(0, 0, 0);  // Start with no movement
        setBoundingBox(width, height);
    }

    public void jump() {
        if (isOnGround) {
            velocity.y = jumpStrength;
            isOnGround = false;
        }
    }

    // Check if player is grounded or not
    public boolean isOnGround() {
        return isOnGround;
    }

    public void setOnGround(boolean onGround) {
        this.isOnGround = onGround;
    }

    private void setBoundingBox(float width, float height) {
        Vector3 min = new Vector3(position.x - width / 2, position.y, position.z - width / 2);
        Vector3 max = new Vector3(position.x + width / 2, position.y + height, position.z + width / 2);
        boundingBox = new BoundingBox(min, max);
    }

    public void update(float deltaTime) {
        if (!isOnGround) {
            // Apply gravity to the player's velocity (downward force on Y-axis)
            velocity.y += gravity * deltaTime;
        }
        move(new Vector3(velocity.x * deltaTime, velocity.y * deltaTime, velocity.z * deltaTime));
    }

    public void move(Vector3 delta) {
        position.add(delta);
        setBoundingBox(boundingBox.getWidth(), boundingBox.getHeight());
    }

    // Getter for velocity
    public Vector3 getVelocity() {
        return velocity;
    }

    // Setter for velocity (to allow control over movement)
    public void setVelocity(Vector3 velocity) {
        this.velocity.set(velocity);
    }

}
