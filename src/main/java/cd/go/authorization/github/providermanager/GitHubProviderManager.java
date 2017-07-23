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

package cd.go.authorization.github.providermanager;

import cd.go.authorization.github.GitHubProvider;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.AuthenticateWith;
import cd.go.authorization.github.models.GitHubConfiguration;
import org.brickred.socialauth.SocialAuthConfig;
import org.brickred.socialauth.SocialAuthManager;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static cd.go.authorization.github.GitHubPlugin.LOG;
import static java.text.MessageFormat.format;

public class GitHubProviderManager {
    private static final GitHubProviderManager PROVIDER_MANAGER = new GitHubProviderManager();
    private final Map<String, GitHubProvider> cache = new HashMap<>();

    private GitHubProviderManager() {
    }

    public GitHubProvider getGitHubProvider(AuthConfig authConfig) {
        final GitHubConfiguration gitHubConfiguration = authConfig.gitHubConfiguration();
        final GitHubProvider provider = cache.get(authConfig.getId());

        if (provider != null && provider.gitHubConfiguration().equals(gitHubConfiguration)) {
            return provider;
        }

        final GitHubProvider newProvider = createProvider(authConfig);
        registerProvider(authConfig.getId(), newProvider);

        return newProvider;
    }

    public GitHubProvider getTemporaryGitHubProvider(GitHubConfiguration gitHubConfiguration) {
        try {
            return new GitHubProvider(gitHubConfiguration, createSocialAuthManager(gitHubConfiguration), createGitHub(gitHubConfiguration));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create GitHubProvider", e);
        }
    }

    protected void registerProvider(String authConfigId, GitHubProvider provider) {
        cache.put(authConfigId, provider);
    }

    protected GitHubProvider createProvider(AuthConfig authConfig) {
        final GitHubConfiguration gitHubConfiguration = authConfig.gitHubConfiguration();
        try {
            return new GitHubProvider(gitHubConfiguration, createSocialAuthManager(gitHubConfiguration), createGitHub(gitHubConfiguration));
        } catch (Exception e) {
            throw new RuntimeException(format("Failed to create GitHubProvider using auth config {0}", authConfig.getId()), e);
        }
    }

    private GitHub createGitHub(GitHubConfiguration gitHubConfiguration) throws IOException {
        if (gitHubConfiguration.authenticateWith() == AuthenticateWith.GITHUB_ENTERPRISE) {
            LOG.debug("Create GitHub connection to enterprise GitHub with token");
            return GitHub.connectToEnterprise(gitHubConfiguration.gitHubEnterpriseUrl(), gitHubConfiguration.personalAccessToken());
        } else {
            LOG.debug("Create GitHub connection to public GitHub with token");
            return GitHub.connectUsingOAuth(gitHubConfiguration.personalAccessToken());
        }
    }

    private SocialAuthManager createSocialAuthManager(GitHubConfiguration gitHubConfiguration) throws Exception {
        final SocialAuthConfig socialAuthConfiguration = SocialAuthConfig.getDefault();
        socialAuthConfiguration.load(gitHubConfiguration.oauthConfiguration());
        final SocialAuthManager manager = new SocialAuthManager();
        manager.setSocialAuthConfig(socialAuthConfiguration);
        return manager;
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }

    public static final GitHubProviderManager getInstance() {
        return PROVIDER_MANAGER;
    }
}
