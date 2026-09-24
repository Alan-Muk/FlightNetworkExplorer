package com.backend.backend.controller;

import com.backend.backend.dto.HubDTO;
import com.backend.backend.service.HubService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/hubs", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class HubController {

  private final HubService hubService;

  public HubController(HubService hubService) {
    this.hubService = hubService;
  }

  /**
   * Returns the top-N hubs ranked by combined departure + arrival connectivity.
   *
   * @param limit how many hubs to return (1–500, default 150)
   */
  @GetMapping
  public List<HubDTO> hubs(@RequestParam(defaultValue = "150") @Min(1) @Max(500) int limit) {
    return hubService.getMajorHubs(limit);
  }
}
