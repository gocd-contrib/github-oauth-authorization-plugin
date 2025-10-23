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
import com.thoughtworks.go.plugin.api.logging.Logger;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHTeam;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public class MembershipChecker {
    public static final Logger LOG = Logger.getLoggerFor(MembershipChecker.class);

    private final GitHubClientBuilder clientBuilder;

    public MembershipChecker() {
        this(new GitHubClientBuilder());
    }

    MembershipChecker(GitHubClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }


    public boolean isAMemberOfAtLeastOneOrganization(GHUser ghUser, AuthConfig authConfig, List<String> organizationsAllowed) throws IOException {
        if (organizationsAllowed.isEmpty()) {
            LOG.debug("No organizations provided - not allowed.");
            return false;
        }

        return checkMembershipUsingPersonalAccessToken(ghUser, authConfig, organizationsAllowed);
    }

    private boolean checkMembershipUsingPersonalAccessToken(GHUser ghUser, AuthConfig authConfig, List<String> organizationsAllowed) throws IOException {
        final GitHub gitHubForPersonalAccessToken = clientBuilder.fromServerPersonalAccessToken(authConfig.gitHubConfiguration());

        for (String organizationName : organizationsAllowed) {
            final GHOrganization organization = gitHubForPersonalAccessToken.getOrganization(organizationName);
            if (organization != null && organization.hasMember(ghUser)) {
                LOG.info("User `{}` is a member of allowed `{}` organization.", ghUser.getLogin(), organizationName);
                return true;
            }
        }

        return false;
    }

    public boolean isAMemberOfAtLeastOneTeamOfOrganization(GHUser ghUser, AuthConfig authConfig, Map<String, List<String>> organizationAndTeamsAllowed) throws IOException {
        if (organizationAndTeamsAllowed.isEmpty()) {
            LOG.debug("No teams provided - not allowed.");
            return false;
        }

        return checkTeamMembershipUsingPersonalAccessToken(ghUser, authConfig, organizationAndTeamsAllowed);
    }

    private boolean checkTeamMembershipUsingPersonalAccessToken(GHUser ghUser, AuthConfig authConfig, Map<String, List<String>> organizationAndTeamsAllowed) throws IOException {
        final GitHub gitHubForPersonalAccessToken = clientBuilder.fromServerPersonalAccessToken(authConfig.gitHubConfiguration());

        for (String organizationName : organizationAndTeamsAllowed.keySet()) {
            final GHOrganization organization = gitHubForPersonalAccessToken.getOrganization(organizationName);

            if (organization != null) {
                final List<String> allowedTeamsFromRole = organizationAndTeamsAllowed.get(organizationName);
                final Map<String, GHTeam> teamsFromGitHub = organization.getTeams();

                for (GHTeam team : teamsFromGitHub.values()) {
                    if (allowedTeamsFromRole.contains(team.getName().toLowerCase()) && team.hasMember(ghUser)) {
                        LOG.info("User `{}` is a member of allowed `{}` team.", ghUser.getLogin(), team.getName());
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
