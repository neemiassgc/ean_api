package com.api.controller;

import com.api.job.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Log4j2
public class ActionController {

    private final Job job;

    @GetMapping("api/jobs/updatePrices")
    public ResponseEntity<String> doJob(final @RequestHeader HttpHeaders httpHeaders) {
        final List<String> appEngineCronHeader = httpHeaders.get("X-Appengine-Cron");
        if (Objects.isNull(appEngineCronHeader)) return ResponseEntity.badRequest().body(HttpStatus.BAD_REQUEST.name());
        if (appEngineCronHeader.contains("true")) {
            runParallel(job::execute);
            return ResponseEntity.ok(HttpStatus.OK.name());
        }
        return ResponseEntity.badRequest().body(HttpStatus.BAD_REQUEST.name());
    }

    private void runParallel(final Runnable task) {
        log.info("Running the job in parallel");
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(task);
        executorService.shutdown();
    }

    @GetMapping("hit")
    @ResponseStatus(HttpStatus.OK)
    public String hit() {
        return "OK";
    }
}
