package dev.mehdizebhi.tdd.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc
public class PostControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PostRepository postRepository;

    List<Post> posts = new ArrayList<>();

    @BeforeEach
    void setUp() {
        posts = List.of(
                new Post(1, 1, "Test Post Title 1", "This is my 1st post.", null),
                new Post(2, 1, "Test Post Title 2", "This is my 2nd post.", null)
        );
    }

    @Test
    void shouldFindAllPosts() throws Exception {

        var jsonResponse = """
                [
                    {
                        "id":1,
                        "userId":1,
                        "title":"Test Post Title 1",
                        "body":"This is my 1st post.",
                        "version": null
                    },
                    {
                        "id":2,
                        "userId":1,
                        "title":"Test Post Title 2",
                        "body":"This is my 2nd post.",
                        "version": null
                    }
                ]
                """;

        when(postRepository.findAll()).thenReturn(posts);

        mockMvc
                .perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));

    }

    @Test
    void shouldFindPostWhenGivenValidId() throws Exception {
        var jsonResponse = """
                {
                    "id":1,
                    "userId":1,
                    "title":"Test Post Title 1",
                    "body":"This is my 1st post.",
                    "version": null
                }
                """;
        when(postRepository.findById(1)).thenReturn(Optional.of(posts.get(0)));

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));
    }

    @Test
    void shouldNotFindPostWhenGivenInvalidId() throws Exception {
        when(postRepository.findById(999)).thenThrow(PostNotFoundException.class);

        mockMvc.perform(get("/api/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewPostWhenGivenValidID() throws Exception {
        Post post = new Post(3,1,"This is my brand new post", "TEST BODY",null);
        when(postRepository.save(post)).thenReturn(post);
        var json = """
                {
                    "id":3,
                    "userId":1,
                    "title":"This is my brand new post",
                    "body":"TEST BODY",
                    "version": null
                }
                """;

        mockMvc.perform(post("/api/posts")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().json(json));
    }

    @Test
    void shouldUpdatePostWhenGivenValidPost() throws Exception {
        Post updated = new Post(1,1,"This is my brand new post", "UPDATED BODY",1);
        when(postRepository.findById(1)).thenReturn(Optional.of(posts.get(0)));
        when(postRepository.save(updated)).thenReturn(updated);
        var requestBody = """
                {
                    "id":1,
                    "userId":1,
                    "title":"This is my brand new post",
                    "body":"UPDATED BODY",
                    "version": 1
                }
                """;

        mockMvc.perform(put("/api/posts/1")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotUpdateAndThrowNotFoundWhenGivenAnInvalidPostID() throws Exception {
        Post updated = new Post(50,1,"This is my brand new post", "UPDATED BODY",1);
        when(postRepository.save(updated)).thenReturn(updated);
        var json = """
                {
                    "id":50,
                    "userId":1,
                    "title":"This is my brand new post",
                    "body":"UPDATED BODY",
                    "version": 1
                }
                """;

        mockMvc.perform(put("/api/posts/999")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeletePostWhenGivenValidID() throws Exception {
        doNothing().when(postRepository).deleteById(1);

        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isNoContent());

        verify(postRepository, times(1)).deleteById(1);
    }
}