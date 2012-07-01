/*
 * Licensed to David Pilato and Malloum Laya (the "Authors") under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Authors licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package fr.issamax.itest.essearch.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import fr.issamax.essearch.api.common.data.RestResponseWelcome;
import fr.issamax.essearch.api.common.data.Welcome;

/**
 * If you extend this test class, it will check
 * if your API respect some rules :
 * <ul>
 * <li>You should have an entry point GET /_help on your API
 * @author David Pilato
 *
 */
public abstract class AbstractApiTest extends AbstractConfigurationIntegrationTest {
	protected static final String BASE_URL = "http://localhost:9090/essearch/api/";

	private ESLogger logger = ESLoggerFactory.getLogger(AbstractApiTest.class
			.getName());

	/**
	 * Define your API URL to append to the common base, e.g.: doc/
	 * @return
	 */
	protected abstract String getModuleApiUrl();
	
	/**
	 * Rest Client is automatically injected
	 */
	@Autowired protected RestTemplate restTemplate;

	/**
	 * We check that your API respect the rule : you should provide some help
	 * @throws Exception
	 */
	@Test
	public void asking_for_help() throws Exception {
		logger.debug("Testing _help entry point for {} api.", getModuleApiUrl() == null ? "/" : getModuleApiUrl());
		RestResponseWelcome response = restTemplate.getForObject(buildFullApiUrl("_help"), RestResponseWelcome.class);
		assertNotNull(response);
		assertTrue(response.isOk());
		assertNotNull(response.getObject());
		Welcome output = (Welcome) response.getObject();
		assertNotNull(output);
		assertNotNull(output.getApis());
		assertTrue(output.getApis().length > 0);
		logger.debug("{} _help entry point provided {} hints.", 
				getModuleApiUrl() == null ? "/" : getModuleApiUrl(), 
				output.getApis().length);
	}

	/**
	 * Here you can get the final URL you want to test
	 * @param append String to append to URL (could be null)
	 * @return A full URL for test
	 */
	protected String buildFullApiUrl(String append) {
		StringBuffer sbf = new StringBuffer(BASE_URL);
		if (getModuleApiUrl() != null) sbf.append(getModuleApiUrl());
		if (append != null) sbf.append(append);
		return sbf.toString();
	}

	/**
	 * Here you can get the final URL you want to test
	 * @return A full URL for test
	 */
	protected String buildFullApiUrl() {
		return buildFullApiUrl(null);
	}
}