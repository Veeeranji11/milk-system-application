package com.example.MilkSystemApplication.controller;

import com.example.MilkSystemApplication.model.Farmer;
import com.example.MilkSystemApplication.service.FarmerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/farmers")
public class FarmerController {

    @Autowired
    private FarmerService farmerService;

    // Get all farmers or search
    @GetMapping
    public List<Farmer> getAllFarmers(@RequestParam(value = "search", required = false) String search) {
        if (search != null && !search.trim().isEmpty()) {
            return farmerService.searchFarmers(search);
        }
        return farmerService.getAllFarmers();
    }

    // Get farmer by id
    @GetMapping("/{id}")
    public ResponseEntity<Farmer> getFarmerById(@PathVariable Long id) {
        return farmerService.getFarmerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Add a new farmer
    @PostMapping(value = {"", "/add"})
    public ResponseEntity<Farmer> addFarmer(@RequestBody Farmer farmer) {
        Farmer saved = farmerService.addFarmer(farmer);
        return ResponseEntity.ok(saved);
    }

    // Update farmer
    @PutMapping("/{id}")
    public ResponseEntity<Farmer> updateFarmer(@PathVariable Long id, @RequestBody Farmer farmer) {
        try {
            Farmer updated = farmerService.updateFarmer(id, farmer);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete farmer
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFarmer(@PathVariable Long id) {
        farmerService.deleteFarmer(id);
        return ResponseEntity.noContent().build();
    }
}


