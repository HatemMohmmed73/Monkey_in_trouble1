package com.monkeyintrouble.map;

public enum TileType {
    EMPTY(0),
    FLOOR(1),
    WALL_STONE(3),
    WALL_TOP(4),
    WALL_SIDE(5),
    WALL_CORNER(6),
    WALL_INNER_CORNER(7),
    WALL_LEFT(8),
    WALL_RIGHT(11),
    CHEST(13),
    FLOOR_CRACKED(14),
    WALL_BOTTOM_LEFT(16),
    WALL_BOTTOM(17),
    WALL_18(18),
    WALL_BOTTOM_RIGHT(19),
    WALL_CORNER_SPECIAL(20),
    FLOOR_DIAMOND(21),
    WALL_22(22),
    WALL_TRANSITION(23),
    WALL_SPECIAL(24),
    WALL_DAMAGED(25),
    WALL_PILLAR(27),
    WALL_WINDOW(29),
    TRAP(31),
    SPIKES(32),
    TORCH(35),
    DECORATION(36),
    GHOST_SPAWN(38),
    CHEST_SPECIAL(42),
    BARS(43),
    BARS_BROKEN(44),
    SKULL(47),
    WALL_SPECIAL_2(50),
    BONES(51),
    BONES_PILE(52),
    CHAINS(53),
    DOOR(54),
    WALL_DARK(64),
    WALL_CRACK(65),
    WALL_MOSS(67),
    WALL_SLIME(68),
    WALL_CHAINS(69),
    WALL_TORCH(70),
    BLOCK(2);

    private final int tileId;

    TileType(int tileId) {
        this.tileId = tileId;
    }

    public int getTileId() {
        return tileId;
    }

    public static TileType fromId(int id) {
        for (TileType type : values()) {
            if (type.tileId == id) {
                return type;
            }
        }
        return EMPTY;
    }

    public boolean isCollidable() {
        return this == WALL_STONE || this == WALL_TOP || this == WALL_SIDE ||
            this == WALL_CORNER || this == WALL_LEFT || this == WALL_RIGHT ||
            this == WALL_BOTTOM_LEFT || this == WALL_BOTTOM || this == WALL_18 ||
            this == WALL_BOTTOM_RIGHT || this == WALL_CORNER_SPECIAL || this == WALL_TRANSITION ||
            this == WALL_SPECIAL || this == WALL_DAMAGED || this == WALL_PILLAR ||
            this == WALL_WINDOW || this == SPIKES || this == WALL_SPECIAL_2 ||
            this == WALL_DARK || this == WALL_CHAINS || this == BARS || this == WALL_22 ||
            this == TRAP || this == WALL_INNER_CORNER || this == BLOCK;
    }

    public boolean isPushable() {
        return this == CHEST_SPECIAL;
    }
}
