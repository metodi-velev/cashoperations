package com.example.cashdocumentsservice.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

@RestController
public class StatsController {

    @GetMapping("/stats")
    public Map<String, Object> getThreadStats() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        Map<String, Object> statsMap = new LinkedHashMap<>();
        List<Object> stats = new LinkedList<>();

        stats.add(new Pair("activeThreads", threadBean.getThreadCount()));
        stats.add(new Pair("peakThreadCount", threadBean.getPeakThreadCount()));
        stats.add(new Pair("virtualThreadsSupported", Thread.currentThread().isVirtual()));
        stats.add(new Pair("daemonThreadCount", threadBean.getDaemonThreadCount()));
        stats.add(new Pair("totalStartedThreadCount", threadBean.getTotalStartedThreadCount()));

        List<List<Object>> statsList = List.of(stats);

        statsMap.put("Thread Stats", statsList);

        return statsMap;
    }
}

@Getter
@Setter
class Pair {
    private String name;
    private Object value;
    public Pair(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public Pair() {}
}
