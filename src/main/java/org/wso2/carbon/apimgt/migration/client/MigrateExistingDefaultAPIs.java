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

package org.wso2.carbon.apimgt.migration.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.apimgt.migration.APIMigrationException;
import org.wso2.carbon.apimgt.migration.client.internal.ServiceHolder;
import org.wso2.carbon.apimgt.migration.util.Constants;
import org.wso2.carbon.apimgt.migration.util.ResourceUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains all the methods which is used to migrate already created APIs
 * with CORSHandler
 *
 */
@SuppressWarnings("unchecked")
public class MigrateExistingDefaultAPIs implements MigrationClient {

    private static final Log log = LogFactory.getLog(MigrateExistingDefaultAPIs.class);
    private List<Tenant> tenantsArray;

    public MigrateExistingDefaultAPIs(String tenantArguments) throws UserStoreException {
        TenantManager tenantManager = ServiceHolder.getRealmService().getTenantManager();

        if (tenantArguments != null) {  // Tenant arguments have been provided so need to load specific ones
            tenantArguments = tenantArguments.replaceAll("\\s", ""); // Remove spaces and tabs

            tenantsArray = new ArrayList();

            if (tenantArguments.contains(",")) { // Multiple arguments specified
                String[] parts = tenantArguments.split(",");

                for (String part : parts) {
                    if (part.length() > 0) {
                        populateTenants(tenantManager, tenantsArray, part);
                    }
                }
            } else { // Only single argument provided
                populateTenants(tenantManager, tenantsArray, tenantArguments);
            }
        } else {  // Load all tenants
            tenantsArray = new ArrayList(Arrays.asList(tenantManager.getAllTenants()));
            Tenant superTenant = new Tenant();
            superTenant.setDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            superTenant.setId(MultitenantConstants.SUPER_TENANT_ID);
            tenantsArray.add(superTenant);
        }
    }

    private void populateTenants(TenantManager tenantManager, List<Tenant> tenantList, String argument) throws UserStoreException {
        log.debug("Argument provided : " + argument);

        if (argument.contains("@")) { // Username provided as argument
            int tenantID = tenantManager.getTenantId(argument);

            if (tenantID != -1) {
                tenantList.add(tenantManager.getTenant(tenantID));
            } else {
                log.error("Tenant does not exist for username " + argument);
            }
        } else { // Domain name provided as argument
            Tenant[] tenants = tenantManager.getAllTenantsForTenantDomainStr(argument);

            if (tenants.length > 0) {
                tenantList.addAll(Arrays.asList(tenants));
            } else {
                log.error("Tenant does not exist for domain " + argument);
            }
        }
    }

    /**
     * This method is used to migrate synapse files
     * This changes the synapse api and add the new handlers
     *
     * @throws APIMigrationException
     */
    @Override
    public void defaultAPISynapseMigration() throws APIMigrationException {
        String repository = CarbonUtils.getCarbonRepository();
        String tenantRepository = CarbonUtils.getCarbonTenantsDirPath();
        for (Tenant tenant : tenantsArray) {
            log.debug("Start synapseAPIMigration for tenant " + tenant.getId() + "(" + tenant.getDomain() + ")");
            String apiFilePath;
            if (tenant.getId() != MultitenantConstants.SUPER_TENANT_ID) {
                apiFilePath = tenantRepository + "/" + tenant.getId() +
                        "/synapse-configs/default/api";
            } else {
                apiFilePath = repository + "synapse-configs/default/api";
            }
            File APIFiles = new File(apiFilePath);
            File[] synapseFiles = APIFiles.listFiles();

            if (synapseFiles == null) {
                log.debug("No api folder " + apiFilePath + " exists for tenant " + tenant.getId() + "(" + tenant.getDomain() + ")");
                continue;
            }

            for (File synapseFile : synapseFiles) {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                docFactory.setNamespaceAware(true);

                try {
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                    Document doc = docBuilder.parse(synapseFile);
                    doc.getDocumentElement().normalize();
                    Element rootElement = doc.getDocumentElement();

                    if (Constants.SYNAPSE_API_ROOT_ELEMENT.equals(rootElement.getNodeName()) &&
                            !rootElement.hasAttribute(Constants.SYNAPSE_API_ATTRIBUTE_VERSION)) {
                        ResourceUtil.updateSynapseAPI(doc, synapseFile);
                    }
                } catch (ParserConfigurationException e) {
                    log.error("Parsing exception encountered for " + synapseFile.getAbsolutePath(), e);
                } catch (SAXException e) {
                    log.error("SAX exception encountered for " + synapseFile.getAbsolutePath(), e);
                } catch (IOException e) {
                    log.error("IO exception encountered for " + synapseFile.getAbsolutePath(), e);
                } catch (APIMigrationException e) {
                    log.error("Updating synapse file failed for " + synapseFile.getAbsolutePath(), e);
                }
            }
            log.debug("End synapseAPIMigration for tenant " + tenant.getId() + "(" + tenant.getDomain() + ")");
        }
    }
}

