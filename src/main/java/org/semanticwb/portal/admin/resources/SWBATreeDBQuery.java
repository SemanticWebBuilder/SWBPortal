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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticwb.Logger;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBUtils;
import org.semanticwb.base.db.DBConnectionPool;
import org.semanticwb.model.SWBContext;
import org.semanticwb.model.User;
import org.semanticwb.portal.api.GenericResource;
import org.semanticwb.portal.api.SWBParamRequest;
import org.semanticwb.portal.api.SWBResourceException;

/**
 * Recurso para la administración de WebBuilder que muestra el árbol con el pool
 * de conexiones configuradas en SemanticWebbuilder, que muestran las tablas de las bases de datos con sus columnas.
 *
 * Tree of PoolConnections available on SemanticWebbuilder configuration, shows the existing tables and columns.
 *
 * @author Juan Antonio Fernandez
 * @author Hasdai Pacheco
 */
public class SWBATreeDBQuery extends GenericResource {
    /** The log. */
    private Logger log = SWBUtils.getLogger(SWBATreeDBQuery.class);
   
    //public static final String WBGLOBAL="WBGlobal";
    
    /** The Constant WBADMIN. */
    public static final String WBADMIN=SWBContext.WEBSITE_ADMIN;

    //TODO:Provisional hasta que este AdmFilterMgr
    /** The Constant NO_ACCESS. */
    public static final int NO_ACCESS = 0;
    
    /** The Constant PARCIAL_ACCESS. */
    public static final int PARCIAL_ACCESS = 1;
    
    /** The Constant FULL_ACCESS. */
    public static final int FULL_ACCESS = 2;
    
    private static String[] operValidas = { "select" };
    public static final String MODE_GETTREEDATA = "getTreeData";
    public static final String MODE_EXECUTEQUERY = "execQuery";

    public SWBATreeDBQuery() {
    	//Get allowed DB operations
    	String op = SWBPlatform.getEnv("wb/resDBQueryFilter","select").trim();
    	if (op.contains(",")) {
    		operValidas = op.split(",");
    	} else if (op.contains(";")) {
    		operValidas = op.split(";");
    	}
    	
    	for (int i = 0; i < operValidas.length; i++) {
    		operValidas[i] = operValidas[i].trim();
    	}
	}
    
    /**
     * Process request.
     * 
     * @param request the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @param paramRequest the {@link SWBParamRequest}
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws SWBResourceException the {@link SWBResourceException}
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
    	String mode = paramRequest.getMode();
        if (MODE_GETTREEDATA.equals(mode)) {
        	doGetTreeData(request, response, paramRequest);
        } else if (MODE_EXECUTEQUERY.equals(mode)) {
        	doExecuteQuery(request, response, paramRequest);
        } else {
        	super.processRequest(request,response,paramRequest);
        }
    }
   
    /**
     * Manages request of tree data.
     * @param request {@link HttpServletRequest} object
     * @param response {@link HttpServletResponse} object
     * @param paramRequest {@link SWBParamRequest} object
     * @throws SWBResourceException
     * @throws IOException
     */
    public void doGetTreeData(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
    	response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();
        
        JSONArray ret = getTreeData();
        out.println(ret.toString());
    }
    
    /**
     * Manages query execution requests.
     * @param request {@link HttpServletRequest} object
     * @param response {@link HttpServletResponse} object
     * @param paramRequest {@link SWBParamRequest} object
     * @throws SWBResourceException
     * @throws IOException
     */
    public void doExecuteQuery(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
    	response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();
        BufferedReader reader = request.getReader();
        
        //Read request stream
        String line = null;
        StringBuilder body = new StringBuilder();
        while((line = reader.readLine()) != null) {
            body.append(line);
        }
        reader.close();
        
        JSONArray ret = new JSONArray();
        //Get JSON payload and process queries
        try {
            JSONObject payload = new JSONObject(body.toString());
            String queryString = payload.getString("query");
            String poolName = payload.optString("pool", null);

            HashMap<String, Object> queries = getExecutableQueries(queryString, paramRequest);
            if (!queries.isEmpty()) {
            	Connection conn = null;
            	if (null != poolName && !poolName.isEmpty()) {
            		conn = SWBUtils.DB.getConnection(poolName, "SWBATreeDBQuery");
            	} else {
            		conn = SWBUtils.DB.getDefaultConnection("SWBATreeDBQuery");
            	}
            	
            	if (null == conn) conn = SWBUtils.DB.getDefaultConnection("SWBATreeDBQuery");
            	//Execute queries
	            for (String q: queries.keySet()) {
	            	JSONObject qRes = new JSONObject();
            		qRes.put("query", q);
            		
	            	boolean canExecute = (Boolean)queries.get(q);
	            	if (canExecute) {
	            		try {
		            		Statement st = conn.createStatement();
		            		Long sTime = System.currentTimeMillis();
		            		if(q.toLowerCase().startsWith("delete") || q.toLowerCase().startsWith("insert")
		            				|| q.toLowerCase().startsWith("update") || q.toLowerCase().startsWith("drop")
		            				|| q.toLowerCase().startsWith("alter") || q.toLowerCase().startsWith("create")) {
		            			int affectedRows = st.executeUpdate(q);
		            			qRes.put("affectedRows", affectedRows);
		            		} else {
		            			ResultSet r = st.executeQuery(q);
		            			qRes.put("columns", getResultSetColumnsJSON(r));
		            			qRes.put("data", getResultsetRowsJSON(r));
		            		}
		            		long eTime = System.currentTimeMillis() - sTime;
		            		qRes.put("execTime", eTime > 1000 ? (eTime/1000) + "s" : eTime + "ms");
	            		} catch (SQLException sqlex) {
	            			qRes.put("error", sqlex.getMessage().replace("\n", "\\n").replace("\r", "\\r"));
	            			//qRes.put("trace", sqlex.getStackTrace().toString().replace("\n", "\\n").replace("\r", "\\r"));
	            		}
	            	} else {
	            		qRes.put("error", paramRequest.getLocaleString("errOperNotAllowed"));
	            	}
	            	
	            	ret.put(qRes);
	            }
            }
        } catch (JSONException jsex) {
        	log.error("Cannot parse request body", jsex);
        }

        out.println(ret.toString());
    }

    @Override
    public void doView(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
        String jsp = "/swbadmin/jsp/SWBADBQuery/view.jsp";
        
        RequestDispatcher rd = request.getRequestDispatcher(jsp);
        try {
            request.setAttribute("paramRequest", paramRequest);
            rd.include(request, response);
        } catch (ServletException sex) {
            log.error("SWBAFilters - Error including view", sex);
        }
    }
    
    /**
     * Gets JSON data for building resource tree.
     * @return {@link JSONArray} of final nodes.
     */
    private JSONArray getTreeData() {
    	JSONArray ret = new JSONArray();
        int access = PARCIAL_ACCESS; //modificar
        
        try {
	        JSONObject root = createObj("ConnectionPool", null, "swbIconServer");
	        root.put("uuid", "rootNode");
	        ret.put(root);
	        
	        // Pool de conexiones
	        Enumeration<DBConnectionPool> en = SWBUtils.DB.getPools();
	        while(en.hasMoreElements()) {
	            DBConnectionPool pool = en.nextElement();
	            JSONObject poolObj = createObj(pool.getName(), root.getString("uuid"), "swbIconTemplateGroup");
	            ret.put(poolObj);
	            getTablesData(ret, poolObj);
	        }
        } catch (JSONException ex) {
        	log.error("Error getting json data", ex);
        }
    	
    	return ret;
    }
    
    /**
     * Gets information of database tables.
     * @param ret {@link JSONArray} of final nodes.
     * @param root Wrapper for PoolConection.
     */
    private void getTablesData(JSONArray ret, JSONObject root) {
    	String [] as = { "TABLE" };
    	String poolName = null;
    	if (null != root) poolName = root.optString("name", null);
    	
    	if (null != poolName) {
    		try {
            	Connection conn = SWBUtils.DB.getConnection(poolName, "SWBATreeDBQuery.addPoolConn");
            	DatabaseMetaData metadata = conn.getMetaData();
            	ResultSet rstable = metadata.getTables(null, null, null, as);
            	   	
            	while (rstable.next()) {
            		String tblName = rstable.getString("TABLE_NAME");
            		JSONObject tableObj = createObj(tblName, root.getString("uuid"), "swbIconWebPage");
            		ret.put(tableObj);
            		getTableProperties(ret, metadata, tableObj, true);
            		getTableProperties(ret, metadata, tableObj, false);
                    //addTable(user, rstable.getString("TABLE_NAME"), poolconn, dbcon);
                }
            	rstable.close();
                if(conn!=null)conn.close();
    		} catch (SQLException sqex) {
    			log.error("Error getting poolConnection data", sqex);
    		} catch (JSONException jsex) {
    			log.error("Error getting json data", jsex);
    		}
    	}
    }
    
    /**
     * Gets table column information for a given table.
     * @param ret {@link JSONArray} of final nodes.
     * @param metadata {@link DatabaseMetaData} object from connection.
     * @param tblObj Wrapper object for table.
     * @param primary whether to get properties for primary keys.
     * @throws SQLException
     */
    private void getTableProperties(JSONArray ret, DatabaseMetaData metadata, JSONObject tblObj, boolean primary) throws SQLException {
    	ResultSet cols = null;
    	String cssIcon = "swbIconX509Certificate";
    	JSONObject rootObj = tblObj;
    	
    	if (primary) {
    		rootObj = createObj("PrimaryKeys", tblObj.getString("uuid"), cssIcon);
    		cols = metadata.getPrimaryKeys(null,  null, tblObj.getString("name"));
    		ret.put(rootObj);
    	} else {
    		cssIcon = "swbIconWebPageV";
    		cols = metadata.getColumns(null, null, tblObj.getString("name"), null);
    	}
    	
        while (cols.next()) {
            String column = cols.getString(4);
            JSONObject pkeyObj = createObj(column, rootObj.getString("uuid"), cssIcon);
            ret.put(pkeyObj);
            
            JSONObject dtProp = createObj("Data Type: "+cols.getString(5), pkeyObj.getString("uuid"), "swbIconWebPageV");
            JSONObject tnProp = createObj("Type Name: "+cols.getString(6), pkeyObj.getString("uuid"), "swbIconWebPageV");
            ret.put(dtProp);
            ret.put(tnProp);
            
            if (!primary) {
            	JSONObject szProp = createObj("Size: "+cols.getString(7), pkeyObj.getString("uuid"), "swbIconWebPageV");
            	JSONObject defProp = createObj("Default Value: "+cols.getString(13), pkeyObj.getString("uuid"), "swbIconWebPageV");
            	JSONObject nullProp = createObj("Allows NULL: "+cols.getString(18), pkeyObj.getString("uuid"), "swbIconWebPageV");

            	ret.put(szProp);
            	ret.put(defProp);
            	ret.put(nullProp);
            }
        }
        
        cols.close();
    }
    
    /**
     * Gets a {@link HashMap} of executable queries from a single queryString.
     * @param queryString {@link String} containing client query string.
     * @param paramRequest {@link SWBParamRequest} object
     * @return {@link HashMap} containing analyzed queries with value of true or false for execution.
     */
    private HashMap<String, Object> getExecutableQueries(String queryString, SWBParamRequest paramRequest) {
    	HashMap<String, Object> ret = new HashMap<>();
    	if(null == queryString || queryString.trim().isEmpty()) return ret;

    	//Check user permissions on site
    	boolean canExecute = SWBContext.getAdminWebSite().equals(paramRequest.getWebPage().getWebSite()) && null != SWBContext.getAdminUser();
    	User user = paramRequest.getUser();
    	canExecute = canExecute && user.isRegistered() && user.isSigned();
    	
    	if (canExecute) {
	    	//Get queries
    		String queries [] = null;
	    	String q = queryString.trim();
	    	if (q.contains(";")) { //More than one sentence
	    		queries = q.split(";");
	    	} else {
	    		queries = new String[1];
	    		queries[0] = q;
	    	}
	    	
	    	//Check allowed operations per query
	    	for (String query: queries) {
	    		query = query.trim();
	    		canExecute = false;
	    		for(String op : operValidas) {
	    			if (!query.isEmpty()) { //Ignore empty queries
		    			if (query.toLowerCase().startsWith(op)) {
		    				canExecute = true;
		    				break;
		    			}
	    			}
	    		}
	    		
	    		if (canExecute) {
	    			ret.put(query, Boolean.TRUE);
	    		} else {
	    			ret.put(query, Boolean.FALSE);
	    		}
	    	}
    	}
    	
    	return ret;
    }
    
    /**
     * Gets {@link ResultSet} columns from its {@link ResultSetMetaData}.
     * @param rs {@link ResultSet}
     * @return {@link JSONArray} with a {@link JSONObject} for each column in metadata.
     */
    private JSONArray getResultSetColumnsJSON(ResultSet rs) {
    	JSONArray cols = new JSONArray();
    	try {
	    	ResultSetMetaData md = rs.getMetaData();
	    	
	    	//Get columns
	    	for (int i = 1; i <= md.getColumnCount(); i++) {
	    		JSONObject col = new JSONObject();
	    		col.put("name", md.getColumnName(i));
	    		col.put("type", md.getColumnTypeName(i));
	    		cols.put(col);
	    	}
    	} catch (SQLException sqlex) {
    		
    	}
    	return cols;
    }
    
    /**
     * Gets a {@link JSONArray} with the content of the {@link ResultSet} 
     * @param rs {@link ResultSet}
     * @return {@link JSONArray} with a {@link JSONObject} wrapping each record.
     */
    private JSONArray getResultsetRowsJSON(ResultSet rs) {
    	JSONArray rows = new JSONArray();
    	
    	try {
	    	ResultSetMetaData md = rs.getMetaData();

	    	//Get rows
	    	while(rs.next()) {
	    		JSONObject record = new JSONObject();
	    		for (int i = 1; i <= md.getColumnCount(); i++) {
	    			record.put(md.getColumnName(i), rs.getString(i));
	    		}
	    		rows.put(record);
	    	}
    	} catch (SQLException sqlex) {
    		
    	}
    	
    	return rows;
    }
    
    /**
     * Creates a {@link JSONObject} with a random UUID field, and name, parent and cssIcon properties provided.
     * @param name Name field
     * @param parent Parent field
     * @param cssIcon cssIcon field
     * @return {@link JSONObject} with provided fields
     */
    private JSONObject createObj(String name, String parent, String cssIcon) {
    	JSONObject obj = new JSONObject();
    	
    	try {
    		obj.put("uuid", UUID.randomUUID().toString());
    		if (null != name && !name.isEmpty()) obj.put("name", name);
    		if (null != name && !name.isEmpty()) obj.put("parent", parent);
    		if (null != name && !name.isEmpty()) obj.put("cssIcon", cssIcon);
    	} catch (JSONException jsex) {
    		log.error("Error creating json object for node");
    	}
    	return obj;
    }
}
