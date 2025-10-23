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

package cd.go.authorization.github.executors;

import cd.go.authorization.github.annotation.MetadataHelper;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.GitHubConfiguration;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.List;

import static cd.go.authorization.github.utils.Util.GSON;
import static org.assertj.core.api.Assertions.assertThat;

public class GetAuthConfigMetadataRequestExecutorTest {

    @Test
    public void shouldSerializeAllFields() {
        GoPluginApiResponse response = new GetAuthConfigMetadataRequestExecutor().execute();
        List<AuthConfig> list = GSON.fromJson(response.responseBody(), new TypeToken<>() {}.getType());
        assertThat(list).hasSameSizeAs(MetadataHelper.getMetadata(GitHubConfiguration.class));
    }

    @Test
    public void assertJsonStructure() throws Exception {
        GoPluginApiResponse response = new GetAuthConfigMetadataRequestExecutor().execute();

        assertThat(response.responseCode()).isEqualTo(200);
        String expectedJSON = """
                [
                  {
                    "key": "ClientId",
                    "metadata": {
                      "required": true,
                      "secure": true
                    }
                  },
                  {
                    "key": "ClientSecret",
                    "metadata": {
                      "required": true,
                      "secure": true
                    }
                  },
                  {
                    "key": "AuthenticateWith",
                    "metadata": {
                      "required": false,
                      "secure": false
                    }
                  },
                  {
                    "key": "GitHubEnterpriseUrl",
                    "metadata": {
                      "required": false,
                      "secure": false
                    }
                  },
                  {
                    "key": "AllowedOrganizations",
                    "metadata": {
                      "required": false,
                      "secure": false
                    }
                  },
                  {
                    "key": "PersonalAccessToken",
                    "metadata": {
                      "required": true,
                      "secure": true
                    }
                  }
                ]""";

        JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);
    }
}
