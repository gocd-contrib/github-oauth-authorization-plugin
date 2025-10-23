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

import cd.go.authorization.github.client.GitHubClientBuilder;
import cd.go.authorization.github.exceptions.AuthenticationException;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.OAuthTokenInfo;
import cd.go.authorization.github.requests.FetchAccessTokenRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import okhttp3.Call;
import okhttp3.Response;

import static cd.go.authorization.github.GitHubPlugin.LOG;
import static java.lang.String.format;

public class FetchAccessTokenRequestExecutor implements RequestExecutor {
    private final FetchAccessTokenRequest request;
    private final GitHubClientBuilder gitHubClientBuilder;

    public FetchAccessTokenRequestExecutor(FetchAccessTokenRequest request) {
        this(request, new GitHubClientBuilder());
    }

    FetchAccessTokenRequestExecutor(FetchAccessTokenRequest request, GitHubClientBuilder gitHubClientBuilder) {
        this.request = request;
        this.gitHubClientBuilder = gitHubClientBuilder;
    }

    public GoPluginApiResponse execute() throws Exception {
        final AuthConfig authConfig = request.firstAuthConfig();
        request.validateState();

        final Call request = gitHubClientBuilder.accessTokenRequestFrom(authConfig.gitHubConfiguration(), this.request.authorizationCode(), this.request.codeVerifierEncoded());
        try (Response response = request.execute()) {
            if (response.isSuccessful()) {
                String responseString = response.body().string();
                final OAuthTokenInfo tokenInfo = OAuthTokenInfo.fromJSON(responseString);
                LOG.info("[Fetch Access Token] GitHub OAuth access token for user fetched successfully with scope `%s`.", tokenInfo.scope());
                return DefaultGoPluginApiResponse.success(tokenInfo.toJSON());
            }
            throw new AuthenticationException(format("[Fetch Access Token] %s", response.message()));
        }
    }


}
