/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.rest.crud;

import java.io.File;
import javax.inject.Inject;
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
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource(new File("src/test/resources/lightblue-clud.json"), "lightblue-clud.json")
                .addClasses(RestApplication.class, RestCrudConstants.class, CrudResource.class);//, CrudConfiguration.class);
        System.out.println(archive.toString(true)); // just for debug
        return archive;
                
    }
    @Inject
    private CrudResource cut; //class under test

    @Test
    public void testFirstIntegrationTest() {
        System.out.println("crudResource: " + cut);
        System.out.println("crudResource find: " + cut.find("teste"));
        

    }

}
