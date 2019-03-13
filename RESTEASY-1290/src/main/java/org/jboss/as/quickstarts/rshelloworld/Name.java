/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.as.quickstarts.rshelloworld;

public enum Name {
   John, Jane;
   
   public static Name fromString(String nameStr) {
      return Name.valueOf(nameStr);
   }
}
