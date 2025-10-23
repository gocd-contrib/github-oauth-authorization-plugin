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

import cd.go.authorization.github.annotation.ProfileField;
import cd.go.authorization.github.annotation.Validatable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cd.go.authorization.github.utils.Util.*;

public class GitHubRoleConfiguration implements Validatable {

    @Expose
    @SerializedName("Organizations")
    @ProfileField(key = "Organizations", required = false, secure = false)
    private String organizations;

    @Expose
    @SerializedName("Teams")
    @ProfileField(key = "Teams", required = false, secure = false)
    private String teams;

    @Expose
    @SerializedName("Users")
    @ProfileField(key = "Users", required = false, secure = false)
    private String users;

    public List<String> organizations() {
        return listFromCommaSeparatedString(toLowerCase(organizations));
    }

    public Map<String, List<String>> teams() {
        final HashMap<String, List<String>> organizationToTeams = new HashMap<>();
        splitIntoLinesAndTrimSpaces(toLowerCase(teams)).forEach(line -> {
            if (line.contains(":")) {
                final String[] parts = line.split(":", 2);
                organizationToTeams.put(parts[0], listFromCommaSeparatedString(parts[1]));
            } else {
                throw new RuntimeException("Invalid format. It should be in <organization>:<team-1>,<team-2> format.");
            }
        });

        return organizationToTeams;
    }

    public List<String> users() {
        return listFromCommaSeparatedString(toLowerCase(users));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GitHubRoleConfiguration that = (GitHubRoleConfiguration) o;
        return Objects.equals(organizations, that.organizations) && Objects.equals(teams, that.teams) && Objects.equals(users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizations, teams, users);
    }

    public String toJSON() {
        return GSON.toJson(this);
    }

    public static GitHubRoleConfiguration fromJSON(String json) {
        return GSON.fromJson(json, GitHubRoleConfiguration.class);
    }

    @Override
    public Map<String, String> toProperties() {
        return GSON.fromJson(toJSON(), new TypeToken<Map<String, String>>() {}.getType());
    }

    public boolean hasConfiguration() {
        return isNotBlank(organizations) || isNotBlank(teams) || isNotBlank(users);
    }
}
