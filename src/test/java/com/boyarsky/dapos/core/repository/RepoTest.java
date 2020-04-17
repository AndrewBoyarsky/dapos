package com.boyarsky.dapos.core.repository;

import com.boyarsky.dapos.StoreExtension;
import com.boyarsky.dapos.core.TransactionManager;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public abstract class RepoTest {
    @RegisterExtension
    static StoreExtension extension = new StoreExtension(false);
    @Autowired
    public TransactionManager manager;

    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @ComponentScan("com.boyarsky.dapos.core.repository.aop")
    @TestConfiguration
    public static class Config {
        @Bean
        XodusRepoContext context() {
            return new XodusRepoContext(extension.getStore(), manager());
        }

        @Bean
        TransactionManager manager() {
            return new TransactionManager(extension.getStore());
        }
    }

}
