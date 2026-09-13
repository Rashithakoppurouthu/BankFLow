/**
 * Configuration Layer:
 * 
 * - Defines Spring @Configuration classes that produce custom Spring Beans.
 * - Manages OpenAPI 3 / Swagger metadata and security scheme definitions.
 * - Configures global CORS mappings, JPA auditing providers, and application constants.
 * 
 * Key Interview Concepts:
 * - @Configuration indicates that a class declares one or more @Bean methods and may be processed by the Spring container.
 * - IoC (Inversion of Control) and DI (Dependency Injection): Spring creates and wires beans, shifting lifecycle management away from manual Java new operators.
 */
package com.bankflow.config;
