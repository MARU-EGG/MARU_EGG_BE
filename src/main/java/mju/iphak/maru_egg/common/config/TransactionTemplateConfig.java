package mju.iphak.maru_egg.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class TransactionTemplateConfig {

	@Bean("readTransactionTemplate")
	TransactionTemplate readTransactionTemplate(
		@Qualifier("transactionManager") PlatformTransactionManager transactionManager
	) {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setReadOnly(true);
		return template;
	}

	@Bean("writeTransactionTemplate")
	TransactionTemplate writeTransactionTemplate(
		@Qualifier("transactionManager") PlatformTransactionManager transactionManager
	) {
		return new TransactionTemplate(transactionManager);
	}
}
