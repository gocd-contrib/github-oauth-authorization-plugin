package cd.go.authorization.github.executors;

import cd.go.authorization.github.client.GitHubClientBuilder;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.GitHubConfiguration;
import cd.go.authorization.github.requests.SearchUsersRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.*;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SearchUsersRequestExecutorTest {

    private SearchUsersRequest request;
    private GitHubClientBuilder clientBuilder;
    private GitHub gitHub;
    private GHUserSearchBuilder userSearchBuilder;
    private SearchUsersRequestExecutor executor;

    @BeforeEach
    public void setUp() {
        clientBuilder = mock(GitHubClientBuilder.class);
        request = mock(SearchUsersRequest.class);
        gitHub = mock(GitHub.class);
        userSearchBuilder = mock(GHUserSearchBuilder.class);

        executor = new SearchUsersRequestExecutor(request, clientBuilder);
    }

    @Test
    public void shouldSearchForUsersThatMatchTheSearchTerm() throws Exception {
        AuthConfig authConfig = mock(AuthConfig.class);
        when(authConfig.gitHubConfiguration()).thenReturn(mock(GitHubConfiguration.class));

        when(request.getSearchTerm()).thenReturn("tom");
        when(request.authConfigs()).thenReturn(singletonList(authConfig));
        when(clientBuilder.fromServerPersonalAccessToken(request.authConfigs().get(0).gitHubConfiguration())).thenReturn(gitHub);
        when(gitHub.searchUsers()).thenReturn(userSearchBuilder);
        when(userSearchBuilder.q("tom")).thenReturn(userSearchBuilder);
        @SuppressWarnings("unchecked") PagedSearchIterable<GHUser> pageSearchIterable = mock(PagedSearchIterable.class);
        @SuppressWarnings("unchecked") PagedIterator<GHUser> pagedIterator = mock(PagedIterator.class);
        GHUser githubUser = mock(GHUser.class);
        when(userSearchBuilder.list()).thenReturn(pageSearchIterable);
        when(pageSearchIterable.withPageSize(10)).thenReturn(pageSearchIterable);
        when(pageSearchIterable.iterator()).thenReturn(pagedIterator);
        when(pagedIterator.nextPage()).thenReturn(singletonList(githubUser));
        when(githubUser.getLogin()).thenReturn("tom01");
        when(githubUser.getName()).thenReturn("Tom NoLastname");
        when(githubUser.getEmail()).thenReturn("tom@gocd.org");

        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode()).isEqualTo(200);
        JSONAssert.assertEquals("[{\"username\":\"tom01\", \"display_name\": \"Tom NoLastname\", \"email\": \"tom@gocd.org\"}]", response.responseBody(), true);
    }

    @Test
    public void shouldNotPerformSearchIfAuthConfigsIsEmpty() throws Exception {
        when(request.authConfigs()).thenReturn(Collections.emptyList());

        GoPluginApiResponse response = executor.execute();

        verify(clientBuilder, never()).fromServerPersonalAccessToken(any());
        assertThat(response.responseCode()).isEqualTo(200);
        JSONAssert.assertEquals("[]", response.responseBody(), false);
    }
}
