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

import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.LoggedInUserInfo;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHTeam;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cd.go.authorization.github.GitHubPlugin.LOG;
import static cd.go.authorization.github.utils.Util.toLowerCase;
import static java.text.MessageFormat.format;

public class MembershipChecker {

    public boolean isAMemberOfAtLeastOneOrganization(LoggedInUserInfo loggedInUserInfo, AuthConfig authConfig, List<String> organizationsAllowed) throws IOException {
        if (organizationsAllowed.isEmpty()) {
            LOG.debug("[MembershipChecker] No organizations provided.");
            return false;
        }

        if (authConfig.gitHubConfiguration().authorizeUsingPersonalAccessToken()) {
            return checkMembershipUsingPersonalAccessToken(loggedInUserInfo, authConfig, organizationsAllowed);
        }

        return checkMembershipUsingUsersAccessToken(loggedInUserInfo, organizationsAllowed);
    }

    private boolean checkMembershipUsingPersonalAccessToken(LoggedInUserInfo loggedInUserInfo, AuthConfig authConfig, List<String> organizationsAllowed) throws IOException {
        final GitHub gitHubForPersonalAccessToken = authConfig.gitHubConfiguration().gitHubClient();

        for (String organizationName : organizationsAllowed) {
            final GHOrganization organization = gitHubForPersonalAccessToken.getOrganization(organizationName);
            if (organization != null && organization.hasMember(loggedInUserInfo.getGitHubUser())) {
                LOG.info(format("[MembershipChecker] User `{0}` is a member of `{1}` organization.", loggedInUserInfo.getUser().username(), organizationName));
                return true;
            }
        }

        return false;
    }

    private boolean checkMembershipUsingUsersAccessToken(LoggedInUserInfo loggedInUserInfo, List<String> organizationsAllowed) throws IOException {
        final Map<String, GHOrganization> myGitHubOrganizations = loggedInUserInfo.getGitHub().getMyOrganizations();

        for (String organizationName : myGitHubOrganizations.keySet()) {
            if (organizationsAllowed.contains(toLowerCase(organizationName))) {
                LOG.info(format("[MembershipChecker] User `{0}` is a member of `{1}` organization.", loggedInUserInfo.getUser().username(), organizationName));
                return true;
            }
        }

        return false;
    }

    public boolean isAMemberOfAtLeastOneTeamOfOrganization(LoggedInUserInfo loggedInUserInfo, AuthConfig authConfig, Map<String, List<String>> organizationAndTeamsAllowed) throws IOException {
        if (organizationAndTeamsAllowed.isEmpty()) {
            LOG.debug("[MembershipChecker] No teams provided.");
            return false;
        }

        if (authConfig.gitHubConfiguration().authorizeUsingPersonalAccessToken()) {
            return checkTeamMembershipUsingPersonalAccessToken(loggedInUserInfo, authConfig, organizationAndTeamsAllowed);
        }

        return checkTeamMembershipUsingUserAccessToken(loggedInUserInfo, organizationAndTeamsAllowed);
    }

    private boolean checkTeamMembershipUsingPersonalAccessToken(LoggedInUserInfo loggedInUserInfo, AuthConfig authConfig, Map<String, List<String>> organizationAndTeamsAllowed) throws IOException {
        final GitHub gitHubForPersonalAccessToken = authConfig.gitHubConfiguration().gitHubClient();

        for (String organizationName : organizationAndTeamsAllowed.keySet()) {
            final GHOrganization organization = gitHubForPersonalAccessToken.getOrganization(organizationName);

            if (organization != null) {
                final List<String> allowedTeamsFromRole = organizationAndTeamsAllowed.get(organizationName);
                final Map<String, GHTeam> teamsFromGitHub = organization.getTeams();

                for (GHTeam team : teamsFromGitHub.values()) {
                    if (allowedTeamsFromRole.contains(team.getName().toLowerCase()) && team.hasMember(loggedInUserInfo.getGitHubUser())) {
                        LOG.info(format("[MembershipChecker] User `{0}` is a member of `{1}` team.", loggedInUserInfo.getUser().username(), team.getName()));
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean checkTeamMembershipUsingUserAccessToken(LoggedInUserInfo loggedInUserInfo, Map<String, List<String>> organizationAndTeamsAllowed) throws IOException {
        final Map<String, Set<GHTeam>> myGitHubOrganizationsAndTeams = loggedInUserInfo.getGitHub().getMyTeams();

        for (String organizationName : myGitHubOrganizationsAndTeams.keySet()) {
            final List<String> teamsAllowed = organizationAndTeamsAllowed.get(toLowerCase(organizationName));

            if (teamsAllowed == null || teamsAllowed.isEmpty()) {
                LOG.debug(format("[MembershipChecker] No teams specified for organization `{0}`.", organizationName));
                continue;
            }

            for (GHTeam myGitHubTeam : myGitHubOrganizationsAndTeams.get(organizationName)) {
                if (teamsAllowed.contains(toLowerCase(myGitHubTeam.getName()))) {
                    LOG.debug(format("[MembershipChecker] User is a member of `{0}:{1}` team.", organizationName, myGitHubTeam.getName()));
                    return true;
                }
            }
        }

        return false;
    }
}
