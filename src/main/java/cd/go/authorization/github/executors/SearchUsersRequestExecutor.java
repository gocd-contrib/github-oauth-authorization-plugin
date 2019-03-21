package cd.go.authorization.github.executors;


import cd.go.authorization.github.GitHubClientBuilder;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.User;
import cd.go.authorization.github.requests.SearchUsersRequest;
import cd.go.authorization.github.utils.Util;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedSearchIterable;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cd.go.authorization.github.GitHubPlugin.LOG;

public class SearchUsersRequestExecutor implements RequestExecutor {
    private SearchUsersRequest request;
    private GitHubClientBuilder gitHubClientBuilder;

    public SearchUsersRequestExecutor(SearchUsersRequest request) {
        this(request, new GitHubClientBuilder());
    }

    SearchUsersRequestExecutor(SearchUsersRequest request, GitHubClientBuilder gitHubClientBuilder) {
        this.request = request;
        this.gitHubClientBuilder = gitHubClientBuilder;
    }

    @Override
    public GoPluginApiResponse execute() {
        final Set<User> users = searchUsers(request.getSearchTerm(), request.getAuthConfigs());

        return new DefaultGoPluginApiResponse(200, Util.GSON.toJson(users));
    }

    private Set<User> searchUsers(String searchTerm, List<AuthConfig> authConfigs) {
        final Set<User> users = new HashSet<>();

        if (authConfigs == null || authConfigs.isEmpty()) {
            return users;
        }

        for (AuthConfig authConfig : authConfigs) {
            try {
                LOG.info(String.format("[User Search] Looking up for users matching search_term: `%s`" +
                        " using auth_config: `%s`", searchTerm, authConfig.getId()));
                users.addAll(search(searchTerm, authConfig));
            } catch (Exception e) {
                LOG.error(String.format("[User Search] Error while searching users with auth_config: '%s'", authConfig.getId()), e);
            }
        }

        return users;
    }

    private Set<User> search(String searchText, AuthConfig authConfig) throws IOException {
        Set<User> users = new HashSet<>();
        long start = System.currentTimeMillis();
        GitHub client = gitHubClientBuilder.from(authConfig.gitHubConfiguration());
        PagedSearchIterable<GHUser> ghUsers = client.searchUsers().q(searchText).list();
        long afterRequest = System.currentTimeMillis();
        LOG.debug("Time for request: " + (afterRequest - start) + "ms");
        PagedSearchIterable<GHUser> listOfUsers = ghUsers.withPageSize(10);
        for (GHUser ghUser : listOfUsers.iterator().nextPage()) {
            users.add(new User(ghUser.getLogin(), ghUser.getName(), ghUser.getEmail()));
        }
        long end = System.currentTimeMillis();
        LOG.debug("Total time: " + (end - start) + "ms");
        return users;
    }
}
