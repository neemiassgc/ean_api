package com.api.configuration;

import com.api.job.ScanJob;
import com.api.component.Constants;
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

    private void trigger() throws SchedulerException {
        final JobDetail job = JobBuilder
            .newJob(ScanJob.class)
            .withIdentity("scanJob")
            .storeDurably()
            .requestRecovery()
            .build();

        CronScheduleBuilder cronSchedule = CronScheduleBuilder
            .cronSchedule("0 7 5 * * ?")
            .withMisfireHandlingInstructionFireAndProceed()
            .inTimeZone(TimeZone.getTimeZone(Constants.TIMEZONE));

        Trigger trigger = TriggerBuilder
            .newTrigger()
            .withIdentity("scanTrigger")
            .withSchedule(cronSchedule)
            .build();

        scheduler.scheduleJob(job, trigger);
    }

}
