package com.example.MilkSystemApplication.controller;

import com.example.MilkSystemApplication.model.MilkCollection;
import com.example.MilkSystemApplication.service.MilkCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/collections")
public class MilkCollectionController {

    @Autowired
    private MilkCollectionService milkService;

    // Get all collections or filter by date
    @GetMapping
    public List<MilkCollection> getAllCollections(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date != null) {
            return milkService.getCollectionsByDate(date);
        }
        return milkService.getAllCollections();
    }

    // Get collection by ID
    @GetMapping("/{id}")
    public ResponseEntity<MilkCollection> getCollectionById(@PathVariable Long id) {
        return milkService.getCollectionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get all collections for a farmer
    @GetMapping("/farmer/{farmerId}")
    public List<MilkCollection> getCollectionsByFarmer(@PathVariable Long farmerId) {
        return milkService.getCollectionsByFarmer(farmerId);
    }

    // Add a new milk collection (via JSON body)
    @PostMapping
    public ResponseEntity<MilkCollection> createCollection(@RequestBody MilkCollection request) {
        MilkCollection mc = milkService.addCollection(
                request.getFarmerId(),
                request.getLitres(),
                request.getFatPercentage(),
                request.getShift(),
                request.getDate()
        );
        return ResponseEntity.ok(mc);
    }

    // Add a new milk collection (via form parameters)
    @PostMapping("/add")
    public MilkCollection addCollection(@RequestParam Long farmerId,
                                        @RequestParam double litres,
                                        @RequestParam double fatPercentage,
                                        @RequestParam(defaultValue = "Morning") String shift,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return milkService.addCollection(farmerId, litres, fatPercentage, shift, date);
    }

    // Delete collection
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable Long id) {
        milkService.deleteCollection(id);
        return ResponseEntity.noContent().build();
    }

    // Rate calculator preview endpoint
    @GetMapping("/calculate-rate")
    public Map<String, Double> calculateRate(@RequestParam double fatPercentage, @RequestParam(defaultValue = "1.0") double litres) {
        double rate = milkService.calculateRatePerLitre(fatPercentage);
        double total = Math.round(litres * rate * 100.0) / 100.0;
        return Map.of(
                "fatPercentage", fatPercentage,
                "ratePerLitre", rate,
                "litres", litres,
                "estimatedAmount", total
        );
    }
}


