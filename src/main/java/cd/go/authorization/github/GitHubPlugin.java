/*
 * Copyright 2017 ThoughtWorks, Inc.
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
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import static cd.go.authorization.github.Constants.PLUGIN_IDENTIFIER;

@Extension
public class GitHubPlugin implements GoPlugin {
    public static final Logger LOG = Logger.getLoggerFor(GitHubPlugin.class);

    private GoApplicationAccessor accessor;

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        try {
            switch (RequestFromServer.fromString(request.requestName())) {
                case REQUEST_GET_PLUGIN_ICON:
                    return new GetPluginIconRequestExecutor().execute();
                case REQUEST_GET_CAPABILITIES:
                    return new GetCapabilitiesRequestExecutor().execute();
                case REQUEST_GET_AUTH_CONFIG_METADATA:
                    return new GetAuthConfigMetadataRequestExecutor().execute();
                case REQUEST_AUTH_CONFIG_VIEW:
                    return new GetAuthConfigViewRequestExecutor().execute();
                case REQUEST_VALIDATE_AUTH_CONFIG:
                    return AuthConfigValidateRequest.from(request).execute();
                case REQUEST_VERIFY_CONNECTION:
                    return VerifyConnectionRequest.from(request).execute();
                case REQUEST_GET_ROLE_CONFIG_METADATA:
                    return new GetRoleConfigMetadataRequestExecutor().execute();
                case REQUEST_ROLE_CONFIG_VIEW:
                    return new GetRoleConfigViewRequestExecutor().execute();
                case REQUEST_VALIDATE_ROLE_CONFIG:
                    return RoleConfigValidateRequest.from(request).execute();
                case REQUEST_AUTHORIZATION_SERVER_REDIRECT_URL:
                    return GetAuthorizationServerUrlRequest.from(request).execute();
                case REQUEST_ACCESS_TOKEN:
                    return FetchAccessTokenRequest.from(request).execute();
                case REQUEST_IS_VALID_USER:
                    return ValidateUserRequest.from(request).execute();
                case REQUEST_AUTHENTICATE_USER:
                    return UserAuthenticationRequest.from(request).execute();
                case REQUEST_SEARCH_USERS:
                    return SearchUsersRequest.from(request).execute();
                case REQUEST_GET_USER_ROLES:
                    return GetRolesRequest.from(request).execute();
                default:
                    throw new UnhandledRequestTypeException(request.requestName());
            }
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
