package com.api.configuration;

import com.api.job.ScanJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@Configuration
public class JobConfiguration {

    @Autowired
    private Scheduler scheduler;

    @PostConstruct
    public void init() throws SchedulerException {
        this.trigger();
    }

    public void trigger() throws SchedulerException {
        final JobDetail job = JobBuilder
            .newJob(ScanJob.class)
            .withIdentity("scanJob")
            .storeDurably()
            .build();

        CronScheduleBuilder cronSchedule = CronScheduleBuilder
            .cronSchedule("0 0 5 * * ?")
            .inTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));

        Trigger trigger = TriggerBuilder
            .newTrigger()
            .withIdentity("scanTrigger")
            .withSchedule(cronSchedule)
            .build();

        scheduler.scheduleJob(job, trigger);
    }

}
