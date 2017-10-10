/**
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */ 
package restingzipeap.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyBuilder;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.interceptors.encoding.AcceptEncodingGZIPFilter;
import org.jboss.resteasy.plugins.interceptors.encoding.AcceptEncodingGZIPInterceptor;
import org.jboss.resteasy.plugins.interceptors.encoding.GZIPDecodingInterceptor;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.formatter.Formatter;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.undertow.attribute.AuthenticationTypeExchangeAttribute.Builder;
import restingzipeap.IEchoRest;
import restingzipeap.rest.JaxRsActivator;

import javax.ws.rs.ApplicationPath;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$
 * $Date$:    Date of last commit
 *
 */
@RunWith(Arquillian.class)
@RunAsClient
public class SimpleEchoTest {
	
	private static final String RESOURCE_PREFIX = JaxRsActivator.class.getAnnotation(ApplicationPath.class).value().substring(1);
	
	@Deployment(name="war_with_providers_file")
	public static WebArchive createWebDeploymentWithGzipProviders() {
		return createWebArchive("test_war_with_providers.war", true);
	}

	@Deployment(name="war_without_providers_file")
	public static WebArchive createWebDeploymentWithoutGzipProviders() {
		return createWebArchive("test_war_without_providers.war", false);
	}

	private static WebArchive createWebArchive(String name, boolean activateGzip) {
		WebArchive war = ShrinkWrap.create(WebArchive.class, name)
				.addPackage(IEchoRest.class.getPackage())
				.addPackage(JaxRsActivator.class.getPackage())
				.addAsWebInfResource(EmptyAsset.INSTANCE, "WEB-INF/beans.xml")
				.setWebXML("WEB-INF/web.xml");
				//.setWebXML("WEB-INF/webWithListener.xml");
		
		if (activateGzip) {
			war.addAsManifestResource("services/javax.ws.rs.ext.Providers");
		}
		return war;
	}
	
	@ArquillianResource
	URL deploymentUrl;

	@BeforeClass
	public static void initResteasyClient() {
		RegisterBuiltin.register(ResteasyProviderFactory.getInstance());

		// GZIP needs to be registered client side too when using 2.3.17 +
		// ResteasyProviderFactory.getInstance().registerProvider(GZIPDecodingInterceptor.class);		 
	}

	@Test
	@OperateOnDeployment("war_with_providers_file")
	public void testGzipWithProviders(){
		test();
		
	}

	@Test
	@OperateOnDeployment("war_without_providers_file")
	public void testGzipWithoutProviders() {
		test();

	}

	private void test() {
		String message2echo = "some statement";
		System.out.println("URL: "+deploymentUrl.toString() + RESOURCE_PREFIX);
		for(Class<?> c : ResteasyProviderFactory.getInstance().getProviderClasses()){
			System.out.println("Provider class: " + c.getName());
		}
		IEchoRest iEchoRest = ProxyFactory.create(IEchoRest.class, deploymentUrl.toString() + RESOURCE_PREFIX);
		String echo = iEchoRest.echo(message2echo);
		System.out.println("ECHO from server: " + echo);
		assertNotNull(echo);
		assertEquals(message2echo, echo);
	}

}
