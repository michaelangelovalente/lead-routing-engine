
# System Decision Log:
- 1. Requirements 
    - Functional extracted from description
    - Non-functional @/lead-routing-engine/docs/system_requirments_v1.md

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
```json

POST /v1/leads  (resp 201 or 400, 422--> no valid agent)
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

"Salva **l'assegnazione su database per mantenere un audit trail.**"<br>
*"Monitoraggio e Osservabilità (come capiremmo se un agente sta ricevendo troppi lead?)"* <br>

Assignments endpoint: avoids having to directly query the DB. This can be used by a monitoring tool
```
  GET /v1/agents/{agentId}/assignments
```

Checks if
```
  GET /actuator/health (Free w/ Springboot)
```
  
      
---

# Decision Log
## Architecture 
Domain framework is free - no Spring imports, less complexity and overhead and no dependencies on spring.
This makes is easier to test (domain logic is bounded).
Clean port boundaries -> easier to swap adapters and straightforward extraction.
REST implementation lives in adapter

Example:
1.  [InMemoryNotificationClient.java](../src/main/java/com/casavo/leadrouting/leadrouting/adapter/out/notification/InMemoryNotificationClient.java) --> NotificationService
- OutboxRelay polls outbox_event
  - calls /port/out/NotificationPort.java

- Notification MS
  - Consumer listens on RMQ
    - delivers email
    
2. [AgentAssignmentsController.java](../src/main/java/com/casavo/leadrouting/leadrouting/adapter/in/rest/AgentAssignmentsController.java) + [ListAssignmentsUseCase.java](../src/main/java/com/casavo/leadrouting/leadrouting/application/query/ListAssignmentsUseCase.java) --> Query/Read Service<br>
   (Read only service `AssignmentService`: allows for easier scaling since this can be optimized for READ heavy queries, a read only replica DB could be added easily)
    - LeadRoutingService writes POST /v1/leads 
      - Writes to Primary DB
    
    - AssignmentManagement MS GET /v1/agents/{id}/assignments
      - Read from replica


## Scalability
"Una breve spiegazione di come renderesti questo sistema pronto per gestire un traffico di migliaia di lead al secondo (es. durante una campagna marketing massiva)."
 

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
`JdbcClient` was used:
- The schema is simple so adding JPA would just add overhead without any benefit, and in our case would add the issue above
- The application needs fine grained contol for the locking mechanism (altough @NativeQuery(could be used but, it wouldn't be worth the overhead?)

# Outbox Relay/Transaction Outbot
*"Immagina che il sistema di notifica agli agenti (esterno) possa fallire. Come garantiamo che il lead non vada perduto?"* <br>
Async processing Routing Service <-> RabbitMQ <-> Notifaction Service

On lead assignment 2 things can happen:
- Save assignment to the DB
- Notify Agentt / call notification through PORT <br>
    2 phases if the second phase fails the lead gets lost, by appending the AssignLead event to the outbox that the assignment does not get lost.

## Summarry
Outbox converts cross-system calls (DB <-> external services/Notification ) into centralized db rows. <br>
At-least-once delivery runs in the background


## Pessimistic locking on agents
*" Il sistema deve evitare che due lead diversi vengano assegnati contemporaneamente allo stesso agente se questo ha un solo "slot" disponibile"*<br>
Conflict rates for agents here are high, Optimistic locking acts on the assumption that conflicts are rare.<br>
They aren't in this system, the probability of having multiple leads from the same city (e.g. MILAN) is high<br>
Optimistic locking would do extra work (ROLLBACK) without the blocking mechanism.<br>

Pesimistic Llocking if we had agent A and Tx1 and Tx2 leads, Tx1 would hold the transaction, complete it, set the counter to 5 and commit. <br>
Tx2 would load the up-to-date counter and conclude that it cannot increment it.<br>


# Idempotency Key
USed by client, if for some reason theres a network failure <br>
while the request is being received, client can retry with the same `lead.idempotency_key`. <br>
This avoids duplicate assignments on our system

Example: <br>
 A retry 1 BEGIN findIdemPotKey("abc") --> not seen --> build lead, add agent, insert lead(key=abc), insert assignment COMMIT<br>
(B findIdemPotKey("abc") happens while A is still processing)<br>
 B retry 2 BEGIN findIdemPotKey("abc") --> not seen --> build lead, add agent, insert lead(key=abc) --> UNIQUE CONSTRAINT VIOLATION --> ROLLBACK <br>


# Routing Algorithm
*"Se più agenti sono idonei, scegli quello con il carico minore o usa una logica Round Robin." ("If multiple agents are eligible, choose the one with the lowest load or use a Round Robin logic.")*

## Round Robin has multiple issues:
 - Tracking sequence A -> B -> C -> D
   - If for some reason an instance that is tracking the sequence goes down where does it restart?
     - We would need to persist it somehow (DB)
 - Agent Removal/Deactivation
   - Admin removes Agent (retired, got fired etcc...)
     - If deactivation happens mid cylce (the system might handle thousand of agents) if any of these are removed we wouldn't know how where to continue
 - Horizontal Scaling
   - 2 instances of the system need a shared state
     - this can either be persisted or we would need another way to handle sequence distribution
## Lowest Load
The decision was mainly made through elimination at first. <br>
The reasoning on why it's a better decision came after; it sums up to simplicity (3 liner code) vs Sequence Distribution Algorithm + DB persistence + multi instance alignment
```java
int minLoad = candidates.stream().mapToInt(AgentWithLoad::currentLoad).min().orElseThrow();
List<AgentWithLoad> tied = candidates.stream().filter(c -> c.currentLoad() == minLoad).toList();
return Optional.of(tied.get(tiebreaker.nextInt(tied.size())));
```

---
# What could've been added 
- OpenApi spec first contracts
- Scalability: Redis/cache optimization (cities and status leads are rarely modified).
  - Adds complexity through invalidation / alignment logic
---

