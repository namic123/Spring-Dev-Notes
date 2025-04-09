package study.quartzschedulertest.config;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import study.quartzschedulertest.listener.JobCustomListener;
import study.quartzschedulertest.listener.TriggerCustomListener;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

/**
 * Quartz Scheduler의 설정 클래스입.
 * SchedulerFactoryBean을 Spring에 등록하고,
 * 커스텀 JobFactory, 트랜잭션 매니저, 외부 설정 파일 등 Quartz 실행에 필요한 기본 구성을 제공.
 *
 * @author 박재성
 * @version 1.0
 * @since 2025-03-25
 */
@Configuration // Spring 설정 클래스임을 명시
@AllArgsConstructor // 생성자 주입을 자동 생성 (transactionManager 주입)
public class QuartzConfig {

    // Spring의 ApplicationContext로, Quartz Job이 의존성을 주입받을 수 있도록 도와줌
    ApplicationContext applicationContext;

    // Quartz의 전역 트리거 리스너
    // 모든 트리거 실행 시 동작할 커스텀 로직을 정의
    TriggerCustomListener triggerCustomListener;

    // Quartz의 전역 Job 리스너
    // 모든 Job 실행 시 동작할 커스텀 로직을 정의
    JobCustomListener jobCustomListener;

    // Quartz에서 사용할 DataSource
    // Spring에서 관리하는 커넥션 풀을 Quartz에 전달
    DataSource dataSource;

    // 트랜잭션 매니저
    // Quartz 작업에서도 Spring의 트랜잭션 기능을 사용할 수 있도록 연결
    PlatformTransactionManager transactionManager;

    // 외부에서 quartz 설정 파일 경로를 주입받음 (application.yml 등에서 설정)
//    @NonFinal
//    @Value("${setting.quartz.file-path}")
//    String filePath;

    /**
     * SchedulerFactoryBean을 생성하고 커스텀 JobFactory, 트리거/잡 리스너, 설정 파일 등을 포함한 Quartz 설정을 구성
     *
     * @return SchedulerFactoryBean (Spring이 관리하는 Quartz Scheduler)
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        // Quartz 스케줄러를 만드는 핵심 Bean
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();

        // Quartz Job 객체에 Spring 의존성 주입을 가능하게 하는 JobFactory 생성
        AutowiringSpringBeanJobFactory autowiringSpringBeanJobFactory =
                new AutowiringSpringBeanJobFactory(applicationContext);

        // 위에서 만든 JobFactory를 SchedulerFactory에 설정
        schedulerFactoryBean.setJobFactory(autowiringSpringBeanJobFactory);

        // Spring에서 관리하는 DataSource 사용
        schedulerFactoryBean.setDataSource(dataSource);

        // 전역 잡 리스너 설정
        schedulerFactoryBean.setGlobalJobListeners(jobCustomListener);

        // 전역 트리거 리스너 설정
        schedulerFactoryBean.setGlobalTriggerListeners(triggerCustomListener);

        // 애플리케이션 시작 시 자동으로 스케줄러를 시작하지 않음
        schedulerFactoryBean.setAutoStartup(false);

        // 트랜잭션 매니저 설정
        schedulerFactoryBean.setTransactionManager(transactionManager);

        // 외부 설정 파일을 불러와 Quartz 프로퍼티로 등록
        schedulerFactoryBean.setQuartzProperties(quartzProperties());

        return schedulerFactoryBean;
    }

    /**
     * Quartz 설정을 담은 properties 파일을 로드
     * classpath 경로에 존재하는 quartz.properties 파일을 읽어와 Properties 객체로 반환
     *
     * @return Quartz 설정을 담은 Properties 객체
     */
    private Properties quartzProperties() {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();

        // classpath 아래 quartz.properties 파일 참조
        propertiesFactoryBean.setLocation(new ClassPathResource("quartz.properties"));

        Properties properties = null;

        try {
            // propertiesFactoryBean을 초기화하고 설정된 파일로부터 properties 객체 생성
            propertiesFactoryBean.afterPropertiesSet();
            properties = propertiesFactoryBean.getObject();
        } catch (IOException e) {
            // 예외 발생 시 콘솔에 출력
            e.printStackTrace();
        }

        return properties;
    }
}
