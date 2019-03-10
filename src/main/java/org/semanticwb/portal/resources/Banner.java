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
 * dirección electrónica: http://www.semanticwebbuilder.org.mx
 */
package org.semanticwb.portal.resources;

import org.semanticwb.*;
import org.semanticwb.model.Resource;
import org.semanticwb.model.ResourceType;
import org.semanticwb.model.User;
import org.semanticwb.model.WebPage;
import org.semanticwb.portal.api.SWBActionResponse;
import org.semanticwb.portal.api.SWBParamRequest;
import org.semanticwb.portal.api.SWBResourceException;
import org.semanticwb.portal.resources.base.BannerBase;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

/**
 * Componente para administrar un banner Web.
 * @author : Hasdai Pacheco
 */
public class Banner extends BannerBase {
    private static Logger LOG = SWBUtils.getLogger(Banner.class);

    @Override
    public void init() throws SWBResourceException {
        super.init();
        migrate(getResource().getResourceType());
    }

    @Override
    public void install(ResourceType recobj) throws SWBResourceException {
        super.install(recobj);
        copyResources(recobj);
    }

    @Override
    public void uninstall(ResourceType recobj) throws SWBResourceException {
        super.uninstall(recobj);

        //Remove resource assets
        File jsp = new File(getAssetsFolderPath(recobj));
        if (jsp.exists()) {
            SWBUtils.IO.removeDirectory(jsp.getAbsolutePath());
            LOG.info("Resource " + recobj.getTitle() +" assets removed");
        }
    }

    @Override
    public void processAction(HttpServletRequest request, SWBActionResponse response) throws SWBResourceException, IOException {
        //Record hit on resource
        Resource base = getResourceBase();
        base.addHit(request, response.getUser(), response.getWebPage());

        //Redirect to banner URL
        String href = replaceTags(getURL(), request, response.getUser(), response.getWebPage());

        if (null != href && !href.isEmpty()) {
            response.sendRedirect(href);
        }
    }

    @Override
    public void doView(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
        String jsp = getViewJSP();
        if (null == jsp || jsp.isEmpty()) {
            jsp = SWBPortal.getWebWorkPath() + "/models/" + paramRequest.getWebPage().getWebSiteId() + "/jsp/resources/banner/view.jsp";
        }

        RequestDispatcher rd = request.getRequestDispatcher(jsp);
        request.setAttribute("SWBParamRequest", paramRequest);
        request.setAttribute("banner", this);

        try {
            rd.include(request, response);
        } catch (ServletException sex) {
            LOG.error("org.semanticwb.portal.resources.Banner: Error including view.jsp", sex);
        }
    }

    /**
     * Gets the full Web path of the Banner's image.
     * @return Full Web path of image.
     */
    public String getImagePath() {
        return SWBPortal.getWebWorkPath() + getSemanticObject().getWorkPath() + "/" +
                Banner.swbres_bannerImage.getName() + "_" + getResourceBase().getId() + "_" + getImage();
    }

    /**
     * Migrates data in resource base to SemanticProperties of resource.
     * This method should be removed after migration to version 5.1.
     */
    private void migrate(ResourceType recobj) {
        Resource base = getResourceBase();
        String rid = base.getId();

        if (!"true".equals(base.getAttribute("migrated", "false"))) {
            LOG.info("Migrating resource " + rid + " data...");
            String code = base.getAttribute("code");
            String img = base.getAttribute("img", "");
            String longdesc = base.getAttribute("longdesc");
            String url = base.getAttribute("url");
            String altText = base.getAttribute("alt");
            Boolean openNewWindow = Boolean.parseBoolean(base.getAttribute("target", "true"));

            if (null != code) {
                setCode(code);
            }

            if (null != img) {
                setImage(img);

                String sourcePath = SWBPortal.getWorkPath() + base.getWorkPath() + "/";
                String destPath = SWBPortal.getWorkPath() + getSemanticObject().getWorkPath() + "/";
                File f = new File(destPath);
                if (!f.exists()) {
                    f.mkdirs();
                }

                try (FileInputStream fis = new FileInputStream(new File(sourcePath + img))) {
                    FileOutputStream fos = new FileOutputStream(new File(destPath + Banner.swbres_bannerImage.getName() + "_" + getResourceBase().getId() + "_" + img));

                    SWBUtils.IO.copyStream(fis, fos);
                    SWBUtils.IO.removeDirectory(sourcePath);
                } catch (IOException ioex) {
                    LOG.info("Error migrating banner " + rid + " image", ioex);
                }
            }

            if (null != url) {
                setURL(url);
            }

            if (null != altText) {
                setAltText(altText);
            }

            if (openNewWindow) {
                setURLTarget("_blank");
            } else {
                setURLTarget("_self");
            }

            copyResources(recobj);

            base.setAttribute("migrated", "true");
            try {
                base.updateAttributesToDB();
            } catch (SWBException swbex) {
                LOG.info("Error setting banner as migrated", swbex);
            }
        }
    }

    /**
     * Copies resource assets.
     */
    private void copyResources(ResourceType recobj) {
        LOG.info("Migrating resource " + recobj.getTitle() + " assets...");
        File jsps = new File(getAssetsFolderPath(recobj));

        if (jsps.exists() && jsps.isDirectory()) {
            LOG.info("Assets already in resource path");
        } else if (jsps.mkdirs()) {
            URL fis = getClass().getClassLoader().getResource("swbadmin/jsp/resources/banner/view.jsp");
            try  {
                File destFile = new File(getAssetsFolderPath(recobj) + "/swbadmin/jsp/resources/banner/view.jsp");
                Files.write(destFile.toPath(), SWBUtils.IO.readInputStream(fis.openStream()).getBytes(), StandardOpenOption.CREATE);
            } catch (IOException ioex) {
                LOG.error("Error migrating resource assets. ", ioex);
            }
        }
    }

    /**
     * Gets resource assets folder path.
     * @return Resource assets folder path.
     */
    private String getAssetsFolderPath(ResourceType recobj) {
        return SWBPortal.getWorkPath() + "/models/" + recobj.getWebSite().getId()+ "/jsp/resources/banner";
    }

    public String replaceTags(String str, HttpServletRequest request, User user, WebPage webpage) {
        if (str == null || str.trim().length() == 0) {
            return null;
        }

        str = str.trim();

        Iterator it = SWBUtils.TEXT.findInterStr(str, "{request.getParameter(\"", "\")}");
        while (it.hasNext()) {
            String s = (String) it.next();
            str = SWBUtils.TEXT.replaceAll(str, "{request.getParameter(\"" + s + "\")}", request.getParameter(replaceTags(s, request, user, webpage)));
        }

        it = SWBUtils.TEXT.findInterStr(str, "{session.getAttribute(\"", "\")}");
        while (it.hasNext()) {
            String s = (String) it.next();
            str = SWBUtils.TEXT.replaceAll(str, "{session.getAttribute(\"" + s + "\")}", (String) request.getSession().getAttribute(replaceTags(s, request, user, webpage)));
        }

        it = SWBUtils.TEXT.findInterStr(str, "{getEnv(\"", "\")}");
        while (it.hasNext()) {
            String s = (String) it.next();
            str = SWBUtils.TEXT.replaceAll(str, "{getEnv(\"" + s + "\")}", SWBPlatform.getEnv(replaceTags(s, request, user, webpage)));
        }

        str = SWBUtils.TEXT.replaceAll(str, "{user.login}", user.getLogin());
        str = SWBUtils.TEXT.replaceAll(str, "{user.email}", user.getEmail());
        str = SWBUtils.TEXT.replaceAll(str, "{user.language}", user.getLanguage());
        str = SWBUtils.TEXT.replaceAll(str, "{webpath}", SWBPortal.getContextPath());
        str = SWBUtils.TEXT.replaceAll(str, "{distpath}", SWBPortal.getDistributorPath());
        str = SWBUtils.TEXT.replaceAll(str, "{webworkpath}", SWBPortal.getWebWorkPath());
        str = SWBUtils.TEXT.replaceAll(str, "{workpath}", SWBPortal.getWorkPath());
        str = SWBUtils.TEXT.replaceAll(str, "{websiteid}", webpage.getWebSiteId());
        return str;
    }
}