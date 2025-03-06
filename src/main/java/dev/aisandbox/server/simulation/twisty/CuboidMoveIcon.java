package dev.aisandbox.server.simulation.twisty;

import dev.aisandbox.server.simulation.twisty.model.Move;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class CuboidMoveIcon {

    final int width;
    final int height;
    final int depth; // NOT USED CURRENTLY

    BufferedImage background;
    BufferedImage midground;
    BufferedImage foreground;

    BufferedImage arrows;

    Graphics2D backgroundGraphics;
    Graphics2D midgroundGraphics;
    Graphics2D foregroundGraphics;

    // constants to frame the icon
    final static int marginLeft = 10;
    final static int marginRight=10;
    final static int marginTop =10;
    final static int marginBottom=34;

    // calculated values
    final double scale;
    final int startX;
    final int startY;

    // colours to fill squares
    final static Color UNFILLED = Color.WHITE;
    final static Color FILLED = Color.lightGray;

    public CuboidMoveIcon(int width, int height, int depth) throws IOException {
        this.width = width;
        this.height = height;
        this.depth = depth;
        // setup images
        background =
                new BufferedImage(Move.MOVE_ICON_WIDTH, Move.MOVE_ICON_HEIGHT, BufferedImage.TYPE_INT_RGB);
        backgroundGraphics = background.createGraphics();
        backgroundGraphics.setColor(Color.WHITE);
        backgroundGraphics.fillRect(0, 0, Move.MOVE_ICON_WIDTH, Move.MOVE_ICON_HEIGHT);
        midground =
                new BufferedImage(Move.MOVE_ICON_WIDTH, Move.MOVE_ICON_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        midgroundGraphics = midground.createGraphics();
        foreground =
                new BufferedImage(Move.MOVE_ICON_WIDTH, Move.MOVE_ICON_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        foregroundGraphics = foreground.createGraphics();
        // work out scale
        double hscale = (Move.MOVE_ICON_WIDTH-marginLeft-marginRight)/(1.0*width);
        double vscale = (Move.MOVE_ICON_HEIGHT-marginTop-marginBottom)/(1.0*height);
        scale=Math.min(hscale,vscale);

        startY=Move.MOVE_ICON_HEIGHT-marginBottom-(int)((Move.MOVE_ICON_HEIGHT-marginBottom-marginTop-height*scale)/2.0);
        startX=marginLeft+(int)((Move.MOVE_ICON_WIDTH-marginLeft-marginRight-width*scale)/2.0);

        // draw cuboid
        midgroundGraphics.setColor(UNFILLED);
        midgroundGraphics.fillRect(startX,startY-(int)(height*scale),(int)(width*scale),(int)(height*scale));


        //   midgroundGraphics.setColor(Color.cyan);
        //   midgroundGraphics.fillRect(marginLeft,marginTop,Move.MOVE_ICON_WIDTH-marginLeft-marginRight,Move.MOVE_ICON_HEIGHT-marginTop-marginBottom);
        // load arrows
        arrows = ImageIO.read(CuboidMoveIcon.class.getResourceAsStream("/images/twisty/CuboidArrows.png"));
    }

    private void drawCuboid() {
        midgroundGraphics.setColor(Color.darkGray);
        // draw front vertical lines
        for (int i=0;i<=width;i++) {
            midgroundGraphics.drawLine((int)(startX+i*scale),startY,(int)(startX+i*scale),(int)(startY-height*scale));
        }
        // draw front horizontal lines
        for (int i=0;i<=height;i++) {
            midgroundGraphics.drawLine(startX,startY-(int)(i*scale),startX+(int)(width*scale),startY-(int)(i*scale));
        }


    }

    public void fillFrontFace(int num) {
        fillFrontFace(num%width,num/width);
    }

    public void fillFrontFace() {
        for(int x=0;x<width;x++) {
            for (int y=0;y<height;y++) {
                fillFrontFace(x,y);
            }
        }
    }

    public void fillFrontFace(int left,int down) {
        midgroundGraphics.setColor(FILLED);
        midgroundGraphics.fillRect(startX+(int)(left*scale),startY+(int)(scale*(down-height)),(int)scale+1,(int)scale+1);
    }

    public void fillFrontFace(int left,int down,Color color) {
        midgroundGraphics.setColor(color);
        midgroundGraphics.setColor(FILLED);
        midgroundGraphics.fillRect(startX+(int)(left*scale),startY+(int)(scale*(down-height)),(int)scale+1,(int)scale+1);
    }

    public void setRotation(char face,boolean inverse) {
        switch (face) {
            case 'R' :
                backgroundGraphics.drawImage(arrows.getSubimage(32+4*60,20,28,40),32,20,null);
                foregroundGraphics.drawImage(arrows.getSubimage(40+(inverse?60:0),20,20,40),40,20,null);
                break;
            case 'L' :
                backgroundGraphics.drawImage(arrows.getSubimage(4*60,20,28,40),0,20,null);
                foregroundGraphics.drawImage(arrows.getSubimage(0+(inverse?60:0),20,20,40),0,20,null);
                break;
            case 'U' :
                backgroundGraphics.drawImage(arrows.getSubimage(4*60,0,60,30),0,0,null);
                foregroundGraphics.drawImage(arrows.getSubimage(0+(inverse?60:0),0,60,15),0,0,null);
                break;
            case 'D' :
                backgroundGraphics.drawImage(arrows.getSubimage(4*60,40,60,35),0,40,null);
                foregroundGraphics.drawImage(arrows.getSubimage(0+(inverse?60:0),60,60,15),0,60,null);
                break;
            case 'F' :
                foregroundGraphics.drawImage(arrows.getSubimage((inverse?3:2)*60,0,60,100),0,0,null);
                break;
            case 'B' :
                backgroundGraphics.drawImage(arrows.getSubimage((inverse?2:3)*60,0,60,100),0,0,null);
                break;
        }
    }

    public BufferedImage getImage() {
        // draw cube overlay
        drawCuboid();
        // merge the layers
        BufferedImage image =
                new BufferedImage(Move.MOVE_ICON_WIDTH, Move.MOVE_ICON_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.drawImage(background, 0, 0, null);
        g.drawImage(midground,0,0,null);
        g.drawImage(foreground, 0, 0, null);
        return image;
    }
}
