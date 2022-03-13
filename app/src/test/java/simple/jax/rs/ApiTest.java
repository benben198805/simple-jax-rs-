package simple.jax.rs;

import jakarta.ws.rs.core.MediaType;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.FormRequestContent;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import simple.jax.rs.dto.test.Group;
import simple.jax.rs.dto.test.Member;
import simple.jax.rs.dto.test.Project;
import simple.jax.rs.resources.GroupResource;
import simple.jax.rs.resources.MemberResource;
import simple.jax.rs.resources.NameResource;
import simple.jax.rs.resources.ProjectMemberClassResource;
import simple.jax.rs.resources.ProjectMemberResource;
import simple.jax.rs.resources.ProjectResource;
import simple.jax.rs.resources.SellersResource;
import simple.jax.rs.resources.StudentResource;
import simple.jax.rs.resources.UserResource;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ApiTest {
    private JettyServer server;
    private HttpClient httpClient;

    @BeforeEach
    public void beforeEach() throws Exception {
        httpClient = new HttpClient();
        httpClient.setFollowRedirects(true);
        httpClient.start();
    }

    private void startServer(Class... resources) throws Exception {
        server = new JettyServer(resources);
        server.start();
    }

    @AfterEach
    public void afterEach() throws Exception {
        server.stop();
        httpClient.stop();
    }

    @Test
    @Disabled
    public void should() throws Exception {
        startServer(NameResource.class);

        ContentResponse response = httpClient.GET("http://localhost:8080/name");
        assertEquals("name", response.getContentAsString());
    }

    @Test
    @Disabled
    public void should_get_users() throws Exception {
        startServer(UserResource.class);

        ContentResponse response = httpClient.GET("http://localhost:8080/users");
        assertEquals("John", response.getContentAsString());
    }

    @Test
    public void should_get_project_by_id() throws Exception {
        startServer(ProjectResource.class);

        ContentResponse response = httpClient.GET("http://localhost:8080/projects/1");
        assertEquals((new Project("CRM-1")).toString(), response.getContentAsString());
    }

    @Test
    public void should_get_project_by_id_and_item_name() throws Exception {
        startServer(ProjectResource.class);

        ContentResponse response = httpClient.GET("http://localhost:8080/projects/1/items/abc");
        assertEquals((new Project("CRM-1(abc)")).toString(), response.getContentAsString());
    }

    @Test
    public void should_query_project_by_query_params() throws Exception {
        startServer(ProjectResource.class);

        ContentResponse response = httpClient.GET("http://localhost:8080/projects?start=1&size=10");

        ArrayList<Project> projects = new ArrayList<>();
        projects.add(new Project("CRM-1"));
        projects.add(new Project("CRM-10"));
        assertEquals(projects.toString(), response.getContentAsString());
    }

    @Test
    public void should_query_project_by_list_query_params() throws Exception {
        startServer(GroupResource.class);

        ContentResponse response = httpClient.GET("http://localhost:8080/groups?status=active&status=init");

        ArrayList<Group> groups = new ArrayList<>();
        groups.add(new Group("GROUP-active"));
        groups.add(new Group("GROUP-init"));
        assertEquals(groups.toString(), response.getContentAsString());
    }

    @Test
    public void should_query_sub_resource() throws Exception {
        startServer(ProjectResource.class, MemberResource.class);

        ContentResponse response = httpClient.GET("http://localhost:8080/projects/members/1");

        Member member = new Member("MEMBER-1");
        assertEquals(member.toString(), response.getContentAsString());
    }

    @Test
    public void should_query_sub_resource_with_empty_path() throws Exception {
        startServer(ProjectMemberResource.class, MemberResource.class);

        ContentResponse response = httpClient.GET("http://localhost:8080/projects/members/1");

        Member member = new Member("MEMBER-1");
        assertEquals(member.toString(), response.getContentAsString());
    }

    @Test
    public void should_query_sub_resource_with_return_class() throws Exception {
        startServer(ProjectMemberClassResource.class, MemberResource.class);

        ContentResponse response = httpClient.GET("http://localhost:8080/projects/members/1");

        Member member = new Member("MEMBER-1");
        assertEquals(member.toString(), response.getContentAsString());
    }

    @Test
    public void should_post_user() throws Exception {
        startServer(StudentResource.class);
        Fields fields = new Fields();
        fields.add("name", "John");
        FormRequestContent content = new FormRequestContent(fields);

        ContentResponse response = httpClient.POST("http://localhost:8080/students").body(content).send();

        assertEquals("post John", response.getContentAsString());
    }

    @Test
    public void should_get_method_with_correct_method() throws Exception {
        startServer(SellersResource.class);

        ContentResponse response =
                httpClient.newRequest("http://localhost:8080/sellers").method(HttpMethod.GET)
                          .header("Accept", MediaType.APPLICATION_JSON).send();

        assertEquals("get as APPLICATION_JSON", response.getContentAsString());
    }
}
