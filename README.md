# Spring Coffees App
This is an exemplary project which makes use of Spring Boot starters to create a handy web-application with perspective to serve for needs of a small coffees shop. In addition to keeping records of Suppliers, Coffees, Sales and Users tables, it also shows some Statistics as to who are the best sellers among them for a chosen period. 

### Demo
To get a quick idea of how it works, open the following link https://spring-coffees.alwaysdata.net in your browser. In demo mode, this app has already been pre-populated with some data, so that you can play around with the tables, while adding or updating Suppliers, selling or buying Coffees, registering new Users, changing their authorities from NEWCOMER to MANAGER, etc... At any point, you can click "Restart" button on the right top corner to discard all changes and re-populate the tables with demonstration data. 

For authentication, please use `admin` username with the default `admin` password, or some other username from Users table (such as `alex` or `jane` with `12345678` password). The difference between admin and other users is in authorities level: users who don't  have administrative authorities are restricted in access to Users table and can't change their status.

### Build
To build and run the project locally with production profile, first make sure you have [Java 11] or higher installed on your machine as well as [Apache Maven] command tool at hand. Also, because this app relies on [PostgreSQL] as the database management system, you need to have a running instance of it with a dedicated database that the application can use to store its data. Finally, you'd better have your personal SMTP mailing service available, so that confirmation emails (involved in new users registration or resetting existing users passwords) can be sent by the application on behalf of your mailing account. With all preparations done, now simply follow these steps:  
1. Clone or download and unzip the contents of this repository to your local directory.
2. Jump to `spring-coffees-app\src\main\resources` and make some changes to the properties files (first, replace *Mail Server* settings with your host, port, username and password details in `application.properties`, then provide your own *Data Source* url, username and password in `application-prod.properties`. 
3. Return to the main project directory where the parent `pom.xml` file is located, and using your favorite command line shell (such as Bash or whatever), execute these instructions, one by one: 
```
$ mvn clean package
$ java -jar spring-coffees-app/target/*.jar --spring.profiles.active=prod
```
4. After application has been successfully built and started, open your browser and enter the following address in there http://localhost:8080/.
5. Unlike in *demo* mode, running application with *prod* profile doesn't pre-populate tables with data, except for Users table where the only default `admin` user with `admin` password is created, so that you may use those credentials for your first authentication. As soon as you've been logged in, it's recommended to change admin's email for your own and then use it to reset admin's password. Note: many more users with administrative authorities can be added later, but `amdin` user is the default one and therefore can't be deleted from the Users table.

### Modules 
Here is a short description of the application modules with brief enumeration of [Spring Framework] (and some other related libraries) interfaces, classes and annotations used in those modules: 

- **spring-coffees-app**

Serving as a starting point for a Spring Boot application, this module holds a class (annotated with homonymous annotation) that includes the `main()` method. Also, it's where application properties are defined as resources. Uses: [@SpringBootApplication].  

- **spring-coffees-domain**

Contains domain classes representing POJO objects with fields matching table column names, so that [RowMapper] implementations then can be used to translate results of the database queries into these objects. Uses: [@Data], [@AllArgsConstructor], [PageImpl], [Pageable].

- **spring-coffees-jdbc**

Includes a set of domain-related repositories based on Spring Data JDBC core classes and various callback interfaces which make possible execution of SQL queries and [ResultSet] processing. Also, includes a [FlyWay] library with plain SQL `schema` and `data` scripts defined as resources to help migrate the database on application startup. Notice that two different RDBMS are chosen between depending on the application's active profile: an in-memory [H2] - for *demo* and aforementioned PostgreSQL - for *prod*. Uses: [JdbcTemplate], [SimpleJdbcInsert], [NamedParameterJdbcTemplate], [MapSqlParameterSource], [RowMapper], [ResultSetExtractor], [RowCallbackHandler], [DataIntegrityViolationException], [DuplicateKeyException], [EmptyResultDataAccessException], [Pageable], [Sort], [@Repository], [@Transactional]. 

- **spring-coffees-security**

Apart from configuring users authentication and authorization against information kept in the database via JDBC and setting permission rules for securing HTTP-requests, this module also provides essential web-interface for new users registration, login/logout as well as tokenized 'reset password' endpoints while enabling mechanism preventing cross-site request forgery (CSRF). Uses: [@Configuration], [@EnableWebSecurity], [WebSecurityConfigurerAdapter], [AuthenticationManagerBuilder], [HttpSecurity], [PasswordEncoder], [BCryptPasswordEncoder], [HttpServletRequest], [HttpServletResponse], [HttpSession], [Authentication], [SessionRegistry], [SessionRegistryImpl], [AuthenticationFailureHandler], [AuthenticationException], [BadCredentialsException], [DisabledException], [WebMvcConfigurer], [ViewControllerRegistry], [@Controller], [@RequestMapping], [@GetMapping], [@PostMapping], [Model], [@ModelAttribute], [@Validated], [@Valid], [@NotBlank], [@Email], [@Pattern], [@Size], [AbstractBindingResult], [FieldError], [@Data], [@Value], [SimpleMailMessage], [MimeMessage], [MimeMessageHelper], [JavaMailSender]. 

- **spring-coffees-web**

This module exploits a classic MVC pattern based on Spring Web and [Thymeleaf] boot-starter dependencies, to provide a convenient web-interface for data views and editing along with an appropriate form and request parameters validation. Uses: [WebMvcConfigurer], [ViewControllerRegistry], [@Controller], [@RequestMapping], [@GetMapping], [@PostMapping], [Model], [ModelAndView], [@ModelAttribute], [@SessionAttributes], [@RequestParam], [HttpServletRequest], [SessionRegistry], [SessionInformation], [UserDetails], [BindingResult], [@InitBinder], [WebDataBinder], [PropertyEditorSupport], [Formatter], [Converter], [@DateTimeFormat], [@Validated], [@Valid], [@NotNull], [@NotBlank], [@Min], [@Max], [@DecimalMin], [@Digits], [@Range], [@Email], [@Pattern], [@ConfigurationProperties], [PageRequest], [Pageable], [Sort], [@ControllerAdvice], [@ExceptionHandler], [@Data], [@NoArgsConstructor], [MessageSource], [ReloadableResourceBundleMessageSource].

[//]: # (These are reference links used in the body of this document:)

   [Heroku Cloud Platform]: <https://www.heroku.com/free>
   [Apache Maven]: <https://maven.apache.org/download.cgi>
   [Java 11]: <https://www.oracle.com/java/technologies/javase-jdk11-downloads.html>
   [PostgreSQL]: <https://www.postgresql.org/download/>
   [Spring Framework]: <https://spring.io/>
   [@SpringBootApplication]:  <https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/autoconfigure/SpringBootApplication.html>
   [@Data]: <https://projectlombok.org/features/Data>
   [@AllArgsConstructor]: <https://projectlombok.org/features/constructor>
   [@NoArgsConstructor]: <https://projectlombok.org/features/constructor>
   [PageImpl]: <https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/domain/PageImpl.html>
   [Pageable]: <https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/domain/Pageable.html>
   [RowMapper]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/RowMapper.html>
   [ResultSet]: <https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/java/sql/ResultSet.html>
   [FlyWay]: <https://flywaydb.org/>
   [H2]: <https://h2database.com/html/main.html>
   [JdbcTemplate]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/JdbcTemplate.html>
   [SimpleJdbcInsert]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/simple/SimpleJdbcInsert.html>
   [NamedParameterJdbcTemplate]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/namedparam/NamedParameterJdbcTemplate.html>
   [MapSqlParameterSource]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/namedparam/MapSqlParameterSource.html>   
   [ResultSetExtractor]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/ResultSetExtractor.html>   
   [RowCallbackHandler]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/RowCallbackHandler.html>   
   [DataIntegrityViolationException]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/dao/DataIntegrityViolationException.html>
   [DuplicateKeyException]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/dao/DuplicateKeyException.html>
   [EmptyResultDataAccessException]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/dao/EmptyResultDataAccessException.html>
   [Sort]: <https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/domain/Sort.html>
   [@Repository]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/stereotype/Repository.html>
   [@Transactional]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/transaction/annotation/Transactional.html>
   [@Configuration]: <https://docs.spring.io/spring-framework/docs/5.2.8.RELEASE/javadoc-api/org/springframework/context/annotation/Configuration.html>
   [@EnableWebSecurity]: <https://docs.spring.io/spring-security/site/docs/5.4.0-RC1/api/org/springframework/security/config/annotation/web/configuration/EnableWebSecurity.html>
   [WebSecurityConfigurerAdapter]: <https://docs.spring.io/spring-security/site/docs/5.4.0-RC1/api/org/springframework/security/config/annotation/web/configuration/WebSecurityConfigurerAdapter.html>
   [AuthenticationManagerBuilder]: <https://docs.spring.io/spring-security/site/docs/5.4.0-RC1/api/org/springframework/security/config/annotation/authentication/builders/AuthenticationManagerBuilder.html>
   [HttpSecurity]: <https://docs.spring.io/spring-security/site/docs/5.4.0-RC1/api/org/springframework/security/config/annotation/web/builders/HttpSecurity.html>
   [PasswordEncoder]: <https://docs.spring.io/spring-security/site/docs/5.4.0-RC1/api/org/springframework/security/crypto/password/PasswordEncoder.html>
   [BCryptPasswordEncoder]: <https://docs.spring.io/spring-security/site/docs/5.4.0-RC1/api/org/springframework/security/crypto/bcrypt/BCryptPasswordEncoder.html>
   [HttpServletRequest]: <https://tomcat.apache.org/tomcat-9.0-doc/servletapi/javax/servlet/http/HttpServletRequest.html>
   [HttpServletResponse]: <https://tomcat.apache.org/tomcat-9.0-doc/servletapi/javax/servlet/http/HttpServletResponse.html>
   [HttpSession]: <https://tomcat.apache.org/tomcat-9.0-doc/servletapi/javax/servlet/http/HttpSession.html>
   [Authentication]: <https://docs.spring.io/spring-security/site/docs/5.4.0-RC1/api/org/springframework/security/core/Authentication.html>
   [SessionRegistry]: <https://docs.spring.io/spring-security/site/docs/5.4.0-RC1/api/org/springframework/security/core/session/SessionRegistry.html>
   [SessionRegistryImpl]: <https://docs.spring.io/spring-security/site/docs/5.4.0-RC1/api/org/springframework/security/core/session/SessionRegistryImpl.html>
   [AuthenticationFailureHandler]: <https://docs.spring.io/spring-security/site/docs/5.4.0-RC1/api/org/springframework/security/web/authentication/AuthenticationFailureHandler.html>
   [AuthenticationException]: <https://docs.spring.io/spring-security/site/docs/5.4.0-RC1/api/org/springframework/security/core/AuthenticationException.html>
   [BadCredentialsException]: <https://docs.spring.io/spring-security/site/docs/5.4.0-RC1/api/org/springframework/security/authentication/BadCredentialsException.html>
   [DisabledException]: <https://docs.spring.io/spring-security/site/docs/5.4.0-RC1/api/org/springframework/security/authentication/DisabledException.html>
   [WebMvcConfigurer]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/config/annotation/WebMvcConfigurer.html>
   [ViewControllerRegistry]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/config/annotation/ViewControllerRegistry.html>
   [@Controller]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/stereotype/Controller.html>
   [@RequestMapping]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestMapping.html>
   [@GetMapping]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/bind/annotation/GetMapping.html>
   [@PostMapping]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/bind/annotation/PostMapping.html>
   [Model]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/ui/Model.html>
   [@ModelAttribute]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/bind/annotation/ModelAttribute.html>
   [@Valid]: <https://docs.jboss.org/hibernate/beanvalidation/spec/2.0/api/javax/validation/Valid.html>
   [@Validated]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/validation/annotation/Validated.html>
   [@NotBlank]: <https://docs.jboss.org/hibernate/beanvalidation/spec/2.0/api/javax/validation/constraints/NotBlank.html>
   [@Email]: <https://docs.jboss.org/hibernate/beanvalidation/spec/2.0/api/javax/validation/constraints/Email.html>
   [@Pattern]: <https://docs.jboss.org/hibernate/beanvalidation/spec/2.0/api/javax/validation/constraints/Pattern.html>
   [@Size]: <https://docs.jboss.org/hibernate/beanvalidation/spec/2.0/api/javax/validation/constraints/Size.html>
   [AbstractBindingResult]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/validation/AbstractBindingResult.html>
   [FieldError]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/validation/FieldError.html>   
   [@Value]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/beans/factory/annotation/Value.html>   
   [SimpleMailMessage]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/mail/SimpleMailMessage.html>
   [MimeMessage]: <https://jakarta.ee/specifications/mail/1.6/apidocs/javax/mail/internet/MimeMessage.html>
   [MimeMessageHelper]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/mail/javamail/MimeMessageHelper.html>
   [JavaMailSender]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/mail/javamail/JavaMailSender.html>
   [Thymeleaf]: <https://www.thymeleaf.org/>
   [ModelAndView]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/ModelAndView.html>
   [SessionInformation]: <https://docs.spring.io/spring-security/site/docs/5.4.0-RC1/api/org/springframework/security/core/session/SessionInformation.html>
   [UserDetails]: <https://docs.spring.io/spring-security/site/docs/5.4.0-RC1/api/org/springframework/security/core/userdetails/UserDetails.html>
   [@SessionAttributes]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/bind/annotation/SessionAttributes.html>
   [@RequestParam]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestParam.html>
   [BindingResult]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/validation/BindingResult.html>
   [@InitBinder]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/bind/annotation/InitBinder.html>
   [WebDataBinder]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/bind/WebDataBinder.html>
   [PropertyEditorSupport]: <https://docs.oracle.com/en/java/javase/11/docs/api/java.desktop/java/beans/PropertyEditorSupport.html>
   [Formatter]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/format/Formatter.html>
   [Converter]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/core/convert/converter/Converter.html>
   [@DateTimeFormat]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/format/annotation/DateTimeFormat.html>
   [@NotNull]: <https://docs.jboss.org/hibernate/beanvalidation/spec/2.0/api/javax/validation/constraints/NotNull.html>
   [@Min]: <https://docs.jboss.org/hibernate/beanvalidation/spec/2.0/api/javax/validation/constraints/Min.html>
   [@Max]: <https://docs.jboss.org/hibernate/beanvalidation/spec/2.0/api/javax/validation/constraints/Max.html>
   [@DecimalMin]: <https://docs.jboss.org/hibernate/beanvalidation/spec/2.0/api/javax/validation/constraints/DecimalMin.html>
   [@Digits]: <https://docs.jboss.org/hibernate/beanvalidation/spec/2.0/api/javax/validation/constraints/Digits.html>
   [@Range]: <https://docs.jboss.org/hibernate/stable/validator/api/org/hibernate/validator/constraints/Range.html>
   [@ConfigurationProperties]: <https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/context/properties/ConfigurationProperties.html>
   [PageRequest]: <https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/domain/PageRequest.html>
   [@ControllerAdvice]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/bind/annotation/ControllerAdvice.html>
   [@ExceptionHandler]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/bind/annotation/ExceptionHandler.html>
   [MessageSource]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/MessageSource.html>
   [ReloadableResourceBundleMessageSource]: <https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/support/ReloadableResourceBundleMessageSource.html>
