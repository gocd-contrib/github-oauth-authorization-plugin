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

package cd.go.authorization.github;

import cd.go.authorization.github.client.GitHubClientBuilder;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.LoggedInUserInfo;
import cd.go.authorization.github.models.OAuthTokenInfo;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.List;

import static cd.go.authorization.github.GitHubPlugin.LOG;

public class GitHubAuthenticator {
    private final MembershipChecker membershipChecker;
    private final GitHubClientBuilder gitHubClientBuilder;

    public GitHubAuthenticator() {
        this(new MembershipChecker(), new GitHubClientBuilder());
    }

    GitHubAuthenticator(MembershipChecker membershipChecker, GitHubClientBuilder gitHubClientBuilder) {
        this.membershipChecker = membershipChecker;
        this.gitHubClientBuilder = gitHubClientBuilder;
    }

    public LoggedInUserInfo authenticate(OAuthTokenInfo tokenInfo, AuthConfig authConfig) throws IOException {
        final GitHub gitHub = gitHubClientBuilder.fromUserOAuthAccessToken(tokenInfo.oauthAccessToken(),authConfig.gitHubConfiguration());
        final List<String> allowedOrganizations = authConfig.gitHubConfiguration().organizationsAllowed();
        final LoggedInUserInfo loggedInUserInfo = new LoggedInUserInfo(gitHub);

        if (allowedOrganizations.isEmpty()) {
            LOG.info("[Authenticate] User `{}` authenticated successfully, organisation membership not required.", loggedInUserInfo.getUser().username());
            return loggedInUserInfo;
        } else if (membershipChecker.isAMemberOfAtLeastOneOrganization(loggedInUserInfo.getGitHubUser(), authConfig, allowedOrganizations)) {
            LOG.info("[Authenticate] User `{}` authenticated successfully as member of an allowed organisation.", loggedInUserInfo.getUser().username());
            return loggedInUserInfo;
        }

        return null;
    }

}
