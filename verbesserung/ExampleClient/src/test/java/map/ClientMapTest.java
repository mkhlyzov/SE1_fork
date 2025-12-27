package map;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;

public class ClientMapTest {

    private final int NUM_MAP_GENERATIONS = 100;
    @Test
    void halfMapCorrectDimensions() {
        for(int i = 0; i < NUM_MAP_GENERATIONS; i++)
        {  
            ClientMap map = new ClientMap("player1");
            PlayerHalfMap halfMap = map.generate();
            int width = map.getWidth();
            int height = map.getHeight();
            assertEquals(width * height, halfMap.getMapNodes().size());
            int maxX = halfMap.getMapNodes().stream().mapToInt(PlayerHalfMapNode::getX).max().orElse(0);
            int maxY = halfMap.getMapNodes().stream().mapToInt(PlayerHalfMapNode::getY).max().orElse(0);
            assertEquals(height, maxY + 1);
            assertEquals(width, maxX + 1);
        }
    }

    @Test
    void halfMapContainsAtLeastOneCastle() {
        ClientMap map = new ClientMap("player1");
        for(int i = 0; i < NUM_MAP_GENERATIONS; i++)
        {  

            PlayerHalfMap halfMap = map.generate();

            boolean castleExists = halfMap.getMapNodes().stream()
                    .anyMatch(PlayerHalfMapNode::isFortPresent);

            assertTrue(castleExists, "HalfMap must contain at least one castle");
        }    
    }

    @Test
    void halfMapTerrainDistributionIsValid() {
        ClientMap map = new ClientMap("player1");
        for(int i = 0; i < NUM_MAP_GENERATIONS; i++)
        {    
            PlayerHalfMap halfMap = map.generate();
            Collection<PlayerHalfMapNode> nodes = halfMap.getMapNodes();
            int total = nodes.size();
            long grass = nodes.stream().filter(n->n.getTerrain() == ETerrain.Grass).count();
            long water = nodes.stream().filter(n->n.getTerrain() == ETerrain.Water).count();
            long mountain = nodes.stream().filter(n->n.getTerrain() == ETerrain.Mountain).count();
            
            double grassPercent = grass * 100.0 / total;
            double waterPercent = water * 100.0 / total;
            double mountainPercent = mountain * 100.0 / total;
            
            assertTrue(grassPercent >= 48.0);
            assertTrue(waterPercent >= 14.0);
            assertTrue(mountainPercent >= 10.0);
        }
    }
    
    private boolean borderIsPassableEnough(List<PlayerHalfMapNode> border)
    {
        long passable = border.stream().filter(n->n.getTerrain() != ETerrain.Water).count();

        return passable * 1.0 / border.size() >= 0.51;
    }
    @Disabled
    @Test
    void eachMapBorderHasAtLeast51PercentPassableTiles()
    {
        ClientMap map = new ClientMap("player1");
        for(int i = 0; i < NUM_MAP_GENERATIONS; i++)
        {
            PlayerHalfMap halfMap = map.generate();

            List<PlayerHalfMapNode> nodes = halfMap.getMapNodes().stream().collect(Collectors.toList());
            // int maxX = nodes.stream().mapToInt(PlayerHalfMapNode::getX).max().orElse(0);
            // int maxY = nodes.stream().mapToInt(PlayerHalfMapNode::getY).max().orElse(0);
            int width = map.getWidth();
            int height = map.getHeight();
            assertTrue(borderIsPassableEnough(nodes.stream().filter(n->n.getY() == 0).collect(Collectors.toList())));
            assertTrue(borderIsPassableEnough(nodes.stream().filter(n->n.getY() == height - 1).collect(Collectors.toList())));
            assertTrue(borderIsPassableEnough(nodes.stream().filter(n->n.getX() == 0).collect(Collectors.toList())));
            assertTrue(borderIsPassableEnough(nodes.stream().filter(n->n.getX() == width - 1).collect(Collectors.toList())));
        }
    }    

    private boolean borderHasAtLeastPercentAccessible(List<PlayerHalfMapNode> border, double percent) {
        long accessible = border.stream().filter(n->n.getTerrain() != ETerrain.Water).count();

        return accessible * 1.0 / border.size() >= percent;
    }

    private boolean borderHasAtLeastPercentInaccessible(List<PlayerHalfMapNode> border, double percent) {
        
        long inaccessible = border.stream().filter(n->n.getTerrain() == ETerrain.Water).count();
        
        return inaccessible * 1.0 / border.size() >= percent;
    }
    
    
    @Test
    void extendedBorderRulesAreSatisfied() {
        ClientMap map = new ClientMap("player1");
        for(int i = 0; i < NUM_MAP_GENERATIONS; i++)
        {
            PlayerHalfMap halfMap = map.generate();
            List<PlayerHalfMapNode> nodes = new ArrayList<>(halfMap.getMapNodes());

            int width = map.getWidth();
            int height = map.getHeight();
            List<PlayerHalfMapNode> topBorder = nodes.stream().filter(n->n.getY() == 0).collect(Collectors.toList());
            List<PlayerHalfMapNode> bottomBorder = nodes.stream().filter(n->n.getY() == height - 1).collect(Collectors.toList());
            List<PlayerHalfMapNode> leftBorder = nodes.stream().filter(n->n.getX() == 0).collect(Collectors.toList());
            List<PlayerHalfMapNode> rigthBorder = nodes.stream().filter(n->n.getX() == width - 1).collect(Collectors.toList());
            assertTrue(borderHasAtLeastPercentAccessible(topBorder,0.40));
            assertTrue(borderHasAtLeastPercentInaccessible(topBorder,0.20));
            assertTrue(borderHasAtLeastPercentAccessible(bottomBorder,0.40));
            assertTrue(borderHasAtLeastPercentInaccessible(bottomBorder,0.20));
            assertTrue(borderHasAtLeastPercentAccessible(leftBorder,0.40));
            assertTrue(borderHasAtLeastPercentInaccessible(leftBorder,0.20));
            assertTrue(borderHasAtLeastPercentAccessible(rigthBorder,0.40));
            assertTrue(borderHasAtLeastPercentInaccessible(rigthBorder,0.20));
        }    
    }

}
