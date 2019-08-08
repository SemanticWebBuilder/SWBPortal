/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.semanticwb.portal.resources;

import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

import java.io.File;
import java.io.Writer;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import org.semanticwb.Logger;
import org.semanticwb.SWBUtils;
import org.semanticwb.SWBPortal;
import org.semanticwb.model.WebSite;
import org.semanticwb.model.WebPage;
import org.semanticwb.model.Template;
import org.semanticwb.model.TemplateGroup;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.semanticwb.model.Resource;
import org.semanticwb.model.ResourceType;
import org.semanticwb.model.ResourceSubType;

import org.semanticwb.model.SWBModel;
import org.semanticwb.model.ModelProperty;
import org.semanticwb.portal.util.GridUtils;
import org.semanticwb.portal.api.SWBResource;
import org.semanticwb.portal.api.SWBParamRequest;
import org.semanticwb.portal.api.GenericAdmResource;
import org.semanticwb.portal.api.SWBParamRequestImp;
import org.semanticwb.portal.api.SWBResourceException;
import org.semanticwb.portal.util.GridUtils.GridCell;

/**
 *
 * @author sergio.tellez
 */
public class GridContent extends GenericAdmResource {
    
    /** The log. */
    private static final Logger LOG = SWBUtils.getLogger(GridContent.class);
    public static final String DEFAUL_HTML = "<template method=\"setHeaders\" Content-Type=\"text/html\"  response=\"{response}\" />\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>\n<title>\n   <TOPIC METHOD=\"getDisplayName\" LANGUAGE=\"{user@getLanguage}\"/>\n</title>\n\n"+gridPathLibs()+"</head>\n <body>\n <RESOURCE TYPE=\"GridContent\" />\n </body>\n</html>";
    
    @Override
    public void install(ResourceType recobj) throws SWBResourceException {
        ResourceType menuNivel = ResourceType.ClassMgr.getResourceType("MenuNivel", recobj.getWebSite());
        ResourceType staticText = ResourceType.ClassMgr.getResourceType("StaticText", recobj.getWebSite());
        if (null != menuNivel) {
            ResourceSubType resSubType = ResourceSubType.ClassMgr.createResourceSubType("gmenu", recobj.getWebSite());
            resSubType.setTitle("Grid Elements");
            resSubType.setDescription("Menu nivel created by composer");
            resSubType.setType(menuNivel);
        }
        if (null != staticText) {
            ResourceSubType resSubType = ResourceSubType.ClassMgr.createResourceSubType("gelements", recobj.getWebSite());
            resSubType.setTitle("Grid Elements");
            resSubType.setDescription("Static text created by composer");
            resSubType.setType(staticText);
        }
        String idTplGroup = createTemplateGroup("Behaviour", recobj.getWebSite()).getId();
        Template gridTpl = createTemplate("grid", "Template for grid component", true, recobj.getWebSite(), idTplGroup);
        recobj.getWebSite().addModelProperty(getModelProperty("idSubTypeST", "Identificador de subtipo de menu nivel", "gmenu", recobj.getWebSite()));
        recobj.getWebSite().addModelProperty(getModelProperty("idGridTmpl", "Identificador de contenedor de grid", gridTpl.getId(), recobj.getWebSite()));
        recobj.getWebSite().addModelProperty(getModelProperty("idSubTypeMN", "Identificador de subtipo de texto estático", "gelements", recobj.getWebSite()));
    }
    
    @Override
    public void doView(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws IOException {
        WebPage page = paramRequest.getWebPage();
        Iterator<Resource> it = page.listResources();
        Map<String, Resource> resources = new LinkedHashMap<>();
        //String path = "/work/models/"+page.getWebSite().getId()+"/jsp/composer/preview.jsp";
        String jsongrid = page.getProperty("_elements");
        while (it.hasNext()) {
            Resource res = (Resource)it.next();
            resources.put(res.getId(), res);
        }
        JSONObject obj = new JSONObject(jsongrid);
        JSONArray jsonCells = obj.getJSONArray("elements");
        render(jsonCells, (short) 50, "cellClass", request, response, paramRequest);
        /**if (null != jsongrid && !jsongrid.isEmpty()) {
            List<Map<String, Object>> elements = jsonToRes(jsongrid);
            for (Map<String, Object> map : elements) {
                String resourceId = (String)map.get("resourceId");
                if (null != resourceId && !resourceId.isEmpty()) {
                    if (!resources.containsKey(resourceId)) {
                        resources.remove(resourceId);
                    }else {
                        doPreview(request, response, paramRequest, resourceId);
                    }
                }
            }
        }
        request.setAttribute("components", resources);
        RequestDispatcher rd = request.getRequestDispatcher(path);
        try {
            rd.include(request, response);
        } catch (ServletException se) {
            LOG.error(se);
        }**/
    }
    
    private void render(JSONArray nodes, short heightUnit, String styleClass, HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws IOException {
        PrintWriter out = response.getWriter();
        if (heightUnit < 50) heightUnit = 50;
        short maxRow = (short) (nodes.getJSONObject(nodes.length() - 1).getInt("y"));
        String[] rows = new String[maxRow + 1];
        List<GridCell> cells = GridUtils.convert2Cells(nodes);
        GridUtils.renderRow(rows, maxRow);
        //StringBuilder bsHtml = new StringBuilder(128);
        for (short h = 0; h <= maxRow; h++) {
            short row = h;
            List<GridCell> cellsInRow = cells.stream().filter(cell -> cell.getY() == row).collect(Collectors.toCollection(ArrayList::new));
            //bsHtml.append(rows[h]);
            out.print(rows[h]);
            for (short k = 0; k < cellsInRow.size(); k++) {
                short offset = 0;
                if (k == 0) {
                    offset = (short) (cellsInRow.get(k).getX());
                } else {
                    GridCell leftCell = cellsInRow.get(k - 1);
                    offset = (short) (cellsInRow.get(k).getX() - (leftCell.getX() + leftCell.getWidth()));
                }
                //bsHtml.append(GridUtils.renderCell(cellsInRow.get(k), styleClass, offset, heightUnit));
                renderCell(cellsInRow.get(k), styleClass, offset, heightUnit, request, response, paramRequest);
            }
            //bsHtml.append("</div>");
            out.println("</div>");
        }
        //return bsHtml.toString();
    }
    
    private void renderCell(GridCell cell, String styleClass, short offset, short heightUnit, HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws IOException {
        PrintWriter out = response.getWriter();
        out.print("<div class=\"");
        String cssName = null != cell.getCssName() && !cell.getCssName().isEmpty() ? cell.getCssName() : styleClass;
        out.print(cssName);
        out.print(cell.getColXs() > 0 ? " col-" + cell.getColXs() : "");
        out.print(cell.getColSm() > 0 ? " col-sm-" + cell.getColSm() : "");
        out.print(cell.getWidth() > 0 ? " col-md-" + cell.getWidth() : " col-md-auto");
        out.print(cell.getColLg() > 0 ? " col-lg-" + cell.getColLg() : "");
        out.print(cell.getColXl() > 0 ? " col-xl-" + cell.getColXl() : "");
        if (offset > 0) {
            out.print(" col-md-offset-");
            out.print(offset);
        }
        out.print("\"");
        out.print(" style=\"min-height: ");
        out.print(cell.getHeight() * heightUnit);
        out.print("px;\"");
        out.print(" id=\"");
        out.print(cell.getId());
        out.print("\">");
        //de acuerdo al tipo de recurso, crear el tag del recurso, como si estuviera en una plantilla
        doPreview(request, response, paramRequest, cell.getId());
        out.println("</div>");
        //return div.toString();
    }
    
    private void doPreview(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest, String id) {
        try {
            SWBResource res = SWBPortal.getResourceMgr().getResource(paramRequest.getWebPage().getWebSiteId(), id);
            ((SWBParamRequestImp) paramRequest).setResourceBase(res.getResourceBase());
            ((SWBParamRequestImp) paramRequest).setWindowState(SWBParamRequest.Mode_VIEW);
            res.render(request, response, paramRequest);
        } catch (IOException | SWBResourceException e) {
            LOG.error("Error while getting content string, id:" + id, e);
        }
    }
    
    /**private List<Map<String, Object>> jsonToRes(String jsongrid) {
        List<Map<String, Object>> elements = new ArrayList<>();
        if (null == jsongrid || jsongrid.trim().isEmpty()) return elements;
        JSONObject obj = new JSONObject(jsongrid);
        JSONArray arr = obj.getJSONArray("elements");
        for (int i = 0; i < arr.length(); i++) {
            Map<String, Object> element = new LinkedHashMap<>();
            element.put("x", arr.getJSONObject(i).getInt("x"));
            element.put("y", arr.getJSONObject(i).getInt("y"));
            element.put("width", arr.getJSONObject(i).getInt("width"));
            element.put("height", arr.getJSONObject(i).getInt("height"));
            element.put("resourceType", arr.getJSONObject(i).getString("resourceType"));
            element.put("resourceId", arr.getJSONObject(i).getString("resourceId"));
            elements.add(element);
        }
        return elements;
    }**/
    
    private TemplateGroup createTemplateGroup(String title, WebSite site) {
        TemplateGroup grp = site.createTemplateGroup();
        grp.setTitle(title);
        return grp;
    }
    
    private Template createTemplate(String title, String description, Boolean active, WebSite site, String templateGroup) {
        int vnum = 1;
        Template tmp = site.createTemplate();
        tmp.setTitle(title);
        tmp.setActive(active);
        tmp.setDescription(description);
        Template tpl = SWBPortal.getTemplateMgr().getTemplateImp(tmp);
        if (null != tpl) {
            String defaultTPL = DEFAUL_HTML;
            tpl.setGroup(site.getTemplateGroup(templateGroup));
            String rutaFS_target_path = SWBPortal.getWorkPath() + tpl.getWorkPath() + "/" + vnum + "/";
            File f = new File(rutaFS_target_path);
            if (!f.exists()) {
                f.mkdirs();
            }
            File ftmpl = new File(tpl.getWorkPath() + "/" + vnum + "/template.html");
            try {
                Writer output = new BufferedWriter(new FileWriter(ftmpl));
                output.write(defaultTPL);
                output.close();
            } catch (IOException ex) {
                LOG.error(ex);
            }
            tpl.reload();
        }
        return tpl;
    }
    
    private ModelProperty getModelProperty(String id, String title, String value, SWBModel model) {
        ModelProperty mProperty = ModelProperty.ClassMgr.createModelProperty(id, model);
        mProperty.setTitle(title);
        mProperty.setValue(value);
        return mProperty;
    }
    
    private static String gridPathLibs() {
        StringBuilder libs = new StringBuilder();
        libs.append("\n <link rel=\"stylesheet\" href=\"{webpath}/work/models/basico/css/bootstrap.min.css\">");
        libs.append("\n <link rel='stylesheet' type='text/css' media='all' href='{webpath}/swbadmin/css/swb.css' />");
        libs.append("\n <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js\"></script>");
        libs.append("\n <script src=\"https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.11.0/jquery-ui.js\"></script>");
        libs.append("\n <script src=\"{webpath}/work/models/basico/js/bootstrap.min.js\"></script>");
        
        libs.append("\n\n <link rel=\"stylesheet\" href=\"{webpath}/swbadmin/js/gridstack1.0.0/gridstack.css\" />");
        libs.append("\n <link rel=\"stylesheet\" href=\"{webpath}/swbadmin/js/gridstack1.0.0/gridstack-extra.css\" />");
        libs.append("\n <script src=\"https://cdnjs.cloudflare.com/ajax/libs/lodash.js/3.5.0/lodash.min.js\" ></script>");
        libs.append("\n <script src=\"{webpath}/swbadmin/js/gridstack1.0.0/gridstack.js\" ></script>");
        libs.append("\n <script src=\"{webpath}/swbadmin/js/gridstack1.0.0/gridstack.jQueryUI.js\" ></script>\n");
        return libs.toString();
    }
}