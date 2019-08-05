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
            GridCell gc = new GridCell((short) node.getInt("x"),
                    (short) node.getInt("y"), (short) node.getInt("width"),
                    (short) node.getInt("height"), node.getString("resourceType"),
                    node.getString("resourceId"), node.getString("title"),
                    node.getString("classname"), (short) node.getInt("colXs"),
                    (short) node.getInt("colSm"), (short) node.getInt("colLg"),
                    (short) node.getInt("colXl"));
            allCells.add(gc);
        }
        return allCells;
    }
    
    
    /** Representa una celda en el grid utilizado por Bootstrap para distribuir el
        contenido de una pagina Web */
    public static class GridCell {
        
        /** Define la coordenada de la esquina superior izquierda de la celda en el eje X */
        private short x;
        /** Define la coordenada de la esquina superior izquierda de la celda en el eje Y */
        private short y;
        /** Ancho de la celda (en columnas) */
        private short width;
        /** Alto de la celda (en filas) */
        private short height;
        /** Tipo de recurso contenido en la celda */
        private String type;
        /** Identificador de la instancia del recurso contenido en la celda */
        private String id;
        /** Titulo a mostrar en el grid */
        private String title;
        /** Nombre de la clase de estilos a utilizar para la celda */
        private String cssName;
        /** Numero de columnas para una resolucion extra pequeña */
        private short colXs;
        /** Numero de columnas para una resolucion pequeña */
        private short colSm;
        /** Numero de columnas para una resolucion grande */
        private short colLg;
        /** Numero de columnas para una resolucion extra grande */
        private short colXl;
        
        
        public GridCell() {
        }
        
        public GridCell(short x, short y, short width, short height, String type,
                String id, String title, String cssName, short colXs, short colSm,
                short colLg, short colXl) {
            
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.type = (type != null ? type : "");
            this.id = (id != null ? id : "");
            this.title = (title != null ? title : this.type);
            this.cssName = (cssName != null ? cssName : "");
            this.colXs = colXs;
            this.colSm = colSm;
            this.colLg = colLg;
            this.colXl = colXl;
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

        /**
         * @return the title
         */
        public String getTitle() {
            return title;
        }

        /**
         * @param title the title to set
         */
        public void setTitle(String title) {
            this.title = title;
        }

        /**
         * @return the cssName
         */
        public String getCssName() {
            return cssName;
        }

        /**
         * @param cssName the cssName to set
         */
        public void setCssName(String cssName) {
            this.cssName = cssName;
        }

        /**
         * @return the colXs
         */
        public short getColXs() {
            return colXs;
        }

        /**
         * @param colXs the colXs to set
         */
        public void setColXs(short colXs) {
            this.colXs = colXs;
        }

        /**
         * @return the colSm
         */
        public short getColSm() {
            return colSm;
        }

        /**
         * @param colSm the colSm to set
         */
        public void setColSm(short colSm) {
            this.colSm = colSm;
        }

        /**
         * @return the colLg
         */
        public short getColLg() {
            return colLg;
        }

        /**
         * @param colLg the colLg to set
         */
        public void setColLg(short colLg) {
            this.colLg = colLg;
        }

        /**
         * @return the colXl
         */
        public short getColXl() {
            return colXl;
        }

        /**
         * @param colXl the colXl to set
         */
        public void setColXl(short colXl) {
            this.colXl = colXl;
        }
        
        @Override
        public String toString() {
            return "x:" + this.x + ", y:" + this.y + ", width:" + this.width +
                    ", height:" + this.height + ", type:" + this.type +
                    ", id:" + this.id + ", title:" + this.title + ", cssName:" +
                    this.cssName + ", colXs:" + this.colXs + ", colSm:" +
                    this.colSm + ", colLg:" + this.colLg + ", colXl:" + this.colXl;
        }
    }
}