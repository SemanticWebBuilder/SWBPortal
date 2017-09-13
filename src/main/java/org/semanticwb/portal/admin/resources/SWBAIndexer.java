/*
 * SemanticWebBuilder es una plataforma para el desarrollo de portales y aplicaciones de integración,
 * colaboración y conocimiento, que gracias al uso de tecnología semántica puede generar contextos de
 * información alrededor de algún tema de interés o bien integrar información y aplicaciones de diferentes
 * fuentes, donde a la información se le asigna un significado, de forma que pueda ser interpretada y
 * procesada por personas y/o sistemas, es una creación original del Fondo de Información y Documentación
 * para la Industria INFOTEC, cuyo registro se encuentra actualmente en trámite.
 *
 * INFOTEC pone a su disposición la herramienta SemanticWebBuilder a través de su licenciamiento abierto al público ('open source'),
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
package org.semanticwb.portal.admin.resources;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.semanticwb.Logger;
import org.semanticwb.SWBPortal;
import org.semanticwb.SWBUtils;
import org.semanticwb.model.Resource;
import org.semanticwb.model.SWBContext;
import org.semanticwb.model.SWBModel;
import org.semanticwb.model.User;
import org.semanticwb.model.WebSite;
import org.semanticwb.portal.api.GenericResource;
import org.semanticwb.portal.api.SWBActionResponse;
import org.semanticwb.portal.api.SWBParamRequest;
import org.semanticwb.portal.api.SWBResourceException;
import org.semanticwb.portal.api.SWBResourceModes;
import org.semanticwb.portal.api.SWBResourceURL;
import org.semanticwb.portal.indexer.SWBIndexer;

/**
 * Recurso de WB para la administracion de los Indexadores.
 * 
 * @author  Juan Antonio Fernandez Arias
 */
public class SWBAIndexer extends GenericResource {

    /** The log. */
    private Logger log = SWBUtils.getLogger(SWBAIndexer.class);
    
    /** The base. */
    private Resource base = null;

    /**
     * Creates a new instance of SWBAIndexer.
     */
    public SWBAIndexer() {
    }

    /* (non-Javadoc)
     * @see org.semanticwb.portal.api.GenericResource#doView(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.semanticwb.portal.api.SWBParamRequest)
     */
    @Override
    public void doView(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
        PrintWriter out = response.getWriter();
        User user = paramRequest.getUser();
        // Mostrar la lista de indexadores existentes
        HashMap<String, String> hmtmind = new HashMap<>();

        base = getResourceBase();
        String act = request.getParameter("act");
        String indexName = request.getParameter("indexName");
        if(null==indexName)indexName = SWBPortal.getIndexMgr().getDefaultIndexer().getName();
        if (null == act) {
            act = "";
        }

        SWBIndexer ind = SWBPortal.getIndexMgr().getDefaultIndexer();
        if ("".equals(act)&&ind.getIndexSize()==0) {
            
            if (null != indexName) {
                hmtmind = loadHMINDTM(indexName);
                
                out.println("<div id=\"" + base.getId() + "/statusIndex\" class=\"swbform\">");
                out.println("<form dojoType=\"dijit.form.Form\" id=\"" + getResourceBase().getId() + "/frmInd\" name=\"" + getResourceBase().getId() + "/frmInd\" action=\"\" method=\"post\">");
                out.println("<fieldset>");
                out.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\" width=\"100%\">");
                out.println("<tr>");
                out.println("<td>" + "Lista de sitios asociados al indexador" + " - " + indexName + "</td>");
                out.println("</tr>");
                out.println("<tr>");
                out.println("<td>");
                out.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\" width=\"100%\">");
                Iterator<WebSite> ittm = SWBContext.listWebSites();
                while (ittm.hasNext()) {
                    WebSite tmp = (WebSite) ittm.next();
                    if(SWBPortal.getAdminFilterMgr().haveAccessToSemanticObject(user,tmp.getSemanticObject()))
                    {

                        out.println("<tr><td>");
                        String tmid = tmp.getId();
                        String str_check = "";
                        if (null != hmtmind && hmtmind.get(tmid) != null) {
                            str_check = "checked";
                        }
                        out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"checkbox\" name=\"tmids\" value=\"" + tmid + "\" " + str_check + ">");
                        out.println(tmp.getDisplayTitle(user.getLanguage()));
                        out.println("</td>");
                        out.println("</tr>");
                        Iterator<SWBModel> it = tmp.listSubModels();
                        while (it.hasNext()) {
                            SWBModel sWBModel = it.next();
                            out.println("<tr><td>");
                            tmid = sWBModel.getId();
                            str_check = "";
                            if (null != hmtmind && hmtmind.get(tmid) != null) {
                                str_check = "checked";
                            }
                            out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-&gt;<input type=\"checkbox\" name=\"tmids\" value=\"" + tmid + "\" " + str_check + ">");
                            out.println(sWBModel.getSemanticObject().getDisplayName(user.getLanguage()));
                            out.println("</td>");
                            out.println("</tr>");
                        }
                    }
                }

                out.println("<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;");
                out.println("</td></tr>");
                out.println("<tr><td>");
                out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Reemplazar Indice" + ":");
                out.println("<input type=\"checkbox\" name=\"reemplazar\" value=\"replaceIndex\">");
                out.println("</td>");
                out.println("</tr>");
                out.println("<tr><td >");
                out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Indice bloqueado" + ":");
                out.println(ind.isLocked() ? "Si" : "No");
                out.println("</td>");
                out.println("</tr>");
                out.println("<tr><td>");
                out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Archivos por indexar" + ":");
                out.println(ind.getIndexSize());
                out.println("</td>");
                out.println("</tr>");
                out.println("</table>");
                out.println("</td>");
                out.println("</tr>");

                SWBResourceURL urlInd = paramRequest.getActionUrl();
                urlInd.setAction("indexar");
                SWBResourceURL urldel = paramRequest.getActionUrl();
                urldel.setAction("eliminar");
                SWBResourceURL urldes = paramRequest.getActionUrl();
                urldes.setAction("desbloquear");
                SWBResourceURL urlOpt = paramRequest.getActionUrl();
                urlOpt.setAction("optimizar");

                out.println("</table>");
                out.println("<input type=\"hidden\" name=\"indexID\" value=\"" + indexName + "\">");
                out.println("</fieldset>");
                out.println("<fieldset>");
                out.println("<button dojoType=\"dijit.form.Button\" onclick=\"enviar('" + getResourceBase().getId() + "/frmInd','" + urlInd + "'); return false;\">Indexar</button>");
                out.println("<button dojoType=\"dijit.form.Button\" onclick=\"enviar('" + getResourceBase().getId() + "/frmInd','" + urldel + "'); return false;\">Eliminar</button>");
                if (ind.isLocked()) {
                    out.println("<button dojoType=\"dijit.form.Button\" onclick=\"enviar('" + getResourceBase().getId() + "/frmInd','" + urldes + "'); return false;\">Desbloquear</button>");
                }
                out.println("<button dojoType=\"dijit.form.Button\" onclick=\"enviar('" + getResourceBase().getId() + "/frmInd','" + urlOpt + "'); return false;\">Optimizar</button>");
                out.println("</fieldset>");
                out.println("</form>");
                out.println("</div>");
            }
        }
        else if(ind.getIndexSize()>0)
        {
            StringBuilder status_msg = new StringBuilder();
            SWBResourceURL url1 = paramRequest.getRenderUrl();
            status_msg.append("\n<div id=\"" + base.getId() + "/statusIndex\" class=\"swbform\">");
            status_msg.append("\n<fieldset>");
            status_msg.append("\n<label for=\"indice\">"+indexName+",  - indexando. Archivos faltantes:</label>");
            status_msg.append("\n<input type=\"text\" name=\"" + base.getId() + "/indice\" value=\""+ind.getIndexSize()+"\" dojoType=\"dijit.form.TextBox\" trim=\"true\" id=\"" + base.getId() + "/indice\"  />");
            status_msg.append("\n</fieldset>");
            status_msg.append("\n</div>");
            out.println(status_msg.toString());
            SWBResourceURL url = paramRequest.getRenderUrl();
            url.setMode("doStatus");
            url.setCallMethod(SWBResourceURL.Call_DIRECT);
            url.setWindowState(SWBResourceModes.WinState_MAXIMIZED);
            out.println("<script type=\"text/javascript\">");
            out.println("  indexcheck('"+url+"','" + base.getId() + "/indice');");
            out.println("   sleep(1000);");
            out.println("   submitUrl('"+url1+"',document.getElementById('"+base.getId() + "/statusIndex'));");
            out.println("</script>");
        }

        

    }

    /**
     * Do status.
     * 
     * @param request the request
     * @param response the response
     * @param paramRequest the param request
     * @throws SWBResourceException the sWB resource exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void doStatus(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
        PrintWriter out = response.getWriter();
        SWBIndexer ind = SWBPortal.getIndexMgr().getDefaultIndexer();
        base = getResourceBase();
        String indexName = ind.getName();
        if (null != indexName) {
            StringBuilder status_msg = new StringBuilder();
            status_msg.append(ind.getIndexSize());
            out.println(status_msg.toString());
        }

    }

     /**
      * Load hmindtm.
      * 
      * @param indexName the index name
      * @return the hash map
      * @return
      */
     public HashMap<String, String> loadHMINDTM(String indexName) {
        base = getResourceBase();
        HashMap<String, String> hm_ret = new HashMap<>();
        String ind_tms = base.getAttribute("index" + indexName, "");

        log.debug("loadHMINDTM: " + ind_tms);

        if (!"".equals(ind_tms)) {
            StringTokenizer str_token = new StringTokenizer(ind_tms, "|");
            while (str_token.hasMoreTokens()) {
                String token = str_token.nextToken();
                hm_ret.put(token, token);
            }

        }
        return hm_ret;
    }

    /* (non-Javadoc)
     * @see org.semanticwb.portal.api.GenericResource#processAction(javax.servlet.http.HttpServletRequest, org.semanticwb.portal.api.SWBActionResponse)
     */
    @Override
    public void processAction(HttpServletRequest request, SWBActionResponse response) throws SWBResourceException, IOException {
        base = getResourceBase();
        String act = response.getAction();

        if (null == act) {
            act = "";
        }
        String newInd = request.getParameter("reemplazar");
        String indexName = request.getParameter("indexID");
        indexName = SWBPortal.getIndexMgr().getDefaultIndexer().getName();
        HashMap<String, String> hmtmind = null;
        if (null != indexName) {
            hmtmind = loadHMINDTM(indexName);
        }

        if ("indexar".equals(act)) {
            if (null != newInd && "replaceIndex".equals(newInd)) {
                SWBIndexer tmindexer = SWBPortal.getIndexMgr().getDefaultIndexer();
                tmindexer.reset();
                hmtmind = new HashMap<>();
            }

            String[] tmarr = request.getParameterValues("tmids");
            if (null != tmarr) {
                SWBIndexer tmindexer = SWBPortal.getIndexMgr().getDefaultIndexer();
                String tmids = "";
                for (int i = 0; i < tmarr.length; i++) {
                    String tmid = tmarr[i];
                    if (null != hmtmind && hmtmind.get(tmid) == null) {
                        tmindexer.indexModel(tmid);
                    }
                    if (null != hmtmind && hmtmind.get(tmid) != null) {
                        hmtmind.remove(tmid);
                    }
                    log.debug("ws:" + tmid + ", cadena: " + tmids);
                    tmids += tmid;
                    if (i < tmarr.length) {
                        tmids += "|";
                    }
                }
                if (null != hmtmind && !hmtmind.isEmpty()) // QUITANDO TM DEL INDICE
                {
                    Iterator<String> ite = hmtmind.keySet().iterator();
                    while (ite.hasNext()) {
                        String tm_del = ite.next();
                        tmindexer.removeModel(tm_del);
                    }
                }
                log.debug("cadena: " + tmids + ", name: " + indexName);
                base.setAttribute("index" + indexName, tmids);
            } else {
                base.removeAttribute("index" + indexName);
            }
            response.setRenderParameter("act", "");
            response.setRenderParameter("status","go");

        }

        if ("eliminar".equals(act)) {
            if (null != indexName && "".equals(indexName)) {
                SWBIndexer tmindexer = SWBPortal.getIndexMgr().getDefaultIndexer();
                tmindexer.remove();  // eliminar indice
            }
            base.removeAttribute("index" + indexName);
            response.setRenderParameter("act", "");
        }

        if ("desbloquear".equals(act)) {
            if (null != indexName && "".equals(indexName)) {
                SWBIndexer tmindexer = SWBPortal.getIndexMgr().getDefaultIndexer();
                if (tmindexer.isLocked()) {
                    tmindexer.unLock();  // desbloquear indice
                }
            }
            response.setRenderParameter("act", "");
        }

        if ("optimizar".equals(act)) {
            if (null != indexName && "".equals(indexName)) {
                SWBIndexer tmindexer = SWBPortal.getIndexMgr().getDefaultIndexer();
                tmindexer.optimize();
            }
            response.setRenderParameter("act", "");
        }
        if (null != indexName) {
            response.setRenderParameter("indexName", indexName);
        }

        try {
            base.updateAttributesToDB();
        } catch (Exception e) {
            log.error("Error al actualizar datos del recurso SWBAIndexer.", e);
        }

    }




    /* (non-Javadoc)
     * @see org.semanticwb.portal.api.GenericResource#processRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.semanticwb.portal.api.SWBParamRequest)
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
        if(paramRequest.getMode().equals("doStatus"))
        {
            doStatus(request,response,paramRequest);
        }
        else
        {
            super.processRequest(request, response, paramRequest);
        }
    }
}
