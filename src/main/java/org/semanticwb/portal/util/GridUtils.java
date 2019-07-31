/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.semanticwb.portal.util;

import java.util.List;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author sergio.tellez
 */
public class GridUtils {
    
    public static void renderRow(String[] rows, short maxRows) {
        String div = "<div class=\"row col-md-12\">";
        for (short i = 0; i <= maxRows; i++) {
            rows[i] = div;
        }
    }
    
    public static List<GridCell> convert2Cells(JSONArray nodes) {
        List<GridCell> allCells = new ArrayList(nodes.length());
        for (short j = 0; j < nodes.length(); j++) {
            JSONObject node = nodes.getJSONObject(j);
            GridCell gc = new GridCell((short) node.getInt("x"), (short) node.getInt("y"), (short) node.getInt("width"), (short) node.getInt("height"), node.getString("resourceType"), node.getString("resourceId"));
            allCells.add(gc);
        }
        return allCells;
    }
    
    public static class GridCell {
        
        private short x;
        private short y;
        private short width;
        private short height;
        private String type;
        private String id;
        
        public GridCell() {
        }
        
        public GridCell(short x, short y, short width, short height, String type, String id) {
            
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.type = (type != null ? type : "");
            this.id = (id != null ? id : "");
        }

        /**
         * @return the x
         */
        public short getX() {
            return x;
        }

        /**
         * @param x the x to set
         */
        public void setX(short x) {
            this.x = x;
        }

        /**
         * @return the y
         */
        public short getY() {
            return y;
        }

        /**
         * @param y the y to set
         */
        public void setY(short y) {
            this.y = y;
        }

        /**
         * @return the width
         */
        public short getWidth() {
            return width;
        }

        /**
         * @param width the width to set
         */
        public void setWidth(short width) {
            this.width = width;
        }

        /**
         * @return the height
         */
        public short getHeight() {
            return height;
        }

        /**
         * @param height the height to set
         */
        public void setHeight(short height) {
            this.height = height;
        }

        /**
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * @param type the type to set
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(String id) {
            this.id = id;
        }
        
        @Override
        public String toString() {
            return "x:" + this.x + ", y:" + this.y + ", width:" + this.width + ", height:" + this.height + ", type:" + this.type + ", id:" + this.id;
        }
    }
}