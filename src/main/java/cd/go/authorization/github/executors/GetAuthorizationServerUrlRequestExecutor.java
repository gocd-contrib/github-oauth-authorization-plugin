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

package cd.go.authorization.github.executors;

import cd.go.authorization.github.Constants;
import cd.go.authorization.github.client.GitHubClientBuilder;
import cd.go.authorization.github.exceptions.NoAuthorizationConfigurationException;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.GitHubConfiguration;
import cd.go.authorization.github.requests.GetAuthorizationServerUrlRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.List;
import java.util.Map;

import static cd.go.authorization.github.GitHubPlugin.LOG;
import static cd.go.authorization.github.utils.Util.GSON;

public class GetAuthorizationServerUrlRequestExecutor implements RequestExecutor {
    private final GetAuthorizationServerUrlRequest request;
    private final GitHubClientBuilder gitHubClientBuilder;

    public GetAuthorizationServerUrlRequestExecutor(GetAuthorizationServerUrlRequest request) {
        this(request, new GitHubClientBuilder());
    }

    public GetAuthorizationServerUrlRequestExecutor(GetAuthorizationServerUrlRequest request, GitHubClientBuilder gitHubClientBuilder) {
        this.request = request;
        this.gitHubClientBuilder = gitHubClientBuilder;
    }

    public GoPluginApiResponse execute() throws Exception {
        if (request.authConfigs() == null || request.authConfigs().isEmpty()) {
            throw new NoAuthorizationConfigurationException("[Authorization Server Url] No authorization configuration found.");
        }

        LOG.debug("[Get Authorization Server URL] Getting authorization server url from auth config.");

        final AuthConfig authConfig = request.authConfigs().get(0);
        final GitHubConfiguration gitHubConfiguration = authConfig.gitHubConfiguration();

        List<String> result = gitHubClientBuilder.authorizationServerArgs(gitHubConfiguration, request.callbackUrl());

        return DefaultGoPluginApiResponse.success(GSON.toJson(Map.of(
                "authorization_server_url", result.get(0),
                "auth_session", Map.of(Constants.AUTH_SESSION_STATE, result.get(1), Constants.AUTH_CODE_VERIFIER_ENCODED, result.get(2))
        )));
    }
}
