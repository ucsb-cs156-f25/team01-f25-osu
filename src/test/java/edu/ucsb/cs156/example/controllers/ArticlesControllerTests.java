package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Article;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

  @MockBean ArticlesRepository articlesRepository;

  @MockBean UserRepository userRepository;

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_articles() throws Exception {

    // arrange

    Article article1 =
        Article.builder()
            .title("Test Article 1")
            .url("https://example.com/article1")
            .explanation("This is a test article")
            .email("test1@example.com")
            .dateAdded(LocalDateTime.now())
            .build();

    Article article2 =
        Article.builder()
            .title("Test Article 2")
            .url("https://example.com/article2")
            .explanation("This is another test article")
            .email("test2@example.com")
            .dateAdded(LocalDateTime.now())
            .build();

    ArrayList<Article> expectedArticles = new ArrayList<>();
    expectedArticles.addAll(Arrays.asList(article1, article2));

    when(articlesRepository.findAll()).thenReturn(expectedArticles);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/articles/all")).andExpect(status().isOk()).andReturn();

    // assert

    verify(articlesRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedArticles);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_article() throws Exception {
    // arrange

    Article article =
        Article.builder()
            .title("New Article")
            .url("https://example.com/new")
            .explanation("This is a new article")
            .email("admin@example.com")
            .dateAdded(LocalDateTime.now())
            .build();

    ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);
    when(articlesRepository.save(articleCaptor.capture())).thenReturn(article);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/articles/post?title=New Article&url=https://example.com/new&explanation=This is a new article&email=admin@example.com")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(articlesRepository, times(1)).save(any(Article.class));

    // Verify the actual Article passed to save() has correct values
    Article savedArticle = articleCaptor.getValue();
    assertEquals("New Article", savedArticle.getTitle());
    assertEquals("https://example.com/new", savedArticle.getUrl());
    assertEquals("This is a new article", savedArticle.getExplanation());
    assertEquals("admin@example.com", savedArticle.getEmail());
    assertNotNull(savedArticle.getDateAdded());

    // Also verify the response JSON
    Map<String, Object> json = responseToJson(response);
    assertEquals("New Article", json.get("title"));
    assertEquals("https://example.com/new", json.get("url"));
    assertEquals("This is a new article", json.get("explanation"));
    assertEquals("admin@example.com", json.get("email"));
    // Verify dateAdded exists but don't compare its value since it's set to now()
    assertNotNull(json.get("dateAdded"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange

    Article article =
        Article.builder()
            .title("Test Article")
            .url("https://example.com/article")
            .explanation("This is a test article")
            .email("test@example.com")
            .dateAdded(LocalDateTime.now())
            .build();

    when(articlesRepository.findById(eq(1L))).thenReturn(Optional.of(article));

    // act
    MvcResult response =
        mockMvc.perform(get("/api/articles?id=1")).andExpect(status().isOk()).andReturn();

    // assert

    verify(articlesRepository, times(1)).findById(eq(1L));
    String expectedJson = mapper.writeValueAsString(article);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(articlesRepository.findById(eq(999L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc.perform(get("/api/articles?id=999")).andExpect(status().isNotFound()).andReturn();

    // assert

    verify(articlesRepository, times(1)).findById(eq(999L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("Article with id 999 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_article() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");

    Article articleOrig =
        Article.builder()
            .title("Original Article")
            .url("https://example.com/original")
            .explanation("Original explanation")
            .email("original@example.com")
            .dateAdded(ldt1)
            .build();

    Article articleEdited =
        Article.builder()
            .title("Edited Article")
            .url("https://example.com/edited")
            .explanation("Edited explanation")
            .email("edited@example.com")
            .dateAdded(ldt2)
            .build();

    String requestBody = mapper.writeValueAsString(articleEdited);

    when(articlesRepository.findById(eq(67L))).thenReturn(Optional.of(articleOrig));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/articles?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(articlesRepository, times(1)).findById(67L);
    verify(articlesRepository, times(1)).save(articleEdited); // should be saved with correct user
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_article_that_does_not_exist() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    Article articleEdited =
        Article.builder()
            .title("Edited Article")
            .url("https://example.com/edited")
            .explanation("Edited explanation")
            .email("edited@example.com")
            .dateAdded(ldt1)
            .build();

    String requestBody = mapper.writeValueAsString(articleEdited);

    when(articlesRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/articles?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(articlesRepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("Article with id 67 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_an_article() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    Article article1 =
        Article.builder()
            .title("Test Article")
            .url("https://example.com/article")
            .explanation("Test explanation")
            .email("test@example.com")
            .dateAdded(ldt1)
            .build();

    when(articlesRepository.findById(eq(15L))).thenReturn(Optional.of(article1));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/articles?id=15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(articlesRepository, times(1)).findById(15L);
    verify(articlesRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("Article with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_article_and_gets_right_error_message()
      throws Exception {
    // arrange

    when(articlesRepository.findById(eq(15L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/articles?id=15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(articlesRepository, times(1)).findById(15L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("Article with id 15 not found", json.get("message"));
  }
}
