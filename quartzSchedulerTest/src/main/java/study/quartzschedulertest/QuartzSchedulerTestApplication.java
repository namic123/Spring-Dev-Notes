package study.quartzschedulertest;

import lombok.RequiredArgsConstructor;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import study.quartzschedulertest.job.TestSimpleJob;

import static org.quartz.JobBuilder.newJob;

@SpringBootApplication
@RequiredArgsConstructor
@EnableScheduling
public class QuartzSchedulerTestApplication {


    public static void main(String[] args) {
        SpringApplication.run(QuartzSchedulerTestApplication.class, args);

    }

}
