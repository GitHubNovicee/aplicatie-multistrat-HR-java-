package org.hr.config;

import org.hr.services.IHolidayCalendar;
import org.hr.services.RomanianLegalHolidayCalendar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Spring App Configuration Class.
 * Componentele (@Service, @Repository, @Component) sunt descoperite prin component-scan
 * pornind de la pachetul org.hr (HrApplication). Aici se declara doar beanurile de infrastructura
 * care nu sunt clase proprii adnotate:
 * - Clock: sursa de timp injectabila (CR-02, CR-08, CR-10 depind de "data curenta"; testele pot inlocui beanul);
 * - IHolidayCalendar: calendarul sarbatorilor legale folosit la calculul zilelor lucratoare (CR-02).
 */
@Configuration(proxyBeanMethods = false)
public class AppScanConfig {

    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public IHolidayCalendar holidayCalendar() {
        return new RomanianLegalHolidayCalendar();
    }
}
