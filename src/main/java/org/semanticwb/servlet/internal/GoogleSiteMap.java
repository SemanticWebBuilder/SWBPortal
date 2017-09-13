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
package org.semanticwb.servlet.internal;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.semanticwb.Logger;
import org.semanticwb.SWBUtils;
import org.semanticwb.model.SWBContext;
import org.semanticwb.model.WebPage;
import org.semanticwb.model.WebSite;

/**
 * The Class GoogleSiteMap.
 * 
 * @author serch
 */
public class GoogleSiteMap implements InternalServlet {

    /** The log. */
    private static Logger log = SWBUtils.getLogger(GoogleSiteMap.class);

    /* (non-Javadoc)
     * @see org.semanticwb.servlet.internal.InternalServlet#init(javax.servlet.ServletContext)
     */
    public void init(ServletContext config) throws ServletException
    {
        log.event("Initializing InternalServlet GoogleSiteMap...");
    }

    /* (non-Javadoc)
     * @see org.semanticwb.servlet.internal.InternalServlet#doProcess(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.semanticwb.servlet.internal.DistributorParams)
     */
    public void doProcess(HttpServletRequest req, HttpServletResponse resp, DistributorParams dparams) throws IOException, ServletException
    {
        String lang = dparams.getUser().getLanguage();
        String host = "http://"+req.getServerName();
        if (req.getServerPort()!=80) host += ":"+req.getServerPort();
        resp.setContentType("text/xml");
        resp.setCharacterEncoding("UTF-8");
        StringBuilder ret=new StringBuilder();
        ret.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        ret.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">");

        String modelid=dparams.getModelId();

        {
            WebSite map = SWBContext.getWebSite(modelid);

            String hn = host;

            WebPage topicH = map.getHomePage();
            processWebPage(ret, hn, topicH, 95, lang);

        }
        ret.append("</urlset>");
        resp.getWriter().print(SWBUtils.TEXT.encode(ret.toString(), SWBUtils.TEXT.CHARSET_UTF8));
    }

    void processWebPage(StringBuilder ret, String hn, WebPage page, int score, String lang)
    {
        int tscore = score-5;
        String scoregap = score==5?"05":""+score;
        if (page.isVisible())
        {
            String urlcnt;
            urlcnt = page.getUrl();

            if(page.getWebPageURL()==null||!page.getWebPageURL().startsWith("http:"))
            {
                ret.append("<url><loc>" + hn +urlcnt + "</loc>");
                if (!"".equals(page.getContentsLastUpdate(lang, "yyyy-mm-dd")))
                    ret.append("<lastmod>"+page.getContentsLastUpdate(lang, "yyyy-mm-dd")+"</lastmod>");
                ret.append("<priority>0."+scoregap+"</priority></url>");
            }            
            
        }
        Iterator<WebPage> childs =page.listChilds(lang, true, false, null, true);
        if (score==5) { tscore = score;}
        while (childs.hasNext())
        {
            processWebPage(ret, hn, childs.next(), tscore, lang);
        }
    }

}
