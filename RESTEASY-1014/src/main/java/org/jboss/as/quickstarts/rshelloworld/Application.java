package org.jboss.as.quickstarts.rshelloworld;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;

import org.jboss.resteasy.plugins.cache.server.ServerCacheFeature;

/**
 * @author pjurak
 *
 */
//@ApplicationPath("/rest")
public class Application extends javax.ws.rs.core.Application
{

   @Override
   public Set<Class<?>> getClasses()
   {
     Set<Class<?>> classes = new HashSet<>();
     classes.add(ServerCacheFeature.class);
     return classes;
   }
}
