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

package cd.go.authorization.github.providermanager;

import cd.go.authorization.github.GitHubProvider;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.GitHubConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class GitHubProviderManagerTest {

    private GitHubProviderManager providerManagerSpy;

    @Before
    public void setUp() throws Exception {


        providerManagerSpy = spy(GitHubProviderManager.getInstance());
    }

    @After
    public void tearDown() throws Exception {
        providerManagerSpy.clear();
    }

    @Test
    public void shouldCreateProviderWithConfiguration() throws Exception {
        final AuthConfig authConfig = mock(AuthConfig.class);
        final GitHubConfiguration gitHubConfiguration = mock(GitHubConfiguration.class);

        when(gitHubConfiguration.oauthConfiguration()).thenReturn(new Properties());
        when(authConfig.getId()).thenReturn("github-config");
        when(authConfig.gitHubConfiguration()).thenReturn(gitHubConfiguration);

        final GitHubProvider provider = providerManagerSpy.getGitHubProvider(authConfig);

        assertNotNull(provider);
        assertThat(providerManagerSpy.size(), is(1));
        verify(providerManagerSpy, times(1)).createProvider(authConfig);
    }

    @Test
    public void shouldReturnExistingProviderIfExist() throws Exception {
        final AuthConfig authConfig = mock(AuthConfig.class);
        final GitHubConfiguration gitHubConfiguration = mock(GitHubConfiguration.class);

        when(gitHubConfiguration.oauthConfiguration()).thenReturn(new Properties());
        when(authConfig.getId()).thenReturn("github-config");
        when(authConfig.gitHubConfiguration()).thenReturn(gitHubConfiguration);

        final GitHubProvider newProvider = providerManagerSpy.getGitHubProvider(authConfig);
        verify(providerManagerSpy, times(1)).createProvider(authConfig);
        assertNotNull(newProvider);
        assertThat(providerManagerSpy.size(), is(1));

        providerManagerSpy.getGitHubProvider(authConfig);
        verify(providerManagerSpy, times(1)).createProvider(authConfig);
        assertThat(providerManagerSpy.size(), is(1));
    }

    @Test
    public void shouldReturnCreateNewProviderIfGitHubConfigurationHasChanged() throws Exception {
        final AuthConfig authConfig = mock(AuthConfig.class);
        final GitHubConfiguration oldGitHubConfiguration = mock(GitHubConfiguration.class);

        when(oldGitHubConfiguration.oauthConfiguration()).thenReturn(new Properties());
        when(authConfig.getId()).thenReturn("github-config");
        when(authConfig.gitHubConfiguration()).thenReturn(oldGitHubConfiguration);

        final GitHubProvider newProvider = providerManagerSpy.getGitHubProvider(authConfig);
        verify(providerManagerSpy, times(1)).createProvider(authConfig);
        assertNotNull(newProvider);
        assertThat(providerManagerSpy.size(), is(1));

        final GitHubConfiguration newGitHubConfiguration = mock(GitHubConfiguration.class);
        when(authConfig.gitHubConfiguration()).thenReturn(newGitHubConfiguration);

        providerManagerSpy.getGitHubProvider(authConfig);
        verify(providerManagerSpy, times(2)).createProvider(authConfig);
        assertThat(providerManagerSpy.size(), is(1));
    }

    @Test
    public void shouldCreateNewProviderForDifferentAuthConfig() throws Exception {
        final AuthConfig gitHubAuthConfig = mock(AuthConfig.class);
        final GitHubConfiguration gitHubConfiguration = mock(GitHubConfiguration.class);

        when(gitHubConfiguration.oauthConfiguration()).thenReturn(new Properties());
        when(gitHubAuthConfig.getId()).thenReturn("github-config");
        when(gitHubAuthConfig.gitHubConfiguration()).thenReturn(gitHubConfiguration);

        final GitHubProvider newProvider = providerManagerSpy.getGitHubProvider(gitHubAuthConfig);

        assertNotNull(newProvider);
        verify(providerManagerSpy, times(1)).createProvider(gitHubAuthConfig);
        assertThat(providerManagerSpy.size(), is(1));

        final AuthConfig gitHubEnterpriseAuthConfig = mock(AuthConfig.class);
        when(gitHubEnterpriseAuthConfig.getId()).thenReturn("github-enterprise-config");
        when(gitHubEnterpriseAuthConfig.gitHubConfiguration()).thenReturn(gitHubConfiguration);

        final GitHubProvider gitHubEnterpriseProvider = providerManagerSpy.getGitHubProvider(gitHubEnterpriseAuthConfig);

        assertNotNull(gitHubEnterpriseProvider);
        verify(providerManagerSpy, times(1)).createProvider(gitHubEnterpriseAuthConfig);
        assertThat(providerManagerSpy.size(), is(2));
    }
}