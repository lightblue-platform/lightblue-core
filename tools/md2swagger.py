#!/usr/bin/python

# Convert lightblue metadata to swagger yaml
#
# This is useful in creating the first data model
# for swagger specifications from lightblue metadata.
# Run the script as:
#
#  md2swagger.py <metadata files> > model.yaml
#
#

import sys
import json
import yaml

def getTypeProperties(type):
    properties=dict()
    if type=='bigdecimal' or type=='biginteger':
        properties['type']='string'
    elif type=='boolean':
        properties['type']='boolean'
    elif type=='date':
        properties['type']='string'
        properties['format']='date-time'
    elif type=='double':
        properties['type']='number'
        properties['format']='double'
    elif type=='integer':
        properties['type']='integer'
        properties['format']='int64'
    else:
        properties['type']='string'
    return properties;

def capital1(s):
    return s[0].upper()+s[1:] 

def singular(s):
    if s[-3:]=='ies':
        return s[0:-3]+'y'
    elif s[-4:]=='sses':
        return s[0:-4]+'ss'
    elif s[-1]=='s':
        return s[0:-1]
    else:
        return s;

def makeEntityName(stack):
    return ''.join(stack);

def processFieldTree(fields,toplevel,nameStack):
    properties=dict()
    for field in fields.keys():
        if field.find('#')==-1:
            fieldBlock=dict();
            fieldContents=fields[field]
            if fieldContents['type']=='reference':
                fieldBlock['type']='array'
                fieldBlock['items']={'$ref':'#/definitions/'+capital1(fieldContents['entity'])}
            elif fieldContents['type']=='object':
                # Add a reference to the top level object
                # For that, we have to generate a name for this class
                nameStack.append(capital1(field))
                newEntityName=makeEntityName(nameStack)
                fieldBlock['$ref']='#/definitions/'+newEntityName
                newEntityDefinition=dict()
                newEntityDefinition['type']='object'
                newEntityDefinition['properties']=processFieldTree(fieldContents['fields'],toplevel,nameStack)
                toplevel[newEntityName]=newEntityDefinition
                nameStack.pop()
            elif fieldContents['type']=='array':
                fieldBlock['type']='array'
                arrayType=fieldContents['items']['type']
                if arrayType=='object':
                    nameStack.append(singular(capital1(field)))
                    newEntityName=makeEntityName(nameStack)
                    fieldBlock['items']={'$ref':'#definitions/'+newEntityName}
                    newEntityDefinition['type']='object'
                    newEntityDefinition['properties']=processFieldTree(fieldContents['items']['fields'],toplevel,nameStack)
                    toplevel[newEntityName]=processFieldTree(fieldContents['items']['fields'],toplevel,nameStack)
                    nameStack.pop()
                else:
                    fieldBlock['items']=getTypeProperties(arrayType)
            else:
                fieldBlock.update(getTypeProperties(fieldContents['type']))
            properties[field]=fieldBlock
    return properties

def mdJson2yaml(md):
    entity=dict()
    fields=dict()
    fields['type']='object'
    entityName=capital1(md['schema']['name'])
    fields['properties']=processFieldTree(md['schema']['fields'],entity,[entityName])
    entity[entityName]=fields
    return entity

yml=dict();
for f in sys.argv[1:]:
    md=json.load(open(f))
    yml.update(mdJson2yaml(md))

definitions=dict()
definitions['definitions']=yml
print(yaml.safe_dump(definitions, default_flow_style=False))

