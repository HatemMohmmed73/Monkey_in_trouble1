package com.monkeyintrouble.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.monkeyintrouble.entities.SawTrap;
import com.monkeyintrouble.entities.Player;
import java.util.ArrayList;
import java.util.List;

public class GameMap implements Disposable {
    private static final int TILE_SIZE = 32;
    private final Array<Room> rooms;
    private final Texture[] tileTextures;
    private final Array<Box> boxes;
    private final Array<SawTrap> sawTraps;
    private boolean asset56Changed = false;
    private boolean asset29Changed = false;
    private final List<Position> originalDoorPositions = new ArrayList<>();  // Track original door positions
    private float teleportCooldown = 0f;  // Add teleport cooldown timer
    private static final float TELEPORT_COOLDOWN_DURATION = 1.0f;  // 1 second cooldown
    private boolean isCurrentlyTeleporting = false;  // Add flag to track teleport state
    private Player player;  // Add player reference

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

        public int getHeight() {
            return mapData.length;
        }

        public int getWidth() {
            return mapData[0].length;
        }

        public int getTile(int x, int y) {
            if (y >= 0 && y < mapData.length && x >= 0 && x < mapData[y].length) {
                return mapData[y][x];
            }
            return -1; // Assuming -1 represents an invalid tile
        }

        public void setTile(int x, int y, int tileId) {
            if (y >= 0 && y < mapData.length && x >= 0 && x < mapData[y].length) {
                mapData[y][x] = tileId;
            }
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

    private static class Position {
        final int x;
        final int y;
        final int roomIndex;

        Position(int x, int y, int roomIndex) {
            this.x = x;
            this.y = y;
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
        tileTextures = new Texture[74];
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

                // Store original door positions
                if (tileId == 29) {  // If it's a door tile
                    originalDoorPositions.add(new Position(x, y, roomIndex));
                }

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

        // Update teleport cooldown
        if (teleportCooldown > 0) {
            teleportCooldown -= deltaTime;
            if (teleportCooldown <= 0) {
                isCurrentlyTeleporting = false;
            }
        }
    }

    public void render(SpriteBatch batch) {
        // First render the base tiles
        for (Room room : rooms) {
            for (int y = 0; y < room.mapData.length; y++) {
                for (int x = 0; x < room.mapData[y].length; x++) {
                    int tileId = room.mapData[y][x];

                    // Handle special asset changes
                    if (tileId == 56 && asset56Changed) {
                        tileId = 57; // Change to pressed button texture
                    }
                    if (tileId == 29 && asset29Changed) {
                        tileId = 1; // Change door to floor texture
                        System.out.println("Rendering door at " + x + "," + y + " as floor (1)");
                    }

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

        for (SawTrap sawTrap : sawTraps) {
            sawTrap.dispose();
        }
    }

    public void reset() {
        System.out.println("Resetting boxes...");
        // Reset boxes to original positions
        for (Box box : boxes) {
            Room room = rooms.get(box.roomIndex);
            room.mapData[box.originalY][box.originalX] = 2; // Reset to box tile
            box.bounds.x = (box.originalX + room.offsetX) * TILE_SIZE;
            box.bounds.y = (room.mapData.length - box.originalY - 1 + room.offsetY) * TILE_SIZE;
        }

        // Reset doors using stored original positions
        for (Position doorPos : originalDoorPositions) {
            Room room = rooms.get(doorPos.roomIndex);
            // Change tile back to door
            room.mapData[doorPos.y][doorPos.x] = 29;

            // Add back collision box for the door
            float worldY = (room.mapData.length - doorPos.y - 1) * TILE_SIZE;
            float worldX = (doorPos.x + room.offsetX) * TILE_SIZE;
            worldY += (room.offsetY * TILE_SIZE);
            Rectangle doorBox = new Rectangle(worldX, worldY, TILE_SIZE, TILE_SIZE);

            // Check if collision box already exists before adding
            boolean boxExists = false;
            for (Rectangle existingBox : room.collisionBoxes) {
                if (existingBox.x == doorBox.x && existingBox.y == doorBox.y) {
                    boxExists = true;
                    break;
                }
            }
            if (!boxExists) {
                room.collisionBoxes.add(doorBox);
            }

            System.out.println("Restored door at position: " + doorPos.x + "," + doorPos.y + " in room " + doorPos.roomIndex);
        }

        // Reset asset state flags
        asset56Changed = false;  // Reset button state
        asset29Changed = false;  // Reset door state

        // Reset saw traps
        for (SawTrap sawTrap : sawTraps) {
            sawTrap.reset();
        }

        System.out.println("Map Reset - All doors closed and buttons reset!");
    }

    public Vector2 handleAssetCollision(Rectangle playerBounds) {
        // Skip if we're currently teleporting or on cooldown
        if (isCurrentlyTeleporting || teleportCooldown > 0) {
            return null;
        }

        for (Room room : rooms) {
            for (int y = 0; y < room.mapData.length; y++) {
                for (int x = 0; x < room.mapData[y].length; x++) {
                    int tileId = room.mapData[y][x];
                    float worldX = (x + room.offsetX) * TILE_SIZE;
                    float worldY = (room.mapData.length - y - 1) * TILE_SIZE + (room.offsetY * TILE_SIZE);

                    Rectangle tileBounds = new Rectangle(worldX, worldY, TILE_SIZE, TILE_SIZE);

                    if (tileBounds.overlaps(playerBounds)) {
                        if (tileId == 56) {
                            asset56Changed = true;
                            System.out.println("Button (56) pressed at position: " + x + "," + y);
                            openDoor();
                        } else if (tileId == 72) {
                            if (player != null) {
                                if (player.isGhostMode()) {
                                    // If in ghost mode, destroy the asset 72 and return to normal state
                                    room.setTile(x, y, 1); // Change to floor texture
                                    player.setGhostMode(false);
                                } else {
                                    // If in normal mode, take damage
                                    player.takeDamage();
                                }
                            }
                        } else if (tileId == 63) {
                            // Change monkey to ghost state when colliding with asset 63
                            if (player != null) {
                                player.setGhostMode(true);
                            }
                        } else if (tileId == 34 && !isCurrentlyTeleporting) {
                            // Teleport to right top room
                            Vector2 destination = findTeleportDestination();
                            if (destination != null) {
                                isCurrentlyTeleporting = true;
                                teleportCooldown = TELEPORT_COOLDOWN_DURATION;
                                System.out.println("Found teleport destination: " + destination.x + "," + destination.y);
                                return destination;
                            } else {
                                System.out.println("Error: Could not find teleport destination (tile 66)!");
                            }
                        } else if ((tileId == 67 || tileId == 68) && !isCurrentlyTeleporting) {
                            // Teleport back to start room
                            Vector2 destination = findReturnTeleportDestination();
                            if (destination != null) {
                                isCurrentlyTeleporting = true;
                                teleportCooldown = TELEPORT_COOLDOWN_DURATION;
                                System.out.println("Found return teleport destination: " + destination.x + "," + destination.y);
                                return destination;
                            } else {
                                System.out.println("Error: Could not find return teleport destination (tile 34)!");
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private Vector2 findTeleportDestination() {
        // The right top room is at index 1 in the rooms array
        Room rightTopRoom = rooms.get(1);

        System.out.println("Searching for destination in right top room. Dimensions: " +
                          rightTopRoom.getWidth() + "x" + rightTopRoom.getHeight());

        // First find the teleport destination tile
        int destX = -1, destY = -1;
        for (int y = 0; y < rightTopRoom.mapData.length; y++) {
            for (int x = 0; x < rightTopRoom.mapData[y].length; x++) {
                if (rightTopRoom.mapData[y][x] == 66) {
                    destX = x;
                    destY = y;
                    break;
                }
            }
            if (destX != -1) break;
        }

        if (destX != -1) {
            // Found the destination tile, now find a safe spot next to it
            // Try to place the player to the right of the destination
            int safeX = destX + 2; // Two tiles to the right
            int safeY = destY;

            // Calculate world coordinates for the safe position
            float worldX = (safeX + rightTopRoom.offsetX) * TILE_SIZE;
            float worldY = (rightTopRoom.mapData.length - safeY - 1) * TILE_SIZE + (rightTopRoom.offsetY * TILE_SIZE);

            System.out.println("Found destination tile at: " + destX + "," + destY);
            System.out.println("Placing player at safe position: " + safeX + "," + safeY);
            System.out.println("World coordinates: " + worldX + "," + worldY);

            return new Vector2(worldX, worldY);
        }
        return null;
    }

    private Vector2 findReturnTeleportDestination() {
        // The main room is at index 0 in the rooms array
        Room mainRoom = rooms.get(0);

        System.out.println("Searching for return destination in main room. Dimensions: " +
                          mainRoom.getWidth() + "x" + mainRoom.getHeight());

        // Find the teleport destination tile (34.png)
        int destX = -1, destY = -1;
        for (int y = 0; y < mainRoom.mapData.length; y++) {
            for (int x = 0; x < mainRoom.mapData[y].length; x++) {
                if (mainRoom.mapData[y][x] == 34) {
                    destX = x;
                    destY = y;
                    break;
                }
            }
            if (destX != -1) break;
        }

        if (destX != -1) {
            // Found the destination tile, now find a safe spot two blocks to the left
            int safeX = destX - 2; // Two tiles to the left
            int safeY = destY;

            // Calculate world coordinates for the safe position
            float worldX = (safeX + mainRoom.offsetX) * TILE_SIZE;
            float worldY = (mainRoom.mapData.length - safeY - 1) * TILE_SIZE + (mainRoom.offsetY * TILE_SIZE);

            System.out.println("Found return destination tile at: " + destX + "," + destY);
            System.out.println("Placing player at safe position: " + safeX + "," + safeY);
            System.out.println("World coordinates: " + worldX + "," + worldY);

            return new Vector2(worldX, worldY);
        }
        return null;
    }

    private void openDoor() {
        // First collect all door positions
        List<Position> doorPositions = new ArrayList<>();
        for (int roomIndex = 0; roomIndex < rooms.size; roomIndex++) {
            Room room = rooms.get(roomIndex);
            for (int y = 0; y < room.getHeight(); y++) {
                for (int x = 0; x < room.getWidth(); x++) {
                    if (room.getTile(x, y) == 29) { // Door tile
                        doorPositions.add(new Position(x, y, roomIndex));
                    }
                }
            }
        }

        // Then modify the doors and update collision boxes
        for (Position pos : doorPositions) {
            Room room = rooms.get(pos.roomIndex);
            room.setTile(pos.x, pos.y, 1); // Change to floor tile

            // Remove the collision box for this door
            float worldY = (room.mapData.length - pos.y - 1) * TILE_SIZE;
            float worldX = (pos.x + room.offsetX) * TILE_SIZE;
            worldY += (room.offsetY * TILE_SIZE);
            Rectangle doorBox = new Rectangle(worldX, worldY, TILE_SIZE, TILE_SIZE);

            // Remove any collision box that matches this position
            room.collisionBoxes.removeValue(doorBox, false);

            Gdx.app.log("GameMap", "Opening door at position: " + pos.x + "," + pos.y + " [" + pos.roomIndex + "s]");
        }
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
