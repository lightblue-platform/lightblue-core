package com.redhat.lightblue.common.rdbms;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.SimpleField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DMLConvert {

    public static void main(String[] args) throws IOException {
        final class TableField {
            String tableName;
            String fieldName;
            String nullable;
            String type;

            @Override
            public String toString() {
                return "TableField[tableName='" + tableName + "',fieldName='" + fieldName + "',nullable='" + nullable + "',type='" + type + "']";
            }
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = null;
        String[] words = null;
        ArrayList<TableField> tableFieldArrayList = new ArrayList<>();
        line = br.readLine();

        words = line.split("\\s+");
        String desc = words[0];

        if ("desc".equalsIgnoreCase(desc)) {
            String table = words[1];
            br.readLine(); //header, which is assumed to be the default 3 columns
            br.readLine(); // nothing
            line = br.readLine();
            while (line != null) {
                words = line.split("\\s+");
                if (words.length == 0 || words.length == 1) { // EoF
                    break;
                }

                TableField field = new TableField();

                field.tableName = table;
                field.fieldName = words[0];
                if ("not".equalsIgnoreCase(words[1])) {
                    field.nullable = "false";
                    field.type = words[3];
                } else {
                    field.nullable = "true";
                    field.type = words[1];
                }

                tableFieldArrayList.add(field);
                line = br.readLine();
            }
        }

        for (int i = 0; i < tableFieldArrayList.size(); i++) {
            System.out.println(tableFieldArrayList.get(i));
        }

        EntityMetadata entityMetadata = new EntityMetadata("test");

        for (int i = 0; i < tableFieldArrayList.size(); i++) {
            SimpleField f = new SimpleField(tableFieldArrayList.get(i).fieldName);

            f.getProperties().put("tableName", tableFieldArrayList.get(i).tableName);
            f.getProperties().put("fieldName", tableFieldArrayList.get(i).fieldName);
            f.getProperties().put("nullable", tableFieldArrayList.get(i).nullable);
            f.getProperties().put("type", tableFieldArrayList.get(i).type);

            entityMetadata.getEntitySchema().getFields().addNew(f);
            System.out.println(f.getProperties());
        }
    }
}
