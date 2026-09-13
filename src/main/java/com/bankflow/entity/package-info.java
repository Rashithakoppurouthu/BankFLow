/**
 * Entity Layer (Domain Models):
 * 
 * - Represents relational database tables mapped to Java classes via JPA/Hibernate.
 * - Enforces schema constraints (e.g. unique constraints, not-null, precise column definitions).
 * - Utilizes BigDecimal for monetary amounts to guarantee zero floating-point calculation drift.
 * - Implements JPA Auditing fields (createdAt, updatedAt).
 * 
 * Key Interview Concepts:
 * - @Entity: Declares class as a persistent database entity.
 * - @Table: Specifies table name, indexes, and unique constraints.
 * - Lazy vs Eager loading: FetchType.LAZY loads associated collections only when accessed (preventing N+1 problem).
 */
package com.bankflow.entity;
