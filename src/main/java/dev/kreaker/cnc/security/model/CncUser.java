/* (c) 2026 Alejandro Lopez Monzon <alejandro@kreaker.dev> for Kreaker Developments */
package dev.kreaker.cnc.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CncUser {

   private Long id;
   private String username;
   private String email;
   private String password;
   private String displayName;
   private boolean enabled;
   private String createdAt;
   private String updatedAt;
}
