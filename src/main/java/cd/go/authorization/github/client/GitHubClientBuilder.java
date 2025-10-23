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

package cd.go.authorization.github.client;

import cd.go.authorization.github.models.AuthenticateWith;
import cd.go.authorization.github.models.GitHubConfiguration;
import com.thoughtworks.go.plugin.api.logging.Logger;
import okhttp3.*;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.GitHubRateLimitHandler;
import org.kohsuke.github.connector.GitHubConnector;
import org.kohsuke.github.extras.okhttp3.OkHttpGitHubConnector;

import java.io.IOException;

public class GitHubClientBuilder {
    private static final Logger LOG = Logger.getLoggerFor(GitHubClientBuilder.class);

    public static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder().build();
    private static final int OKHTTP_CACHE_MAX_AGE_SECONDS = 60;
    private static final GitHubConnector GITHUB_CONNECTOR = new OkHttpGitHubConnector(HTTP_CLIENT, OKHTTP_CACHE_MAX_AGE_SECONDS);

    public GitHub fromServerPersonalAccessToken(GitHubConfiguration gitHubConfiguration) throws IOException {
        return clientFor(gitHubConfiguration.personalAccessToken(), gitHubConfiguration);
    }

    public GitHub fromUserOAuthAccessToken(String accessToken, GitHubConfiguration gitHubConfiguration) throws IOException {
        return clientFor(accessToken, gitHubConfiguration);
    }

    private GitHub clientFor(String personalAccessTokenOrUsersAccessToken, GitHubConfiguration gitHubConfiguration) throws IOException {
        if (gitHubConfiguration.authenticateWith() == AuthenticateWith.GITHUB_ENTERPRISE) {
            LOG.debug("Create GitHub connection to enterprise GitHub with token");
            return GitHub.connectToEnterpriseWithOAuth(gitHubConfiguration.gitHubEnterpriseApiUrl(), null, personalAccessTokenOrUsersAccessToken);
        } else {
            LOG.debug("Create GitHub connection to public GitHub with token");
            return new GitHubBuilder()
                    .withConnector(GITHUB_CONNECTOR)
                    .withOAuthToken(personalAccessTokenOrUsersAccessToken)
                    .withRateLimitHandler(GitHubRateLimitHandler.FAIL)
                    .build();
        }
    }

    public AuthorizationServerArgs authorizationServerArgs(GitHubConfiguration config, String callbackUrl) {
        String state = StateGenerator.generate();
        ProofKey proofKey = new ProofKey();
        String authorizationServerUrl = HttpUrl.parse(config.authenticateWith() == AuthenticateWith.GITHUB ? GitHubConfiguration.GITHUB_URL : config.gitHubEnterpriseUrl())
                .newBuilder()
                .addPathSegment("login")
                .addPathSegment("oauth")
                .addPathSegment("authorize")
                .addQueryParameter("client_id", config.clientId())
                .addQueryParameter("redirect_uri", callbackUrl)
                .addQueryParameter("response_type", "code")
                .addQueryParameter("scope", config.scope())
                .addQueryParameter("state", state)
                .addQueryParameter("code_challenge_method", "S256")
                .addEncodedQueryParameter("code_challenge", proofKey.codeChallengeEncoded())
                .build().toString();
        return new AuthorizationServerArgs(authorizationServerUrl, state, proofKey.codeVerifierEncoded());
    }

    public Call accessTokenRequestFrom(GitHubConfiguration config, String authorizationCode, String codeVerifierEncoded) {
        HttpUrl accessTokenUrl = HttpUrl.parse(config.authenticateWith() == AuthenticateWith.GITHUB ? GitHubConfiguration.GITHUB_URL : config.gitHubEnterpriseUrl())
                .newBuilder()
                .addPathSegment("login")
                .addPathSegment("oauth")
                .addPathSegment("access_token")
                .build();

        return HTTP_CLIENT.newCall(new Request.Builder()
                .url(accessTokenUrl)
                .addHeader("Accept", "application/json")
                .post(new FormBody.Builder()
                        .add("client_id", config.clientId())
                        .add("client_secret", config.clientSecret())
                        .add("code", authorizationCode)
                        .add("code_verifier", codeVerifierEncoded)
                        .build())
                .build());
    }

}
