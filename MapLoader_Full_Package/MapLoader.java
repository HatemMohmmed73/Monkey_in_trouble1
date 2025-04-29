import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class MapLoader {

    public static void loadLevel(GameMap gameMap) {
        TmxMapLoader loader = new TmxMapLoader();

        // === Load room TMX files ===
        TiledMap mainRoom = loader.load("maps/main_room.tmx");
        TiledMap rightTop = loader.load("maps/right_top.tmx");
        TiledMap rightMid = loader.load("maps/right_mid.tmx");
        TiledMap rightBottom = loader.load("maps/right_bottom.tmx");

        // === Get map dimensions (in tiles) ===
        int mainWidth = getMapWidth(mainRoom);
        int spacing = 2; // space between rooms

        int rightOffsetX = mainWidth + spacing;

        int rtHeight = getMapHeight(rightTop);
        int rmHeight = getMapHeight(rightMid);

        // === Add rooms to GameMap ===
        gameMap.addRoom(mainRoom, 0, 0);
        System.out.println("Added main room at (0, 0)");

        gameMap.addRoom(rightTop, rightOffsetX, 0);
        System.out.println("Added rightTop room at (" + rightOffsetX + ", 0)");

        gameMap.addRoom(rightMid, rightOffsetX, rtHeight + spacing);
        System.out.println("Added rightMid room at (" + rightOffsetX + ", " + (rtHeight + spacing) + ")");

        gameMap.addRoom(rightBottom, rightOffsetX, rtHeight + spacing + rmHeight + spacing);
        System.out.println("Added rightBottom room at (" + rightOffsetX + ", " + (rtHeight + spacing + rmHeight + spacing) + ")");
    }

    private static int getMapWidth(TiledMap map) {
        MapProperties props = map.getProperties();
        return props.get("width", Integer.class);
    }

    private static int getMapHeight(TiledMap map) {
        MapProperties props = map.getProperties();
        return props.get("height", Integer.class);
    }
}
