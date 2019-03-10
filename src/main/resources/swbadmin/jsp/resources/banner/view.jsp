<%@ page import="org.semanticwb.portal.api.SWBParamRequest" %>
<%@ page import="org.semanticwb.portal.api.SWBResourceURL" %>
<%@ page import="org.semanticwb.portal.resources.Banner" %>
<%@ page contentType="text/html;charset=UTF-8"%>
<%
    SWBParamRequest paramRequest = (SWBParamRequest) request.getAttribute("SWBParamRequest");
    Banner banner = (Banner) request.getAttribute("banner");
    SWBResourceURL actURL = paramRequest.getActionUrl();
    String lang = paramRequest.getUser().getLanguage();
    if (null == lang) {
        lang = "es";
    }

    String css = banner.getCssClass();
    String title = banner.getImageTitle(lang);
    if (null == title || title.isEmpty()) {
        title = banner.getImageTitle();
    }

    String img = banner.getImagePath();
    String altText = banner.getAltText(lang);
    if (null == altText || altText.isEmpty()) {
        altText = banner.getAltText();
    }
%>
<div class="swb-banner">
    <a href="<%= actURL %>" target="<%= banner.getURLTarget() %>">
        <img src="<%= img %>" title="<%= title %>" alt="<%= altText %>" class="<%= null != css && !css.isEmpty() ? css : "" %>"/>
    </a>
</div>
