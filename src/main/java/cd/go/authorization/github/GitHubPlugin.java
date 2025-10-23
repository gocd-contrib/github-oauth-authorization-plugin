/*
 * Copyright 2022 Thoughtworks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.authorization.github;

import cd.go.authorization.github.exceptions.NoSuchRequestHandlerException;
import cd.go.authorization.github.executors.*;
import cd.go.authorization.github.requests.*;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static cd.go.authorization.github.Constants.PLUGIN_IDENTIFIER;

@Extension
public class GitHubPlugin implements GoPlugin {
    private static final Logger LOG = Logger.getLoggerFor(GitHubPlugin.class);

    static {
        // Redirect logging from the GitHub API and OKHttp to SLF4J and thus GoCD's logging system
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor accessor) {}

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        try {
            return switch (RequestFromServer.fromString(request.requestName())) {
                case REQUEST_GET_PLUGIN_ICON -> new GetPluginIconRequestExecutor().execute();
                case REQUEST_GET_CAPABILITIES -> new GetCapabilitiesRequestExecutor().execute();
                case REQUEST_GET_AUTH_CONFIG_METADATA -> new GetAuthConfigMetadataRequestExecutor().execute();
                case REQUEST_AUTH_CONFIG_VIEW -> new GetAuthConfigViewRequestExecutor().execute();
                case REQUEST_VALIDATE_AUTH_CONFIG -> AuthConfigValidateRequest.from(request).execute();
                case REQUEST_VERIFY_CONNECTION -> VerifyConnectionRequest.from(request).execute();
                case REQUEST_GET_ROLE_CONFIG_METADATA -> new GetRoleConfigMetadataRequestExecutor().execute();
                case REQUEST_ROLE_CONFIG_VIEW -> new GetRoleConfigViewRequestExecutor().execute();
                case REQUEST_VALIDATE_ROLE_CONFIG -> RoleConfigValidateRequest.from(request).execute();
                case REQUEST_AUTHORIZATION_SERVER_REDIRECT_URL -> GetAuthorizationServerUrlRequest.from(request).execute();
                case REQUEST_ACCESS_TOKEN -> FetchAccessTokenRequest.from(request).execute();
                case REQUEST_IS_VALID_USER -> ValidateUserRequest.from(request).execute();
                case REQUEST_AUTHENTICATE_USER -> UserAuthenticationRequest.from(request).execute();
                case REQUEST_SEARCH_USERS -> SearchUsersRequest.from(request).execute();
                case REQUEST_GET_USER_ROLES -> GetRolesRequest.from(request).execute();
            };
        } catch (NoSuchRequestHandlerException e) {
            LOG.warn(e.getMessage());
            return null;
        } catch (Exception e) {
            LOG.error("Error while executing request " + request.requestName(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return PLUGIN_IDENTIFIER;
    }
}
