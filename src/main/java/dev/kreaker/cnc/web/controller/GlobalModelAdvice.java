/* (c) 2026 Alejandro Lopez Monzon <alejandro@kreaker.dev> for Kreaker Developments */
package dev.kreaker.cnc.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

   @Value("${DB_HOST:localhost}")
   private String dbHost;

   @Value("${DB_PORT:1521}")
   private String dbPort;

   @Value("${DB_SERVICE:ORCL}")
   private String dbService;

   @ModelAttribute("currentUser")
   public String currentUser() {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
         return auth.getName();
      }
      return null;
   }

   @ModelAttribute("dbService")
   public String getDbService() {
      return dbService;
   }

   @ModelAttribute("dbConnectionString")
   public String getDbConnectionString() {
      return dbHost + ":" + dbPort + "/" + dbService;
   }
}
