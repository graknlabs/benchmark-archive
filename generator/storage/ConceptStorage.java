/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package grakn.benchmark.generator.storage;

import grakn.client.concept.Concept;

import java.util.Date;
import java.util.List;

/**
 *
 */
public interface ConceptStorage {

    int getConceptCount(String typeLabel);

    Long getConceptKey(String typeLabel, int offset);

    List<Long> getKeysNotPlayingRole(String typeLabel, String relationshipType, String role);

    Integer numIdsNotPlayingRole(String typeLabel, String relationshipType, String role);

    Date getDate(String typeLabel, int offset);

    void addConcept(Concept concept, Long conceptKey);

    void addRolePlayerByKey(Long conceptKey, String conceptType, String relationshipType, String role);

    int totalExplicitRelationships();

    int totalImplicitRelationships();

    int totalEntities();

    int totalAttributes();

    int totalRolePlayers();

    int totalExplicitRolePlayers();

    int totalOrphanEntities();

    int totalOrphanAttributes();

    int totalRelationshipsRolePlayersOverlap();

    int getGraphScale();
}
