package com.redhat.lightblue.crud.valuegenerators;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.interceptors.UIDInterceptor;
import com.redhat.lightblue.crud.validator.DefaultFieldConstraintValidators;
import com.redhat.lightblue.crud.validator.EmptyEntityConstraintValidators;
import com.redhat.lightblue.eval.EvalTestContext;
import com.redhat.lightblue.mediator.MockCrudController;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;

public class GeneratedFieldsTest extends AbstractJsonSchemaTest {

    private EntityMetadata md;
    private JsonDoc jd;
    private Factory factory;

    @Before
    public void setUp() throws Exception {
        md = EvalTestContext.getMd("./user-complex-md.json");
        jd = EvalTestContext.getDoc("./user-complex.json");
        factory = new Factory();
        factory.addFieldConstraintValidators(new DefaultFieldConstraintValidators());
        factory.addEntityConstraintValidators(new EmptyEntityConstraintValidators());
        new UIDInterceptor().register(factory.getInterceptors());
        new GeneratedFieldInterceptor().register(factory.getInterceptors());
        factory.addCRUDController("mongo", new MockCrudController());

    }

    @Test
    public void test() {
        GeneratedFields.initializeGeneratedFields(factory, md, jd);
        String uid = jd.get(new Path("legalEntities.0.permissions.0.uid")).asText();
        Assert.assertNotNull(uid);
    }
}
