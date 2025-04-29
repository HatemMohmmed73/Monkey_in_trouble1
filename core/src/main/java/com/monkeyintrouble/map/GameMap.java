package com.monkeyintrouble.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.monkeyintrouble.entities.SawTrap;

public class GameMap implements Disposable {
    private static final int TILE_SIZE = 32;
    private final Array<Room> rooms;
    private final Texture[] tileTextures;
    private final Array<Box> boxes;
    private final Array<SawTrap> sawTraps;

    public static class Room {
        final int[][] mapData;
        final Array<Rectangle> collisionBoxes;
        final int offsetX;
        final int offsetY;

        public Room(int[][] mapData, int offsetX, int offsetY) {
            this.mapData = mapData;
            this.collisionBoxes = new Array<>();
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }

    public static class Box {
        public Rectangle bounds;
        public int originalX;
        public int originalY;
        public int roomIndex;

        public Box(Rectangle bounds, int originalX, int originalY, int roomIndex) {
            this.bounds = bounds;
            this.originalX = originalX;
            this.originalY = originalY;
            this.roomIndex = roomIndex;
        }
    }

    public GameMap(int[][] mainRoom, int[][] rightTopRoom, int[][] rightBottomRoom) {
        this.rooms = new Array<>();
        this.boxes = new Array<>();
        this.sawTraps = new Array<>();

        // Add main room at origin (0,0)
        rooms.add(new Room(mainRoom, 0, 0));

        // Position right rooms with 2 tile gap
        int rightRoomsX = mainRoom[0].length + 2;

        // Add right top room
        rooms.add(new Room(rightTopRoom, rightRoomsX, 0));

        // Add right bottom room directly below the top room
        rooms.add(new Room(rightBottomRoom, rightRoomsX, rightTopRoom.length));

        // Load tile textures
        tileTextures = new Texture[71];
        for (int i = 0; i < tileTextures.length; i++) {
            try {
                tileTextures[i] = new Texture(Gdx.files.internal(i + ".png"));
            } catch (Exception e) {
                Gdx.app.log("GameMap", "Failed to load texture: " + i + ".png");
            }
        }

        // Create collision boxes and track boxes for all rooms
        for (int roomIndex = 0; roomIndex < rooms.size; roomIndex++) {
            Room room = rooms.get(roomIndex);
            createCollisionBoxes(room, roomIndex);
        }
    }

    private void createCollisionBoxes(Room room, int roomIndex) {
        for (int y = 0; y < room.mapData.length; y++) {
            for (int x = 0; x < room.mapData[y].length; x++) {
                int tileId = room.mapData[y][x];
                TileType type = TileType.fromId(tileId);

                float worldY = (room.mapData.length - y - 1) * TILE_SIZE;
                float worldX = (x + room.offsetX) * TILE_SIZE;
                worldY += (room.offsetY * TILE_SIZE);

                Rectangle box = new Rectangle(worldX, worldY, TILE_SIZE, TILE_SIZE);

                if (type.isCollidable()) {
                    room.collisionBoxes.add(box);
                }
                if (type.isPushable()) {
                    System.out.println("Creating box at: " + x + ", " + y + " in room " + roomIndex);
                    boxes.add(new Box(box, x, y, roomIndex));
                    // Replace the box tile with floor (1.png)
                    room.mapData[y][x] = 1;
                }
                // Handle saw trap tiles
                if (tileId == 43) { // Left trap tile
                    sawTraps.add(new SawTrap(worldX, worldY));
                    System.out.println("Saw trap initialized at: " + worldX + ", " + worldY);

                    // Replace trap tiles with floor
                    for (int i = 0; i < 3; i++) {
                        if (x + i < room.mapData[y].length) {
                            room.mapData[y][x + i] = 1;
                        }
                    }
                }
            }
        }
    }

    public void update(float deltaTime) {
        // Update saw traps
        for (SawTrap sawTrap : sawTraps) {
            sawTrap.update(deltaTime);
        }
    }

    public void render(SpriteBatch batch) {
        // First render the base tiles
        for (Room room : rooms) {
            for (int y = 0; y < room.mapData.length; y++) {
                for (int x = 0; x < room.mapData[y].length; x++) {
                    int tileId = room.mapData[y][x];
                    // Skip invalid tile IDs and use floor texture (1.png) for missing textures
                    if (tileId < 0 || tileId >= tileTextures.length) {
                        tileId = 1; // Use floor texture for invalid tiles
                    }
                    if (tileTextures[tileId] != null) {
                        float worldY = (room.mapData.length - y - 1) * TILE_SIZE;
                        batch.draw(
                            tileTextures[tileId],
                            (x + room.offsetX) * TILE_SIZE,
                            worldY + (room.offsetY * TILE_SIZE),
                            TILE_SIZE,
                            TILE_SIZE
                        );
                    }
                }
            }
        }

        // Render saw traps
        for (SawTrap sawTrap : sawTraps) {
            sawTrap.render(batch, tileTextures);
        }

        // Then render the boxes on top
        for (Box box : boxes) {
            batch.draw(
                tileTextures[42], // Box texture
                box.bounds.x,
                box.bounds.y,
                TILE_SIZE,
                TILE_SIZE
            );
        }
    }

    public boolean isColliding(Rectangle bounds) {
        for (Room room : rooms) {
            for (Rectangle box : room.collisionBoxes) {
                if (box.overlaps(bounds)) {
                    return true;
                }
            }
        }
        return false;
    }

    public TileType getTileTypeAt(int x, int y) {
        for (Room room : rooms) {
            int localX = x - room.offsetX;
            int localY = y - room.offsetY;
            if (localY >= 0 && localY < room.mapData.length &&
                localX >= 0 && localX < room.mapData[localY].length) {
                return TileType.fromId(room.mapData[localY][localX]);
            }
        }
        return TileType.EMPTY;
    }

    public int getTileSize() {
        return TILE_SIZE;
    }

    public boolean tryPushBox(Rectangle playerBounds, float deltaX, float deltaY) {
        for (Box box : boxes) {
            if (box.bounds.overlaps(playerBounds)) {
                // Check if the monkey is actually pushing the box
                boolean isPushing = false;

                // Moving right: monkey must be on the left side of the box
                if (deltaX > 0 && playerBounds.x + playerBounds.width <= box.bounds.x + 2) {
                    isPushing = true;
                }
                // Moving left: monkey must be on the right side of the box
                else if (deltaX < 0 && playerBounds.x >= box.bounds.x + box.bounds.width - 2) {
                    isPushing = true;
                }
                // Moving up: monkey must be below the box
                else if (deltaY > 0 && playerBounds.y + playerBounds.height <= box.bounds.y + 2) {
                    isPushing = true;
                }
                // Moving down: monkey must be above the box
                else if (deltaY < 0 && playerBounds.y >= box.bounds.y + box.bounds.height - 2) {
                    isPushing = true;
                }

                if (isPushing) {
                    // Calculate new box position
                    float newBoxX = box.bounds.x + deltaX;
                    float newBoxY = box.bounds.y + deltaY;

                    // Check if the new position is valid (not colliding with walls)
                    Rectangle newBoxBounds = new Rectangle(newBoxX, newBoxY, box.bounds.width, box.bounds.height);
                    if (!isColliding(newBoxBounds)) {
                        // Move the box
                        box.bounds.x = newBoxX;
                        box.bounds.y = newBoxY;
                        return true;
                    }
                }
                return false; // Return false if we hit a box but couldn't push it
            }
        }
        return false;
    }

    public boolean isCollidingWithSawTrap(Rectangle bounds) {
        for (SawTrap sawTrap : sawTraps) {
            if (sawTrap.getHitbox().overlaps(bounds)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void dispose() {
        for (Texture texture : tileTextures) {
            if (texture != null) {
                texture.dispose();
            }
        }
    }

    public void reset() {
        System.out.println("Resetting boxes...");
        for (Box box : boxes) {
            Room room = rooms.get(box.roomIndex);
            float worldY = (room.mapData.length - box.originalY - 1) * TILE_SIZE;
            float newX = (box.originalX + room.offsetX) * TILE_SIZE;
            float newY = worldY + (room.offsetY * TILE_SIZE);

            System.out.println("Box original position: " + box.originalX + ", " + box.originalY);
            System.out.println("Box new position: " + newX + ", " + newY);

            box.bounds.x = newX;
            box.bounds.y = newY;
        }
    }
}
