/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2019 Grakn Labs Ltd
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

package grakn.benchmark.generator.provider.concept;

import grakn.benchmark.generator.storage.ConceptStorage;
import grakn.core.concept.ConceptId;

import java.util.List;
import java.util.Random;

public class NotInRelationshipConceptIdProvider implements ConceptIdProvider {

    private String relationshipLabel;
    private String roleLabel;
    private final Random rand;
    private String typeLabel;
    ConceptStorage conceptStorage;

    public NotInRelationshipConceptIdProvider(Random rand,
                                              ConceptStorage conceptStorage,
                                              String rolePlayerTypeLabel,
                                              String relationshipLabel,
                                              String roleLabel
    ) {
        this.rand = rand;
        this.typeLabel = rolePlayerTypeLabel;
        this.relationshipLabel = relationshipLabel;
        this.roleLabel = roleLabel;
        this.conceptStorage = conceptStorage;
    }


    @Override
    public boolean hasNext() {
        return !conceptStorage.getIdsNotPlayingRole(typeLabel, relationshipLabel, roleLabel).isEmpty();
    }

    @Override
    public boolean hasNextN(int n) {
        return conceptStorage.getIdsNotPlayingRole(typeLabel, relationshipLabel, roleLabel).size() >= n;
    }

    @Override
    public ConceptId next() {
        List<ConceptId> notInRelationshipConceptIds = conceptStorage.getIdsNotPlayingRole(typeLabel, relationshipLabel, roleLabel);
        int randomOffset = rand.nextInt(notInRelationshipConceptIds.size());
        return notInRelationshipConceptIds.get(randomOffset);
    }

}
