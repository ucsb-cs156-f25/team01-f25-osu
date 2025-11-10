package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItems;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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

/** This is a REST controller for UCSBDiningCommonsMenuItems */
@Tag(name = "UCSBDiningCommonsMenuItems")
@RequestMapping("/api/ucsb-dining-commons-menu-items")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {
  @Autowired UCSBDiningCommonsMenuItemsRepository ucsbDiningCommonsMenuItemsRepository;

  /**
   * List all UCSB dining commons menu items
   *
   * @return an iterable of UCSBDiningCommonsMenuItems
   */
  @Operation(summary = "List all ucsb dining commons menu items")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBDiningCommonsMenuItems> allUCSBDiningCommonsMenuItems() {
    Iterable<UCSBDiningCommonsMenuItems> items = ucsbDiningCommonsMenuItemsRepository.findAll();
    return items;
  }

  /**
   * Get a single menu item by id
   *
   * @param id the id of the menu item
   * @return a UCSBDiningCommonsMenuItems
   */
  @Operation(summary = "Get a single menu item by id")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBDiningCommonsMenuItems getById(@Parameter(name = "id") @RequestParam Long id) {
    UCSBDiningCommonsMenuItems ucsbDiningCommonsMenuItem =
        ucsbDiningCommonsMenuItemsRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItems.class, id));

    return ucsbDiningCommonsMenuItem;
  }

  /**
   * Create a new menu item
   *
   * @param name the item name
   * @param diningCommonsCode the dining commons code
   * @param station the station
   * @return the saved ucsb dining commons menu item
   */
  @Operation(summary = "Create a new item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBDiningCommonsMenuItems postUCSBDiningCommonsMenuItem(
      @Parameter(name = "diningCommonsCode") @RequestParam String diningCommonsCode,
      @Parameter(name = "name") @RequestParam String name,
      @Parameter(name = "station") @RequestParam String station)
      throws JsonProcessingException {

    log.info(
        "POST /api/ucsb-dining-commons-menu-items/post diningCommonsCode={} name={} station={}",
        diningCommonsCode,
        name,
        station);

    UCSBDiningCommonsMenuItems ucsbDiningCommonsMenuItem = new UCSBDiningCommonsMenuItems();
    ucsbDiningCommonsMenuItem.setDiningCommonsCode(diningCommonsCode);
    ucsbDiningCommonsMenuItem.setName(name);
    ucsbDiningCommonsMenuItem.setStation(station);

    UCSBDiningCommonsMenuItems savedUcsbDiningCommonsMenuItem =
        ucsbDiningCommonsMenuItemsRepository.save(ucsbDiningCommonsMenuItem);

    return savedUcsbDiningCommonsMenuItem;
  }

  /**
   * Update a single menu item
   *
   * @param id id of the menu item to update
   * @param incoming the new name
   * @return the updated menu item object
   */
  @Operation(summary = "Update a single menu item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public UCSBDiningCommonsMenuItems updateUCSBDiningCommonsMenuItem(
      @Parameter(name = "id") @RequestParam Long id,
      @RequestBody @Valid UCSBDiningCommonsMenuItems incoming) {
    UCSBDiningCommonsMenuItems ucsbDiningCommonsMenuItem =
        ucsbDiningCommonsMenuItemsRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItems.class, id));

    ucsbDiningCommonsMenuItem.setDiningCommonsCode(incoming.getDiningCommonsCode());
    ucsbDiningCommonsMenuItem.setName(incoming.getName());
    ucsbDiningCommonsMenuItem.setStation(incoming.getStation());
    ucsbDiningCommonsMenuItemsRepository.save(ucsbDiningCommonsMenuItem);

    return ucsbDiningCommonsMenuItem;
  }

  /**
   * Delete a UCSBDiningCommonsMenuItem by id
   *
   * @param id the id of the menu item to delete
   * @return a message indicating the menu item was deleted
   */
  @Operation(summary = "Delete a UCSBDiningCommonsMenuItem")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteUCSBDiningCommonsMenuItem(@Parameter(name = "id") @RequestParam Long id) {
    UCSBDiningCommonsMenuItems ucsbDiningCommonsMenuItem =
        ucsbDiningCommonsMenuItemsRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItems.class, id));

    ucsbDiningCommonsMenuItemsRepository.delete(ucsbDiningCommonsMenuItem);
    return genericMessage("UCSBDiningCommonsMenuItem with id %s deleted".formatted(id));
  }
}
