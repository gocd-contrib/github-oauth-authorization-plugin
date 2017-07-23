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
import cd.go.authorization.github.models.User;
import cd.go.authorization.github.providermanager.GitHubProviderManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitHubAuthorizer {
    private final GitHubProviderManager providerManager;

    public GitHubAuthorizer() {
        this(GitHubProviderManager.getInstance());
    }

    GitHubAuthorizer(GitHubProviderManager providerManager) {
        this.providerManager = providerManager;
    }


    public List<String> authorize(User user, AuthConfig authConfig, List<Role> roles) throws IOException {
        final List<String> assignedRoles = new ArrayList<>();

        if (roles.isEmpty()) {
            return assignedRoles;
        }

        final GitHubProvider provider = providerManager.getGitHubProvider(authConfig);

        for (Role role : roles) {
            final List<String> allowedUsers = role.roleConfiguration().users();
            if (!allowedUsers.isEmpty() && allowedUsers.contains(user.username())) {
                assignedRoles.add(role.name());
            }

            if (provider.isAMemberOfAtLeastOneOrganization(user, role.roleConfiguration().organizations())) {
                assignedRoles.add(role.name());
            }

            if (provider.isAMemberOfAtLeastOneTeamOfOrganization(user, role.roleConfiguration().teams())) {
                assignedRoles.add(role.name());
            }
        }

        return assignedRoles;
    }
}
