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

import cd.go.authorization.github.exceptions.AuthenticationException;
import cd.go.authorization.github.exceptions.NoAuthorizationConfigurationException;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.GitHubConfiguration;
import cd.go.authorization.github.models.TokenInfo;
import cd.go.authorization.github.requests.FetchAccessTokenRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import okhttp3.*;

import static cd.go.authorization.github.GitHubPlugin.LOG;
import static java.text.MessageFormat.format;

public class FetchAccessTokenRequestExecutor implements RequestExecutor {
    private final FetchAccessTokenRequest request;
    private final OkHttpClient httpClient;

    public FetchAccessTokenRequestExecutor(FetchAccessTokenRequest request) {
        this(request, new OkHttpClient());
    }

    FetchAccessTokenRequestExecutor(FetchAccessTokenRequest request, OkHttpClient httpClient) {
        this.request = request;
        this.httpClient = httpClient;
    }

    public GoPluginApiResponse execute() throws Exception {
        if (request.authConfigs() == null || request.authConfigs().isEmpty()) {
            throw new NoAuthorizationConfigurationException("[Get Access Token] No authorization configuration found.");
        }

        if (!request.requestParameters().containsKey("code")) {
            throw new IllegalArgumentException("Get Access Token] Expecting `code` in request params, but not received.");
        }

        request.validateState();

        final AuthConfig authConfig = request.authConfigs().get(0);
        final GitHubConfiguration gitHubConfiguration = authConfig.gitHubConfiguration();

        final Request fetchAccessTokenRequest = accessTokenRequestFrom(gitHubConfiguration);

        final Response response = httpClient.newCall(fetchAccessTokenRequest).execute();
        if (response.isSuccessful()) {
            LOG.info("[Get Access Token] Access token fetched successfully.");
            final TokenInfo tokenInfo = TokenInfo.fromJSON(response.body().string());
            return DefaultGoPluginApiResponse.success(tokenInfo.toJSON());
        }

        throw new AuthenticationException(format("[Get Access Token] {0}", response.message()));
    }

    private Request accessTokenRequestFrom(GitHubConfiguration gitHubConfiguration) {
        return new Request.Builder()
                .url(accessTokenUrl(gitHubConfiguration))
                .addHeader("Accept", "application/json")
                .post(new FormBody.Builder()
                        .add("client_id", gitHubConfiguration.clientId())
                        .add("client_secret", gitHubConfiguration.clientSecret())
                        .add("code", request.requestParameters().get("code"))
                        .build())
                .build();
    }

    private HttpUrl accessTokenUrl(GitHubConfiguration gitHubConfiguration) {
        return HttpUrl.parse(gitHubConfiguration.apiUrl())
                .newBuilder()
                .addPathSegment("login")
                .addPathSegment("oauth")
                .addPathSegment("access_token")
                .build();
    }
}
