package com.DigitalDining.demo.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.DigitalDining.demo.model.User;
import com.DigitalDining.demo.repository.UserRepository;

@Service
public class WeeklyMenuScheduler {

	private final WeeklyMenuPdfService weeklyMenuPdfService;
    private final UserRepository userRepository;

    public WeeklyMenuScheduler(WeeklyMenuPdfService weeklyMenuPdfService,
                               UserRepository userRepository) {
        this.weeklyMenuPdfService = weeklyMenuPdfService;
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 8 ? * MON", zone = "Europe/Budapest")
    public void sendWeeklyMenuEveryMonday() {
        List<User> subscribedUsers = userRepository.findAllByWeeklyMenuSubscribedTrue();

        for (User user : subscribedUsers) {
            try {
                weeklyMenuPdfService.sendWeeklyMenuPdfByEmail(user.getEmail());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
