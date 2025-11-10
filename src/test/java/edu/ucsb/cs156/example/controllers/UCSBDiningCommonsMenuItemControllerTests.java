package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItems;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemsRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {
  @MockBean UCSBDiningCommonsMenuItemsRepository ucsbDiningCommonsMenuItemsRepository;

  @MockBean UserRepository userRepository;

  // Authorization tests for /api/ucsb-dining-commons-menu-items/admin/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsb-dining-commons-menu-items/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsb-dining-commons-menu-items/all"))
        .andExpect(status().is(200)); // logged
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/ucsb-dining-commons-menu-items?id=7"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  // Authorization tests for /api/ucsb-dining-commons-menu-items/post
  // (Perhaps should also have these for put and delete)

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/ucsb-dining-commons-menu-items/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/ucsb-dining-commons-menu-items/post"))
        .andExpect(status().is(403)); // only admins can post
  }

  // // Tests with mocks for database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsbdiningcommonsmenuitems() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItems menuItem1 =
        UCSBDiningCommonsMenuItems.builder()
            .diningCommonsCode("test-code-1")
            .name("test item 1")
            .station("test station 1")
            .build();

    UCSBDiningCommonsMenuItems menuItem2 =
        UCSBDiningCommonsMenuItems.builder()
            .diningCommonsCode("test-code-2")
            .name("test item 2")
            .station("test station 2")
            .build();

    ArrayList<UCSBDiningCommonsMenuItems> expectedItems = new ArrayList<>();
    expectedItems.addAll(Arrays.asList(menuItem1, menuItem2));

    when(ucsbDiningCommonsMenuItemsRepository.findAll()).thenReturn(expectedItems);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsb-dining-commons-menu-items/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedItems);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange

    UCSBDiningCommonsMenuItems ucsbDiningCommonsMenuItems =
        UCSBDiningCommonsMenuItems.builder()
            .diningCommonsCode("Ortega")
            .name("French Fries")
            .station("side")
            .build();

    when(ucsbDiningCommonsMenuItemsRepository.findById(eq(7L)))
        .thenReturn(Optional.of(ucsbDiningCommonsMenuItems));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsb-dining-commons-menu-items?id=7"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(ucsbDiningCommonsMenuItems);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_not_get_by_id_when_the_id_does_not_exist()
      throws Exception {

    // arrange

    when(ucsbDiningCommonsMenuItemsRepository.findById(eq(7L))).thenReturn(Optional.empty());
    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsb-dining-commons-menu-items?id=7"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBDiningCommonsMenuItems with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_ucsbdiningcommonsmenuitem() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItems menuItem =
        UCSBDiningCommonsMenuItems.builder()
            .diningCommonsCode("test-code-1")
            .name("test item 1")
            .station("test station 1")
            .build();

    when(ucsbDiningCommonsMenuItemsRepository.save(eq(menuItem))).thenReturn(menuItem);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsb-dining-commons-menu-items/post?diningCommonsCode=test-code-1&name=test item 1&station=test station 1")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemsRepository, times(1)).save(menuItem);
    String expectedJson = mapper.writeValueAsString(menuItem);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_ucsbdiningcommonsmenuitem() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItems ucsbDiningCommonsMenuItemOrig =
        UCSBDiningCommonsMenuItems.builder()
            .diningCommonsCode("Ortega")
            .name("French Fries")
            .station("side")
            .build();

    UCSBDiningCommonsMenuItems ucsbDiningCommonMenuItemEdited =
        UCSBDiningCommonsMenuItems.builder()
            .diningCommonsCode("Edited Code")
            .name("Edited name")
            .station("Edited station")
            .build();

    String requestBody = mapper.writeValueAsString(ucsbDiningCommonMenuItemEdited);

    when(ucsbDiningCommonsMenuItemsRepository.findById(eq(67L)))
        .thenReturn(Optional.of(ucsbDiningCommonsMenuItemOrig));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsb-dining-commons-menu-items?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(67L);
    verify(ucsbDiningCommonsMenuItemsRepository, times(1))
        .save(ucsbDiningCommonMenuItemEdited); // should be saved with
    // correct user
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_ucsbiningcommonsmenuitem_that_does_not_exist() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItems ucsbEditedDiningCommonsMenuItems =
        UCSBDiningCommonsMenuItems.builder()
            .diningCommonsCode("Edited Code")
            .name("Edited Name")
            .station("Edited Station")
            .build();

    String requestBody = mapper.writeValueAsString(ucsbEditedDiningCommonsMenuItems);

    when(ucsbDiningCommonsMenuItemsRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsb-dining-commons-menu-items?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItems with id 67 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_menu_item() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItems ucsbDiningCommonsMenuItem1 =
        UCSBDiningCommonsMenuItems.builder()
            .diningCommonsCode("Test Code")
            .name("Test Name")
            .station("Test Station")
            .build();

    when(ucsbDiningCommonsMenuItemsRepository.findById(eq(15L)))
        .thenReturn(Optional.of(ucsbDiningCommonsMenuItem1));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsb-dining-commons-menu-items?id=15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(15L);
    verify(ucsbDiningCommonsMenuItemsRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItem with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_menu_item_and_gets_right_error_message()
      throws Exception {
    // arrange

    when(ucsbDiningCommonsMenuItemsRepository.findById(eq(15L))).thenReturn(Optional.empty());
    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsb-dining-commons-menu-items?id=15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(15L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItems with id 15 not found", json.get("message"));
  }
}
