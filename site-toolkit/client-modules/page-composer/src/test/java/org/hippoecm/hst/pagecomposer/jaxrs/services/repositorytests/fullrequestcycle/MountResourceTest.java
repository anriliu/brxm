/*
 *  Copyright 2016 Hippo B.V. (http://www.onehippo.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hippoecm.hst.pagecomposer.jaxrs.services.repositorytests.fullrequestcycle;

import java.io.IOException;
import java.util.Map;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.hippoecm.hst.pagecomposer.jaxrs.AbstractFullRequestCycleTest;
import org.hippoecm.hst.pagecomposer.jaxrs.AbstractPageComposerTest;
import org.hippoecm.hst.pagecomposer.jaxrs.model.ExtIdsRepresentation;
import org.hippoecm.hst.site.HstServices;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static java.util.Collections.singletonList;
import static org.hippoecm.hst.configuration.HstNodeTypes.BRANCH_PROPERTY_BRANCHOF;
import static org.hippoecm.hst.configuration.HstNodeTypes.GENERAL_PROPERTY_INHERITS_FROM;
import static org.hippoecm.hst.configuration.HstNodeTypes.GENERAL_PROPERTY_LOCKED_BY;
import static org.hippoecm.hst.configuration.HstNodeTypes.MIXINTYPE_HST_BRANCH;
import static org.hippoecm.repository.util.JcrUtils.getMultipleStringProperty;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MountResourceTest extends AbstractFullRequestCycleTest {

    private static final SimpleCredentials ADMIN_CREDENTIALS = new SimpleCredentials("admin", "admin".toCharArray());
    private final SimpleCredentials EDITOR_CREDENTIALS = new SimpleCredentials("editor", "editor".toCharArray());

    @Before
    public void setUp() throws Exception {
        super.setUp();
        final Session session = createSession("admin", "admin");
        AbstractPageComposerTest.createHstConfigBackup(session);
        // move the hst:sitemap and hst:pages below the 'workspace' because since HSTTWO-3959 only the workspace
        // gets copied to preview configuration
        if (!session.nodeExists("/hst:hst/hst:configurations/unittestproject/hst:workspace")) {
            session.getNode("/hst:hst/hst:configurations/unittestproject").addNode("hst:workspace", "hst:workspace");
        }
        session.move("/hst:hst/hst:configurations/unittestproject/hst:sitemap",
                "/hst:hst/hst:configurations/unittestproject/hst:workspace/hst:sitemap");
        session.save();
        session.logout();
    }

    @After
    public void tearDown() throws Exception {
        final Session session = createSession("admin", "admin");
        AbstractPageComposerTest.restoreHstConfigBackup(session);
        session.logout();
        super.tearDown();
    }

    @Test
    public void start_edit_creating_preview_config_as_admin() throws Exception {
        startEditAssertions(ADMIN_CREDENTIALS, true);
    }

    @Test
    public void start_edit_creating_preview_config_as_webmaster() throws Exception {
        startEditAssertions(EDITOR_CREDENTIALS, true);
    }

    @Test
    public void liveuser_cannot_start_edit() throws Exception {
        Credentials liveUserCreds= HstServices.getComponentManager().getComponent(Credentials.class.getName() + ".default.delegating");
        startEditAssertions(liveUserCreds, false);
    }

    private Map<String, Object> startEdit(final Credentials creds) throws RepositoryException, IOException, ServletException {
        final String mountId = getNodeId("/hst:hst/hst:hosts/dev-localhost/localhost/hst:root");
        final RequestResponseMock requestResponse = mockGetRequestResponse(
                "http", "localhost", "/_rp/"+ mountId + "./edit", null, "POST");

        final MockHttpServletResponse response = render(mountId, requestResponse, creds);
        final String restResponse = response.getContentAsString();
        return mapper.reader(Map.class).readValue(restResponse);
    }

    private void startEditAssertions(final Credentials creds, final boolean shouldSucceed) throws RepositoryException, IOException, ServletException {
        final Map<String, Object> responseMap = startEdit(creds);
        if (shouldSucceed) {
            assertEquals(Boolean.TRUE, responseMap.get("success"));
            assertEquals("Site can be edited now", responseMap.get("message"));
        } else {
            assertEquals(Boolean.FALSE, responseMap.get("success"));
            assertTrue(responseMap.get("message").toString().contains("Could not create a preview configuration"));
        }
    }

    @Test
    public void copy_a_page_as_admin() throws Exception {
        copyPageAssertions(ADMIN_CREDENTIALS, true);
    }

    @Test
    public void copy_a_page_as_editor() throws Exception {
        copyPageAssertions(EDITOR_CREDENTIALS, true);
    }

    @Test
    public void liveuser_cannot_copy_page() throws Exception {
        Credentials liveUserCreds= HstServices.getComponentManager().getComponent(Credentials.class.getName() + ".default.delegating");
        copyPageAssertions(liveUserCreds, false);
    }

    private Map<String, Object> copyPage(final Credentials creds) throws RepositoryException, IOException, ServletException {
        startEdit(ADMIN_CREDENTIALS);

        final String mountId = getNodeId("/hst:hst/hst:hosts/dev-localhost/localhost/hst:root");
        final String siteMapId = getNodeId("/hst:hst/hst:configurations/unittestproject-preview/hst:workspace/hst:sitemap");
        final String siteMapItemToCopyId = getNodeId("/hst:hst/hst:configurations/unittestproject-preview/hst:workspace/hst:sitemap/home");

        final RequestResponseMock requestResponse = mockGetRequestResponse(
                "http", "localhost", "/_rp/"+ siteMapId + "./copy", null, "POST");

        requestResponse.getRequest().addHeader("mountId", mountId);
        requestResponse.getRequest().addHeader("siteMapItemUUID", siteMapItemToCopyId);
        requestResponse.getRequest().addHeader("targetName", "home-copy");

        final MockHttpServletResponse response = render(mountId, requestResponse, creds);
        final String restResponse = response.getContentAsString();
        return mapper.reader(Map.class).readValue(restResponse);
    }

    private void copyPageAssertions(final Credentials creds, final boolean shouldSucceed) throws Exception {
        // first create preview config with admin creds
        final Map<String, Object> responseMap = copyPage(creds);
        if (shouldSucceed) {
            assertEquals(Boolean.TRUE, responseMap.get("success"));
            assertEquals("Item created successfully", responseMap.get("message"));

            final Session admin = createSession("admin", "admin");
            final String workspacePath = "/hst:hst/hst:configurations/unittestproject-preview/hst:workspace";
            assertTrue(admin.getNode(workspacePath + "/hst:sitemap/home-copy").getProperty(GENERAL_PROPERTY_LOCKED_BY).getString().equals(((SimpleCredentials)creds).getUserID()));
            assertTrue(admin.getNode(workspacePath + "/hst:pages/home-copy").getProperty(GENERAL_PROPERTY_LOCKED_BY).getString().equals(((SimpleCredentials)creds).getUserID()));
            admin.logout();
        } else {
            assertEquals(Boolean.FALSE, responseMap.get("success"));
        }
    }

    @Test
    public void publish_as_admin_changes_of_admin() throws Exception {
        publishAssertions(ADMIN_CREDENTIALS, true);
    }

    @Test
    public void publish_as_editor_changes_of_editor() throws Exception {
        publishAssertions(EDITOR_CREDENTIALS, true);
    }

    private Map<String, Object> publish(final Credentials creds) throws Exception {
        final String mountId = getNodeId("/hst:hst/hst:hosts/dev-localhost/localhost/hst:root");
        final RequestResponseMock requestResponse = mockGetRequestResponse(
                "http", "localhost", "/_rp/"+ mountId + "./publish", null, "POST");

        final MockHttpServletResponse response = render(mountId, requestResponse, creds);
        final String restResponse = response.getContentAsString();
        return mapper.reader(Map.class).readValue(restResponse);
    }

    private void publishAssertions(final Credentials creds, final boolean shouldSucceed) throws Exception {
        // first force a change
        copyPage(creds);
        final Map<String, Object> responseMap = publish(creds);
        if (shouldSucceed) {
            assertEquals(Boolean.TRUE, responseMap.get("success"));
            assertEquals("Site is published", responseMap.get("message"));
        } else {
            assertEquals(Boolean.FALSE, responseMap.get("success"));
        }
    }

    @Test
    public void discard_as_admin_changes_of_admin() throws Exception {
        discardAssertions(ADMIN_CREDENTIALS, true);
    }

    @Test
    public void discard_as_editor_changes_of_editor() throws Exception {
        discardAssertions(EDITOR_CREDENTIALS, true);
    }

    private Map<String, Object> discard(final Credentials creds) throws Exception {
        final String mountId = getNodeId("/hst:hst/hst:hosts/dev-localhost/localhost/hst:root");
        final RequestResponseMock requestResponse = mockGetRequestResponse(
                "http", "localhost", "/_rp/"+ mountId + "./discard", null, "POST");

        final MockHttpServletResponse response = render(mountId, requestResponse, creds);
        final String restResponse = response.getContentAsString();
        return mapper.reader(Map.class).readValue(restResponse);
    }

    private void discardAssertions(final Credentials creds, final boolean shouldSucceed) throws Exception {
        // first force a change
        copyPage(creds);
        final Map<String, Object> responseMap = discard(creds);
        if (shouldSucceed) {
            assertEquals(Boolean.TRUE, responseMap.get("success"));
            assertTrue(responseMap.get("message").toString().contains("discarded"));
        } else {
            assertEquals(Boolean.FALSE, responseMap.get("success"));
        }
    }

    /**
     * Normally the security model in production is read from jcr node
     * /hippo:configuration/hippo:frontend/cms/hippo-channel-manager/channel-manager-perspective/templatecomposer
     * During these unit tests, we we change the above path to /hst:hst/hst:hosts/dev-localhost/localhost/hst:root and
     * try to read properies 'manage.changes.privileges' and 'manage.changes.privileges.path' from there. However during this
     * unit test, these props are not yet set.
     *
     * However, this node is by default not present, and as a result, the org.hippoecm.hst.pagecomposer.jaxrs.security.SecurityModel
     * cannot map ChannelManagerAdmin to 'hippo:admin'
     * Note that @Path("/userswithchanges/publish") on MountResource has the annotation @RolesAllowed(CHANNEL_MANAGER_ADMIN_ROLE)
     */
    @Test
    public void publish_userswithchanges_as_admin_fails_if_security_model_cannot_be_loaded() throws Exception {
        publishAssertions(ADMIN_CREDENTIALS, EDITOR_CREDENTIALS, false);
    }

    @Test
    public void publish_userswithchanges_as_admin_succeeds_if_security_model_can_be_loaded() throws Exception {
        setPrivilegePropsForSecurityModel();
        publishAssertions(ADMIN_CREDENTIALS, EDITOR_CREDENTIALS, true);
    }

    @Test
    public void publish_userswithchanges_as_editor_fails_regardless_of_security_model_can_be_loaded() throws Exception {
        publishAssertions(EDITOR_CREDENTIALS, EDITOR_CREDENTIALS, false);
    }

    @Test
    public void publish_userswithchanges_as_editor_fails_if_security_model_can_be_loaded() throws Exception {
        setPrivilegePropsForSecurityModel();
        publishAssertions(EDITOR_CREDENTIALS, EDITOR_CREDENTIALS, false);
    }

    private MockHttpServletResponse publish(final Credentials publishCreds, final SimpleCredentials changesCreds) throws Exception {
        final String mountId = getNodeId("/hst:hst/hst:hosts/dev-localhost/localhost/hst:root");
        final RequestResponseMock requestResponse = mockGetRequestResponse(
                "http", "localhost", "/_rp/"+ mountId + "./userswithchanges/publish", null, "POST");

        final MockHttpServletRequest request = requestResponse.getRequest();
        final ExtIdsRepresentation extIdsRepresentation = new ExtIdsRepresentation();
        extIdsRepresentation.setData(singletonList(changesCreds.getUserID()));
        final String idsToPublish = mapper.writeValueAsString(extIdsRepresentation);
        request.setContent(idsToPublish.getBytes("UTF-8"));
        request.setContentType("application/json");
        return render(mountId, requestResponse, publishCreds);
    }

    private void publishAssertions(final Credentials publishCreds, final SimpleCredentials changesCreds, final boolean shouldSucceed) throws Exception {
        // first force a change *by* changesCreds
        copyPage(changesCreds);
        final MockHttpServletResponse response = publish(publishCreds, changesCreds);

        if (shouldSucceed) {
            final String restResponse = response.getContentAsString();
            final Map<String, Object> responseMap = mapper.reader(Map.class).readValue(restResponse);

            assertEquals(Boolean.TRUE, responseMap.get("success"));
            assertEquals("Site is published", responseMap.get("message"));
        } else {
            assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        }
    }

    private void setPrivilegePropsForSecurityModel() throws RepositoryException {
        final Session admin = createSession("admin", "admin");
        final Node mount = admin.getNode("/hst:hst/hst:hosts/dev-localhost/localhost/hst:root");
        // make sure that users that have 'hippo:admin' role on /hst:hst/hst:channels can publish other ones their changes
        mount.setProperty("manage.changes.privileges","hippo:admin");
        mount.setProperty("manage.changes.privileges.path","/hst:hst/hst:channels");
        admin.save();
        admin.logout();
    }

    @Test
    public void discard_userswithchanges_as_admin_fails_if_security_model_cannot_be_loaded() throws Exception {
        discardAssertions(ADMIN_CREDENTIALS, EDITOR_CREDENTIALS, false);
    }

    @Test
    public void discard_userswithchanges_as_admin_succeeds_if_security_model_can_be_loaded() throws Exception {
        setPrivilegePropsForSecurityModel();
        discardAssertions(ADMIN_CREDENTIALS, EDITOR_CREDENTIALS, true);
    }

    @Test
    public void discard_userswithchanges_as_editor_fails_regardless_of_security_model_can_be_loaded() throws Exception {
        discardAssertions(EDITOR_CREDENTIALS, EDITOR_CREDENTIALS, false);
    }

    @Test
    public void discard_userswithchanges_as_editor_fails_if_security_model_can_be_loaded() throws Exception {
        setPrivilegePropsForSecurityModel();
        discardAssertions(EDITOR_CREDENTIALS, EDITOR_CREDENTIALS, false);
    }

    private MockHttpServletResponse discard(final Credentials publishCreds, final SimpleCredentials changesCreds) throws Exception {
        final String mountId = getNodeId("/hst:hst/hst:hosts/dev-localhost/localhost/hst:root");
        final RequestResponseMock requestResponse = mockGetRequestResponse(
                "http", "localhost", "/_rp/"+ mountId + "./userswithchanges/discard", null, "POST");

        final MockHttpServletRequest request = requestResponse.getRequest();
        final ExtIdsRepresentation extIdsRepresentation = new ExtIdsRepresentation();
        extIdsRepresentation.setData(singletonList(changesCreds.getUserID()));
        final String idsToPublish = mapper.writeValueAsString(extIdsRepresentation);
        request.setContent(idsToPublish.getBytes("UTF-8"));
        request.setContentType("application/json");
        return render(mountId, requestResponse, publishCreds);
    }

    private void discardAssertions(final Credentials publishCreds, final SimpleCredentials changesCreds, final boolean shouldSucceed) throws Exception {
        // first force a change *by* changesCreds
        copyPage(changesCreds);
        final MockHttpServletResponse response = discard(publishCreds, changesCreds);

        if (shouldSucceed) {
            final String restResponse = response.getContentAsString();
            final Map<String, Object> responseMap = mapper.reader(Map.class).readValue(restResponse);

            assertEquals(Boolean.TRUE, responseMap.get("success"));
            assertTrue(responseMap.get("message").toString().contains("discarded"));
        } else {
            assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        }
    }

    @Test
    public void create_branch_as_admin() throws Exception {
        createBranchAssertions(ADMIN_CREDENTIALS, "foo");
    }

    @Test
    public void create_branch_as_webmaster() throws Exception {
        createBranchAssertions(EDITOR_CREDENTIALS, "foo");
    }

    @Test
    public void branch_is_always_created_from_live_and_not_from_preview() throws Exception {
        // make sure to trigger some changes in preview of unittestproject before creating a branch
        copyPage(ADMIN_CREDENTIALS);
        Session session = createSession("admin", "admin");
        assertFalse(session.nodeExists("/hst:hst/hst:configurations/unittestproject/hst:workspace/hst:sitemap/home-copy"));
        assertTrue(session.nodeExists("/hst:hst/hst:configurations/unittestproject-preview/hst:workspace/hst:sitemap/home-copy"));

        createBranch(ADMIN_CREDENTIALS, "foo");

        // now we expect the branch *not* to contain 'home-copy' because that page was not yet in live
        assertTrue(session.nodeExists("/hst:hst/hst:configurations/unittestproject-foo/hst:workspace/hst:sitemap"));
        assertFalse(session.nodeExists("/hst:hst/hst:configurations/unittestproject-foo/hst:workspace/hst:sitemap/home-copy"));

        // now remove the created branch, publish the preview page, and create the branch again: Then we expect the
        // home-copy to be there
        session.getNode("/hst:hst/hst:configurations/unittestproject-foo").remove();
        session.save();
        publish(ADMIN_CREDENTIALS);
        assertTrue(session.nodeExists("/hst:hst/hst:configurations/unittestproject/hst:workspace/hst:sitemap/home-copy"));
        assertTrue(session.nodeExists("/hst:hst/hst:configurations/unittestproject-preview/hst:workspace/hst:sitemap/home-copy"));

        createBranch(ADMIN_CREDENTIALS, "foo");
        // now we expect the branch to contain 'home-copy' because that page was in live
        assertTrue(session.nodeExists("/hst:hst/hst:configurations/unittestproject-foo/hst:workspace/hst:sitemap/home-copy"));

        session.logout();
    }

    @Test
    public void creating_an_existing_branch_not_allowed() throws Exception {
        createBranch(ADMIN_CREDENTIALS, "foo");
        final Map<String, Object> responseMap = createBranch(ADMIN_CREDENTIALS, "foo");

        assertEquals(Boolean.FALSE, responseMap.get("success"));
        assertEquals("ITEM_EXISTS", responseMap.get("errorCode"));
    }

    private Map<String, Object> createBranch(final Credentials creds, final String branchName) throws RepositoryException, IOException, ServletException {
        final String mountId = getNodeId("/hst:hst/hst:hosts/dev-localhost/localhost/hst:root");
        final RequestResponseMock requestResponse = mockGetRequestResponse(
                "http", "localhost", "/_rp/"+ mountId + "./createbranch/"+ branchName, null, "PUT");

        final MockHttpServletResponse response = render(mountId, requestResponse, creds);
        final String restResponse = response.getContentAsString();
        return mapper.reader(Map.class).readValue(restResponse);
    }

    private void createBranchAssertions(final Credentials creds, final String branchName) throws RepositoryException, IOException, ServletException {
        final Map<String, Object> responseMap = createBranch(creds, branchName);
        assertEquals(Boolean.TRUE, responseMap.get("success"));
        assertEquals("Branch created successfully", responseMap.get("message"));

        Session session = createSession("admin", "admin");
        assertTrue(session.nodeExists("/hst:hst/hst:configurations/unittestproject/hst:workspace"));
        assertTrue(session.nodeExists("/hst:hst/hst:configurations/unittestproject/hst:workspace/hst:sitemap"));
        assertFalse(session.nodeExists("/hst:hst/hst:configurations/unittestproject/hst:workspace/hst:pages"));

        // the created branch *does* have pages as well in the workspace because always automatically added to a branch

        assertTrue(session.nodeExists("/hst:hst/hst:configurations/unittestproject-"+branchName+"/hst:workspace"));
        assertTrue(session.nodeExists("/hst:hst/hst:configurations/unittestproject-"+branchName+"/hst:workspace/hst:sitemap"));
        assertTrue(session.nodeExists("/hst:hst/hst:configurations/unittestproject-"+branchName+"/hst:workspace/hst:pages"));

        Node branchHstConfigNode = session.getNode("/hst:hst/hst:configurations/unittestproject-" + branchName);
        assertArrayEquals(new String[]{"../unittestproject"}, getMultipleStringProperty(branchHstConfigNode, GENERAL_PROPERTY_INHERITS_FROM, null));
        assertTrue(branchHstConfigNode.isNodeType(MIXINTYPE_HST_BRANCH));


        assertEquals("unittestproject", branchHstConfigNode.getProperty(BRANCH_PROPERTY_BRANCHOF).getString());


        session.logout();

    }

}
