package study.quartzschedulertest.config;


import lombok.RequiredArgsConstructor;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * Spring의 의존성 주입 기능을 Quartz Job에 통합하기 위한 커스텀 JobFactory 클래스
 * Job 생성 시 Spring의 BeanFactory를 통해 자동 주입을 수행.
 *
 * 기본 SpringBeanJobFactory를 확장하여 Quartz Job 인스턴스를 생성할 때
 * Spring ApplicationContext를 통해 의존성을 자동 주입(Auto-wiring)함.
 *
 * 이 클래스를 SchedulerFactoryBean에 설정하면,
 * Quartz에서 생성하는 Job 클래스에 Spring의 @Autowired 또는 기타 Bean 주입이 정상 동작
 *
 * @author 박재성
 * @version 1.0
 * @since 2025-03-25
 */
@RequiredArgsConstructor
public class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {

    /**
     * Spring의 의존성 주입을 수행하는 BeanFactory.
     * ApplicationContext로부터 주입받아 사용.
     */
    private AutowireCapableBeanFactory autowireCapableBeanFactory;


    /**
     * 생성자 오버로딩을 통해 ApplicationContext로부터 BeanFactory를 초기화.
     * 이를 통해 생성된 Quartz Job 인스턴스에 Spring Bean을 자동으로 주입할 수 있음.
     *
     * @param context 현재 Spring의 ApplicationContext
     */
    public AutowiringSpringBeanJobFactory(ApplicationContext context) {
        this.autowireCapableBeanFactory = context.getAutowireCapableBeanFactory();
    }

    /**
     * Quartz Job 인스턴스를 생성하고,
     * 생성된 인스턴스에 대해 Spring의 의존성 자동 주입을 수행.
     *
     * @param bundle Trigger에 의해 실행될 Job에 대한 메타정보를 담은 객체
     * @return 의존성이 주입된 Quartz Job 인스턴스
     * @throws Exception 인스턴스 생성 또는 의존성 주입 중 오류 발생 시 예외 발생
     */
    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
        Object job = super.createJobInstance(bundle); // 기본 Job 생성
        autowireCapableBeanFactory.autowireBean(job); // 의존성 주입 수행
        return job;
    }
}
