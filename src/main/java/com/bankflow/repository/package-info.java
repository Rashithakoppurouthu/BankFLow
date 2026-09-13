/**
 * Repository Layer (Data Access Layer):
 * 
 * - Bridges the application and the underlying relational database (MySQL).
 * - Leverages Spring Data JPA to provide boilerplate-free CRUD, pagination, and sorting.
 * - Supports custom JPQL queries, native SQL queries, and explicit database locking (@Lock).
 * 
 * Key Interview Concepts:
 * - @Repository translates vendor-specific SQL exceptions into Spring's unified DataAccessException hierarchy.
 * - JpaRepository extends PagingAndSortingRepository and CrudRepository.
 * - Method name queries (e.g. findByEmail, existsByAccountNumber) are parsed automatically by Spring Data JPA at runtime.
 */
package com.bankflow.repository;
