/* 
 * Copyright 2012 Devoteam http://www.devoteam.com
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * 
 * This file is part of Multi-Protocol Test Suite (MTS).
 * 
 * Multi-Protocol Test Suite (MTS) is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License.
 * 
 * Multi-Protocol Test Suite (MTS) is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Multi-Protocol Test Suite (MTS).
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.devoteam.srit.xmlloader.sigtran.ap;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bn.types.ObjectIdentifier;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 *
 * @author fhenry
 */
public class Asn1ToXml 
{

    public Asn1ToXml() 
    {
    }

    public static Document getDocumentXML(final String xmlFileName)
    {
        Document document = null;
        SAXReader reader = new SAXReader();
        try 
        {
            document = reader.read(xmlFileName);
        }
        catch (DocumentException ex) 
        {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, ex, "Wrong ASN1 file : ");
        }
        return document;
    }

    public String toXML(Object objClass, int indent)  
    {
    	indent = indent + 2;
    	String ret = "";
		try 
		{
			if (objClass ==  null)
	    	{
	    		return ret;
	    	}

			String strClass = objClass.getClass().toString();
	    	int pos = strClass.lastIndexOf('.');
	    	if (pos >= 0)
	    	{
	    		strClass = strClass.substring(pos + 1);
	    	}
	    	
			ret += "\n" + indent(indent);
	    	ret += "<";
	    	ret += strClass;
	    	ret +=">";
	    	
	        // parsing object methods 
	    	Method[] methods = objClass.getClass().getDeclaredMethods();
	    	//for (int i= methods.length - 1; i >=0; i--)
	    	boolean simple = true;
	    	for (int i= 0; i < methods.length; i++)
	    	{
    			String name = methods[i].getName();
    			if (name.startsWith("get") && !name.equals("getIntArray") && !"getPreparedData".equals(name))
    			{
    				System.out.println(methods[i]);
    				Object subObject = methods[i].invoke(objClass);
    				if (subObject == null) 
					{
    					continue;
					}
    				Class subClass = subObject.getClass();
    				if (subClass != null && subClass.getCanonicalName().equals("java.lang.Boolean"))
    				{
    					ret += "<Boolean>"; 
    					ret +=subObject.toString();
    					ret += "</Boolean>";
    				} 
    				else if (subClass != null && subClass.getCanonicalName().equals("java.lang.Long"))
    				{
    					ret += "<Long>";
    					ret +=subObject.toString();
    					ret += "</Long>";
    				}
    				else if (subClass != null && subClass.getCanonicalName().equals("java.lang.Integer"))
    				{
    					ret += "<Integer>";
    					ret +=subObject.toString();
    					ret += "</Integer>";
    				} 
    				else if (subClass != null && subClass.getCanonicalName().equals("java.lang.String"))
    				{
    					ret += "<String>";
    					ret +=subObject.toString();
    					ret += "</String>";
    				}
    				else if (subClass != null && subClass.getCanonicalName().equals("byte[]"))
    				{
    					byte[] bytes = (byte[]) subObject;
    					ret += "<Bytes>";
    					ret += Utils.toHexaString(bytes, "");
    					ret += "</Bytes>";
    				}
    				else if (subClass != null && subClass.getCanonicalName().equals("java.util.ArrayList"))
    				{
    					Collection coll = (Collection) subObject;
    					Iterator iter = coll.iterator();
    					indent = indent;
    					ret += "\n" + indent(indent + 2);
    					ret += "<ArrayList>";
    					while (iter.hasNext())
    					{
    						Object subObj = iter.next();
    						ret += toXML(subObj, indent + 2);
    					}
    					ret += "\n" + indent(indent + 2);
    					ret += "</ArrayList>";
    					ret += "\n" + indent(indent);
    				}
    				else if (subClass != null && subClass.getCanonicalName().equals("org.bn.types.ObjectIdentifier"))
    				{
    					ObjectIdentifier objId = (ObjectIdentifier) subObject;
    					ret += "\n" + indent(indent + 2);
    					ret += "<" + name +">";
    					ret += "<ObjectIdentifier>";
    					ret += objId.getValue();
    					ret += "</ObjectIdentifier>";
    					ret += "</" + name +">";
    				}
    				else
    				{
    					ret += toXML(subObject, indent);
    					simple = false;
       				}
    			}
			}
	    	
			if (!simple)
			{
		    	ret += "\n" + indent(indent);
			}
	    	ret += "</";
	    	ret += strClass;
	    	ret += ">";
	    	
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

    	return ret;
    }
    
    /**
     * generates a string of nb*"    " (four spaces nb times), used for intentation in printAvp
     */
    private static String indent(int nb)
    {
        String str = "";
        for (int i = 0; i < nb; i++)
        {
            str += " ";
        }
        return str;
    }

}