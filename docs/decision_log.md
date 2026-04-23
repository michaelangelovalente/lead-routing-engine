
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
    
- 3. APIs
  POST /v1/leads  (resp 201 or 400, 422--> no valid agent)
  header: <Idempotency-Key: 01932f4c-7d8e-7a3b-b2c5-8f9a1e2d3c4b>
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
  GET /actuator/health (Free w/ Springboot)
  
  GET /v1/agents/{agentId}/assignments
  GET /actuator/prometheus (? Check if free w/ Springboot, otherwise discard)
      
---

# Decision Log
