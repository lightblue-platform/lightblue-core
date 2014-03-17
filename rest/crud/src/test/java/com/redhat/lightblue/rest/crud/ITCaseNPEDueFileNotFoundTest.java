/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.rest.crud;

import com.redhat.lightblue.crud.CrudConfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import javax.inject.Inject;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author lcestari
 */
@RunWith(Arquillian.class)
@Ignore
public class ITCaseNPEDueFileNotFoundTest {

    @Deployment
    public static WebArchive createDeployment() throws FileNotFoundException {
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies().resolve().withTransitivity().asFile();

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource(new File("src/test/resources/lightblue-clud.json"), "lightblue-clud.json")
                //.addAsResource(new ByteArrayAsset(new FileInputStream (new File("src/test/resources/lightblue-clud.json"))), "lightblue-clud.json")
                ;

        for (File file : libs) {
            archive.addAsLibrary(file);
        }
        archive.addPackages(true, "com.redhat.lightblue");

        System.out.println(archive.toString(true)); // just for debug: print the generated WAR file
        return archive;

    }

    @Test
    public void testFirstIntegrationTest() throws IOException {
        System.out.println("init: ");

        StringBuilder buff = new StringBuilder();
        //try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(CrudConfiguration.FILENAME);
        //try (InputStream is = getClass().getClassLoader().getResourceAsStream(CrudConfiguration.FILENAME);
        //try (InputStream is = getClass().getResourceAsStream(CrudConfiguration.FILENAME);
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(CrudConfiguration.FILENAME);
                InputStreamReader isr = new InputStreamReader(is, Charset.defaultCharset());// ====> is is null!!!!
                BufferedReader reader = new BufferedReader(isr)) {
            String line;
            while ((line = reader.readLine()) != null) {
                buff.append(line).append("\n");
            }
        }
        System.out.println("buff: " + buff);

    }

}
