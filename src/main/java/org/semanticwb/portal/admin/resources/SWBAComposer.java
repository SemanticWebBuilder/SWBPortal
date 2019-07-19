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
package org.semanticwb.portal.admin.resources;

/**
 *
 * @author sergio.tellez
 */
package org.semanticwb.portal.admin.resources;

import java.util.Map;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.semanticwb.Logger;
import org.semanticwb.SWBUtils;
import org.semanticwb.SWBException;
import org.semanticwb.model.Resource;
import org.semanticwb.model.ResourceType;
import org.semanticwb.model.WebPage;
import org.semanticwb.model.Template;
import org.semanticwb.model.TemplateRef;
import org.semanticwb.model.WebSite;
import org.semanticwb.portal.api.SWBParamRequest;
import org.semanticwb.portal.api.SWBActionResponse;
import org.semanticwb.portal.api.GenericAdmResource;
import org.semanticwb.portal.api.SWBResourceException;

public class SWBAComposer extends GenericAdmResource {
    
    /** The log. */
    private static final Logger LOG = SWBUtils.getLogger(SWBAComposer.class);
    
    public static final String ACTION_ADD_GRID = "addg";
    public static final String ACTION_UPD_GRID = "updg";
    
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
        super.processRequest(request, response, paramRequest);
    }
    
    @Override
    public void doView(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
        String path = "/swbadmin/jsp/composer/gridEditor.jsp";
        RequestDispatcher rd = request.getRequestDispatcher(path);
        try {
            request.setAttribute("paramRequest", paramRequest);
            if (null != request.getParameter("suri"))
                request.setAttribute("suri", request.getParameter("suri"));
            rd.include(request, response);
        } catch (ServletException se) {
            LOG.error(se);
        }
    }
    
    @Override
    public void processAction(HttpServletRequest request, SWBActionResponse response) throws SWBResourceException, IOException {
        String suri = request.getParameter("suri");
        int index = suri.lastIndexOf(":");
        String id = suri.substring(index + 1, suri.length());
        String idTmpl = getResourceBase().getAttribute("idTmpl","2");
        WebPage wp = response.getWebPage().getWebSite().getWebPage(id);
        try {
            if (ACTION_ADD_GRID.equalsIgnoreCase(response.getAction())) {
                String jsongrid = request.getParameter("jsongrid");
                if (null != wp) {
                    Template templateIndex = Template.ClassMgr.getTemplate(idTmpl, response.getWebPage().getWebSite());
                    TemplateRef temrefindex = TemplateRef.ClassMgr.createTemplateRef(response.getWebPage().getWebSite());
                    temrefindex.setActive(Boolean.TRUE);
                    temrefindex.setTemplate(templateIndex);
                    temrefindex.setInherit(TemplateRef.INHERIT_ACTUAL);
                    temrefindex.setPriority(2);
                    wp.addTemplateRef(temrefindex);
                    String elements = resToJson(jsongrid, wp);
                    if (null != elements && elements.isEmpty()) {
                        Resource grid = createResource(wp, "GridContent", "Grid component");
                        if (null != grid) {
                            grid.setAttribute("elements", elements);
                            grid.updateAttributesToDB();
                        }
                    }
                }
            }
        } catch (SWBException ex) {
            LOG.error(ex);
        }
    }
    
    private String resToJson(String json, WebPage wp) {
        JSONObject resources = new JSONObject();
        List<Map<String, String>> elements = new ArrayList<>();
        if (null == json || json.trim().isEmpty()) return null;
        if (!json.startsWith("{"))
            json = "{" + " \"elements: \"" + json +  "}";
        JSONObject obj = new JSONObject(json);
        JSONArray arr = obj.getJSONArray("elements");
        for (int i = 0; i < arr.length(); i++) {
            String resourceType = arr.getJSONObject(i).getString("resourceType");
            Resource resource = createResource(wp, resourceType, "Grid component");
            if (null != resource) {
                wp.addResource(resource);
                Map<String, String> element = new LinkedHashMap<>();
                element.put("x", arr.getJSONObject(i).getString("x"));
                element.put("y", arr.getJSONObject(i).getString("y"));
                element.put("width", arr.getJSONObject(i).getString("width"));
                element.put("height", arr.getJSONObject(i).getString("height"));
                element.put("height", arr.getJSONObject(i).getString("height"));
                element.put("resourceType", resourceType);
                element.put("resourceId", resource.getId());
                elements.add(element);
            }
        }
        if (!elements.isEmpty()) resources.put("elements", elements);
        return resources.toString(3);
    }
    
    private Resource createResource(WebPage wp, String resourceType, String resourceTitle) {
        Resource res = null;
        if (null == wp) return null;
        WebSite site = wp.getWebSite();
        try {
            ResourceType resType = ResourceType.ClassMgr.getResourceType(resourceType, site);
            res = site.createResource();
            res.setIndex(2);
            res.setResourceType(resType);
            res.setTitle(resourceTitle);
            res.setActive(Boolean.TRUE);
            res.updateAttributesToDB();
        }catch (SWBException e) {
            LOG.error(e);
        }
        return res;
    }
}