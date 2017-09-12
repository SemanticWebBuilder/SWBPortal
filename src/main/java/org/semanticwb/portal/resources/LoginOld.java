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
package org.semanticwb.portal.resources;

import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.transform.Templates;

import org.semanticwb.Logger;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBPortal;
import org.semanticwb.SWBUtils;
import org.semanticwb.model.Resource;
import org.semanticwb.model.SWBContext;
import org.semanticwb.model.WebSite;
import org.semanticwb.portal.api.GenericAdmResource;
import org.semanticwb.portal.api.SWBResourceException;

/**
 * Recurso que permite que el usuario final se firme en el sitio. Utilizando un
 * archivo xslt para mostrar la forma para pedir usuario y contraseña.
 * <p/>
 * Resource that allows that the end user signs itself in the site. Using
 * file xslt to show the form to request user and password.
 * <p/>
 * <p/>
 * User: Sergio Martínez
 * Date: 14/10/2004
 * Time: 06:04:00 PM
 */
public class LoginOld extends GenericAdmResource
{
    
    /** The log. */
    private static final Logger log = SWBUtils.getLogger(LoginOld.class);
    
    /** The global. */
    boolean global = false;
    //LoginInterface li = null;
    /** The list. */
    Hashtable list = null;
    
    /** The templates. */
    Templates templates = null;
    
    /** The base. */
    Resource base = null;
    
    /** The path. */
    String path = SWBPlatform.getContextPath() + "swbadmin/xsl/Login/";
    
    /** The redirect. */
    String redirect = SWBPlatform.getContextPath();
    
    /** The msg. */
    String msg = null;
    
    /** The flag. */
    boolean flag = false;

    /**
     * Initializes the Login resource loading a set of classes and xslt templates.
     * 
     * @throws AFException if something goes wrong
     * @throws SWBResourceException the sWB resource exception
     */
    public void init() throws SWBResourceException {
        // super.init();
        base = super.getResourceBase();
        log.debug("base.getRecResource().getTopicMapId(): " + base.getWebSiteId());
        if (base.getWebSiteId().equals("WBAGlobal")) {
            global = true;
            Iterator <WebSite> itWs = SWBContext.listWebSites();
            list = new Hashtable();
            while (itWs.hasNext()) {
                WebSite ws = itWs.next();
                try {
                    ClassLoader loader = SWBPortal.getResourceMgr().getResourceLoader(getClass().getName());
                    //TODO:VER 4.0 Class cls = loader.loadClass(DBUser.getInstance(tm.getDbdata().getRepository()).getProperty("loginInterface", ""));
                    //TODO:VER 4.0 list.put(ws.getId(), cls.newInstance());
                }
                catch (Exception e) {
                    log.error("Exception Initiating a Global Login resource " + base.getId(), e);
                }
            }
        } else {
            WebSite ws=base.getWebSite();
            try {
                ClassLoader loader = SWBPortal.getResourceMgr().getResourceLoader(getClass().getName());
                //TODO:VER Class cls = loader.loadClass(DBUser.getInstance(tm.getDbdata().getRepository()).getProperty("loginInterface", ""));
                //TODO:VER li = (LoginInterface) cls.newInstance();
            }
            catch (Exception e) {
               log.error("Exception Initiating a Login resource " + base.getId(), e);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.semanticwb.portal.api.GenericAdmResource#setResourceBase(org.semanticwb.model.Resource)
     */
    public void setResourceBase(Resource base) {
        try {
            super.setResourceBase(base);
        }
        catch (Exception e) {
            log.error("Error while setting resource base: " + base.getId() + "-" + base.getTitle(), e);
        }
        if (!"".equals(base.getAttribute("rpath", "").trim()))
            redirect = base.getAttribute("rpath", "").trim();
        msg = base.getAttribute("alert", "");
        try {
            if ("true".equalsIgnoreCase(base.getAttribute("autoval", "false"))) flag = true;
            else flag = false;
        } catch (Exception ignore) {
        }
        if (!"".equals(base.getAttribute("XSLT", "").trim())) {
            try {
                templates = SWBUtils.XML.loadTemplateXSLT(SWBPortal.getFileFromWorkPath(base.getWorkPath() + "/" + base.getAttribute("XSLT").trim()));
                path = SWBPortal.getWebWorkPath() + base.getWorkPath() + "/";
            }
            catch (Exception e) {
                log.error("Error while loading resource template: " + base.getId(), e);
            }
        }
        if (templates == null) {
            try {
                templates = SWBUtils.XML.loadTemplateXSLT(SWBPortal.getAdminFileStream("/swbadmin/xsl/Login/Login.xslt"));
            }
            catch (Exception e) {
                log.error("Error while loading default resource template: " + base.getId(), e);
            }
        }
    }
}
