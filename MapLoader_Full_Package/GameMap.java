import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;

public class GameMap {

    private TiledMapTileLayer mainLayer;

    public GameMap(int width, int height, int tileWidth, int tileHeight) {
        mainLayer = new TiledMapTileLayer(width, height, tileWidth, tileHeight);
    }

    public void addRoom(TiledMap roomMap, int offsetX, int offsetY) {
        TiledMapTileLayer roomLayer = (TiledMapTileLayer) roomMap.getLayers().get(0);

        for (int x = 0; x < roomLayer.getWidth(); x++) {
            for (int y = 0; y < roomLayer.getHeight(); y++) {
                Cell cell = roomLayer.getCell(x, y);
                if (cell != null) {
                    mainLayer.setCell(x + offsetX, y + offsetY, cell);
                }
            }
        }
    }

    public TiledMapTileLayer getLayer() {
        return mainLayer;
    }
}
