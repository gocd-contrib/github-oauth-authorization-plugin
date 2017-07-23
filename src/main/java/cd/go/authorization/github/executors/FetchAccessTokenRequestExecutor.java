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

import cd.go.authorization.github.GitHubProvider;
import cd.go.authorization.github.exceptions.NoAuthorizationConfigurationException;
import cd.go.authorization.github.providermanager.GitHubProviderManager;
import cd.go.authorization.github.models.TokenInfo;
import cd.go.authorization.github.requests.FetchAccessTokenRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

public class FetchAccessTokenRequestExecutor implements RequestExecutor {
    private final FetchAccessTokenRequest request;
    private final GitHubProviderManager providerManager;

    public FetchAccessTokenRequestExecutor(FetchAccessTokenRequest request) {
        this(request, GitHubProviderManager.getInstance());
    }

    FetchAccessTokenRequestExecutor(FetchAccessTokenRequest request, GitHubProviderManager providerManager) {
        this.request = request;
        this.providerManager = providerManager;
    }

    public GoPluginApiResponse execute() throws Exception {
        if (request.authConfigs() == null || request.authConfigs().isEmpty()) {
            throw new NoAuthorizationConfigurationException("[Get Access Token] No authorization configuration found.");
        }

        final GitHubProvider provider = providerManager.getGitHubProvider(request.authConfigs().get(0));
        final TokenInfo tokenInfo = provider.accessToken(request.requestParameters());
        return DefaultGoPluginApiResponse.success(tokenInfo.toJSON());
    }
}
