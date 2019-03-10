package org.semanticwb.portal.resources.base;


public abstract class BannerBase extends org.semanticwb.portal.api.GenericSemResource implements org.semanticwb.portal.resources.sem.ViewConfigurable
{
    public static final org.semanticwb.platform.SemanticProperty swbres_bannerURL=org.semanticwb.SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty("http://org.semanticwebbuilder/portal/resources#bannerURL");
    public static final org.semanticwb.platform.SemanticProperty swbres_bannerLinkTarget=org.semanticwb.SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty("http://org.semanticwebbuilder/portal/resources#bannerLinkTarget");
    public static final org.semanticwb.platform.SemanticProperty swbres_bannerImage=org.semanticwb.SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty("http://org.semanticwebbuilder/portal/resources#bannerImage");
    public static final org.semanticwb.platform.SemanticProperty swbres_viewJSP=org.semanticwb.SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty("http://org.semanticwebbuilder/portal/resources#viewJSP");
    public static final org.semanticwb.platform.SemanticProperty swbres_bannerCSSClass=org.semanticwb.SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty("http://org.semanticwebbuilder/portal/resources#bannerCSSClass");
    public static final org.semanticwb.platform.SemanticProperty swbres_bannerAltText=org.semanticwb.SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty("http://org.semanticwebbuilder/portal/resources#bannerAltText");
    public static final org.semanticwb.platform.SemanticProperty swbres_bannerScriptCode=org.semanticwb.SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty("http://org.semanticwebbuilder/portal/resources#bannerScriptCode");
    public static final org.semanticwb.platform.SemanticClass swb_Resource=org.semanticwb.SWBPlatform.getSemanticMgr().getVocabulary().getSemanticClass("http://www.semanticwebbuilder.org/swb4/ontology#Resource");
    public static final org.semanticwb.platform.SemanticProperty swb_semanticResourceInv=org.semanticwb.SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty("http://www.semanticwebbuilder.org/swb4/ontology#semanticResourceInv");
    public static final org.semanticwb.platform.SemanticProperty swbres_bannerImageTitle=org.semanticwb.SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty("http://org.semanticwebbuilder/portal/resources#bannerImageTitle");
    public static final org.semanticwb.platform.SemanticClass swbres_Banner=org.semanticwb.SWBPlatform.getSemanticMgr().getVocabulary().getSemanticClass("http://org.semanticwebbuilder/portal/resources#Banner");
    public static final org.semanticwb.platform.SemanticClass sclass=org.semanticwb.SWBPlatform.getSemanticMgr().getVocabulary().getSemanticClass("http://org.semanticwebbuilder/portal/resources#Banner");

    public BannerBase()
    {
    }

   /**
   * Constructs a BannerBase with a SemanticObject
   * @param base The SemanticObject with the properties for the Banner
   */
    public BannerBase(org.semanticwb.platform.SemanticObject base)
    {
        super(base);
    }

    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() 
    {
        return getSemanticObject().hashCode();
    }

    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) 
    {
        if(obj==null)return false;
        return hashCode()==obj.hashCode();
    }

/**
* Gets the URL property
* @return String with the URL
*/
    public String getURL()
    {
        return getSemanticObject().getProperty(swbres_bannerURL);
    }

/**
* Sets the URL property
* @param value long with the URL
*/
    public void setURL(String value)
    {
        getSemanticObject().setProperty(swbres_bannerURL, value);
    }

/**
* Gets the URLTarget property
* @return String with the URLTarget
*/
    public String getURLTarget()
    {
        return getSemanticObject().getProperty(swbres_bannerLinkTarget);
    }

/**
* Sets the URLTarget property
* @param value long with the URLTarget
*/
    public void setURLTarget(String value)
    {
        getSemanticObject().setProperty(swbres_bannerLinkTarget, value);
    }

/**
* Gets the Image property
* @return String with the Image
*/
    public String getImage()
    {
        return getSemanticObject().getProperty(swbres_bannerImage);
    }

/**
* Sets the Image property
* @param value long with the Image
*/
    public void setImage(String value)
    {
        getSemanticObject().setProperty(swbres_bannerImage, value);
    }

/**
* Gets the ViewJSP property
* @return String with the ViewJSP
*/
    public String getViewJSP()
    {
        return getSemanticObject().getProperty(swbres_viewJSP);
    }

/**
* Sets the ViewJSP property
* @param value long with the ViewJSP
*/
    public void setViewJSP(String value)
    {
        getSemanticObject().setProperty(swbres_viewJSP, value);
    }

/**
* Gets the CssClass property
* @return String with the CssClass
*/
    public String getCssClass()
    {
        return getSemanticObject().getProperty(swbres_bannerCSSClass);
    }

/**
* Sets the CssClass property
* @param value long with the CssClass
*/
    public void setCssClass(String value)
    {
        getSemanticObject().setProperty(swbres_bannerCSSClass, value);
    }

/**
* Gets the AltText property
* @return String with the AltText
*/
    public String getAltText()
    {
        return getSemanticObject().getProperty(swbres_bannerAltText);
    }

/**
* Sets the AltText property
* @param value long with the AltText
*/
    public void setAltText(String value)
    {
        getSemanticObject().setProperty(swbres_bannerAltText, value);
    }

    public String getAltText(String lang)
    {
        return getSemanticObject().getProperty(swbres_bannerAltText, null, lang);
    }

    public String getDisplayAltText(String lang)
    {
        return getSemanticObject().getLocaleProperty(swbres_bannerAltText, lang);
    }

    public void setAltText(String bannerAltText, String lang)
    {
        getSemanticObject().setProperty(swbres_bannerAltText, bannerAltText, lang);
    }

/**
* Gets the Code property
* @return String with the Code
*/
    public String getCode()
    {
        return getSemanticObject().getProperty(swbres_bannerScriptCode);
    }

/**
* Sets the Code property
* @param value long with the Code
*/
    public void setCode(String value)
    {
        getSemanticObject().setProperty(swbres_bannerScriptCode, value);
    }
   /**
   * Sets the value for the property Resource
   * @param value Resource to set
   */

    public void setResource(org.semanticwb.model.Resource value)
    {
        if(value!=null)
        {
            getSemanticObject().setObjectProperty(swb_semanticResourceInv, value.getSemanticObject());
        }else
        {
            removeResource();
        }
    }
   /**
   * Remove the value for Resource property
   */

    public void removeResource()
    {
        getSemanticObject().removeProperty(swb_semanticResourceInv);
    }

   /**
   * Gets the Resource
   * @return a org.semanticwb.model.Resource
   */
    public org.semanticwb.model.Resource getResource()
    {
         org.semanticwb.model.Resource ret=null;
         org.semanticwb.platform.SemanticObject obj=getSemanticObject().getObjectProperty(swb_semanticResourceInv);
         if(obj!=null)
         {
             ret=(org.semanticwb.model.Resource)obj.createGenericInstance();
         }
         return ret;
    }

/**
* Gets the ImageTitle property
* @return String with the ImageTitle
*/
    public String getImageTitle()
    {
        return getSemanticObject().getProperty(swbres_bannerImageTitle);
    }

/**
* Sets the ImageTitle property
* @param value long with the ImageTitle
*/
    public void setImageTitle(String value)
    {
        getSemanticObject().setProperty(swbres_bannerImageTitle, value);
    }

    public String getImageTitle(String lang)
    {
        return getSemanticObject().getProperty(swbres_bannerImageTitle, null, lang);
    }

    public String getDisplayImageTitle(String lang)
    {
        return getSemanticObject().getLocaleProperty(swbres_bannerImageTitle, lang);
    }

    public void setImageTitle(String bannerImageTitle, String lang)
    {
        getSemanticObject().setProperty(swbres_bannerImageTitle, bannerImageTitle, lang);
    }
}
