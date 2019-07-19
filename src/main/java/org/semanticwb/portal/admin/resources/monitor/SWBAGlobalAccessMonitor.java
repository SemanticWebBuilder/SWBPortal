package org.semanticwb.portal.admin.resources.monitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.semanticwb.SWBException;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBPortal;
import org.semanticwb.model.Resource;
import org.semanticwb.model.SWBContext;
import org.semanticwb.model.User;
import org.semanticwb.model.WebSite;
import org.semanticwb.portal.admin.admresources.util.WBAdmResourceUtils;
import org.semanticwb.portal.api.GenericResource;
import org.semanticwb.portal.api.SWBActionResponse;
import org.semanticwb.portal.api.SWBParamRequest;
import org.semanticwb.portal.api.SWBResourceException;
import org.semanticwb.portal.api.SWBResourceURL;
import org.semanticwb.portal.db.SWBRecHit;
import org.semanticwb.portal.db.SWBRecHits_;

/**
 *
 * @author carlos.ramos
 */
public class SWBAGlobalAccessMonitor extends GenericResource {
    private static final String Mode_HMONITOR = "hm";
    
    /**
     * Process the user request.
     * 
     * @param request input parameters
     * @param response an answer to the request
     * @param paramsRequest a list of objects (topic, user, action, ...)
     * @throws SWBResourceException an AF Exception
     * @throws IOException an IO Exception
     */    
    @Override
    public void processRequest(HttpServletRequest request,
            HttpServletResponse response, SWBParamRequest paramsRequest)
            throws SWBResourceException, IOException {
        final String mode = paramsRequest.getMode();
        
        if(null != mode) {
            switch (mode) {
                case Mode_HMONITOR:
                    hitsMonitor(request, response, paramsRequest);
                    break;
                default:
                    super.processRequest(request, response, paramsRequest);
                    break;
            }
        }else {
            super.processRequest(request, response, paramsRequest);
        }
    }       
    
    /**
     * User view of the SWBAHitsMonitor resource.
     * 
     * @param request input parameters
     * @param response an answer to the request
     * @param paramsRequest a list of objects (topic, user, action, ...)
     * @throws SWBResourceException an AF Exception
     * @throws IOException an IO Exception
     */    
    @Override
    public void doView(HttpServletRequest request, HttpServletResponse response,
            SWBParamRequest paramsRequest) 
            throws SWBResourceException, IOException {
        SWBResourceURL url;
        url = paramsRequest.getRenderUrl().setCallMethod(SWBResourceURL.Call_DIRECT);
        final User user = paramsRequest.getUser();
        final String lang = user.getLanguage();
        PrintWriter out = response.getWriter();
        final Resource base = getResourceBase();

        
        out.println("<script type=\"text/javascript\" src=\""
                    +SWBPlatform.getContextPath()
                    +"/swbadmin/js/dojo/dojo/dojo.js\" djconfig=\"parseOnLoad: true, isDebug: false, locale: 'es'\"></script>");
        out.println("<div class=\"swbform\">");
        out.println(" <fieldset>");
        out.println("<a href=\""
                +paramsRequest.getRenderUrl().setMode(SWBResourceURL.Mode_ADMIN).setParameter("supil", "true")
                +"\"><img src=\"/swbadmin/images/portlet/configuration.png\" alt=\"Configurar\" /></a>");
HashMap<String,String> mySites = new HashMap<>();
WebSite site;
String[] sites;
if(base.getAttribute("sites")==null) {
    sites=new String[2];
    sites[0] = SWBContext.WEBSITE_ADMIN;
    site = SWBContext.getWebSite("demo");
    if(site!=null) {
        sites[1] = "demo";
    }
}else {
    sites = base.getAttribute("sites").split(",");
}
System.out.println("sites="+Arrays.toString(sites));
for(String siteId : sites) {
//while(sites.hasNext() && mySites.size()<sb) {
    site = SWBContext.getWebSite(siteId);
    if(site==null) continue;
    //site = sites.next();
    if(!site.isValid()) continue;
    
    //id = site.getId();
    //if(id.equals(SWBContext.getGlobalWebSite().getId()))
    if(siteId.equals(SWBContext.getGlobalWebSite().getId()))
        continue;
    if(SWBPortal.getAdminFilterMgr().haveAccessToSemanticObject(user, site.getSemanticObject())) {
        //mySites.put(id, site.getDisplayTitle(lang));
        mySites.put(siteId, site.getDisplayTitle(lang));
    }
}

        

        out.println("  <div style=\"width:450px\">");
        for(Map.Entry<String, String> entry : mySites.entrySet()) {
            out.println("  <div id=\"chart_"+entry.getKey()+"_div\" style=\"border: 1px solid #ccc\"></div>");
        }
        out.println("  </div>");
        out.println("<script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>");
        out.println("<script type=\"text/javascript\">");
        out.println("  google.charts.load('current', {packages:['line'], 'language': 'es'});");
        out.println("  function postSyncHtml_(url)");
        out.println("  {");
        out.println("      var ret = [];");
        out.println("      var obj = dojo.xhrPost({");
        out.println("          url: url,");
        out.println("          sync: true,");
        out.println("          load: function(data) {");
        out.println("              ret = data;");
        out.println("          },");
        out.println("          error: function(data) {");
        out.println("              alert('An error occurred, with response: ' + data);");
        out.println("          }");
        out.println("      });");
        out.println("      return ret;");
        out.println("  }");
            
        for(Map.Entry<String, String> entry : mySites.entrySet()) {
            url.setParameter("sid", entry.getKey());
            out.println("  function drawChart_"+entry.getKey()+"() {");
            out.println("    var jsonData = postSyncHtml_('"+url.setMode(Mode_HMONITOR).toString()+"');");
            out.println("    var jsonObj = window.JSON.parse(jsonData);");
            out.println("    if(jsonObj.length===0) return;");
            out.println("    var data = new google.visualization.DataTable();");
            out.println("    data.addColumn('date', '"+paramsRequest.getLocaleString("lblHAxisTitle")+"');");
            out.println("    data.addColumn('number', '"+entry.getValue()+"');");
            out.println("    dojo.forEach(jsonObj, function(entry, i){");
            out.println("      data.addRow([new Date(entry.d),entry.v]);");
            out.println("    });");

            out.println("    var options = {");
            out.println("      chart: {");
            out.println("           title: '"+paramsRequest.getLocaleString("lblTitle")+" "+entry.getValue()+"',");
            out.println("           subtitle: '"+paramsRequest.getLocaleString("lblSubtitle")+"'");
            out.println("      },");

            //out.println("      legend: { position: 'center' },");
            //out.println("      width: 500,");
            //out.println("      height: 300,");
            out.println("      chartArea:{left:20,top:20, bottom:80, right:80},");
            out.println("      hAxis: {title: '"+paramsRequest.getLocaleString("lblHAxisTitle")+"', titleTextStyle: {color:'#9FC9EC',bold:true,italic:false}, textStyle: {color: '#336799', fontSize:10}, format: 'dd/MM/yy', gridlines: {count: 30}},");
            out.println("      vAxis: {title: '"+paramsRequest.getLocaleString("lblVAxisTitle")+"', titleTextStyle: {color:'#9FC9EC',bold:true,italic:false}, textStyle: {color: '#336799', fontSize:10}, minValue: 0, format: 'decimal', gridlines: {count: 30}},");
            out.println("      colors: ['#6699CD'],");
            out.println("    };");

            out.println("    var chart = new google.charts.Line(document.getElementById('chart_"+entry.getKey()+"_div'));");
            out.println("    chart.draw(data, options);");
            //notifyDataSetChanged();
            out.println("  }");
        }
        for(Map.Entry<String, String> entry : mySites.entrySet()) {
            out.println("  google.charts.setOnLoadCallback(drawChart_"+entry.getKey()+");");
        }
            
        out.println("  function getLiveDataSet(url, modelName, chart, data) {");
        out.println("    var jsonData = postSyncHtml_(url);");
        out.println("    var jsonObj = window.JSON.parse(jsonData);");
        out.println("    data = new google.visualization.DataTable();");
        out.println("    data.addColumn('datetime', 'tiempo');");
        out.println("    data.addColumn('number', modelName);");
        out.println("    dojo.forEach(jsonObj, function(entry, i){");
        out.println("      data.addRow([new Date(entry.y,entry.Mo,entry.d),entry.v]);");
        out.println("    });");
        out.println("    var chart = new google.charts.Line(document.getElementById(objId));");
        out.println("    chart.draw(data, optionsM);");
        out.println("  }");
        
        int clk = 0;
        try{
            clk = Integer.parseInt(base.getAttribute("clk","0"));
            clk *= 1000;
        }catch(Exception e) {
            clk = 0;
        }
        if(clk>0) {
            out.println("  window.setInterval(function(){");
            for(Map.Entry<String, String> entry : mySites.entrySet()) {
                out.println("getLiveDataSet('"+url.setMode(Mode_HMONITOR).toString()+"','"+entry.getKey()+"','chart_"+entry.getKey()+"_div');");
            }
            out.println("}, "+clk+");");
        }
        out.println("</script>");
        
        out.println("</fieldset>");
        out.println("</div>"); // .swbform
    }
    
    /**
     * Hits monitor.
     * 
     * @param request the request
     * @param response the response
     * @param paramsRequest the params request
     * @throws SWBResourceException the sWB resource exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void hitsMonitor(HttpServletRequest request,
            HttpServletResponse response, SWBParamRequest paramsRequest)
            throws SWBResourceException, IOException {
        response.setContentType("text/json; charset=utf-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        
        User user = paramsRequest.getUser();
        Locale locale = new Locale(
                user.getLanguage()==null?"es":user.getLanguage()
                ,user.getCountry()==null?"MX":user.getCountry());
        StringBuilder json = new StringBuilder();
        PrintWriter out = response.getWriter();
        final Resource base = getResourceBase();


//HashMap<String,String> sites = new HashMap<>();
//String id;
//WebSite site;    
//Iterator<WebSite> webSites = SWBContext.listWebSites(false);
//while(webSites.hasNext()) {
//    site = webSites.next();
//    id = site.getId();
//    if(id.equals(SWBContext.getGlobalWebSite().getId()))
//        continue;
//    if(SWBPortal.getAdminFilterMgr().haveAccessToSemanticObject(user, site.getSemanticObject()))
//        sites.put(id, site.getDisplayTitle(paramsRequest.getUser().getLanguage()));
//}
        
        
        final String sid = request.getParameter("sid");
        int elapsedDays;
        try {
            elapsedDays = Integer.parseInt(base.getAttribute("elapsedDays","7"));
        }catch(NumberFormatException nfe) {
            elapsedDays = 7;
        }
        if(elapsedDays>0) elapsedDays=-elapsedDays;
        
        Calendar today, pastDays, cal;
        today = new GregorianCalendar(locale);
        pastDays = (GregorianCalendar)today.clone();
        pastDays.add(Calendar.DAY_OF_YEAR, elapsedDays);
        List<SWBRecHit> resumeRecHits;
        resumeRecHits 
                = SWBRecHits_.getInstance().getResHitsLog(sid, "_", 0, 
                        pastDays.get(Calendar.YEAR),
                        pastDays.get(Calendar.MONTH)+1,
                        pastDays.get(Calendar.DAY_OF_MONTH),
                        today.get(Calendar.YEAR),
                        today.get(Calendar.MONTH)+1,
                        today.get(Calendar.DAY_OF_MONTH));
        Iterator<SWBRecHit> hits = resumeRecHits.iterator();
        SWBRecHit hit;
        json.append("[");
        while(hits.hasNext()) {
            hit = hits.next();
            cal = new GregorianCalendar(hit.getYear(),hit.getMonthToInt()-1,hit.getDay());
            json.append("{");
            json.append("\"d\":").append(cal.getTimeInMillis()).append(",");
            json.append("\"v\":").append(hit.getHits());
            json.append("}");
            if(hits.hasNext()) json.append(",");
        }
        json.append("]");
        out.print(json.toString());
    }

    private String getForm(HttpServletRequest request, SWBParamRequest paramsRequest) throws SWBResourceException
    {
        //String name = getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1);
        //WBAdmResourceUtils admResUtils = new WBAdmResourceUtils();
        StringBuilder ret = new StringBuilder();
        Resource base = getResourceBase();
        SWBResourceURL url = paramsRequest.getActionUrl().setAction("update");
//        final String supil = request.getParameter("supil");
        final boolean supil = Boolean.parseBoolean(request.getParameter("supil"));
        if(supil) {
            url.setParameter("supil", Boolean.toString(supil));
        }
        
        String siteId;
        WebSite site;
        
        final User user = paramsRequest.getUser();
        final String lang = user.getLanguage();
        
        ret.append("<div class=\"swbform\">");
        ret.append("<form name=\"frmResource\" method=\"post\" action=\"");
        ret.append(url.toString());
        ret.append("\"> \n");
        ret.append("<fieldset>");
        ret.append("<legend>"+paramsRequest.getLocaleString("lblMonitoring")+"</legend>");
        ret.append("<table>");
        
        ret.append("<tr> \n");
        ret.append("<td align=\"right\">"
                +paramsRequest.getLocaleString("lblElapsedDays")+ " *:" + "</td> \n");
        ret.append("<td>");
        ret.append("<input type=\"text\" name=\"elapsedDays\" dojoType=\"dijit.form.ValidationTextBox\" regExp=\"\\d{1,2}\" ");
        ret.append(" required=\"true\" promptMessage=\""+paramsRequest.getLocaleString("msgPromptMsgElapsedDays")+"\" invalidMessage=\""+paramsRequest.getLocaleString("msgInvalidMsgElapsedDays")+"\" ");
        //if (!"".equals(base.getAttribute("elapsedDays", "").trim())) {
            ret.append(" value=\""+base.getAttribute("elapsedDays","7")+"\"");
        //}
        ret.append("/></td> \n");
        ret.append("</tr> \n");
        
        ret.append("<tr> \n");
        ret.append("<td align=\"right\">"
                +paramsRequest.getLocaleString("lblTimer")+ " <i>("+paramsRequest.getLocaleString("lblSeconds")+")</i> *:" + "</td> \n");
        ret.append("<td>");
        ret.append("<input type=\"text\" name=\"clk\" dojoType=\"dijit.form.ValidationTextBox\" regExp=\"\\d{1,2}\" ");
        ret.append(" required=\"true\" promptMessage=\""+paramsRequest.getLocaleString("msgPromptMsgTimer")+"\" invalidMessage=\""+paramsRequest.getLocaleString("msgInvalidMsgTimer")+"\" ");
        //if (!"".equals(base.getAttribute("clk", "0").trim())) {
            ret.append(" value=\""+base.getAttribute("clk","0")+"\"");
        //}
        ret.append("/></td> \n"); 
        ret.append("</tr> \n");
        
        String mySites = base.getAttribute("sites","");
        if(mySites.isEmpty()) {
            mySites = SWBContext.WEBSITE_ADMIN;
            site = SWBContext.getWebSite("demo");
            if(site!=null) {
                mySites += ",demo";
            }
        }
        ret.append("<tr> \n");
        ret.append("<td align=\"right\">"
                +paramsRequest.getLocaleString("lblSelectWebSites")+ " *:" + "</td> \n");
        ret.append("<td>");
        ret.append("<select name=\"sites\" id=\"sites\" class=\"form-control\" multiple=\"true\">");
        //ret.append(" <option value=\"\">Seleccionar...</option>");
        Iterator<WebSite> sites;
        sites = SWBContext.listWebSites(false);
        while(sites.hasNext()) {
            site = sites.next();
            if(!site.isValid()) continue;
            siteId = site.getId();
            if(siteId.equals(SWBContext.getGlobalWebSite().getId()))
                continue;
            if(SWBPortal.getAdminFilterMgr().haveAccessToSemanticObject(user, site.getSemanticObject())) {
                //mySites.put(id, site.getDisplayTitle(lang));
                ret.append(" <option value=\"").append(site.getId()).append("\" ");
                if(mySites.contains(siteId)) {
                    ret.append("selected=\"selected\"");
                }
                ret.append(">");
                ret.append(site.getDisplayTitle(lang));
                ret.append("</option>\n");
            }
        }
        ret.append("</select>");
        ret.append("</td> \n");
        ret.append("</tr> \n");
            
        ret.append("</table> \n");
        ret.append("</fieldset>");
            
            ret.append("<fieldset>\n");
            ret.append("<table> \n");
            ret.append("<tr> \n");
            ret.append("\n<td>");   
            ret.append("\n <button dojoType=\"dijit.form.Button\" type=\"submit\">"
                    +paramsRequest.getLocaleString("btnSubmit")
                    +"</button>&nbsp;");
            if(supil) {
                ret.append("\n <button dojoType=\"dijit.form.Button\" type=\"reset\" onclick=\"window.location.href='")
                        .append(paramsRequest.getRenderUrl().setMode(SWBResourceURL.Mode_VIEW)).append("'\" >")
                        .append(paramsRequest.getLocaleString("btnCancel")).append("</button>");
            }else {
                ret.append("\n <button dojoType=\"dijit.form.Button\" type=\"reset\">"
                    +paramsRequest.getLocaleString("btnReset")
                    +"</button>");
            }            
            ret.append("\n</td>");
            ret.append("\n</tr>");
            ret.append("\n</table>");
            ret.append("</fieldset>\n");
            ret.append("\n<br>* " + paramsRequest.getLocaleString("msgRequiredData"));
            ret.append("</form> \n");
            ret.append("</div> \n");
            return ret.toString();
    }
    
    @Override
    public void doAdmin(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest)
            throws SWBResourceException, IOException {
        response.setContentType("text/html; charset=ISO-8859-1");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        
        PrintWriter out = response.getWriter();
        
        final boolean supil = Boolean.parseBoolean(request.getParameter("supil"));
        
        String action = null != request.getParameter("act") && !"".equals(request.getParameter("act").trim()) ? request.getParameter("act").trim() : paramRequest.getAction();

        if (action.equalsIgnoreCase("add") || action.equalsIgnoreCase("edit"))
        {
            out.println(getForm(request, paramRequest));
        }
        else if (action.equalsIgnoreCase("update"))
        {
            String msg = request.getParameter("msg");
            if(msg!=null) {
                SWBResourceURL url = paramRequest.getRenderUrl().setAction("edit");
                if(supil) {
                    url.setMode(SWBResourceURL.Mode_VIEW);
                }
                msg = paramRequest.getLocaleString(msg);
                out.println("<script type=\"text/javascript\" language=\"JavaScript\">");
                out.println("   alert('" + msg+ " "+ getResourceBase().getId() + "');");
                out.println("   location='" + url.toString() + "';");
                out.println("</script>");
            }
        }
    }

    @Override
    public void processAction(HttpServletRequest request, SWBActionResponse response)
            throws SWBResourceException, IOException
    {
System.out.println("processAction...");
System.out.println("request.getParameter(\"supil\")="+request.getParameter("supil"));
        Resource base = getResourceBase();
        final String action = response.getAction();
        if("update".equalsIgnoreCase(action)) {
            int elapsedDays = 0, clk = 0;
            try {
                elapsedDays = Integer.parseInt(request.getParameter("elapsedDays"));
            }catch(Exception nfe) {
                elapsedDays = 7;
            }finally {
                base.setAttribute("elapsedDays", Integer.toString(elapsedDays));
            }
            try {
                clk = Integer.parseInt(request.getParameter("clk"));
            }catch(Exception nfe) {
                clk = 0;
            }finally {
                base.setAttribute("clk", Integer.toString(clk));
            }
            String sites;
System.out.println("request.getParameterValues(\"sites\")="+request.getParameterValues("sites"));
            if(request.getParameterValues("sites")==null) {
                base.removeAttribute("sites");
            }else {
                sites = String.join(",", request.getParameterValues("sites"));
                if(sites!=null) {
                    base.setAttribute("sites", sites);
                }
            }
            try {
                response.getResourceBase().updateAttributesToDB();
            } catch (SWBException swbe) {
                throw new SWBResourceException("Updating attributes to DB", swbe);
            }
            response.setRenderParameter("msg", "msgOkUpdateResource");
            
            final String supil = request.getParameter("supil");
            if(supil != null) {
                response.setRenderParameter("supil", supil);
            }
        }
    }
}
