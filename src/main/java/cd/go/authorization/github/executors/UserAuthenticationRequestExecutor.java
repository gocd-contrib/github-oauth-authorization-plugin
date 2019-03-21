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

package cd.go.authorization.github.executors;

import cd.go.authorization.github.GitHubAuthenticator;
import cd.go.authorization.github.GitHubAuthorizer;
import cd.go.authorization.github.exceptions.NoAuthorizationConfigurationException;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.LoggedInUserInfo;
import cd.go.authorization.github.requests.UserAuthenticationRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.HashMap;
import java.util.Map;

import static cd.go.authorization.github.utils.Util.GSON;

public class UserAuthenticationRequestExecutor implements RequestExecutor {
    private final UserAuthenticationRequest request;
    private final GitHubAuthenticator gitHubAuthenticator;
    private final GitHubAuthorizer gitHubAuthorizer;

    public UserAuthenticationRequestExecutor(UserAuthenticationRequest request) {
        this(request, new GitHubAuthenticator(), new GitHubAuthorizer());
    }

    UserAuthenticationRequestExecutor(UserAuthenticationRequest request, GitHubAuthenticator gitHubAuthenticator, GitHubAuthorizer gitHubAuthorizer) {
        this.request = request;
        this.gitHubAuthenticator = gitHubAuthenticator;
        this.gitHubAuthorizer = gitHubAuthorizer;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        if (request.authConfigs() == null || request.authConfigs().isEmpty()) {
            throw new NoAuthorizationConfigurationException("[Authenticate] No authorization configuration found.");
        }

        final AuthConfig authConfig = request.authConfigs().get(0);
        final LoggedInUserInfo loggedInUserInfo = gitHubAuthenticator.authenticate(request.tokenInfo(), authConfig);

        Map<String, Object> userMap = new HashMap<>();
        if (loggedInUserInfo != null) {
            userMap.put("user", loggedInUserInfo.getUser());
            userMap.put("roles", gitHubAuthorizer.authorize(loggedInUserInfo.getGitHubUser(), authConfig, request.roles()));
        }

        return DefaultGoPluginApiResponse.success(GSON.toJson(userMap));
    }
}
