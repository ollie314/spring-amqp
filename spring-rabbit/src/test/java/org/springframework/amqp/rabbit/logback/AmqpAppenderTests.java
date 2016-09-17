/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.amqp.rabbit.logback;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author Stephen Oakey
 *
 * @since 2.0
 */
public class AmqpAppenderTests {

	@Test
	public void testDefaultConfiguration() {
		AmqpAppender appender = new AmqpAppender();

		RabbitConnectionFactoryBean bean = mock(RabbitConnectionFactoryBean.class);
		appender.configureRabbitConnectionFactory(bean);

		verifyDefaultHostProperties(bean);
		verify(bean, never()).setUseSSL(anyBoolean());
	}

	@Test
	public void testCustomHostInformation() {
		AmqpAppender appender = new AmqpAppender();

		String host = "rabbitmq.com";
		int port = 5671;
		String username = "user";
		String password = "password";
		String virtualHost = "vhost";

		appender.setHost(host);
		appender.setPassword(password);
		appender.setPort(port);
		appender.setUsername(username);
		appender.setVirtualHost(virtualHost);

		RabbitConnectionFactoryBean bean = mock(RabbitConnectionFactoryBean.class);
		appender.configureRabbitConnectionFactory(bean);

		verify(bean).setHost(host);
		verify(bean).setPort(port);
		verify(bean).setUsername(username);
		verify(bean).setPassword(password);
		verify(bean).setVirtualHost(virtualHost);
	}

	@Test
	public void testDefaultSslConfiguration() {
		AmqpAppender appender = new AmqpAppender();
		appender.setUseSsl(true);

		RabbitConnectionFactoryBean bean = mock(RabbitConnectionFactoryBean.class);
		appender.configureRabbitConnectionFactory(bean);

		verifyDefaultHostProperties(bean);
		verify(bean).setUseSSL(eq(true));
		verify(bean, never()).setSslAlgorithm(anyString());
	}

	@Test
	public void testSslConfigurationWithAlgorithm() {
		AmqpAppender appender = new AmqpAppender();
		appender.setUseSsl(true);
		String sslAlgorithm = "TLSv2";
		appender.setSslAlgorithm(sslAlgorithm);

		RabbitConnectionFactoryBean bean = mock(RabbitConnectionFactoryBean.class);
		appender.configureRabbitConnectionFactory(bean);

		verifyDefaultHostProperties(bean);
		verify(bean).setUseSSL(eq(true));
		verify(bean).setSslAlgorithm(eq(sslAlgorithm));
	}

	@Test
	public void testSslConfigurationWithSslPropertiesResource() {
		AmqpAppender appender = new AmqpAppender();
		appender.setUseSsl(true);

		String path = "ssl.properties";
		appender.setSslPropertiesLocation("classpath:" + path);

		RabbitConnectionFactoryBean bean = mock(RabbitConnectionFactoryBean.class);
		appender.configureRabbitConnectionFactory(bean);

		verifyDefaultHostProperties(bean);
		verify(bean).setUseSSL(eq(true));
		verify(bean).setSslPropertiesLocation(eq(new ClassPathResource(path)));
		verify(bean, never()).setKeyStore(anyString());
		verify(bean, never()).setKeyStorePassphrase(anyString());
		verify(bean, never()).setKeyStoreType(anyString());
		verify(bean, never()).setTrustStore(anyString());
		verify(bean, never()).setTrustStorePassphrase(anyString());
		verify(bean, never()).setTrustStoreType(anyString());
	}

	@Test
	public void testSslConfigurationWithKeyAndTrustStore() {
		AmqpAppender appender = new AmqpAppender();
		appender.setUseSsl(true);

		String keyStore = "file:/path/to/client/keycert.p12";
		String keyStorePassphrase = "secret";
		String keyStoreType = "foo";
		String trustStore = "file:/path/to/client/truststore";
		String trustStorePassphrase = "secret2";
		String trustStoreType = "bar";

		appender.setKeyStore(keyStore);
		appender.setKeyStorePassphrase(keyStorePassphrase);
		appender.setKeyStoreType(keyStoreType);
		appender.setTrustStore(trustStore);
		appender.setTrustStorePassphrase(trustStorePassphrase);
		appender.setTrustStoreType(trustStoreType);

		RabbitConnectionFactoryBean bean = mock(RabbitConnectionFactoryBean.class);
		appender.configureRabbitConnectionFactory(bean);

		verifyDefaultHostProperties(bean);
		verify(bean).setUseSSL(eq(true));
		verify(bean, never()).setSslPropertiesLocation(any());
		verify(bean).setKeyStore(keyStore);
		verify(bean).setKeyStorePassphrase(keyStorePassphrase);
		verify(bean).setKeyStoreType(keyStoreType);
		verify(bean).setTrustStore(trustStore);
		verify(bean).setTrustStorePassphrase(trustStorePassphrase);
		verify(bean).setTrustStoreType(trustStoreType);
	}

	@Test
	public void testSslConfigurationWithKeyAndTrustStoreDefaultTypes() {
		AmqpAppender appender = new AmqpAppender();
		appender.setUseSsl(true);

		RabbitConnectionFactoryBean bean = mock(RabbitConnectionFactoryBean.class);
		appender.configureRabbitConnectionFactory(bean);

		verifyDefaultHostProperties(bean);
		verify(bean).setUseSSL(eq(true));
		verify(bean).setKeyStoreType("JKS");
		verify(bean).setTrustStoreType("JKS");
	}

	@Test
	public void testStartWithInvalidConnectionConfiguration() {
		AmqpAppender appender = new AmqpAppender();
		appender.setUseSsl(true);
		appender.setKeyStore("foo");
		appender.start();

		assertFalse((boolean) ReflectionTestUtils.getField(appender, "started"));
	}

	private void verifyDefaultHostProperties(RabbitConnectionFactoryBean bean) {
		verify(bean).setHost("localhost");
		verify(bean).setPort(5672);
		verify(bean).setUsername("guest");
		verify(bean).setPassword("guest");
		verify(bean).setVirtualHost("/");
	}

}
