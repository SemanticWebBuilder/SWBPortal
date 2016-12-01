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
package org.semanticwb.portal.admin.resources.monitor;


import org.semanticwb.portal.api.GenericResource;
import org.semanticwb.portal.api.SWBParamRequest;
import org.semanticwb.portal.api.SWBResourceException;
import org.semanticwb.portal.api.SWBResourceURL;
import org.semanticwb.portal.api.SWBActionResponse;
import org.semanticwb.portal.SWBMonitor;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBPortal;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;
import org.semanticwb.model.User;
import org.semanticwb.portal.monitor.SWBSummary;
import org.semanticwb.servlet.internal.Distributor;

/** Recurso de administración de WebBuilder que monitorea el uso de memoria de la
 * aplicación, su memoria máxima, la memoria total, la memoria utilizada y la
 * memoria disponible. Permite también invocar al colector de basura.
 *
 * WebBuilder administration resource,it shows the memory used, the maximum
 * memory, the total memory, and the memory available, also allows to call the garbage
 * collector.
 * @author Carlos Ramos Incháustegui
 */
public class SWBAMMemory extends GenericResource {
    private static final String Mode_MEMMONITOR = "mm";
    
    /** The swb summary. */
    private SWBSummary swbSummary = null;
    
    /**
     * Creates a new instance of WBAMemory.
     */
    public SWBAMMemory() {
        swbSummary = new SWBSummary();
    }
    
    
    /**
     * Process action.
     * 
     * @param request the request
     * @param response the response
     * @throws SWBResourceException the sWB resource exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void processAction(HttpServletRequest request,
            SWBActionResponse response)
            throws SWBResourceException, IOException {
        String gc = request.getParameter("gc");
        if (gc != null) {
            System.gc();
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
    public void doView(HttpServletRequest request, HttpServletResponse response,
            SWBParamRequest paramsRequest) throws SWBResourceException, IOException {
        response.setContentType("text/html; charset=ISO-8859-1");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        
        SWBResourceURL url;
        url = paramsRequest.getRenderUrl().setCallMethod(SWBResourceURL.Call_DIRECT);
        url.setMode(Mode_MEMMONITOR);
        
        PrintWriter out = response.getWriter();

        out.println("<div class=\"swbform\">");
        if (SWBPlatform.getEnv("wb/systemMonitor", "false").equals("true")) {
            out.println("<fieldset>");
            out.println("<div style=\"width:100%\">");
            out.println("  <div id=\"chart_div\" style=\"width:100%; border:1px solid #ccc\"></div>");
            out.println("</div>");
            out.println("<script type=\"text/javascript\" src=\"/swbadmin/js/dojo/dojo/dojo.js\" djconfig=\"parseOnLoad: true, isDebug: false, locale: 'es'\"></script>");
            out.println("<script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>");
            out.println("<script type=\"text/javascript\">");
            out.println("  google.charts.load('current', {packages: ['corechart'], 'language': 'en'});");
            out.println("  google.charts.setOnLoadCallback(drawChart);");
            out.println("  var chart; var data; var options;");
            
            out.println("  function drawChart() {");
            out.println("    var jsonData = postSyncHtml_('"+url.toString()+"');");
            out.println("    var jsonObj = window.JSON.parse(jsonData);");
            out.println("    data = new google.visualization.DataTable();");
            out.println("    data.addColumn('datetime', 'instant');");
            out.println("    data.addColumn('number', 'Memoria total');");
            out.println("    data.addColumn('number', 'Memoria libre');");
            out.println("    data.addColumn('number', 'Memoria usada');");
            out.println("    dojo.forEach(jsonObj, function(entry, i){");
            out.print("        data.addRow([new Date(entry.y,entry.Mo,entry.d,entry.h,entry.mi,entry.s)");
            out.print("        ,entry.tm");
            out.print("        ,entry.fm");
            out.println("      ,entry.um]);");
            out.println("    });");
            
            out.println("    options = {");
            //out.println("      chart:{title:'Monitoreo de memoria', subtitle:'"+paramsRequest.getLocaleString("max")+": "+(Runtime.getRuntime().maxMemory()/1000000)+" (Mb)'},");
            out.println("      title: 'Monitoreo de memoria "+paramsRequest.getLocaleString("max")+": "+(Runtime.getRuntime().maxMemory()/1000000)+" (Mb)',");
            out.println("      width: '95%',");
            out.println("      height: 300,");
            out.println("      legend: { position: 'center' },");
            out.println("      chartArea:{left:100,top:40, bottom:80, width:'65%',height:180},");
            out.println("      hAxis: {title: 'Hora del día', titleTextStyle: {color:'#262626',bold:true,italic:false}, textStyle: {color: '#336799', fontSize:10}, format: 'hh:mm:ss', gridlines: {count: 10}},");
            out.println("      vAxis: {title: 'Memoria (Mb)', titleTextStyle: {color:'#262626',bold:true,italic:false}, textStyle: {color:'#336799', fontSize:10}, baseline:0, minValue:0, format: 'decimal', gridlines: {count: 10}},");
            out.println("      colors: ['#de3163', '#708a2b', '#3c6090'],");
            out.println("      explorer: { actions: ['dragToZoom', 'rightClickToReset'], axis: 'horizontal', maxZoomIn:16}");
            out.println("    };");
            
            out.println("    chart = new google.visualization.LineChart(document.getElementById('chart_div'));");
            out.println("    chart.draw(data, options);");
            out.println("  }");
            
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
            
            out.println("  function getLiveDataSet() {");
            out.println("    var jsonData = postSyncHtml_('"+url.toString()+"');");
            out.println("    var jsonObj = window.JSON.parse(jsonData);");
            out.println("    data = new google.visualization.DataTable();");
            out.println("    data.addColumn('datetime', 'instant');");
            out.println("    data.addColumn('number', 'Memoria total');");
            out.println("    data.addColumn('number', 'Memoria libre');");
            out.println("    data.addColumn('number', 'Memoria usada');");
            out.println("    dojo.forEach(jsonObj, function(entry, i){");
            out.print("        data.addRow([new Date(entry.y,entry.Mo,entry.d,entry.h,entry.mi,entry.s)");
            out.print("        ,entry.tm");
            out.print("        ,entry.fm");
            out.println("      ,entry.um]);");
            out.println("    });");
            
            out.println("    chart.draw(data, options);");
            out.println("  }");
            out.println("  window.setInterval(getLiveDataSet, 5000);");
            out.println("</script>");

            out.println("</fieldset>");
            
            out.println("<fieldset>");
            out.println("  <form id=\""+getResourceBase().getId()+"/MMemory\" action=\"" + paramsRequest.getActionUrl() + "\" method=\"POST\">");
            //out.println("    <button dojoType=\"dijit.form.Button\" name=\"gc\" onclick=\"submitForm('"+getResourceBase().getId()+"/MMemory'); return false;\" name=\"gc\">" + paramsRequest.getLocaleString("gc") + "</button>");
            out.println("    <button dojoType=\"dijit.form.Submit\" name=\"gc\" name=\"gc\">" + paramsRequest.getLocaleString("gc") + "</button>");
            out.println("  </form>");
            out.println("</fieldset>");
            out.println("<fieldset>");
            out.println("  <div class=\"swbform\">");
            out.println(swbSummary.getSample().GetSumaryHTML());
            out.println("    <div id=\"Alerts\">");
            out.println("      <ul><li>Is Page cache active: "+ Distributor.isPageCache()+"</li>");
            out.println("      </ul>");
            out.println("    </div>");
            out.println("  </div>");
            out.println("</fieldset>");
        } else {
            out.println("<fieldset>");
            out.println(paramsRequest.getLocaleString("msgIsNotActive"));
            out.println("</fieldset>");
        }
        out.println("</div>");
    }
    
    /**
     * Gets the data.
     * 
     * @param request the request
     * @param response the response
     * @param paramsRequest the params request
     * @return the data
     * @throws SWBResourceException the sWB resource exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void getData(HttpServletRequest request, HttpServletResponse response,
            SWBParamRequest paramsRequest)
            throws SWBResourceException, IOException {
        response.setContentType("text/json; charset=utf-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        
        User user = paramsRequest.getUser();
        Locale locale = new Locale(
                user.getLanguage()==null?"es":user.getLanguage()
                , user.getCountry()==null?"MX":user.getCountry());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", locale);
        Date date;
        Calendar cal;
        StringBuilder json = new StringBuilder();
        
        PrintWriter out = response.getWriter();

        int max = 100;
        int ratio = ((SWBPortal.getMonitor().getMonitorRecords().size() - 1) / max) + 1;
        Vector data = SWBPortal.getMonitor().getAverageMonitorRecords(ratio);
        int labc = data.size() / 10;
        if (labc == 0) {
            labc = 1;
        }

        Enumeration en = data.elements();
        int x = -1;
        long total, free, used;
        json.append("[");
        while (en.hasMoreElements()) {
            SWBMonitor.MonitorRecord mr = (SWBMonitor.MonitorRecord) en.nextElement();
            total = mr.getTotalMemory() / 1000000;
            free = mr.getFreeMemory() / 1000000;
            used = mr.getUsedMemory() / 1000000;
            if (x >= 0) {
                date = mr.getDate();
                cal = Calendar.getInstance(locale);
                cal.setTime(date);
                
                json.append("{");
                json.append("\"y\":"+cal.get(Calendar.YEAR)+",");
                json.append("\"Mo\":"+cal.get(Calendar.MONTH)+",");
                json.append("\"d\":"+cal.get(Calendar.DAY_OF_MONTH)+",");
                json.append("\"h\":"+cal.get(Calendar.HOUR_OF_DAY)+",");
                json.append("\"mi\":"+cal.get(Calendar.MINUTE)+",");
                json.append("\"s\":"+cal.get(Calendar.SECOND)+",");
                json.append("\"tm\":"+total+",");
                json.append("\"fm\":"+free+",");
                json.append("\"um\":"+used);
                json.append("},");
            }
            x++;
        }
        String d = json.toString();
        if(d.endsWith(",")) {
            int li = d.lastIndexOf(",");
            json.replace(li, li+1, "");
        }
        json.append("]");
        out.print(json.toString());
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
    public void processRequest(HttpServletRequest request,
            HttpServletResponse response, SWBParamRequest paramsRequest)
            throws SWBResourceException, IOException {
        final String mode = paramsRequest.getMode();
        
         if(Mode_MEMMONITOR.equals(mode)) {
            getData(request, response, paramsRequest);
        }else {
            super.processRequest(request, response, paramsRequest);
        }
    }    
}
