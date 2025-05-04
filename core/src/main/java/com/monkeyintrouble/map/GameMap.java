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
import com.monkeyintrouble.entities.FireHazard;
import com.monkeyintrouble.entities.BoxTrap;
import java.util.ArrayList;
import java.util.List;

public class GameMap implements Disposable {
    private static final int TILE_SIZE = 32;
    private final Array<Room> rooms;
    private final Texture[] tileTextures;
    private final Array<Box> boxes;
    private final Array<SawTrap> sawTraps;
    private final Array<FireHazard> fireHazards;
    private final Array<Vector2> fireStartPositions; // Track fire starting positions
    private final Array<Vector2> bananas;
    private int totalBananasDropped = 0; // Track total bananas dropped
    private static final int MAX_BANANAS = 3; // Maximum number of bananas allowed
    private boolean asset56Changed = false;
    private boolean asset29Changed = false;
    private final List<Position> originalDoorPositions = new ArrayList<>();  // Track original door positions
    private float teleportCooldown = 0f;  // Add teleport cooldown timer
    private static final float TELEPORT_COOLDOWN_DURATION = 1.0f;  // 1 second cooldown
    private boolean isCurrentlyTeleporting = false;  // Add flag to track teleport state
    private Player player;  // Add player reference
    private float fireSpawnTimer = 0;
    private static final float FIRE_SPAWN_INTERVAL = 2.0f; // Time between fire spawns
    private int currentFireIndex = 0; // Track which fire to spawn next
    private final Array<BoxTrap> boxTraps;
    private boolean isGameWon = false;  // Add this at the top with other fields
    private final Array<Vector2> ghostPositions;  // Add this with other fields
    private final List<Position> originalAsset74Positions = new ArrayList<>();
    private final List<Position> originalTrapPositions = new ArrayList<>();
    private final List<Position> originalBox39Positions = new ArrayList<>();

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
        this.fireHazards = new Array<>();
        this.fireStartPositions = new Array<>();
        this.bananas = new Array<>();
        this.boxTraps = new Array<>();
        this.ghostPositions = new Array<>();  // Initialize ghost positions array

        // Add main room at origin (0,0)
        rooms.add(new Room(mainRoom, 0, 0));

        // Position right rooms with 2 tile gap
        int rightRoomsX = mainRoom[0].length + 2;

        // Add right top room
        rooms.add(new Room(rightTopRoom, rightRoomsX, 0));

        // Add right bottom room directly below the top room
        rooms.add(new Room(rightBottomRoom, rightRoomsX, rightTopRoom.length));

        // Load tile textures
        tileTextures = new Texture[75];
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

        // Process map data to initialize box traps
        processMapData();

        // Store initial ghost positions
        for (Room room : rooms) {
            for (int y = 0; y < room.mapData.length; y++) {
                for (int x = 0; x < room.mapData[y].length; x++) {
                    if (room.mapData[y][x] == 72) { // Ghost tile
                        float worldX = (x + room.offsetX) * TILE_SIZE;
                        float worldY = (room.mapData.length - y - 1) * TILE_SIZE + (room.offsetY * TILE_SIZE);
                        ghostPositions.add(new Vector2(worldX, worldY));
                    }
                }
            }
        }
        // Store original asset 74 positions
        for (int roomIndex = 0; roomIndex < rooms.size; roomIndex++) {
            Room room = rooms.get(roomIndex);
            for (int y = 0; y < room.mapData.length; y++) {
                for (int x = 0; x < room.mapData[y].length; x++) {
                    if (room.mapData[y][x] == 74) {
                        originalAsset74Positions.add(new Position(x, y, roomIndex));
                    }
                    if (room.mapData[y][x] == 31) {
                        originalTrapPositions.add(new Position(x, y, roomIndex));
                    }
                    if (room.mapData[y][x] == 39) {
                        originalBox39Positions.add(new Position(x, y, roomIndex));
                    }
                }
            }
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

                // Skip collision box for button (32.png) and inactive trap (30.png)
                if (tileId == 32 || tileId == 30) {
                    continue;
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
                // Store fire hazard starting positions
                if (tileId == 37 || tileId == 35) {
                    fireStartPositions.add(new Vector2(worldX, worldY));
                    System.out.println("Fire hazard position stored at: " + worldX + ", " + worldY);
                }
            }
        }
    }

    public void update(float deltaTime) {
        // Update saw traps
        for (SawTrap sawTrap : sawTraps) {
            sawTrap.update(deltaTime);
        }

        // Update fire hazards
        for (int i = fireHazards.size - 1; i >= 0; i--) {
            FireHazard fire = fireHazards.get(i);
            fire.update(deltaTime);
            if (!fire.isActive()) {
                fireHazards.removeIndex(i);
            }
        }

        // Spawn new fires
        fireSpawnTimer += deltaTime;
        if (fireSpawnTimer >= FIRE_SPAWN_INTERVAL && !fireStartPositions.isEmpty()) {
            fireSpawnTimer = 0;
            Vector2 startPos = fireStartPositions.get(currentFireIndex);
            fireHazards.add(new FireHazard(startPos.x, startPos.y));
            currentFireIndex = (currentFireIndex + 1) % fireStartPositions.size;
        }

        // Update teleport cooldown
        if (teleportCooldown > 0) {
            teleportCooldown -= deltaTime;
            if (teleportCooldown <= 0) {
                isCurrentlyTeleporting = false;
            }
        }

        // --- NEW: Check if any box overlaps any button (tile 32) ---
        for (Box box : boxes) {
            for (Room room : rooms) {
                for (int y = 0; y < room.mapData.length; y++) {
                    for (int x = 0; x < room.mapData[y].length; x++) {
                        if (room.mapData[y][x] == 32) { // Button tile
                            float buttonX = (x + room.offsetX) * TILE_SIZE;
                            float buttonY = (room.mapData.length - y - 1) * TILE_SIZE + (room.offsetY * TILE_SIZE);
                            Rectangle buttonRect = new Rectangle(buttonX, buttonY, TILE_SIZE, TILE_SIZE);
                            if (box.bounds.overlaps(buttonRect)) {
                                // Change asset 31 (trap) to 30 and asset 39 (box) to 41 in the same room
                                for (int yy = 0; yy < room.mapData.length; yy++) {
                                    for (int xx = 0; xx < room.mapData[yy].length; xx++) {
                                        if (room.mapData[yy][xx] == 31) {
                                            room.mapData[yy][xx] = 30; // Change trap to inactive
                                            // Remove the collision box for this tile
                                            float trapWorldY = (room.mapData.length - yy - 1) * TILE_SIZE;
                                            float trapWorldX = (xx + room.offsetX) * TILE_SIZE;
                                            trapWorldY += (room.offsetY * TILE_SIZE);
                                            Rectangle trapBox = new Rectangle(trapWorldX, trapWorldY, TILE_SIZE, TILE_SIZE);
                                            // Remove any collision box that matches this position
                                            for (int i = 0; i < room.collisionBoxes.size; i++) {
                                                Rectangle existingBox = room.collisionBoxes.get(i);
                                                if (existingBox.x == trapBox.x && existingBox.y == trapBox.y) {
                                                    room.collisionBoxes.removeIndex(i);
                                                    break;
                                                }
                                            }
                                        }
                                        if (room.mapData[yy][xx] == 39) {
                                            room.mapData[yy][xx] = 41; // Change box to pressed
                                        }
                                    }
                                }
                                System.out.println("Box at (" + box.bounds.x + ", " + box.bounds.y + ") is on button at (" + buttonX + ", " + buttonY + ")! Trap and box updated.");
                            }
                        }
                    }
                }
            }
        }
        // --- END NEW ---

        for (BoxTrap trap : boxTraps) {
            trap.update(deltaTime);
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

        // Render fire hazards
        for (FireHazard fire : fireHazards) {
            fire.render(batch, tileTextures[73]); // Use fire.png texture
        }

        // Render bananas
        for (Vector2 bananaPos : bananas) {
            batch.draw(
                tileTextures[64], // Banana texture
                bananaPos.x,
                bananaPos.y,
                TILE_SIZE,
                TILE_SIZE
            );
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

        for (BoxTrap trap : boxTraps) {
            trap.render(batch, tileTextures);
        }
    }

    public boolean isColliding(Rectangle bounds) {
        for (Room room : rooms) {
            for (Rectangle box : room.collisionBoxes) {
                // Skip collision check for button (32.png) and teleport point (69.png)
                int tileX = (int)(box.x / TILE_SIZE) - room.offsetX;
                int tileY = room.mapData.length - 1 - (int)(box.y / TILE_SIZE) + room.offsetY;
                if (tileX >= 0 && tileX < room.mapData[0].length &&
                    tileY >= 0 && tileY < room.mapData.length) {
                    int tileId = room.mapData[tileY][tileX];
                    if (tileId == 32 || tileId == 69) { // Button or teleport point
                        continue;
                    }
                    // Check if this is a trap (31.png) and if it's triggered
                    if (tileId == 31) {
                        boolean isTriggered = false;
                        for (BoxTrap trap : boxTraps) {
                            if (trap.getTrapHitbox().overlaps(box) && trap.isTriggered()) {
                                isTriggered = true;
                                break;
                            }
                        }
                        if (isTriggered) {
                            continue; // Skip collision check for triggered trap
                        }
                    }
                }
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
        // First check regular boxes
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

        // Then check box trap pushable boxes
        for (BoxTrap trap : boxTraps) {
            if (trap.getPushableBoxHitbox().overlaps(playerBounds)) {
                System.out.println("Player overlapping with pushable box");

                // Check if the monkey is actually pushing the box
                boolean isPushing = false;
                Rectangle boxHitbox = trap.getPushableBoxHitbox();

                // Moving right: monkey must be on the left side of the box
                if (deltaX > 0 && playerBounds.x + playerBounds.width <= boxHitbox.x + 2) {
                    isPushing = true;
                }
                // Moving left: monkey must be on the right side of the box
                else if (deltaX < 0 && playerBounds.x >= boxHitbox.x + boxHitbox.width - 2) {
                    isPushing = true;
                }
                // Moving up: monkey must be below the box
                else if (deltaY > 0 && playerBounds.y + playerBounds.height <= boxHitbox.y + 2) {
                    isPushing = true;
                }
                // Moving down: monkey must be above the box
                else if (deltaY < 0 && playerBounds.y >= boxHitbox.y + boxHitbox.height - 2) {
                    isPushing = true;
                }

                if (isPushing) {
                    float newBoxX = boxHitbox.x + deltaX;
                    float newBoxY = boxHitbox.y + deltaY;

                    // Check if the new position is valid (not colliding with walls)
                    Rectangle newBoxBounds = new Rectangle(newBoxX, newBoxY, TILE_SIZE, TILE_SIZE);
                    if (!isColliding(newBoxBounds)) {
                        System.out.println("Moving pushable box to: (" + newBoxX + ", " + newBoxY + ")");
                        // Move the pushable box
                        trap.movePushableBox(deltaX, deltaY);
                        return true;
                    } else {
                        System.out.println("Cannot move box - collision detected");
                    }
                }
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

    public boolean isCollidingWithHazards(Rectangle bounds) {
        // Check saw trap collisions
        if (!player.isGhostMode() && isCollidingWithSawTrap(bounds)) {
            return true;
        }

        // Check fire hazard collisions
        for (FireHazard fire : fireHazards) {
            if (fire.getHitbox().overlaps(bounds)) {
                return true;
            }
        }

        // Check box trap collisions
        for (BoxTrap trap : boxTraps) {
            if (!trap.isTriggered() && trap.getTrapHitbox().overlaps(bounds)) {
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
        System.out.println("Resetting game...");
        isGameWon = false;  // Reset victory state
        System.out.println("Resetting boxes...");
        // Reset boxes to original positions
        for (Box box : boxes) {
            Room room = rooms.get(box.roomIndex);
            room.mapData[box.originalY][box.originalX] = 1; // Always set to floor under the box
            box.bounds.x = (box.originalX + room.offsetX) * TILE_SIZE;
            box.bounds.y = (room.mapData.length - box.originalY - 1 + room.offsetY) * TILE_SIZE;
        }

        // Clear all fire hazards
        fireHazards.clear();
        fireSpawnTimer = 0;
        currentFireIndex = 0;

        // Clear all bananas and reset counter
        bananas.clear();
        totalBananasDropped = 0;

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

        // Reset ghost (asset 72) in all rooms
        for (Room room : rooms) {
            for (int y = 0; y < room.mapData.length; y++) {
                for (int x = 0; x < room.mapData[y].length; x++) {
                    if (room.mapData[y][x] == 1) { // If it's a floor tile
                        // Check if this was originally a ghost tile
                        float worldX = (x + room.offsetX) * TILE_SIZE;
                        float worldY = (room.mapData.length - y - 1) * TILE_SIZE + (room.offsetY * TILE_SIZE);
                        for (Vector2 ghostPos : ghostPositions) {
                            if (Math.abs(ghostPos.x - worldX) < 1 && Math.abs(ghostPos.y - worldY) < 1) {
                                room.mapData[y][x] = 72; // Restore ghost tile
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Restore all original asset 74 positions
        for (Position pos : originalAsset74Positions) {
            Room room = rooms.get(pos.roomIndex);
            room.mapData[pos.y][pos.x] = 74;
        }

        // Restore all original trap (31) positions
        for (Position pos : originalTrapPositions) {
            Room room = rooms.get(pos.roomIndex);
            room.mapData[pos.y][pos.x] = 31;
        }
        // Restore all original box (39) positions
        for (Position pos : originalBox39Positions) {
            Room room = rooms.get(pos.roomIndex);
            room.mapData[pos.y][pos.x] = 39;
        }

        System.out.println("Map Reset - All doors closed, buttons reset, and ghost restored!");
    }

    private Vector2 findTeleportTo69Destination() {
        // Search all rooms for asset 69
        for (Room room : rooms) {
            for (int y = 0; y < room.mapData.length; y++) {
                for (int x = 0; x < room.mapData[y].length; x++) {
                    if (room.mapData[y][x] == 69) {
                        // Found asset 69, return position one block upward
                        float worldX = (x + room.offsetX) * TILE_SIZE;
                        // Add TILE_SIZE to move one block upward
                        float worldY = (room.mapData.length - y - 1) * TILE_SIZE + (room.offsetY * TILE_SIZE) + TILE_SIZE;
                        System.out.println("Teleporting to one block above asset 69 at: " + worldX + "," + worldY);
                        return new Vector2(worldX, worldY);
                    }
                }
            }
        }
        return null;
    }

    private Vector2 findTeleportTo51Destination() {
        // Search all rooms for asset 51
        for (Room room : rooms) {
            for (int y = 0; y < room.mapData.length; y++) {
                for (int x = 0; x < room.mapData[y].length; x++) {
                    if (room.mapData[y][x] == 51) {
                        // Found asset 51, return position with a small offset to prevent immediate re-teleport
                        float worldX = (x + room.offsetX) * TILE_SIZE + 2;
                        float worldY = (room.mapData.length - y - 1) * TILE_SIZE + (room.offsetY * TILE_SIZE);
                        return new Vector2(worldX, worldY);
                    }
                }
            }
        }
        return null;
    }

    public Vector2 handleAssetCollision(Rectangle playerBounds) {
        // Skip if we're currently teleporting or on cooldown
        if (isCurrentlyTeleporting || teleportCooldown > 0) {
            return null;
        }

        // Check for banana collection
        for (int i = bananas.size - 1; i >= 0; i--) {
            Vector2 bananaPos = bananas.get(i);
            Rectangle bananaBounds = new Rectangle(bananaPos.x, bananaPos.y, TILE_SIZE, TILE_SIZE);
            if (bananaBounds.overlaps(playerBounds)) {
                bananas.removeIndex(i);
                if (player != null && player.getBananas() < MAX_BANANAS) {
                    player.setBananas(player.getBananas() + 1);
                    System.out.println("Collected banana: " + player.getBananas() + " of " + MAX_BANANAS);
                }
            }
        }

        for (Room room : rooms) {
            for (int y = 0; y < room.mapData.length; y++) {
                for (int x = 0; x < room.mapData[y].length; x++) {
                    int tileId = room.mapData[y][x];
                    float worldX = (x + room.offsetX) * TILE_SIZE;
                    float worldY = (room.mapData.length - y - 1) * TILE_SIZE + (room.offsetY * TILE_SIZE);

                    Rectangle tileBounds = new Rectangle(worldX, worldY, TILE_SIZE, TILE_SIZE);

                    // NEW: If monkey collides with asset 41, change to 40 and drop a banana
                    if (tileId == 41 && tileBounds.overlaps(playerBounds)) {
                        room.mapData[y][x] = 40; // Change to asset 40
                        if (totalBananasDropped < MAX_BANANAS && bananas.size < MAX_BANANAS) {
                            bananas.add(new Vector2(worldX, worldY));
                            totalBananasDropped++;
                            System.out.println("Dropped banana " + totalBananasDropped + " of " + MAX_BANANAS + " at asset 41");
                        }
                    }
                    // END NEW

                    if (tileBounds.overlaps(playerBounds)) {
                        if (tileId == 50) {
                            isGameWon = true;
                            if (player != null && player.getObserver() != null) {
                                player.getObserver().onGameWon();
                            }
                            System.out.println("*********************************");
                            System.out.println("*           VICTORY!           *");
                            System.out.println("*     Congratulations! You     *");
                            System.out.println("*    have completed the game!  *");
                            System.out.println("*********************************");
                            return null;
                        } else if (tileId == 56) {
                            asset56Changed = true;
                            System.out.println("Button (56) pressed at position: " + x + "," + y);
                            openDoor();
                        } else if (tileId == 72) {
                            if (player != null) {
                                if (player.isGhostMode()) {
                                    // If in ghost mode, destroy the asset 72 and return to normal state
                                    room.setTile(x, y, 1); // Change to floor texture
                                    player.setGhostMode(false);
                                    // Drop a banana only if we haven't dropped all 3 yet
                                    if (totalBananasDropped < MAX_BANANAS && bananas.size < MAX_BANANAS) {
                                        bananas.add(new Vector2(worldX, worldY));
                                        totalBananasDropped++;
                                        System.out.println("Dropped banana " + totalBananasDropped + " of " + MAX_BANANAS);
                                    }
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
                        } else if (tileId == 52 && !isCurrentlyTeleporting) {
                            // Teleport to asset 69
                            Vector2 destination = findTeleportTo69Destination();
                            if (destination != null) {
                                isCurrentlyTeleporting = true;
                                teleportCooldown = TELEPORT_COOLDOWN_DURATION;
                                System.out.println("Found teleport destination (69): " + destination.x + "," + destination.y);
                                return destination;
                            } else {
                                System.out.println("Error: Could not find teleport destination (tile 69)!");
                            }
                        } else if (tileId == 69 && !isCurrentlyTeleporting) {
                            // Teleport to asset 51
                            Vector2 destination = findTeleportTo51Destination();
                            if (destination != null) {
                                isCurrentlyTeleporting = true;
                                teleportCooldown = TELEPORT_COOLDOWN_DURATION;
                                System.out.println("Found teleport destination (51): " + destination.x + "," + destination.y);
                                return destination;
                            } else {
                                System.out.println("Error: Could not find teleport destination (tile 51)!");
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
                        } else if (tileId == 70) {
                            // When monkey collides with asset 70, change all asset 74 tiles to asset 50
                            List<Position> tilesToChange = new ArrayList<>();

                            // First collect all positions that need to change
                            for (int roomIndex = 0; roomIndex < rooms.size; roomIndex++) {
                                Room searchRoom = rooms.get(roomIndex);
                                for (int searchY = 0; searchY < searchRoom.mapData.length; searchY++) {
                                    for (int searchX = 0; searchX < searchRoom.mapData[searchY].length; searchX++) {
                                        if (searchRoom.mapData[searchY][searchX] == 74) {
                                            tilesToChange.add(new Position(searchX, searchY, roomIndex));
                                        }
                                    }
                                }
                            }

                            // Then change all collected positions
                            for (Position pos : tilesToChange) {
                                Room targetRoom = rooms.get(pos.roomIndex);
                                targetRoom.mapData[pos.y][pos.x] = 50;
                                System.out.println("Changed asset 74 to 50 at position: " + pos.x + "," + pos.y + " in room " + pos.roomIndex);
                            }

                            // Drop a banana at the collision position if we haven't dropped all 3 yet
                            if (totalBananasDropped < MAX_BANANAS && bananas.size < MAX_BANANAS) {
                                bananas.add(new Vector2(worldX, worldY));
                                totalBananasDropped++;
                                System.out.println("Dropped banana " + totalBananasDropped + " of " + MAX_BANANAS + " at asset 70");
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

    private void processMapData() {
        for (Room room : rooms) {
            for (int y = 0; y < room.mapData.length; y++) {
                for (int x = 0; x < room.mapData[y].length; x++) {
                    int tileId = room.mapData[y][x];
                    float worldX = x * TILE_SIZE + (room.offsetX * TILE_SIZE);
                    float worldY = (room.mapData.length - y - 1) * TILE_SIZE + (room.offsetY * TILE_SIZE);

                    // Look for box trap components
                    if (tileId == 31) { // Trap
                        System.out.println("Found trap at: (" + x + ", " + y + ")");
                        // Find the box (39), button (32), and pushable box (42) positions
                        float boxX = -1, boxY = -1;
                        float buttonX = -1, buttonY = -1;
                        float pushableBoxX = -1, pushableBoxY = -1;

                        // Search the entire room for components
                        for (int searchY = 0; searchY < room.mapData.length; searchY++) {
                            for (int searchX = 0; searchX < room.mapData[searchY].length; searchX++) {
                                int checkTile = room.mapData[searchY][searchX];
                                float componentWorldX = (searchX + room.offsetX) * TILE_SIZE;
                                float componentWorldY = (room.mapData.length - searchY - 1) * TILE_SIZE + (room.offsetY * TILE_SIZE);

                                if (checkTile == 39) { // Box
                                    boxX = componentWorldX;
                                    boxY = componentWorldY;
                                    System.out.println("Found box at: (" + searchX + ", " + searchY + ")");
                                } else if (checkTile == 32) { // Button
                                    buttonX = componentWorldX;
                                    buttonY = componentWorldY;
                                    System.out.println("Found button at: (" + searchX + ", " + searchY + ")");
                                } else if (checkTile == 42) { // Pushable box
                                    pushableBoxX = componentWorldX;
                                    pushableBoxY = componentWorldY;
                                    System.out.println("Found pushable box at: (" + searchX + ", " + searchY + ")");
                                }
                            }
                        }

                        if (boxX != -1 && buttonX != -1 && pushableBoxX != -1) {
                            System.out.println("Creating box trap with components:");
                            System.out.println("Trap at: (" + worldX + ", " + worldY + ")");
                            System.out.println("Box at: (" + boxX + ", " + boxY + ")");
                            System.out.println("Button at: (" + buttonX + ", " + buttonY + ")");
                            System.out.println("Pushable box at: (" + pushableBoxX + ", " + pushableBoxY + ")");
                            boxTraps.add(new BoxTrap(
                                worldX,
                                worldY,
                                boxX,
                                boxY,
                                buttonX,
                                buttonY,
                                pushableBoxX,
                                pushableBoxY
                            ));
                        } else {
                            System.out.println("Could not find all components for box trap at: (" + x + ", " + y + ")");
                        }
                    }
                }
            }
        }
    }

    public boolean isGameWon() {
        return isGameWon;
    }
}
