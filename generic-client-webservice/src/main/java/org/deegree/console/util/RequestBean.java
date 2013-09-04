//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-client/deegree-jsf-console/src/main/java/org/deegree/client/generic/RequestBean.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.console.util;

import static java.io.File.separator;
import static java.util.Collections.sort;
import static org.deegree.commons.utils.net.HttpUtils.enableProxyUsage;
import static org.slf4j.LoggerFactory.getLogger;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.deegree.client.core.utils.MessageUtils;
import org.deegree.commons.utils.net.DURL;
import org.deegree.commons.xml.XMLAdapter;
import org.slf4j.Logger;

/**
 * A request scoped bean handling the requests (MS: changed to request scope to
 * cope with reload problems).
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: mschneider $
 *
 * @version $Revision: 29926 $, $Date: 2011-03-08 11:47:59 +0100 (Di, 08. Mär
 * 2011) $
 */
@ManagedBean
@SessionScoped
public class RequestBean implements Serializable {

    private static final long serialVersionUID = 293894352421399345L;
    private static final Logger LOG = getLogger(RequestBean.class);
    private File requestsBaseDir;
    private String selectedService;
    private String selectedReqProfile;
    private String selectedRequest;
    private String mimeType;
    private List<String> services;
    private List<String> requestProfiles;
    private List<SelectItem> requests;
    private String request;
    private String saveRequestName;
    private String requestFilter;
    private String response;
    // SERVICE
    // -- PROFILE
    // ----REQUESTTYPE
    // --------xml
    // --------REQUEST
    private TreeMap<String, Map<String, Map<String, List<String>>>> allRequests = new TreeMap<String, Map<String, Map<String, List<String>>>>();
    private String responseFile;
    private String targetUrl;
    // file name that stores active workspaces (per webapp)
    private final String ACTIVE_WS_CONFIG_FILE = "webapps.properties";
    // default workspace dir for request maps
    private final String DEFAULT_REQUEST_MAP_DIR = "generic-client-webservice";


    public File getRequestsBaseDir() {
        return requestsBaseDir;
    }

    public void setRequestsBaseDir(File requestsBaseDir) {
        this.requestsBaseDir = requestsBaseDir;
    }

    public String getSelectedService() {
        return selectedService;
    }

    public void setSelectedService(String selectedService) {
        this.selectedService = selectedService;
    }

    public String getSelectedReqProfile() {
        return selectedReqProfile;
    }

    public void setSelectedReqProfile(String selectedReqProfile) {
        this.selectedReqProfile = selectedReqProfile;
    }

    public String getSelectedRequest() {
        return selectedRequest;
    }

    public void setSelectedRequest(String selectedRequest) {
        this.selectedRequest = selectedRequest;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public String getSaveRequestName() {
        return saveRequestName;
    }

    public void setSaveRequestName(String saveRequestName) {
        this.saveRequestName = saveRequestName;
    }

    public String getRequestFilter() {
        return requestFilter;
    }

    public void setRequestFilter(String requestFilter) {
        this.requestFilter = requestFilter;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public TreeMap<String, Map<String, Map<String, List<String>>>> getAllRequests() {
        return allRequests;
    }

    public void setAllRequests(TreeMap<String, Map<String, Map<String, List<String>>>> allRequests) {
        this.allRequests = allRequests;
    }

    public String getResponseFile() {
        return responseFile;
    }

    public void setResponseFile(String responseFile) {
        this.responseFile = responseFile;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public static Logger getLog() {
        return LOG;
    }

    public void setRequestProfiles(List<String> requestProfiles) {
        this.requestProfiles = requestProfiles;
    }

    public void setRequests(List<SelectItem> requests) {
        this.requests = requests;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    @PostConstruct
    public void init() {
        allRequests.clear();

        preInit();
        initRequestMap();

        List<String> services = new ArrayList<String>();
        for (String service : allRequests.keySet()) {
            services.add(service);
        }
        this.services = services;
        if (services.size() > 0) {
            this.selectedService = services.get(0);
        }

        setRequestProfiles();
        if (requestProfiles.size() > 0) {
            this.selectedReqProfile = requestProfiles.get(0);
        }

        setRequests();
        if (requests.size() > 0) {
            for (SelectItem sel : requests) {
                if (!(sel instanceof SelectItemGroup)) {
                    this.selectedRequest = (String) sel.getValue();
                }
            }
        }
        //this.setWorkspaceService("generic-client-workspace");

        loadExample();
    }

    public void addRequest() {
        String subdir = new File(selectedRequest).getParentFile().getParentFile().getName();
        File saveRequest;
        try {
            saveRequest = new File(saveRequestName);
        } catch (Exception e) {
            return;
        }
        File file = new File(requestsBaseDir, selectedService + separator + selectedReqProfile + separator + subdir
                + separator + "xml" + separator + saveRequest.getName() + ".xml");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            IOUtils.write(request, out);
            allRequests.clear();
            initRequestMap();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(out);
        }
        this.saveRequestName = "";
        setRequests();
    }

    public void saveRequest() {
        File file = new File(requestsBaseDir, selectedRequest);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            IOUtils.write(request, out);
            allRequests.clear();
            initRequestMap();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    public void deleteRequest() {
        new File(requestsBaseDir, selectedRequest).delete();
        allRequests.clear();
        initRequestMap();
    }


//    public String getTargetUrl() {
//        if(workspaceService.equals( "" )) {
//            return getEndpoint();
//        } else {
//            return getEndpoint() + "/" + workspaceService;
//        }
//    }
    public void sendRequest()
            throws UnsupportedEncodingException {
        if (!request.startsWith("<?xml")) {
            request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + request;
        }

        String targetUrl = getTargetUrl();
        LOG.debug("Try to send the following request to " + targetUrl + " : \n" + request);
        if (targetUrl != null && targetUrl.length() > 0 && request != null && request.length() > 0) {
            InputStream is = new ByteArrayInputStream(request.getBytes("UTF-8"));
            try {
                DURL u = new DURL(targetUrl);
                DefaultHttpClient client = enableProxyUsage(new DefaultHttpClient(), u);
                HttpPost post = new HttpPost(targetUrl);
                post.setHeader("Content-Type", "text/xml;charset=UTF-8");
                post.setEntity(new InputStreamEntity(is, -1));
                HttpResponse response = client.execute(post);
                Header[] headers = response.getHeaders("Content-Type");
                if (headers.length > 0) {
                    mimeType = headers[0].getValue();
                    LOG.debug("Response mime type: " + mimeType);
                    if (!mimeType.toLowerCase().contains("xml")) {
                        this.response = null;
                        FacesMessage fm = MessageUtils.getFacesMessage(FacesMessage.SEVERITY_INFO,
                                "INFO_RESPONSE_NOT_XML");
                        FacesContext.getCurrentInstance().addMessage(null, fm);
                    }
                }
                if (mimeType == null) {
                    mimeType = "text/plain";
                }

                InputStream in = response.getEntity().getContent();
                File file = File.createTempFile("genericclient", ".xml");
                responseFile = file.getName().toString();
                FileOutputStream out = new FileOutputStream(file);
                try {
                    IOUtils.copy(in, out);
                } finally {
                    IOUtils.closeQuietly(out);
                    IOUtils.closeQuietly(in);
                }
                BufferedReader reader = null;
                try {
                    in = new BoundedInputStream(new FileInputStream(file), 1024 * 256);
                    reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String s;
                    while ((s = reader.readLine()) != null) {
                        sb.append(s);
                    }
                    this.response = sb.toString();
                } finally {
                    IOUtils.closeQuietly(reader);
                }
            } catch (IOException e) {
                FacesMessage fm = new FacesMessage(e.getMessage());
                fm.setSeverity(FacesMessage.SEVERITY_ERROR);
                FacesContext.getCurrentInstance().addMessage(null, fm);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
    }

    public List<String> getRequestProfiles() {
        setRequestProfiles();
        return requestProfiles;
    }

    public List<SelectItem> getRequests() {
        setRequests();
        return requests;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getRequest() {
        loadExample();
        return request;
    }

    private static Properties loadWebappToWsMappings(File file) {

        Properties props = new Properties();
        if (file.exists()) {
            LOG.info("Loading webapp-to-workspace mappings from file '" + file + "'");
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                props = new Properties();
                props.load(fis);
            } catch (IOException e) {
                String msg = "Error reading webapp-to-workspace mappings from file '" + file + "': " + e.getMessage();
                LOG.error(msg);
                //throw new IOException(msg, e);
            } finally {
                IOUtils.closeQuietly(fis);
            }
        }
        return props;
    }

    private void preInit() {
        File wsRoot = new File(System.getProperty("user.home") + separator + ".deegree");
        File activeWsConfigFile = new File(wsRoot, ACTIVE_WS_CONFIG_FILE);

        Properties props = loadWebappToWsMappings(activeWsConfigFile);
        String wsName = props.getProperty("generic-client");

        File wsBaseDir = new File(wsRoot + separator + wsName);
        if ( wsName == null ) {
            wsBaseDir = new File(wsRoot + separator + DEFAULT_REQUEST_MAP_DIR);
        }
        LOG.info("using '" + wsBaseDir + "' for request maps");

        requestsBaseDir = new File(wsBaseDir, "manager/requests");
        if ( ! requestsBaseDir.exists() ) {
            String realPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/requests");
            requestsBaseDir = new File(realPath);
        }

        LOG.debug("Using requests directory " + requestsBaseDir);        
    }

    private void initRequestMap() {

        String[] serviceTypes = requestsBaseDir.list();
        if (serviceTypes != null && serviceTypes.length > 0) {
            Arrays.sort(serviceTypes);
            for (String serviceType : serviceTypes) {
                if (ignoreFile(serviceType)) {
                    continue;
                }
                // for each service subdir (wfs, wms, etc.)
                File serviceDir = new File(requestsBaseDir, serviceType);
                String[] profileDirs = serviceDir.list();
                Map<String, Map<String, List<String>>> requestProfiles = new LinkedHashMap<String, Map<String, List<String>>>();
                if (profileDirs != null && profileDirs.length > 0) {
                    Arrays.sort(profileDirs);
                    for (String profile : profileDirs) {
                        if (ignoreFile(profile)) {
                            continue;
                        }
                        // for each profile subdir (demo, philosopher, etc.)
                        File profileDir = new File(serviceDir, profile);
                        String[] requestTypeDirs = profileDir.list();
                        Map<String, List<String>> requestTypes = new LinkedHashMap<String, List<String>>();
                        if (requestTypeDirs != null && requestTypeDirs.length > 0) {
                            Arrays.sort(requestTypeDirs);
                            for (String requestType : requestTypeDirs) {
                                if (ignoreFile(requestType)) {
                                    continue;
                                }
                                // for each request type subdir (GetCapabilities, GetFeature, etc.)
                                File requestTypeDir = new File(profileDir, requestType + File.separator + "xml");
                                String[] requests = requestTypeDir.list(new FilenameFilter() {
                                    public boolean accept(File dir, String name) {
                                        if (name.toLowerCase().endsWith(".xml")) {
                                            return true;
                                        }
                                        return false;
                                    }
                                });
                                List<String> requestUrls = new ArrayList<String>();
                                if (requests != null && requests.length > 0) {
                                    Arrays.sort(requests);
                                    for (int l = 0; l < requests.length; l++) {
                                        String requestUrl = serviceType + "/" + profile + "/" + requestType + "/xml/"
                                                + requests[l];
                                        // for each request example
                                        requestUrls.add(requestUrl);
                                    }
                                }
                                requestTypes.put(requestType, requestUrls);
                            }
                        }
                        requestProfiles.put(profile, requestTypes);
                    }
                }
                allRequests.put(serviceType, requestProfiles);
            }
        }

    }

    boolean ignoreFile(String name) {
        return name.endsWith("CVS") || name.startsWith(".svn");
    }

    private void setRequestProfiles() {
        List<String> profiles = new ArrayList<String>();
        if (selectedService != null) {
            for (String s : allRequests.keySet()) {
                if (selectedService.equals(s)) {
                    for (String profile : allRequests.get(s).keySet()) {
                        profiles.add(profile);
                    }
                }
            }
        }
        sort(profiles);
        this.requestProfiles = profiles;
    }

    private void setRequests() {
        selectedRequest = null;
        List<SelectItem> types = new ArrayList<SelectItem>();
        for (String s : allRequests.keySet()) {
            if (selectedService != null && selectedService.equals(s)) {
                for (String p : allRequests.get(s).keySet()) {
                    if (selectedReqProfile != null && selectedReqProfile.equals(p)) {
                        Map<String, List<String>> ts = allRequests.get(s).get(p);
                        for (String t : ts.keySet()) {
                            SelectItem[] urls = new SelectItem[ts.get(t).size()];
                            int i = 0;
                            for (String url : ts.get(t)) {
                                String fileName = url.substring(url.lastIndexOf(File.separator) + 1, url.length());
                                urls[i++] = new SelectItem(url, fileName);
                                if (selectedRequest == null) {
                                    selectedRequest = url;
                                }
                            }
                            SelectItemGroup typeGrp = new SelectItemGroup(t, "", false, urls);
                            types.add(typeGrp);
                        }
                    }
                }
            }
        }
        this.requests = types;
    }

    private void loadExample() {
        if (selectedRequest != null) {
            LOG.debug("load request " + selectedRequest);
            File file = new File(requestsBaseDir, selectedRequest);
            if (file.exists()) {
                XMLAdapter adapter = new XMLAdapter(file);
                setRequest(adapter.toString());
            }
        }
    }


    public String getDlparams()
            throws UnsupportedEncodingException {
        return "mt=" + URLEncoder.encode(mimeType, "UTF-8") + "&file=" + URLEncoder.encode(responseFile, "UTF-8");
    }
    // @Override
    // public String toString() {
    // return generateToString( this );
    // }

}
