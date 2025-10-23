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
import cd.go.authorization.github.client.AuthorizationServerArgs;
import cd.go.authorization.github.client.GitHubClientBuilder;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.GitHubConfiguration;
import cd.go.authorization.github.requests.GetAuthorizationServerUrlRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Map;

import static cd.go.authorization.github.requests.GetAuthorizationServerUrlRequest.LOG;
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

    public GoPluginApiResponse execute() {
        final AuthConfig authConfig = request.firstAuthConfig();
        LOG.info("Initiating GitHub OAuth authentication from auth config `{}`", authConfig.getId());
        final GitHubConfiguration gitHubConfiguration = authConfig.gitHubConfiguration();

        AuthorizationServerArgs result = gitHubClientBuilder.authorizationServerArgs(gitHubConfiguration, request.callbackUrl());

        return DefaultGoPluginApiResponse.success(GSON.toJson(Map.of(
                "authorization_server_url", result.url(),
                "auth_session", Map.of(Constants.AUTH_SESSION_STATE, result.state(), Constants.AUTH_CODE_VERIFIER_ENCODED, result.codeVerifierEncoded())
        )));
    }
}
