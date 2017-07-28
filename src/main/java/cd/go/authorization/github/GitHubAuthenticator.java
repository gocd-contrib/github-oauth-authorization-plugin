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
import cd.go.authorization.github.models.User;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.List;

import static cd.go.authorization.github.GitHubPlugin.LOG;
import static java.text.MessageFormat.format;

public class GitHubAuthenticator {
    private final MembershipChecker membershipChecker;

    public GitHubAuthenticator() {
        this(new MembershipChecker());
    }

    GitHubAuthenticator(MembershipChecker membershipChecker) {
        this.membershipChecker = membershipChecker;
    }

    public User authenticate(GitHub gitHub, AuthConfig authConfig) throws IOException {
        final GHMyself myself = gitHub.getMyself();
        final User user = new User(myself.getLogin(), myself.getName(), myself.getEmail());
        final List<String> allowedOrganizations = authConfig.gitHubConfiguration().organizationsAllowed();

        if (allowedOrganizations.isEmpty() || membershipChecker.isAMemberOfAtLeastOneOrganization(gitHub, allowedOrganizations)) {
            LOG.info(format("[Authenticate] User `{0}` authenticated successfully.", user.username()));
            return user;
        }

        return null;
    }

}
