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


import org.semanticwb.base.db.DBConnectionPool;
import org.semanticwb.base.db.PoolConnectionTimeLock;
import org.semanticwb.portal.api.GenericResource;
import org.semanticwb.portal.api.SWBParamRequest;
import org.semanticwb.portal.api.SWBResourceException;
import org.semanticwb.portal.api.SWBResourceURL;
import org.semanticwb.portal.api.SWBActionResponse;
import org.semanticwb.portal.SWBMonitor;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBPortal;
import org.semanticwb.SWBUtils;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentHashMap;
import org.semanticwb.model.User;


/** Muestra el pool de conexiones a la base de datos seleccionada por el usuario
 * administrador.
 *
 * Shows the conection pool of the data base selected by the administrator
 * user.
 * @author Carlos Ramos Incháustegui
 */
public class SWBADBPool extends GenericResource {
    private static final String Mode_ACTIVITY = "hm";
    private static final String Mode_HITSTIME = "ht";
    private static final String Mode_TIME = "hs";
    
    /**
     * Creates a new instance of SWBADBPool.
     */
    public SWBADBPool() {
    }
        
    /**
     * Process the user request action.
     * 
     * @param request the request
     * @param response the response
     * @throws SWBResourceException, a Resource Exception
     * @throws IOException, an In Out Exception
     */    
    @Override
    public void processAction(HttpServletRequest request,
            SWBActionResponse response) throws SWBResourceException, IOException {
System.out.println("dbpool process action");
        String clear = request.getParameter("clear");
        String dbcon = request.getParameter("dbcon");
System.out.println("clear="+clear+"; dbcon="+dbcon);
        if(clear != null) {
            for (Enumeration<DBConnectionPool> en = SWBUtils.DB.getPools(); en.hasMoreElements();) {
                DBConnectionPool dbPool = en.nextElement();
                if (dbPool.getName().equals(dbcon)) {
                    dbPool.release();
                }
            }
        }
        response.setRenderParameter("dbcon", dbcon);
    }
    
    /**
     * Show the user view of the connection pool.
     * 
     * @param request the request
     * @param response the response
     * @param paramsRequest the params request
     * @throws SWBResourceException, a Resource Exception
     * @throws IOException, an In Out Exception
     */    
    @Override
    public void doView(HttpServletRequest request, HttpServletResponse response,
            SWBParamRequest paramsRequest)
            throws SWBResourceException, IOException {
        response.setContentType("text/html; charset=ISO-8859-1");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        PrintWriter out = response.getWriter();
        String dbcon = request.getParameter("dbcon");
        DBConnectionPool selectedPool = null;
        
        if (dbcon == null) {
            dbcon = SWBPlatform.getEnv("wb/db/nameconn", "swb");
        }
        
        out.println("<div class=\"swbform\">");
        out.println("<fieldset>");
        out.println("<table border=\"0\" cellspacing=\"2\" cellpadding=\"0\" width=\"98%\">");
        out.println("<tr><td width=\"200\">");
        out.println(paramsRequest.getLocaleString("connPool"));
        out.println("</td><td>");
        SWBResourceURL url = paramsRequest.getRenderUrl();
        out.println("<form id=\""+getResourceBase().getId()+"/ocDB\" action=\"" + url + "\" method=\"post\">");
        out.println("<select name=\"dbcon\" onChange=\"submitForm('"+getResourceBase().getId()+"/ocDB');return false;\">");
        Enumeration<DBConnectionPool> en = SWBUtils.DB.getPools();
        while (en.hasMoreElements()) {
            DBConnectionPool pool = (DBConnectionPool) en.nextElement();
            String name = pool.getName();
            out.print("<option value=\""+name+"\"");
            if (name.equals(dbcon)) {
                out.print(" selected");
                selectedPool = pool;
            }
            out.println(">" + name + "</option>");
        }
        out.println("</select>");
        out.println("</form>");
        out.println("</td></tr>");
        out.println("</table>");
        out.println("</fieldset>");
        out.println("</div>");
        
        PoolConnectionTimeLock timelock = SWBUtils.DB.getTimeLock();
        
        if (selectedPool != null) {
            String dbname = "-";
            String dbversion = "-";
            String drivername = "-";
            String driverversion = "-";
            String encpasswd = selectedPool.getPassword();
            try {
                Connection con = SWBUtils.DB.getConnection(selectedPool.getName(), "WBADBPool");
                java.sql.DatabaseMetaData md = con.getMetaData();
                dbname = md.getDatabaseProductName();
                dbversion = md.getDatabaseProductVersion();
                drivername = md.getDriverName();
                driverversion = md.getDriverVersion();
                con.close();
            } catch (Exception e) {}
            
            int tot = SWBUtils.DB.getConnections(selectedPool.getName());
            int free=+SWBUtils.DB.getFreeConnections(selectedPool.getName());
            out.println("<div class=\"swbform\">");
            out.println("<fieldset>");
            out.println("  <table width=\"98%\" border=0>");
            out.println("    <tr><td>DBName:</td><td>" + dbname + " (" + dbversion + ")</td>");
            out.println("        <td>" + paramsRequest.getLocaleString("total") + ":</td><td>" + tot + "</td></tr>");
            out.println("    <tr><td>Driver:</td><td>" + drivername + " (" + driverversion + ")</td>");
            out.println("       <td>" + paramsRequest.getLocaleString("used") + ":</td><td>" + (tot - free) + "</td></tr>");
            out.println("    <tr><td>URL:</td><td>" + selectedPool.getURL() + "</td>");
            out.println("        <td>" + paramsRequest.getLocaleString("free") + ":</td><td>" + free + "</td></tr>");
            out.println("    <tr><td>" + paramsRequest.getLocaleString("user") + ":</td><td>" + selectedPool.getUser() + "</td>");
            out.println("        <td>" + paramsRequest.getLocaleString("max") + ":</td><td>" + selectedPool.getMaxConn() + "</td></tr>");
            out.println("    <tr><td>" + paramsRequest.getLocaleString("password") + ":</td><td>" + selectedPool.getPassword() + "</td>");
            out.println("        <td>Hits:</td><td>" + selectedPool.getHits() + "</td></tr>");
            out.println("  </table>");
            out.println("</fieldset>");
            out.println("<fieldset>");
            
            SWBResourceURL url1 =  paramsRequest.getActionUrl();
            url1.setParameter("dbcon", dbcon);
            url1.setParameter("clear", "clear");
            out.println("  <button dojoType=\"dijit.form.Button\" name=\"clear\" onclick=\"location.href='"+url1+"'\" >" + paramsRequest.getLocaleString("clear") +" " + selectedPool.getName() + "</button>");
            //SWBResourceURL url2 =  paramsRequest.getRenderUrl();
            url.setParameter("dbcon", dbcon);
            url.setParameter("reload", "reload");
            out.println("  <button dojoType=\"dijit.form.Button\" name=\"reload\" onclick=\"location.href='"+url+"'\" name=\"gc\">" + paramsRequest.getLocaleString("update") + "</button>");
            
            out.println("</fieldset>");
            out.println("</div>");
            
            if (SWBPlatform.getEnv("wb/systemMonitor", "false").equals("true")) {
                //SWBResourceURL url;
                url = paramsRequest.getRenderUrl().setCallMethod(SWBResourceURL.Call_DIRECT);
                
                out.println("<div class=\"swbform\">");
                out.println("<fieldset>");
                
                out.println("<div style=\"width:100%\">");
                out.println("  <div id=\"chartM_div\" style=\"width:100%; border:1px solid #ccc\"></div>");
                out.println("  <div id=\"chartT_div\" style=\"width:100%; border:1px solid #ccc\"></div>");
                out.println("  <div id=\"chartS_div\" style=\"width:100%; border:1px solid #ccc\"></div>");
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
                out.println("    var jsonData = postSyncHtml_('"+url.setMode(Mode_ACTIVITY).toString()+"');");
                out.println("    var jsonObj = window.JSON.parse(jsonData);");
                out.println("    dataM = new google.visualization.DataTable();");
                out.println("    dataM.addColumn('datetime','"+paramsRequest.getLocaleString("lblInstant")+"');");
                out.println("    dataM.addColumn('number',  '"+paramsRequest.getLocaleString("total")+"');");
                out.println("    dataM.addColumn('number',  '"+paramsRequest.getLocaleString("free")+"');");
                out.println("    dataM.addColumn('number',  '"+paramsRequest.getLocaleString("used")+"');");
                out.println("    dojo.forEach(jsonObj, function(entry, i){");
                out.println("      dataM.addRow([new Date(entry.y,entry.Mo,entry.d,entry.h,entry.mi,entry.s)");
                out.print("        ,entry.tm");
                out.print("        ,entry.fm");
                out.println("      ,entry.um]);");
                out.println("    });");

                out.println("    optionsM = {");
                out.println("      title: '"+paramsRequest.getLocaleString("connPool")+"',");
                out.println("      width: '90%',");
                out.println("      height: 250,");
                out.println("      legend: { position: 'center' },");
                out.println("      chartArea:{left:100,top:40, bottom:40, width:'75%',height:150},");
                out.println("      hAxis: {title: '"+paramsRequest.getLocaleString("msgTimeOfDay")+"', titleTextStyle: {color:'#262626',bold:true,italic:false}, textStyle: {color: '#336799', fontSize:10}, format: 'hh:mm:ss', gridlines: {count: 10}},");
                out.println("      vAxis: {title: '"+paramsRequest.getLocaleString("total")+"', titleTextStyle: {color:'#262626',bold:true,italic:false}, textStyle: {color: '#336799', fontSize:10}, minValue: 0, format: 'decimal', gridlines: {count: 10}},");
                out.println("      colors: ['#de3163', '#708a2b', '#3c6090'],");
                out.println("      explorer: { actions: ['dragToZoom', 'rightClickToReset'], axis: 'horizontal', maxZoomIn:16}");
                out.println("    };");

                out.println("    chartM = new google.visualization.LineChart(document.getElementById('chartM_div'));");
                out.println("    chartM.draw(dataM, optionsM);");
                out.println("  }");

                out.println("  function drawChartT() {");
                out.println("    var jsonData = postSyncHtml_('"+url.setMode(Mode_HITSTIME).toString()+"');");
                out.println("    var jsonObj = window.JSON.parse(jsonData);");
                out.println("    dataT = new google.visualization.DataTable();");
                out.println("    dataT.addColumn('datetime', '"+paramsRequest.getLocaleString("msgTimeOfDay")+"');");
                out.println("    dataT.addColumn('number', '"+paramsRequest.getLocaleString("hits")+"');");
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
                out.println("      vAxis: {title: '"+paramsRequest.getLocaleString("hits")+"', titleTextStyle: {color:'#262626',bold:true,italic:false}, textStyle: {color: '#336799', fontSize:10}, minValue: 0, format: 'decimal', gridlines: {count: 10}},");
                out.println("      explorer: { actions: ['dragToZoom', 'rightClickToReset'], axis: 'horizontal', maxZoomIn:16}");
                out.println("    };");

                out.println("    chartT = new google.visualization.LineChart(document.getElementById('chartT_div'));");
                out.println("    chartT.draw(dataT, optionsT);");
                out.println("  }");

                out.println("  function drawChartS() {");
                out.println("    var jsonData = postSyncHtml_('"+url.setMode(Mode_TIME).toString()+"');");
                out.println("    var jsonObj = window.JSON.parse(jsonData);");
                out.println("    dataS = new google.visualization.DataTable();");
                out.println("    dataS.addColumn('datetime', '"+paramsRequest.getLocaleString("msgTimeOfDay")+"');");
                out.println("    dataS.addColumn('number', '"+paramsRequest.getLocaleString("hitsTime")+"');");
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
                out.println("      vAxis: {title: '"+paramsRequest.getLocaleString("hitsTime")+"', titleTextStyle: {color:'#262626',bold:true,italic:false}, textStyle: {color: '#336799', fontSize:10}, minValue: 0, format: 'decimal', gridlines: {count: 10}},");
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
                out.println("    var jsonData = postSyncHtml_('"+url.setMode(Mode_ACTIVITY).toString()+"');");
                out.println("    var jsonObj = window.JSON.parse(jsonData);");
                out.println("    dataM = new google.visualization.DataTable();");
                out.println("    dataM.addColumn('datetime','"+paramsRequest.getLocaleString("lblInstant")+"');");
                out.println("    dataM.addColumn('number',  '"+paramsRequest.getLocaleString("total")+"');");
                out.println("    dataM.addColumn('number',  '"+paramsRequest.getLocaleString("free")+"');");
                out.println("    dataM.addColumn('number',  '"+paramsRequest.getLocaleString("used")+"');");
                out.println("    dojo.forEach(jsonObj, function(entry, i){");
                out.println("      dataM.addRow([new Date(entry.y,entry.Mo,entry.d,entry.h,entry.mi,entry.s)");
                out.print("        ,entry.tm");
                out.print("        ,entry.fm");
                out.println("      ,entry.um]);");
                out.println("    });");

                out.println("    jsonData = postSyncHtml_('"+url.setMode(Mode_HITSTIME).toString()+"');");
                out.println("    jsonObj = window.JSON.parse(jsonData);");
                out.println("    dataT = new google.visualization.DataTable();");
                out.println("    dataT.addColumn('datetime', '"+paramsRequest.getLocaleString("msgTimeOfDay")+"');");
                out.println("    dataT.addColumn('number', '"+paramsRequest.getLocaleString("hits")+"');");
                out.println("    dojo.forEach(jsonObj, function(entry, i){");
                out.println("      dataT.addRow([new Date(entry.y,entry.Mo,entry.d,entry.h,entry.mi,entry.s),entry.v]);");
                out.println("    });");

                out.println("    jsonData = postSyncHtml_('"+url.setMode(Mode_TIME).toString()+"');");
                out.println("    jsonObj = window.JSON.parse(jsonData);");
                out.println("    dataS = new google.visualization.DataTable();");
                out.println("    dataS.addColumn('datetime', '"+paramsRequest.getLocaleString("msgTimeOfDay")+"');");
                out.println("    dataS.addColumn('number', '"+paramsRequest.getLocaleString("hitsTime")+"');");
                out.println("    dojo.forEach(jsonObj, function(entry, i){");
                out.println("      dataS.addRow([new Date(entry.y,entry.Mo,entry.d,entry.h,entry.mi,entry.s),entry.v]);");
                out.println("    });");

                out.println("    chartM.draw(dataM, optionsM);");
                out.println("    chartT.draw(dataT, optionsT);");
                out.println("    chartS.draw(dataS, optionsS);");
                out.println("  }");
                out.println("  window.setInterval(getLiveDataSet, 5000);");
                out.println("</script>");
                
                out.println("</fieldset>");
                out.println("</div>");
            }

            ConcurrentHashMap timepool = (ConcurrentHashMap) timelock.getPools().get(selectedPool.getName());
            if (timepool != null) {
                HashMap pool2 = new HashMap(timepool);
                int ps = pool2.size();
                if (ps > 0) {
                    out.println("<div class=\"swbform\">");
                    out.println("<fieldset>");
                    out.println("  <table width=\"100%\" border=0>");
                    out.println("    <tr><td colspan=4><strong>"
                            + paramsRequest.getLocaleString("connDetails")
                            + "</strong></td></tr>");
                    out.println("    <tr><td colspan=4>");
                    out.println("      <table width=\"100%\" border=0>");
                    out.println("        <tr><td>ID</td><td>"
                            + paramsRequest.getLocaleString("description")
                            + "</td><td>"
                            + paramsRequest.getLocaleString("time")
                            + "</td></tr>");

                    long actual = System.currentTimeMillis();
                    Iterator it2 = pool2.keySet().iterator();
                    while (it2.hasNext()) {
                        Long time = (Long) it2.next();
                        String desc = (String) pool2.get(time);
                        long seg = (actual - time) / 1000;
                        out.println("        <tr ");
                        if ((time + 300000L) < actual) {
                            out.print("bgcolor=\"#FFA0A0\"");
                        }
                        out.println("><td>" + time
                                + "</td><td>" + desc
                                + "</td><td>" + seg
                                + "s</td></tr>");
                    }
                    out.println("      </table>");
                    out.println("    </td></tr>");
                    out.println("  </table>");
                    out.println("</fieldset>");
                    out.println("</div>");
                }        
            }
        } else {
            out.println(paramsRequest.getLocaleString("nopool"));
        }
    }

    /**
     * Gets the data used into the chart.
     * 
     * @param request the request
     * @param response the response
     * @param paramsRequest the params request
     * @throws SWBResourceException, a Resource Exception
     * @throws IOException, an In Out Exception
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
        java.util.Date date;
        Calendar cal;
        StringBuilder json = new StringBuilder();
        
        PrintWriter out = response.getWriter();
        String dbcon = request.getParameter("dbcon");
        if(dbcon == null) {
            dbcon = SWBPlatform.getEnv("wb/db/nameconn", "swb");
        }

        int max = 100;
        int ratio = ((SWBPortal.getMonitor().getMonitorRecords().size() - 1) / max) + 1;
        Vector data = SWBPortal.getMonitor().getAverageMonitorRecords(ratio);
        int labc = data.size() / 10;
        if (labc == 0) {
            labc = 1;
        }
        
        Enumeration en = data.elements();
        int x = -1;
        long total = 0;
        long free = 0;
        long used = 0;
        json.append("[");
        while (en.hasMoreElements()) {
            SWBMonitor.MonitorRecord mr = (SWBMonitor.MonitorRecord) en.nextElement();
            SWBMonitor.PoolRecord pr = (SWBMonitor.PoolRecord) mr.getPools().get(dbcon);
            total = pr.getTotalConnections();
            free = pr.getFreeConnections();
            used = pr.getUsedConnections();
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
     * Gets the data used into the chart.
     * 
     * @param request the request
     * @param response the response
     * @param paramsRequest the params request
     * @throws SWBResourceException, a Resource Exception
     * @throws IOException, an In Out Exception
     */
    public void getData2(HttpServletRequest request,
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
        java.util.Date date;
        Calendar cal;
        StringBuilder json = new StringBuilder();
        
        PrintWriter out = response.getWriter();
        String dbcon = request.getParameter("dbcon");
        if(dbcon == null) {
            dbcon = SWBPlatform.getEnv("wb/db/nameconn", "swb");
        }

        int max = 100;
        int ratio = ((SWBPortal.getMonitor().getMonitorRecords().size() - 1) / max) + 1;
        Vector data = SWBPortal.getMonitor().getAverageMonitorRecords(ratio);
        int labc = data.size() / 10;
        if(labc == 0) {
            labc = 1;
        }
        Enumeration en = data.elements();
        int x = -1;
        json.append("[");
        while (en.hasMoreElements()) {
            SWBMonitor.MonitorRecord mr = (SWBMonitor.MonitorRecord) en.nextElement();
            SWBMonitor.PoolRecord pr = (SWBMonitor.PoolRecord) mr.getPools().get(dbcon);
            long hits = pr.getHits();
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
                json.append("\"v\":"+hits);
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
     * Gets the data used into the chart.
     * 
     * @param request the request
     * @param response the response
     * @param paramsRequest the params request
     * @throws SWBResourceException, a Resource Exception
     * @throws IOException, an In Out Exception
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
        java.util.Date date;
        Calendar cal;
        StringBuilder json = new StringBuilder();
        
        PrintWriter out = response.getWriter();
        String dbcon = request.getParameter("dbcon");
        if(dbcon == null) {
            dbcon = SWBPlatform.getEnv("wb/db/nameconn", "swb");
        }
        
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
            SWBMonitor.PoolRecord pr = (SWBMonitor.PoolRecord) mr.getPools().get(dbcon);
            long val = pr.getHitsTime();
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
     * Process the user request mode to show.
     * 
     * @param request the request
     * @param response the response
     * @param paramsRequest the params request
     * @throws SWBResourceException, a Resource Exception
     * @throws IOException, an In Out Exception
     */    
    @Override
    public void processRequest(HttpServletRequest request,
            HttpServletResponse response, SWBParamRequest paramsRequest)
            throws SWBResourceException, IOException {
        final String mode = paramsRequest.getMode();
        
        if(null != mode) {
            switch (mode) {
                case Mode_ACTIVITY:
                    getData(request, response, paramsRequest);
                    break;
                case Mode_HITSTIME:
                    hitsTime(request, response, paramsRequest);
                    break;
                case Mode_TIME:
                    getData2(request, response, paramsRequest);
                    break;
                default:
                    super.processRequest(request, response, paramsRequest);
                    break;
            }
        }else {
            super.processRequest(request, response, paramsRequest);
        }
    }    
    
}
