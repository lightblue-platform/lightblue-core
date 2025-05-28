package com.redhat.lightblue.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.Path;

public class MaskedSetExpression extends SetExpression {
    private static final long serialVersionUID = 1L;
    private List<FieldProjection> maskFields;

    public MaskedSetExpression(UpdateOperator op, List<FieldAndRValue> list, List<FieldProjection> maskFields) {
        super(op, list);
        this.maskFields = maskFields;
    }

    public List<FieldProjection> getMaskFields() {
        return maskFields;
    }

    @Override
    public JsonNode toJson() {
        ArrayNode node = getFactory().arrayNode();
        for (FieldProjection x : maskFields) {
            node.add(x.toJson());
        }

        ObjectNode objectNode = getFactory().objectNode();
        JsonNode setJson = super.toJson();
        if (setJson.has(UpdateOperator._set.toString())) {
            objectNode.set(UpdateOperator._set.toString(), setJson.get(UpdateOperator._set.toString()));
        } else if (setJson.has(UpdateOperator._add.toString())) {
            objectNode.set(UpdateOperator._add.toString(), setJson.get(UpdateOperator._add.toString()));
        }
        objectNode.set("fields", node);
        return objectNode;
    }

    public static MaskedSetExpression fromJson(ObjectNode node) {
        ObjectNode setNode = getFactory().objectNode();
        if (node.has(UpdateOperator._set.toString())) {
            setNode.set(UpdateOperator._set.toString(), node.get(UpdateOperator._set.toString()));
        } else if (node.has(UpdateOperator._add.toString())) {
            setNode.set(UpdateOperator._add.toString(), node.get(UpdateOperator._add.toString()));
        }
        SetExpression fromJson = SetExpression.fromJson(setNode);
        List<FieldProjection> mf = new ArrayList<FieldProjection>();
        Iterator<JsonNode> nodeIt = node.get("fields").elements();
        while (nodeIt.hasNext()) {
            JsonNode n = nodeIt.next();
            mf.add((FieldProjection) FieldProjection.fromJson(n));
        }
        return new MaskedSetExpression(fromJson.getOp(), fromJson.getFields(), mf);
    }
}
