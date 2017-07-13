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
package org.semanticwb.portal.resources;

import org.semanticwb.Logger;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBPortal;
import org.semanticwb.SWBUtils;
import org.semanticwb.model.Resource;
import org.semanticwb.portal.api.GenericAdmResource;


// TODO: Auto-generated Javadoc
/** Objeto que se encarga de desplegar y administrar Mis Secciones Favoritas de los usuarios finales
 * bajo ciertos criterios(configuración de recurso).
 *
 * Object that is in charge to unfold and to administer My Favorite Topics of the end
 * users under certain criteria (resource configuration).
 *
 * @author : Vanessa Arredondo Núñez
 * @version 1.0
 */

public class FavoriteTopics extends GenericAdmResource 
{
    
    /** The log. */
    private static Logger log = SWBUtils.getLogger(FavoriteTopics.class);
    
    /** The tpl. */
    javax.xml.transform.Templates tpl; 
    
    /** The web work path. */
    String webWorkPath = "/work"; 
    
    /** The path. */
    String path = SWBPlatform.getContextPath() +"swbadmin/xsl/FavoriteTopics/";
    
    /**
     * Creates a new instance of FavoriteTopics.
     */
    public FavoriteTopics() { 
    }
    
    /**
     * Sets the resource base.
     * 
     * @param base the new resource base
     */      
    public void setResourceBase(Resource base)
    {
        try 
        { 
            super.setResourceBase(base); 
            webWorkPath = (String) SWBPortal.getWebWorkPath() +  base.getWorkPath();
        }
        catch(Exception e) { log.error("Error while setting resource base: "+base.getId() +"-"+ base.getTitle(), e);  }
        if(!"".equals(base.getAttribute("template","").trim()))
        {
            try 
            { 
                tpl = SWBUtils.XML.loadTemplateXSLT(SWBUtils.IO.getStreamFromString(SWBUtils.IO.getFileFromPath(base.getWorkPath()+"/"+ base.getAttribute("template").trim()))); 
                path=webWorkPath + "/";
            }
            catch(Exception e) { log.error("Error while loading resource template: "+base.getId(), e); }
        }
        if(tpl==null)
        {
            try { tpl = SWBUtils.XML.loadTemplateXSLT(SWBPortal.getAdminFileStream("/swbadmin/xsl/FavoriteTopics/FavoriteTopics.xslt"));} 
            catch(Exception e) { log.error("Error while loading default resource template: "+base.getId(), e); }
        }
    }
    
    /**
     * @param doc
     * @param parent
     * @param elemName
     * @param elemValue
     */      
    private void addElem(org.w3c.dom.Document doc, org.w3c.dom.Node parent, String elemName, String elemValue)
    {
        org.w3c.dom.Element elem = doc.createElement(elemName);
        elem.appendChild(doc.createTextNode(elemValue));
        parent.appendChild(elem);
    }    

    /**
     * Adds the elem.
     * 
     * @param doc the doc
     * @param parent the parent
     * @param elemName the elem name
     * @param elemValue the elem value
     */     
    private void addElem(org.w3c.dom.Document doc, org.w3c.dom.Element parent, String elemName, String elemValue)
    {
        org.w3c.dom.Element elem = doc.createElement(elemName);
        elem.appendChild(doc.createTextNode(elemValue));
        parent.appendChild(elem);
    }    
    
}
