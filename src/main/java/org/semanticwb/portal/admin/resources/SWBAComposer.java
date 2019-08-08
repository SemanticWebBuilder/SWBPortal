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
 * SWBAComposer implements grid composer behaviour
 *
 * @author sergio.tellez
 * @version 1.0
 */
package org.semanticwb.portal.admin.resources;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.semanticwb.Logger;
import org.semanticwb.SWBUtils;
import org.semanticwb.SWBException;
import org.semanticwb.model.WebPage;
import org.semanticwb.model.WebSite;
import org.semanticwb.model.Resource;
import org.semanticwb.model.ResourceType;
import org.semanticwb.model.ResourceSubType;

import org.semanticwb.model.Template;
import org.semanticwb.model.TemplateRef;
import org.semanticwb.portal.api.SWBParamRequest;
import org.semanticwb.portal.api.SWBActionResponse;
import org.semanticwb.portal.api.GenericAdmResource;
import org.semanticwb.portal.api.SWBResourceException;

 /**
  * @see GenericAdmResource 
  */
public class SWBAComposer extends GenericAdmResource {
    
    /** The log. */
    private static final Logger LOG = SWBUtils.getLogger(SWBAComposer.class);
    
    public static final String ACTION_ADD_GRID = "addg";
    public static final String ACTION_UPD_GRID = "updg";
    public static final String MODE_RES_ADM_CALL = "resadmcall";
    
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
        if (paramRequest.getMode().equals(SWBAComposer.MODE_RES_ADM_CALL)) {
            doResourceAdminCall(request, response, paramRequest);
        } else {
            super.processRequest(request, response, paramRequest);
        }
    }
  
    @Override
    public void doView(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
        String path = "/swbadmin/jsp/composer/gridEditor.jsp";
        RequestDispatcher rd = request.getRequestDispatcher(path);
        try {
            WebPage wp = getHost(request);
            request.setAttribute("paramRequest", paramRequest);
            if (null != request.getParameter("suri")) {
                request.setAttribute("suri", request.getParameter("suri"));
                request.setAttribute("_elements", wp.getProperty("_elements", ""));
                request.setAttribute("webSiteId", wp.getWebSiteId());
            }
            rd.include(request, response);
        } catch (ServletException se) {
            LOG.error(se);
        }
    }
    
    @Override
    public void processAction(HttpServletRequest request, SWBActionResponse response) throws SWBResourceException, IOException {
        WebPage wp = getHost(request);
        String jsongrid = request.getParameter("jsongrid");
        if (null != wp) {
            if (ACTION_ADD_GRID.equalsIgnoreCase(response.getAction())) {
                String idTmpl = null != wp.getWebSite().getModelProperty("idGridTmpl") ? wp.getWebSite().getModelProperty("idGridTmpl") : "2"; /* Read id grid default template from site properties*/
                Template templateIndex = Template.ClassMgr.getTemplate(idTmpl, wp.getWebSite());
                TemplateRef temrefindex = getTemplateRef(wp, templateIndex);
                if (!wp.hasTemplateRef(temrefindex)) { /* if grid template is not previously assigned */
                    temrefindex.setActive(Boolean.TRUE);
                    temrefindex.setTemplate(templateIndex);
                    temrefindex.setInherit(TemplateRef.INHERIT_ACTUAL);
                    temrefindex.setPriority(2);
                    wp.addTemplateRef(temrefindex);
                }
            }else if (ACTION_UPD_GRID.equalsIgnoreCase(response.getAction())) {
                //ACTIONS FOR UPDATE
            }
            String elements = resToJson(jsongrid, wp);
            if (null != elements && !elements.isEmpty())
                wp.setProperty("_elements", elements); /* assign json grid to web page props */
        }
        response.setRenderParameter("suri", request.getParameter("suri"));
    }
    
    /**
     * This method is used to call admin resources.
     * @param request
     * @param response
     * @param paramRequest
     * @throws IOException 
     * @throws SWBResourceException
     */
    public void doResourceAdminCall(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
        String path = "/swbadmin/jsp/composer/resourceAdmCaller.jsp";
        RequestDispatcher rd = request.getRequestDispatcher(path);
        try {
            rd.include(request, response);
        } catch (ServletException se) {
            LOG.error(se);
        }
    }
    
    /**
     * This method is used to create new resources from grid definition
     * @param json json grid definition
     * @param wp   wep page that allocates composer
     * @return String resources json mapping
     */
    private String resToJson(String json, WebPage wp) {
        JSONObject resources = new JSONObject();
        List<Map<String, Object>> elements = new ArrayList<>();
        if (null == json || json.trim().isEmpty()) return null;
        if (!json.startsWith("{"))
            json = "{" + " \"elements\" : " + json +  "}";
        JSONObject obj = new JSONObject(json);
        JSONArray arr = obj.getJSONArray("elements");
        Map<String, Resource> map = mapResources(wp);
        for (int i = 0; i < arr.length(); i++) {
            Resource resource = null;
            String resourceId = arr.getJSONObject(i).getString("resourceId");
            String resourceType = arr.getJSONObject(i).getString("resourceType");
            if (null != resourceId && !resourceId.isEmpty() && map.containsKey(resourceId)) {
                resource = map.get(resourceId);
                map.remove(resourceId);
            }else 
                resource = createResource(wp, resourceType);
            if (null != resource) {
                Map<String, Object> element = new LinkedHashMap<>();
                element.put("x", arr.getJSONObject(i).getInt("x"));
                element.put("y", arr.getJSONObject(i).getInt("y"));
                element.put("width", arr.getJSONObject(i).getInt("width"));
                element.put("height", arr.getJSONObject(i).getInt("height"));
                element.put("resourceType", resourceType);
                element.put("resourceId", resource.getId());
                element.put("colXs", !arr.getJSONObject(i).getString("colXs").isEmpty() ? arr.getJSONObject(i).getInt("colXs") : 0);
                element.put("colSm", !arr.getJSONObject(i).getString("colSm").isEmpty() ? arr.getJSONObject(i).getInt("colSm") : 0);
                element.put("colLg", !arr.getJSONObject(i).getString("colLg").isEmpty() ? arr.getJSONObject(i).getInt("colLg") : 0);
                element.put("colXl", !arr.getJSONObject(i).getString("colXl").isEmpty() ? arr.getJSONObject(i).getInt("colXl") : 0);
                element.put("title", arr.getJSONObject(i).getString("title"));
                element.put("classname", arr.getJSONObject(i).getString("classname"));
                elements.add(element);
            }
        }
        if (!map.isEmpty()) removeResources(map, wp);
        if (!elements.isEmpty()) resources.put("elements", elements);
        return resources.toString(3);
    }
    
    /**
     * This method is used to create new resources from resourceType definition
     * @param String resourceType id that identifies resource type
     * @param wp wep page that allocates composer
     * @return Resource new resource instance
     * @see Resource
     * 
     */
    private Resource createResource(WebPage wp, String resourceType) {
        Resource res = null;
        ResourceSubType resSubType = null;
        if (null == wp) return null;
        WebSite site = wp.getWebSite();
        try {
            String idResourceType = getIdResourceType(resourceType);
            if (null == idResourceType) return null;
            ResourceType resType = ResourceType.ClassMgr.getResourceType(idResourceType, site);
            res = site.createResource();
            res.setResourceType(site.getResourceType(idResourceType));
            res.setIndex(2);
            res.setActive(Boolean.TRUE);
            res.setResourceType(resType);
            res.setTitle(idResourceType+"-"+res.getId());
            if ("StaticText".equalsIgnoreCase(idResourceType) && null != site.getModelProperty("idSubTypeST"))/** Group resources in components sub type*/
                resSubType = ResourceSubType.ClassMgr.getResourceSubType(getResourceBase().getAttribute("idSubTypeST"), site);
            else if ("MenuNivel".equalsIgnoreCase(idResourceType) && null != site.getModelProperty("idSubTypeMN"))
                resSubType = ResourceSubType.ClassMgr.getResourceSubType(getResourceBase().getAttribute("idSubTypeMN"), site);
            if (null != resSubType) res.setResourceSubType(resSubType);
            res.updateAttributesToDB();
            wp.addResource(res);
        }catch (SWBException e) {
            LOG.error(e);
        }
        return res;
    }
    
    /**
     * This method is used to retrieve swb canonical resource id
     * @param String resourceType id that identifies resource type
     * @return String id resource type
     * 
     */
    private String getIdResourceType(String resourceType) {
        String idResourceType = null;
        if (resourceType.equalsIgnoreCase("menu")) idResourceType = "MenuNivel";
        else if (resourceType.equalsIgnoreCase("staticText")) idResourceType = "StaticText";
        else if (resourceType.equalsIgnoreCase("htmlContent")) idResourceType = "HTMLContent";
        return idResourceType;
    }
    
    /**
     * This method is used to obtain web page where the composer is being used to design it.
     * @param HttpServletRequest request
     * @return String id resource type
     * @see WebPage
     */
    private WebPage getHost(HttpServletRequest request) {
        String suri = request.getParameter("suri");/**parameter suri is sended by swb admin tab (behaviour)*/
        if (null == suri) return null;
        int index = suri.lastIndexOf(":");
        String idpage = suri.substring(index + 1, suri.length());
        int init = suri.indexOf(".")+1;
        index = suri.indexOf(".", init);
        String idsite = suri.substring(init, index);
        WebSite site = WebSite.ClassMgr.getWebSite(idsite);
        WebPage wp = site.getWebPage(idpage);
        return wp;
    }
    
    /**
     * This method is used to obtain list resources assigned to web page.
     * @param WebPage selected web page 
     * @return Map<String, Resource> map resources by identifier key
     * @see WebPage, Resource
     */
    private Map<String, Resource> mapResources(WebPage wp) {
        Map<String, Resource> map = new LinkedHashMap<>();
        Iterator<Resource> it = wp.listResources();
        while (it.hasNext()) {
            Resource res = it.next();
            map.put(res.getId(), res);
        }
        return map;
    }
    
    
    /**
     * This method is used to remove resources previously erased from grid composer.
     * @param Map<String, Resource> map resources by identifier key
     * @param WebPage web page that removed resources from grid
     * @return Map<String, Resource> map resources by identifier key
     * @see WebPage, Resource
     */
    private void removeResources(Map<String, Resource> map, WebPage wp) {
        Iterator<String> it = map.keySet().iterator();
        while (it.hasNext()) {
            Resource.ClassMgr.removeResource(it.next(), wp.getWebSite());
        }
    }
    
    private TemplateRef getTemplateRef(WebPage wp, Template templateIndex) {
        Iterator<TemplateRef> it = wp.listTemplateRefs();
        while (it.hasNext()) {
            TemplateRef tmp = it.next();
            if (tmp.getTemplate().equals(templateIndex)) return tmp;
        }
        return TemplateRef.ClassMgr.createTemplateRef(wp.getWebSite());
    }
}
