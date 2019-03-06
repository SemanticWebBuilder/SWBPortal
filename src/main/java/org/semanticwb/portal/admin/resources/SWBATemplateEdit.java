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
 *  http://www.semanticwebbuilder.org
 */
package org.semanticwb.portal.admin.resources;

import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBPortal;
import org.semanticwb.SWBUtils;
import org.semanticwb.model.*;
import org.semanticwb.platform.SemanticClass;
import org.semanticwb.platform.SemanticObject;
import org.semanticwb.platform.SemanticOntology;
import org.semanticwb.portal.SWBFormMgr;
import org.semanticwb.portal.api.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

// TODO: Auto-generated Javadoc

/**
 * The Class SWBATemplateEdit.
 * @author juan.fernandez
 * @author Hasdai Pacheco
 */
public class SWBATemplateEdit extends GenericResource {

    /**
     * The webpath.
     */
    String webpath = SWBPlatform.getContextPath();
    /**
     * The base.
     */
    Resource base;
    /**
     * The ont.
     */
    SemanticOntology ont = SWBPlatform.getSemanticMgr().getOntology();
    /**
     * The LOG.
     */
    private static final org.semanticwb.Logger LOG = SWBUtils.getLogger(SWBATemplateEdit.class);

    /**
     * Handles HTML content parsing to replace paths on loaded templates.
     *
     * @param request
     * @param response
     * @param paramRequest
     * @throws SWBResourceException
     * @throws IOException
     */
    public void doParseHTML(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
        response.setContentType("text/html");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        String in = SWBUtils.IO.readInputStream(request.getInputStream());
        String ret = SWBPortal.UTIL.parseHTML(in, "images/");

        out.print(ret);
    }

    /**
     * Handles fileExplorer mode for TemplateEditor
     *
     * @param request
     * @param response
     * @param paramRequest
     * @throws SWBResourceException
     * @throws IOException
     */
    public void doFileExplorer(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
        String id = request.getParameter("suri");
        SemanticObject obj = SemanticObject.createSemanticObject(id);

        String jsp = "/swbadmin/jsp/SWBATemplateEdit/fileManager.jsp";
        if (obj != null) {
            RequestDispatcher rd = request.getRequestDispatcher(jsp);

            try {
                request.setAttribute("paramRequest", paramRequest);
                request.setAttribute("webSiteId", obj.getModel().getName());
                request.setAttribute("templateId", obj.getId());
                request.setAttribute("verNum", Integer.parseInt(request.getParameter("vnum")));
                rd.include(request, response);
            } catch (Exception ex) {
                LOG.error("Error iuncluding fileManager", ex);
            }
        }
    }


    /* (non-Javadoc)
     * @see org.semanticwb.portal.api.GenericResource#doView(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.semanticwb.portal.api.SWBParamRequest)
     */
    @Override
    public void doView(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
        response.setContentType("text/html; charset=ISO-8859-1");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        base = getResourceBase();
        User user = paramRequest.getUser();
        PrintWriter out = response.getWriter();
        String id = request.getParameter("suri");

        if (request.getParameter("dialog") != null && request.getParameter("dialog").equals("close")) {
            out.println("<script type=\"javascript\">");
            out.println(" hideDialog(); ");
            out.println(" reloadTab('" + id + "'); ");
            out.println("</script>");
            return;
        }


        if (null == id) {
            out.println("<fieldset>");
            out.println("URI faltante");
            out.println("</fieldset>");
        } else {
            String action = request.getParameter("act");
            GenericObject obj = ont.getGenericObject(id);

            Template tmpl = (Template) obj;

            LOG.debug("doView(), suri: " + id);
            VersionInfo via = null;
            VersionInfo vio = null;

            if (obj instanceof Versionable) {
                vio = (VersionInfo) findFirstVersion(obj);
                via = ((Versionable) obj).getActualVersion();

                if (action == null || action.equals("")) {

                    LOG.debug("act:''");
                    out.println("<div class=\"swbform\">");
                    out.println("<fieldset>");
                    out.println("<table width=\"98%\" >");
                    out.println("<thead>");
                    out.println("<tr>");
                    out.println("<th>");
                    out.println(paramRequest.getLocaleString("msgVersion"));
                    out.println("</th>");
                    out.println("<th>");
                    out.println(paramRequest.getLocaleString("msgAction"));
                    out.println("</th>");
                    out.println("<th>");
                    out.println(paramRequest.getLocaleString("msgComment"));
                    out.println("</th>");
                    out.println("</tr>");
                    out.println("</thead>");
                    out.println("<tbody>");
                    if (null != vio) {
                        while (vio != null) {

                            out.println("<tr>");
                            out.println("<td align=\"center\">");
                            out.println(vio.getVersionNumber());
                            out.println("</td>");
                            out.println("<td>");

                            SWBResourceURL urle = paramRequest.getRenderUrl();
                            urle.setParameter("suri", id);
                            urle.setParameter("sobj", vio.getURI());
                            urle.setParameter("vnum", Integer.toString(vio.getVersionNumber()));
                            urle.setParameter("act", "edit_temp");
                            urle.setMode(SWBResourceURL.Mode_EDIT);
                            out.println("<a href=\"#\" onclick=\"submitUrl('" + urle + "',this); return false;\"><img src=\"" + SWBPlatform.getContextPath() + "/swbadmin/icons/editar_1.gif\" border=\"0\" alt=\"editar version\"></a>");

                            SWBResourceURL urlnv = paramRequest.getRenderUrl();
                            urlnv.setParameter("suri", id);
                            urlnv.setParameter("sobj", vio.getURI());
                            urlnv.setParameter("vnum", Integer.toString(vio.getVersionNumber()));
                            urlnv.setParameter("act", "newversion");
                            urlnv.setMode(SWBResourceURL.Mode_EDIT);
                            out.println("<a href=\"#\" onclick=\"showDialog('" + urlnv + "','Nueva versión de Plantilla');\"><img src=\"" + SWBPlatform.getContextPath() + "/swbadmin/icons/nueva_version.gif\" border=\"0\" alt=\"" + paramRequest.getLocaleString("msgNewVersion") + "\"></a>");

                            if (!vio.equals(via)) {
                                SWBResourceURL urlsa = paramRequest.getActionUrl();
                                urlsa.setParameter("suri", id);
                                urlsa.setParameter("sval", vio.getURI());
                                urlsa.setAction("setactual");
                                out.println("<a href=\"#\" onclick=\"submitUrl('" + urlsa + "',this); return false;\"><img src=\"" + SWBPlatform.getContextPath() + "/swbadmin/icons/cambio_version.gif\" border=\"0\" alt=\"" + paramRequest.getLocaleString("logTplSetActual") + "\"></a>");
                            } else {
                                out.println("<img src=\"" + SWBPlatform.getContextPath() + "/swbadmin/icons/activa.gif\" border=\"0\" alt=\"" + paramRequest.getLocaleString("msgActualVersion") + "\">");
                            }

                            out.println("<a href=\"#\" onclick=\"window.open('" + SWBPortal.getWebWorkPath() + tmpl.getWorkPath() + "/" + vio.getVersionNumber() + "/" + tmpl.getFileName(vio.getVersionNumber()) + "','Preview','scrollbars, resizable, width=550, height=550');\"><img src=\"" + SWBPlatform.getContextPath() + "/swbadmin/icons/preview.gif\" border=\"0\" alt=\"" + paramRequest.getLocaleString("msgPreview") + "\"></a>"); //submitUrl('" + urlec + "',this); return false;

                            SWBResourceURL urlr = paramRequest.getActionUrl();
                            urlr.setParameter("suri", id);
                            urlr.setParameter("sval", vio.getURI());
                            urlr.setAction("remove");
                            out.println("<a href=\"#\" onclick=\"if(confirm('" + paramRequest.getLocaleString("q_removeVersion") + "')){submitUrl('" + urlr + "',this);} return false;\"><img src=\"" + SWBPlatform.getContextPath() + "/swbadmin/images/delete.gif\" border=\"0\" alt=\"" + paramRequest.getLocaleString("msgRemoveVersion") + "\"></a>");
                            if (vio.equals(via)) {
                                out.println("( " + paramRequest.getLocaleString("msgActualVersion") + " ) ");
                            }
                            out.println("</td>");
                            out.println("<td>");

                            String comment = vio.getVersionComment() != null && vio.getVersionComment().trim().length() > 0 && !vio.getVersionComment().equals("null") ? vio.getVersionComment() : "";
                            out.println(comment + "</td>");
                            out.println("</tr>");
                            vio = vio.getNextVersion();
                        }
                    }
                    out.println("</tbody>");
                    out.println("</table>");
                    out.println("</fieldset>");
                    out.println("<fieldset>");
                    SWBResourceURL urlNew = paramRequest.getRenderUrl();
                    urlNew.setParameter("suri", id);
                    urlNew.setParameter("act", "newversion");
                    urlNew.setMode(SWBResourceURL.Mode_EDIT);
                    out.println("<button dojoType=\"dijit.form.Button\" onclick=\"showDialog('" + urlNew + "','Agregar plantilla de defecto');\">" + paramRequest.getLocaleString("btn_addnew") + "</button>");
                    SWBResourceURL urlVR = paramRequest.getActionUrl();
                    urlVR.setParameter("suri", id);
                    urlVR.setAction("resetversion");
                    urlVR.setMode(SWBResourceURL.Mode_VIEW);
                    out.println("<button dojoType=\"dijit.form.Button\" onclick=\"if(confirm('" + paramRequest.getLocaleString("q_resetVersion") + "')){submitUrl('" + urlVR.toString() + "',this.domNode);} return false;\">" + paramRequest.getLocaleString("btn_versionreset") + "</button>");
                    out.println("</fieldset>");
                    out.println("</div>");
                }
            }
        }
    }

    /**
     * Find first version.
     *
     * @param obj the obj
     * @return the version info
     */
    private VersionInfo findFirstVersion(GenericObject obj) {
        VersionInfo ver = null;
        if (obj != null) {
            ver = ((Versionable) obj).getActualVersion();
        }
        if (null != ver) {
            while (ver.getPreviousVersion() != null) { //
                ver = ver.getPreviousVersion();
            }
        }
        return ver;
    }

    /**
     * Find version number.
     *
     * @param obj  the obj
     * @param vnum the vnum
     * @return the version info
     */
    private VersionInfo findVersionNumber(GenericObject obj, int vnum) {
        VersionInfo ver = findFirstVersion(obj);

        if (ver != null) {
            if (ver.getVersionNumber() != vnum) {
                VersionInfo vnext = ver.getNextVersion();
                while (vnext != null) {
                    if (vnext.getVersionNumber() == vnum) {
                        ver = vnext;
                        break;
                    }
                    vnext = vnext.getNextVersion();
                }
            }
        }
        return ver;
    }

    /**
     * Builds a JSON String containing resources data to build resource Tree
     *
     * @param request
     * @param response
     * @param paramRequest
     * @throws IOException
     */
    private void getResourceList(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        StringBuilder treeData = new StringBuilder();
        String templateId = (String) request.getParameter("templateId");
        String websiteId = (String) request.getParameter("webSiteId");
        int verNum = Integer.parseInt(request.getParameter("verNum"));
        WebSite site = SWBContext.getWebSite(websiteId);
        User user = paramRequest.getUser();

        SortedMap<String, HashMap<String, String>> elements = new TreeMap();
        HashMap<String, String> root = new HashMap<>();
        root.put("name", "Recursos");
        root.put("uuid", "rootNode");
        elements.put("rootNode", root);

        Iterator<ResourceType> rtypes = site.listResourceTypes();
        while (rtypes.hasNext()) {
            ResourceType rtype = rtypes.next();
            if (! rtype.isDeleted() && user.haveAccess(rtype) && (rtype.getResourceMode() == ResourceType.MODE_STRATEGY || rtype.getResourceMode() == ResourceType.MODE_SYSTEM)) {
                //Add parent entry
                String uid = UUID.randomUUID().toString();
                String rtypeTitle = rtype.getTitle();
                HashMap<String, String> entry = new HashMap<>();
                entry.put("uuid", uid);
                entry.put("name", rtypeTitle);
                entry.put("id", rtype.getId());
                entry.put("parent", "rootNode");
                elements.put(rtypeTitle + "-" + uid, entry);

                Iterator<ResourceSubType> rsubtypes = rtype.listSubTypes();
                if (rsubtypes.hasNext()) {
                    //Add child entries
                    while (rsubtypes.hasNext()) {
                        ResourceSubType rsubtype = rsubtypes.next();

                        if (!rsubtype.isDeleted() && user.haveAccess(rtype)) {
                            String childuid = UUID.randomUUID().toString();
                            String rsubtypeTitle = rsubtype.getTitle();
                            HashMap<String, String> childentry = new HashMap<>();
                            childentry.put("id", rsubtype.getId());
                            childentry.put("uuid", childuid);
                            childentry.put("name", rsubtypeTitle);
                            childentry.put("parent", uid);
                            childentry.put("parenttype", rtype.getId());
                            elements.put(rsubtypeTitle + "-" + childuid, childentry);
                        }
                    }
                }
            }
        }

        treeData.append("[");
        Iterator<HashMap<String, String>> it = elements.values().iterator();
        while (it.hasNext()) {
            HashMap<String, String> entry = it.next();
            treeData.append("{");

            Iterator<String> objKeys = entry.keySet().iterator();
            while (objKeys.hasNext()) {
                String key = objKeys.next();
                String val = entry.get(key);

                treeData.append("\"").append(key).append("\":");
                if (!"subtype".equals(key)) treeData.append("\"");
                treeData.append(val);
                if (!"subtype".equals(key)) treeData.append("\"");

                if (objKeys.hasNext()) treeData.append(",");
            }

            treeData.append("}");
            if (it.hasNext()) treeData.append(",");
        }
        treeData.append("]");

        out.println(treeData.toString());
    }

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
        String mode = paramRequest.getMode();
        if ("getResourceList".equals(mode)) {
            getResourceList(request, response, paramRequest);
        } else if ("getTemplateContent".equals(mode)) {
            getTemplateContent(request, response, paramRequest);
        } else if ("fileManager".equals(mode)) {
            doFileExplorer(request, response, paramRequest);
        } else if ("parseHTML".equals(mode)) {
            doParseHTML(request, response, paramRequest);
        } else {
            super.processRequest(request, response, paramRequest);
        }
    }

    /**
     * Gets content from a template file.
     *
     * @param request
     * @param response
     * @param paramRequest
     * @throws SWBResourceException
     * @throws IOException
     */
    private void getTemplateContent(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        String templateId = (String) request.getParameter("templateId");
        String websiteId = (String) request.getParameter("webSiteId");
        int verNum = Integer.parseInt(request.getParameter("verNum"));
        WebSite site = SWBContext.getWebSite(websiteId);
        User user = paramRequest.getUser();
        Template tpl = site.getTemplate(templateId);

        String templatePath = SWBPortal.getWorkPath() + tpl.getWorkPath() + "/" + verNum + "/" + URLEncoder.encode(tpl.getFileName(verNum));

        if (null != tpl && user.haveAccess(tpl)) {
            FileInputStream fis = new FileInputStream(templatePath);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            SWBUtils.IO.copyStream(fis, out);
            response.getWriter().print(out.toString());
            fis.close();
        }
    }

    @Override
    public void doEdit(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
        String action = request.getParameter("act");
        String id = request.getParameter("suri");

        if ("edit_temp".equals(action)) {
            SemanticObject obj = SemanticObject.createSemanticObject(id);
            String jsp = "/swbadmin/jsp/SWBATemplateEdit/templateEdit.jsp";
            if (obj != null) {
                RequestDispatcher rd = request.getRequestDispatcher(jsp);

                try {
                    request.setAttribute("paramRequest", paramRequest);
                    request.setAttribute("webSiteId", obj.getModel().getName());
                    request.setAttribute("templateId", obj.getId());
                    request.setAttribute("verNum", Integer.parseInt(request.getParameter("vnum")));
                    rd.include(request, response);
                } catch (Exception ex) {
                    LOG.error("Error iuncluding templateEditor", ex);
                }
            }
        } else {
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            Resource base = getResourceBase();
            User user = paramRequest.getUser();

            String idvi = request.getParameter("sobj");
            String vnum = request.getParameter("vnum");
            SemanticObject so = null;
            PrintWriter out = response.getWriter();
            SWBFormMgr fm = null;

            if (action.equals("newversion")) {

                SemanticObject soref = ont.getSemanticObject(id);
                SWBResourceURL urla = paramRequest.getActionUrl();
                urla.setAction("newversion");

                fm = new SWBFormMgr(Versionable.swb_VersionInfo, soref, null);
                fm.addHiddenParameter("suri", id);
                fm.addHiddenParameter("psuri", id);
                if (vnum != null) {
                    fm.addHiddenParameter("vnum", vnum);
                }
                fm.setAction(urla.toString());
                out.println("<div class=\"swbform\">");
                out.println("<form id=\"" + id + "/" + idvi + "/" + base.getId() + "/FVIComment\" action=\"" + urla + "\" method=\"post\" onsubmit=\"submitForm('" + id + "/" + idvi + "/" + base.getId() + "/FVIComment');return false;\">");
                out.println("<input type=\"hidden\" name=\"suri\" value=\"" + id + "\">");
                if (vnum != null) {
                    out.println("<input type=\"hidden\" name=\"vnum\" value=\"" + vnum + "\">");
                }
                out.println("<fieldset>");
                out.println("<table>");
                out.println("<tbody>");
                out.println("<tr>");
                out.println("<td>");
                out.println(fm.renderElement(request, VersionInfo.swb_versionComment.getLabel()) != null ? fm.renderElement(request, VersionInfo.swb_versionComment.getLabel()) : "Comment");
                out.println("</td>");
                out.println("<td>");
                out.println(fm.renderElement(request, VersionInfo.swb_versionComment, SWBFormMgr.MODE_EDIT));
                out.println("</td>");
                out.println("</tr>");
                out.println("</tbody>");
                out.println("</table>");
                out.println("</filedset>");
                out.println("<filedset>");
                out.println("<button dojoType=\"dijit.form.Button\" type=\"submit\" >Guardar</button>"); //_onclick=\"submitForm('"+id+"/"+idvi+"/"+base.getId()+"/FVIComment');return false;\"
                SWBResourceURL urlb = paramRequest.getRenderUrl();
                urlb.setMode(SWBResourceURL.Mode_VIEW);
                urlb.setParameter("act", "");
                urlb.setParameter("suri", id);
                out.println("<button dojoType=\"dijit.form.Button\" onclick=\"hideDialog(); return false;\">Cancelar</button>"); //submitUrl('" + urlb + "',this.domNode); hideDialog();
                out.println("</filedset>");
                out.println("</form>");
                out.println("</div>");

            } else if (action.equals("edit")) {

                SWBResourceURL urla = paramRequest.getActionUrl();
                urla.setAction("update");
                LOG.debug("VI id:" + idvi);
                so = ont.getSemanticObject(idvi);
                fm = new SWBFormMgr(so, null, SWBFormMgr.MODE_EDIT);
                fm.addHiddenParameter("suri", id);
                fm.addHiddenParameter("psuri", id);
                fm.addHiddenParameter("sobj", so.getURI());
                fm.setAction(urla.toString());

                out.println(fm.renderForm(request));
            }
        }
    }

    @Override
    public void processAction(HttpServletRequest request, SWBActionResponse response) throws SWBResourceException, IOException {
        String id = request.getParameter("suri"); // uri de la plantilla
        String act = response.getAction();
        LOG.debug("Action:" + act + ", suri: " + id);
        GenericObject go = ont.getGenericObject(id);
        VersionInfo vio = null;
        VersionInfo via = null;
        VersionInfo vil = null;
        VersionInfo vin = null;
        if (act == null) {
            act = "";
        }
        if ("newversion".equals(act)) {
            if (go instanceof Versionable) {

                LOG.debug("processAction. newVersion(Versionable)");
                Versionable gov = (Versionable) go;
                SemanticObject sobase = ont.getSemanticObject(id);
                SemanticClass sc = VersionInfo.swb_VersionInfo;
                long lid = 0;
                if (sc.isAutogenId()) {
                    lid = sobase.getModel().getCounter(sc);
                }
                SemanticObject nvinf = sobase.getModel().createSemanticObject(sobase.getModel().getObjectUri("" + lid, sc), sc);
                GenericObject ngo = nvinf.getGenericInstance(); //ont.getGenericObject(nvinf.getURI());
                vin = (VersionInfo) ngo;
                int vnum = 1;
                vio = (VersionInfo) findFirstVersion(go);
                VersionInfo vicopy = null;
                if (vio != null) {
                    vil = gov.getLastVersion();
                    vnum = vil.getVersionNumber() + 1;
                    LOG.debug("version num:" + vnum);
                    nvinf.setObjectProperty(VersionInfo.swb_previousVersion, vil.getSemanticObject()); //vin.setVersionComment(VersionComment);
                    vil.getSemanticObject().setObjectProperty(VersionInfo.swb_nextVersion, nvinf);
                } else {
                    gov.getSemanticObject().setObjectProperty(Versionable.swb_actualVersion, nvinf);
                }
                nvinf.setIntProperty(VersionInfo.swb_versionNumber, vnum);
                if (request.getParameter("vnum") == null)
                    nvinf.setProperty(VersionInfo.swb_versionFile, "template.html");
                String VersionComment = request.getParameter("versionComment");
                LOG.debug(VersionComment);
                if (VersionComment != null) {
                    nvinf.setProperty(VersionInfo.swb_versionComment, VersionComment); //vin.setVersionComment(VersionComment);
                }
                gov.getSemanticObject().setObjectProperty(Versionable.swb_lastVersion, nvinf);

                Template tmpl = (Template) go;

                if (request.getParameter("vnum") != null) {
                    // copiar archivos

                    VersionInfo vi = findVersionNumber(go, Integer.parseInt(request.getParameter("vnum")));
                    nvinf.setProperty(VersionInfo.swb_versionFile, vi.getVersionFile());

                    String rutaFS_source_path = SWBPortal.getWorkPath() + tmpl.getWorkPath() + "/" + request.getParameter("vnum") + "/";
                    String rutaFS_target_path = SWBPortal.getWorkPath() + tmpl.getWorkPath() + "/" + vnum + "/";
                    String rutaWeb_source_path = SWBPortal.getWebWorkPath() + tmpl.getWorkPath() + "/" + request.getParameter("vnum");
                    String rutaWeb_target_path = SWBPortal.getWebWorkPath() + tmpl.getWorkPath() + "/" + vnum;

                    if (SWBUtils.IO.copyStructure(rutaFS_source_path, rutaFS_target_path, true, rutaWeb_source_path, rutaWeb_target_path)) {
//                        System.out.println("Copied OK");
                    }
                } else {
                    String defaultTPL = Template.DEFAUL_HTML;
                    String rutaFS_target_path = SWBPortal.getWorkPath() + tmpl.getWorkPath() + "/" + vnum + "/";
                    File f = new File(rutaFS_target_path);
                    if (!f.exists()) {
                        f.mkdirs();
                    }

                    File ftmpl = new File(SWBPortal.getWorkPath() + tmpl.getWorkPath() + "/" + vnum + "/template.html");
                    Writer output = new BufferedWriter(new FileWriter(ftmpl));
                    try {
                        output.write(defaultTPL);
                    } finally {
                        output.close();
                    }
                }

                SWBPortal.getTemplateMgr().reloadTemplate(tmpl);
                response.setRenderParameter("dialog", "close");
                response.setRenderParameter("act", "");
                response.setMode(response.Mode_VIEW);
            }
        } else if ("update".equals(act)) {
            id = request.getParameter("psuri");
            response.setRenderParameter(act, "");
            response.setMode(response.Mode_VIEW);
        } else if ("setactual".equals(act)) {
            String idval = request.getParameter("sval");
            SemanticObject sobase = ont.getSemanticObject(id);
            SemanticObject soactual = ont.getSemanticObject(idval);
            sobase.setObjectProperty(Versionable.swb_actualVersion, soactual);

            response.setRenderParameter(act, "");
            response.setMode(response.Mode_VIEW);
        } else if ("remove".equals(act)) {
            String idval = request.getParameter("sval"); // version a eliminar
            LOG.debug("suri:" + id + "sval:" + idval);
            SemanticObject sobj = ont.getSemanticObject(id);
            GenericObject sobase = ont.getGenericObject(idval); //version info a eliminar
            vio = null;
            VersionInfo vip = null;
            vin = null;

            GenericObject gobj = ont.getGenericObject(id); // se obtiene la plantilla y se verifica que sea versionable
            if (gobj instanceof Versionable) {
                Versionable vigo = (Versionable) gobj;
                via = vigo.getActualVersion(); // Version Actual de la plantilla
                vil = vigo.getLastVersion(); // Última versión de la plantilla
                if (sobase instanceof VersionInfo) {
                    vio = (VersionInfo) sobase; // version a eliminar
                    vip = vio.getPreviousVersion();
                    vin = vio.getNextVersion();

                    if (null != vip) // si es una versión intermedia ó la última versión
                    {
                        LOG.debug("version intermedia o ultima --- ");
                        if (null != vin) // si es una version intermedia a eliminarse
                        {
                            LOG.debug("Version intermedia");
                            vip.setNextVersion(vin);
                            vin.setPreviousVersion(vip);

                            if (via.equals(vio)) {
                                sobj.setObjectProperty(Versionable.swb_actualVersion, vin.getSemanticObject());
                            }
                            if (vil.equals(vio)) {
                                sobj.setObjectProperty(Versionable.swb_lastVersion, vin.getSemanticObject());
                            }
                        } else // la ultima version
                        {
                            LOG.debug("Ultima version");
                            vip.getSemanticObject().removeProperty(VersionInfo.swb_nextVersion);
                            if (via.equals(vio)) {
                                sobj.setObjectProperty(Versionable.swb_actualVersion, vip.getSemanticObject());
                            }
                            if (vil.equals(vio)) {
                                sobj.setObjectProperty(Versionable.swb_lastVersion, vip.getSemanticObject());
                            }
                        }
                    } else if (null != vin) //  era la primera version
                    {
                        LOG.debug("primera version");
                        vin.getSemanticObject().removeProperty(VersionInfo.swb_previousVersion);
                        if (via.equals(vio)) {
                            sobj.setObjectProperty(Versionable.swb_actualVersion, vin.getSemanticObject());
                        }
                        if (vil.equals(vio)) {
                            sobj.setObjectProperty(Versionable.swb_lastVersion, vin.getSemanticObject());
                        }
                    } else if (vip == null && vin == null) // es la única version
                    {
                        LOG.debug("Unica version");
                        gobj.getSemanticObject().removeProperty(Versionable.swb_actualVersion);
                        gobj.getSemanticObject().removeProperty(Versionable.swb_lastVersion);
                    }
                    int vnumdel = vio.getVersionNumber();
                    Template tmpl = (Template) go;
                    String rutaFS_source_path = SWBPortal.getWorkPath() + tmpl.getWorkPath() + "/" + vnumdel;
                    if (SWBUtils.IO.removeDirectory(rutaFS_source_path)) {
                        //System.out.println("Remove OK");
                    }


                    vio.removeNextVersion();
                    vio.removePreviousVersion();
                    vio.remove();
                }
            }

            response.setRenderParameter(act, "");
            response.setMode(response.Mode_VIEW);
        } else if ("resetversion".equals(act)) {
            Template tmpl = (Template) go;
            VersionInfo va = tmpl.getActualVersion();
            VersionInfo vl = tmpl.getLastVersion();

            VersionInfo temp = va.getPreviousVersion();
            VersionInfo temp2 = null;

            while (temp != null) {
                temp2 = temp;
                String rutaFS_source_path = SWBPortal.getWorkPath() + tmpl.getWorkPath() + "/" + temp2.getVersionNumber();
                if (SWBUtils.IO.removeDirectory(rutaFS_source_path)) {
                    //System.out.println("Remove back OK by Reset Version: " + temp2.getVersionNumber());
                }
                temp2.remove();
                temp = temp.getPreviousVersion();
            }

            temp = va.getNextVersion();
            if (temp != null) {
                temp.removePreviousVersion();
            }

            // eliminación de archivos de las versiones de las plantillas
            while (temp != null) {
                temp2 = temp;
                temp = temp.getNextVersion();
                String rutaFS_source_path = SWBPortal.getWorkPath() + tmpl.getWorkPath() + "/" + temp2.getVersionNumber();
                if (SWBUtils.IO.removeDirectory(rutaFS_source_path)) {
                    //System.out.println("Remove next OK by Reset Version: " + temp2.getVersionNumber());
                }
            }

            va.removeNextVersion();

            // eliminando versiones de la ultima a una despues de la actual
            if (!vl.equals(va)) {
                vl.remove();
            }

            int va_num = va.getVersionNumber();
            if (va_num != 1) {
                String rutaFS_source_path = SWBPortal.getWorkPath() + tmpl.getWorkPath() + "/" + va_num + "/";
                String rutaFS_target_path = SWBPortal.getWorkPath() + tmpl.getWorkPath() + "/1/";
                String rutaWeb_source_path = SWBPortal.getWebWorkPath() + tmpl.getWorkPath() + "/" + va_num;
                String rutaWeb_target_path = SWBPortal.getWebWorkPath() + tmpl.getWorkPath() + "/1";

                if (SWBUtils.IO.copyStructure(rutaFS_source_path, rutaFS_target_path, true, rutaWeb_source_path, rutaWeb_target_path)) {
                    //System.out.println("Copied actual to 1 OK by Reset Version");
                }

                // Eliminando la version actual
                if (SWBUtils.IO.removeDirectory(rutaFS_source_path)) {
                    //System.out.println("Remove OK actual");
                }

            }
            va.setVersionNumber(1);
            tmpl.setActualVersion(va);
            tmpl.setLastVersion(va);
            SWBPortal.getTemplateMgr().reloadTemplate(tmpl);
        }
        response.setRenderParameter("suri", id);
    }
}
