package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Article;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for Articles */
@Tag(name = "Articles")
@RequestMapping("/api/articles")
@RestController
public class ArticlesController extends ApiController {

  /** The ArticlesRepository is used to interact with the database for Article entities */
  @Autowired ArticlesRepository articlesRepository;

  /**
   * This method returns a list of all articles.
   *
   * @return a list of all articles
   */
  @Operation(summary = "List all articles")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<Article> allArticles() {
    Iterable<Article> articles = articlesRepository.findAll();
    return articles;
  }

  /**
   * This method returns a single article.
   *
   * @param id id of the article to get
   * @return a single article
   */
  @Operation(summary = "Get a single article")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public Article getById(@Parameter(name = "id") @RequestParam Long id) {
    Article article =
        articlesRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Article.class, id));

    return article;
  }

  /**
   * This method creates a new article. Accessible only to users with the role "ROLE_ADMIN".
   *
   * @param title title of the article
   * @param url url of the article
   * @param explanation explanation of the article
   * @param email email of the person who submitted the article
   * @return the saved article (with its id field set by the database)
   */
  @Operation(summary = "Create a new article")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public Article postArticle(
      @Parameter(name = "title") @RequestParam String title,
      @Parameter(name = "url") @RequestParam String url,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "email") @RequestParam String email) {
    Article article = new Article();

    article.setTitle(title);
    article.setUrl(url);
    article.setExplanation(explanation);
    article.setEmail(email);
    article.setDateAdded(LocalDateTime.now());

    Article savedArticle = articlesRepository.save(article);
    return savedArticle;
  }

  /**
   * Update a single article. Accessible only to users with the role "ROLE_ADMIN".
   *
   * @param id id of the article to update
   * @param incoming the new article contents
   * @return the updated article object
   */
  @Operation(summary = "Update a single article")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public Article updateArticle(
      @Parameter(name = "id") @RequestParam Long id, @RequestBody @Valid Article incoming) {

    Article article =
        articlesRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Article.class, id));

    article.setTitle(incoming.getTitle());
    article.setUrl(incoming.getUrl());
    article.setExplanation(incoming.getExplanation());
    article.setEmail(incoming.getEmail());
    article.setDateAdded(incoming.getDateAdded());

    articlesRepository.save(article);

    return article;
  }

  /**
   * Deletes an article. Accessible only to users with the role "ROLE_ADMIN".
   *
   * @param id id of the article to delete
   * @return a message indicating that the article was deleted
   */
  @Operation(summary = "Delete an article")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteArticle(@Parameter(name = "id") @RequestParam Long id) {
    Article article =
        articlesRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Article.class, id));

    articlesRepository.delete(article);
    return genericMessage("Article with id %s deleted".formatted(id));
  }
}
