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

    public boolean isAMemberOfAtLeastOneOrganization(GitHub gitHub, List<String> organizationsAllowed) {
        try {
            if (organizationsAllowed.isEmpty()) {
                LOG.debug("[MembershipChecker] No organizations to provided.");
                return false;
            }

            final Map<String, GHOrganization> myGitHubOrganizations = gitHub.getMyOrganizations();

            for (String organizationName : myGitHubOrganizations.keySet()) {
                if (organizationsAllowed.contains(toLowerCase(organizationName))) {
                    LOG.info(format("[MembershipChecker] User is a member of {0} organization.", organizationName));
                    return true;
                }
            }
        } catch (Exception e) {
            LOG.warn("[MembershipChecker] Error occurred while trying to check if user is member of organization", e);
        }
        return false;
    }

    public boolean isAMemberOfAtLeastOneTeamOfOrganization(GitHub gitHub, Map<String, List<String>> organizationAndTeamsAllowed) throws IOException {
        if (organizationAndTeamsAllowed.isEmpty()) {
            return false;
        }

        final Map<String, Set<GHTeam>> myGitHubOrganizationsAndTeams = gitHub.getMyTeams();

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
