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

package cd.go.authorization.github.models;

import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GitHub;

import java.io.IOException;

public class LoggedInUserInfo {
    private final GHMyself gitHubUser;
    private final User user;

    public LoggedInUserInfo(GitHub gitHub) throws IOException {
        gitHubUser = gitHub.getMyself();
        user = new User(gitHubUser.getLogin(), gitHubUser.getName(), gitHubUser.getEmail());
    }

    public GHMyself getGitHubUser() {
        return gitHubUser;
    }

    public User getUser() {
        return user;
    }
}
