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
package org.semanticwb.portal.resources.sem.contact;

import javax.servlet.http.HttpServletRequest;
import org.semanticwb.portal.api.SWBActionResponse;

public class Address extends org.semanticwb.portal.resources.sem.contact.base.AddressBase 
{
    public Address(org.semanticwb.platform.SemanticObject base)
    {
        super(base);
    }

    /*public void edit(HttpServletRequest request, SWBActionResponse response) {
        setStreet(SWBUtils.TEXT.nullValidate(request.getParameter("str"), ""));
        setSuburb(SWBUtils.TEXT.nullValidate(request.getParameter("sb"), ""));
        setCity(SWBUtils.TEXT.nullValidate(request.getParameter("ct"), ""));
        setState(SWBUtils.TEXT.nullValidate(request.getParameter("st"), ""));
        setCp(SWBUtils.TEXT.nullValidate(request.getParameter("cp"), ""));
//        setCountry(Country.ClassMgr.getCountry(request.getParameter("country"),));
        setIsToCheck(request.getParameter("ck")==null?false:true);
        setIsToMail(request.getParameter("ml")==null?false:true);
    }*/

    public String toString(String language) {
        StringBuilder sb = new StringBuilder();
        if(getStreet()!=null)
            sb.append(getStreet()).append(";");
        if(getSuburb()!=null)
            sb.append(getSuburb()).append(";");
        if(getCity()!=null)
            sb.append(getCity()).append(";");
        if(getState()!=null)
            sb.append(getState()).append(";");
        if(getCp()!=null)
            sb.append(getCp()).append(";");
        if(getCountry()!=null)
            sb.append(getCountry().getDisplayTitle(language));
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(getStreet()!=null)
            sb.append(getStreet()).append(";");
        if(getSuburb()!=null)
            sb.append(getSuburb()).append(";");
        if(getCity()!=null)
            sb.append(getCity()).append(";");
        if(getState()!=null)
            sb.append(getState()).append(";");
        if(getCp()!=null)
            sb.append(getCp()).append(";");
        if(getCountry()!=null)
            sb.append(getCountry().getTitle());
        return sb.toString();
    }
}