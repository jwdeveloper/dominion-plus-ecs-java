/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.examples.hello;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Scheduler;
import dev.dominion.ecs.examples.components.*;


public class HelloDominion {

    public static void main(String[] args) throws Exception {
        // create your world
        Dominion hello = Dominion.create();

        // create an entity with components

        // create a system
        Runnable system = () -> {
            //find entities
            hello.findEntitiesWith(Position.class, Velocity.class)
                    // stream the results
                    .stream().forEach(result -> {
                        Position position = result.comp1();
                        Velocity velocity = result.comp2();
                        position.x += velocity.x;
                        position.y += velocity.y;
                        System.out.printf("Entity %s moved with %s to %s\n",
                                result.entity().getName(), velocity, position);
                    });
        };




    }

    // component types can be both classes and records

    static class Position {
        double x, y;

        public Position(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Position{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    record Velocity(double x, double y) {
    }
}
