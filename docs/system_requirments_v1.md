# Casavo Lead Routing Engine: Core Requirements

## 1. Functional Requirements

* **Lead Ingestion:** * Expose an idempotent `POST /v1/leads` API accepting customer details, property ID, and city.
    * Return strict RFC 7807 Problem Details for all errors.
* **Routing Rules:** Assign the lead to exactly one agent who:
    * Operates in the same **city**.
    * Has available capacity (**<= 5 assignments** in the last 24 hours).
    * *Tiebreaker:* Choose the agent with the lowest current load.
    * *Fallback:* Return a `422 Unprocessable Entity` if no agent is eligible.
* **Audit Trail:** Persist every assignment decision immutably to PostgreSQL.

## 2. Technical Constraints & Non-Functional Requirements

* **Strict Concurrency:** The system must prevent two concurrent leads from being assigned to the same agent if only one capacity slot remains.
* **Guaranteed Delivery:** Lead assignments must never be lost, even if the external notification system fails. (Requires an asynchronous, replay --> outbox).
* **Performance & Scale:** Target design for horizontal scalability to handle traffic bursts
* **Operability:** Must be fully runnable locally via a single `docker-compose` command ~~using Flyway for schema migrations~~.

## 3. Out of Scope
* **Authentication:** Assumed to be handled upstream by an API Gateway.