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

import cd.go.authorization.github.annotation.ProfileField;
import cd.go.authorization.github.annotation.Validatable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static cd.go.authorization.github.utils.Util.GSON;
import static cd.go.authorization.github.utils.Util.listFromCommaSeparatedString;

public class GitHubConfiguration implements Validatable {

    @Expose
    @SerializedName("ClientId")
    @ProfileField(key = "ClientId", required = true, secure = true)
    private String clientId;

    @Expose
    @SerializedName("ClientSecret")
    @ProfileField(key = "ClientSecret", required = true, secure = true)
    private String clientSecret;

    @Expose
    @SerializedName("PersonalAccessToken")
    @ProfileField(key = "PersonalAccessToken", required = true, secure = true)
    private String personalAccessToken;

    @Expose
    @SerializedName("AuthenticateWith")
    @ProfileField(key = "AuthenticateWith", required = false, secure = false)
    private AuthenticateWith authenticateWith;

    @Expose
    @SerializedName("GitHubEnterpriseUrl")
    @ProfileField(key = "GitHubEnterpriseUrl", required = false, secure = false)
    private String gitHubEnterpriseUrl;

    @Expose
    @SerializedName("AllowedOrganizations")
    @ProfileField(key = "AllowedOrganizations", required = false, secure = false)
    private String allowedOrganizations;


    public GitHubConfiguration() {
    }

    public GitHubConfiguration(String clientId, String clientSecret, String personalAccessToken) {
        this(clientId, clientSecret, personalAccessToken, AuthenticateWith.GITHUB, null, null);
    }

    public GitHubConfiguration(String clientId, String clientSecret, String personalAccessToken, AuthenticateWith authenticateWith, String gitHubEnterpriseUrl, String allowedOrganizations) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.personalAccessToken = personalAccessToken;
        this.authenticateWith = authenticateWith;
        this.gitHubEnterpriseUrl = gitHubEnterpriseUrl;
        this.allowedOrganizations = allowedOrganizations;
    }

    public Properties oauthConfiguration() {
        Properties properties = new Properties();
        properties.put("api.github.com.consumer_key", clientId);
        properties.put("api.github.com.consumer_secret", clientSecret);
        properties.put("api.github.com.custom_permissions", authenticateWith.permission());
        return properties;
    }

    public String clientId() {
        return clientId;
    }

    public String clientSecret() {
        return clientSecret;
    }

    public String toJSON() {
        return GSON.toJson(this);
    }

    public String personalAccessToken() {
        return personalAccessToken;
    }

    public AuthenticateWith authenticateWith() {
        return authenticateWith;
    }

    public String gitHubEnterpriseUrl() {
        return gitHubEnterpriseUrl;
    }

    public static GitHubConfiguration fromJSON(String json) {
        return GSON.fromJson(json, GitHubConfiguration.class);
    }

    public List<String> allowedOrganizations() {
        return listFromCommaSeparatedString(allowedOrganizations);
    }

    public Map<String, String> toProperties() {
        return GSON.fromJson(toJSON(), new TypeToken<Map<String, String>>() {
        }.getType());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GitHubConfiguration that = (GitHubConfiguration) o;

        if (clientId != null ? !clientId.equals(that.clientId) : that.clientId != null) return false;
        if (clientSecret != null ? !clientSecret.equals(that.clientSecret) : that.clientSecret != null) return false;
        if (personalAccessToken != null ? !personalAccessToken.equals(that.personalAccessToken) : that.personalAccessToken != null)
            return false;
        if (authenticateWith != that.authenticateWith) return false;
        if (gitHubEnterpriseUrl != null ? !gitHubEnterpriseUrl.equals(that.gitHubEnterpriseUrl) : that.gitHubEnterpriseUrl != null)
            return false;
        return allowedOrganizations != null ? allowedOrganizations.equals(that.allowedOrganizations) : that.allowedOrganizations == null;
    }

    @Override
    public int hashCode() {
        int result = clientId != null ? clientId.hashCode() : 0;
        result = 31 * result + (clientSecret != null ? clientSecret.hashCode() : 0);
        result = 31 * result + (personalAccessToken != null ? personalAccessToken.hashCode() : 0);
        result = 31 * result + (authenticateWith != null ? authenticateWith.hashCode() : 0);
        result = 31 * result + (gitHubEnterpriseUrl != null ? gitHubEnterpriseUrl.hashCode() : 0);
        result = 31 * result + (allowedOrganizations != null ? allowedOrganizations.hashCode() : 0);
        return result;
    }
}
