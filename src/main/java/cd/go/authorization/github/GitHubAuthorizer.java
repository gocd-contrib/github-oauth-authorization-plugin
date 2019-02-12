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
import cd.go.authorization.github.models.Role;
import org.kohsuke.github.GHUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static cd.go.authorization.github.GitHubPlugin.LOG;
import static java.text.MessageFormat.format;

public class GitHubAuthorizer {
    private final MembershipChecker membershipChecker;

    public GitHubAuthorizer() {
        this(new MembershipChecker());
    }

    public GitHubAuthorizer(MembershipChecker membershipChecker) {
        this.membershipChecker = membershipChecker;
    }

    public List<String> authorize(GHUser user, AuthConfig authConfig, List<Role> roles) throws IOException {
        final List<String> assignedRoles = new ArrayList<>();

        if (roles.isEmpty()) {
            return assignedRoles;
        }

        LOG.debug(format("[Authorize] Authorizing user {0}", user.getLogin()));

        for (Role role : roles) {
            final List<String> allowedUsers = role.roleConfiguration().users();
            if (!allowedUsers.isEmpty() && allowedUsers.contains(user.getLogin().toLowerCase())) {
                LOG.info(format("[Authorize] Assigning role `{0}` to user `{1}`. As user belongs to allowed users list.", role.name(), user.getLogin()));
                assignedRoles.add(role.name());
                continue;
            }

            if (membershipChecker.isAMemberOfAtLeastOneOrganization(user, authConfig, role.roleConfiguration().organizations())) {
                LOG.debug(format("[Authorize] Assigning role `{0}` to user `{1}`. As user is a member of at least one organization.", role.name(), user.getLogin()));
                assignedRoles.add(role.name());
                continue;
            }

            if (membershipChecker.isAMemberOfAtLeastOneTeamOfOrganization(user, authConfig, role.roleConfiguration().teams())) {
                LOG.debug(format("[Authorize] Assigning role `{0}` to user `{1}`. As user is a member of at least one team of the organization.", role.name(), user.getLogin()));
                assignedRoles.add(role.name());
            }
        }

        LOG.debug(format("[Authorize] User `{0}` is authorized with `{1}` role(s).", user.getLogin(), assignedRoles));

        return assignedRoles;
    }
}
