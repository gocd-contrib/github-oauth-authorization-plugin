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
import cd.go.authorization.github.models.GitHubConfiguration;
import cd.go.authorization.github.models.TokenInfo;
import cd.go.authorization.github.models.User;
import org.brickred.socialauth.Permission;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.util.AccessGrant;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cd.go.authorization.github.GitHubPlugin.LOG;

public class GitHubProvider {
    private static final String GITHUB_PROVIDER = "github";
    private final GitHubConfiguration gitHubConfiguration;
    private final SocialAuthManager authManager;
    private final GitHub gitHub;

    public GitHubProvider(GitHubConfiguration pluginConfiguration, SocialAuthManager authManager, GitHub gitHub) {
        gitHubConfiguration = pluginConfiguration;
        this.authManager = authManager;
        this.gitHub = gitHub;
    }

    public String providerName() {
        return GITHUB_PROVIDER;
    }

    public Permission permission() {
        return new Permission(gitHubConfiguration.authenticateWith().permission());
    }

    public String authorizationServerUrl(String callbackUrl) throws Exception {
        return authManager.getAuthenticationUrl(providerName(), callbackUrl);
    }

    public TokenInfo accessToken(Map<String, String> params) throws Exception {
        try {
            final AccessGrant accessGrant = authManager.createAccessGrant(providerName(), params.get("code"), "");
            return new TokenInfo(accessGrant);
        } finally {
            authManager.disconnectProvider(providerName());
        }
    }

    public User userFromTokenInfo(TokenInfo tokenInfo) {
        try {
            return new User(authManager.connect(tokenInfo.toAccessGrant()).getUserProfile());
        } catch (Exception e) {
            throw new AuthenticationException(e);
        } finally {
            authManager.disconnectProvider(providerName());
        }
    }

    public boolean isAMemberOfAtLeastOneOrganization(User user, List<String> allowedOrganizations) {
        try {
            final GHUser gitHubUser = gitHub.getUser(user.username());

            for (String orgName : allowedOrganizations) {
                GHOrganization organization = gitHub.getOrganization(orgName);
                if (organization != null && gitHubUser.isMemberOf(organization)) {
                    return true;
                }
            }
        } catch (Exception e) {
            LOG.warn("Error occurred while trying to check if user is member of organization", e);
        }
        return false;
    }

    public boolean isAMemberOfAtLeastOneTeamOfOrganization(User user, Map<String, List<String>> allowedOrganizationTeams) throws IOException {
        if (allowedOrganizationTeams.isEmpty()) {
            return false;
        }

        final GHUser gitHubUser = gitHub.getUser(user.username());
        final Map<String, Set<GHTeam>> teamsFromGitHub = gitHub.getMyTeams();

        for (String organization : teamsFromGitHub.keySet()) {
            final List<String> allowedTeams = allowedOrganizationTeams.get(organization);

            if (allowedTeams == null || allowedTeams.isEmpty()) {
                continue;
            }

            for (GHTeam team : teamsFromGitHub.get(organization)) {
                if (gitHubUser.isMemberOf(team)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void verifyConnection() throws IOException {
        //TODO: ?
    }

    public GitHubConfiguration gitHubConfiguration() {
        return gitHubConfiguration;
    }

    public List<User> searchUsers(String searchTerm, int maxResults) {
        final List<User> users = new ArrayList<>();

        if (maxResults < 0) {
            return users;
        }

        try {
            gitHub.searchUsers().q(searchTerm).list();
            final GHUserSearchBuilder searchBuilder = gitHub.searchUsers().q(searchTerm);
            PagedSearchIterable<GHUser> gitHubSearchResults = searchBuilder.list();
            int count = 0;
            for (GHUser user : gitHubSearchResults) {
                users.add(new User(user.getLogin(), user.getName(), user.getEmail()));
                count++;
                if (count >= maxResults) {
                    break;
                }
            }
        } catch (Exception e) {
            LOG.warn("Error occurred while trying to perform user search", e);
        }

        return users;
    }
}
