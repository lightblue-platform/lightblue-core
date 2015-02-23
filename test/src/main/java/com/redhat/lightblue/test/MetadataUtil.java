/*
 Copyright 2015 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.metadata.EntityConstraint;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.FieldConstraintParser;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.test.metadata.parser.FakeDataStoreParser;

public final class MetadataUtil {

    /**
     * Creates an instance of {@link JSONMetadataParser}.
     * @param backend - Name of backend to use.
     * @param fieldConstraintParsers - <i>(optional)</i> {@link FieldConstraintParser}s to register on the {@link Extensions} used to parse
     * the {@link EntityMetadata}. If <code>null</code> then nothing will be set.
     * @return An instance of {@link JSONMetadataParser}.
     */
    public static JSONMetadataParser createJSONMetadataParser(
            String backend,
            Map<String, ? extends FieldConstraintParser<JsonNode>> fieldConstraintParsers){
        FakeDataStoreParser<JsonNode> dsParser = new FakeDataStoreParser<JsonNode>(backend);

        Extensions<JsonNode> extensions = new Extensions<JsonNode>();
        extensions.registerDataStoreParser(dsParser.getDefaultName(), dsParser);
        extensions.addDefaultExtensions();
        if (fieldConstraintParsers != null) {
            for (Entry<String, ? extends FieldConstraintParser<JsonNode>> checker : fieldConstraintParsers.entrySet()) {
                extensions.registerFieldConstraintParser(checker.getKey(), checker.getValue());
            }
        }

        return new JSONMetadataParser(
                extensions,
                new DefaultTypes(),
                JsonNodeFactory.withExactBigDecimals(false));
    }

    /**
     * Creates an instance of {@link EntityMetadata} for testing purposes.
     * @param backend - Name of backend to use.
     * @param node - Json node to parse metadata from.
     * @param entityConstraints - <i>(optional)</i> {@link EntityConstraint}s to set on the {@link EntityMetadata}.
     * If <code>null</code> then nothing will be set.
     * @param fieldConstraintParsers - <i>(optional)</i> {@link FieldConstraintParser}s to register on the {@link Extensions} used to parse
     * the {@link EntityMetadata}. If <code>null</code> then nothing will be set.
     * @return An instance of {@link EntityMetadata} for testing purposes.
     */
    public static EntityMetadata createEntityMetadata(
            String backend,
            JsonNode node,
            List<? extends EntityConstraint> entityConstraints,
            Map<String, ? extends FieldConstraintParser<JsonNode>> fieldConstraintParsers) {

        JSONMetadataParser jsonParser = createJSONMetadataParser(backend, fieldConstraintParsers);

        EntityMetadata entityMetadata = jsonParser.parseEntityMetadata(node);
        if ((entityConstraints != null) && !entityConstraints.isEmpty()) {
            entityMetadata.setConstraints(new ArrayList<EntityConstraint>(entityConstraints));
        }

        return entityMetadata;
    }

    private MetadataUtil(){}

}
