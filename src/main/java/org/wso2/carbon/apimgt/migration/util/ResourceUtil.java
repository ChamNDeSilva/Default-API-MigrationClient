/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.migration.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.apimgt.migration.APIMigrationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class ResourceUtil {

    private static final Log log = LogFactory.getLog(ResourceUtil.class);

    /**
     * To handle exceptions
     *
     * @param msg error message
     * @throws APIMigrationException
     */
    public static void handleException(String msg, Throwable e) throws APIMigrationException {
        log.error(msg, e);
        throw new APIMigrationException(msg, e);
    }

    /**
     * To update synapse API
     *
     * @param document       XML document object
     * @param file       synapse file
     * @throws APIMigrationException
     */
    public static void updateSynapseAPI(Document document, File file) throws APIMigrationException {
        try {
            updateHandlers(document, file);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            handleException("Could not initiate TransformerFactory Builder.", e);
        } catch (TransformerException e) {
            handleException("Could not transform the source.", e);
        }
    }

    /**
     * To update synapse API with CORSHandler
     *
     * @param document  XML document object
     * @param file   synapse file
     * @throws APIMigrationException
     */
    private static void updateHandlers(Document document, File file) throws APIMigrationException {
        Element handlersElement = (Element) document.getElementsByTagNameNS(Constants.SYNAPSE_API_XMLNS,
                Constants.SYNAPSE_API_ELEMENT_HANDLERS).item(0);

        NodeList handlerNodes = handlersElement.getElementsByTagName(Constants.SYNAPSE_API_ELEMENT_HANDLER);
        boolean corsHandlerExists = false;

        for (int i = 0; i < handlerNodes.getLength(); ++i) {
            Element handler = (Element) handlerNodes.item(i);
            String className = handler.getAttribute(Constants.SYNAPSE_API_ATTRIBUTE_CLASS);

            if (className.equals(Constants.SYNAPSE_API_VALUE_CORS_HANDLER)) {
                corsHandlerExists = true;
                break;
            }
        }
        if(!corsHandlerExists) {
            Element corsHandlerNode = document.createElement(Constants.SYNAPSE_API_ELEMENT_HANDLER);
            Element corsHandlerProperty = document.createElement(Constants.SYNAPSE_API_ELEMENT_PROPERTY);

            corsHandlerNode.setAttribute(Constants.SYNAPSE_API_ATTRIBUTE_CLASS,
                    Constants.SYNAPSE_API_VALUE_CORS_HANDLER);
            corsHandlerProperty.setAttribute(Constants.SYNAPSE_API_ATTRIBUTE_NAME,
                    Constants.SYNAPSE_API_CORS_HANDLER_NAME);
            corsHandlerProperty.setAttribute(Constants.SYNAPSE_API_ATTRIBUTE_VALUE,
                    Constants.SYNAPSE_API_CORS_HANDLER_VALUE);

            handlersElement.appendChild(corsHandlerNode);
            corsHandlerNode.appendChild(corsHandlerProperty);
            corsHandlerNode.removeAttribute(Constants.XML_NAMESPACE_ATTRIBUTE);
        }
    }
}
