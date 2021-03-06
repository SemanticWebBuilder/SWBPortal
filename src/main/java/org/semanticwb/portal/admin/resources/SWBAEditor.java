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
package org.semanticwb.portal.admin.resources;

import java.io.*;
import javax.servlet.http.*;
import javax.servlet.*;
import org.w3c.dom.*;
import java.util.*;
import org.semanticwb.Logger;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBPortal;
import org.semanticwb.SWBUtils;
import org.semanticwb.base.util.URLEncoder;
import org.semanticwb.model.ResourceSubType;
import org.semanticwb.model.ResourceType;
import org.semanticwb.model.SWBComparator;
import org.semanticwb.model.SWBContext;
import org.semanticwb.model.Template;
import org.semanticwb.model.User;
import org.semanticwb.model.VersionInfo;
import org.semanticwb.model.WebSite;
import org.semanticwb.portal.api.GenericResource;
import org.semanticwb.portal.api.SWBParamRequest;
import org.semanticwb.portal.api.SWBResourceException;

// TODO: Auto-generated Javadoc
/** Recurso de administración que permite la edición de contenidos y plantillas en
 * la administración de WebBuilder.
 *
 * Resource of administration that allows the edition of contents and templates
 * in the administration of WebBuilder.
 * @author Javier Solis Gonzalez
 */
public class SWBAEditor extends GenericResource
{

    /** The log. */
    private static Logger log = SWBUtils.getLogger(SWBAEditor.class);

    /**
     * The Class ResCatComp.
     */
    class ResCatComp implements Comparator
    {

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object o1, Object o2)
        {
            if (o1 instanceof ResourceType && o2 instanceof ResourceType)
            {
                ResourceType r1 = (ResourceType) o1;
                ResourceType r2 = (ResourceType) o2;
                return r1.getTitle().toLowerCase().compareTo(r2.getTitle().toLowerCase());
            }
            return 0;
        }
    }

    /**
     * The Class SubResCatComp.
     */
    class SubResCatComp implements Comparator
    {

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object o1, Object o2)
        {
            if (o1 instanceof ResourceSubType && o2 instanceof ResourceSubType)
            {
                ResourceSubType r1 = (ResourceSubType) o1;
                ResourceSubType r2 = (ResourceSubType) o2;
                return r1.getTitle().toLowerCase().compareTo(r2.getTitle().toLowerCase());
            }
            return 0;
        }
    }

    /**
     * Creates a new instance of WBTree.
     */
    public SWBAEditor()
    {
    }

    /**
     * Process request.
     * 
     * @param request the request
     * @param response the response
     * @param paramsRequest the params request
     * @throws SWBResourceException the sWB resource exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramsRequest) throws SWBResourceException, IOException
    {
        if (request.getHeader("FTP") != null)
        {
            SWBAFTP.processRequestFTP(request, response, paramsRequest);
        }
        else
        {
            if (paramsRequest.getMode().equals("gateway"))
            {
                doGateway(request, response, paramsRequest);
            }
            else if (paramsRequest.getMode().equals("upload"))
            {
                doUpload(request, response, paramsRequest);
            }
            else
            {
                super.processRequest(request, response, paramsRequest);
            }
        }

    }

    /**
     * Sort iterator.
     * 
     * @param it the it
     * @return the iterator
     * @return
     */
    public Iterator sortIterator(Iterator it)
    {
        return SWBComparator.sortSemanticObjects(it);
    }

    /**
     * Gets the resource type cat.
     * 
     * @param res the res
     * @param tm the tm
     * @return the resource type cat
     */
    public void getResourceTypeCat(Element res, String tm)
    {

        WebSite map = SWBContext.getWebSite(tm);
        WebSite tmInit = map;
        //TODO:listar globales
        Iterator elements = sortIterator(map.listResourceTypes());
        while (elements.hasNext())
        {
            ResourceType obj = (ResourceType) elements.next();
            if (obj.getResourceMode() == 2 || obj.getResourceMode() == 3)
            {
                Element erole = addNode("resourceType", obj.getId(), obj.getTitle(), res);
                erole.setAttribute("topicmap", map.getId());
                erole.setAttribute("topicmapname", map.getTitle());
                //TODO:listar globales
                Iterator itsub = sortIterator(obj.listSubTypes());

                while (itsub.hasNext())
                {
                    ResourceSubType sub = (ResourceSubType) itsub.next();
                    Element esubType = addNode("subResourceType", "" + sub.getId(), sub.getTitle(), erole);
                    esubType.setAttribute("topicmap", sub.getWebSite().getId());
                }
            }
        }
    }

    /**
     * Gets the service.
     * 
     * @param paramsRequest the params request
     * @param cmd the cmd
     * @param src the src
     * @param user the user
     * @param request the request
     * @param response the response
     * @return the service
     */
    private Document getService(SWBParamRequest paramsRequest, String cmd, Document src, User user, HttpServletRequest request, HttpServletResponse response)
    {
        String type = request.getHeader("TYPE");
        String tm = request.getHeader("TM");
        String id = request.getHeader("ID");
        String sver = request.getHeader("VER");
        int ver = 0;
        if (sver == null)
        {
            sver = request.getParameter("version");
        }

        String webpath = null;
        String workpath = null;
        String filename = null;
        if ("Template".equalsIgnoreCase(type))
        {
            Template template = SWBPortal.getTemplateMgr().getTemplateImp(SWBContext.getWebSite(tm).getTemplate(id));
            String templatepath = template.getWorkPath();
            templatepath = templatepath.substring(SWBUtils.getApplicationPath().length());
            if (templatepath.startsWith("/work/"))
            {
                templatepath = templatepath.substring(5);
            }
            webpath = SWBPortal.getWebWorkPath() + templatepath;
            workpath = template.getWorkPath();
            if (sver == null)
            {
                ver = template.getLastVersion().getVersionNumber();
            }
            else
            {
                ver = Integer.parseInt(sver);
            }
            filename = template.getFileName(ver);
        }
        else if ("LocalContent".equalsIgnoreCase(type))
        {
            //TODO
            /*
            Resource res=ResourceMgr.getInstance().getResource(tm,id).getResourceBase();
            webpath=WBUtils.getInstance().getWebWorkPath()+res.getResourceWorkPath();
            workpath=WBUtils.getInstance().getWorkPath()+res.getResourceWorkPath();
            if(sver==null)ver=res.getLastversion();
            else ver=Integer.parseInt(sver);     
            filename=res.getAttribute("url"+ver);
             */
        }

        if (cmd.equals("init"))
        {
            return init(user, src, ver);
        }
        if (request.getHeader("FTP") != null)
        {
            return SWBAFTP.getDocument(user, src, cmd, id);
        }
        else if (cmd.equals("getResourceTypeCat"))
        {
            try
            {
                Document doc = SWBUtils.XML.getNewDocument();
                Element res = doc.createElement("res");
                doc.appendChild(res);
                if (src.getElementsByTagName("tm").getLength() > 0)
                {
                    getResourceTypeCat(res, src.getElementsByTagName("tm").item(0).getFirstChild().getNodeValue());
                    return doc;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace(System.out);
                log.error(e);
            }
            return null;
        }
        else if (cmd.equals("initFiles"))
        {

            return initFiles(paramsRequest, user, src, workpath, webpath, filename, ver);
        }
        else
        {
            return getDocument(paramsRequest, user, src, cmd, workpath, webpath, filename, ver);
        }
    }

    /**
     * Inits the.
     * 
     * @param user the user
     * @param src the src
     * @param ver the ver
     * @return the document
     * @return
     */
    public Document init(User user, Document src, int ver)
    {
        Document dom = null;
        try
        {
            WebSite tma = SWBContext.getAdminWebSite();

            dom = SWBUtils.XML.getNewDocument();
            Element res = dom.createElement("res");
            dom.appendChild(res);
            //config
            Element config = addNode("config", "config", "Config", res);

            Element icons = addNode("icons", "icons", "Icons", config);
            Element icon = addNode("icon", "titulo", "Titulo", icons);
            icon.setAttribute("path", "images/ico_titulo.gif");
            icon = addNode("icon", "idioma", "Idioma", icons);
            icon.setAttribute("path", "images/ico_idioma.gif");
            icon.setAttribute("text", "ES");

            Element events = addNode("events", "events", "Events", config);
            Element unload = addNode("unload", "unload", "unload", events);
            unload.setAttribute("action", "showurl=" + tma.getWebPage("WBAd_infoi_TopicTitles").getUrl() + "?act=treeReload&status=true");
            unload.setAttribute("target", "status");

        }
        catch (Exception e)
        {
            log.error(e);
            return getError(3);
        }
        return dom;
    }

    /**
     * Inits the files.
     * 
     * @param paramsRequest the params request
     * @param user the user
     * @param src the src
     * @param workpath the workpath
     * @param webpath the webpath
     * @param filename the filename
     * @param ver the ver
     * @return the document
     * @return
     */
    public Document initFiles(SWBParamRequest paramsRequest, User user, Document src, String workpath, String webpath, String filename, int ver)
    {
        Document dom = null;
        try
        {
            WebSite tma = SWBContext.getAdminWebSite();

            dom = SWBUtils.XML.getNewDocument();
            Element res = dom.createElement("res");
            dom.appendChild(res);
            //config
            Element config = addNode("config", "config", "Config", res);
            config.setAttribute("docPath", webpath + "/" + ver + "/");

            Element icons = addNode("icons", "icons", "Icons", config);
            Element icon = addNode("icon", "folderO", "Folder Open", icons);
            icon.setAttribute("path", "images/f_abierto.gif");
            icon = addNode("icon", "folderC", "Folder Closed", icons);
            icon.setAttribute("path", "images/f_general.gif");
            icon = addNode("icon", "file", "File", icons);
            icon.setAttribute("path", "images/i_documento.gif");

            getFiles(paramsRequest, res, workpath, webpath, filename, ver);

        }
        catch (Exception e)
        {
            log.error(e);
            return getError(3);
        }
        return dom;
    }

    /**
     * Gets the files.
     * 
     * @param paramsRequest the params request
     * @param res the res
     * @param workpath the workpath
     * @param webpath the webpath
     * @param filename the filename
     * @param ver the ver
     * @return the files
     * @throws SWBResourceException the sWB resource exception
     */
    public void getFiles(SWBParamRequest paramsRequest, Element res, String workpath, String webpath, String filename, int ver) throws SWBResourceException
    {
        WebSite tma = SWBContext.getAdminWebSite();
        Element menu = null;
        Element option = null;
        Element events = null;
        Element event = null;

        //tree nodes
        Element root = addNode("node", "", "Files", res);
        root.setAttribute("reload", "getFiles");;
        root.setAttribute("type", "DIR");

        menu = addNode("menu", "menu", "Menu", root);

        option = addNode("option", "add", paramsRequest.getLocaleString("addFile"), menu);
        option.setAttribute("action", "addFile");
        option.setAttribute("shortCut", "INSERT");
        addNode("separator", "-", "-", menu);

        option = addNode("option", "refresh", paramsRequest.getLocaleString("refresh"), menu);
        option.setAttribute("shortCut", "F5");
        option.setAttribute("action", "reload");


        File dir = new File(workpath + "/" + ver + "/");
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        File files[] = dir.listFiles();
        for (int x = 0; x < files.length; x++)
        {
            File file = files[x];
            if (file.isDirectory())
            {
                getDir(paramsRequest, root, file, workpath, webpath, ver);
            }
            else if (file.isFile())
            {
                if (!file.getName().equals(filename))
                {
                    getFile(paramsRequest, root, file, workpath, webpath, ver);
                }
            }
        }
    }

    /**
     * Gets the file.
     * 
     * @param paramsRequest the params request
     * @param root the root
     * @param file the file
     * @param workpath the workpath
     * @param webpath the webpath
     * @param ver the ver
     * @return the file
     * @throws SWBResourceException the sWB resource exception
     */
    public void getFile(SWBParamRequest paramsRequest, Element root, File file, String workpath, String webpath, int ver) throws SWBResourceException
    {
        WebSite tma = SWBContext.getAdminWebSite();
        Element menu = null;
        Element option = null;

        File docDir = new File(workpath + "/" + ver + "/");
        String id = file.getPath().substring(docDir.getPath().length() + 1);
        id = id.replace('\\', '/');
        String path = webpath + "/" + ver + "/" + id;
        Element ele = addNode("node", id, file.getName(), root);
        ele.setAttribute("path", path);
        ele.setAttribute("reload", "getFile." + id);
        ele.setAttribute("type", "FILE");
        ele.setAttribute("size", "" + file.length());
        Date dt = new Date(file.lastModified());
        ele.setAttribute("date", "" + dt.toLocaleString());
        ele.setAttribute("time", "" + dt.getHours() + ":" + dt.getMinutes() + ":" + dt.getSeconds());

        //menu
        menu = addNode("menu", "menu", "Menu", ele);
        option = addNode("option", "add", paramsRequest.getLocaleString("addFile"), menu);
        option.setAttribute("action", "addFile");
        option.setAttribute("shortCut", "INSERT");
        addNode("separator", "-", "-", menu);
        option = addNode("option", "remove", paramsRequest.getLocaleString("removeFile"), menu);
        option.setAttribute("action", "removeFile." + id);
        option.setAttribute("shortCut", "DELETE");
        option.setAttribute("confirm", paramsRequest.getLocaleString("confirm"));
        addNode("separator", "-", "-", menu);
        option = addNode("option", "refresh", paramsRequest.getLocaleString("refresh"), menu);
        option.setAttribute("shortCut", "F5");
        option.setAttribute("action", "reload");

    }

    /**
     * Gets the dir.
     * 
     * @param paramsRequest the params request
     * @param root the root
     * @param dir the dir
     * @param workpath the workpath
     * @param webpath the webpath
     * @param ver the ver
     * @return the dir
     * @throws SWBResourceException the sWB resource exception
     */
    public void getDir(SWBParamRequest paramsRequest, Element root, File dir, String workpath, String webpath, int ver) throws SWBResourceException
    {
        WebSite tma = SWBContext.getAdminWebSite();
        Element menu = null;
        Element option = null;


        File docDir = new File(workpath + "/" + ver + "/");
        String id = dir.getPath().substring(docDir.getPath().length() + 1);
        id = id.replace('\\', '/');

        Element ele = addNode("node", id, dir.getName(), root);
        ele.setAttribute("reload", "getDir." + id);
        ele.setAttribute("type", "DIR");

        //menu
        menu = addNode("menu", "menu", "Menu", ele);
        option = addNode("option", "add", paramsRequest.getLocaleString("addFile"), menu);
        option.setAttribute("action", "addFile");
        option.setAttribute("shortCut", "INSERT");
        addNode("separator", "-", "-", menu);
        option = addNode("option", "refresh", paramsRequest.getLocaleString("refresh"), menu);
        option.setAttribute("shortCut", "F5");
        option.setAttribute("action", "reload");


        File files[] = dir.listFiles();
        for (int x = 0; x < files.length; x++)
        {
            File file = files[x];
            if (file.isDirectory())
            {
                getDir(paramsRequest, ele, file, workpath, webpath, ver);
            }
            else if (file.isFile())
            {
                getFile(paramsRequest, ele, file, workpath, webpath, ver);
            }
        }
    }

    /**
     * Gets the document.
     * 
     * @param paramsRequest the params request
     * @param user the user
     * @param src the src
     * @param act the act
     * @param workpath the workpath
     * @param webpath the webpath
     * @param filename the filename
     * @param ver the ver
     * @return the document
     * @return
     */
    public Document getDocument(SWBParamRequest paramsRequest, User user, Document src, String act, String workpath, String webpath, String filename, int ver)
    {
        Document dom = null;
        try
        {
            dom = SWBUtils.XML.getNewDocument();
            Element res = dom.createElement("res");
            dom.appendChild(res);

            String cmd = null;
            String id = null;
            int ind = act.indexOf('.');
            if (ind > 0)
            {
                cmd = act.substring(0, ind);
                id = act.substring(ind + 1);
            }
            else
            {
                cmd = act;
            }

            if (cmd.equals("getFiles"))
            {
                getFiles(paramsRequest, res, workpath, webpath, filename, ver);
            }
            else if (cmd.equals("getFile"))
            {
                File file = new File(workpath + "/" + ver + "/" + id);
                getFile(paramsRequest, res, file, workpath, webpath, ver);
            }
            else if (cmd.equals("getDir"))
            {
                File file = new File(workpath + "/" + ver + "/" + id);
                getDir(paramsRequest, res, file, workpath, webpath, ver);
            }
            else if (cmd.equals("removeFile"))
            {
                File file = new File(workpath + "/" + ver + "/" + id);
                if (file.delete())
                {
                }
                else
                {
                    return getError(3);
                }

            }
            else
            {
                return getError(2);
            }
        }
        catch (Exception e)
        {
            log.error(e);
            return getError(3);
        }
        return dom;
    }

    /**
     * Adds the node.
     * 
     * @param node the node
     * @param id the id
     * @param name the name
     * @param parent the parent
     * @return the element
     */
    private Element addNode(String node, String id, String name, Element parent)
    {
        Element ret = addElement(node, null, parent);
        if (id != null)
        {
            ret.setAttribute("id", id);
        }
        if (name != null)
        {
            ret.setAttribute("name", name);
        }
        return ret;
    }

    /**
     * Adds the element.
     * 
     * @param name the name
     * @param value the value
     * @param parent the parent
     * @return the element
     */
    private Element addElement(String name, String value, Element parent)
    {
        Document doc = parent.getOwnerDocument();
        Element ele = doc.createElement(name);
        if (value != null)
        {
            ele.appendChild(doc.createTextNode(value));
        }
        parent.appendChild(ele);
        return ele;
    }

    /**
     * Gets the error.
     * 
     * @param id the id
     * @return the error
     */
    private Document getError(int id)
    {
        Document dom = null;
        try
        {
            dom = SWBUtils.XML.getNewDocument();
            Element res = dom.createElement("res");
            dom.appendChild(res);
            Element err = dom.createElement("err");
            res.appendChild(err);
            addElement("id", "" + id, err);
            if (id == 0)
            {
                addElement("message", SWBUtils.TEXT.getLocaleString("locale_Gateway", "usrmsg_Gateway_getService_loginfail") + "...", err);
            }
            else if (id == 1)
            {
                addElement("message", SWBUtils.TEXT.getLocaleString("locale_Gateway", "usrmsg_Gateway_getService_nouser") + "...", err);
            }
            else if (id == 2)
            {
                addElement("message", SWBUtils.TEXT.getLocaleString("locale_Gateway", "usrmsg_Gateway_getService_noservice") + "...", err);
            }
            else if (id == 3)
            {
                addElement("message", SWBUtils.TEXT.getLocaleString("locale_Gateway", "usrmsg_Gateway_getService_serviceprocessfail") + "...", err);
            }
            else if (id == 4)
            {
                addElement("message", SWBUtils.TEXT.getLocaleString("locale_Gateway", "usrmsg_Gateway_getService_parametersprocessfail") + "...", err);
            }
            else if (id == 5)
            {
                addElement("message", SWBUtils.TEXT.getLocaleString("locale_Gateway", "usrmsg_Gateway_getService_noTopicmap") + "...", err);
            }
            else if (id == 6)
            {
                addElement("message", SWBUtils.TEXT.getLocaleString("locale_Gateway", "usrmsg_Gateway_getService_noTopic") + "...", err);
            }
            else if (id == 7)
            {
                addElement("message", SWBUtils.TEXT.getLocaleString("locale_Gateway", "usrmsg_Gateway_getService_usernopermiss") + "...", err);
            }
            else if (id == 8)
            {
                addElement("message", SWBUtils.TEXT.getLocaleString("locale_Gateway", "usrmsg_Gateway_getService_TopicAlreadyexist") + "...", err);
            }
            else if (id == 9)
            {
                addElement("message", SWBUtils.TEXT.getLocaleString("locale_Gateway", "usrmsg_Gateway_getService_byImplement") + "...", err);
            }
            else if (id == 10)
            {
                addElement("message", SWBUtils.TEXT.getLocaleString("locale_Gateway", "usrmsg_Gateway_getService_TopicMapAlreadyExist") + "...", err);
            }
            else if (id == 11)
            {
                addElement("message", SWBUtils.TEXT.getLocaleString("locale_Gateway", "usrmsg_Gateway_getService_FileNotFound") + "...", err);
            }
            else if (id == 12)
            {
                addElement("message", SWBUtils.TEXT.getLocaleString("locale_Gateway", "usrmsg_Gateway_getService_noversions") + "...", err);
            }
            else if (id == 13)
            {
                addElement("message", SWBUtils.TEXT.getLocaleString("locale_Gateway", "usrmsg_Gateway_getError_xmlinconsistencyversion") + "...", err);
            }
            else if (id == 14)
            {
                addElement("message", SWBUtils.TEXT.getLocaleString("locale_Gateway", "usrmsg_Gateway_getError_noResourcesinMemory") + "...", err);
            }
            else if (id == 15)
            {
                addElement("message", SWBUtils.TEXT.getLocaleString("locale_Gateway", "usrmsg_Gateway_getError_noTemplatesinMemory") + "...", err);
            }
            else if (id == 16)
            {
                addElement("message", SWBUtils.TEXT.getLocaleString("locale_Gateway", "usrmsg_Gateway_getError_TemplatenotRemovedfromFileSystem") + "...", err);
            }
            else if (id == 17)
            {
                addElement("message", SWBUtils.TEXT.getLocaleString("locale_Gateway", "usrmsg_Gateway_getError_adminUsernotCreated") + "...", err);
            }
            else
            {
                addElement("message", SWBUtils.TEXT.getLocaleString("locale_Gateway", "usrmsg_Gateway_getService_errornotfound") + "...", err);
            }
        }
        catch (Exception e)
        {
            log.error(SWBUtils.TEXT.getLocaleString("locale_Gateway", "error_Gateway_getService_documentError") + "...", e);
        }
        return dom;
    }

    /**
     * Do upload.
     *
     * @param request the request
     * @param response the response
     * @param paramRequest the param request
     * @throws SWBResourceException, a Resource Exception
     * @throws IOException, an In Out Exception
     * @throws SWBResourceException the sWB resource exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void doUploadFile(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException
    {
        String path = request.getHeader("PATHFILEWB");

        if (path != null)
        {
            try
            {
                File f = new File(path);
                if (!SWBAFTP.hasPermission(paramRequest.getUser(), f))
                {
                    return;
                }
                if (SWBAFTP.isProtected(f))
                {
                    return;
                }
                if (f.isDirectory())
                {
                    SWBAFTP.log("CREATED|DIR:\"" + f.getCanonicalPath() + "\"|USER:\"" + paramRequest.getUser().getLogin() + "_" + paramRequest.getUser().getUserRepository().getId() + "\"", request.getRemoteAddr());
                }
                else
                {
                    SWBAFTP.log("CREATED|FILE:\"" + f.getCanonicalPath() + "\"|USER:\"" + paramRequest.getUser().getLogin() + "_" + paramRequest.getUser().getUserRepository().getId() + "\"", request.getRemoteAddr());
                }

                FileOutputStream fout = new FileOutputStream(f);
                InputStream in = request.getInputStream();
                byte[] bcont = new byte[8192];
                int ret = in.read(bcont);
                while (ret != -1)
                {
                    fout.write(bcont, 0, ret);
                    ret = in.read(bcont);
                }
                in.close();
                fout.close();
            }
            catch (Exception e)
            {
                response.sendError(500, e.getMessage());
            }
        }
        else
        {
            response.sendError(500);
        }
    }

    /**
     * Do upload.
     * 
     * @param request the request
     * @param response the response
     * @param paramsRequest the params request
     * @throws SWBResourceException the sWB resource exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void doUpload(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramsRequest) throws SWBResourceException, IOException
    {
        if (request.getHeader("FTP") != null)
        {
            SWBAFTP.doUpload(request, response, paramsRequest);
            return;
        }
        PrintWriter out = response.getWriter();
        ServletInputStream in = request.getInputStream();

        try
        {
            String tm = request.getHeader("TM");
            String id = request.getHeader("ID");
            int ver = Integer.parseInt(request.getHeader("VER"));
            String type = request.getHeader("TYPE");

            String name = request.getHeader("PATHFILEWB");
            String doc = request.getHeader("DOCUMENT");
            String work = null;

            if (type.equalsIgnoreCase("Template"))
            {
                Template template = SWBPortal.getTemplateMgr().getTemplateImp(SWBContext.getWebSite(tm).getTemplate(id));
                work = template.getWorkPath() + "/" + ver + "/";

                if (request.getHeader("ATTACHWB") != null)
                {
                    work += "images/";
                }

                String ret = "";
                if (!("FINDATTACHES".equals(doc)))
                {
                    File fpath = new File(work);
                    fpath.mkdirs();
                    File file = new File(work + name);
                    
                    FileOutputStream fout=new FileOutputStream(file);
                    ByteArrayOutputStream bout=new ByteArrayOutputStream();
                    SWBUtils.IO.copyStream(in, bout);
                    String data=new String(bout.toByteArray(),"UTF-8");
                    fout.write(data.getBytes());
                    fout.flush();
                    fout.close();
                }
                else
                {
                    ret = SWBUtils.IO.readInputStream(in);
                }

                if (doc != null)
                {
                    if (doc.equals("RELOAD"))
                    {
                        template.setActive(template.isActive());
                        out.println("ok");
                    }
                    else if (doc.equals("REPLACE"))
                    {
                        VersionInfo verinfo = template.getLastVersion();
                        while (verinfo != null && verinfo.getVersionNumber() != ver)
                        {
                            verinfo = verinfo.getPreviousVersion();
                        }
                        if (verinfo != null)
                        {
                            verinfo.setVersionFile(name);
                        }
                        out.println("ok");
                    }
                    else if (doc.equals("FINDATTACHES"))
                    {
                        if (request.getHeader("CSSTYPE") != null && request.getHeader("CSSTYPE").equals("TRUE"))
                        {
                            // find css attaches
                            for (String file : SWBPortal.UTIL.findAttachesFromCss(ret))
                            {
                                out.print(file);
                                out.print("|");
                            }
                        }
                        else
                        {
                            out.print(SWBPortal.UTIL.FindAttaches(ret));
                            out.print("|");
                            out.print(SWBPortal.UTIL.parseHTML(ret, "images/"));
                        }
                        //System.out.print("TODO:SWBAeditor - FINDATTACHES");
                    }
                }

            }
            else if (type.equalsIgnoreCase("LocalContent"))
            {
                //TODO:
/*
                Resource res=ResourceMgr.getInstance().getResource(tm, id).getResourceBase();
                work=WBUtils.getInstance().getWorkPath()+res.getResourceWorkPath()+"/"+ver+"/";
                webWork=WBUtils.getInstance().getWebWorkPath()+res.getResourceWorkPath()+"/"+ver+"/";
                
                //System.out.println("work:"+work+" name:"+name);

                if(!("FINDATTACHES".equals(doc)))
                {
                File fpath=new File(work);
                fpath.mkdirs();
                File file=new File(work+name);
                fout=new FileOutputStream(file);
                }
                
                if(doc!=null)
                {
                String ret=writeFile(in, null);

                if(doc.equals("RELOAD"))
                {
                writeParseFile(fout,webWork,ret);
                res.getRecResource().update();
                DBAdmLog.getInstance().saveContentLog(paramsRequest.getUser().getId(),tm,tp,"update",id,"Content updated with id:"+ id + " "+", in topic:"+ tp);
                out.println("ok");
                }else if(doc.equals("REPLACE"))
                {
                writeParseFile(fout,webWork,ret);
                res.setAttribute("url"+ver,name);
                res.updateAttributesToDB();
                DBAdmLog.getInstance().saveContentLog(user.getId(), tm, tp, "update", res.getId(), "Content created width id:"+res.getId()+",and title:"+res.getTitle());
                out.println("ok");
                }else if(doc.equals("FINDATTACHES"))
                {
                out.print(WBUtils.getInstance().FindAttaches(ret));
                out.print("|");
                out.print(WBUtils.getInstance().parseHTML(ret,""));
                }
                }else
                {
                writeFile(in, fout);
                }
                 */
            }

        }
        catch (Exception e)
        {
            log.error(e);
        }
    }

    /**
     * Write file.
     * 
     * @param in the in
     * @param fout the fout
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @return
     */
    public String writeFile(InputStream in, File file) throws IOException
    {
        String str = "";
        try
        {
            str = SWBUtils.IO.readInputStream(in);
            if (str != null && str.length() > 0)
            {
                FileOutputStream fout = new FileOutputStream(file);
                fout.write(str.getBytes());
                fout.flush();
                fout.close();
            }
        }
        catch (Exception e)
        {
            log.error(e);
        }
        return str;
    }

    /**
     * Do gateway.
     * 
     * @param request the request
     * @param response the response
     * @param paramsRequest the params request
     * @throws SWBResourceException the sWB resource exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void doGateway(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramsRequest) throws SWBResourceException, IOException
    {
        PrintWriter out = response.getWriter();
        ServletInputStream in = request.getInputStream();
        Document dom = SWBUtils.XML.xmlToDom(in);
        if (dom == null || !dom.getFirstChild().getNodeName().equals("req"))
        {
            out.print(SWBUtils.XML.domToXml(getError(3)));
            return;
        }

        String cmd = null;
        if (dom.getElementsByTagName("cmd").getLength() > 0)
        {
            cmd = dom.getElementsByTagName("cmd").item(0).getFirstChild().getNodeValue();
        }

        if (cmd == null)
        {
            response.sendError(404, request.getRequestURI());
            return;
        }
        String ret;
        WebSite tm = SWBContext.getWebSite(request.getHeader("TM"));

        if (tm != null)
        {
            Document res = getService(paramsRequest, cmd, dom, paramsRequest.getUser(), request, response);
            if (res == null)
            {
                ret = SWBUtils.XML.domToXml(getError(3));
            }
            else
            {
                ret = SWBUtils.XML.domToXml(res, true);
            }
            out.print(new String(ret.getBytes()));
        }
        else
        {
            Document res = getService(paramsRequest, cmd, dom, paramsRequest.getUser(), request, response);
            if (res == null)
            {
                ret = SWBUtils.XML.domToXml(getError(3));
            }
            else
            {
                ret = SWBUtils.XML.domToXml(res, true);
            }
            out.print(new String(ret.getBytes()));
        }

    }

    /**
     * Do view.
     * 
     * @param request the request
     * @param response the response
     * @param paramsRequest the params request
     * @throws SWBResourceException the sWB resource exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void doView(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramsRequest) throws SWBResourceException, IOException
    {
        PrintWriter out = response.getWriter();
        getTemplateApplet(out, "sep", "1", 1, paramsRequest.getUser(), request.getSession().getId());
    }

    /**
     * Gets the template applet.
     * 
     * @param out the out
     * @param topicmapid the topicmapid
     * @param templateid the templateid
     * @param version the version
     * @param user the user
     * @param session the session
     * @return the template applet
     */
    public static void getTemplateApplet(PrintWriter out, String topicmapid, String templateid, int version, User user, String session)
    {
        out.println("<applet id=\"apptpleditor\" name=\"apptpleditor\" code=\"applets.htmleditor.TemplateEditor\" codebase=\"" + SWBPlatform.getContextPath() + "/\" archive=\"swbadmin/lib/SWBAplHtmlEditor.jar, swbadmin/lib/SWBAplCommons.jar,swbadmin/lib/SWBAplFtp.jar\" width=\"100%\" height=\"500\">");
        String url = SWBPortal.getDistributorPath() + "/SWBAdmin/WBAd_utl_HTMLEditor/_rid/1/_mto/3/_mod/";

        out.println("<param name=\"jsess\" value=\"" + session + "\">");
        out.println("<param name=\"upload\" value=\"" + url + "upload" + "\">");
        out.println("<param name=\"download\" value=\"" + url + "download" + "\">");
        out.println("<param name=\"gateway\" value=\"" + url + "gateway" + "\">");
        out.println("<param name=\"startview\" value=\"text\">");
        out.println("<param name=\"tm\" value=\"" + topicmapid + "\">");
        out.println("<param name=\"id\" value=\"" + templateid + "\">");

        Template tpl = SWBContext.getWebSite(topicmapid).getTemplate(templateid);
        out.println("<param name=\"document\" value=\"" + SWBPortal.getWebWorkPath() + tpl.getWorkPath() + "/" + version + "/" + URLEncoder.encode(tpl.getFileName(version)) + "\">");
        out.println("<param name=\"filename\" value=\"" + tpl.getFileName(version) + "\">");
        out.println("<param name=\"ver\" value=\"" + version + "\">");
        out.println("<param name=\"locale\" value=\"" + user.getLanguage() + "\">");
        out.println("<param name=\"type\" value=\"Template\">");

        out.println("<param name =\"ContextPath\" value=\"" + SWBPortal.getContextPath() + "\">");
        out.println("<param name =\"ApplicationPath\" value=\"" + SWBUtils.getApplicationPath()+ "\">");
        out.println("</applet>");
    }
}
