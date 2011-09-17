/**
 *   The MIT License
 *
 *  Copyright 2011 Andrew James <ephphatha@thelettereph.com>.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package au.edu.csu.bofsa;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.gui.GUIContext;
import org.newdawn.slick.tiled.TiledMap;

/**
 * @author ephphatha
 *
 */
public class GameLevelST {

  protected Dimension size;
  protected BoardNode[][] board;
  protected List<SpawnPoint> spawns;
  
  public static class BoardNode {
    protected EnumSet<Attribute> attributes;
    
    protected Image background;
    
    protected Sprite tower;
    
    public static enum Attribute {
      PATHABLE,
      BUILDABLE;
    }
    
    public BoardNode(EnumSet<Attribute> attributes) {
      this(null, attributes);
    }
    
    public BoardNode(Image image) {
      this(image, EnumSet.range(Attribute.PATHABLE, Attribute.BUILDABLE));
    }
    
    public BoardNode(Image image, EnumSet<Attribute> attributes) {
      this.background = image;
      
      this.attributes = attributes;
      
      this.tower = null;
    }
    
    public void draw(Graphics g, Rectangle tile) {
      if (this.background!= null) {
        this.background.draw(tile.getX(), tile.getY(), tile.getWidth(), tile.getHeight());
      }
      
      if (this.tower != null) {
        this.tower.draw(g, tile);
      }
    }
    
    public void update(float dt) {
      if (this.tower != null) {
        this.tower.update(dt);
      }
    }
    
    public boolean isPathable() {
      return this.tower == null && this.attributes.contains(Attribute.PATHABLE);
    }
    
    public void setPathable() {
      this.attributes.add(Attribute.PATHABLE);
    }
    
    public void setUnpathable() {
      this.attributes.remove(Attribute.PATHABLE);
    }
    
    public void setBuildable() {
      this.attributes.add(Attribute.BUILDABLE);
    }
    
    public void setUnbuildable() {
      this.attributes.remove(Attribute.BUILDABLE);
    }
    
    public boolean placeTower(Sprite t) {
      if (this.attributes.contains(Attribute.BUILDABLE)) {
        if (this.tower == null) {
          this.tower = t;
          return true;
        }
      }
      return false;
    }
  }
  
  public GameLevelST(String levelName) throws SlickException {
    TiledMap map = new TiledMap("levels/" + levelName + ".tmx");
    
    this.size = new Dimension(map.getWidth(), map.getHeight());
    
    this.board = new BoardNode[this.size.width][];
    
    int backgroundLayer =  map.getLayerIndex("Background");
    int buildableLayer = map.getLayerIndex("Buildable");
    int pathableLayer = map.getLayerIndex("Pathable");
    
    for (int x = 0; x < this.size.width; ++x) {
      this.board[x] = new BoardNode[this.size.height];
      
      for (int y = 0; y < this.size.height; ++y) {
        Image image = null;

        EnumSet<BoardNode.Attribute> attributes = EnumSet.noneOf(BoardNode.Attribute.class);
        
        if (backgroundLayer >= 0) {
          image = map.getTileImage(x, y, backgroundLayer);
        }

        if (buildableLayer >= 0) {
          if (map.getTileImage(x, y, buildableLayer) != null) {
            attributes.add(BoardNode.Attribute.BUILDABLE);
          }
        }
        
        if (pathableLayer >= 0) {
          if (map.getTileImage(x, y, pathableLayer) != null) {
            attributes.add(BoardNode.Attribute.PATHABLE);
          }
        }
        
        this.board[x][y] = new BoardNode(image, attributes);
      }
    }
    
    this.spawns = new ArrayList<SpawnPoint>();
    
    int objectGroups = map.getObjectGroupCount();
    int validGroups = 0;
    
    if (objectGroups > 1) {
      System.out.println("Multiple object groups detected. Assuming multiple spawns");
    }
    
    for (int i = 0; i < objectGroups; ++i) {
      SpawnPoint spawn = null;
      
      Queue<CheckPoint> checkpoints = new PriorityQueue<CheckPoint>();
      Vector2f goal = null;
      
      for (int j = 0; j < map.getObjectCount(i); ++j) {
        String s = map.getObjectType(i, j);
        Vector2f position = new Vector2f((float) map.getObjectX(i, j) / (float) map.getTileWidth(),
            (float) map.getObjectY(i, j) / (float) map.getTileHeight());
        
        if (s.equalsIgnoreCase("Checkpoint")) {
          //add to checkpoint queue
          
          try {
            checkpoints.add(new CheckPoint(Integer.parseInt(map.getObjectName(i, j)), position));
          } catch (NumberFormatException e) {
            // Goggles.
          }
        } else if (s.equalsIgnoreCase("Spawn")) {
          //create spawn point
          
          if (spawn == null) {
            try {
              spawn = new SpawnPoint(position,
                  Float.parseFloat(map.getObjectProperty(i, j, "spawnDuration", "5")),
                  Float.parseFloat(map.getObjectProperty(i, j, "spawnInterval", "1")),
                  Float.parseFloat(map.getObjectProperty(i, j, "lullDuration", "20")));

            } catch (NumberFormatException e) {
              // Goggles.
            }
          }
        } else if (s.equalsIgnoreCase("Goal")) {
          //set goal
          
          if (goal == null) {
            goal = position;
          }
        }
      }
      
      if (spawn != null || goal != null) {
        validGroups++;
        
        //add to list of spawns
        
        spawn.setCheckPoints(checkpoints);
        spawn.setGoal(goal);
        
        this.spawns.add(spawn);
      }
    }
    
    if (validGroups <= 0) {
      //throw error, no spawn point defined.
      
      System.out.println("No valid spawn point defined");
    }
  }
  
  public Dimension getSize() {
    return this.size;
  }
  
  public int getWidth() {
    return (int) this.size.getWidth();
  }
  
  public int getHeight() {
    return (int) this.size.getHeight();
  }
  
  public void render(GUIContext container, Graphics g) {
    g.pushTransform();
    
    Rectangle tile = new Rectangle(0, 0, container.getWidth() / this.size.width, container.getHeight() / this.size.height);
    
    for (int x = 0; x < this.size.width; ++x) {
      for (int y = 0; y < this.size.height; ++y) {
        tile.setLocation(tile.getWidth() * x, tile.getHeight() * y);
        this.board[x][y].draw(g, tile);
      }
    }
    
    g.popTransform();
  }
  
  public void update(float dt) {
    for (int x = 0; x < this.size.width; ++x) {
      for (int y = 0; y < this.size.height; ++y) {
        this.board[x][y].update(dt);
      }
    }
  }
  
  public void update(CreepManager cm, float dt) {
    for (SpawnPoint sp : this.spawns) {
      sp.update(cm, dt);
    }
  }
  
  public Tower spawnTower(Vector2f pos) {
    Tower t =  Tower.createTower(pos);
    if (this.spawnTower(t)) {
      return t;
    } else {
      return null;
    }
  }
  
  private boolean spawnTower(Tower t) {
    if (t == null) {
      return false;
    } else if (t.position.x >= 0 && t.position.x < this.size.width &&
        t.position.y >= 0 && t.position.y < this.size.height) {
      return this.board[(int) Math.floor(t.position.x)][(int) Math.floor(t.position.y)].placeTower(t.sprite);
    } else {
      return false;
    }
  }
}
