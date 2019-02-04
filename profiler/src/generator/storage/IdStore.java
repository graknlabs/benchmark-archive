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

package grakn.benchmark.profiler.generator.storage;

import grakn.core.concept.ConceptId;

import java.util.Date;
import java.util.List;

/**
 *
 */
public interface IdStore extends ConceptStore {

    int getConceptCount(String typeLabel);

    ConceptId getConceptId(String typeLabel, int offset);
    List<String> getIdsNotPlayingRole(String typeLabel, String relationshipType, String role);
    Integer numIdsNotPlayingRole(String typeLabel, String relationshipType, String role);
    String getString(String typeLabel, int offset);
    Double getDouble(String typeLabel, int offset);
    Long getLong(String typeLabel, int offset);
    Boolean getBoolean(String typeLabel, int offset);
    Date getDate(String typeLabel, int offset);
}
