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

public class Constants {

    public static final String ALTER = "alter";

    // Migration client argument property names
    public static final String ARG_MIGRATE_TENANTS = "tenants";
    public static final String ARG_MIGRATE_FILE_SYSTEM = "migrate-def-api";

    // Synapse configuration related
    public static final String SYNAPSE_API_ROOT_ELEMENT = "api";
    public static final String SYNAPSE_API_ATTRIBUTE_VERSION = "version";
    public static final String SYNAPSE_API_ATTRIBUTE_NAME = "name";
    public static final String SYNAPSE_API_ATTRIBUTE_CLASS = "class";
    public static final String SYNAPSE_API_ATTRIBUTE_VALUE = "value";
    public static final String SYNAPSE_API_ELEMENT_PROPERTY = "property";
    public static final String SYNAPSE_API_ELEMENT_HANDLERS = "handlers";
    public static final String SYNAPSE_API_ELEMENT_HANDLER = "handler";
    public static final String SYNAPSE_API_XMLNS = "http://ws.apache.org/ns/synapse";
    public static final String SYNAPSE_API_CORS_HANDLER_NAME = "apiImplementationType";
    public static final String SYNAPSE_API_CORS_HANDLER_VALUE = "ENDPOINT";
    public static final String XML_NAMESPACE_ATTRIBUTE = "xmlns";
    public static final String SYNAPSE_API_VALUE_CORS_HANDLER =
            "org.wso2.carbon.apimgt.gateway.handlers.security.CORSRequestHandler";

}
