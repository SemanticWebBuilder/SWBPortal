/*
 * SemanticWebBuilder es una plataforma para el desarrollo de portales y aplicaciones de integración,
 * colaboración y conocimiento, que gracias al uso de tecnología semántica puede generar contextos de
 * información alrededor de algún tema de interés o bien integrar información y aplicaciones de diferentes
 * fuentes, donde a la información se le asigna un significado, de forma que pueda ser interpretada y
 * procesada por personas y/o sistemas, es una creación original del Fondo de Información y Documentación
 * para la Industria INFOTEC, cuyo registro se encuentra actualmente en trámite.
 *
 * INFOTEC pone a su disposición la herramienta SemanticWebBuilder a través de su licenciamiento abierto al público (‘open source’),
 * en virtud del cual, usted podrá usarlo en las mismas condiciones con que INFOTEC lo ha diseñado y puesto a su disposición;
 * aprender de él; distribuirlo a terceros; acceder a su código fuente y modificarlo, y combinarlo o enlazarlo con otro software,
 * todo ello de conformidad con los términos y condiciones de la LICENCIA ABIERTA AL PÚBLICO que otorga INFOTEC para la utilización
 * del SemanticWebBuilder 4.0.
 *
 * INFOTEC no otorga garantía sobre SemanticWebBuilder, de ninguna especie y naturaleza, ni implícita ni explícita,
 * siendo usted completamente responsable de la utilización que le dé y asumiendo la totalidad de los riesgos que puedan derivar
 * de la misma.
 *
 * Si usted tiene cualquier duda o comentario sobre SemanticWebBuilder, INFOTEC pone a su disposición la siguiente
 * dirección electrónica:
 *  http://www.semanticwebbuilder.org.mx
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
import org.semanticwb.SWBException;

import org.semanticwb.model.Resource;
import org.semanticwb.model.ResourceType;

import org.semanticwb.model.SWBModel;
import org.semanticwb.model.ModelProperty;
import org.semanticwb.model.ResourceSubType;
import org.semanticwb.portal.util.GridUtils;
import org.semanticwb.portal.api.SWBResource;
import org.semanticwb.portal.api.GenericResource;
import org.semanticwb.portal.api.SWBParamRequest;
import org.semanticwb.portal.api.SWBParamRequestImp;
import org.semanticwb.portal.api.SWBResourceException;
import org.semanticwb.portal.util.GridUtils.GridCell;

/**
 * GridContent implements grid composer view
 *
 * @author sergio.tellez
 * @version 1.0
 * @see GenericResource
 */
public class GridContent extends GenericResource {
    
    /** The log. */
    private static final Logger LOG = SWBUtils.getLogger(GridContent.class);
    public static final String DEFAUL_HTML = "<template method=\"setHeaders\" Content-Type=\"text/html\"  response=\"{response}\" />\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>\n<title>\n   <TOPIC METHOD=\"getDisplayName\" LANGUAGE=\"{user@getLanguage}\"/>\n</title>\n\n"+gridPathLibs()+"</head>\n <body>\n <RESOURCE TYPE=\"GridContent\" />\n </body>\n</html>";
    
    @Override
    public void install(ResourceType recobj) throws SWBResourceException {
        ResourceType menuNivel = ResourceType.ClassMgr.getResourceType("MenuNivel", recobj.getWebSite());/* Retrieve resources type from site for create composer sub types */
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
        String idTplGroup = createTemplateGroup("Behaviour", recobj.getWebSite()).getId(); /* create group and default template for grid viewer */
        Template gridTpl = createTemplate("grid", "Template for grid component", true, recobj.getWebSite(), idTplGroup);
        recobj.getWebSite().addModelProperty(getModelProperty("idSubTypeST", "Identificador de subtipo de menu nivel", "gmenu", recobj.getWebSite()));/* create site properties for default groupers and template */
        recobj.getWebSite().addModelProperty(getModelProperty("idGridTmpl", "Identificador de contenedor de grid", gridTpl.getId(), recobj.getWebSite()));
        recobj.getWebSite().addModelProperty(getModelProperty("idSubTypeMN", "Identificador de subtipo de texto estático", "gelements", recobj.getWebSite()));
        Resource res = recobj.getWebSite().createResource();
        res.setResourceType(recobj);/* create resource instance for grid viewer */
        res.setIndex(2);
        res.setTitle("composer");
        res.setActive(Boolean.TRUE);
        res.setDescription("Grid reader");
        try {
            res.updateAttributesToDB();
        } catch (SWBException ex) {
            LOG.error(ex);
        }
    }
    
    @Override
    public void doView(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws IOException {
        WebPage page = paramRequest.getWebPage();
        Iterator<Resource> it = page.listResources();/* Get resources associated with the grid's host web page*/
        Map<String, Resource> resources = new LinkedHashMap<>();
        String jsongrid = page.getProperty("_elements");/* Get json grid definition from page property*/
        while (it.hasNext()) {
            Resource res = (Resource)it.next();
            resources.put(res.getId(), res);
        }
        JSONObject obj = new JSONObject(jsongrid);
        JSONArray jsonCells = obj.getJSONArray("elements");
        render(jsonCells, (short) 50, "cellClass", request, response, paramRequest);
    }
    
    /**
     * This method is used to render resources view according grid definition.
     * @param JSONArray json nodes grid definition.
     * @param short heightUnit cell height
     * @param String styleClass css class name
     * @throws IOException 
     * @see GridCell, GridUtils
     */
    private void render(JSONArray nodes, short heightUnit, String styleClass, HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws IOException {
        PrintWriter out = response.getWriter();
        if (heightUnit < 50) heightUnit = 50;
        short maxRow = (short) (nodes.getJSONObject(nodes.length() - 1).getInt("y"));
        String[] rows = new String[maxRow + 1];
        List<GridCell> cells = GridUtils.convert2Cells(nodes);
        GridUtils.renderRow(rows, maxRow);
        for (short h = 0; h <= maxRow; h++) {
            short row = h;
            List<GridCell> cellsInRow = cells.stream().filter(cell -> cell.getY() == row).collect(Collectors.toCollection(ArrayList::new));
            out.print(rows[h]);
            for (short k = 0; k < cellsInRow.size(); k++) {
                short offset = 0;
                if (k == 0) {
                    offset = (short) (cellsInRow.get(k).getX());
                } else {
                    GridCell leftCell = cellsInRow.get(k - 1);
                    offset = (short) (cellsInRow.get(k).getX() - (leftCell.getX() + leftCell.getWidth()));
                }
                renderCell(cellsInRow.get(k), styleClass, offset, heightUnit, request, response, paramRequest);
            }
            out.println("</div>");
        }
    }
    
    /**
     * This method is used to construct responsive cell according to grid definition.
     * @param GridCell cell unit grid structure.
     * @param String styleClass css class name.
     * @param short offset cell compensation in grid struture.
     * @param short heightUnit cell height
     * @throws IOException 
     * @see GridCell
     */
    private void renderCell(GridCell cell, String styleClass, short offset, short heightUnit, HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws IOException {
        PrintWriter out = response.getWriter();
        out.print("<div class=\"");
        String cssName = null != cell.getCssName() && !cell.getCssName().isEmpty() ? cell.getCssName() : styleClass;
        out.print(cssName);
        out.print(cell.getColXs() > 0 ? " col-xs-" + cell.getColXs() : "");
        out.print(cell.getColSm() > 0 ? " col-sm-" + cell.getColSm() : "");
        out.print(cell.getWidth() > 0 ? " col-md-" + cell.getWidth() : " col-md-auto");
        out.print(cell.getColLg() > 0 ? " col-lg-" + cell.getColLg() : "");
        //out.print(cell.getColXl() > 0 ? " col-xl-" + cell.getColXl() : "");
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
        doPreview(request, response, paramRequest, cell.getId());
        out.println("</div>");
    }
    
    /**
     * This method is used to render resources content.
     * @param String id resource identifier
     * @see SWBResource, SWBParamRequestImp
     */
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
    
    /**
     * This method is used to create group template.
     * @param String title
     * @param WebSite site 
     * @return TemplateGroup New template group.
     * @see TemplateGroup, WebSite
     */
    private TemplateGroup createTemplateGroup(String title, WebSite site) {
        TemplateGroup grp = site.createTemplateGroup();
        grp.setTitle(title);
        return grp;
    }
    
    /**
     * Returns new template that contains default html for grid viewer.
     * @param String title
     * @param String description
     * @param Boolean active 
     * @param WebSite site 
     * String templateGroup Identifier of group to template will belong.
     * @return Template New web page template
     * @see Template, WebSite
     */
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
    
    /**
     * Returns site property that can then be used by several resoures.
     * @param String id Identifier for property.
     * @param String title Name for property.
     * @param String value Value for property.
     * @param SWBModel model Site to will assign property.
     * @return ModelProperty site property
     * @see ModelProperty, SWBModel
     */
    private ModelProperty getModelProperty(String id, String title, String value, SWBModel model) {
        ModelProperty mProperty = ModelProperty.ClassMgr.createModelProperty(id, model);
        mProperty.setTitle(title);
        mProperty.setValue(value);
        return mProperty;
    }
    
    /**
     * Returns path libraries used in default html definition.
     * @return String path libraries
     */
    private static String gridPathLibs() {
        StringBuilder libs = new StringBuilder();
        libs.append("\n <link rel=\"stylesheet\" href=\"{webpath}/swbadmin/css/bootstrap/bootstrap_3.3.7.min.css\">");
        libs.append("\n <link rel='stylesheet' type='text/css' media='all' href='{webpath}/swbadmin/css/swb.css' />");
        libs.append("\n <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js\"></script>");
        libs.append("\n <script src=\"https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.11.0/jquery-ui.js\"></script>");
        libs.append("\n <script src=\"{webpath}/swbadmin/js/bootstrap/bootstrap_3.3.7.min.js\"></script>");
        
        libs.append("\n\n <link rel=\"stylesheet\" href=\"{webpath}/swbadmin/js/gridstack1.0.0/gridstack.css\" />");
        libs.append("\n <link rel=\"stylesheet\" href=\"{webpath}/swbadmin/js/gridstack1.0.0/gridstack-extra.css\" />");
        libs.append("\n <script src=\"https://cdnjs.cloudflare.com/ajax/libs/lodash.js/3.5.0/lodash.min.js\" ></script>");
        libs.append("\n <script src=\"{webpath}/swbadmin/js/gridstack1.0.0/gridstack.js\" ></script>");
        libs.append("\n <script src=\"{webpath}/swbadmin/js/gridstack1.0.0/gridstack.jQueryUI.js\" ></script>\n");
        return libs.toString();
    }
}