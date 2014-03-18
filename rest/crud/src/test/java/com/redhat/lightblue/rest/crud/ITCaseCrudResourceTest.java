/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * This file is part of lightblue.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.rest.crud;

import com.redhat.lightblue.config.metadata.MetadataConfiguration;
import com.redhat.lightblue.crud.CrudConfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import javax.inject.Inject;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author lcestari
 */
@RunWith(Arquillian.class)
public class ITCaseCrudResourceTest {

    @Deployment
    public static WebArchive createDeployment() {
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies().resolve().withTransitivity().asFile();

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource(new File("src/test/resources/lightblue-crud.json"), MetadataConfiguration.FILENAME)
                .addAsResource(EmptyAsset.INSTANCE, "resources/test.properties");

        for (File file : libs) {
            archive.addAsLibrary(file);
        }
        archive.addPackages(true, "com.redhat.lightblue");
        return archive;

    }
    @Inject
    private CrudResource cut; //class under test

    @Test
    public void testFirstIntegrationTest() throws IOException {
        System.out.println("crudResource: " + cut);
        System.out.println("crudResource find: " + cut.find("{\"test\":\"true\"}"));

    }

}
