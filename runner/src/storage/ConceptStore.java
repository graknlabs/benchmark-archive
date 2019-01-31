/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2018 Grakn Labs Ltd
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.runner.storage;

import grakn.core.concept.Concept;

/**
 *
 */
public interface ConceptStore {

    void addConcept(Concept concept);
    void addRolePlayer(String conceptId, String conceptType, String relationshipType, String role);

    int totalExplicitRelationships();
    int totalEntities();
    int totalAttributes();

    int totalRolePlayers();
    int totalExplicitRolePlayers();

    int totalOrphanEntities();
    int totalOrphanAttributes();
    int totalRelationshipsRolePlayersOverlap();
}
