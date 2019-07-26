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
import java.io.BufferedWriter;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.semanticwb.Logger;
import org.semanticwb.SWBUtils;
import org.semanticwb.SWBPortal;
import org.semanticwb.model.WebSite;
import org.semanticwb.model.WebPage;
import org.semanticwb.model.Template;
import org.semanticwb.model.TemplateGroup;

import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.semanticwb.model.Resource;
import org.semanticwb.model.ResourceType;
import org.semanticwb.model.ResourceSubType;
import org.semanticwb.portal.api.SWBResource;
import org.semanticwb.portal.api.SWBParamRequest;
import org.semanticwb.portal.api.GenericAdmResource;
import org.semanticwb.portal.api.SWBParamRequestImp;
import org.semanticwb.portal.api.SWBResourceException;

/**
 *
 * @author sergio.tellez
 */
public class GridContent extends GenericAdmResource {
    
    /** The log. */
    private static final Logger LOG = SWBUtils.getLogger(GridContent.class);
    public static final String DEFAUL_HTML = "<template method=\"setHeaders\" Content-Type=\"text/html\"  response=\"{response}\" />\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>\n<title>\n   <TOPIC METHOD=\"getDisplayName\" LANGUAGE=\"{user@getLanguage}\"/>\n</title>\n\n</head>\n <body>\n <RESOURCE TYPE=\"GridContent\" />\n </body>\n</html>";
    
    @Override
    public void install(ResourceType recobj) throws SWBResourceException {
        ResourceType menuNivel = ResourceType.ClassMgr.getResourceType("MenuNivel", getResourceBase().getWebSite());
        ResourceType staticText = ResourceType.ClassMgr.getResourceType("StaticText", getResourceBase().getWebSite());
        if (null != menuNivel) {
            ResourceSubType resSubType = ResourceSubType.ClassMgr.createResourceSubType("gmenu", getResourceBase().getWebSite());
            resSubType.setType(menuNivel);
        }
        if (null != staticText) {
            ResourceSubType resSubType = ResourceSubType.ClassMgr.createResourceSubType("gelements", getResourceBase().getWebSite());
            resSubType.setType(staticText);
        }
        String idTplGroup = createTemplateGroup("Behaviour", getResourceBase().getWebSite()).getId();
        Template gridTpl = createTemplate("grid", "Template for grid component", true, getResourceBase().getWebSite(), idTplGroup);
        getResourceBase().setAttribute("idTemplate", gridTpl.getId());
        //Create site properties for config composer
    }
    
    @Override
    public void doView(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws IOException {
        String jsongrid = "";
        WebPage page = paramRequest.getWebPage();
        Iterator it = page.listResources();
        Map<String, Resource> resources = new LinkedHashMap<>();
        String path = "/work/models/"+page.getWebSite().getId()+"/jsp/composer/preview.jsp";
        ResourceType resType = ResourceType.ClassMgr.getResourceType("GridContent", page.getWebSite());
        while (it.hasNext()) {
            Resource res = (Resource)it.next();
            if (res.getResourceType().equals(resType)) jsongrid = res.getAttribute("elements");
            else resources.put(res.getId(), res);
        }
        if (null != jsongrid && !jsongrid.isEmpty()) {
            List<Map<String, String>> elements = jsonToRes(jsongrid);
            Iterator<Map<String, String>> mresources = elements.iterator();
            while (it.hasNext()) {
                Map<String, String> map = mresources.next();
                String resourceId = map.get("resourceId");
                if (null != resourceId && !resourceId.isEmpty()) {
                    if (!resources.containsKey(resourceId)) {
                        resources.remove(resourceId);
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
        }
    }
    
    public static void doPreview(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest, String id) {
        try {
            SWBResource res = SWBPortal.getResourceMgr().getResource(id);
            ((SWBParamRequestImp) paramRequest).setResourceBase(res.getResourceBase());
            ((SWBParamRequestImp) paramRequest).setWindowState(SWBParamRequest.Mode_VIEW);
            res.render(request, response, paramRequest);
        } catch (IOException | SWBResourceException e) {
            LOG.error("Error while getting content string, id:" + id, e);
        }
    }
    
    private List<Map<String, String>> jsonToRes(String jsongrid) {
        List<Map<String, String>> elements = new ArrayList<>();
        if (null == jsongrid || jsongrid.trim().isEmpty()) return elements;
        JSONObject obj = new JSONObject(jsongrid);
        JSONArray arr = obj.getJSONArray("elements");
        for (int i = 0; i < arr.length(); i++) {
            Map<String, String> element = new LinkedHashMap<>();
            element.put("x", arr.getJSONObject(i).getString("x"));
            element.put("y", arr.getJSONObject(i).getString("y"));
            element.put("width", arr.getJSONObject(i).getString("width"));
            element.put("height", arr.getJSONObject(i).getString("height"));
            element.put("height", arr.getJSONObject(i).getString("height"));
            element.put("resourceType", arr.getJSONObject(i).getString("resourceType"));
            element.put("resourceId", arr.getJSONObject(i).getString("resourceId"));
            elements.add(element);
        }
        return elements;
    }
    
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
}