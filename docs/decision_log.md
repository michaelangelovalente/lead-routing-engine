
# System Decision Log:
- 1. Requirements 
    - Functional extracted from description
    - Non-functional

- 2. Core Entities
    - Defined "Ubiq. Lang" to extract business domain see @/lead-routing-engine/docs/business_domain/terms.md
    - Defined Core entities, value objects, and constants/enums from business terms. (Modified throughout step 3, 4, 5 while working on project)
      Entities
        - Lead
        - Agent
        - Assignment
      Value Objects
        - LeadId(UUID)
        - AgentId(UUID)
        - City(String)
        - CustomerContanct(String, String, String)
        - IdempotencyKey(String)
      Enum:
        - LeadStatus{NEW, ASSIGNED}
The relationship: 
        - Lead produces at most 1 assignment
        - This either gets ASSIGNED to an agent (status=ASSIGNED) or is saved with status=NEW
```
CustomerContact -- Lead ----- Assignment --- Agent
                      |               |
                   NEW/ASSIGNED    timestamp
```
- 3. APIs
  POST /v1/leads  (resp 201 or 400, 422--> no valid agent)
```
  header: <Idempotency-Key: 01932f4c-7d8e-7a3b-b2c5-8f9a1e2d3c4b>
```
```json
      {
        "customer": {
        "name": "Mario Rossi",
        "email": "mario.rossi@example.com",
        "phone": "+393331234567"
        },
        "propertyReference": "PROP-MIL-2891",
        "city": "Milano"
        }
```
  GET /v1/agents/{agentId}/assignments

  GET /actuator/health (Free w/ Springboot)
  
      
---

# Decision Log
## Architecture 
DOmain framework is free and faster typically (although setting it up in spring might seem counterintuitive?)
The main reason is to allow domain extraction into independent services and facilitate scaling through the port layer, REST implementation lives in adapter

## JPA was not used (JdbcClient instead)
**Initial state of the project** The project initially used JPA. Through research some issues came out:

### The locking path is the blocker

The core invariant/immutable state requires `SELECT ... FOR UPDATE` on agents and `SELECT ... FOR UPDATE SKIP LOCKED` on the outbox.

- `SKIP LOCKED` is only available via native SQL in JPA (the abstraction is lost immediatesly)
- JPA's flush order and entity lifecycle management can cause subtle bugs: 
  - Hibernate may reorder writes, insert before lock acquisition, or flush at unexpected times within the transaction. (can cause unexpected behaviour)

### The schema 
Five tables, clear one-way relationships (Assignment references Lead and Agent by ID, never the other way). 
No complex object graphs. `JdbcClient` handles this with explicit and a limited amount of SQL statements.

### JPA would add cost with no benefit

- `@Entity` 
- To keep JPA only in the adapter layer, a parallel set of JPA entity classes mirroring the domain model would be require (duplication for persistence types and mapping code)
### Summary for JPA removal
`JdbcClient` was used
    - The schema is simple so adding JPA would just add overhead without any benefit, and in our case would add the issue above
    - The application needs fine grained contol for the locking mechanism (altough @NativeQuery(could be used but, it wouldn't be worth the overhead?)

# Outbox Relay/Transaction Outbot
*"Immagina che il sistema di notifica agli agenti (esterno) possa fallire. Come garantiamo che il lead non vada perduto?"*
Async processing Routing Service <-> RabbitMQ <-> Notifaction Service

On lead assignment 2 things can happen:
    - Save assignment to the DB
    - Notify Agentt / call notification through PORT
    2 phases if the second phase fails the lead gets lost, by appending the AssignLead event to the outbox

## Summarry
Outbox converts cross-system calls (DB <-> externa services/Notification ) into centralized db rows.
at-least-once delivery runs in the background


## Pessimistic locking on agents
*" Il sistema deve evitare che due lead diversi vengano assegnati contemporaneamente allo stesso agente se questo ha un solo "slot" disponibile"*
Conflict rates for agents here are high, Optimistic locking acts on the assumption that conflicts are rare.
They aren't in this system, the probability of having multiple leads from the same city (e.g. MILAN) is high
Optimistic locking would do extra work (ROLLBACK) without the blocking mechanism.

Pesimistic Llocking if we had agent A and Tx1 and Tx2 leads, Tx1 would hold the transaction, complete it, set the counter to 5 and commit.
Tx2 would load the up-to-date counter and conclude that it cannot increment it.


# Idempotency Key




---
# What could've been added 
- OpenApi spec first contracts
- Scalability: Redis/cache optimization (cities and status leads are rarely modified).
  - This adds complexity through invalidation logic

