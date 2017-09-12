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
 * dirección electrónica:
 *  http://www.semanticwebbuilder.org.mx
 */
package org.semanticwb.portal.admin.resources;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.semanticwb.Logger;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBUtils;
import org.semanticwb.model.Country;
import org.semanticwb.model.Device;
import org.semanticwb.model.DisplayProperty;
import org.semanticwb.model.Language;
import org.semanticwb.model.Role;
import org.semanticwb.model.Rule;
import org.semanticwb.model.SWBContext;
import org.semanticwb.model.SWBRuleMgr;
import org.semanticwb.model.User;
import org.semanticwb.model.UserGroup;
import org.semanticwb.model.UserRepository;
import org.semanticwb.model.WebSite;
import org.semanticwb.platform.SemanticOntology;
import org.semanticwb.platform.SemanticProperty;
import org.semanticwb.portal.api.GenericResource;
import org.semanticwb.portal.api.SWBActionResponse;
import org.semanticwb.portal.api.SWBParamRequest;
import org.semanticwb.portal.api.SWBResourceException;
import org.semanticwb.portal.api.SWBResourceModes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Admin resource to manage and edit SWBRules.
 * 
 * @author juan.fernandez
 * @author Hasdai Pacheco
 */
public class SWBARule extends GenericResource {

    /** The log. */
    private Logger log = SWBUtils.getLogger(SWBARule.class);
    
    /** The sb tree. */
    StringBuilder sbTree = null;
    
    /** The combo att. */
    HashMap comboAtt = null;
    
    /** The vec order att. */
    Vector vecOrderAtt = null;
    
    /** The local doc. */
    Document localDoc = null;
    
    /** The elem num. */
    int elemNum = 0;
    
    /** The xml attr. */
    String xmlAttr = null;

    /**
     * Creates a new instance of SWBARule resource.
     */
    public SWBARule() {}

    /**
     * Add, update or remove rules.
     * 
     * @param request input parameters
     * @param response an answer to the request
     * @throws SWBResourceException an SWB Resource Exception
     * @throws IOException an IO Exception
     */
    @Override
    public void processAction(HttpServletRequest request, SWBActionResponse response) throws SWBResourceException, IOException {
        String accion = request.getParameter("act");
        String id = request.getParameter("id");
        User user = response.getUser();
        String tmparam = request.getParameter("tm");
        try {
            if (id != null) {
                if (accion.equals("removeit")) {
                    try {
                        SWBContext.getWebSite(tmparam).removeRule(id);
                        response.setCallMethod(SWBActionResponse.Call_CONTENT);
                        response.setMode(SWBActionResponse.Mode_VIEW);

                        response.setRenderParameter("act", "removemsg");
                        response.setRenderParameter("status", "true");
                    } catch (Exception e) {
                        response.setRenderParameter("act", "notremovemsg");
                        response.setRenderParameter("status", "true");

                    }
                    response.setMode(SWBActionResponse.Mode_VIEW);
                }
                if (accion.equals("update")) {
                    String tmsid = "global";
                    if (request.getParameter("tmsid") != null) {
                        tmsid = request.getParameter("tmsid");
                    }
                    WebSite ptm = SWBContext.getWebSite(tmsid);
                    Rule rRule = ptm.getRule(id);
                    rRule.setTitle(request.getParameter("title"));
                    rRule.setDescription(request.getParameter("description"));
                    rRule.setModifiedBy(user);

                    response.setRenderParameter("act", "edit");
                    response.setRenderParameter("id", id);
                    response.setRenderParameter("tree", "reload");
                    response.setRenderParameter("tmsid", tmsid);
                    response.setRenderParameter("lastTM", request.getParameter("lastTM"));
                    response.setRenderParameter("tm", request.getParameter("tmsid"));
                    if (request.getParameter("tp") != null) {
                        response.setRenderParameter("tp", request.getParameter("tp"));
                    }
                    if (request.getParameter("title") != null) {
                        response.setRenderParameter("title", request.getParameter("title"));
                    }
                    response.setMode(SWBActionResponse.Mode_EDIT);
                }

                if (accion.trim().equalsIgnoreCase("editadd")) {
                    try {
                        Document newRuleDoc = SWBUtils.XML.xmlToDom("<?xml version=\"1.0\" encoding=\"UTF-8\"?><rule/>");
                        String tmsid = "global";
                        if (request.getParameter("tmsid") != null) {
                            tmsid = request.getParameter("tmsid");
                        }
                        WebSite ptm = SWBContext.getWebSite(tmsid);
                        Rule newRule = ptm.createRule(); 
                        newRule.setTitle(request.getParameter("title"));
                        newRule.setDescription(request.getParameter("description"));
                        newRule.setXml(SWBUtils.XML.domToXml(newRuleDoc));
                        newRule.setCreator(user);

                        String idnew = newRule.getId();
                        response.setRenderParameter("act", "details");
                        response.setRenderParameter("id", idnew);
                        response.setRenderParameter("tree", "reload");
                        response.setRenderParameter("tmsid", tmsid);
                        response.setRenderParameter("tm", tmparam);
                        if (request.getParameter("tm") != null) {
                            response.setRenderParameter("tm", request.getParameter("tm"));
                        }
                        if (request.getParameter("tp") != null) {
                            response.setRenderParameter("tp", request.getParameter("tp"));
                        }
                        if (request.getParameter("title") != null) {
                            response.setRenderParameter("title", request.getParameter("title"));
                        }
                        response.setMode(SWBActionResponse.Mode_EDIT);
                    } catch (Exception ei) {
                        log.error(response.getLocaleString("msgErrorAddNewRule") + ". WBARules.processAction", ei);
                    }
                }
            }
        } catch (Exception ee) {
            log.error(ee);
        }
    }

    /**
     * Process request.
     * 
     * @param request the request
     * @param response the response
     * @param paramRequest the param request
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws SWBResourceException the sWB resource exception
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
    	String mode = paramRequest.getMode();
    	if ("editRule".equals(mode)) {
        	doEditRule(request, response, paramRequest);
        } else if ("getRuleFilters".equals(mode)) {
        	doGetRuleFilters(request, response, paramRequest);
        } else if ("updateRuleDefinition".equals(mode)) {
        	doUpdateRuleDefinition(request, response, paramRequest);
        } else if ("getRuleDefinition".equals(mode)) {
        	doGetRuleJson(request, response, paramRequest);
        } else {
            super.processRequest(request, response, paramRequest);
        }
    }

    /**
     * Processes requests to update rule definition object
     * @param request
     * @param response
     * @param paramRequest
     * @throws SWBResourceException
     * @throws IOException
     */
    public void doUpdateRuleDefinition(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
    	response.setContentType("text/html; charset=UTF-8");
    	PrintWriter out = response.getWriter();
    	User admUser = SWBContext.getAdminUser();
    	int callm = paramRequest.getCallMethod();
		
		if (callm != SWBResourceModes.Call_DIRECT || null == admUser) { //Can only be used with direct call and by admin user
			out.println("{\"status\":\"error\", \"msg\":\"permission denied\"}");
			return;
		}
		
		String tmparam = request.getParameter("tm");
		String ruleId = request.getParameter("id");
		WebSite site = SWBContext.getWebSite(tmparam);
		if (null != site && null != site.getRule(ruleId)) {
			Rule r = site.getRule(ruleId);
			//Get request body and generate rule XML
			String body = SWBUtils.IO.readInputStream(request.getInputStream(), "UTF-8");
			if (null != body) {
				JSONObject payload = new JSONObject(body);
				String xml = getRuleXML(payload);
				r.setXml(xml);
				out.println("{\"status\":\"ok\"}");
			}
		}
    }
    
    /**
     * Gets Rule element definitions recursively
     * @param doc Main DOM Document
     * @param parent current parent element
     * @param elemDef JSON definition of element
     */
    private void getRuleElements(Document doc, Element parent, JSONObject elemDef) {
    	boolean isNot = elemDef.optBoolean("not", false);
    	String type = elemDef.optString("condition");
    	Element el = null;
    	
    	if (type.isEmpty()) { //Add condition tag
    		el = doc.createElement(elemDef.getString("id"));
    		String op = elemDef.getString("operator");
    		String val = elemDef.getString("value");
    		
    		switch(op) {
    			case "equal":
    				op = "=";
    				break;
    			case "not_equal":
    				op = "!=";
    				break;
    			case "greater":
    				op = "&gt;";
    				break;
    			case "less":
    				op = "&lt;";
    				break;
    			case "history":
    				op = val;
    				break;
    		}
    		el.setAttribute("cond", op);
    		el.setTextContent(val);
    		parent.appendChild(el);
    	} else { //Add condition node
    		if (isNot) type = "not";
    		JSONArray rules = elemDef.getJSONArray("rules");
    		if (("AND".equals(type) || "OR".equals(type)) && rules.length() == 1) {
    			el = parent;
    		} else {
    			el = doc.createElement(type.toLowerCase());
    			parent.appendChild(el);
    		}
    		
	    	for (int i = 0; i < rules.length(); i++) {
	    		JSONObject rDef = rules.getJSONObject(i);
	    		getRuleElements(doc, el, rDef);
	    	}
    	}
    }
    
    /**
     * Gets JSON object containing rule definition as required by query-builder.
     * @param rule Rule to get definition from.
     * @return JSON object containing rule definition or null if rule is undefined.
     */
    private JSONObject getRuleJSON(Rule rule) {
    	Document dom = SWBUtils.XML.xmlToDom(rule.getXml());
    	Element root = (Element) dom.getElementsByTagName("rule").item(0);
    	JSONObject ret = null;
    	
    	if (null != root) {
    		NodeList childs = root.getChildNodes();
    		if (childs.getLength() > 0) {
    			root = (Element) childs.item(0);
    		} else {
    			return ret;
    		}
    	}
    	
    	return getElementsJSON(root);
    }

    /**
     * Gets subrule definitions
     * @param root Root element to get definitions from
     * @return Array with subrule definitions
     */
    private JSONArray getElementRules(Element root) {
    	NodeList childs = root.getChildNodes();
    	
    	if (childs.getLength() == 0) return null;
    	
    	JSONArray rules = new JSONArray();
    	for (int i = 0; i < childs.getLength(); i++) {
    		Element child = (Element) childs.item(i);
    		JSONObject c = getElementsJSON(child);
    		rules.put(c);
    	}
    	
    	return rules;
    }

    /**
     * Gets JSON node with subrule definition or condition definition
     * @param root Root element
     * @return JSON node with subrule definition or condition definition
     */
    private JSONObject getElementsJSON(Element root) {
    	JSONObject obj = new JSONObject();
    	String tagName = root.getTagName();
    	String types = "and|or|not";
    	boolean isNot = false;
    	
    	if (types.contains(tagName)) {
    		if("not".equals(tagName)) {
    			tagName = "and";
    			isNot = true;
    		}
    		obj.put("not", isNot);
    		obj.put("condition", tagName.toUpperCase());
    		JSONArray rules = getElementRules(root);
    		if (null != rules) obj.put("rules", rules);
    	} else {
    		obj.put("id", tagName);
    		String op = root.getAttribute("cond");
    		String val = root.getTextContent();
    		switch(op) {
    			case "=":
    				op = "equal";
    				break;
    			case "!=":
    				op = "not_equal";
    				break;
    			case "&gt;":
    				op = "greater";
    				break;
    			case "&lt;":
    				op = "less";
    				break;
    			default:
    				if (op.startsWith("-")) op = "history";
    				break;
    		}
    		obj.put("operator", op);
    		obj.put("value", val);
    	}
    	
    	return obj;
    }
    
    /**
     * Gets XML string for JSON rule definition
     * @param ruleDef JSON object for rule definition
     * @return XML representation of JSON rule definition
     */
    private String getRuleXML(JSONObject ruleDef) {
    	Document ret = SWBUtils.XML.xmlToDom("<?xml version=\"1.0\" encoding=\"UTF-8\"?><rule/>");
    	Element root = (Element) ret.getElementsByTagName("rule").item(0);
    	
    	getRuleElements(ret, root, ruleDef);

    	return SWBUtils.XML.domToXml(ret);
    }
    
    /**
     * Process requests to get rule configuration options (conditions, operators, values)
     * @param request
     * @param response
     * @param paramRequest
     * @throws SWBResourceException
     * @throws IOException
     */
    public void doGetRuleFilters(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
    	response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
    	
    	PrintWriter out = response.getWriter();
    	
    	out.print(getJSONComboAttr());
    }
    
    /**
     * Process requests to get rule configuration data
     * @param request
     * @param response
     * @param paramRequest
     * @throws SWBResourceException
     * @throws IOException
     */
    public void doGetRuleJson(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
    	response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
    	
        String rrid = request.getParameter("suri");
        Rule rRule = (Rule) SWBPlatform.getSemanticMgr().getOntology().getGenericObject(rrid);
        
    	PrintWriter out = response.getWriter();
    	JSONObject ret = getRuleJSON(rRule);
    	if (null != ret) {
    		out.print(ret.toString());
    	} else {
    		out.print("{}");
    	}
    }
    
    /**
     * Shows edit UI for rules
     * @param request
     * @param response
     * @param paramRequest
     * @throws SWBResourceException
     * @throws IOException
     */
    public void doEditRule(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
        SemanticOntology ont = SWBPlatform.getSemanticMgr().getOntology();
        String rrid = request.getParameter("suri");
        Rule rRule = (Rule) ont.getGenericObject(rrid);
        String tmsid = rRule.getWebSite().getId();
        if (tmsid == null) {
            tmsid = paramRequest.getWebPage().getWebSiteId();
        }

        log.debug("tm:"+tmsid);

        comboAtt = null;
        vecOrderAtt = null;
        loadComboAttr(tmsid, rRule.getURI(), paramRequest);
        
        String accion = request.getParameter("act");
        if (accion == null) {
            accion = "edit";
        }

        try {
            if (null != rRule) {
                if (accion.equals("edit") || accion.equals("details")) {
                    String xml = rRule.getXml();
                    if (null == xml) {
                        xml = "<rule/>";
                        rRule.setXml(xml);
                    }

                    Document docxml = SWBUtils.XML.xmlToDom(xml);
                    
                    if (docxml != null) {
                    	String jsp = "/swbadmin/jsp/SWBARules/view.jsp";
                    	RequestDispatcher rd = request.getRequestDispatcher(jsp);
                	
                		request.setAttribute("paramRequest", paramRequest);
                		request.setAttribute("ruleId", rRule.getId());
                		request.setAttribute("ruleModel", rRule.getSemanticObject().getModel().getName());
                		rd.include(request, response);
                    }
                }
            }
        } catch (Exception e) {
            log.error(paramRequest.getLocaleString("msgErrorEditRule") + ", WBARules.doEdit", e);
        }
    }

    /**
     * Edit view, Rule information edition.
     * 
     * @param request input parameters
     * @param response an answer to the request
     * @param paramRequest a list of objects (WebPage, user, action, ...)
     * @throws AFException an AF Exception
     * @throws IOException an IO Exception
     * @throws SWBResourceException the sWB resource exception
     */
    @Override
    public void doView(HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest) throws SWBResourceException, IOException {
    	PrintWriter out = response.getWriter();
        out.print("<iframe style=\"border:0px; width:100%; height:100%; position:absolute; top:0px; left:0px;\" frameborder=\"0\" scrolling=\"no\" src=\""+paramRequest.getRenderUrl().setMode("editRule").setParameter("suri", request.getParameter("suri"))+"\"></iframe>");
    }

    /**
     * load in a HashMap all user attributes and default attribute to creates rules.
     * 
     * @param tmid identificador de mapa de tópicos
     * @param ruleid identificador de la regla
     * @param paramRequest lista de objetos (WebPage, user, action, ...)
     * @throws SWBResourceException the sWB resource exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void loadComboAttr(String tmid, String ruleid, SWBParamRequest paramRequest) throws SWBResourceException, java.io.IOException {

        log.debug("loadComboAttr ruleid: " + ruleid + ", tmid: " + tmid);

        WebSite ws = SWBContext.getWebSite(tmid);
        User user = paramRequest.getUser();
        UserRepository usrRepo = ws.getUserRepository();
        comboAtt = new HashMap<>();
        vecOrderAtt = new Vector(1, 1);
        // Agreando valores iniciales al HashMap como son isloged, isregistered, language, device
        HashMap<String, Object> hmAttr = new HashMap<>();
        HashMap<String, String> hmOper = new HashMap<>();
        HashMap<String, String> hmValues = new HashMap<>();
        hmAttr.put("Etiqueta", paramRequest.getLocaleString("msgUserRegistered"));  ///////////////////////////
        hmAttr.put("Tipo", "select");
        hmOper.put("=", paramRequest.getLocaleString("msgSameAs"));
        hmOper.put("!=", paramRequest.getLocaleString("msgNotEqual"));
        hmAttr.put("Operador", hmOper);
        hmValues.put("true", paramRequest.getLocaleString("msgYes"));
        hmValues.put("false", paramRequest.getLocaleString("msgNo"));
        hmAttr.put("Valor", hmValues);
        comboAtt.put(SWBRuleMgr.TAG_INT_ISREGISTERED, hmAttr); //RuleMgr.TAG_INT_ISREGISTERED

        hmAttr = new HashMap<>();
        hmOper = new HashMap<>();
        hmValues = new HashMap<>();
        hmAttr.put("Etiqueta", paramRequest.getLocaleString("msgUserSigned"));   ///////////////////////////
        hmAttr.put("Tipo", "select");
        hmOper.put("=", paramRequest.getLocaleString("msgSameAs"));
        hmOper.put("!=", paramRequest.getLocaleString("msgNotEqual"));
        hmAttr.put("Operador", hmOper);
        hmValues.put("true", paramRequest.getLocaleString("msgYes"));
        hmValues.put("false", paramRequest.getLocaleString("msgNo"));
        hmAttr.put("Valor", hmValues);
        comboAtt.put(SWBRuleMgr.TAG_INT_ISSIGNED, hmAttr); //RuleMgr.TAG_INT_ISLOGED

        // DISPOSITIVOS POR SITIO
        hmAttr = new HashMap<>();
        hmOper = new HashMap<>();
        hmValues = new HashMap<>();
        hmAttr.put("Etiqueta", paramRequest.getLocaleString("msgDevice"));   ///////////////////////////
        hmAttr.put("Tipo", "select");
        Iterator<Device> eleDev = SWBContext.getWebSite(tmid).listDevices();
        while (eleDev.hasNext()) {
            Device rDev = eleDev.next();
            hmValues.put(rDev.getURI(), rDev.getDisplayTitle(user.getLanguage()));
        }
        hmAttr.put("Valor", hmValues);
        hmOper.put("=", paramRequest.getLocaleString("msgSameAs"));
        hmOper.put("!=", paramRequest.getLocaleString("msgNotEqual"));
        hmAttr.put("Operador", hmOper);
        comboAtt.put(SWBRuleMgr.TAG_INT_DEVICE, hmAttr); //RuleMgr.TAG_INT_DEVICE

        int numero = 0;
        vecOrderAtt.add(numero++, SWBRuleMgr.TAG_INT_ISREGISTERED); //RuleMgr.TAG_INT_ISREGISTERED
        vecOrderAtt.add(numero++, SWBRuleMgr.TAG_INT_ISSIGNED); //RuleMgr.TAG_INT_ISLOGED
        vecOrderAtt.add(numero++, SWBRuleMgr.TAG_INT_DEVICE); //RuleMgr.TAG_INT_DEVICE

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Grupo de usuarios
        try {
            hmAttr = new HashMap<>();
            hmOper = new HashMap<>();
            hmValues = new HashMap<>();
            hmAttr.put("Etiqueta", paramRequest.getLocaleString("msgUserGroups"));   ///////////////////////////
            hmAttr.put("Tipo", "select");
            Iterator<UserGroup> enumUsrG = usrRepo.listUserGroups();
            while (enumUsrG.hasNext()) {
                UserGroup usrGroup = enumUsrG.next();
                hmValues.put(usrGroup.getURI(), usrGroup.getDisplayTitle(user.getLanguage()));
            }
            hmAttr.put("Valor", hmValues);
            hmOper.put("=", paramRequest.getLocaleString("msgHave"));
            hmOper.put("!=", paramRequest.getLocaleString("msgNotHave"));
            hmAttr.put("Operador", hmOper);

            comboAtt.put(SWBRuleMgr.TAG_INT_USERGROUP, hmAttr); 
            vecOrderAtt.add(numero++, SWBRuleMgr.TAG_INT_USERGROUP); 

            // Se agrega la parte de roles
            hmAttr = new HashMap<>();
            hmOper = new HashMap<>();
            hmValues = new HashMap<>();
            hmAttr.put("Etiqueta", paramRequest.getLocaleString("msgRoles"));   ///////////////////////////
            hmAttr.put("Tipo", "select");
            hmOper.put("=", paramRequest.getLocaleString("msgHave"));
            hmOper.put("!=", paramRequest.getLocaleString("msgNotHave"));
            hmAttr.put("Operador", hmOper);
            Iterator<Role> enumRoles = SWBContext.getWebSite(tmid).getUserRepository().listRoles();
            while (enumRoles.hasNext()) {
                Role role = enumRoles.next();
                hmValues.put(role.getURI(), role.getDisplayTitle(user.getLanguage()));
            }
            hmAttr.put("Valor", hmValues);
            //TODO: RuleMgr ???ROLE
            comboAtt.put(SWBRuleMgr.TAG_INT_ROLE, hmAttr); //SWBRuleMgr.TAG_INT_ROLE
            vecOrderAtt.add(numero++, SWBRuleMgr.TAG_INT_ROLE); //RuleMgr.TAG_INT_ROLE

            // Se agrega la parte de reglas
            if (ruleid == null) {
                ruleid = "";
            }

            int numreglas = 0;
            Iterator<Rule> numrules = SWBContext.getWebSite(tmid).listRules();
            while (numrules.hasNext()) {
                Rule rule = numrules.next();
                if (!ruleid.equals(rule.getURI())) {
                    numreglas++;
                }
            }
            log.debug("numReglas:" + numreglas);
            if (numreglas > 0) {
                hmAttr = new HashMap<>();
                hmOper = new HashMap<>();
                hmValues = new HashMap<>();
                hmAttr.put("Etiqueta", paramRequest.getLocaleString("msgRules"));   ///////////////////////////
                hmAttr.put("Tipo", "select");
                hmOper.put("=", paramRequest.getLocaleString("msgCumpla"));
                hmOper.put("!=", paramRequest.getLocaleString("msgNoCumpla"));
                hmAttr.put("Operador", hmOper);
                Iterator<Rule> enumRules = SWBContext.getWebSite(tmid).listRules();
                while (enumRules.hasNext()) {
                    Rule rule = enumRules.next();
                    if (!ruleid.equals(rule.getURI())) {
                        hmValues.put(rule.getURI(), rule.getDisplayTitle(user.getLanguage()));
                    }
                }
                hmAttr.put("Valor", hmValues);
                comboAtt.put(SWBRuleMgr.TAG_INT_RULE, hmAttr); //RuleMgr.TAG_INT_RULE
                vecOrderAtt.add(numero++, SWBRuleMgr.TAG_INT_RULE); //RuleMgr.TAG_INT_RULE
            }

            // Se agrega la parte de paises

            List<Country> countries = SWBUtils.Collections.copyIterator(SWBContext.getWebSite(tmid).listCountries());
            log.debug("numPais:" + countries.size());
            if (!countries.isEmpty()) {
                hmAttr = new HashMap<>();
                hmOper = new HashMap<>();
                hmValues = new HashMap<>();
                hmAttr.put("Etiqueta", paramRequest.getLocaleString("msgCountries"));   ///////////////////////////
                hmAttr.put("Tipo", "select");
                hmOper.put("=", paramRequest.getLocaleString("msgIs"));
                hmOper.put("!=", paramRequest.getLocaleString("msgNotIs"));
                hmAttr.put("Operador", hmOper);
                Iterator<Country> enumCountry = countries.iterator();
                while (enumCountry.hasNext()) {
                    Country country = enumCountry.next();
                    hmValues.put(country.getId(), country.getDisplayTitle(user.getLanguage()));
                }
                hmAttr.put("Valor", hmValues);
                comboAtt.put(User.swb_usrCountry.getName(), hmAttr); //falta tag del pais
                vecOrderAtt.add(numero++, User.swb_usrCountry.getName()); //falta tag del pais
            }

            //Se agrega la parte de IP del usuario
            hmAttr = new HashMap<>();
            hmOper = new HashMap<>();
            hmValues = new HashMap<>();
            hmAttr.put("Etiqueta", paramRequest.getLocaleString("msgIPUser"));
            hmAttr.put("Tipo", "TEXT");
            hmOper.put("=", paramRequest.getLocaleString("msgIs"));
            hmOper.put("!=", paramRequest.getLocaleString("msgNotIs"));
            hmAttr.put("Operador", hmOper);
            comboAtt.put(SWBRuleMgr.TAG_INT_USERIP, hmAttr); //falta tag de IP
            vecOrderAtt.add(numero++, SWBRuleMgr.TAG_INT_USERIP); //falta tag de IP

            //Se agrega la parte de WebPage visitada por el usuario
            hmAttr = new HashMap<>();
            hmOper = new HashMap<>();
            hmValues = new HashMap<>();
            hmAttr.put("Etiqueta", paramRequest.getLocaleString("msgWebPageUser"));
            hmAttr.put("Tipo", "TEXT");
            hmOper.put("=", paramRequest.getLocaleString("msgContains"));
            hmOper.put("!=", paramRequest.getLocaleString("msgNotContains"));
            hmAttr.put("Operador", hmOper);
            comboAtt.put(SWBRuleMgr.TAG_WEBPAGEVISITED_ATT, hmAttr);
            vecOrderAtt.add(numero++, SWBRuleMgr.TAG_WEBPAGEVISITED_ATT);


            //Se agrega la parte de Historial de WebPages visitada por el usuario
            hmAttr = new HashMap<>();
            hmOper = new HashMap<>();
            hmValues = new HashMap<>();
            hmAttr.put("Etiqueta", paramRequest.getLocaleString("msgHistoryWebPageUser"));
            hmAttr.put("Tipo", "TEXT");
            hmOper.put("=", paramRequest.getLocaleString("msgContains"));
            hmOper.put("!=", paramRequest.getLocaleString("msgNotContains"));
            for(int i=1; i<26; i++)
            {
                hmOper.put("-"+i, "-"+i);
            }
            hmAttr.put("Operador", hmOper);
            comboAtt.put(SWBRuleMgr.TAG_WEBPAGEHISTORY_ATT, hmAttr); //
            vecOrderAtt.add(numero++, SWBRuleMgr.TAG_WEBPAGEHISTORY_ATT); //



            //Tipo de usuario
            Iterator<String> usrTypes = usrRepo.getUserTypes();
            log.debug("usrTypes:" + usrTypes.hasNext());
            while (usrTypes.hasNext()) {

                String uType = usrTypes.next();
                log.debug("UType: " + uType);
                hmAttr = new HashMap<>();
                hmOper = new HashMap<>();
                hmValues = new HashMap<>();
                hmAttr.put("Etiqueta", uType);   ///////////////////////////
                hmAttr.put("Tipo", "select");
                hmOper.put("=", paramRequest.getLocaleString("msgSameAs"));
                hmOper.put("!=", paramRequest.getLocaleString("msgNotEqual"));
                hmAttr.put("Operador", hmOper);
                Iterator<SemanticProperty> iteruserType = usrRepo.listAttributesofUserType(uType);
                while (iteruserType.hasNext()) {
                    SemanticProperty usrTypeAtt = iteruserType.next();
                    hmValues.put(usrTypeAtt.getName(), usrTypeAtt.getDisplayName(user.getLanguage()));
                }
                hmAttr.put("Valor", hmValues);
                if (!tmid.equals(SWBContext.getGlobalWebSite().getId())) {
                    comboAtt.put(uType, hmAttr);
                }
                vecOrderAtt.add(numero++, uType); 
                log.debug("tipo de usuario: " + uType);
            }

            Iterator<SemanticProperty> iteratt = usrRepo.listAttributes(); //listBasicAttributes
            int attnum = 0;
            while (iteratt.hasNext()) {

                String tipoControl = "TEXT";
                SemanticProperty usrAtt = iteratt.next();
                attnum++;
                log.debug("ListAttributes:" + usrAtt.getName() + ", " + attnum + ", objProp: " + usrAtt.isObjectProperty());
                hmAttr = new HashMap<>();
                hmOper = new HashMap<>();
                hmValues = new HashMap<>();

                hmAttr.put("Etiqueta", usrAtt.getDisplayName(user.getLanguage()));
                if (usrAtt.getDisplayProperty() != null) {
                    log.debug("DisplayProperty");
                    DisplayProperty dobj = new DisplayProperty(usrAtt.getDisplayProperty());

                    if(!dobj.isHidden())
                    {
                    String selectValues = dobj.getSelectValues(user.getLanguage());
                    if (selectValues != null) {
                        tipoControl = "select";
                        hmAttr.put("Tipo", tipoControl);
                        hmOper.put("=", paramRequest.getLocaleString("msgSameAs"));
                        hmOper.put("!=", paramRequest.getLocaleString("msgNotEqual"));
                        hmAttr.put("Operador", hmOper);

                        if(usrAtt.getName().equals("usrLanguage"))
                        {
                            Iterator<Language> itlang= ws.listLanguages();
                            while (itlang.hasNext()) {
                                Language lang = itlang.next();
                                if (null!=lang) {
                                    String strLang = lang.getDisplayTitle(user.getLanguage());
                                    String strVal = lang.getId();
                                    hmValues.put(strVal,strLang);
                                }
                            }
                        }
                        else
                        {
                            StringTokenizer st = new StringTokenizer(selectValues, "|");
                            while (st.hasMoreTokens()) {
                                String tok = st.nextToken();
                                int ind = tok.indexOf(':');
                                String idt = tok;
                                String val = tok;
                                if (ind > 0) {
                                    idt = tok.substring(0, ind);
                                    val = tok.substring(ind + 1);
                                }
                                hmValues.put(idt, val);
                            }
                        }
                        hmAttr.put("Valor", hmValues);
                        comboAtt.put(usrAtt.getName(), hmAttr);
                        vecOrderAtt.add(numero++, usrAtt.getName());
                    } else if (!usrAtt.equals(User.swb_usrPassword)){
                        if (usrAtt.isDataTypeProperty()) {
                            log.debug("DP: DataTypeProperty");
                            if (usrAtt.isInt()||usrAtt.isFloat()||usrAtt.isLong()) {
                                tipoControl = "TEXT";
                                hmAttr.put("Tipo", tipoControl);
                                hmOper.put("&gt;", paramRequest.getLocaleString("msgGreaterThan"));
                                hmOper.put("&lt;", paramRequest.getLocaleString("msgLessThan"));
                                hmOper.put("=", paramRequest.getLocaleString("msgIs"));
                                hmOper.put("!=", paramRequest.getLocaleString("msgNotIs"));
                                hmAttr.put("Operador", hmOper);
                                comboAtt.put(usrAtt.getName(), hmAttr);
                                vecOrderAtt.add(numero++, usrAtt.getName());
                            } else if (usrAtt.isBoolean()) {
                                tipoControl = "select";
                                hmAttr.put("Tipo", tipoControl);
                                hmOper.put("=", paramRequest.getLocaleString("msgIs"));
                                hmOper.put("!=", paramRequest.getLocaleString("msgNotIs"));
                                hmAttr.put("Operador", hmOper);
                                hmValues.put("true", paramRequest.getLocaleString("msgTrue"));
                                hmValues.put("false", paramRequest.getLocaleString("msgFalse"));
                                hmAttr.put("Valor", hmValues);
                                comboAtt.put(usrAtt.getName(), hmAttr);
                                vecOrderAtt.add(numero++, usrAtt.getName());
                            } else if (usrAtt.isString()) {
                                tipoControl = "TEXT";
                                hmAttr.put("Tipo", tipoControl);
                                hmOper.put("=", paramRequest.getLocaleString("msgIs"));
                                hmOper.put("!=", paramRequest.getLocaleString("msgNotIs"));
                                hmAttr.put("Operador", hmOper);
                                comboAtt.put(usrAtt.getName(), hmAttr);
                                vecOrderAtt.add(numero++, usrAtt.getName());
                            }
                        }
                    }
                    log.debug("DP:Tipo --- " + tipoControl);
                  }
                }
            }
        } catch (Exception e) {
            log.error(paramRequest.getLocaleString("msgErrorLoadingUserAttributeList") + ". SWBARules.loadComboAttr", e);
        }
    }
    
    /**
     * Gets a JSON String with the user attributes.
     * @return JSON String with the user attributes
     */
    private String getJSONComboAttr() {
    	JSONArray attributes = new JSONArray();
    	
    	for (int i = 0; i < vecOrderAtt.size(); i++) {
        String valor = (String) vecOrderAtt.get(i);
        HashMap hmAttr = (HashMap) comboAtt.get(valor);
        String label = (String) hmAttr.get("Etiqueta");

        	JSONObject attribute = new JSONObject();
        attribute.put("type", (String) hmAttr.get("Tipo"));
        attribute.put("name", valor);
        attribute.put("title", label);

        HashMap hmOper = (HashMap) hmAttr.get("Operador");
        Iterator itOper = hmOper.keySet().iterator();
        JSONArray operators = new JSONArray();
            
        while (itOper.hasNext()) {
            String thisValue = (String) itOper.next();
            String thisLabel = (String) hmOper.get(thisValue);
            JSONObject operator = new JSONObject();
            operator.put("value", thisValue);
            operator.put("title", thisLabel);
            operators.put(operator);
        }
        attribute.put("operators", operators);
        hmOper = null;
        JSONArray attValues = new JSONArray();

        if (!hmAttr.get("Tipo").equals("TEXT")) {
            HashMap valoresCombo = (HashMap) hmAttr.get("Valor");
            Iterator itValCombo = valoresCombo.keySet().iterator();
                
            while (itValCombo.hasNext()) {
                String nomValCombo = (String) itValCombo.next();
                String labelValCombo = (String) valoresCombo.get(nomValCombo);
                JSONObject attValue = new JSONObject();
                attValue.put("title", labelValCombo);
                attValue.put("value", nomValCombo);
                attValues.put(attValue);
            }
            attribute.put("catalog", attValues);
        } else {
            //armar text para pedir/mostrar valor
            	JSONObject attValue = new JSONObject();
            attValue.put("title", "");
            attValue.put("value", "TEXT");
            attValues.put(attValue);
            attribute.put("catalog", attValues);
        }
        attributes.put(attribute);
    }
        
    	JSONObject ret = new JSONObject();
    	ret.put("attributes", attributes);
        return ret.toString();
    }
}
