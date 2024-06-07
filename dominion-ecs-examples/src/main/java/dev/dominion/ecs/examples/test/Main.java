/*
 * /*
 *  * Copyright (c) 2021 Enrico Stara
 *  * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 *  */

package dev.dominion.ecs.examples.test;

import dev.dominion.ecs.api.Dominion;

public class Main {
    public static void main(String[] args) {

        Dominion api = Dominion.create();

        var totalInterfaces =0;
        for (var i = 0; i < 1000000; i++) {
            TestInterface testComponent = new TestComponent();
            api.createEntity(testComponent);
            totalInterfaces++;
        }
        for (var i = 0; i < 1000000; i++) {
            TestInterface testComponent2 = new TestComponent2();
            api.createEntity(testComponent2);
            totalInterfaces++;
        }
        for (var i = 0; i < 1000000; i++) {
            TestInterface testComponent2 = new TestComponent3();
            api.createEntity(testComponent2);
            totalInterfaces++;
        }


        var foundTotalInterfaces = 0;
        for (var iter : api.findEntitiesWith(TestInterface.class))
        {
            System.out.println("Render entity " + iter.comp().sayHello());
            foundTotalInterfaces++;
        }

        if(totalInterfaces == foundTotalInterfaces)
        {
            System.out.println("it works!");
        }
        //  System.out.println("Has interface " + entity.has(TestInterface.class) + " Has class" + entity.has(TestComponent.class));
    }


}
