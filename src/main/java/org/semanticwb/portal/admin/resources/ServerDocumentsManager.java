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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.semanticwb.Logger;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBUtils;
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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Presenta la interfaz del administrador de documentos del servidor, con la que los usuarios
 * con los permisos adecuados, pueden gestionar los archivos contenidos en la instancia de SWB.
 *
 * Displays the interface for the server documents administrator, with which users who have the
 * proper permissions, can manage files within SWB's instance
 * @author jose.jimenez
 */
public class ServerDocumentsManager extends GenericResource {

    private UserGroup superUserGroup = null;
    private UserGroup adminUserGroup = null;
    
    /** The log. */
    private static Logger LOG = SWBUtils.getLogger(ServerDocumentsManager.class);
    

    @Override
    public void doView(HttpServletRequest request, HttpServletResponse response,
            SWBParamRequest paramRequest) throws SWBResourceException, IOException {

        PrintWriter out = response.getWriter();
        StringBuilder htmlCode = new StringBuilder(256);
        User user = paramRequest.getUser();
        String[] userValidation = this.validateUserAccess(user,
                paramRequest.getAdminTopic(), request).split(",");
        boolean addParamAttrib = (userValidation.length == 2 && !userValidation[1].isEmpty());
        String sessionAttrib = addParamAttrib ? userValidation[1] : "";
        String showInterfacePath = addParamAttrib
                ? paramRequest.getRenderUrl().setMode("showInter")
                        .setCallMethod(SWBResourceURL.Call_DIRECT)
                        .setParameter("attrib", sessionAttrib).toString()
                : paramRequest.getRenderUrl().setMode("showInter").
                    setCallMethod(SWBResourceURL.Call_DIRECT).toString();
        boolean isUserValid = Boolean.parseBoolean(userValidation[0]);
        
        if (isUserValid) {
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

    /**
     * Genera el codigo HTML necesario para mostrar la interfaz del administrador de documentos
     * contenidos en el contexto de la instancia de SWB.
     * 
     * Generates the HTML code that shows the documents administrator's interface
     * for the files stored in the instance of SWB.
     * @param request la peticion hecha por el usuario
     * @param response la respuesta generada para devolverla al usuario
     * @param paramRequest los datos asociados a la peticion del usuario, propios de SWB
     * @throws SWBResourceException en caso de encontrar algun problema con la ejecucion de objetos de SWB
     * @throws IOException en caso de haber un problema de entrada o salida de datos
     */
    public void doShowInterface(HttpServletRequest request, HttpServletResponse response,
            SWBParamRequest paramRequest) throws SWBResourceException, IOException {

        PrintWriter out = response.getWriter();
        StringBuilder htmlCode = new StringBuilder(512);
        String jsBasePath = SWBPlatform.getContextPath() + "/swbadmin/js/elfinder/";
        String attrib = (null != request.getParameter("attrib") && 
                !"".equals(request.getParameter("attrib")))
                ? request.getParameter("attrib") : null;

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
        
        if (null != attrib) {
            htmlCode.append("              attrib : '");
            htmlCode.append(attrib);  //atributo de sesion con rutas de directorios permitidos
            htmlCode.append("',\n");
        }
        
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
     * @param request la peticion del usuario, para agregar atributos de sesion, en su caso
     * @return un String con dos valores separados por coma, indicando si el usuario 
     *  cumple con los criterios para ejecutar este recurso, y si es así, el segundo valor
     *  corresponde al nombre del atributo de sesion que contiene las rutas permitidas al usuario
     */
    private String validateUserAccess(User user, WebPage webPage, HttpServletRequest request) {

        boolean userHasAccess = false;
        boolean superUserGroupKnown = false;
        String attribName = "";

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
                    //LOG.error(filter.getXml());
                    userHasAccess = filter.haveAccessToWebPage(webPage);
                    if (userHasAccess) {
                        ArrayList<String> paths = new ArrayList();
                        if (null != filter.getXml()) {
                             NodeList list = SWBUtils.XML.xmlToDom(filter.getXml())
                                    .getElementsByTagName("dirs");
                            if (list.getLength() > 0) {
                                Element elem = (Element) list.item(0);
                                if (elem.getChildNodes().getLength() > 0) {
                                    for (int i = 0; i < elem.getChildNodes().getLength(); i++) {
                                        Element path = (Element) elem.getChildNodes().item(i);
                                        String onePath = path.getAttribute("path");
                                        paths.add(onePath);
                                    }
                                }
                            }
                        }
                        if (!paths.isEmpty()) {
                            attribName = "userPaths_" + webPage.getId() + "_" + user.getId();
                            request.getSession(false).setAttribute(attribName, paths);
                        } else {
                            attribName = "";
                        }
                        break;
                    }
                }
            }
        }
        return userHasAccess + "," + attribName;
    }
}
