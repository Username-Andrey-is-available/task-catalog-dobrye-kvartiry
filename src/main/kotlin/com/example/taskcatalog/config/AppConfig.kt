package com.example.taskcatalog.config

import java.time.Clock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
