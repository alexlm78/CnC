# CnC - Catalogs & Conversions

A Spring Boot web application for managing catalog conversions between legacy (RV_CATALOGOS) and RPRO (RV_RPRO_CATALOGO) catalog systems, with full CRUD operations on conversion mappings stored in AL_CATALOG_TWOSTEP.

## Features

- **Unified Catalog View**: Displays combined catalogs from both RV_CATALOGOS (Legacy) and RV_RPRO_CATALOGO (RPRO) systems
- **Smart Filtering**: Filter by Module, Field, Chain (GNC/Arca), Catalog Source, and Conversion Status
- **Auto-Submit Filters**: Instant updates without manual button clicks
- **Dynamic Field Filtering**: Campo dropdown automatically updates based on selected Modulo
- **Conversion Management**: Full CRUD operations (Create, Read, Update, Delete) for conversion mappings
- **Catalog Source Visibility**: Clear indication of whether catalog items originate from Legacy or RPRO systems
- **Automatic Auditing**: Tracks creation and modification timestamps with user information
- **Oracle 11g Compatible**: Custom Hibernate dialect using ROWNUM-based pagination
- **Composite Key Support**: Handles multi-field primary keys (MODULO, CAMPO, VALOR, CADENA)
- **Bootstrap 5 UI**: Modern, responsive interface with Thymeleaf templates

## Prerequisites

- **Java**: 21 or higher
- **Gradle**: 9.2 or higher
- **Oracle Database**: 11g or higher
- **Database Access**: Connection to REPORTUSER schema

## Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd CnC
```

### 2. Database Configuration

#### Option A: Using .env file (Recommended)

Create a `.env` file in the project root:

```properties
DB_HOST=your-oracle-host
DB_PORT=1521
DB_SERVICE=your-service-name
DB_USER=REPORTUSER
DB_PASSWORD=your-password
```

**Note**: The `.env` file is already in `.gitignore` and will not be committed to version control.

#### Option B: Direct Configuration

Edit `src/main/resources/application.properties` and update the default values:

```properties
spring.datasource.url=jdbc:oracle:thin:@//${DB_HOST:your-host}:${DB_PORT:1521}/${DB_SERVICE:your-service}
spring.datasource.username=${DB_USER:REPORTUSER}
spring.datasource.password=${DB_PASSWORD:your-password}
```

### 3. Database Migration (If Required)

If your AL_CATALOG_TWOSTEP table doesn't have the MODULO field, run the migration script:

```bash
sqlplus REPORTUSER/your-password@your-service @doxs/ALTER_ADD_MODULO.sql
```

This script:
- Adds the MODULO column to AL_CATALOG_TWOSTEP
- Updates the composite primary key to include MODULO
- Preserves existing data

## Build & Run

### Build the Project

```bash
./gradlew clean build
```

### Run the Application

```bash
./gradlew bootRun
```

The application will be available at: **http://localhost:8080**

## Usage

### Main Catalog View

Navigate to `http://localhost:8080/catalogs` to:

- View all active catalog items from both Legacy and RPRO systems
- Filter by Module, Field, Chain (GNC/Arca), Source, and Conversion Status
- See which catalog items have conversion mappings
- Create new conversions for items without mappings
- View/edit existing conversions

### Create a Conversion

1. In the catalog list, find an item with "None" conversion badge
2. Click "Add Conversion"
3. Fill in Domain and Status (checkbox defaults to Active)
4. Submit to create the conversion mapping

### View Conversion Details

1. Click "View" on any catalog item with an existing conversion
2. See all conversion details including:
   - Catalog Source (Legacy or RPRO)
   - Module, Field, Value, Chain
   - Domain and Status
   - Audit trail (Created/Modified timestamps and users)

### Edit a Conversion

1. Click "Edit" on the detail view or directly from the catalog list
2. Modify Domain or Status
3. Note: Composite key fields (MODULO, CAMPO, VALOR, CADENA) are read-only after creation
4. Submit to update

### Delete a Conversion

1. Go to the conversion detail view
2. Click "Delete"
3. Confirm the deletion
4. The catalog item will now show "None" badge and allow creating a new conversion

## Project Structure

```
src/
├── main/
│   ├── java/dev/kreaker/cnc/
│   │   ├── config/
│   │   │   ├── ApplicationConfig.java          # Environment variable loading
│   │   │   ├── HibernateConfig.java            # Oracle 11g dialect configuration
│   │   │   ├── JpaAuditingConfig.java          # JPA auditing setup
│   │   │   └── WebConfig.java                  # Web MVC configuration
│   │   ├── domain/
│   │   │   ├── entity/
│   │   │   │   ├── AlCatalogTwostep.java       # Conversion entity (CRUD)
│   │   │   │   ├── AlCatalogTwostepId.java     # Composite key
│   │   │   │   ├── RvCatalogos.java            # Legacy catalog (read-only)
│   │   │   │   └── RvRproCatalogo.java         # RPRO catalog (read-only)
│   │   │   ├── repository/
│   │   │   │   ├── AlCatalogTwostepRepository.java
│   │   │   │   ├── RvCatalogosRepository.java
│   │   │   │   └── RvRproCatalogoRepository.java
│   │   │   └── model/
│   │   │       └── CatalogSource.java          # Enum (LEGACY, RPRO)
│   │   ├── service/
│   │   │   ├── CatalogService.java             # Catalog business logic
│   │   │   ├── ConversionService.java          # Conversion CRUD operations
│   │   │   └── dto/
│   │   │       ├── CatalogFilterDTO.java       # Filter criteria
│   │   │       ├── CatalogItemDTO.java         # Unified catalog view
│   │   │       └── ConversionDTO.java          # Conversion data transfer
│   │   ├── web/
│   │   │   └── controller/
│   │   │       ├── CatalogController.java      # Catalog endpoints
│   │   │       └── ConversionController.java   # Conversion endpoints
│   │   └── security/
│   │       └── AuditorAwareImpl.java           # Audit user provider
│   └── resources/
│       ├── application.properties              # Application configuration
│       └── templates/                          # Thymeleaf templates
│           ├── layout/
│           │   └── main.html                   # Base layout
│           ├── catalog/
│           │   └── list.html                   # Catalog list view
│           └── conversion/
│               ├── form.html                   # Create/Edit form
│               └── detail.html                 # Conversion details
└── test/
```

## Key Technical Decisions

### 1. Oracle 11g Compatibility

The application uses a custom `Oracle11gDialect` that extends Hibernate's `OracleDialect` with explicit version 11.2:

```java
public static class Oracle11gDialect extends OracleDialect {
    public Oracle11gDialect() {
        super(DatabaseVersion.make(11, 2));
    }
}
```

This ensures Hibernate generates ROWNUM-based pagination instead of FETCH FIRST syntax (which is only available in Oracle 12c+). Additionally, all `existsById()` calls are replaced with `findById().isPresent()` to avoid incompatible SQL generation.

### 2. Composite Primary Key

The conversion table uses a composite key consisting of four fields:
- MODULO (Module)
- CAMPO (Field)
- VALOR (Value)
- CADENA (Chain/SBS_NO)

This is implemented using JPA's `@EmbeddedId` pattern with `AlCatalogTwostepId` class, matching the business domain natural key rather than using a surrogate key.

### 3. Read-Only Catalog Entities

Both `RvCatalogos` and `RvRproCatalogo` entities are marked with `@Immutable` annotation, preventing accidental modifications and enabling Hibernate optimizations for read-only queries.

### 4. JPA Auditing

Automatic audit trail using Spring Data JPA annotations:
- `@CreatedDate` / `@CreatedBy`: Set on entity creation
- `@LastModifiedDate` / `@LastModifiedBy`: Updated on modification
- Current user: "SYSTEM" (can be integrated with Spring Security for real users)

### 5. Filter State Preservation

When navigating between catalog list and conversion views, all filter parameters are preserved via URL parameters (`returnModulo`, `returnCampo`, `returnSbsNo`, `returnHasConversion`). This provides a seamless user experience without losing filter context.

### 6. Auto-Submit Filters

JavaScript event listeners on all filter dropdowns enable instant form submission without a manual "Apply" button, providing immediate visual feedback.

### 7. Dynamic Campo Filtering

The Campo (Field) dropdown dynamically updates based on the selected Modulo using a JavaScript map (`moduloCamposMap`) populated from the backend, improving usability by showing only relevant options.

## Database Schema

### 1. RV_CATALOGOS (Legacy Catalog - Read Only)

| Column | Type | Description |
|--------|------|-------------|
| P_ID | NUMBER | Primary key |
| SBS_NO | NUMBER | Chain identifier (1=GNC, 2=Arca) |
| MODULO | VARCHAR2(50) | Module name |
| CAMPO | VARCHAR2(50) | Field name |
| VALOR | VARCHAR2(50) | Value code |
| DESCRIPCION | VARCHAR2(200) | Description |
| ACTIVO | NUMBER(1) | Active flag (1=active, 0=inactive) |
| ORDEN | NUMBER | Display order |
| CREATED_AT | TIMESTAMP | Creation timestamp |
| CREATED_BY | VARCHAR2(50) | Created by user |
| MODIFIED_AT | TIMESTAMP | Last modification timestamp |
| MODIFIED_BY | VARCHAR2(50) | Modified by user |

### 2. RV_RPRO_CATALOGO (RPRO Catalog - Read Only)

| Column | Type | Description |
|--------|------|-------------|
| RPRO_SID | NUMBER | Primary key |
| SBS_NO | NUMBER | Chain identifier (1=GNC, 2=Arca) |
| MODULO | VARCHAR2(50) | Module name |
| CAMPO | VARCHAR2(50) | Field name |
| VALOR | VARCHAR2(50) | Value code |
| DESCRIPCION | VARCHAR2(200) | Description |
| ACTIVO | NUMBER(1) | Active flag (1=active, 0=inactive) |
| ORDEN | NUMBER | Display order |
| PADRE_SID | NUMBER | Parent catalog ID |
| CREATED_AT | TIMESTAMP | Creation timestamp |
| CREATED_BY | VARCHAR2(50) | Created by user |
| MODIFIED_AT | TIMESTAMP | Last modification timestamp |
| MODIFIED_BY | VARCHAR2(50) | Modified by user |

### 3. AL_CATALOG_TWOSTEP (Conversions - Full CRUD)

| Column | Type | PK | Description |
|--------|------|-------|-------------|
| MODULO | VARCHAR2(50) | ✓ | Module name (part of composite key) |
| CAMPO | VARCHAR2(50) | ✓ | Field name (part of composite key) |
| VALOR | VARCHAR2(50) | ✓ | Value code (part of composite key) |
| CADENA | NUMBER | ✓ | Chain identifier (part of composite key) |
| DOMAIN | VARCHAR2(20) | | Domain value for conversion |
| STATUS | NUMBER | | Status (1=active, 0=inactive) |
| CREATED_AT | TIMESTAMP | | Creation timestamp (auto-filled) |
| CREATED_BY | VARCHAR2(50) | | Created by user (auto-filled) |
| MODIFIED_AT | TIMESTAMP | | Last modification timestamp (auto-updated) |
| MODIFIED_BY | VARCHAR2(50) | | Modified by user (auto-updated) |

### Relationship

Conversions in AL_CATALOG_TWOSTEP link to catalog items via:

```
AL_CATALOG_TWOSTEP.MODULO = RV_CATALOGOS.MODULO / RV_RPRO_CATALOGO.MODULO
AL_CATALOG_TWOSTEP.CAMPO = RV_CATALOGOS.CAMPO / RV_RPRO_CATALOGO.CAMPO
AL_CATALOG_TWOSTEP.VALOR = RV_CATALOGOS.VALOR / RV_RPRO_CATALOGO.VALOR
AL_CATALOG_TWOSTEP.CADENA = RV_CATALOGOS.SBS_NO / RV_RPRO_CATALOGO.SBS_NO
```

A catalog item can exist in either RV_CATALOGOS or RV_RPRO_CATALOGO (or both), and may have a corresponding conversion mapping in AL_CATALOG_TWOSTEP.

## Validations

The application enforces the following validations:

- **MODULO**: Required, maximum 50 characters
- **CAMPO**: Required, maximum 50 characters
- **VALOR**: Required, maximum 50 characters
- **CADENA**: Required, integer value (1=GNC, 2=Arca)
- **DOMAIN**: Optional, maximum 20 characters
- **STATUS**: Optional, integer (1=active, 0=inactive), defaults to 1
- **Duplicate Prevention**: Cannot create multiple conversions for the same catalog item
- **Catalog Validation**: Verifies catalog item exists in either RV_CATALOGOS or RV_RPRO_CATALOGO before allowing conversion creation

## Future Enhancements

- [ ] **Pagination**: Implement pagination for large datasets to improve performance
- [ ] **Export Functionality**: Export catalog and conversion data to CSV/Excel formats
- [ ] **Bulk Operations**: Mass create, update, or delete conversions
- [ ] **Spring Security Integration**: Implement real user authentication and authorization
- [ ] **REST API**: Provide REST endpoints for external system integration
- [ ] **Advanced Search**: Full-text search capabilities across catalogs
- [ ] **Dashboard**: Statistics and analytics dashboard showing conversion coverage, usage patterns
- [ ] **Business Rule Validation**: Additional domain-specific validation rules
- [ ] **Audit History**: View complete history of changes for each conversion
- [ ] **Import Functionality**: Bulk import conversions from CSV/Excel files

## Troubleshooting

### Error: ORA-00933 SQL command not properly ended

**Symptom**: Application crashes with FETCH FIRST syntax error when creating conversions.

**Cause**: Oracle 11g doesn't support FETCH FIRST syntax (only available in 12c+).

**Solution**: This is already handled by the custom `Oracle11gDialect` in `HibernateConfig.java`. If you still see this error:
1. Verify `HibernateConfig.java` exists in `src/main/java/dev/kreaker/cnc/config/`
2. Check logs for "CUSTOM ORACLE 11G DIALECT LOADED" message
3. Ensure no other dialect is configured in `application.properties`

### Error: Duplicate key AlCatalogTwostepId

**Symptom**: Error loading catalog page with duplicate key message.

**Cause**: Multiple rows in AL_CATALOG_TWOSTEP with the same composite key (MODULO + CAMPO + VALOR + CADENA).

**Solution**:

1. Check for duplicates:
```sql
SELECT MODULO, CAMPO, VALOR, CADENA, COUNT(*) as COUNT
FROM REPORTUSER.AL_CATALOG_TWOSTEP
GROUP BY MODULO, CAMPO, VALOR, CADENA
HAVING COUNT(*) > 1;
```

2. Use the cleanup script: `doxs/CHECK_AND_FIX_DUPLICATES.sql`

3. The application now handles duplicates gracefully (keeps first occurrence) and logs warnings.

### Error: Environment variables not loading

**Symptom**: Application can't connect to database, default values are used.

**Solution**:
1. Verify `spring-dotenv` dependency is in `build.gradle`:
```gradle
implementation 'me.paulschwarz:spring-dotenv:4.0.0'
```

2. Ensure `.env` file exists in project root
3. Check `.env` file has correct format (no spaces around `=`)
4. Restart application after modifying `.env`

### Error: Connection to Oracle failed

**Checklist**:
1. Verify host and port are correct
2. Ensure service name (not SID) is correct
3. Confirm user credentials and permissions
4. Test connection with SQL Developer or sqlplus
5. Check firewall rules allow connection to Oracle port

### Error: Entity not found / Table does not exist

**Solution**:
1. Verify schema in `application.properties`:
```properties
spring.jpa.properties.hibernate.default_schema=REPORTUSER
```

2. Ensure user has SELECT/INSERT/UPDATE/DELETE permissions on all tables
3. Confirm tables exist in REPORTUSER schema

### Status not saving on create

**Symptom**: Status field is not saved when creating a new conversion.

**Cause**: JavaScript initialization issue with checkbox.

**Solution**: This is already fixed. Verify:
1. JavaScript `updateStatusValue()` function exists in `conversion/form.html`
2. DOMContentLoaded event listener is present
3. Check browser console for JavaScript errors

## License

Internal project - All rights reserved

## Support

For issues or feature requests:
1. Review this documentation first
2. Check application logs for detailed error messages
3. Consult the troubleshooting section
4. Contact the development team

---

**Version**: 1.0.0
**Last Updated**: 2026-01-13
**Technologies**: Spring Boot 4.0.1, Java 25, Hibernate 7.2, Oracle 11g, Thymeleaf, Bootstrap 5
