# Bounded Context Glossary

| Term | Meaning                                                                                                                                                                              |
| :--- |:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Lead** | A potential customer who has expressed interest in a specific property through one of our portals. Each lead represents one inquiry.                                                 |
| **Agent** | A real estate agent who handles customers in a specific city. Agents have a limit on how many new leads they can take on at once.                                                    |
| **Assignment** | The moment a lead is handed to a specific agent. Once recorded, this connection is permanent — we keep it as part of our history.                                                    |
| **Capacity** | The limit on how many leads an agent can take on at a time. No more than 5 new leads in the last 24 hours. Keeps agents from getting overwhelmed.                                    |
| **City** | The geographic area a lead and an agent belong to. Leads are only routed to agents in the same city. Different spellings ("Milano", "milano", "MILAN") are treated as the same city. |
| **Eligibility** | Whether a specific agent is a valid choice for a specific lead. An agent is eligible if they're active, serve the lead's city, and haven't hit their capacity limit.                 |
| **Routing decision** | The automatic choice of which eligible agent gets a given lead. When multiple agents are eligible, the one with the lightest current workload wins.                                  |
| **Customer contact** | The customer's name, email, and phone number — the information an agent needs to reach out.                                                                                          |
| **Notification** | The alert sent to an agent when they've been assigned a new lead. Handled by a separate system, but this service is responsible for making sure the hand-off happens reliably.       |
