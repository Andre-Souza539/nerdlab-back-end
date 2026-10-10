package com.nerdlab.application.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseService.class);

    /**
     * Placeholder method for Udemy API integration.
     * In the future, this will use a RestTemplate or WebClient to fetch data from Udemy.
     */
    public void syncCoursesWithUdemy() {
        log.info("Starting course synchronization with Udemy API...");
        // TODO: Implement actual API call using client credentials
        log.info("Successfully synced courses (Mock).");
    }

    public List<Object> getFeaturedCourses() {
        // Return mock data for now
        return List.of();
    }
}
