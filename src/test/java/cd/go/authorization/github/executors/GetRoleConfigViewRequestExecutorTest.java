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

package cd.go.authorization.github.executors;

import cd.go.authorization.github.annotation.MetadataHelper;
import cd.go.authorization.github.annotation.ProfileMetadata;
import cd.go.authorization.github.models.GitHubRoleConfiguration;
import cd.go.authorization.github.utils.Util;
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class GetRoleConfigViewRequestExecutorTest {

    @Test
    public void allFieldsShouldBePresentInView() throws Exception {
        String template = Util.readResource("/role-config.template.html");
        final Document document = Jsoup.parse(template);

        final List<ProfileMetadata> metadataList = MetadataHelper.getMetadata(GitHubRoleConfiguration.class);
        for (ProfileMetadata field : metadataList) {
            final Elements inputFieldForKey = document.getElementsByAttributeValue("ng-model", field.getKey());
            assertThat(inputFieldForKey, hasSize(1));

            final Elements spanToShowError = document.getElementsByAttributeValue("ng-class", "{'is-visible': GOINPUTNAME[" + field.getKey() + "].$error.server}");
            assertThat(spanToShowError, hasSize(1));
            assertThat(spanToShowError.attr("ng-show"), is("GOINPUTNAME[" + field.getKey() + "].$error.server"));
            assertThat(spanToShowError.text(), is("{{GOINPUTNAME[" + field.getKey() + "].$error.server}}"));
        }

        final Elements inputs = document.select("textarea,input,select");
        assertThat("should contains only inputs that defined in GitHubRoleConfiguration.java",inputs, hasSize(metadataList.size()));
    }

    @Test
    public void shouldRenderTheTemplateInJSON() throws Exception {
        GoPluginApiResponse response = new GetRoleConfigViewRequestExecutor().execute();
        assertThat(response.responseCode(), is(200));
        Map<String, String> hashSet = new Gson().fromJson(response.responseBody(), HashMap.class);
        assertThat(hashSet, hasEntry("template", Util.readResource("/role-config.template.html")));
    }
}