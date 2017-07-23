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

package cd.go.authorization.github.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import static cd.go.authorization.github.utils.Util.GSON;

public class Role {
    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("auth_config_id")
    private String authConfigId;

    @Expose
    @SerializedName("configuration")
    private GitHubRoleConfiguration configuration;

    public String name() {
        return name;
    }

    public GitHubRoleConfiguration roleConfiguration() {
        return configuration;
    }

    public String authConfigId() {
        return authConfigId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role that = (Role) o;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return configuration != null ? configuration.equals(that.configuration) : that.configuration == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (configuration != null ? configuration.hashCode() : 0);
        return result;
    }

    public static Role fromJSON(String json) {
        return GSON.fromJson(json, Role.class);
    }
}
