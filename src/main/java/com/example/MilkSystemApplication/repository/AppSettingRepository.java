package com.example.MilkSystemApplication.repository;

import com.example.MilkSystemApplication.model.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingRepository extends JpaRepository<AppSetting, Long> {
}