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

import org.semanticwb.portal.api.GenericResource;
import org.semanticwb.portal.api.SWBParamRequest;
import org.semanticwb.portal.api.SWBResourceException;
import org.semanticwb.portal.SWBMonitor;
import org.semanticwb.SWBPortal;
import javax.servlet.http.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import org.semanticwb.SWBPlatform;
import org.semanticwb.model.User;
import org.semanticwb.portal.api.SWBResourceURL;


/** Muestra una gráfica para monitorear los hits en unperiodo de tiempo.
 *
 * Shows a graph to monitoring the hits in a period of time.
 * @author Carlos Ramos Incháustegui
 */
public class SWBAHitsMonitor extends GenericResource {
    private static final String Mode_HMONITOR = "hm";
    private static final String Mode_HITSTIME = "ht";
    private static final String Mode_HITSXSEC = "hs";

    
    /**
     * Creates a new instance of WBAHitsMonitor.
     */
    public SWBAHitsMonitor() {
    }
    
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
                case Mode_HITSTIME:
                    hitsTime(request, response, paramsRequest);
                    break;
                case Mode_HITSXSEC:
                    hitsXSec(request, response, paramsRequest);
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
        
        PrintWriter out = response.getWriter();
        
        out.println("<div class=\"swbform\">");
        out.println("<fieldset>");
        if (SWBPlatform.getEnv("wb/systemMonitor","false").equals("true")) {
            out.println("<div style=\"width: \"80%\">");
            out.println("  <div id=\"chartM_div\" style=\"border: 1px solid #ccc\"></div>");
            out.println("  <div id=\"chartT_div\" style=\"border: 1px solid #ccc\"></div>");
            out.println("  <div id=\"chartS_div\" style=\"border: 1px solid #ccc\"></div>");
            out.println("</div>");
            out.println("<script type=\"text/javascript\" src=\""
                    +SWBPlatform.getContextPath()
                    +"/swbadmin/js/dojo/dojo/dojo.js\" djconfig=\"parseOnLoad: true, isDebug: false, locale: 'es'\"></script>");
            out.println("<script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>");
            out.println("<script type=\"text/javascript\">");
            out.println("  google.charts.load('current', {packages: ['corechart'], 'language': 'en'});");
            // Técnica sugerida por GoogleCharts
            // https://developers.google.com/chart/interactive/docs/basic_multiple_charts
            out.println("  google.charts.setOnLoadCallback(drawChartM);");
            out.println("  google.charts.setOnLoadCallback(drawChartT);");
            out.println("  google.charts.setOnLoadCallback(drawChartS);");

            out.println("  var chartM; var dataM; var chartT; var dataT; var chartS; var dataS;");
            out.println("  var optionsM; var optionsT; var optionsS;");
            
            out.println("  function drawChartM() {");
            out.println("    var jsonData = postSyncHtml_('"+url.setMode(Mode_HMONITOR).toString()+"');");
            out.println("    var jsonObj = window.JSON.parse(jsonData);");
            out.println("    dataM = new google.visualization.DataTable();");
            out.println("    dataM.addColumn('datetime', '"+paramsRequest.getLocaleString("lblInstant")+"');");
            out.println("    dataM.addColumn('number', '"+paramsRequest.getLocaleString("msgHits")+"');");
            out.println("    dojo.forEach(jsonObj, function(entry, i){");
            out.println("      dataM.addRow([new Date(entry.y,entry.Mo,entry.d,entry.h,entry.mi,entry.s),entry.v]);");
            out.println("    });");
            
            out.println("    optionsM = {");
            out.println("      title: 'Monitoreo de peticiones',");
            out.println("      width: '90%',");
            out.println("      height: 250,");
            out.println("      legend: { position: 'center' },");
            out.println("      chartArea:{left:100,top:40, bottom:40, width:'75%',height:150},");
            out.println("      hAxis: {title: '"+paramsRequest.getLocaleString("msgTimeOfDay")+"', titleTextStyle: {color:'#262626',bold:true,italic:false}, textStyle: {color: '#336799', fontSize:10}, format: 'hh:mm:ss', gridlines: {count: 10}},");
            out.println("      vAxis: {title: '"+paramsRequest.getLocaleString("msgHits")+"', titleTextStyle: {color:'#262626',bold:true,italic:false}, textStyle: {color: '#336799', fontSize:10}, minValue: 0, format: 'decimal', gridlines: {count: 10}},");
            out.println("      colors: ['#de3163'],");
            out.println("      explorer: { actions: ['dragToZoom', 'rightClickToReset'], axis: 'horizontal', maxZoomIn:16}");
            out.println("    };");

            out.println("    chartM = new google.visualization.LineChart(document.getElementById('chartM_div'));");
            out.println("    chartM.draw(dataM, optionsM);");
            //notifyDataSetChanged();
            out.println("  }");
            
            out.println("  function drawChartT() {");
            out.println("    var jsonData = postSyncHtml_('"+url.setMode(Mode_HITSTIME).toString()+"');");
            out.println("    var jsonObj = window.JSON.parse(jsonData);");
            out.println("    dataT = new google.visualization.DataTable();");
            out.println("    dataT.addColumn('datetime', '"+paramsRequest.getLocaleString("lblInstant")+"');");
            out.println("    dataT.addColumn('number', '"+paramsRequest.getLocaleString("hitsTime")+"');");
            out.println("    dojo.forEach(jsonObj, function(entry, i){");
            out.println("      dataT.addRow([new Date(entry.y,entry.Mo,entry.d,entry.h,entry.mi,entry.s),entry.v]);");
            out.println("    });");
            
            out.println("    optionsT = {");
            out.println("      title: 'Tiempo de procesamiento',");
            out.println("      width: '90%',");
            out.println("      height: 250,");
            out.println("      legend: { position: 'center' },");
            out.println("      chartArea:{left:100,top:40, bottom:40, width:'75%',height:150},");
            out.println("      hAxis: {title: '"+paramsRequest.getLocaleString("msgTimeOfDay")+"', titleTextStyle: {color:'#262626',bold:true,italic:false}, textStyle: {color: '#336799', fontSize:10}, format: 'hh:mm:ss', gridlines: {count: 10}},");
            out.println("      vAxis: {title: '"+paramsRequest.getLocaleString("hitsTime")+"', titleTextStyle: {color:'#262626',bold:true,italic:false}, textStyle: {color: '#336799', fontSize:10}, minValue: 0, format: 'decimal', gridlines: {count: 10}},");
            out.println("      colors: ['#708a2b'],");
            out.println("      explorer: { actions: ['dragToZoom', 'rightClickToReset'], axis: 'horizontal', maxZoomIn:16}");
            out.println("    };");
            
            out.println("    chartT = new google.visualization.LineChart(document.getElementById('chartT_div'));");
            out.println("    chartT.draw(dataT, optionsT);");
            out.println("  }");
            
            out.println("  function drawChartS() {");
            out.println("    var jsonData = postSyncHtml_('"+url.setMode(Mode_HITSXSEC).toString()+"');");
            out.println("    var jsonObj = window.JSON.parse(jsonData);");
            out.println("    dataS = new google.visualization.DataTable();");
            out.println("    dataS.addColumn('datetime', '"+paramsRequest.getLocaleString("lblInstant")+"');");
            out.println("    dataS.addColumn('number', '"+paramsRequest.getLocaleString("msgHitsXseg")+"');");
            out.println("    dojo.forEach(jsonObj, function(entry, i){");
            out.println("      dataS.addRow([new Date(entry.y,entry.Mo,entry.d,entry.h,entry.mi,entry.s),entry.v]);");
            out.println("    });");
            
            out.println("    optionsS = {");
            out.println("      title: 'Peticiones por segundo',");
            out.println("      width: '90%',");
            out.println("      height: 250,");
            out.println("      legend: { position: 'center' },");
            out.println("      chartArea:{left:100,top:40, bottom:40, width:'75%',height:150},");
            out.println("      hAxis: {title: '"+paramsRequest.getLocaleString("msgTimeOfDay")+"', titleTextStyle: {color:'#262626',bold:true,italic:false}, textStyle: {color: '#336799', fontSize:10}, format: 'hh:mm:ss', gridlines: {count: 10}},");
            out.println("      vAxis: {title: '"+paramsRequest.getLocaleString("msgHitsXseg")+"', titleTextStyle: {color:'#262626',bold:true,italic:false}, textStyle: {color: '#336799', fontSize:10}, minValue: 0, format: 'decimal', gridlines: {count: 10}},");
            out.println("      colors: ['#3c6090'],");
            out.println("      explorer: { actions: ['dragToZoom', 'rightClickToReset'], axis: 'horizontal', maxZoomIn:16}");
            out.println("    };");
            
            out.println("    chartS = new google.visualization.LineChart(document.getElementById('chartS_div'));");
            out.println("    chartS.draw(dataS, optionsS);");
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
            out.println("    var jsonData = postSyncHtml_('"+url.setMode(Mode_HMONITOR).toString()+"');");
            out.println("    var jsonObj = window.JSON.parse(jsonData);");
            out.println("    dataM = new google.visualization.DataTable();");
            out.println("    dataM.addColumn('datetime', '"+paramsRequest.getLocaleString("lblInstant")+"');");
            out.println("    dataM.addColumn('number', 'Peticiones');");
            out.println("    dojo.forEach(jsonObj, function(entry, i){");
            out.println("      dataM.addRow([new Date(entry.y,entry.Mo,entry.d,entry.h,entry.mi,entry.s),entry.v]);");
            out.println("    });");
            
            out.println("    jsonData = postSyncHtml_('"+url.setMode(Mode_HITSTIME).toString()+"');");
            out.println("    jsonObj = window.JSON.parse(jsonData);");
            out.println("    dataT = new google.visualization.DataTable();");
            out.println("    dataT.addColumn('datetime', '"+paramsRequest.getLocaleString("lblInstant")+"');");
            out.println("    dataT.addColumn('number', 'Tiempo');");
            out.println("    dojo.forEach(jsonObj, function(entry, i){");
            out.println("      dataT.addRow([new Date(entry.y,entry.Mo,entry.d,entry.h,entry.mi,entry.s),entry.v]);");
            out.println("    });");
            
            out.println("    jsonData = postSyncHtml_('"+url.setMode(Mode_HITSXSEC).toString()+"');");
            out.println("    jsonObj = window.JSON.parse(jsonData);");
            out.println("    dataS = new google.visualization.DataTable();");
            out.println("    dataS.addColumn('datetime', '"+paramsRequest.getLocaleString("lblInstant")+"');");
            out.println("    dataS.addColumn('number', 'Total de peticiones');");
            out.println("    dojo.forEach(jsonObj, function(entry, i){");
            out.println("      dataS.addRow([new Date(entry.y,entry.Mo,entry.d,entry.h,entry.mi,entry.s),entry.v]);");
            out.println("    });");
            
            out.println("    chartM.draw(dataM, optionsM);");
            out.println("    chartT.draw(dataT, optionsT);");
            out.println("    chartS.draw(dataS, optionsS);");
            out.println("  }");
            out.println("  window.setInterval(getLiveDataSet, 5000);");
            out.println("</script>");
        }else {
            out.println(paramsRequest.getLocaleString("msgIsNotActive"));
        }
        out.println("</fieldset>");
        out.println("</div>");
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
                , user.getCountry()==null?"MX":user.getCountry());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", locale);
        Date date;
        Calendar cal;
        StringBuilder json = new StringBuilder();
        
        PrintWriter out = response.getWriter();
        Vector data = SWBPortal.getMonitor().getMonitorRecords();
        
        Enumeration en = data.elements();
        int x = 0;
        int y = 0;
        int z = ((data.size() - 1) / 100) + 1;
        long prom = 0;
        json.append("[");
        while(en.hasMoreElements())
        {
            y++;
            SWBMonitor.MonitorRecord mr = (SWBMonitor.MonitorRecord) en.nextElement();
            long val = mr.getHits();
            prom += val;
            if ((y % z) == 0) {
                prom = prom / z;
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
                json.append("\"v\":"+prom);
                json.append("},");
                x++;
                prom = 0;
            }
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
     * Hits time.
     * 
     * @param request the request
     * @param response the response
     * @param paramsRequest the params request
     * @throws SWBResourceException the sWB resource exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void hitsTime(HttpServletRequest request, HttpServletResponse response,
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
        Vector data = SWBPortal.getMonitor().getMonitorRecords();
        Enumeration en = data.elements();
        int x = 0;
        int y = 0;
        int z = ((data.size() - 1) / 100) + 1;
        long prom = 0;
        json.append("[");
        while (en.hasMoreElements()) {
            y++;
            SWBMonitor.MonitorRecord mr = (SWBMonitor.MonitorRecord) en.nextElement();
            long val = mr.getHitsTime()/1000;
            prom += val;
            if ((y % z) == 0) {
                prom = prom / z;
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
                json.append("\"v\":"+prom);
                json.append("},");
                
                x++;
                prom = 0;
            }
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
     * Hits x sec.
     * 
     * @param request the request
     * @param response the response
     * @param paramsRequest the params request
     * @throws SWBResourceException the sWB resource exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void hitsXSec(HttpServletRequest request, HttpServletResponse response,
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
        Vector data = SWBPortal.getMonitor().getMonitorRecords();
        Enumeration en = data.elements();
        int x = 0;
        int y = 0;
        int z = ((data.size() - 1) / 100) + 1;
        long prom = 0;
        long old = 0;
        json.append("[");
        while (en.hasMoreElements()) {
            y++;
            SWBMonitor.MonitorRecord mr = (SWBMonitor.MonitorRecord) en.nextElement();
            long val = mr.getHits();
            if (y == 1) {
                old = val;
            }
            prom += val;
            if ((y % z) == 0) {
                prom = prom / z;
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
                json.append("\"v\":"+( (prom - old) / (SWBPortal.getMonitor().getDelay() * z) ));
                json.append("},");
                
                x++;
                old = prom;
                prom = 0;
            }
        }
        String d = json.toString();
        if(d.endsWith(",")) {
            int li = d.lastIndexOf(",");
            json.replace(li, li+1, "");
        }
        json.append("]");
        out.print(json.toString());
    }
}
