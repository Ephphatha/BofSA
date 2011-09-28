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
import java.util.Collections;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.gui.GUIContext;
import org.newdawn.slick.tiled.TiledMap;

import au.edu.csu.bofsa.Behaviours.SpawnBehaviour;
import au.edu.csu.bofsa.Events.BuildAreaModEvent;
import au.edu.csu.bofsa.Events.Event;
import au.edu.csu.bofsa.Events.EventSink;
import au.edu.csu.bofsa.Events.GenericEvent;
import au.edu.csu.bofsa.Signals.InputSignal;
import au.edu.csu.bofsa.Signals.Signal;

/**
 * @author ephphatha
 *
 */
public class GameLevelTB {

  protected Dimension size;
  protected BoardNode[][] board;
  
  public static class BoardNode {
    protected Image background;
    
    public BoardNode(Image image) {
      this.background = image;
    }
    
    public void draw(Graphics g, Rectangle tile) {
      if (this.background!= null) {
        this.background.draw(tile.getX(), tile.getY(), tile.getWidth(), tile.getHeight());
      }
    }
  }
  
  public GameLevelTB(
      String levelName,
      EventSink behaviourListener,
      EventSink spawnListener,
      EventSink buildListener) throws SlickException {
    TiledMap map;
    
    String fileName = "/levels/" + levelName + ".tmx";
    
    try {
      map = new TiledMap(this.getClass().getResource(fileName).getRef());
    } catch (NullPointerException e) {
      map = new TiledMap(fileName);
    }
    
    this.size = new Dimension(map.getWidth(), map.getHeight());
    
    this.board = new BoardNode[this.size.width][];
    
    int backgroundLayer =  map.getLayerIndex("Background");
    int buildableLayer = map.getLayerIndex("Buildable");
    
    for (int x = 0; x < this.size.width; ++x) {
      this.board[x] = new BoardNode[this.size.height];
      
      for (int y = 0; y < this.size.height; ++y) {
        Image image = null;

        if (backgroundLayer >= 0) {
          image = map.getTileImage(x, y, backgroundLayer);
        }

        if (buildableLayer >= 0) {
          if (map.getTileImage(x, y, buildableLayer) != null) {
            buildListener.handleEvent(
                new BuildAreaModEvent(
                    this,
                    new BuildAreaModEvent.Data(
                        BuildAreaModEvent.Data.Type.ADD_LOCATION,
                        new CopyablePoint(x, y)),
                    Event.Type.TARGETTED,
                    System.nanoTime()));
          }
        }
        
        this.board[x][y] = new BoardNode(image);
      }
    }
    
    int objectGroups = map.getObjectGroupCount();
    int validGroups = 0;
    
    for (int i = 0; i < objectGroups; ++i) {
      InputSignal<CopyableVector2f> spawnPos = null;
      InputSignal<CopyableFloat> spawnDuration = null;
      InputSignal<CopyableFloat> spawnInterval = null;
      InputSignal<CopyableFloat> lullDuration = null;
      CopyableList<CheckPoint> checkpoints = new CopyableList<CheckPoint>();
      Vector2f goal = null;
      
      for (int j = 0; j < map.getObjectCount(i); ++j) {
        String s = map.getObjectType(i, j);
        Vector2f pos = new Vector2f((float) map.getObjectX(i, j) / (float) map.getTileWidth(),
            (float) map.getObjectY(i, j) / (float) map.getTileHeight());
        
        if (s.equalsIgnoreCase("Checkpoint")) {
          //add to checkpoint queue
          try {
            checkpoints.add(new CheckPoint(Integer.parseInt(map.getObjectName(i, j)), pos));
          } catch (NumberFormatException e) {
            // Goggles.
          }
        } else if (s.equalsIgnoreCase("Spawn")) {
          //create spawn point
          if (spawnPos == null) {
            try {
              spawnPos = new Signal<CopyableVector2f>(new CopyableVector2f(pos));
              spawnDuration = new Signal<CopyableFloat>(new CopyableFloat(Float.parseFloat(map.getObjectProperty(i, j, "spawnDuration", "5"))));
              spawnInterval = new Signal<CopyableFloat>(new CopyableFloat(Float.parseFloat(map.getObjectProperty(i, j, "spawnInterval", "1"))));
              lullDuration = new Signal<CopyableFloat>(new CopyableFloat(Float.parseFloat(map.getObjectProperty(i, j, "lullDuration", "20"))));
            } catch (NumberFormatException e) {
              spawnPos = null;
              spawnDuration = null;
              spawnInterval = null;
              lullDuration = null;
            }
          }
        } else if (s.equalsIgnoreCase("Goal")) {
          //set goal
          
          if (goal == null) {
            goal = pos;
          }
        }
      }
      
      if (spawnPos != null || goal != null) {
        validGroups++;
        
        Collections.sort(checkpoints);
        checkpoints.add(new CheckPoint(checkpoints.getLast().index + 1, goal));
        
        SpawnBehaviour spawn = new SpawnBehaviour(
            new Signal<CopyableLong>(new CopyableLong(System.nanoTime())),
            spawnPos,
            new Signal<CopyableList<CheckPoint>>(checkpoints),
            spawnDuration,
            spawnInterval,
            lullDuration,
            spawnListener);
        
        behaviourListener.handleEvent(
            new GenericEvent(
                spawn,
                GenericEvent.Message.NEW_BEHAVIOUR,
                Event.Type.TARGETTED,
                System.nanoTime()));
      }
    }
    
    if (validGroups <= 0) {
      // TODO throw error, no spawn point defined.
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
}
