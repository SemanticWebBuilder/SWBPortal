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
package org.semanticwb.portal.resources;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBPortal;
import org.semanticwb.model.AdminFilter;
import org.semanticwb.model.GenericIterator;
import org.semanticwb.model.SWBContext;
import org.semanticwb.model.User;
import org.semanticwb.model.UserGroup;
import org.semanticwb.model.WebPage;
import org.semanticwb.portal.api.GenericResource;
import org.semanticwb.portal.api.SWBParamRequest;
import org.semanticwb.portal.api.SWBResourceException;
import org.semanticwb.portal.api.SWBResourceURL;

/**
 * Presenta la interface del administrador de documentos del servidor, con la que los usuarios
 * con los permisos adecuados, pueden gestionar los archivos contenidos en la instancia de SWB.
 * 
 * Displays the interface for the server documents administrator, with which users who have the 
 * proper permissions, can manage files within SWB's instance
 * @author jose.jimenez
 */
public class ServerDocumentsManager extends GenericResource {
    
    private UserGroup superUserGroup = null;
    private UserGroup adminUserGroup = null;

    @Override
    public void doView(HttpServletRequest request, HttpServletResponse response,
            SWBParamRequest paramRequest) throws SWBResourceException, IOException {
        
        PrintWriter out = response.getWriter();
        StringBuilder htmlCode = new StringBuilder(256);
        String showInterfacePath = paramRequest.getRenderUrl().setMode("showInter").
                setCallMethod(SWBResourceURL.Call_DIRECT).toString();
        
        User user = paramRequest.getUser();
        if (this.validateUser(user, paramRequest.getAdminTopic())) {
            htmlCode.append("<iframe id=\"srvrdcmts");
            htmlCode.append(this.getResourceBase().getId());
            htmlCode.append("\" width=\"100%\" height=\"70%\" dojoType=\"dijit.layout.ContentPane\" src=\"");
            htmlCode.append(showInterfacePath);
            htmlCode.append("\" frameborder=\"0\"></iframe>");
        } else {
            htmlCode.append(paramRequest.getLocaleString("errorNoAccess"));
        }
        out.println(htmlCode.toString());
        out.flush();
    }

    public void doShowInterface(HttpServletRequest request, HttpServletResponse response,
            SWBParamRequest paramRequest) throws SWBResourceException, IOException {
        
        PrintWriter out = response.getWriter();
        StringBuilder htmlCode = new StringBuilder(512);
        String jsBasePath = SWBPlatform.getContextPath() + "/swbadmin/js/elfinder/";
        
        htmlCode.append("<!DOCTYPE html>\n");
        htmlCode.append("<html>\n");
        htmlCode.append("  <head>\n");
        htmlCode.append("    <meta charset=\"utf-8\">\n");
        htmlCode.append("    <title>Documentos del servidor</title>\n");
        htmlCode.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=2\" />\n");
//        htmlCode.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\"//ajax.googleapis.com/ajax/libs/jqueryui/1.11.4/themes/smoothness/jquery-ui.css\">\n");
//        htmlCode.append("    <script src=\"//ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js\"></script>\n");
//        htmlCode.append("    <script src=\"//ajax.googleapis.com/ajax/libs/jqueryui/1.11.4/jquery-ui.min.js\"></script>\n");
        htmlCode.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\"");
        htmlCode.append(jsBasePath);
        htmlCode.append("css/jquery-ui.css\">\n");
        htmlCode.append("    <script src=\"");
        htmlCode.append(jsBasePath);
        htmlCode.append("js/jquery.min.js\"></script>\n");
        htmlCode.append("    <script src=\"");
        htmlCode.append(jsBasePath);
        htmlCode.append("js/jquery-ui.min.js\"></script>\n");
        htmlCode.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\"");
        htmlCode.append(jsBasePath);
        htmlCode.append("css/elfinder.min.css\">\n");
        htmlCode.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\"");
        htmlCode.append(jsBasePath);
        htmlCode.append("css/theme.css\">\n");
        htmlCode.append("    <script src=\"");
        htmlCode.append(jsBasePath);
        htmlCode.append("js/elfinder.min.js\"></script>\n");
        htmlCode.append("    <script type=\"text/javascript\" charset=\"utf-8\">\n");
        htmlCode.append("      $(document).ready(function() {\n");
        htmlCode.append("        $('#elfinder').elfinder({\n");
        htmlCode.append("          url : '");
        htmlCode.append(SWBPlatform.getContextPath());
        htmlCode.append("/elFinderConnector',  // connector URL (REQUIRED)\n");
        htmlCode.append("          resourceId : '");
        htmlCode.append(this.getResourceBase().getId());
        htmlCode.append("',\n");
        htmlCode.append("          resourcePath : '");
        htmlCode.append(this.getResourceBase().getWorkPath());
        htmlCode.append("',\n");
        htmlCode.append("          customData : {\n");
        htmlCode.append("              URI : '");
        htmlCode.append(paramRequest.getAdminTopic().getURI());
        htmlCode.append("'\n");
        htmlCode.append("          }\n");
        htmlCode.append("        });\n");
        htmlCode.append("      });\n");
        htmlCode.append("    </script>\n");
        htmlCode.append("  </head>\n");
        htmlCode.append("  <body>\n");
        htmlCode.append("    <div id=\"elfinder\"></div>\n");
        htmlCode.append("  </body>\n");
        htmlCode.append("</html>\n");
        out.println(htmlCode.toString());
        out.flush();

    }
    
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response,
            SWBParamRequest paramRequest) throws SWBResourceException, IOException {
        
        String mode = paramRequest.getMode();
        if (mode.equalsIgnoreCase("showInter")) {
            doShowInterface(request, response, paramRequest);
        } else {
            super.processRequest(request, response, paramRequest);
        }
        
    }
    
    /**
     * Permite el uso del recurso a los usuarios que tengan asignados el grupo de superUsuario
     * o el grupo de administradores y que ademas tengan un filtro que explicitamente asigne permisos
     * a la seccion de documentos del servidor.
     * @param user el usuario a validar
     * @param webPage la seccion desde donde se esta ejecutando este recurso
     * @return un boolean indicando si el usuario cumple con los criterios para ejecutar este recurso
     */
    private boolean validateUser(User user, WebPage webPage) {
        
        boolean userHasAccess = false;
        boolean superUserGroupKnown = false;
        
        if (null == this.superUserGroup) {
            
            this.superUserGroup = SWBContext.getAdminRepository().getUserGroup("su");
            this.adminUserGroup = SWBContext.getAdminRepository().getUserGroup("admin");
            if (null != this.superUserGroup) {
                superUserGroupKnown = true;
            }
        } else {
            superUserGroupKnown = true;
        }
        
        if (superUserGroupKnown && user.hasUserGroup(this.superUserGroup)) {
            userHasAccess = true;
        } else if (superUserGroupKnown && user.hasUserGroup(this.adminUserGroup)) {
            GenericIterator<AdminFilter> filterList = user.listAdminFilters();
            if (null != filterList && filterList.hasNext()) {
                while (filterList.hasNext()) {
                    AdminFilter filter = filterList.next();
                    userHasAccess = filter.haveAccessToWebPage(webPage);
                    if (userHasAccess) {
                        break;
                    }
                }
            }
        }
        return userHasAccess;
    }
}
