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

import cd.go.authorization.github.exceptions.AuthenticationException;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.TokenInfo;
import cd.go.authorization.github.models.User;
import cd.go.authorization.github.providermanager.GitHubProviderManager;

import java.util.List;

import static java.text.MessageFormat.format;

public class GitHubAuthenticator {
    private final GitHubProviderManager gitHubProviderManager;

    public GitHubAuthenticator() {
        this(GitHubProviderManager.getInstance());
    }

    GitHubAuthenticator(GitHubProviderManager gitHubProviderManager) {
        this.gitHubProviderManager = gitHubProviderManager;
    }

    public User authenticate(TokenInfo tokenInfo, AuthConfig authConfig) {
        final GitHubProvider provider = gitHubProviderManager.getGitHubProvider(authConfig);
        final User user = provider.userFromTokenInfo(tokenInfo);
        final List<String> allowedOrganizations = authConfig.gitHubConfiguration().allowedOrganizations();

        if (allowedOrganizations.isEmpty() || provider.isAMemberOfAtLeastOneOrganization(user, allowedOrganizations)) {
            return user;
        }

        throw new AuthenticationException(format("[Authenticate] User {0} is not belongs to organizations {1}", user.username(), allowedOrganizations));
    }
}
