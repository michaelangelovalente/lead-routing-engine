package com.casavo.leadrouting.leadrouting.port.out;

import com.casavo.leadrouting.leadrouting.domain.model.Agent;
import com.casavo.leadrouting.leadrouting.domain.model.AgentId;
import com.casavo.leadrouting.leadrouting.domain.model.City;

import java.util.List;

public interface AgentRepository {
    boolean existsById(AgentId id);
    List<Agent> findActiveInCityForUpdate(City city);
}
