package com.api.controller;

import com.api.job.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class JobsRouter {

    private final Job job;

    @GetMapping("api/jobs/updatePrices")
    public ResponseEntity<HttpStatus> doJob(final @RequestHeader HttpHeaders httpHeaders) {
        final List<String> appEngineCronHeader = httpHeaders.get("X-Appengine-Cron");
        if (Objects.isNull(appEngineCronHeader)) return ResponseEntity.badRequest().body(HttpStatus.BAD_REQUEST);
        if (appEngineCronHeader.contains("true")) {
            job.execute();
            return ResponseEntity.ok(HttpStatus.OK);
        }
        return ResponseEntity.badRequest().body(HttpStatus.BAD_REQUEST);
    }
}
