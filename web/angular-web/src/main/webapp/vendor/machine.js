/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
(function (lib, img, cjs, ss) {

    var p; // shortcut to reference prototypes

// library properties:
    lib.properties = {
        width: 1390,
        height: 330,
        fps: 25,
        color: "#FFFFFF",
        manifest: []
    };



// symbols:



    (lib.X_on_box = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 3
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#FF6E6E").s().p("AgqAwIAtg+IACADIAtgoIABABIgwBBIgpAlgAAQAWIALgPIAYAVIgLAPgAgyglIAMgOIALAKIgLAPg");
        this.shape.setTransform(2.3,0.8);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FF0000").s().p("AAiArIAMgQIALAKIgNAOgAg4AlIArglIgfgbIAMgPIAfAcIguA9gAAAAMIgBACIAvhAIALAMIgrAmIAHAGIgLAPg");
        this.shape_1.setTransform(2.9,0.9);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_1},{t:this.shape}]}).to({state:[]},17).wait(377));

        // Layer 2
        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f("#E6F9E7").s().p("AglBhIB5imQALAOAHAQIhpCRQgSgBgQgIgAhBBQIB8iqQAHAFAHAGIh7CpQgIgEgHgGgAhlAAQAAgqAfggQAfgfAqAAIAPABIhtCVQgKgVAAgYg");
        this.shape_2.setTransform(2.4,0.6);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f("#D3F4D4").s().p("AgMBpIBpiRQAIATAAAVQAAAqggAgQgfAggqgBIgIAAgAg7BaIB7ipIAFAEIAGAGIh5CmIgNgHgAhPBKQgNgOgIgPIBtiVQAWACATAMIh8CqIgFgGg");
        this.shape_3.setTransform(3.3,0.6);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_3},{t:this.shape_2}]}).to({state:[]},17).wait(377));

        // Layer 1
        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f().s("#2E873B").ss(1.5).p("ADhD4InBAAQgKAAgGgHQgHgGAAgKIAAnBQAAgKAHgGQAGgHAKAAIHBAAQAKAAAGAHQAHAGAAAKIAAHBQAAAKgHAGQgGAHgKAAg");
        this.shape_4.setTransform(0.1,-0.3);

        this.shape_5 = new cjs.Shape();
        this.shape_5.graphics.f("#3DB44F").s().p("AgrD6QgEAAgEgEQgDgDAAgEIAAneQAAgDADgEQAEgDAEAAIBXAAQAFAAACADQADAEAAADIAAHeQAAAEgDADQgCAEgFAAg");
        this.shape_5.setTransform(-21.4,0.2,0.636,1);

        this.shape_6 = new cjs.Shape();
        this.shape_6.graphics.f("#8EDC91").s().p("AjgD4QgKAAgGgHQgHgGAAgKIAAnBQAAgKAHgGQAGgHAKAAIHBAAQAKAAAGAHQAHAGAAAKIAAHBQAAAKgHAGQgGAHgKAAg");
        this.shape_6.setTransform(0.1,-0.3);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_6},{t:this.shape_5},{t:this.shape_4}]}).to({state:[]},17).wait(377));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-25.7,-26.1,51.7,51.7);


    (lib.x = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#FF3300").s().p("AAAAeIhpBeIgeghIBnhbIhnhaIAeghIBpBeIBqheIAeAhIhnBaIBnBbIgeAhg");

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-13.6,-12.5,27.3,25);


    (lib.wire = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#C5C5C5").s().p("AjzDVQgLgFAHgLQAGgLALAGQBrA3BxgnQBwgmAwhvQANgcAFgtIAIhTQAKhoArgqQAJgJAJAJQAIAJgIAJQghAfgJBKQgFApgFBMQgOBXg9BDQhEBNhlARQgaAEgZAAQhJAAhGgkg");

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-25.1,-24.9,50.3,49.8);


    (lib.will = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#FFFFFF").ss(2.2).p("AAjAAQAAAOgKALQgLAKgOAAQgNAAgLgKQgKgLAAgOQAAgNAKgLQALgKANAAQAOAAALAKQAKALAAANg");

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#C5C5C5").s().p("AgYAZQgKgLAAgOQAAgNAKgLQALgKANAAQAOAAALAKQAKALAAANQAAAOgKALQgLAKgOAAQgNAAgLgKg");

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#C5C5C5").ss(2.2).p("ABFAAQAAAdgUAUQgVAUgcAAQgcAAgUgUQgUgUAAgdQAAgbAUgVQAUgUAcAAQAcAAAVAUQAUAVAAAbg");

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f("#FFFFFF").s().p("AgwAxQgUgUAAgdQAAgbAUgVQAUgUAcAAQAcAAAVAUQAUAVAAAbQAAAdgUAUQgVAUgcAAQgcAAgUgUg");

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_3},{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-8,-8.1,16.2,16.2);


    (lib.v_on_box = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 9
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#8AE38E").s().p("Ag1AfIAOgTIAHAIIAhggIgqA5gAAqgsIAMAMIhJBIg");
        this.shape.setTransform(4.4,1.1);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#6FD373").s().p("AgUArIApg5IAlglIAEAFIg+BUIgMAOgAg9AAIAFgHIALgKIAbAbIgPATg");
        this.shape_1.setTransform(2.4,1.4);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f("#C5C5C5").s().p("AgBACIADgEIgCAFg");
        this.shape_2.setTransform(-3.7,0.9);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).to({state:[]},60).wait(334));

        // Layer 8
        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f("#E6F9E7").s().p("AglBhIB5imQALAOAHAQIhpCRQgSgBgQgIgAhBBQIB8iqQAHAFAHAGIh7CpQgIgEgHgGgAhlAAQAAgqAfggQAfgfAqAAIAPABIhtCVQgKgVAAgYg");
        this.shape_3.setTransform(2.4,0.6);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f("#D3F4D4").s().p("AgMBpIBpiRQAIATAAAVQAAAqggAgQgfAggqgBIgIAAgAg7BaIB7ipIAFAEIAGAGIh5CmIgNgHgAhPBKQgNgOgIgPIBtiVQAWACATAMIh8CqIgFgGg");
        this.shape_4.setTransform(3.3,0.6);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_4},{t:this.shape_3}]}).to({state:[]},60).wait(334));

        // Layer 10
        this.shape_5 = new cjs.Shape();
        this.shape_5.graphics.f().s("#2E873B").ss(1.5).p("ADhD4InBAAQgKAAgGgHQgHgGAAgKIAAnBQAAgKAHgGQAGgHAKAAIHBAAQAKAAAGAHQAHAGAAAKIAAHBQAAAKgHAGQgGAHgKAAg");
        this.shape_5.setTransform(0.1,-0.3);

        this.shape_6 = new cjs.Shape();
        this.shape_6.graphics.f("#3DB44F").s().p("AgrD6QgEAAgEgEQgDgDAAgEIAAneQAAgDADgEQAEgDAEAAIBXAAQAFAAACADQADAEAAADIAAHeQAAAEgDADQgCAEgFAAg");
        this.shape_6.setTransform(-21.4,0.2,0.636,1);

        this.shape_7 = new cjs.Shape();
        this.shape_7.graphics.f("#8EDC91").s().p("AjgD4QgKAAgGgHQgHgGAAgKIAAnBQAAgKAHgGQAGgHAKAAIHBAAQAKAAAGAHQAHAGAAAKIAAHBQAAAKgHAGQgGAHgKAAg");
        this.shape_7.setTransform(0.1,-0.3);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_7},{t:this.shape_6},{t:this.shape_5}]}).to({state:[]},60).wait(334));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-25.7,-26.1,51.7,51.7);


    (lib.v = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#66CC66").s().p("AilgGIAqgmIBQBXICpinIAoAoIjUDRg");

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-16.6,-12.6,33.3,25.2);


    (lib.Tween24 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape.setTransform(139.8,0);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_1.setTransform(125.1,0);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_2.setTransform(110.4,0);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_3.setTransform(95.6,0);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_4.setTransform(80.9,0);

        this.shape_5 = new cjs.Shape();
        this.shape_5.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_5.setTransform(66.2,0);

        this.shape_6 = new cjs.Shape();
        this.shape_6.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_6.setTransform(51.5,0);

        this.shape_7 = new cjs.Shape();
        this.shape_7.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_7.setTransform(36.8,0);

        this.shape_8 = new cjs.Shape();
        this.shape_8.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_8.setTransform(22.1,0);

        this.shape_9 = new cjs.Shape();
        this.shape_9.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_9.setTransform(7.4,0);

        this.shape_10 = new cjs.Shape();
        this.shape_10.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_10.setTransform(-7.4,0);

        this.shape_11 = new cjs.Shape();
        this.shape_11.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_11.setTransform(-22.1,0);

        this.shape_12 = new cjs.Shape();
        this.shape_12.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_12.setTransform(-36.8,0);

        this.shape_13 = new cjs.Shape();
        this.shape_13.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_13.setTransform(-51.5,0);

        this.shape_14 = new cjs.Shape();
        this.shape_14.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_14.setTransform(-66.2,0);

        this.shape_15 = new cjs.Shape();
        this.shape_15.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_15.setTransform(-80.9,0);

        this.shape_16 = new cjs.Shape();
        this.shape_16.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_16.setTransform(-95.6,0);

        this.shape_17 = new cjs.Shape();
        this.shape_17.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_17.setTransform(-110.4,0);

        this.shape_18 = new cjs.Shape();
        this.shape_18.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_18.setTransform(-125.1,0);

        this.shape_19 = new cjs.Shape();
        this.shape_19.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_19.setTransform(-139.8,0);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_19},{t:this.shape_18},{t:this.shape_17},{t:this.shape_16},{t:this.shape_15},{t:this.shape_14},{t:this.shape_13},{t:this.shape_12},{t:this.shape_11},{t:this.shape_10},{t:this.shape_9},{t:this.shape_8},{t:this.shape_7},{t:this.shape_6},{t:this.shape_5},{t:this.shape_4},{t:this.shape_3},{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-144.1,-9.5,288.2,19);


    (lib.Tween3 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(2).p("AAhAAQAAAOgKAJQgKAKgNAAQgNAAgJgKQgKgJAAgOQAAgMAKgKQAJgKANAAQANAAAKAKQAKAKAAAMg");
        this.shape.setTransform(-115.9,-22.5);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AgWAXQgKgJAAgOQAAgMAKgKQAJgKANAAQANAAAKAKQAKAKAAAMQAAAOgKAJQgKAKgNAAQgNAAgJgKg");
        this.shape_1.setTransform(-115.9,-22.5);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#C5C5C5").ss(2).p("AAhAAQAAAOgKAJQgKAKgNAAQgNAAgJgKQgKgJAAgOQAAgMAKgKQAJgKANAAQAOAAAJAKQAKAKAAAMg");
        this.shape_2.setTransform(80.5,-22.5);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f("#FFFFFF").s().p("AgWAXQgKgJAAgOQAAgMAKgKQAJgKANAAQAOAAAJAKQAKAKAAAMQAAAOgKAJQgKAKgNAAQgNAAgJgKg");
        this.shape_3.setTransform(80.5,-22.5);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f().s("#C5C5C5").ss(2).p("AhSgIIAAARQAAAOAKALQAKAKAPAAIBfAAQAOAAALgKQAKgLAAgOIAAgRQAAgOgKgLQgLgKgOAAIhfAAQgPAAgKAKQgKALAAAOg");
        this.shape_4.setTransform(-115.9,29.5);

        this.shape_5 = new cjs.Shape();
        this.shape_5.graphics.f("#FFFFFF").s().p("AgvAsQgPAAgKgLQgKgKAAgOIAAgRQAAgOAKgLQAKgKAPAAIBfAAQAOAAALAKQAKALAAAOIAAARQAAAOgKAKQgLALgOAAg");
        this.shape_5.setTransform(-115.9,29.5);

        this.shape_6 = new cjs.Shape();
        this.shape_6.graphics.f().s("#C5C5C5").ss(2).p("AAIhSIgQAAQgOAAgLAKQgKALAAAOIAABfQAAAPAKAKQALAKAOAAIAQAAQAPAAAKgKQALgKAAgPIAAhfQAAgOgLgLQgKgKgPAAg");
        this.shape_6.setTransform(95.7,9.3);

        this.shape_7 = new cjs.Shape();
        this.shape_7.graphics.f("#FFFFFF").s().p("AgHBTQgPAAgKgKQgLgKAAgPIAAhfQAAgOALgLQAKgKAPAAIAQAAQAOAAAKAKQALALAAAOIAABfQAAAPgLAKQgKAKgOAAg");
        this.shape_7.setTransform(95.7,9.3);

        this.shape_8 = new cjs.Shape();
        this.shape_8.graphics.f("#C5C5C5").s().p("AAAATQgIAAgFgGQgGgGABgHQAAgIAGgFQAGgGAGABQAJAAAFAGQAGAGgBAGQAAAJgGAFQgGAFgHAAIAAAAg");
        this.shape_8.setTransform(56.7,-22.5);

        this.shape_9 = new cjs.Shape();
        this.shape_9.graphics.f().s("#C5C5C5").ss(1.5).p("AAggcQAMANgBAQQgBASgNANQgNAMgQgBQgSgBgNgNQgMgNABgQQABgSANgNQANgMAQABQASABANANg");
        this.shape_9.setTransform(56.7,-22.5);

        this.shape_10 = new cjs.Shape();
        this.shape_10.graphics.f("#FFFFFF").s().p("AAAArQgSgBgNgNQgMgNABgQQABgSANgNQANgMAQABQASABANANQAMANgBAQQgBASgNANQgNALgQAAIAAAAg");
        this.shape_10.setTransform(56.7,-22.5);

        this.shape_11 = new cjs.Shape();
        this.shape_11.graphics.f("#C5C5C5").s().p("AAAATQgIAAgFgGQgGgGABgHQAAgIAGgFQAGgGAGABQAJAAAFAGQAGAGgBAGQAAAJgGAFQgGAFgHAAIAAAAg");
        this.shape_11.setTransform(35.6,-22.5);

        this.shape_12 = new cjs.Shape();
        this.shape_12.graphics.f().s("#C5C5C5").ss(1.5).p("AAggcQAMANgBAQQgBASgNANQgNAMgQgBQgSgBgNgNQgMgNABgQQABgSANgNQANgMAQABQASABANANg");
        this.shape_12.setTransform(35.6,-22.5);

        this.shape_13 = new cjs.Shape();
        this.shape_13.graphics.f("#FFFFFF").s().p("AAAArQgSgBgNgNQgMgNABgQQABgSANgNQANgMAQABQASABANANQAMANgBAQQgBASgNANQgNALgQAAIAAAAg");
        this.shape_13.setTransform(35.6,-22.5);

        this.shape_14 = new cjs.Shape();
        this.shape_14.graphics.f("#C5C5C5").s().p("AAAATQgIAAgFgGQgFgGgBgHQABgIAGgFQAGgGAGABQAJAAAFAGQAGAGAAAGQgBAJgGAFQgGAFgHAAIAAAAg");
        this.shape_14.setTransform(14.6,-22.5);

        this.shape_15 = new cjs.Shape();
        this.shape_15.graphics.f().s("#C5C5C5").ss(1.5).p("AAggcQAMANgBAQQgBASgNANQgNAMgQgBQgSgBgMgNQgNgNABgQQABgSANgNQANgMAQABQASABANANg");
        this.shape_15.setTransform(14.6,-22.5);

        this.shape_16 = new cjs.Shape();
        this.shape_16.graphics.f("#FFFFFF").s().p("AAAArQgSgBgMgNQgNgNABgQQABgSANgNQANgMAQABQASABANANQAMANgBAQQgBASgNANQgNALgQAAIAAAAg");
        this.shape_16.setTransform(14.6,-22.5);

        this.shape_17 = new cjs.Shape();
        this.shape_17.graphics.f("#C5C5C5").s().p("AAAATQgIAAgFgGQgGgGAAgHQABgIAGgFQAGgGAGABQAIAAAGAGQAFAGABAGQgBAJgGAFQgFAFgIAAIAAAAg");
        this.shape_17.setTransform(-6.4,-22.5);

        this.shape_18 = new cjs.Shape();
        this.shape_18.graphics.f().s("#C5C5C5").ss(1.5).p("AAggcQAMANgBAQQgBASgNANQgNAMgQgBQgTgBgLgNQgNgNABgQQABgSANgNQANgMARABQASABAMANg");
        this.shape_18.setTransform(-6.4,-22.5);

        this.shape_19 = new cjs.Shape();
        this.shape_19.graphics.f("#FFFFFF").s().p("AgBArQgRgBgNgNQgMgNABgQQABgSANgNQANgMARABQARABAMANQANANgBAQQgBASgNANQgNALgQAAIgBAAg");
        this.shape_19.setTransform(-6.4,-22.5);

        this.shape_20 = new cjs.Shape();
        this.shape_20.graphics.f("#C5C5C5").s().p("AAAATQgIAAgFgGQgGgGAAgHQABgIAGgFQAGgGAGABQAIAAAGAGQAFAGABAGQgBAJgGAFQgGAFgHAAIAAAAg");
        this.shape_20.setTransform(-27.5,-22.5);

        this.shape_21 = new cjs.Shape();
        this.shape_21.graphics.f().s("#C5C5C5").ss(1.5).p("AAggcQAMANgBAQQgBASgNANQgNAMgRgBQgSgBgMgNQgMgNABgQQABgSANgNQANgMARABQASABAMANg");
        this.shape_21.setTransform(-27.5,-22.5);

        this.shape_22 = new cjs.Shape();
        this.shape_22.graphics.f("#FFFFFF").s().p("AgBArQgRgBgNgNQgMgNABgQQABgSANgNQAOgMAQABQARABAMANQANANgBAQQgBASgNANQgMALgRAAIgBAAg");
        this.shape_22.setTransform(-27.5,-22.5);

        this.shape_23 = new cjs.Shape();
        this.shape_23.graphics.f("#C5C5C5").s().p("AAAATQgHAAgGgGQgGgGABgHQAAgIAGgFQAGgGAHABQAIAAAFAGQAFAGABAGQgBAJgGAFQgGAFgHAAIAAAAg");
        this.shape_23.setTransform(-48.5,-22.5);

        this.shape_24 = new cjs.Shape();
        this.shape_24.graphics.f().s("#C5C5C5").ss(1.5).p("AAfgcQANANgBAQQgBASgNANQgNAMgQgBQgSgBgNgNQgMgNABgQQABgSANgNQANgMAQABQASABAMANg");
        this.shape_24.setTransform(-48.5,-22.5);

        this.shape_25 = new cjs.Shape();
        this.shape_25.graphics.f("#FFFFFF").s().p("AAAArQgSgBgNgNQgMgNABgQQABgSANgNQANgMAQABQASABAMANQANANgBAQQgBASgNANQgNALgQAAIAAAAg");
        this.shape_25.setTransform(-48.5,-22.5);

        this.shape_26 = new cjs.Shape();
        this.shape_26.graphics.f("#C5C5C5").s().p("AAAATQgIAAgFgGQgGgGABgHQAAgIAGgFQAGgGAGABQAJAAAFAGQAGAGgBAGQAAAJgGAFQgGAFgHAAIAAAAg");
        this.shape_26.setTransform(-69.6,-22.5);

        this.shape_27 = new cjs.Shape();
        this.shape_27.graphics.f().s("#C5C5C5").ss(1.5).p("AAggcQAMANgBAQQgBASgNANQgNAMgQgBQgSgBgNgNQgMgNABgQQABgSANgNQANgMAQABQASABANANg");
        this.shape_27.setTransform(-69.6,-22.5);

        this.shape_28 = new cjs.Shape();
        this.shape_28.graphics.f("#FFFFFF").s().p("AAAArQgSgBgNgNQgMgNABgQQABgSANgNQANgMAQABQASABANANQAMANgBAQQgBASgNANQgNALgQAAIAAAAg");
        this.shape_28.setTransform(-69.6,-22.5);

        this.shape_29 = new cjs.Shape();
        this.shape_29.graphics.f("#C5C5C5").s().p("AAAATQgIAAgFgGQgGgGABgHQAAgIAGgFQAGgGAGABQAJAAAFAGQAGAGgBAGQAAAJgGAFQgGAFgHAAIAAAAg");
        this.shape_29.setTransform(-90.6,-22.5);

        this.shape_30 = new cjs.Shape();
        this.shape_30.graphics.f().s("#C5C5C5").ss(1.5).p("AAggcQAMANgBAQQgBASgNANQgNAMgQgBQgSgBgNgNQgMgNABgQQABgSANgNQANgMAQABQASABANANg");
        this.shape_30.setTransform(-90.6,-22.5);

        this.shape_31 = new cjs.Shape();
        this.shape_31.graphics.f("#FFFFFF").s().p("AAAArQgSgBgNgNQgMgNABgQQABgSANgNQANgMAQABQASABANANQAMANgBAQQgBASgNANQgNALgQAAIAAAAg");
        this.shape_31.setTransform(-90.6,-22.5);

        this.shape_32 = new cjs.Shape();
        this.shape_32.graphics.f().s("#C5C5C5").ss(2).p("ABpBBQAAAjgZAZQgYAZgjAAIgpAAQgjAAgYgZQgZgZAAgjIAAiCQAAgjAZgYQAYgZAjAAIApAAQAjAAAYAZQAZAYAAAjg");
        this.shape_32.setTransform(80.5,-22.4);

        this.shape_33 = new cjs.Shape();
        this.shape_33.graphics.f("#FFFFFF").s().p("AgUCWQgiAAgZgZQgZgYAAgkIAAiCQAAgjAZgYQAZgZAiAAIApAAQAjAAAYAZQAYAYAAAjIAACCQAAAkgYAYQgYAZgjAAg");
        this.shape_33.setTransform(80.5,-22.4);

        this.shape_34 = new cjs.Shape();
        this.shape_34.graphics.f().s("#C5C5C5").ss(2).p("ABpBBQAAAjgZAZQgYAZgjAAIgpAAQgjAAgYgZQgZgZAAgjIAAiCQAAgjAZgYQAYgZAjAAIApAAQAjAAAYAZQAZAYAAAjg");
        this.shape_34.setTransform(-116.5,-22.4);

        this.shape_35 = new cjs.Shape();
        this.shape_35.graphics.f("#FFFFFF").s().p("AgUCWQgiAAgZgZQgYgYgBgkIAAiCQABgjAYgYQAZgZAiAAIApAAQAjAAAYAZQAYAYAAAjIAACCQAAAkgYAYQgYAZgjAAg");
        this.shape_35.setTransform(-116.5,-22.4);

        this.shape_36 = new cjs.Shape();
        this.shape_36.graphics.f().s("#C5C5C5").ss(2).p("AN6BQI7zAAIAAifIbzAAg");
        this.shape_36.setTransform(-18,-22.4);

        this.shape_37 = new cjs.Shape();
        this.shape_37.graphics.f("#FFFFFF").s().p("At5BQIAAifIbyAAIAACfg");
        this.shape_37.setTransform(-18,-22.4);

        this.shape_38 = new cjs.Shape();
        this.shape_38.graphics.f().s("#C5C5C5").ss(2).p("AD/AtIAABkIn9AAIAAkhIBaAAIAAC9g");
        this.shape_38.setTransform(101.5,0.1);

        this.shape_39 = new cjs.Shape();
        this.shape_39.graphics.f("#FFFFFF").s().p("Aj9CQIAAkfIBaAAIAAC8IGhAAIAABjg");
        this.shape_39.setTransform(101.5,0.1);

        this.shape_40 = new cjs.Shape();
        this.shape_40.graphics.f().s("#C5C5C5").ss(2.1).p("AAxEDIhhAAIAAoFIBhAAg");
        this.shape_40.setTransform(-115.9,11.5);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_40},{t:this.shape_39},{t:this.shape_38},{t:this.shape_37},{t:this.shape_36},{t:this.shape_35},{t:this.shape_34},{t:this.shape_33},{t:this.shape_32},{t:this.shape_31},{t:this.shape_30},{t:this.shape_29},{t:this.shape_28},{t:this.shape_27},{t:this.shape_26},{t:this.shape_25},{t:this.shape_24},{t:this.shape_23},{t:this.shape_22},{t:this.shape_21},{t:this.shape_20},{t:this.shape_19},{t:this.shape_18},{t:this.shape_17},{t:this.shape_16},{t:this.shape_15},{t:this.shape_14},{t:this.shape_13},{t:this.shape_12},{t:this.shape_11},{t:this.shape_10},{t:this.shape_9},{t:this.shape_8},{t:this.shape_7},{t:this.shape_6},{t:this.shape_5},{t:this.shape_4},{t:this.shape_3},{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-128,-38.4,256,76.9);


    (lib.Symbol37 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#848484").s().p("AgYAZIAAgxIAxAAIAAAxg");

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-2.5,-2.5,5.1,5.1);


    (lib.Symbol34 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(2,1,1).p("AFXAoIqtAAAlWgnIKtAA");
        this.shape.setTransform(0,0.8);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AlWAoIAAgBIAAgCIAAgCIAAgBIAAgCIAAgBIAAgBIAAgCIAAgCIAAgBIAAgCIAAgBIAAgBIAAgCIAAgCIAAgBIAAgBIAAgCIAAgBIAAgCIAAgCIAAgBIAAgBIAAgCIAAgBIAAgCIAAgBIAAAAIAAgCIAAgBIAAgBIAAgCIAAgCIAAgBIAAgCIAAgBIAAgBIAAgCIAAgCIAAgBIAAgCIAAgBIAAgBIAAgCIAAgCIAAgBIAAgCIAAgBIAAgBIAAgCIAAgCIAAgBIAAgCIAAAAIKtAAIAABOg");
        this.shape_1.setTransform(0,0.8);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-35.3,-4.2,70.7,10);


    (lib.Symbol33 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#8EDC91").s().p("AhwgDIAdgaIA2A7IBzhyIAbAcIiPCNg");
        this.shape.setTransform(2.8,2.1);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f().s("#C5C5C5").ss(2,1,1).p("AChAbQgGBbguA+QgvA/g+AAQgKAAgNgDQg7gOgnhEQgohGAAhYQAAhbAohEQAnhDA7gNQANgDAKAAQA5AAAtA3QAsA1AMBT");
        this.shape_1.setTransform(2.3,0);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f("#C5C5C5").s().p("AgtCrQgvhIAAhjQAAhlAvhGQAuhHBDAAQAMAAANADQg6ANgmBDQgnBEAABbQAABYAnBGQAmBEA6AOQgMADgNAAQhDAAguhIg");
        this.shape_2.setTransform(-9.6,0);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f("#FFFFFF").s().p("AgZDwQg7gOgnhEQgohGAAhYQAAhbAohEQAnhDA7gNQANgDAMAAIAIABQAiADAcAWQAPALAPASQAMAPAKASQALAUAJAYIAGAUQAFATADAUIAIAAIAAABIAAACIAAABIAAACIAAABIAAACIAAABIAAACIAAABIAAACIAAABIAAACIAAABIAAACIAAABIAAACIAAABIAAACIAAABIAAACIAAABIAAACIAAABIAAACIAAABIAAACIAAABIAAACIAAABIAAACIAAABIAAACIAAABIAAACIAAAAIAAABIAAABIAAACIAAABIAAACIAAABIAAACIAAABIAAACIAAABIAAACIAAABIAAACIAAABIAAACIAAABIAAACIAAABIAAABIgFAAQgCAhgHAcQgKAkgQAeIgCADIgPAXQgRAXgUAPQgcAVgiADIgIABQgMAAgNgDg");
        this.shape_3.setTransform(2.5,0);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f().s("#C5C5C5").ss(2,1,1).p("AgIgnIAQAAAAIAoIgNAA");
        this.shape_4.setTransform(17.5,-1.2);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_4},{t:this.shape_3},{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-18.9,-25.3,38.4,50.7);


    (lib.Symbol29 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(2,1,1).p("AhugjQAJgfAXgUQAVgTAegGQAMgDANAAQAuAAAiAhQAhAhAAAwQAAAughAiQgiAjguAAQgLAAgOgCQgcgHgWgTQgWgTgKgZ");
        this.shape.setTransform(2,0);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AgbBwQgcgGgWgUQgWgSgKgaIgBhOQAJgeAXgUQAVgTAegGQAMgDANAAQAuAAAiAhQAhAhAAAwQAAAughAiQgiAjguAAQgLAAgOgDg");
        this.shape_1.setTransform(2,0);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f("#C5C5C5").s().p("AhEBeQgdgUgMggIgBhOQAMgkAdgVQAdgVAmAAQALAAAMADQAnAJAaAeQAZAfAAApQAAAngZAgQgaAggnAJQgNADgKAAQgkAAgegVg");
        this.shape_2.setTransform(-2,0);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-13.1,-12.5,27.4,25.1);


    (lib.Symbol25 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(2.2,1,1).p("Agsg3IBZBv");

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-5.6,-6.7,11.2,13.6);


    (lib.Symbol24 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(2.2,1,1).p("AApg6IhRB1");

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-5.2,-7,10.5,14.1);


    (lib.Symbol23 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#888888").ss(2.4).p("AJ/AmIzpAAQgXAAgNgLQgNgLAAgQQAAgPALgLQAMgLAQAAITpAAQAQAAAMALQALALAAAPQAAAOgKANQgJALgKAAg");

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#DFDFDF").s().p("ApqAmQgXAAgNgLQgNgLAAgQQAAgPALgLQAMgLAQAAITpAAQAQAAAMALQALALAAAPQAAAOgKANQgJALgKAAg");

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-75.2,-5.8,143.3,11.5);


    (lib.Symbol22 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#333333").s().p("AgcAcIAAg3IA5AAIAAA3g");

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("rgba(51,51,51,0)").s().p("AgcAcIAAg3IA5AAIAAA3g");

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape}]}).to({state:[{t:this.shape_1}]},15).to({state:[{t:this.shape}]},14).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-2.9,-2.9,5.9,5.8);


    (lib.Symbol19 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#888888").s().p("AgcC7IAAl1IA5AAIAAF1g");

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#888888").s().p("AgcCmIAAlLIA5AAIAAFLg");
        this.shape_1.setTransform(0,2.1);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f("#888888").s().p("AgcCSIAAkjIA5AAIAAEjg");
        this.shape_2.setTransform(0,4.1);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f("#888888").s().p("AgcCAIAAj/IA5AAIAAD/g");
        this.shape_3.setTransform(0,5.9);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f("#888888").s().p("AgcBvIAAjeIA5AAIAADeg");
        this.shape_4.setTransform(0,7.6);

        this.shape_5 = new cjs.Shape();
        this.shape_5.graphics.f("#888888").s().p("AgcBgIAAi/IA5AAIAAC/g");
        this.shape_5.setTransform(0,9.1);

        this.shape_6 = new cjs.Shape();
        this.shape_6.graphics.f("#888888").s().p("AgcBSIAAijIA5AAIAACjg");
        this.shape_6.setTransform(0,10.5);

        this.shape_7 = new cjs.Shape();
        this.shape_7.graphics.f("#888888").s().p("AgcBGIAAiLIA5AAIAACLg");
        this.shape_7.setTransform(0,11.7);

        this.shape_8 = new cjs.Shape();
        this.shape_8.graphics.f("#888888").s().p("AgcA7IAAh1IA5AAIAAB1g");
        this.shape_8.setTransform(0,12.8);

        this.shape_9 = new cjs.Shape();
        this.shape_9.graphics.f("#888888").s().p("AgcAyIAAhjIA5AAIAABjg");
        this.shape_9.setTransform(0,13.7);

        this.shape_10 = new cjs.Shape();
        this.shape_10.graphics.f("#888888").s().p("AgcAqIAAhTIA5AAIAABTg");
        this.shape_10.setTransform(0,14.5);

        this.shape_11 = new cjs.Shape();
        this.shape_11.graphics.f("#888888").s().p("AgcAjIAAhFIA5AAIAABFg");
        this.shape_11.setTransform(0,15.2);

        this.shape_12 = new cjs.Shape();
        this.shape_12.graphics.f("#888888").s().p("AgcAeIAAg7IA5AAIAAA7g");
        this.shape_12.setTransform(0,15.7);

        this.shape_13 = new cjs.Shape();
        this.shape_13.graphics.f("#888888").s().p("AgcAaIAAg0IA5AAIAAA0g");
        this.shape_13.setTransform(0,16.1);

        this.shape_14 = new cjs.Shape();
        this.shape_14.graphics.f("#888888").s().p("AgcAYIAAgvIA5AAIAAAvg");
        this.shape_14.setTransform(0,16.3);

        this.shape_15 = new cjs.Shape();
        this.shape_15.graphics.f("#888888").s().p("AgcAXIAAguIA5AAIAAAug");
        this.shape_15.setTransform(0,16.4);

        this.shape_16 = new cjs.Shape();
        this.shape_16.graphics.f("#888888").s().p("AgcAuIAAhbIA5AAIAABbg");
        this.shape_16.setTransform(0,14.1);

        this.shape_17 = new cjs.Shape();
        this.shape_17.graphics.f("#888888").s().p("AgcBDIAAiFIA5AAIAACFg");
        this.shape_17.setTransform(0,12);

        this.shape_18 = new cjs.Shape();
        this.shape_18.graphics.f("#888888").s().p("AgcBWIAAirIA5AAIAACrg");
        this.shape_18.setTransform(0,10.1);

        this.shape_19 = new cjs.Shape();
        this.shape_19.graphics.f("#888888").s().p("AgcBnIAAjOIA5AAIAADOg");
        this.shape_19.setTransform(0,8.4);

        this.shape_20 = new cjs.Shape();
        this.shape_20.graphics.f("#888888").s().p("AgcB3IAAjtIA5AAIAADtg");
        this.shape_20.setTransform(0,6.8);

        this.shape_21 = new cjs.Shape();
        this.shape_21.graphics.f("#888888").s().p("AgcCFIAAkJIA5AAIAAEJg");
        this.shape_21.setTransform(0,5.4);

        this.shape_22 = new cjs.Shape();
        this.shape_22.graphics.f("#888888").s().p("AgcCdIAAk5IA5AAIAAE5g");
        this.shape_22.setTransform(0,3);

        this.shape_23 = new cjs.Shape();
        this.shape_23.graphics.f("#888888").s().p("AgcCuIAAlbIA5AAIAAFbg");
        this.shape_23.setTransform(0,1.3);

        this.shape_24 = new cjs.Shape();
        this.shape_24.graphics.f("#888888").s().p("AgcCzIAAlmIA5AAIAAFmg");
        this.shape_24.setTransform(0,0.8);

        this.shape_25 = new cjs.Shape();
        this.shape_25.graphics.f("#888888").s().p("AgcC4IAAlvIA5AAIAAFvg");
        this.shape_25.setTransform(0,0.3);

        this.shape_26 = new cjs.Shape();
        this.shape_26.graphics.f("#888888").s().p("AgcC6IAAlzIA5AAIAAFzg");
        this.shape_26.setTransform(0,0.1);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape}]}).to({state:[{t:this.shape_1}]},1).to({state:[{t:this.shape_2}]},1).to({state:[{t:this.shape_3}]},1).to({state:[{t:this.shape_4}]},1).to({state:[{t:this.shape_5}]},1).to({state:[{t:this.shape_6}]},1).to({state:[{t:this.shape_7}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_9}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_11}]},1).to({state:[{t:this.shape_12}]},1).to({state:[{t:this.shape_13}]},1).to({state:[{t:this.shape_14}]},1).to({state:[{t:this.shape_15}]},1).to({state:[{t:this.shape_16}]},1).to({state:[{t:this.shape_17}]},1).to({state:[{t:this.shape_18}]},1).to({state:[{t:this.shape_19}]},1).to({state:[{t:this.shape_20}]},1).to({state:[{t:this.shape_21}]},1).to({state:[{t:this.shape_2}]},1).to({state:[{t:this.shape_22}]},1).to({state:[{t:this.shape_1}]},1).to({state:[{t:this.shape_23}]},1).to({state:[{t:this.shape_24}]},1).to({state:[{t:this.shape_25}]},1).to({state:[{t:this.shape_26}]},1).to({state:[{t:this.shape}]},1).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-2.9,-18.7,5.9,37.5);


    (lib.Symbol16 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(1.8,0,1).p("AkOCmIAAkGQAAgcAUgVQAUgUAdAAIHYAAQgbABgTATQgUAVAAAcIAAEGg");
        this.shape.setTransform(-2.4,-0.4);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AkOCmIAAkGQAAgcAUgVQAUgTAdAAIHYAAQgbAAgTATQgUAVAAAcIAAEGg");
        this.shape_1.setTransform(-2.4,-0.4);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-30.5,-18,56.4,35.2);


    (lib.Symbol15 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(1.8,1,1).p("Ah7AAID2AA");
        this.shape.setTransform(-5,-27.5);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f().s("#C5C5C5").ss(1.8,1,1).p("Ah7AAID2AA");
        this.shape_1.setTransform(-5,-2.4);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#C5C5C5").ss(1.8,1,1).p("AitAAIFaAA");
        this.shape_2.setTransform(0,-7.4);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f().s("#C5C5C5").ss(1.8,1,1).p("Ah7AAID2AA");
        this.shape_3.setTransform(-5,-12.4);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f().s("#C5C5C5").ss(1.8,1,1).p("AitAAIFaAA");
        this.shape_4.setTransform(0,-17.4);

        this.shape_5 = new cjs.Shape();
        this.shape_5.graphics.f().s("#C5C5C5").ss(1.8,1,1).p("Ah7AAID2AA");
        this.shape_5.setTransform(-5,-22.4);

        this.shape_6 = new cjs.Shape();
        this.shape_6.graphics.f().s("#C5C5C5").ss(1.8,1,1).p("Ah7AAID2AA");
        this.shape_6.setTransform(-5,22.4);

        this.shape_7 = new cjs.Shape();
        this.shape_7.graphics.f().s("#C5C5C5").ss(1.8,1,1).p("AitAAIFaAA");
        this.shape_7.setTransform(0,17.4);

        this.shape_8 = new cjs.Shape();
        this.shape_8.graphics.f().s("#C5C5C5").ss(1.8,1,1).p("Ah7AAID2AA");
        this.shape_8.setTransform(-5,12.4);

        this.shape_9 = new cjs.Shape();
        this.shape_9.graphics.f().s("#C5C5C5").ss(1.8,1,1).p("AitAAIFaAA");
        this.shape_9.setTransform(0,7.4);

        this.shape_10 = new cjs.Shape();
        this.shape_10.graphics.f().s("#C5C5C5").ss(1.8,1,1).p("Ah7AAID2AA");
        this.shape_10.setTransform(-5,2.4);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_10},{t:this.shape_9},{t:this.shape_8},{t:this.shape_7},{t:this.shape_6},{t:this.shape_5},{t:this.shape_4},{t:this.shape_3},{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-18.4,-28.5,36.8,52);


    (lib.Symbol11 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#848484").s().p("AgEAjQgCgDgBgDIAAg5QABgEACgCQACgCACAAQADAAADACQACACAAAEIAAA5QAAADgCADQgDACgDAAQgCAAgCgCg");

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-0.8,-3.7,1.6,7.5);


    (lib.Symbol9 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#848484").ss(2.4).p("AHGArQAAAWgQAQQgPAPgWAAIshAAQgWAAgPgPQgQgQAAgWIAAhWQAAgWAQgPQAPgQAWAAIMhAAQAWAAAPAQQAQAPAAAWg");

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AmQBgQgWAAgPgPQgQgPAAgXIAAhVQAAgXAQgPQAPgPAWAAIMhAAQAWAAAPAPQAQAPAAAXIAABVQAAAXgQAPQgPAPgWAAg");

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-46.6,-10.9,93.3,21.8);


    (lib.Symbol7 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#848484").ss(2).p("AEYAOQAAAOgKALQgLALgOAAInoAAQgPAAgKgLQgLgLAAgOIAAgcQAAgOALgLQAKgKAPAAIHoAAQAOAAALAKQAKALAAAOg");

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AjzAxQgOAAgLgKQgLgKAAgPIAAgcQAAgOALgLQALgKAOAAIHoAAQAOAAALAKQAJALABAOIAAAcQgBAPgJAKQgLAKgOAAg");

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-29,-6,58,17.7);


    (lib.Symbol5 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(1.4,1,1).p("AAkAAQAAAPgKAKQgLALgPAAQgOAAgKgLQgLgKAAgPQAAgOALgKQAKgLAOAAQAPAAALALQAKAKAAAOg");
        this.shape.setTransform(-11.7,-2);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AgYAZQgLgKAAgPQAAgOALgKQALgLANAAQAPAAAKALQALAKAAAOQAAAPgLAKQgKALgPAAQgNAAgLgLg");
        this.shape_1.setTransform(-11.7,-2);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#C5C5C5").ss(2.2,1,1).p("ACOggIkaBB");
        this.shape_2.setTransform(1.2,-5.8);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-16.3,-10.2,32.9,12.9);


    (lib.Symbol4 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(1.4,1,1).p("AAkAAQAAAPgKAKQgLALgPAAQgOAAgKgLQgLgKAAgPQAAgOALgKQAKgLAOAAQAPAAALALQAKAKAAAOg");
        this.shape.setTransform(-2.2,2.2);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AgYAaQgLgMAAgOQAAgOALgLQALgKANAAQAOAAALAKQALALAAAOQAAAOgLAMQgLAKgOAAQgNAAgLgKg");
        this.shape_1.setTransform(-2.2,2.2);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#C5C5C5").ss(2.2,1,1).p("AAtg+IhZB8");
        this.shape_2.setTransform(1.4,-4.5);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-6.9,-11.9,13.9,18.8);


    (lib.Symbol3 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(1.4,1,1).p("AAkAAQAAAPgKAKQgLALgPAAQgOAAgKgLQgLgKAAgPQAAgOALgKQAKgLAOAAQAPAAALALQAKAKAAAOg");
        this.shape.setTransform(2.3,2.2);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AgYAaQgLgMAAgOQAAgOALgLQALgKANAAQAPAAALAKQAKALAAAOQAAAOgKAMQgLAKgPAAQgNAAgLgKg");
        this.shape_1.setTransform(2.3,2.2);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#C5C5C5").ss(2.2,1,1).p("Agsg+IBZB8");
        this.shape_2.setTransform(-1.4,-4.5);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-7,-11.9,13.9,18.8);


    (lib.Symbol2 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(1.4,1,1).p("AAkAAQAAAPgKALQgLAKgPAAQgNAAgLgKQgLgLAAgPQAAgOALgKQALgLANAAQAPAAALALQAKAKAAAOg");
        this.shape.setTransform(11.9,-1.4);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AgYAaQgLgLAAgPQAAgNALgLQALgLANAAQAOAAALALQALALAAANQAAAPgLALQgLAKgOAAQgNAAgLgKg");
        this.shape_1.setTransform(11.9,-1.4);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#C5C5C5").ss(2.2,1,1).p("AiNggIEaBB");
        this.shape_2.setTransform(-1.3,-5.9);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-16.6,-10.3,33.2,13.6);


    (lib.strips = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape.setTransform(-154.5,8.4);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_1.setTransform(-139.8,8.4);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_2.setTransform(154.5,8.4);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_3.setTransform(139.8,8.4);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_4.setTransform(125.1,8.4);

        this.shape_5 = new cjs.Shape();
        this.shape_5.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_5.setTransform(110.4,8.4);

        this.shape_6 = new cjs.Shape();
        this.shape_6.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_6.setTransform(95.7,8.4);

        this.shape_7 = new cjs.Shape();
        this.shape_7.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_7.setTransform(81,8.4);

        this.shape_8 = new cjs.Shape();
        this.shape_8.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_8.setTransform(66.2,8.4);

        this.shape_9 = new cjs.Shape();
        this.shape_9.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_9.setTransform(51.5,8.4);

        this.shape_10 = new cjs.Shape();
        this.shape_10.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_10.setTransform(36.8,8.4);

        this.shape_11 = new cjs.Shape();
        this.shape_11.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_11.setTransform(22.1,8.4);

        this.shape_12 = new cjs.Shape();
        this.shape_12.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_12.setTransform(7.4,8.4);

        this.shape_13 = new cjs.Shape();
        this.shape_13.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_13.setTransform(-7.3,8.4);

        this.shape_14 = new cjs.Shape();
        this.shape_14.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_14.setTransform(-22,8.4);

        this.shape_15 = new cjs.Shape();
        this.shape_15.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_15.setTransform(-36.8,8.4);

        this.shape_16 = new cjs.Shape();
        this.shape_16.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_16.setTransform(-51.5,8.4);

        this.shape_17 = new cjs.Shape();
        this.shape_17.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_17.setTransform(-66.2,8.4);

        this.shape_18 = new cjs.Shape();
        this.shape_18.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_18.setTransform(-80.9,8.4);

        this.shape_19 = new cjs.Shape();
        this.shape_19.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_19.setTransform(-95.6,8.4);

        this.shape_20 = new cjs.Shape();
        this.shape_20.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_20.setTransform(-110.3,8.4);

        this.shape_21 = new cjs.Shape();
        this.shape_21.graphics.f().s("#CCCCCC").ss(2.3,1).p("AgehSIA9Cl");
        this.shape_21.setTransform(-125,8.4);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_21},{t:this.shape_20},{t:this.shape_19},{t:this.shape_18},{t:this.shape_17},{t:this.shape_16},{t:this.shape_15},{t:this.shape_14},{t:this.shape_13},{t:this.shape_12},{t:this.shape_11},{t:this.shape_10},{t:this.shape_9},{t:this.shape_8},{t:this.shape_7},{t:this.shape_6},{t:this.shape_5},{t:this.shape_4},{t:this.shape_3},{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-158.8,-1.1,317.7,19);


    (lib.robotic_forarm = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(2).p("AgdipIAAFwIA7AAIAAmX");
        this.shape.setTransform(0,0.5);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AgdDMIAAlwIA7gnIAAGXg");

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-4,-21.4,8,42.9);


    (lib.robotic_arm = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(2).p("AH9gmIvHAAQgRAAgLALQgMAMAAAPQAAAQAMAMQAMALAQAAIOqgJ");
        this.shape.setTransform(-0.5,0);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AnrAcQgMgMAAgQQAAgPAMgMQALgLARAAIPHAAIgdBEIuqAJQgQAAgMgLg");

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-56.4,-5,107.9,19);


    (lib.Robot_Head = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(2).p("ACGApIkLAAQgOAAgJgKQgKgJAAgOIAAgQQAAgNAKgJQAJgKAOAAIELAAQAOAAAJAKQAKAJAAANIAAAQQAAAOgKAJQgJAKgOAAg");
        this.shape.setTransform(0,14.5,0.984,0.984);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AiFApQgOAAgJgKQgKgJAAgOIAAgQQAAgNAKgJQAJgKAOAAIELAAQAOAAAJAKQAKAJAAANIAAAQQAAAOgKAJQgJAKgOAAg");
        this.shape_1.setTransform(0,14.5,0.984,0.984);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#C5C5C5").ss(2.2).p("Ah1A7QAAgxAjghQAigjAwAAQAwAAAjAjQAiAhAAAxg");
        this.shape_2.setTransform(0,5.9,0.984,0.984);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f("#FFFFFF").s().p("Ah0A7QgBgxAjghQAigjAwAAQAwAAAjAjQAiAhABAxg");
        this.shape_3.setTransform(0,5.9,0.984,0.984);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_3},{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-17.4,-1.1,35,20.7);


    (lib.pipeta = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#848484").p("ABBABQAAAKgIAHQgGAHgKAAIhSAAQgJAAgHgHQgHgHAAgKIAAgBQAAgKAHgHQAHgHAJAAIBSAAQAKAAAGAHQAIAHAAAKg");
        this.shape.setTransform(0,-6.2);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AgpAZQgJAAgHgHQgHgHABgKIAAgBQgBgKAHgHQAHgGAJgBIBSAAQAKABAGAGQAIAHAAAKIAAABQAAAKgIAHQgGAHgKAAg");
        this.shape_1.setTransform(0,-6.2);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#848484").p("AAtBQIhZAAIAAifIBZAAg");
        this.shape_2.setTransform(0,-16.7);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f("#FFFFFF").s().p("AgrBPIAAieIBYAAIAACeg");
        this.shape_3.setTransform(0,-16.7);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f().s("#848484").ss(0.9,1,1).p("AAwhtIhfAAIAAB+IAjAwIAAAfQAAAFAEAEQAEAFAEAAQAFAAAEgFQADgEAAgFIAAgfIAkgwg");
        this.shape_4.setTransform(-0.3,4.7);

        this.shape_5 = new cjs.Shape();
        this.shape_5.graphics.f("#53AC5D").s().p("AgIBUQgDgEgBgGIAAgfIgjguIAAgnIBfgsIAABTIgjAuIAAAfQgBAGgDAEQgEADgFAAQgEAAgEgDg");
        this.shape_5.setTransform(-0.3,6.9);

        this.shape_6 = new cjs.Shape();
        this.shape_6.graphics.f().s("#848484").ss(0.9,1,1).p("AAwhtIhfAAIAAB+IAjAwIAAAfQAAAFAEAEQAEAFAEAAQAFAAAEgFQADgEAAgFIAAgfIAkgwg");
        this.shape_6.setTransform(-0.3,4.7);

        this.shape_7 = new cjs.Shape();
        this.shape_7.graphics.f("#FFFFFF").s().p("AgIBqQgDgEgBgGIAAgfIgjgwIAAh9IBfAAIAAB9IgjAwIAAAfQgBAGgDAEQgEADgFAAQgEAAgEgDg");
        this.shape_7.setTransform(-0.3,4.7);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_7},{t:this.shape_6},{t:this.shape_5},{t:this.shape_4},{t:this.shape_3},{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).to({state:[]},34).wait(26));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-7.5,-25.7,15,42.4);


    (lib.Pipe_top = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#848484").ss(2.4).p("ADslGInXAAIAAKMIHXAAg");
        this.shape.setTransform(0,32.7);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#DFDFDF").s().p("AjrFHIAAqMIHXAAIAAKMg");
        this.shape_1.setTransform(0,32.7);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-24.8,-1.2,49.7,67.8);


    (lib.Pipe_btm = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#848484").ss(2.4).p("AjpiWIHTAAICLEtIrpAAg");
        this.shape.setTransform(0,15.2);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#DFDFDF").s().p("Al0CXICLktIHTAAICLEtg");
        this.shape_1.setTransform(0,15.2);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-39.2,-1.2,78.4,32.8);


    (lib.outlinebox = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#8EDC91").ss(2).p("ADiDiInDAAIAAnDIHDAAg");

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f().s("#8EDC91").ss(1.2,1,1).p("AAAjhIAAHD");
        this.shape_1.setTransform(-8.8,0);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-23.6,-23.7,47.3,47.3);


    (lib.needle1 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(1.3,1).p("AAAgWIAAAt");

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-1,-3.3,2,6.6);


    (lib.needle_2 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(1.3,1).p("AgIgGIARAN");

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-1.9,-1.7,4,3.5);


    (lib.Lines_02 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#CCCCCC").ss(2.3,1).p("ArXhTIA7CnApMhTIA8CnAKdhTIA7CnAIOhTIA8CnAGDhTIA7CnAD3hTIA7CnABshTIA7CnAgehTIA6CnAiphTIA7CnAnAhTIA7CnAk1hTIA8Cn");
        this.shape.setTransform(72.9,-8.4);

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-1.1,-18,148.1,19.2);


    (lib.line = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#8EDC91").ss(1,0,1).p("AoPAAIQfAA");
        this.shape.setTransform(-34.4,-11.1);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f().s("#8EDC91").ss(3,0,1).p("AoPAAIQfAA");
        this.shape_1.setTransform(-34.4,-8.4);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#FFFFFF").ss(9,0,1).p("AoPAAIQfAA");
        this.shape_2.setTransform(-34.4,-9.9);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-91.7,-14.4,114.6,9);


    (lib.LightBulb = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f("rgba(83,172,93,0.91)").s().p("AgXAYQgKgKAAgOQAAgNAKgJQALgLAMAAQAOAAAJALQALAJAAANQAAANgLALQgJAKgOAAQgMAAgLgKg");
        this.shape.setTransform(3.4,-3.4);

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(30));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(0,-6.8,6.8,6.8);


    (lib.Group_2 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#FFFFFF").s().p("Al/GkIGQvcIFvCVImQPcg");
        this.shape.setTransform(38.5,57);

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(0,0,77,113.9);


    (lib.Group_1 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#FFFFFF").s().p("An1NJILY8BIETBwIrXcBg");
        this.shape.setTransform(50.3,95.4);

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(0,0,100.6,190.7);


    (lib.Group = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#FFFFFF").s().p("AmqNnILY8AIB9AyIrYcBg");
        this.shape.setTransform(42.7,92.3);

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(0,0,85.4,184.6);


    (lib.ClipGroup_6 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 4
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(1,0,1).p("AA6AAQAAAYgRARQgRARgYAAQgXAAgRgRQgRgRAAgYQAAgWARgRQARgSAXAAQAYAAARASQARARAAAWg");
        this.shape.setTransform(6.3,6.3);

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(1));

        // Layer 2 (mask)
        var mask = new cjs.Shape();
        mask._off = true;
        mask.graphics.p("AgoApQgRgRAAgYQAAgWARgRQARgSAXAAQAYAAARASQARARAAAWQAAAYgRARQgRARgYAAQgXAAgRgRg");
        mask.setTransform(6.3,6.3);

        // Layer 3
        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#C5C5C5").s().p("AgoAoQgRgQAAgYQAAgWARgSQARgRAXAAQAYAAARARQARASAAAWQAAAYgRAQQgRASgYgBQgXABgRgSg");
        this.shape_1.setTransform(6.3,11.6);

        this.shape_1.mask = mask;

        this.timeline.addTween(cjs.Tween.get(this.shape_1).wait(1));

        // Layer 1
        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f("#FFFFFF").s().p("AgoApQgRgRAAgYQAAgWARgRQARgSAXAAQAYAAARASQARARAAAWQAAAYgRARQgRARgYAAQgXAAgRgRg");
        this.shape_2.setTransform(6.3,6.3);

        this.timeline.addTween(cjs.Tween.get(this.shape_2).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-0.5,-0.5,13.7,13.7);


    (lib.ClipGroup_3 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 4
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#848484").ss(2,1).p("ACqDDIlTAAIAAmFIFTAAg");
        this.shape.setTransform(54,67.9);

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(1));

        // Layer 2 (mask)
        var mask = new cjs.Shape();
        mask._off = true;
        mask.graphics.p("AioDCIAAmDIFRAAIAAGDg");
        mask.setTransform(54,67.9);

        // Layer 3
        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_1.setTransform(94.9,42.5);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_2.setTransform(88.5,48.9);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_3.setTransform(82,55.3);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_4.setTransform(75.5,61.6);

        this.shape_5 = new cjs.Shape();
        this.shape_5.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_5.setTransform(69.1,68);

        this.shape_6 = new cjs.Shape();
        this.shape_6.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_6.setTransform(62.6,74.3);

        this.shape_7 = new cjs.Shape();
        this.shape_7.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_7.setTransform(56.1,80.7);

        this.shape_8 = new cjs.Shape();
        this.shape_8.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_8.setTransform(49.7,87.1);

        this.shape_9 = new cjs.Shape();
        this.shape_9.graphics.f().s("#848484").ss(1.4).p("AmqmjINVNH");
        this.shape_9.setTransform(43.2,93.4);

        this.shape_1.mask = this.shape_2.mask = this.shape_3.mask = this.shape_4.mask = this.shape_5.mask = this.shape_6.mask = this.shape_7.mask = this.shape_8.mask = this.shape_9.mask = mask;

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_9},{t:this.shape_8},{t:this.shape_7},{t:this.shape_6},{t:this.shape_5},{t:this.shape_4},{t:this.shape_3},{t:this.shape_2},{t:this.shape_1}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(36,47.4,36,41);


    (lib.ClipGroup_2 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 4
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#848484").ss(2,1).p("ACqDDIlTAAIAAmFIFTAAg");
        this.shape.setTransform(54,67.9);

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(1));

        // Layer 2 (mask)
        var mask = new cjs.Shape();
        mask._off = true;
        mask.graphics.p("AipDCIAAmDIFTAAIAAGDg");
        mask.setTransform(54,67.9);

        // Layer 3
        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_1.setTransform(94.9,42.5);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_2.setTransform(88.5,48.9);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f().s("#848484").ss(1.4).p("AmqmjINVNH");
        this.shape_3.setTransform(82,55.3);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_4.setTransform(75.5,61.6);

        this.shape_5 = new cjs.Shape();
        this.shape_5.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_5.setTransform(69.1,68);

        this.shape_6 = new cjs.Shape();
        this.shape_6.graphics.f().s("#848484").ss(1.4).p("AmpmjINUNH");
        this.shape_6.setTransform(62.6,74.3);

        this.shape_7 = new cjs.Shape();
        this.shape_7.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_7.setTransform(56.1,80.7);

        this.shape_8 = new cjs.Shape();
        this.shape_8.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_8.setTransform(49.7,87.1);

        this.shape_9 = new cjs.Shape();
        this.shape_9.graphics.f().s("#848484").ss(1.4).p("AmqmjINVNH");
        this.shape_9.setTransform(43.2,93.4);

        this.shape_1.mask = this.shape_2.mask = this.shape_3.mask = this.shape_4.mask = this.shape_5.mask = this.shape_6.mask = this.shape_7.mask = this.shape_8.mask = this.shape_9.mask = mask;

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_9},{t:this.shape_8},{t:this.shape_7},{t:this.shape_6},{t:this.shape_5},{t:this.shape_4},{t:this.shape_3},{t:this.shape_2},{t:this.shape_1}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(36,47.4,36,41);


    (lib.ClipGroup_1 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 4
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#848484").ss(2,1).p("ACqDDIlTAAIAAmFIFTAAg");
        this.shape.setTransform(54,67.9);

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(1));

        // Layer 2 (mask)
        var mask = new cjs.Shape();
        mask._off = true;
        mask.graphics.p("AipDCIAAmDIFTAAIAAGDg");
        mask.setTransform(54,67.9);

        // Layer 3
        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_1.setTransform(94.9,42.5);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_2.setTransform(88.5,48.9);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f().s("#848484").ss(1.4).p("AmqmjINVNH");
        this.shape_3.setTransform(82,55.3);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_4.setTransform(75.5,61.6);

        this.shape_5 = new cjs.Shape();
        this.shape_5.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_5.setTransform(69.1,68);

        this.shape_6 = new cjs.Shape();
        this.shape_6.graphics.f().s("#848484").ss(1.4).p("AmpmjINUNH");
        this.shape_6.setTransform(62.6,74.3);

        this.shape_7 = new cjs.Shape();
        this.shape_7.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_7.setTransform(56.1,80.7);

        this.shape_8 = new cjs.Shape();
        this.shape_8.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_8.setTransform(49.7,87.1);

        this.shape_9 = new cjs.Shape();
        this.shape_9.graphics.f().s("#848484").ss(1.4).p("AmqmjINVNH");
        this.shape_9.setTransform(43.2,93.4);

        this.shape_1.mask = this.shape_2.mask = this.shape_3.mask = this.shape_4.mask = this.shape_5.mask = this.shape_6.mask = this.shape_7.mask = this.shape_8.mask = this.shape_9.mask = mask;

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_9},{t:this.shape_8},{t:this.shape_7},{t:this.shape_6},{t:this.shape_5},{t:this.shape_4},{t:this.shape_3},{t:this.shape_2},{t:this.shape_1}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(36,47.4,36,41);


    (lib.ClipGroup_0 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 2 (mask)
        var mask = new cjs.Shape();
        mask._off = true;
        mask.graphics.p("AuPFcIAAq3IcfAAIAAK3g");
        mask.setTransform(91.2,104.7);

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#DFDFDF").s().p("AuPFcIAAq3IcfAAIAAK3g");
        this.shape.setTransform(91.2,104.7);

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(0,69.8,182.4,69.8);


    (lib.ClipGroup = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 4
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#848484").ss(2,1).p("ACqDDIlTAAIAAmFIFTAAg");
        this.shape.setTransform(54,67.9);

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(1));

        // Layer 2 (mask)
        var mask = new cjs.Shape();
        mask._off = true;
        mask.graphics.p("AipDCIAAmDIFTAAIAAGDg");
        mask.setTransform(54,67.9);

        // Layer 3
        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_1.setTransform(94.9,42.5);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_2.setTransform(88.5,48.9);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f().s("#848484").ss(1.4).p("AmqmjINVNH");
        this.shape_3.setTransform(82,55.3);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_4.setTransform(75.5,61.6);

        this.shape_5 = new cjs.Shape();
        this.shape_5.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_5.setTransform(69.1,68);

        this.shape_6 = new cjs.Shape();
        this.shape_6.graphics.f().s("#848484").ss(1.4).p("AmpmjINUNH");
        this.shape_6.setTransform(62.6,74.3);

        this.shape_7 = new cjs.Shape();
        this.shape_7.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_7.setTransform(56.1,80.7);

        this.shape_8 = new cjs.Shape();
        this.shape_8.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_8.setTransform(49.7,87.1);

        this.shape_9 = new cjs.Shape();
        this.shape_9.graphics.f().s("#848484").ss(1.4).p("AmqmjINVNH");
        this.shape_9.setTransform(43.2,93.4);

        this.shape_1.mask = this.shape_2.mask = this.shape_3.mask = this.shape_4.mask = this.shape_5.mask = this.shape_6.mask = this.shape_7.mask = this.shape_8.mask = this.shape_9.mask = mask;

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_9},{t:this.shape_8},{t:this.shape_7},{t:this.shape_6},{t:this.shape_5},{t:this.shape_4},{t:this.shape_3},{t:this.shape_2},{t:this.shape_1}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(36,47.4,36,41);


    (lib.ClipGroup_2_1 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 4
        this.shape_10 = new cjs.Shape();
        this.shape_10.graphics.f().s("#848484").ss(2,1).p("ACqDDIlTAAIAAmFIFTAAg");
        this.shape_10.setTransform(54,67.9);

        this.timeline.addTween(cjs.Tween.get(this.shape_10).wait(1));

        // Layer 2 (mask)
        var mask_1 = new cjs.Shape();
        mask_1._off = true;
        mask_1.graphics.p("AioDCIAAmDIFRAAIAAGDg");
        mask_1.setTransform(54,67.9);

        // Layer 3
        this.shape_11 = new cjs.Shape();
        this.shape_11.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_11.setTransform(94.9,42.5);

        this.shape_12 = new cjs.Shape();
        this.shape_12.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_12.setTransform(88.5,48.9);

        this.shape_13 = new cjs.Shape();
        this.shape_13.graphics.f().s("#848484").ss(1.4).p("AmqmjINVNH");
        this.shape_13.setTransform(82,55.3);

        this.shape_14 = new cjs.Shape();
        this.shape_14.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_14.setTransform(75.5,61.6);

        this.shape_15 = new cjs.Shape();
        this.shape_15.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_15.setTransform(69.1,68);

        this.shape_16 = new cjs.Shape();
        this.shape_16.graphics.f().s("#848484").ss(1.4).p("AmpmjINUNH");
        this.shape_16.setTransform(62.6,74.3);

        this.shape_17 = new cjs.Shape();
        this.shape_17.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_17.setTransform(56.1,80.7);

        this.shape_18 = new cjs.Shape();
        this.shape_18.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_18.setTransform(49.7,87.1);

        this.shape_19 = new cjs.Shape();
        this.shape_19.graphics.f().s("#848484").ss(1.4).p("AmqmjINVNH");
        this.shape_19.setTransform(43.2,93.4);

        this.shape_11.mask = this.shape_12.mask = this.shape_13.mask = this.shape_14.mask = this.shape_15.mask = this.shape_16.mask = this.shape_17.mask = this.shape_18.mask = this.shape_19.mask = mask_1;

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_19},{t:this.shape_18},{t:this.shape_17},{t:this.shape_16},{t:this.shape_15},{t:this.shape_14},{t:this.shape_13},{t:this.shape_12},{t:this.shape_11}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(36,47.4,36,41);


    (lib.ClipGroup_1_1 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 4
        this.shape_10 = new cjs.Shape();
        this.shape_10.graphics.f().s("#848484").ss(2,1).p("ACqDDIlTAAIAAmFIFTAAg");
        this.shape_10.setTransform(54,67.9);

        this.timeline.addTween(cjs.Tween.get(this.shape_10).wait(1));

        // Layer 2 (mask)
        var mask_1 = new cjs.Shape();
        mask_1._off = true;
        mask_1.graphics.p("AioDCIAAmDIFRAAIAAGDg");
        mask_1.setTransform(54,67.9);

        // Layer 3
        this.shape_11 = new cjs.Shape();
        this.shape_11.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_11.setTransform(94.9,42.5);

        this.shape_12 = new cjs.Shape();
        this.shape_12.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_12.setTransform(88.5,48.9);

        this.shape_13 = new cjs.Shape();
        this.shape_13.graphics.f().s("#848484").ss(1.4).p("AmqmjINVNH");
        this.shape_13.setTransform(82,55.3);

        this.shape_14 = new cjs.Shape();
        this.shape_14.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_14.setTransform(75.5,61.6);

        this.shape_15 = new cjs.Shape();
        this.shape_15.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_15.setTransform(69.1,68);

        this.shape_16 = new cjs.Shape();
        this.shape_16.graphics.f().s("#848484").ss(1.4).p("AmpmjINUNH");
        this.shape_16.setTransform(62.6,74.3);

        this.shape_17 = new cjs.Shape();
        this.shape_17.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_17.setTransform(56.1,80.7);

        this.shape_18 = new cjs.Shape();
        this.shape_18.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_18.setTransform(49.7,87.1);

        this.shape_19 = new cjs.Shape();
        this.shape_19.graphics.f().s("#848484").ss(1.4).p("AmqmjINVNH");
        this.shape_19.setTransform(43.2,93.4);

        this.shape_11.mask = this.shape_12.mask = this.shape_13.mask = this.shape_14.mask = this.shape_15.mask = this.shape_16.mask = this.shape_17.mask = this.shape_18.mask = this.shape_19.mask = mask_1;

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_19},{t:this.shape_18},{t:this.shape_17},{t:this.shape_16},{t:this.shape_15},{t:this.shape_14},{t:this.shape_13},{t:this.shape_12},{t:this.shape_11}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(36,47.4,36,41);


    (lib.ClipGroup_0_1 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 4
        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f().s("#848484").ss(1.6).p("ABKAAQAAAegWAWQgWAWgeAAQgdAAgWgWQgWgWAAgeQAAgdAWgWQAWgWAdAAQAeAAAWAWQAWAWAAAdg");
        this.shape_1.setTransform(8.3,8.3);

        this.timeline.addTween(cjs.Tween.get(this.shape_1).wait(1));

        // Layer 2 (mask)
        var mask_1 = new cjs.Shape();
        mask_1._off = true;
        mask_1.graphics.p("AgzA0QgWgWAAgeQAAgdAWgWQAVgWAeAAQAeAAAWAWQAWAWAAAdQAAAegWAWQgWAWgeAAQgeAAgVgWg");
        mask_1.setTransform(8.3,8.3);

        // Layer 3
        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f("#848484").s().p("AgzA0QgWgVAAgfQAAgdAWgXQAVgVAeAAQAeAAAWAVQAWAXAAAdQAAAfgWAVQgWAWgeAAQgdAAgWgWg");
        this.shape_2.setTransform(8.3,15.1);

        this.shape_2.mask = mask_1;

        this.timeline.addTween(cjs.Tween.get(this.shape_2).wait(1));

        // Layer 1
        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f("#FFFFFF").s().p("AgzA0QgWgWAAgeQAAgdAWgWQAVgWAeAAQAeAAAWAWQAWAWAAAdQAAAegWAWQgWAWgeAAQgeAAgVgWg");
        this.shape_3.setTransform(8.3,8.3);

        this.timeline.addTween(cjs.Tween.get(this.shape_3).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-0.2,-0.1,16.9,16.8);


    (lib.ClipGroup_4 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 2 (mask)
        var mask_1 = new cjs.Shape();
        mask_1._off = true;
        mask_1.graphics.p("AioDCIAAmDIFRAAIAAGDg");
        mask_1.setTransform(54,67.9);

        // Layer 3
        this.shape_10 = new cjs.Shape();
        this.shape_10.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_10.setTransform(94.9,42.5);

        this.shape_11 = new cjs.Shape();
        this.shape_11.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_11.setTransform(88.5,48.9);

        this.shape_12 = new cjs.Shape();
        this.shape_12.graphics.f().s("#848484").ss(1.4).p("AmqmjINVNH");
        this.shape_12.setTransform(82,55.3);

        this.shape_13 = new cjs.Shape();
        this.shape_13.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_13.setTransform(75.5,61.6);

        this.shape_14 = new cjs.Shape();
        this.shape_14.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_14.setTransform(69.1,68);

        this.shape_15 = new cjs.Shape();
        this.shape_15.graphics.f().s("#848484").ss(1.4).p("AmpmjINUNH");
        this.shape_15.setTransform(62.6,74.3);

        this.shape_16 = new cjs.Shape();
        this.shape_16.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_16.setTransform(56.1,80.7);

        this.shape_17 = new cjs.Shape();
        this.shape_17.graphics.f().s("#848484").ss(1.4).p("AmpmjINTNH");
        this.shape_17.setTransform(49.7,87.1);

        this.shape_18 = new cjs.Shape();
        this.shape_18.graphics.f().s("#848484").ss(1.4).p("AmqmjINVNH");
        this.shape_18.setTransform(43.2,93.4);

        this.shape_10.mask = this.shape_11.mask = this.shape_12.mask = this.shape_13.mask = this.shape_14.mask = this.shape_15.mask = this.shape_16.mask = this.shape_17.mask = this.shape_18.mask = mask_1;

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_18},{t:this.shape_17},{t:this.shape_16},{t:this.shape_15},{t:this.shape_14},{t:this.shape_13},{t:this.shape_12},{t:this.shape_11},{t:this.shape_10}]}).wait(1));

        // Layer 4
        this.shape_19 = new cjs.Shape();
        this.shape_19.graphics.f().s("#848484").ss(2,1).p("ACqDDIlTAAIAAmFIFTAAg");
        this.shape_19.setTransform(54,67.9);

        this.shape_20 = new cjs.Shape();
        this.shape_20.graphics.f("#FFFFFF").s().p("AioDCIAAmDIFRAAIAAGDg");
        this.shape_20.setTransform(54,67.9);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_20},{t:this.shape_19}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(36,47.4,36,41);


    (lib.HandleChupchik = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(2,1,1).p("AAAhHQANAAAKAKQAJAKAAANIAABOQAAANgJAKQgKAJgNAAQgMAAgKgJQgJgKAAgNIAAhOQAAgNAJgKQAKgKAMAAg");
        this.shape.setTransform(0,7.3);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AgWA+QgJgJAAgNIAAhOQAAgNAJgKQAKgKAMAAQANAAAKAKQAJAKAAANIAABOQAAANgJAJQgKAKgNAAQgMAAgKgKg");
        this.shape_1.setTransform(0,7.3);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-4.2,-1,8.6,16.5);


    (lib.Handle_3d = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(1,1,1).p("AgeglQACgEAKAAQAKgBAMAEQAQAEAGAKQAGAJgBARQgCAlg7AD");
        this.shape.setTransform(0.2,4.3);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AgfAnIgBgnQgBgmAEABQADgEAJAAQALgBALAEQAQAEAHAKQAGAJgBARQgDAlg7ADQAAAAgBgBQAAAAAAAAQgBgBAAAAQAAAAAAgBg");
        this.shape_1.setTransform(0,4.3);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-4,-1,8.4,10.6);


    (lib.green_will = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#8EDC91").s().p("AghAiQgOgOAAgUQAAgTAOgOQAOgOATAAQATAAAPAOQAOAOAAATQAAAUgOAOQgPAOgTAAQgTAAgOgOg");

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f().s("#848484").ss(2).p("ABZAAQAAAlgaAZQgbAbgkAAQgjAAgbgbQgagZAAglQAAgkAagaQAbgaAjAAQAkAAAbAaQAaAaAAAkg");

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f("#FFFFFF").s().p("Ag+A/QgagbAAgkQAAgkAagaQAagaAkAAQAkAAAbAaQAaAaAAAkQAAAkgaAbQgbAagkAAQgkAAgagag");

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-9.9,-9.9,19.9,19.9);


    (lib.Gray_Line = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#848484").s().p("AhWALIAAgVICtAAIAAAVg");

        this.timeline.addTween(cjs.Tween.get(this.shape).to({_off:true},15).wait(15));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-8.7,-1.2,17.5,2.3);


    (lib.glass = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#E5E5E5").ss(2,1).p("AgvCWQgYgagMghQgMgjACglQAEhIA4gyQA1gyBLAE");
        this.shape.setTransform(1.6,-11.2);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f().s("#848484").ss(1.5,1,1).p("ADOi5QhNhWhzgGQhxgGhWBNQhWBNgGB0QgGBxBNBWQBOBVBzAGQBxAGBVhNQBWhNAGhzQAGhxhNhWgACTCkQhEA9hZgFQhcgFg9hEQg9hEAFhZQAEhcBEg9QBFg9BZAFQBcAFA9BEQA9BEgFBZQgFBchEA9g");
        this.shape_1.setTransform(10.2,-8.5);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f("#8EDC91").s().p("AgNEWQhzgHhOhVQhMhWAFhxQAGh0BXhMQBVhOBxAHQBzAGBNBVQBOBWgHBxQgGB0hWBNQhPBHhpAAIgOAAgAiSijQhEA9gFBcQgFBaA9BDQA9BFBcAEQBZAFBFg9QBEg9AFhbQAEhag9hEQg9hEhbgFIgMAAQhTAAg/A4g");
        this.shape_2.setTransform(10.2,-8.5);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f("#C5C5C5").s().p("Ag+BzQgQgBgLgMIgVgXIgHgKQgFgMADgMQACgNAKgJICOh/QAMgLAPABQAQAAALALIAWAZQALAMgBARQAAAQgNALIiOB/QgLAKgPAAIgCAAg");
        this.shape_3.setTransform(-25.8,24.8);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f("#C5C5C5").s().p("Ag4ATIBRhJIAgAkIhRBJg");
        this.shape_4.setTransform(-13.5,13.7);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_4},{t:this.shape_3},{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-38,-37.3,77,73.7);


    (lib.forhand = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#848484").s().p("AgsAtQgSgTAAgaQAAgZASgTQATgSAZAAQAaAAATASQASATAAAZQAAAagSATQgTASgaAAQgZAAgTgSg");
        this.shape.setTransform(10.2,13.6);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f().s("#848484").ss(2.2,1,1).p("AiCidIEGE7");
        this.shape_1.setTransform(-3.4,-4.1);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-17.7,-20.9,34.3,40.9);


    (lib.Bulb = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(2,1,1).p("ADNCQQAAAZgSASQgSASgZAAIkfAAQgZAAgSgSQgSgSAAgZIAAkfQAAgZASgSQASgSAZAAIEfAAQAZAAASASQASASAAAZg");
        this.shape.setTransform(20.5,20.5);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AiODNQgaAAgSgSQgRgTgBgYIAAkfQABgZARgSQASgSAaAAIEeAAQAZAAASASQARASABAZIAAEfQgBAYgRATQgSASgZAAg");
        this.shape_1.setTransform(20.5,20.5);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-1,-1,43,43);


    (lib.box_V = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 5
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#FFFFFF").ss(1,1,1).p("AhBh5QAegRAjAAQAtAAAjAZABlhdQAmAnAAA2QAAAngTAfAiDgrQAKgeAYgYQAEgEAEgDAiHAdQgDgOAAgPQAAgJACgKABqBaQgEAEgFAEQgoApg5AAQg4AAgpgpQgUgUgKgX");
        this.shape.setTransform(3.4,-0.5);

        this.timeline.addTween(cjs.Tween.get(this.shape).to({_off:true},22).wait(372));

        // Layer 3
        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AhXgDIAXgTIAqAtIBZhYIAVAVIhwBug");
        this.shape_1.setTransform(3.3,0.6);

        this.timeline.addTween(cjs.Tween.get(this.shape_1).to({_off:true},22).wait(372));

        // Layer 1
        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#2E873B").ss(1.5).p("ADhD4InBAAQgKAAgGgHQgHgGAAgKIAAnBQAAgKAHgGQAGgHAKAAIHBAAQAKAAAGAHQAHAGAAAKIAAHBQAAAKgHAGQgGAHgKAAg");
        this.shape_2.setTransform(0.1,-0.3);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f("#3DB44F").s().p("AAAD5IgEgBQgJgBgCgDIgHgFIgFAAIgGgCIAAnbQAAgFABgCQACgDADgBIA3AAQADABABADQADACAAAFIAAHdQAAAEgDAEQgBACgDAAg");
        this.shape_3.setTransform(-21.3,0.2);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f("#8EDC91").s().p("AjgD4QgKAAgGgHQgHgGAAgKIAAnBQAAgKAHgGQAGgHAKAAIHBAAQAKAAAGAHQAHAGAAAKIAAHBQAAAKgHAGQgGAHgKAAg");
        this.shape_4.setTransform(0.1,-0.3);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_4},{t:this.shape_3},{t:this.shape_2}]}).to({state:[]},22).wait(372));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-25.7,-26.1,51.7,51.7);


    (lib.box = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#2E873B").ss(1.5).p("ADhD4InBAAQgKAAgGgHQgHgGAAgKIAAnBQAAgKAHgGQAGgHAKAAIHBAAQAKAAAGAHQAHAGAAAKIAAHBQAAAKgHAGQgGAHgKAAg");
        this.shape.setTransform(0.1,-0.3);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#3DB44F").s().p("AgrD6QgEAAgEgEQgDgDAAgEIAAneQAAgDADgEQAEgDAEAAIBXAAQAFAAACADQADAEAAADIAAHeQAAAEgDADQgCAEgFAAg");
        this.shape_1.setTransform(-21.4,0.2,0.636,1);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f("#8EDC91").s().p("AjgD4QgKAAgGgHQgHgGAAgKIAAnBQAAgKAHgGQAGgHAKAAIHBAAQAKAAAGAHQAHAGAAAKIAAHBQAAAKgHAGQgGAHgKAAg");
        this.shape_2.setTransform(0.1,-0.3);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).to({state:[]},39).wait(355));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-25.7,-26.1,51.7,51.7);


    (lib.arm = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#848484").ss(2.2,1,1).p("AAyAAQAAAUgPAPQgPAPgUAAQgTAAgPgPQgPgPAAgUQAAgTAPgPQAPgPATAAQAUAAAPAPQAPAPAAATg");
        this.shape.setTransform(-14.2,16.9);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AgiAjQgPgPAAgUQAAgTAPgPQAPgPATAAQAUAAAPAPQAPAPAAATQAAAUgPAPQgPAOgUAAQgTAAgPgOg");
        this.shape_1.setTransform(-14.2,16.9);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#848484").ss(2.2,1,1).p("AilC6QAAAAFLlz");
        this.shape_2.setTransform(2.6,-3.3);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-20.2,-23,40.5,46);


    (lib.Symbol36 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // rectangle
        this.instance = new lib.Symbol37("synched",0);
        this.instance.setTransform(0,5);

        this.timeline.addTween(cjs.Tween.get(this.instance).to({y:-5.2},11,cjs.Ease.get(1)).to({y:4.9},8,cjs.Ease.get(1)).wait(1));

        // line
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(2,1).p("AAAhPIAACf");

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(20));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-2.5,-9,5.1,18.1);


    (lib.Symbol35 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.instance = new lib.Symbol29("synched",0);
        this.instance.setTransform(0.5,0,1,1,0,0,0,0.5,0);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-13.1,-12.5,27.4,25.1);


    (lib.Symbol14 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.instance = new lib.Symbol19("synched",4);
        this.instance.setTransform(14.3,4);

        this.instance_1 = new lib.Symbol19("synched",9);
        this.instance_1.setTransform(7.7,4);

        this.instance_2 = new lib.Symbol19("synched",19);
        this.instance_2.setTransform(1.1,4);

        this.instance_3 = new lib.Symbol19("synched",24);
        this.instance_3.setTransform(-5.5,4);

        this.instance_4 = new lib.Symbol19("synched",0);
        this.instance_4.setTransform(-12.1,4);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.instance_4},{t:this.instance_3},{t:this.instance_2},{t:this.instance_1},{t:this.instance}]}).wait(30));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-15,-14.7,32.3,37.5);


    (lib.Symbol13 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.instance = new lib.Symbol22("synched",24);
        this.instance.setTransform(-6.3,5.9);

        this.instance_1 = new lib.Symbol22("synched",19);
        this.instance_1.setTransform(0.3,5.9);

        this.instance_2 = new lib.Symbol22("synched",9);
        this.instance_2.setTransform(6.9,5.9);

        this.instance_3 = new lib.Symbol22("synched",9);
        this.instance_3.setTransform(-6.3,-0.5);

        this.instance_4 = new lib.Symbol22("synched",4);
        this.instance_4.setTransform(0.3,-0.5);

        this.instance_5 = new lib.Symbol22("synched",0);
        this.instance_5.setTransform(6.9,-0.5);

        this.instance_6 = new lib.Symbol22("synched",24);
        this.instance_6.setTransform(-6.3,-6.9);

        this.instance_7 = new lib.Symbol22("synched",19);
        this.instance_7.setTransform(0.3,-6.9);

        this.instance_8 = new lib.Symbol22("synched",14);
        this.instance_8.setTransform(6.9,-6.9);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.instance_8},{t:this.instance_7},{t:this.instance_6},{t:this.instance_5},{t:this.instance_4},{t:this.instance_3},{t:this.instance_2},{t:this.instance_1},{t:this.instance}]}).wait(60));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-9.2,-9.8,19,18.6);


    (lib.robot_arm = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // cup
        this.instance = new lib.Robot_Head("synched",0);
        this.instance.setTransform(31.8,5.6,1,1,0,0,0,0,2.6);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(38).to({startPosition:0},0).to({regX:0.1,rotation:0.3,x:32.3,y:-14.9},3,cjs.Ease.get(1)).to({y:49.3},5,cjs.Ease.get(-1)).to({startPosition:0},3,cjs.Ease.get(-1)).to({startPosition:0},4).to({rotation:25.8,x:38.3,y:24.9},4,cjs.Ease.get(1)).to({regY:2.7,rotation:-57.4,x:99.5,y:-20.7},2).to({regX:0.2,regY:2.8,rotation:-89.5,x:95.9,y:-47.6},1).to({regY:2.7,rotation:-121.4,x:92,y:-74.2},1).to({regX:0.3,regY:2.9,scaleX:1,scaleY:1,rotation:-117.4,x:94.7,y:-58.2},2).to({regX:0.2,regY:2.8,scaleX:1,scaleY:1,rotation:-92.4,x:106.3,y:-36.1},3).to({regX:0.3,scaleX:1,scaleY:1,rotation:-33.5,x:59.1,y:-1.9},4,cjs.Ease.get(1)).to({regX:0,regY:2.6,scaleX:1,scaleY:1,rotation:5.5,x:28.1,y:5.4},4,cjs.Ease.get(1)).to({rotation:0,x:31.8,y:5.6},3,cjs.Ease.get(1)).to({startPosition:0},3,cjs.Ease.get(1)).wait(1));

        // finger d
        this.instance_1 = new lib.Symbol5("synched",0);
        this.instance_1.setTransform(17.3,18.7,1,1,0,0,0,5.5,-7.2);

        this.timeline.addTween(cjs.Tween.get(this.instance_1).wait(38).to({startPosition:0},0).to({regY:-7.1,rotation:-23.5,x:17.7,y:-1.9},3,cjs.Ease.get(1)).to({rotation:22.9,x:17.6,y:62.3},5,cjs.Ease.get(-1)).to({rotation:-6.3},3,cjs.Ease.get(-1)).to({rotation:-6.3},4).to({regY:-7,rotation:19.2,x:19.4,y:30.4},4,cjs.Ease.get(1)).to({rotation:-93.4,x:102.7,y:-1.4},2).to({regY:-6.9,scaleX:1,scaleY:1,rotation:-130.1,x:108.2,y:-35.3},1).to({regX:5.3,scaleX:1,scaleY:1,rotation:-175.2,x:113.4,y:-69.3},1).to({regX:5.2,regY:-6.7,scaleX:1,scaleY:1,rotation:-170,x:114.1,y:-52.8},2).to({regX:5.5,regY:-6.9,scaleX:1,scaleY:1,rotation:-128.4,x:120,y:-22},3).to({scaleX:1,scaleY:1,rotation:-67.5,x:53.6,y:15.5},4,cjs.Ease.get(1)).to({regY:-7.2,scaleX:1,scaleY:1,rotation:5.5,x:12.4,y:17.1},4,cjs.Ease.get(1)).to({rotation:-20.7,x:17.3,y:18.7},3,cjs.Ease.get(1)).to({rotation:0},3,cjs.Ease.get(1)).wait(1));

        // d
        this.instance_2 = new lib.Symbol25("synched",0);
        this.instance_2.setTransform(0,23.9,1,1,0,0,0,-4.4,-6.2);

        this.timeline.addTween(cjs.Tween.get(this.instance_2).wait(38).to({startPosition:0},0).to({regY:-6.1,rotation:-14,x:3.7,y:9.5},3,cjs.Ease.get(1)).to({regY:-6,rotation:103.6,x:-1.3,y:60.9},5,cjs.Ease.get(-1)).to({regX:-4.3,regY:-6.1,rotation:3,x:0.9,y:69.2},3,cjs.Ease.get(-1)).to({startPosition:0},4).to({regX:-4.2,rotation:92.9,x:-0.7,y:29.4},4,cjs.Ease.get(1)).to({regX:-4.3,regY:-6,rotation:-6.7,x:110.5,y:16.5},2).to({regX:-4.2,scaleX:1,scaleY:1,rotation:-56.6,x:122.2,y:-27.1},1).to({regX:-4.3,scaleX:1,scaleY:1,rotation:-65.9,x:130.8,y:-72.6},1).to({regX:-4,scaleX:1,scaleY:1,rotation:-63.2,x:128.6,y:-54.8},2).to({regX:-4.2,scaleX:1,scaleY:1,rotation:-25.1,x:136.6,y:-11.9},3).to({regY:-5.8,rotation:-45.7,x:51.9,y:33.4},4,cjs.Ease.get(1)).to({regX:-4.5,regY:-6.1,rotation:73.5,x:-5.4,y:20.6},4,cjs.Ease.get(1)).to({regX:-4.4,regY:-6.2,rotation:-5.2,x:3.8,y:30.2},3,cjs.Ease.get(1)).to({rotation:0,x:0,y:23.9},3,cjs.Ease.get(1)).wait(1));

        // finger c
        this.instance_3 = new lib.Symbol4("synched",0);
        this.instance_3.setTransform(26.4,20.2,1,1,0,0,0,4.7,-9.5);

        this.timeline.addTween(cjs.Tween.get(this.instance_3).wait(38).to({startPosition:0},0).to({rotation:-12.7,x:26.7,y:-0.4},3,cjs.Ease.get(1)).to({regX:4.8,regY:-9.3,rotation:23.4,y:63.9},5,cjs.Ease.get(-1)).to({regX:4.7,regY:-9.5,rotation:-7.8,y:63.8},3,cjs.Ease.get(-1)).to({rotation:-7.8},4).to({regX:4.9,regY:-9.3,rotation:30.2,x:27.2,y:35.7},4,cjs.Ease.get(1)).to({regX:4.8,rotation:-82.9,x:108.8,y:-8.3},2).to({scaleX:1,scaleY:1,rotation:-113.8,x:108,y:-42.8},1).to({regY:-9.2,scaleX:1,scaleY:1,rotation:-152.1,x:107.3,y:-77.1},1).to({scaleX:1,scaleY:1,rotation:-146.2,x:109.3,y:-59.8},2).to({scaleX:1,scaleY:1,rotation:-117.9,x:121,y:-31.2},3).to({scaleX:1,scaleY:1,rotation:-56.5,x:61.2,y:11.8},4,cjs.Ease.get(1)).to({regX:4.7,regY:-9.5,scaleX:1,scaleY:1,rotation:5.5,x:21.3,y:19.4},4,cjs.Ease.get(1)).to({regY:-9.4,rotation:-10.5,x:26.5,y:20.2},3,cjs.Ease.get(1)).to({regY:-9.5,rotation:0,x:26.4},3,cjs.Ease.get(1)).wait(1));

        // c
        this.instance_4 = new lib.Symbol25("synched",0);
        this.instance_4.setTransform(19.4,31.9,1,1,0,0,0,-3.6,-5.8);

        this.timeline.addTween(cjs.Tween.get(this.instance_4).wait(38).to({startPosition:0},0).to({regY:-5.7,rotation:-3.5,x:22.5,y:12.5},3,cjs.Ease.get(1)).to({regX:-3.5,regY:-5.6,rotation:83.3,x:16,y:71.3},5,cjs.Ease.get(-1)).to({rotation:20.5,x:22.5,y:76.8},3,cjs.Ease.get(-1)).to({startPosition:0},4).to({regX:-3.4,regY:-5.5,rotation:82.3,x:14.7,y:42.4},4,cjs.Ease.get(1)).to({rotation:-15.9,x:119.8,y:0.3},2).to({scaleX:1,scaleY:1,rotation:-58.6,x:120.7,y:-41.7},1).to({regY:-5.4,scaleX:1,scaleY:1,rotation:-89.1,x:120,y:-84.3},1).to({regX:-3.3,regY:-5.3,scaleX:1,scaleY:1,rotation:-81.1,x:120.8,y:-65.3},2).to({regY:-5.5,scaleX:1,scaleY:1,rotation:-49.6,x:134.9,y:-30.5},3).to({regY:-5.4,rotation:-39.7,x:69.5,y:24.9},4,cjs.Ease.get(1)).to({regX:-3.6,regY:-5.8,rotation:65.2,x:13.2,y:30.4},4,cjs.Ease.get(1)).to({rotation:-4,x:21.7,y:32.9},3,cjs.Ease.get(1)).to({rotation:0,x:19.4,y:31.9},3,cjs.Ease.get(1)).wait(1));

        // finger b
        this.instance_5 = new lib.Symbol3("synched",0);
        this.instance_5.setTransform(38.3,21.3,1,1,0,0,0,-4.2,-8.4);

        this.timeline.addTween(cjs.Tween.get(this.instance_5).wait(38).to({startPosition:0},0).to({regX:-4.1,regY:-8.3,rotation:14.9,x:38.7,y:0.9},3,cjs.Ease.get(1)).to({rotation:-24,x:38.6,y:65},5,cjs.Ease.get(-1)).to({rotation:10.2,y:65.1},3,cjs.Ease.get(-1)).to({rotation:10.2},4).to({regX:-4,rotation:76.2,x:37.4,y:41.9},4,cjs.Ease.get(1)).to({regY:-8.2,rotation:-47.5,x:116.3,y:-17.7},2).to({scaleX:1,scaleY:1,rotation:-77.2,x:109.3,y:-52.8},1).to({regX:-3.9,scaleX:1,scaleY:1,rotation:-126.4,x:102.1,y:-88},1).to({regY:-8,scaleX:1,scaleY:1,rotation:-116.4,x:105.3,y:-69.9},2).to({regY:-8.2,scaleX:1,scaleY:1,rotation:-82.4,x:121.7,y:-43.3},3).to({scaleX:1,scaleY:1,rotation:-50.2,x:71.5,y:6.8},4,cjs.Ease.get(1)).to({regX:-4.2,regY:-8.4,scaleX:1,scaleY:1,rotation:35.9,x:33,y:21.7},4,cjs.Ease.get(1)).to({regX:-4.1,rotation:-8.5,x:38.4,y:21.3},3,cjs.Ease.get(1)).to({regX:-4.2,rotation:0,x:38.3},3,cjs.Ease.get(1)).wait(1));

        // b
        this.instance_6 = new lib.Symbol24("synched",0);
        this.instance_6.setTransform(44.7,31.8,1,1,0,0,0,2.2,-3.7);

        this.timeline.addTween(cjs.Tween.get(this.instance_6).wait(38).to({startPosition:0},0).to({regX:2.3,regY:-3.6,rotation:6.5,x:42.4,y:12.7},3,cjs.Ease.get(1)).to({rotation:-87.6,x:48.9,y:72},5,cjs.Ease.get(-1)).to({regY:-3.5,rotation:-21,x:42.4,y:77.1},3,cjs.Ease.get(-1)).to({startPosition:0},4).to({regX:2.4,rotation:4.5,x:28.2,y:51.3},4,cjs.Ease.get(1)).to({regX:2.3,regY:-3.4,rotation:-88.2,x:128.4,y:-15.6},2).to({scaleX:1,scaleY:1,rotation:-126.9,x:119.7,y:-57.2},1).to({regX:2.2,regY:-3.3,scaleX:1,scaleY:1,rotation:-200.3,x:107.2,y:-101.4},1).to({regX:2.1,scaleX:1,scaleY:1,rotation:-191.6,x:110.3,y:-79.9},2).to({regX:2.2,scaleX:1,scaleY:1,rotation:-158.6,x:132.9,y:-48.4},3).to({regX:2.1,regY:-3.1,scaleX:1,scaleY:1,rotation:-97.7,x:85.7,y:12.2},4,cjs.Ease.get(1)).to({regX:2.2,regY:-3.8,scaleX:1,scaleY:1,rotation:5.5,x:31.9,y:35.1},4,cjs.Ease.get(1)).to({regY:-3.7,rotation:-20,x:45.7,y:32.3},3,cjs.Ease.get(1)).to({rotation:0,x:44.7,y:31.8},3,cjs.Ease.get(1)).wait(1));

        // finger_a
        this.instance_7 = new lib.Symbol2("synched",0);
        this.instance_7.setTransform(45.9,18.4,1,1,0,0,0,-8.1,-7.6);

        this.timeline.addTween(cjs.Tween.get(this.instance_7).wait(38).to({startPosition:0},0).to({regX:-8,regY:-7.5,rotation:15.7,x:46.3,y:-2},3,cjs.Ease.get(1)).to({rotation:-18.7,y:62.2},5,cjs.Ease.get(-1)).to({regX:-7.9,rotation:2.3,x:46.4},3,cjs.Ease.get(-1)).to({rotation:2.3},4).to({regX:-7.8,rotation:104.5,x:43.8,y:46.2},4,cjs.Ease.get(1)).to({regX:-7.9,regY:-7.4,rotation:-19.7,x:121.5,y:-25.5},2).to({regY:-7.5,scaleX:1,scaleY:1,rotation:-59.2,x:108.6,y:-59.4},1).to({regY:-7.3,scaleX:1,scaleY:1,rotation:-120.1,x:95.8,y:-93},1).to({regY:-7.2,scaleX:1,scaleY:1,rotation:-101.8,x:101.2,y:-75.2},2).to({regX:-7.8,regY:-7.4,scaleX:1,scaleY:1,rotation:-54.6,x:121.5,y:-52.6},3).to({regY:-7.3,rotation:-45.3,x:76.9,y:0.8},4,cjs.Ease.get(1)).to({regX:-8,regY:-7.5,rotation:36.4,x:40.9,y:19.6},4,cjs.Ease.get(1)).to({rotation:-7.2,x:46,y:18.5},3,cjs.Ease.get(1)).to({regX:-8.1,regY:-7.6,rotation:0,x:45.9,y:18.4},3,cjs.Ease.get(1)).wait(1));

        // a
        this.instance_8 = new lib.Symbol24("synched",0);
        this.instance_8.setTransform(66,24.4,1,1,0,0,0,4.3,-5.8);

        this.timeline.addTween(cjs.Tween.get(this.instance_8).wait(38).to({startPosition:0},0).to({regY:-5.7,rotation:0.3,x:63.9,y:9.2},3,cjs.Ease.get(1)).to({rotation:-107.3,x:67.2,y:61.4},5,cjs.Ease.get(-1)).to({regX:4.4,rotation:-6.3,x:66.2,y:68.9},3,cjs.Ease.get(-1)).to({rotation:-6.3},4).to({regX:4.5,rotation:2.5,x:33,y:64.4},4,cjs.Ease.get(1)).to({regY:-5.5,rotation:-76.7,x:142.7,y:-26.8},2).to({regX:4.4,scaleX:1,scaleY:1,rotation:-120.8,x:121.7,y:-70.5},1).to({scaleX:1,scaleY:1,rotation:-235.8,x:90,y:-113.4},1).to({regX:4.1,scaleX:1,scaleY:1,rotation:-214.7,x:101.2,y:-90.5},2).to({regX:4.5,regY:-5.4,scaleX:1,scaleY:1,rotation:-163.6,x:137.2,y:-66.1},3).to({regY:-5.3,scaleX:1,scaleY:1,rotation:-114,x:97.9,y:-9.3},4,cjs.Ease.get(1)).to({regX:4.3,regY:-5.8,scaleX:1,scaleY:1,rotation:5.5,x:53.3,y:37.7},4,cjs.Ease.get(1)).to({rotation:-19,x:66.8,y:22.3},3,cjs.Ease.get(1)).to({rotation:0,x:66,y:24.4},3,cjs.Ease.get(1)).wait(1));

        // will
        this.instance_9 = new lib.will("synched",0);
        this.instance_9.setTransform(30.8,-33.5);

        this.timeline.addTween(cjs.Tween.get(this.instance_9).wait(38).to({startPosition:0},0).to({regX:0.1,regY:-0.1,rotation:-12.2,x:31.3,y:-54},3,cjs.Ease.get(1)).to({rotation:23.7,x:31.2,y:7.2},5,cjs.Ease.get(-1)).to({scaleX:1,scaleY:1,rotation:23.6,x:31.3},3).to({startPosition:0},4).to({scaleX:1,scaleY:1,rotation:9.5,x:54.3,y:-14.1},4,cjs.Ease.get(1)).to({rotation:-6.5,x:62,y:-41.7},2).to({scaleX:1,scaleY:1,rotation:-10.3,x:60.8,y:-47.8},1).to({scaleX:1,scaleY:1,rotation:-14.1,x:59.4,y:-53.9},1).to({scaleX:1,scaleY:1,rotation:-14.5,x:60.6,y:-46.1},2).to({scaleX:1,scaleY:1,rotation:-2,x:62.4,y:-34.3},3).to({regX:0.2,rotation:6.4,x:39,y:-31},4,cjs.Ease.get(1)).to({regX:0,regY:0,rotation:0,x:30.8,y:-33.5},4,cjs.Ease.get(1)).to({startPosition:0},3,cjs.Ease.get(1)).to({startPosition:0},3,cjs.Ease.get(1)).wait(1));

        // robotic_forarm
        this.instance_10 = new lib.robotic_forarm("synched",0);
        this.instance_10.setTransform(31.5,-34.3,1,1,0,0,0,0,-20.4);

        this.timeline.addTween(cjs.Tween.get(this.instance_10).wait(38).to({startPosition:0},0).to({regX:0.1,rotation:0.3,x:32.1,y:-54.7},3,cjs.Ease.get(1)).to({y:8.1},5,cjs.Ease.get(-1)).to({x:32},3).to({startPosition:0},4).to({rotation:25.8,x:55.9,y:-12.4},4,cjs.Ease.get(1)).to({regX:0,regY:-20.6,rotation:-57.4,x:64.3,y:-42.6},2).to({regY:-20.4,rotation:-89.5,x:60.8,y:-47.4},1).to({regX:0.1,regY:-20.6,rotation:-121.4,x:56.9,y:-52.3},1).to({regY:-20.4,scaleX:1,scaleY:1,rotation:-108.7,x:59.8,y:-45.6},2).to({regX:0,regY:-20.5,scaleX:1,scaleY:1,rotation:-92.4,x:64.9,y:-33.8},3).to({regX:0.1,regY:-20.6,scaleX:1,scaleY:1,rotation:-33.5,x:38.8,y:-32.4},4,cjs.Ease.get(1)).to({regY:-20.5,scaleX:1,scaleY:1,rotation:5.5,x:31.6,y:-34.3},4,cjs.Ease.get(1)).to({regX:0,regY:-20.4,rotation:0,x:31.5},3,cjs.Ease.get(1)).to({startPosition:0},3,cjs.Ease.get(1)).wait(1));

        // robotic_arm
        this.instance_11 = new lib.robotic_arm("synched",0);
        this.instance_11.setTransform(-65.1,-33.2,1,1,0,0,0,-46,0.2);

        this.timeline.addTween(cjs.Tween.get(this.instance_11).wait(38).to({startPosition:0},0).to({regY:0.1,rotation:-12.2,x:-62.4,y:-33.3},3,cjs.Ease.get(1)).to({regX:-46.1,rotation:23.7,x:-56.8,y:-31},5,cjs.Ease.get(-1)).to({scaleX:1,scaleY:1,rotation:23.6,x:-56.9},3).to({startPosition:0},4).to({scaleX:1,scaleY:1,rotation:9.5,x:-40.3,y:-29.6},4,cjs.Ease.get(1)).to({rotation:-6.5,x:-33.3,y:-30.5},2).to({scaleX:1,scaleY:1,rotation:-10.3,x:-33.6,y:-30.4},1).to({regY:-0.1,scaleX:1,scaleY:1,rotation:-14.1,y:-30.3},1).to({regY:0.1,rotation:-2,x:-33.4,y:-30.4},5).to({regX:-46,regY:0.2,rotation:0,x:-65.1,y:-33.2},8,cjs.Ease.get(1)).to({startPosition:0},3,cjs.Ease.get(1)).to({startPosition:0},3,cjs.Ease.get(1)).wait(1));

        // Line
        this.instance_12 = new lib.wire("synched",0);
        this.instance_12.setTransform(12.7,-30.5,1,1,0,0,0,21.6,-21.2);

        this.timeline.addTween(cjs.Tween.get(this.instance_12).wait(38).to({startPosition:0},0).to({regY:-21.3,rotation:0.3,x:13.3,y:-51.1},3,cjs.Ease.get(1)).to({y:-1.1},5,cjs.Ease.get(-1)).to({regX:21.5,regY:-21.2,x:13.2},3).to({startPosition:0},4).to({regX:21.6,regY:-21.3,x:13.3,y:-20.3},4,cjs.Ease.get(1)).to({y:-31.5},2).to({y:-36.5},1).to({y:-41.6},1).to({y:-31.5},5).to({regX:21.5,rotation:0,x:6.8,y:-29.4},4,cjs.Ease.get(1)).to({regX:21.6,regY:-21.2,x:12.7,y:-30.5},4,cjs.Ease.get(1)).to({startPosition:0},3,cjs.Ease.get(1)).to({startPosition:0},3,cjs.Ease.get(1)).wait(1));

        // box
        this.instance_13 = new lib.box("synched",7);
        this.instance_13.setTransform(32.8,66.9,1,1,0,0,0,0,-25.2);
        this.instance_13._off = true;

        this.timeline.addTween(cjs.Tween.get(this.instance_13).wait(46).to({_off:false},0).wait(3).to({startPosition:9},0).to({startPosition:9},4).to({regX:0.3,rotation:34,x:11.5,y:47.8,startPosition:11},4,cjs.Ease.get(1)).to({regX:0.2,regY:-24.9,rotation:-37.4,x:125.9,y:-8.8,startPosition:13},2).to({rotation:-87.5,x:119.9,y:-49,startPosition:14},1).to({regX:0.3,scaleX:1.54,scaleY:0.87,rotation:-131.5,x:67.1,y:-166.5,startPosition:15},1).to({scaleX:1,scaleY:1,rotation:-213.6,x:49.5,y:-192.2,startPosition:16},1).to({_off:true},1).wait(18));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-75.5,-41.6,146.1,86.1);


    (lib.pipe = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Symbol 7
        this.instance = new lib.Symbol7("synched",0);
        this.instance.setTransform(0.5,-9.4,1,1,0,0,0,0,2.8);

        this.timeline.addTween(cjs.Tween.get(this.instance).to({regX:0.1,regY:2.7,scaleX:1.17,scaleY:0.91,x:0.7,y:-16.7},6,cjs.Ease.get(1)).to({regX:0.2,regY:3,scaleX:0.7,scaleY:1.28,x:0.6,y:14.1},2,cjs.Ease.get(1)).to({regX:0.1,regY:2.6,scaleX:1.07,scaleY:0.96,y:-13.1},3,cjs.Ease.get(1)).to({regX:0,regY:2.8,scaleX:1,scaleY:1,x:0.5,y:-9.4},3,cjs.Ease.get(1)).wait(46));

        // Symbol 7
        this.instance_1 = new lib.Symbol7("synched",0);
        this.instance_1.setTransform(0.5,43.6,1,1,0,0,0,0,2.8);

        this.timeline.addTween(cjs.Tween.get(this.instance_1).to({regX:0.1,regY:2.9,scaleX:1.17,scaleY:0.91,x:0.7,y:31.8},6,cjs.Ease.get(1)).to({regX:0.2,regY:3,scaleX:0.7,scaleY:1.28,x:0.6,y:82.1},2,cjs.Ease.get(1)).to({regX:0.1,regY:2.9,scaleX:1.07,scaleY:0.96,y:37.6},3,cjs.Ease.get(1)).to({regX:0,regY:2.8,scaleX:1,scaleY:1,x:0.5,y:43.6},3,cjs.Ease.get(1)).wait(46));

        // Symbol 7
        this.instance_2 = new lib.Symbol7("synched",0);
        this.instance_2.setTransform(0.5,32.6,1,1,0,0,0,0,2.8);

        this.timeline.addTween(cjs.Tween.get(this.instance_2).to({regX:0.1,regY:2.9,scaleX:1.17,scaleY:0.91,x:0.7,y:21.7},6,cjs.Ease.get(1)).to({regX:0.2,regY:3,scaleX:0.7,scaleY:1.28,x:0.6,y:68},2,cjs.Ease.get(1)).to({regX:0.1,scaleX:1.07,scaleY:0.96,y:27.1},3,cjs.Ease.get(1)).to({regX:0,regY:2.8,scaleX:1,scaleY:1,x:0.5,y:32.6},3,cjs.Ease.get(1)).wait(46));

        // Symbol 7
        this.instance_3 = new lib.Symbol7("synched",0);
        this.instance_3.setTransform(0.5,21.6,1,1,0,0,0,0,2.8);

        this.timeline.addTween(cjs.Tween.get(this.instance_3).to({regX:0.1,regY:2.9,scaleX:1.17,scaleY:0.91,x:0.7,y:11.7},6,cjs.Ease.get(1)).to({regX:0.2,regY:3,scaleX:0.7,scaleY:1.28,x:0.6,y:53.8},2,cjs.Ease.get(1)).to({regX:0.1,scaleX:1.07,scaleY:0.96,y:16.6},3,cjs.Ease.get(1)).to({regX:0,regY:2.8,scaleX:1,scaleY:1,x:0.5,y:21.6},3,cjs.Ease.get(1)).wait(46));

        // Symbol 7
        this.instance_4 = new lib.Symbol7("synched",0);
        this.instance_4.setTransform(0.5,0.6,1,1,0,0,0,0,2.8);

        this.timeline.addTween(cjs.Tween.get(this.instance_4).to({regX:0.1,scaleX:1.17,scaleY:0.91,x:0.7,y:-7.5},6,cjs.Ease.get(1)).to({regX:0.2,regY:2.9,scaleX:0.7,scaleY:1.28,x:0.6,y:26.8},2,cjs.Ease.get(1)).to({regX:0.1,regY:2.6,scaleX:1.07,scaleY:0.96,y:-3.6},3,cjs.Ease.get(1)).to({regX:0,regY:2.8,scaleX:1,scaleY:1,x:0.5,y:0.6},3,cjs.Ease.get(1)).wait(46));

        // Symbol 7
        this.instance_5 = new lib.Symbol7("synched",0);
        this.instance_5.setTransform(0.5,11.1,1,1,0,0,0,0,2.8);

        this.timeline.addTween(cjs.Tween.get(this.instance_5).to({regX:0.1,scaleX:1.17,scaleY:0.91,x:0.7,y:2.1},6,cjs.Ease.get(1)).to({regX:0.2,regY:3,scaleX:0.7,scaleY:1.28,x:0.6,y:40.4},2,cjs.Ease.get(1)).to({regX:0.1,scaleX:1.07,scaleY:0.96,y:6.6},3,cjs.Ease.get(1)).to({regX:0,regY:2.8,scaleX:1,scaleY:1,x:0.5,y:11.1},3,cjs.Ease.get(1)).wait(46));

        // Symbol 7
        this.instance_6 = new lib.Symbol7("synched",0);
        this.instance_6.setTransform(0.5,-9.4,1,1,0,0,0,0,2.8);

        this.timeline.addTween(cjs.Tween.get(this.instance_6).to({regX:0.1,regY:2.7,scaleX:1.17,scaleY:0.91,x:0.7,y:-16.7},6,cjs.Ease.get(1)).to({regX:0.2,regY:3,scaleX:0.7,scaleY:1.28,x:0.6,y:14.1},2,cjs.Ease.get(1)).to({regX:0.1,regY:2.6,scaleX:1.07,scaleY:0.96,y:-13.1},3,cjs.Ease.get(1)).to({regX:0,regY:2.8,scaleX:1,scaleY:1,x:0.5,y:-9.4},3,cjs.Ease.get(1)).wait(46));

        // Symbol 7
        this.instance_7 = new lib.Symbol7("synched",0);
        this.instance_7.setTransform(0.5,-19.4,1,1,0,0,0,0,2.8);

        this.timeline.addTween(cjs.Tween.get(this.instance_7).to({regX:0.1,scaleX:1.17,scaleY:0.91,x:0.7,y:-25.7},6,cjs.Ease.get(1)).to({regX:0.2,regY:2.6,scaleX:0.7,scaleY:1.28,x:0.6,y:1.2},2,cjs.Ease.get(1)).to({regX:0.1,scaleX:1.07,scaleY:0.96,y:-22.7},3,cjs.Ease.get(1)).to({regX:0,regY:2.8,scaleX:1,scaleY:1,x:0.5,y:-19.4},3,cjs.Ease.get(1)).wait(46));

        // Layer 3
        this.instance_8 = new lib.Pipe_btm("synched",0);
        this.instance_8.setTransform(0.4,55.2,1,1,0,0,0,0,15.1);

        this.timeline.addTween(cjs.Tween.get(this.instance_8).to({regY:15.2,scaleX:1.17,scaleY:0.91,x:0.5,y:42.4},6,cjs.Ease.get(1)).to({regY:15.3,scaleX:0.7,scaleY:1.28,x:0.4,y:97},2,cjs.Ease.get(1)).to({regX:0.1,regY:15.2,scaleX:1.07,scaleY:0.96,x:0.5,y:48.7},3,cjs.Ease.get(1)).to({regX:0,regY:15.1,scaleX:1,scaleY:1,x:0.4,y:55.2},3,cjs.Ease.get(1)).wait(46));

        // Layer 44
        this.instance_9 = new lib.Pipe_top("synched",0);
        this.instance_9.setTransform(0,-59.5,1,1,0,0,0,0,32.7);

        this.timeline.addTween(cjs.Tween.get(this.instance_9).to({regY:32.6,scaleX:1.17,scaleY:0.91,y:-62.4},6,cjs.Ease.get(1)).to({regX:0.1,scaleX:0.7,scaleY:1.28,x:0.2,y:-50.2},2,cjs.Ease.get(1)).to({regY:32.5,scaleX:1.07,scaleY:0.96,x:0.1,y:-61.1},3,cjs.Ease.get(1)).to({regX:0,regY:32.7,scaleX:1,scaleY:1,x:0,y:-59.5},3,cjs.Ease.get(1)).wait(46));

        // Layer 45
        this.instance_10 = new lib.Symbol9("synched",0);
        this.instance_10.setTransform(0.4,70.4,1,1,0,0,0,0.1,-9.7);

        this.timeline.addTween(cjs.Tween.get(this.instance_10).to({scaleX:1.17,scaleY:0.91,y:56.1},6,cjs.Ease.get(1)).to({regX:0.2,regY:-9.5,scaleX:0.7,scaleY:1.28,y:116.5},2,cjs.Ease.get(1)).to({regY:-9.6,scaleX:1.07,scaleY:0.96,y:63.2},3,cjs.Ease.get(1)).to({regX:0.1,regY:-9.7,scaleX:1,scaleY:1,y:70.4},3,cjs.Ease.get(1)).wait(46));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-46.3,-93.4,93.3,184.4);


    (lib.pepita_move = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // pipeta 01
        this.instance = new lib.pipeta("synched",0);
        this.instance.setTransform(-38.2,-70.6,1,1,0,0,0,0.8,-23.3);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(1).to({regX:0,regY:-4.5,x:-39,y:-51.8,startPosition:1},0).wait(1).to({regX:0.8,regY:-23.3,x:-38.2,y:-70.6,startPosition:0},0).wait(1).to({regX:0,regY:-4.5,x:-39,y:-46.3,startPosition:1},0).wait(1).to({y:-34.1,startPosition:2},0).wait(1).to({y:-23.2,startPosition:3},0).wait(1).to({regX:0.8,regY:-23.3,x:-38.2,y:-37.8,startPosition:0},0).wait(1).to({regX:0,regY:-4.5,x:-39,y:-19,startPosition:1},0).wait(1).to({startPosition:2},0).wait(1).to({startPosition:3},0).wait(1).to({startPosition:4},0).wait(1).to({startPosition:5},0).wait(1).to({regX:0.8,regY:-23.3,x:-38.2,y:-37.8,startPosition:0},0).wait(1).to({regX:0,regY:-4.5,x:-39,y:-19.2,startPosition:1},0).wait(1).to({y:-19.9,startPosition:2},0).wait(1).to({y:-21.2,startPosition:3},0).wait(1).to({y:-23,startPosition:4},0).wait(1).to({y:-25.6,startPosition:5},0).wait(1).to({y:-28.8,startPosition:6},0).wait(1).to({y:-32.6,startPosition:7},0).wait(1).to({y:-36.8,startPosition:8},0).wait(1).to({y:-41,startPosition:9},0).wait(1).to({y:-44.9,startPosition:10},0).wait(1).to({y:-48,startPosition:11},0).wait(1).to({y:-50.2,startPosition:12},0).wait(1).to({y:-51.4,startPosition:13},0).wait(1).to({regX:0.8,regY:-23.3,x:-38.2,y:-70.6,startPosition:0},0).wait(34));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-46.5,-73,15,42.4);


    (lib.movingline = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // will
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#8EDC91").s().p("AghAiQgOgOAAgUQAAgTAOgOQAPgOASAAQAUAAAOAOQAOAOAAATQAAAUgOAOQgOAOgUAAQgSAAgPgOg");
        this.shape.setTransform(-149.5,0);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f().s("#888888").ss(1.6).p("ABZAAQAAAkgaAbQgaAaglAAQgkAAgagaQgagbAAgkQAAgjAagbQAagaAkAAQAlAAAaAaQAaAbAAAjg");
        this.shape_1.setTransform(-149.5,0);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f("#FFFFFF").s().p("Ag+A/QgagbAAgkQAAgjAagbQAbgaAjAAQAlAAAaAaQAaAbAAAjQAAAkgaAbQgaAaglAAQgjAAgbgag");
        this.shape_2.setTransform(-149.5,0);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(9));

        // mask (mask)
        var mask = new cjs.Shape();
        mask._off = true;
        mask.graphics.p("A3WBaQglgBgagaQgagZAAgmIAAAAQAAgkAagaQAagbAlAAMAuuAAAQAlAAAZAbQAaAaAAAkIAAAAQAAAmgaAZQgaAagkABg");

        // lines
        this.instance = new lib.strips("synched",0);
        this.instance.setTransform(-8.6,-0.4,1,1,0,0,0,0,8.3);

        this.instance.mask = mask;

        this.timeline.addTween(cjs.Tween.get(this.instance).to({x:4.4},8).wait(1));

        // mask copy
        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f().s("#888888").ss(2).p("AYxABQAAAlgbAZQgZAbglAAMguvAAAQgkAAgbgbQgagZAAglIAAgBQAAglAagZQAagbAlAAMAuvAAAQAlAAAZAbQAbAZAAAlg");

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f("#FFFFFF").s().p("A3XBaQgkAAgbgaQgagbAAgkIAAgBQAAglAagaQAagZAlAAMAuvAAAQAlAAAaAZQAaAaAAAlIAAABQAAAkgaAbQgaAaglAAg");

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_4},{t:this.shape_3}]}).wait(9));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-163.6,-10,323.2,20);


    (lib.moving_line = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 2 (mask)
        var mask = new cjs.Shape();
        mask._off = true;
        mask.graphics.p("A4SBXQAogCAZgbQAWgYABgiQACgggWgYQgYgbgsgDMAwlAAAIAACtg");
        mask.setTransform(5.7,-0.3);

        // Layer 1
        this.instance = new lib.Tween24("synched",0);
        this.instance.setTransform(6.2,-0.3);

        this.instance.mask = mask;

        this.timeline.addTween(cjs.Tween.get(this.instance).to({x:-7.3},8).wait(1));

        // Layer 3
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#8EDC91").s().p("AghAiQgOgOAAgUQAAgTAOgOQAPgOASAAQAUAAAOAOQAOAOAAATQAAAUgOAOQgOAOgUAAQgSAAgPgOg");
        this.shape.setTransform(-149.5,0);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f().s("#888888").ss(1.6).p("ABZAAQAAAkgaAbQgaAaglAAQgkAAgagaQgagbAAgkQAAgjAagbQAagaAkAAQAlAAAaAaQAaAbAAAjg");
        this.shape_1.setTransform(-149.5,0);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#888888").ss(2).p("AYxABQAAAlgbAZQgZAbglAAMguvAAAQgkAAgbgbQgagZAAglIAAgBQAAglAagZQAagbAlAAMAuvAAAQAlAAAZAbQAbAZAAAlg");

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f("#FFFFFF").s().p("A3XBaQgkAAgbgaQgagbAAgkIAAgBQAAglAagaQAagZAlAAMAuvAAAQAlAAAaAZQAaAaAAAlIAAABQAAAkgaAbQgaAaglAAg");

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_3},{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(9));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-163.6,-10,323.2,20);


    (lib.moving_linecopy = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // green_will
        this.instance = new lib.green_will("synched",0);
        this.instance.setTransform(61.6,0.3);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(15));

        // mask (mask)
        var mask = new cjs.Shape();
        mask._off = true;
        mask.graphics.p("AphBaQglgBgagaQgagZAAgmIAAAAQAAgkAagaQAZgbAmAAITEAAQAlAAAZAbQAaAaAAAkIAAAAQAAAmgaAZQgZAaglABg");

        // lines
        this.instance_1 = new lib.Lines_02("synched",0);
        this.instance_1.setTransform(-11.5,-0.1,1,1,0,0,0,72.9,-8.4);

        this.instance_1.mask = mask;

        this.timeline.addTween(cjs.Tween.get(this.instance_1).to({x:0.1},14).wait(1));

        // Layer 8
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#848484").ss(2).p("AK8ABQAAAlgbAZQgaAbgkAAIzFAAQgkAAgagbQgbgZAAglIAAgBQAAglAbgZQAagbAkAAITFAAQAlAAAZAbQAbAZAAAlg");

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("ApiBZQgkAAgbgZQgagbAAgkIAAgBQAAglAagaQAbgaAkAAITFAAQAlAAAZAaQAbAaAAAlIAAABQAAAkgbAbQgaAZgkAAg");

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_1},{t:this.shape}]}).wait(15));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-75.2,-10,146.8,20.3);


    (lib.machine_05_springboard = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.instance = new lib.moving_linecopy("synched",0);
        this.instance.setTransform(61.8,0,1,1,0,0,0,62.1,0.1);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(15));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-75.5,-10.1,146.8,20.3);


    (lib.machine_02_X = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{check:22,raise:51});

        // Layer 23
        this.instance = new lib.Tween3("synched",0);
        this.instance.setTransform(0,-23.5);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(60));

        // arm
        this.instance_1 = new lib.arm("synched",0);
        this.instance_1.setTransform(-29.5,-40.6,1,1,7.5,0,0,16.4,-17.6);

        this.timeline.addTween(cjs.Tween.get(this.instance_1).wait(15).to({startPosition:0},0).to({rotation:16.5,x:-30,y:-41.2},4).to({rotation:-14.5,x:-32.1,y:-35.9},7,cjs.Ease.get(1)).wait(1).to({regX:0,regY:0,rotation:-10.6,x:-45.1,y:-15.6},0).wait(1).to({rotation:-6.6,x:-46.4,y:-16.6},0).wait(1).to({rotation:-2.7,x:-47.8,y:-17.6},0).wait(1).to({rotation:1.3,x:-49,y:-18.7},0).wait(1).to({rotation:5.2,x:-50.2,y:-19.9},0).wait(1).to({rotation:9.1,x:-51.3,y:-21.1},0).wait(1).to({rotation:13.1,x:-52.3,y:-22.5},0).wait(1).to({rotation:17,x:-53.2,y:-23.9},0).wait(1).to({rotation:20.9,x:-54,y:-25.3},0).wait(1).to({rotation:24.9,x:-54.7,y:-26.8},0).wait(1).to({rotation:28.8,x:-55.3,y:-28.3},0).wait(1).to({regX:16.1,regY:-17.8,rotation:32.7,x:-32.5,y:-35.8},0).wait(1).to({regX:0,regY:0,rotation:33.1,x:-55.4,y:-30},0).wait(1).to({rotation:34.1,x:-54.9,y:-31.3},0).wait(1).to({rotation:35.3,x:-54.2,y:-32.9},0).wait(1).to({rotation:36.3,x:-53.6,y:-34.3},0).wait(1).to({rotation:37,x:-53.2,y:-35.2},0).wait(1).to({regX:16.3,regY:-17.6,rotation:37.2,x:-29.4,y:-39.8},0).to({startPosition:0},6,cjs.Ease.get(1)).wait(1).to({regX:0,regY:0,rotation:31,x:-52.5,y:-33.3},0).wait(1).to({rotation:25.5,x:-51.7,y:-31.3},0).wait(1).to({rotation:20.7,x:-50.9,y:-29.6},0).wait(1).to({rotation:16.7,x:-50.1,y:-28.2},0).wait(1).to({rotation:13.4,x:-49.4,y:-27.1},0).wait(1).to({rotation:10.8,x:-48.8,y:-26.3},0).wait(1).to({rotation:9,x:-48.3,y:-25.7},0).wait(1).to({rotation:7.9,x:-48,y:-25.4},0).wait(1).to({regX:16.4,regY:-17.6,rotation:7.5,x:-29.5,y:-40.6},0).wait(1));

        // forhand
        this.instance_2 = new lib.forhand("synched",0);
        this.instance_2.setTransform(-64.8,-8.7,1,1,4.6,0,0,-16.4,-18.7);

        this.timeline.addTween(cjs.Tween.get(this.instance_2).wait(15).to({startPosition:0},0).to({rotation:13.3,x:-68.2,y:-12.7},4).to({regX:-16.5,scaleX:1,scaleY:1,rotation:9.8,x:-51.8,y:8.8},7,cjs.Ease.get(1)).wait(1).to({regX:-0.6,regY:-0.5,rotation:12,x:-42.4,y:27.2},0).wait(1).to({rotation:14.2,x:-45.5,y:24.9},0).wait(1).to({rotation:16.3,x:-48.6,y:22.6},0).wait(1).to({rotation:18.5,x:-51.8,y:20.3},0).wait(1).to({rotation:20.7,x:-54.9,y:18},0).wait(1).to({scaleX:1,scaleY:1,rotation:22.8,x:-58.1,y:15.6},0).wait(1).to({rotation:25,x:-61.3,y:13.2},0).wait(1).to({rotation:27.2,x:-64.5,y:10.8},0).wait(1).to({rotation:29.4,x:-67.7,y:8.2},0).wait(1).to({rotation:31.5,x:-70.9,y:5.8},0).wait(1).to({rotation:33.7,x:-74.1,y:3.2},0).wait(1).to({regX:-16.6,regY:-18.6,rotation:35.9,x:-79.6,y:-23.4},0).wait(1).to({regX:-0.6,regY:-0.5,rotation:28.1,x:-73.5,y:-0.5},0).wait(1).to({rotation:7.5,x:-64.1,y:-5.3},0).wait(1).to({rotation:-18.3,x:-54.9,y:-14.9},0).wait(1).to({rotation:-40.7,x:-50.2,y:-25.3},0).wait(1).to({rotation:-54.9,x:-49.1,y:-32.2},0).wait(1).to({regX:-16.4,regY:-18.8,rotation:-59.5,x:-72.7,y:-29.9},0).to({startPosition:0},6,cjs.Ease.get(1)).wait(1).to({regX:-0.6,regY:-0.5,rotation:-46.1,x:-46.9,y:-24.1},0).wait(1).to({rotation:-34.2,x:-46.2,y:-15.2},0).wait(1).to({rotation:-23.9,x:-46.4,y:-7.8},0).wait(1).to({rotation:-15.2,x:-47.2,y:-1.8},0).wait(1).to({rotation:-8.1,x:-48.1,y:3},0).wait(1).to({rotation:-2.6,x:-49,y:6.5},0).wait(1).to({rotation:1.4,x:-49.8,y:8.9},0).wait(1).to({rotation:3.8,x:-50.2,y:10.3},0).wait(1).to({regX:-16.4,regY:-18.7,rotation:4.6,x:-64.8,y:-8.7},0).wait(1));

        // glass
        this.instance_3 = new lib.glass("synched",0);
        this.instance_3.setTransform(-40.8,25.8,1,1,-0.1,0,0,-26,24.7);

        this.timeline.addTween(cjs.Tween.get(this.instance_3).wait(15).to({startPosition:0},0).to({x:-50.8},4).to({regY:24.8,scaleX:1,scaleY:1,rotation:29.8,x:-28.8,y:45.9},7,cjs.Ease.get(1)).wait(1).to({regX:0.4,regY:-0.4,rotation:33,x:3.2,y:37},0).wait(1).to({rotation:36.2,x:-0.1,y:36.9},0).wait(1).to({scaleX:1,scaleY:1,rotation:39.5,x:-3.6,y:36.7},0).wait(1).to({rotation:42.7,x:-7.3,y:36.6},0).wait(1).to({rotation:45.9,x:-10.9,y:36.5},0).wait(1).to({rotation:49.1,x:-14.8,y:36.4},0).wait(1).to({rotation:52.3,x:-18.8,y:36.2},0).wait(1).to({rotation:55.5,x:-22.8,y:36.1},0).wait(1).to({rotation:58.7,x:-27,y:35.9},0).wait(1).to({rotation:61.9,x:-31.3,y:35.7},0).wait(1).to({scaleX:1,scaleY:1,rotation:65.1,x:-35.8,y:35.4},0).wait(1).to({regX:-25.7,regY:24.9,rotation:68.3,x:-73.5,y:19.9},0).wait(1).to({regX:0.4,regY:-0.4,rotation:63,x:-35.6,y:27},0).wait(1).to({rotation:48.8,x:-24.8,y:6.2},0).wait(1).to({rotation:31,x:-14.2,y:-20.1},0).wait(1).to({rotation:15.6,x:-7.8,y:-42.5},0).wait(1).to({rotation:5.8,x:-5,y:-56},0).wait(1).to({regX:-25.9,regY:24.6,rotation:2.6,x:-31.3,y:-36.3},0).to({rotation:8.1,x:-31.4,y:-36.2},6,cjs.Ease.get(1)).wait(1).to({regX:0.4,regY:-0.4,rotation:6.4,x:-4.4,y:-45},0).wait(1).to({rotation:4.9,x:-6.8,y:-34.3},0).wait(1).to({rotation:3.5,x:-8.8,y:-25},0).wait(1).to({rotation:2.4,x:-10.6,y:-17.1},0).wait(1).to({rotation:1.5,x:-12,y:-10.7},0).wait(1).to({rotation:0.8,x:-13.1,y:-5.6},0).wait(1).to({rotation:0.3,x:-13.9,y:-2},0).wait(1).to({rotation:0,x:-14.3,y:0.1},0).wait(1).to({regX:-26,regY:24.7,rotation:-0.1,x:-40.8,y:25.8},0).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-128,-131.2,256,168.6);


    (lib.machine_02 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{"check":22,"raise":51});

        // Layer 23
        this.instance = new lib.Tween3("synched",0);
        this.instance.setTransform(0,-23.5);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(60));

        // arm
        this.instance_1 = new lib.arm("synched",0);
        this.instance_1.setTransform(-29.5,-40.6,1,1,7.5,0,0,16.4,-17.6);

        this.timeline.addTween(cjs.Tween.get(this.instance_1).wait(19).to({startPosition:0},0).to({regX:16.3,rotation:15,x:-29.4,y:-40.7},3,cjs.Ease.get(1)).to({regY:-17.9,rotation:-49.1,x:-28.8,y:-34.9},13,cjs.Ease.get(1)).wait(1).to({regX:0,regY:0,rotation:-48.3,x:-26.2,y:-11},0).wait(1).to({rotation:-46,x:-27.3,y:-11.3},0).wait(1).to({rotation:-42.3,x:-28.9,y:-11.8},0).wait(1).to({rotation:-37.6,x:-30.9,y:-12.6},0).wait(1).to({rotation:-32.4,x:-33.2,y:-13.6},0).wait(1).to({rotation:-27.2,x:-35.4,y:-14.9},0).wait(1).to({rotation:-22.3,x:-37.4,y:-16.2},0).wait(1).to({rotation:-18.1,x:-39.1,y:-17.5},0).wait(1).to({rotation:-14.7,x:-40.5,y:-18.6},0).wait(1).to({rotation:-12.3,x:-41.4,y:-19.5},0).wait(1).to({rotation:-10.9,x:-41.9,y:-20},0).wait(1).to({regX:16.4,regY:-17.6,rotation:-10.5,x:-29.3,y:-40.7},0).wait(1).to({regX:0,regY:0,rotation:-10.2,x:-42.4,y:-20.5},0).wait(1).to({rotation:-9.3,x:-42.7,y:-20.7},0).wait(1).to({rotation:-7.9,x:-43.1,y:-21},0).wait(1).to({rotation:-6.2,x:-43.8,y:-21.5},0).wait(1).to({rotation:-4.1,x:-44.4,y:-22},0).wait(1).to({rotation:-1.8,x:-45.2,y:-22.6},0).wait(1).to({rotation:0.5,x:-45.9,y:-23.2},0).wait(1).to({rotation:2.7,x:-46.6,y:-23.8},0).wait(1).to({rotation:4.6,x:-47.1,y:-24.4},0).wait(1).to({rotation:6.1,x:-47.6,y:-24.9},0).wait(1).to({rotation:7.1,x:-47.9,y:-25.2},0).wait(1).to({regX:16.4,regY:-17.6,rotation:7.5,x:-29.5,y:-40.6},0).wait(1));

        // forhand
        this.instance_2 = new lib.forhand("synched",0);
        this.instance_2.setTransform(-64.8,-8.7,1,1,4.6,0,0,-16.4,-18.7);

        this.timeline.addTween(cjs.Tween.get(this.instance_2).wait(19).to({startPosition:0},0).to({regY:-18.8,rotation:30.3,x:-67.9,y:-14.3},3,cjs.Ease.get(1)).to({regX:-16.5,regY:-18.6,rotation:27.3,x:-21.9,y:17.8},13,cjs.Ease.get(1)).wait(1).to({regX:-0.6,regY:-0.5,rotation:27.8,x:-16.9,y:40.8},0).wait(1).to({rotation:29,x:-19.3,y:39.8},0).wait(1).to({rotation:30.9,x:-23.1,y:38.1},0).wait(1).to({rotation:33.3,x:-28,y:36},0).wait(1).to({rotation:36,x:-33.3,y:33.6},0).wait(1).to({rotation:38.8,x:-38.8,y:31},0).wait(1).to({rotation:41.3,x:-43.9,y:28.6},0).wait(1).to({rotation:43.5,x:-48.3,y:26.6},0).wait(1).to({rotation:45.3,x:-51.7,y:24.9},0).wait(1).to({rotation:46.5,x:-54.2,y:23.6},0).wait(1).to({rotation:47.2,x:-55.7,y:22.9},0).wait(1).to({regX:-16.4,regY:-18.6,rotation:47.5,x:-53.6,y:-1.2},0).wait(1).to({regX:-0.6,regY:-0.5,rotation:46.8,x:-56.1,y:22.5},0).wait(1).to({rotation:44.7,x:-55.8,y:22.3},0).wait(1).to({rotation:41.5,x:-55.3,y:21.7},0).wait(1).to({rotation:37.2,x:-54.6,y:20.9},0).wait(1).to({rotation:32.3,x:-53.9,y:19.9},0).wait(1).to({rotation:26.9,x:-53,y:18.5},0).wait(1).to({rotation:21.4,x:-52.2,y:16.8},0).wait(1).to({rotation:16.2,x:-51.6,y:15.1},0).wait(1).to({rotation:11.5,x:-51.1,y:13.4},0).wait(1).to({rotation:7.8,x:-50.7,y:11.9},0).wait(1).to({rotation:5.4,x:-50.5,y:10.9},0).wait(1).to({regX:-16.4,regY:-18.7,rotation:4.6,x:-64.8,y:-8.7},0).wait(1));

        // glass
        this.instance_3 = new lib.glass("synched",0);
        this.instance_3.setTransform(-40.8,25.8,1,1,-0.1,0,0,-26,24.7);

        this.timeline.addTween(cjs.Tween.get(this.instance_3).wait(19).to({startPosition:0},0).to({regX:-25.9,regY:24.6,x:-61.2,y:27.1},3,cjs.Ease.get(1)).to({regX:-25.7,regY:24.7,rotation:18.4,x:-12.7,y:59.1},13,cjs.Ease.get(1)).wait(1).to({regX:0.4,regY:-0.4,rotation:19.2,x:19.2,y:43.5},0).wait(1).to({rotation:21.5,x:16.9,y:43.8},0).wait(1).to({rotation:25.1,x:13.3,y:44.1},0).wait(1).to({rotation:29.7,x:8.5,y:44.7},0).wait(1).to({rotation:34.9,x:2.9,y:45.4},0).wait(1).to({rotation:40,x:-3.1,y:46.1},0).wait(1).to({rotation:44.8,x:-8.9,y:46.8},0).wait(1).to({rotation:49,x:-14.2,y:47.5},0).wait(1).to({rotation:52.3,x:-18.5,y:47.9},0).wait(1).to({rotation:54.7,x:-21.6,y:48.3},0).wait(1).to({rotation:56.1,x:-23.5,y:48.5},0).wait(1).to({regX:-26,regY:24.8,rotation:56.5,x:-59.5,y:40.6},0).wait(1).to({regX:0.4,regY:-0.4,rotation:55.6,x:-23.4,y:47.9},0).wait(1).to({rotation:52.8,x:-22.2,y:45.4},0).wait(1).to({rotation:48.6,x:-20.5,y:41.6},0).wait(1).to({rotation:43,x:-18.5,y:36.5},0).wait(1).to({rotation:36.4,x:-16.6,y:30.6},0).wait(1).to({rotation:29.3,x:-15.1,y:24.2},0).wait(1).to({rotation:22.1,x:-14.2,y:17.9},0).wait(1).to({rotation:15.2,x:-13.7,y:12},0).wait(1).to({rotation:9.1,x:-13.8,y:7},0).wait(1).to({rotation:4.2,x:-14,y:3.3},0).wait(1).to({rotation:1,x:-14.3,y:0.9},0).wait(1).to({regX:-26,regY:24.7,rotation:-0.1,x:-40.8,y:25.3},0).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-128,-131.2,256,168.6);


    (lib.lines = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.instance = new lib.Lines_02("synched",0);
        this.instance.setTransform(-12.7,0.1,1,1,0,0,0,72.9,-8.4);

        this.timeline.addTween(cjs.Tween.get(this.instance).to({x:-0.1},13).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-86.8,-9.5,148.1,19.2);


    (lib.Lights_static = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 5
        this.instance = new lib.LightBulb("synched",9);
        this.instance.setTransform(3.7,-3.4,1,1,0,0,0,3.4,-3.4);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(0.3,-6.8,6.8,6.8);


    (lib.Lights = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 5
        this.instance = new lib.LightBulb("synched",9);
        this.instance.setTransform(3.7,-3.4,1,1,0,0,0,3.4,-3.4);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(10).to({x:15.7,startPosition:18},0).wait(10).to({x:27.7,startPosition:9},0).wait(10));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(0.3,-6.8,6.8,6.8);


    (lib.hop = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 2
        this.instance = new lib.Symbol23("synched",0);
        this.instance.setTransform(-0.2,-17.8,1,1,0,0,0,-3.6,-0.1);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(1));

        // Layer 3
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#888888").ss(2,1).p("AE0hKIpnCV");
        this.shape.setTransform(0,9.2);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f().s("#888888").ss(2,1).p("AkzhKIJnCV");
        this.shape_1.setTransform(0,9.2);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#888888").ss(2,1).p("AE0hKIpnCV");
        this.shape_2.setTransform(0,-6);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f().s("#888888").ss(2,1).p("AkzhKIJnCV");
        this.shape_3.setTransform(0,-6);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_3},{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-71.8,-23.5,143.3,41.4);


    (lib.computer = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.instance = new lib.Symbol14("synched",0);
        this.instance.setTransform(-40.1,6.4);

        this.instance_1 = new lib.Symbol13("synched",0);
        this.instance_1.setTransform(10,14.2);

        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(1.5,1,1).p("AFNAfIqZAAQgNAAgKgJQgJgJAAgNQAAgLAJgKQAJgJAOAAIKZAAQANAAAKAJQAJAKAAALQAAANgJAJQgKAJgNAAg");
        this.shape.setTransform(-20.7,57.1);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AlNAfQgMAAgJgJQgJgJAAgNQAAgLAJgKQAIgJANAAIKaAAQANAAAKAJQAIAKABALQgBANgIAJQgKAJgNAAg");
        this.shape_1.setTransform(-20.7,57.1);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#C5C5C5").ss(1.5,1,1).p("AFNAfIqZAAQgOAAgJgJQgJgKAAgMQAAgLAJgKQAKgJANAAIKZAAQANAAAKAJQAJAKAAALQAAAMgJAKQgKAJgNAAg");
        this.shape_2.setTransform(-20.7,50.7);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f("#FFFFFF").s().p("AlNAfQgNAAgIgJQgJgKAAgMQAAgLAJgKQAJgJAMAAIKaAAQANAAAKAJQAIAKABALQgBAMgIAKQgKAJgNAAg");
        this.shape_3.setTransform(-20.7,50.7);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f().s("#C5C5C5").ss(1.5,1,1).p("AG6kVIABIpQAAAMgHAHQgIAIgKAAItBACQgLAAgHgIQgIgIAAgLIgBopQAAgMAIgHQAHgIAKAAINBgCQALAAAHAIQAHAIABALg");
        this.shape_4.setTransform(-18.3,10.2);

        this.shape_5 = new cjs.Shape();
        this.shape_5.graphics.f("#FFFFFF").s().p("AmxEpQgIgIAAgLIgBopQAAgMAIgHQAHgIAKAAINBgCQALAAAHAIQAHAIABALIABIpQAAALgHAIQgIAIgKAAItBACQgLAAgHgIg");
        this.shape_5.setTransform(-18.3,10.2);

        this.shape_6 = new cjs.Shape();
        this.shape_6.graphics.f().s("#C5C5C5").ss(1.5,1,1).p("AG6kVIABIpQAAAMgHAHQgIAIgKAAItBACQgLAAgHgIQgIgIAAgLIgBopQAAgMAIgHQAHgIAKAAINBgCQALAAAHAIQAHAIABALg");
        this.shape_6.setTransform(-18.3,10.2);

        this.shape_7 = new cjs.Shape();
        this.shape_7.graphics.f("#FFFFFF").s().p("AmxEpQgIgIAAgLIgBopQAAgMAIgHQAHgIAKAAINBgCQALAAAHAIQAHAIABALIABIpQAAALgHAIQgIAIgKAAItBACQgLAAgHgIg");
        this.shape_7.setTransform(-18.3,10.2);

        this.shape_8 = new cjs.Shape();
        this.shape_8.graphics.f().s("#C5C5C5").ss(1.8).p("AH5lSIACKjQAAAOgJAJQgIAKgMAAIu5ACQgMAAgIgJQgJgKAAgOIgCqiQAAgOAJgKQAIgJAMgBIO5gCQAMAAAIAKQAJAJAAAOg");
        this.shape_8.setTransform(-18.3,10.2);

        this.shape_9 = new cjs.Shape();
        this.shape_9.graphics.f("#FFFFFF").s().p("AnvFrQgJgLAAgNIgCqjQAAgNAJgKQAIgKAMAAIO5gCQAMAAAIAKQAJAJAAAOIACKiQAAAOgJAKQgIAKgMAAIu5ACQgMAAgIgJg");
        this.shape_9.setTransform(-18.3,10.2);

        this.shape_10 = new cjs.Shape();
        this.shape_10.graphics.f().s("#C5C5C5").ss(1.8).p("AH5lSIACKjQAAAOgJAJQgIAKgMAAIu5ACQgMAAgIgJQgJgKAAgOIgCqiQAAgOAJgKQAIgJAMgBIO5gCQAMAAAIAKQAJAJAAAOg");
        this.shape_10.setTransform(-18.3,10.2);

        this.shape_11 = new cjs.Shape();
        this.shape_11.graphics.f("#FFFFFF").s().p("AnvFrQgJgLAAgNIgCqjQAAgNAJgKQAIgKAMAAIO5gCQAMAAAIAKQAJAJAAAOIACKiQAAAOgJAKQgIAKgMAAIu5ACQgMAAgIgJg");
        this.shape_11.setTransform(-18.3,10.2);

        this.shape_12 = new cjs.Shape();
        this.shape_12.graphics.f().s("#C5C5C5").ss(2).p("AAIBMIgPAAQgNAAgKgJQgJgKAAgNIAAhXQAAgNAJgKQAKgJANAAIAPAAQANAAAKAJQAJAKAAANIAABXQAAANgJAKQgKAJgNAAg");
        this.shape_12.setTransform(34.9,14.9);

        this.shape_13 = new cjs.Shape();
        this.shape_13.graphics.f("#FFFFFF").s().p("AgHBMQgNAAgKgJQgJgKAAgNIAAhXQAAgNAJgKQAKgJANAAIAPAAQANAAAKAJQAJAKAAANIAABXQAAANgJAKQgKAJgNAAg");
        this.shape_13.setTransform(34.9,14.9);

        this.shape_14 = new cjs.Shape();
        this.shape_14.graphics.f().s("#C5C5C5").ss(2).p("AhLAIIAAgPQAAgNAJgKQAKgJANAAIBXAAQANAAAKAJQAJAKAAANIAAAPQAAANgJAKQgKAJgNAAIhXAAQgNAAgKgJQgJgKAAgNg");
        this.shape_14.setTransform(61.3,57.5);

        this.shape_15 = new cjs.Shape();
        this.shape_15.graphics.f("#FFFFFF").s().p("AgrAoQgNAAgKgJQgJgKAAgNIAAgPQAAgNAJgKQAKgJANAAIBXAAQANAAAKAJQAJAKAAANIAAAPQAAANgJAKQgKAJgNAAg");
        this.shape_15.setTransform(61.3,57.5);

        this.shape_16 = new cjs.Shape();
        this.shape_16.graphics.f().s("#C5C5C5").ss(2).p("ADDDrIhGAAIAAmZIk/AAIAAg8IFqAAQAOAAAGAFQAHAEAAAMg");
        this.shape_16.setTransform(44.9,36.3);

        this.shape_17 = new cjs.Shape();
        this.shape_17.graphics.f("#FFFFFF").s().p("AB9DqIAAmXIk/AAIAAg9IFqAAQAOABAGADQAHAFgBAMIAAG/g");
        this.shape_17.setTransform(44.9,36.3);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_17},{t:this.shape_16},{t:this.shape_15},{t:this.shape_14},{t:this.shape_13},{t:this.shape_12},{t:this.shape_11},{t:this.shape_10},{t:this.shape_9},{t:this.shape_8},{t:this.shape_7},{t:this.shape_6},{t:this.shape_5},{t:this.shape_4},{t:this.shape_3},{t:this.shape_2},{t:this.shape_1},{t:this.shape},{t:this.instance_1},{t:this.instance}]}).wait(30));

        // mask (mask)
        var mask = new cjs.Shape();
        mask._off = true;
        mask.graphics.p("AkOAiIAAkGQAAgbAUgVQAUgUAdAAIHYAAQgbABgTATQgUAVAAAbIAAEGg");
        mask.setTransform(-21,-29.7);

        // lines
        this.instance_2 = new lib.Symbol15("synched",0);
        this.instance_2.setTransform(-24.9,-28.9);

        this.instance_2.mask = mask;

        this.timeline.addTween(cjs.Tween.get(this.instance_2).to({y:-52.4},29).wait(1));

        // Symbol 16
        this.instance_3 = new lib.Symbol16("synched",0);
        this.instance_3.setTransform(-19.1,-42.9,1,1,0,0,0,-0.5,-0.4);

        this.timeline.addTween(cjs.Tween.get(this.instance_3).wait(30));

        // Layer 5
        this.shape_18 = new cjs.Shape();
        this.shape_18.graphics.f().s("#C5C5C5").ss(2).p("Ah0AKIAAgKIDzAA");
        this.shape_18.setTransform(-6.5,-59.5);

        this.shape_19 = new cjs.Shape();
        this.shape_19.graphics.f("#C5C5C5").s().p("Ah5CoIAAlPIDzAAIAAFPg");
        this.shape_19.setTransform(-5.1,-42.4);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_19},{t:this.shape_18}]}).wait(30));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-74.9,-60.5,144.9,123.1);


    (lib.clockneedle = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.instance = new lib.Symbol11("synched",0);
        this.instance.setTransform(0,3.8,1,1,0,0,0,0,3.8);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-0.8,-3.7,1.6,7.5);


    (lib.clockmachine = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // clock needle
        this.instance = new lib.clockneedle("synched",0);
        this.instance.setTransform(-0.4,3.5,1,1,0,0,0,0,3.8);
        this.instance.filters = [new cjs.ColorFilter(0, 0, 0, 1, 197, 197, 197, 0)];
        this.instance.cache(-3,-6,6,12);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(1).to({regY:0,rotation:0.2,y:-0.3},0).wait(1).to({rotation:0.8},0).wait(1).to({rotation:1.6,x:-0.3},0).wait(1).to({rotation:2.5},0).wait(1).to({rotation:3.5,x:-0.2},0).wait(1).to({rotation:4.3},0).wait(1).to({rotation:5,x:-0.1},0).wait(1).to({rotation:5.3},0).wait(1).to({regY:3.7,rotation:5.5,x:-0.4,y:3.5},0).wait(1).to({regY:0,rotation:4.1,x:-0.2,y:-0.3},0).wait(1).to({rotation:0.3,x:-0.4},0).wait(1).to({rotation:-5.1,x:-0.8},0).wait(1).to({rotation:-11.1,x:-1.2,y:-0.2},0).wait(1).to({rotation:-16.6,x:-1.5},0).wait(1).to({rotation:-21,x:-1.8,y:-0.1},0).wait(1).to({rotation:-23.6,x:-1.9,y:0},0).wait(1).to({regX:0.1,regY:3.6,rotation:-24.5,x:-0.4,y:3.3},0).wait(1).to({regX:0,regY:0,rotation:-23.7,x:-2,y:0},0).wait(1).to({rotation:-21.2,x:-1.8},0).wait(1).to({rotation:-17.2,x:-1.6,y:-0.1},0).wait(1).to({rotation:-12,x:-1.3,y:-0.2},0).wait(1).to({rotation:-5.8,x:-0.9,y:-0.3},0).wait(1).to({rotation:0.9,x:-0.4},0).wait(1).to({rotation:7.9,x:0},0).wait(1).to({rotation:14.7,x:0.4,y:-0.2},0).wait(1).to({rotation:21.1,x:0.8,y:-0.1},0).wait(1).to({rotation:26.8,x:1.1,y:0},0).wait(1).to({rotation:31.6,x:1.4,y:0.2},0).wait(1).to({rotation:35.5,x:1.6,y:0.3},0).wait(1).to({rotation:38.2,x:1.7,y:0.4},0).wait(1).to({rotation:39.9,x:1.8,y:0.5},0).wait(1).to({regY:3.6,rotation:40.4,x:-0.5,y:3.3},0).wait(1).to({regY:0,rotation:38.5,x:1.8,y:0.4},0).wait(1).to({rotation:35.1,x:1.6,y:0.3},0).wait(1).to({regX:-0.1,regY:3.5,rotation:33.7,x:-0.5,y:3.2},0).wait(1).to({regX:0,regY:0,rotation:34.6,x:1.5,y:0.3},0).wait(1).to({rotation:36.7,x:1.6,y:0.4},0).wait(1).to({rotation:39.5,x:1.7,y:0.5},0).wait(1).to({rotation:42,x:1.8,y:0.6},0).wait(1).to({rotation:43.6,x:1.9,y:0.7},0).wait(1).to({regX:-0.1,regY:3.6,rotation:44.2,x:-0.5,y:3.3},0).wait(1).to({regX:0,regY:0,rotation:42,x:1.9,y:0.6},0).wait(1).to({rotation:38.3,x:1.7,y:0.5},0).wait(1).to({regX:-0.1,regY:3.6,rotation:36.7,x:-0.6,y:3.3},0).wait(1).to({regX:0,regY:0,rotation:37,x:1.7,y:0.4},0).wait(1).to({rotation:37.8},0).wait(1).to({rotation:39,y:0.5},0).wait(1).to({rotation:40.1,x:1.8},0).wait(1).to({rotation:41.1},0).wait(1).to({rotation:41.7,x:1.9,y:0.6},0).wait(1).to({regX:-0.2,regY:3.6,rotation:41.9,x:-0.6,y:3.2},0).wait(1).to({regX:0,regY:0,rotation:37.8,x:1.7,y:0.4},0).wait(1).to({regX:-0.3,regY:3.6,rotation:34.4,x:-0.7,y:3.1},0).wait(1).to({regX:0,regY:0,rotation:32.9,x:1.5,y:0.2},0).wait(1).to({rotation:28.4,x:1.3,y:0.1},0).wait(1).to({rotation:21.9,x:1,y:0},0).wait(1).to({rotation:14.2,x:0.6,y:-0.1},0).wait(1).to({rotation:6.6,x:0.2,y:-0.2},0).wait(1).to({regY:3.8,rotation:0,x:-0.4,y:3.5},0).wait(1));

        // clock
        this.instance_1 = new lib.ClipGroup_6();
        this.instance_1.setTransform(0,2.4,1,1,0,0,0,6.3,8.7);

        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(1,0,1).p("AB0AAQAAAwgiAiQgiAigwAAQgvAAgigiQgigiAAgwQAAgvAigiQAigiAvAAQAwAAAiAiQAiAiAAAvg");

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AhRBSQgigiAAgwQAAgvAigiQAigiAvAAQAwAAAiAiQAiAiAAAvQAAAwgiAiQgiAigwAAQgvAAgigig");

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_1},{t:this.shape},{t:this.instance_1}]}).wait(60));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-12.6,-12.6,25.3,25.3);


    (lib.clock = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Big
        this.instance = new lib.needle1("synched",0);
        this.instance.setTransform(-0.2,0.5,1,1,0,0,0,0,2.3);

        this.timeline.addTween(cjs.Tween.get(this.instance).to({rotation:716},59).wait(1));

        // Layer 6
        this.instance_1 = new lib.needle_2("synched",0);
        this.instance_1.setTransform(-0.3,0.4,1,1,0,0,0,-1,-0.8);

        this.timeline.addTween(cjs.Tween.get(this.instance_1).wait(60));

        // Layer 5
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(1.3,1).p("ABEAAQAAAcgUAUQgUATgcAAQgbAAgUgTQgUgUAAgcQAAgaAUgUQAUgUAbAAQAcAAAUAUQAUAUAAAag");

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(60));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-9.8,-7.8,17.6,15.6);


    (lib.BoxOutline_flicker = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.instance = new lib.outlinebox("synched",0);
        this.instance.setTransform(22.7,-22.6);

        this.timeline.addTween(cjs.Tween.get(this.instance).to({_off:true},2).wait(2).to({_off:false},0).to({_off:true},25).wait(3).to({_off:false},0).to({_off:true},3).wait(25));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-1,-46.3,47.3,47.4);


    (lib.Tween16 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 3
        this.instance = new lib.clock("synched",0);
        this.instance.setTransform(15,-95.9,1.749,1.749,0,0,0,-1.2,-0.1);

        this.instance_1 = new lib.clock("synched",0);
        this.instance_1.setTransform(-16.4,-95.8,1.749,1.749,-96.1);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[]}).to({state:[{t:this.instance_1},{t:this.instance}]},2).to({state:[]},60).wait(28));

        // Layer 1
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(2,1,1).p("AizjgIFnAAQAEAAAAAFIABG4QAAAEgFAAIlnAAQgEAAAAgEIgBm4QAAgFAFAAg");
        this.shape.setTransform(-1,58.9);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("AizDgQgEAAAAgDIgBm4QAAgFAFAAIFnAAQAEAAAAAFIABG4QAAADgFAAg");
        this.shape_1.setTransform(-1,58.9);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#848484").ss(2.4).p("AGVAAQAAAQgMAMQgMAMgQAAIrZAAQgRAAgLgMQgMgMAAgQQAAgQAMgLQALgMARAAILZAAQAQAAAMAMQAMALAAAQg");
        this.shape_2.setTransform(0,-72.5);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f("#DFDFDF").s().p("AlsAnQgQAAgMgLQgLgMgBgQQABgPALgMQAMgMAQAAILZAAQAQAAAMAMQAMAMAAAPQAAAQgMAMQgMALgQAAg");
        this.shape_3.setTransform(0,-72.5);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f().s("#848484").ss(2.4).p("AAjPFIhFAAIAA+JIBFAAg");
        this.shape_4.setTransform(0.1,15.8,0.999,0.852);

        this.shape_5 = new cjs.Shape();
        this.shape_5.graphics.f("#DFDFDF").s().p("AgiPFIAA+JIBFAAIAAeJg");
        this.shape_5.setTransform(0.1,15.8,0.999,0.852);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[]}).to({state:[{t:this.shape_5},{t:this.shape_4},{t:this.shape_3},{t:this.shape_2},{t:this.shape_1},{t:this.shape}]},2).to({state:[]},60).wait(28));

        // Layer 4
        this.shape_6 = new cjs.Shape();
        this.shape_6.graphics.f().s("#CCCCCC").ss(2).p("AllDeIAAmDQAAguAuAAIJvAAQAuAAAAAuIAAGD");
        this.shape_6.setTransform(0.3,-94.4);

        this.shape_7 = new cjs.Shape();
        this.shape_7.graphics.f("#FFFFFF").s().p("AllDZIAAmDQAAguAuAAIJvAAQAuAAAAAuIAAGDg");
        this.shape_7.setTransform(0.3,-93.9);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[]}).to({state:[{t:this.shape_7},{t:this.shape_6}]},2).to({state:[]},60).wait(28));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-52,-116.6,104,215.8);


    (lib.Tween15 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.instance = new lib.Symbol35("synched",0);
        this.instance.setTransform(0.5,0,1,1,0,0,0,0.5,0);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-13.1,-12.5,27.4,25.1);


    (lib.Tween14 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.instance = new lib.Symbol35("synched",0);
        this.instance.setTransform(0.5,0,1,1,0,0,0,0.5,0);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-13.1,-12.5,27.4,25.1);


    (lib.Symbol31copy2 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // v
        this.instance = new lib.Symbol33("synched",0);
        this.instance.setTransform(36.1,90,0.206,1,0,0,0,14.8,0);
        this.instance._off = true;

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(2).to({_off:false},0).to({regX:15.1},51,cjs.Ease.get(-1)).to({regX:15.4,scaleX:0.04,x:40.6},2).to({regX:14.8,scaleX:1,x:-45.6},3,cjs.Ease.get(-1)).wait(1).to({scaleX:0.95,scaleY:1.06,x:-45.5},0).wait(1));

        // Layer 2
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#C5C5C5").s().p("AgZDzQgEAAgIgqQgMhCAAh4QAAiNAOhJIANgrIBIAAQgNHlgBAAg");
        this.shape.setTransform(29.3,90.2);
        this.shape._off = true;

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#C5C5C5").s().p("AgXD2QgXj0AXj4IA2AAIAABmIAEAGIgEDkIAACcg");
        this.shape_1.setTransform(32.8,90.5);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f("#C5C5C5").s().p("AgbDzQgDAAAAgKQgBgTgDgNQgMhDABh3QAAiNANhJQAFgSACgOQABgLgBAAIBIAAQgNHlgBAAg");
        this.shape_2.setTransform(36.3,90.2);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f("#C5C5C5").s().p("AALDzIgkgCQgCAAgBgKIgFgYIgCgHQgRhDAAh2IAAgBQAAiFAShIIADgJIAJgfIACgKIgBAAIBJgBIABAMIgMHDQgDAWgCAAg");
        this.shape_3.setTransform(24.6,90.2);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f("#C5C5C5").s().p("AASDzQgSgCgRgGQgEgCgDgHIgLgWIgDgHQgihEAAh1IAAAAQgBiIAmhHIAFgIIASgcIAEgIIAAAAIATgDIA4AAIAGAHIgMHQQgJAOgIAAg");
        this.shape_4.setTransform(-10.2,90.2);

        this.shape_5 = new cjs.Shape();
        this.shape_5.graphics.f("#C5C5C5").s().p("AAgDzQgigLgjgiQhGhGAAh4QAAiNBIhFQAXgWAYgLIAWgHIBJAAQgMHlgBAAg");
        this.shape_5.setTransform(-68.4,90.2);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[]}).to({state:[{t:this.shape}]},2).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape}]},1).to({state:[{t:this.shape_1}]},1).to({state:[{t:this.shape_2}]},1).to({state:[{t:this.shape_3}]},1).to({state:[{t:this.shape_4}]},1).to({state:[{t:this.shape_5}]},1).to({state:[]},1).wait(1));
        this.timeline.addTween(cjs.Tween.get(this.shape).wait(2).to({_off:false},0).wait(51).to({_off:true},1).wait(6));

        // HandleChupchik
        this.instance_1 = new lib.HandleChupchik("synched",0);
        this.instance_1.setTransform(37.4,88.6,1.846,1,0,0,0,0,7.3);
        this.instance_1._off = true;

        this.timeline.addTween(cjs.Tween.get(this.instance_1).wait(2).to({_off:false},0).to({regX:0.1,x:37.6},51,cjs.Ease.get(-1)).to({regX:0,x:37.4},2).to({scaleX:1,x:6},3,cjs.Ease.get(-1)).wait(1).to({x:5.7},0).wait(1));

        // Arm
        this.instance_2 = new lib.Symbol34("synched",0);
        this.instance_2.setTransform(37.3,88.6,0.06,1,0,0,0,34.4,0.5);
        this.instance_2._off = true;

        this.timeline.addTween(cjs.Tween.get(this.instance_2).wait(2).to({_off:false},0).to({regX:35.3,y:88.5},51,cjs.Ease.get(-1)).to({regX:34.4,y:88.6},2).to({regX:34.6,regY:0.4,scaleX:1,x:27.4,y:88.5},3,cjs.Ease.get(-1)).wait(1).to({regX:34.5,scaleX:1.02,x:27.3},0).wait(1));

        // Symbol 35
        this.instance_3 = new lib.Tween15("synched",0);
        this.instance_3.setTransform(36,88.5);
        this.instance_3._off = true;

        this.instance_4 = new lib.Tween14("synched",0);
        this.instance_4.setTransform(36,88.5);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[]}).to({state:[{t:this.instance_3}]},2).to({state:[{t:this.instance_3}]},51).to({state:[{t:this.instance_3}]},2).to({state:[{t:this.instance_4}]},3).to({state:[{t:this.instance_4}]},1).wait(1));
        this.timeline.addTween(cjs.Tween.get(this.instance_3).wait(2).to({_off:false},0).to({startPosition:0},51,cjs.Ease.get(-1)).to({startPosition:0},2).to({_off:true},3,cjs.Ease.get(-1)).wait(2));

        // Symbol 32
        this.instance_5 = new lib.Tween16("synched",2);
        this.instance_5.setTransform(39.2,29.5);
        this.instance_5._off = true;

        this.timeline.addTween(cjs.Tween.get(this.instance_5).wait(2).to({_off:false},0).wait(58));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-2.5,-87.1,90.4,215.8);


    (lib.Symbol31copy = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // v
        this.instance = new lib.Symbol33("synched",0);
        this.instance.setTransform(-45.6,90,1,1,0,0,0,14.8,0);
        this.instance._off = true;

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(2).to({_off:false},0).to({scaleX:0.21,x:36.1},8,cjs.Ease.get(1)).wait(50));

        // Layer 2
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#C5C5C5").s().p("AAYDzQgSgEgSgIQgMgHgNgXIgDgFQgthDAAh2QAAiLAxhGIAFgGQASgbANgHIAVgEIA3AAIAKAFIgMHXQgMAJgMAAg");
        this.shape.setTransform(-33.2,90.2);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#C5C5C5").s().p("AAWDzQgUgDgQgGQgLgFgMgbIgCgFQgmhDAAh2QAAiKAphGIAEgHQAQgfAMgFIATgDIA3AAIAIAHQgLGmgCArQgJANgJAAg");
        this.shape_1.setTransform(-18.6,90.2);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f("#C5C5C5").s().p("AATDzQgTgCgSgEQgIgEgLgeIgCgFQgehDAAh3QAAiIAhhHIADgHQAQgiAIgEIAUgCIA2AAIAFAJQgKGWgCA2QgIAQgGAAg");
        this.shape_2.setTransform(-5.9,90.2);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f("#C5C5C5").s().p("AARDzIgkgEQgIgDgJggIgCgFQgZhCAAh4QAAiHAbhIIADgHQAOglAGgCIATgCIA2AAIAEALIgNHHQgFATgEAAg");
        this.shape_3.setTransform(4.9,90.2);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f("#C5C5C5").s().p("AAPDzIgkgDQgHgBgIgiIgCgFQgUhCAAh4QAAiHAWhIIACgHQAPgoADgBIBIgBIADAMIgNHEQgDAVgDAAg");
        this.shape_4.setTransform(13.6,90.2);

        this.shape_5 = new cjs.Shape();
        this.shape_5.graphics.f("#C5C5C5").s().p("AANDzIgkgBQgFgBgIgkIgBgFQgRhCAAh4QAAiGAShIIABgIQAOgpACgBIBIAAIACANQgKF1gDBMQgCAXgCAAg");
        this.shape_5.setTransform(20.5,90.2);

        this.shape_6 = new cjs.Shape();
        this.shape_6.graphics.f("#C5C5C5").s().p("AAMDzIgkAAQgEgBgIgkIgBgGQgOhBAAh5QAAiFAPhIIABgIIAOgrIBIAAIABAOIgNG/IgCAYg");
        this.shape_6.setTransform(25.4,90.2);

        this.shape_7 = new cjs.Shape();
        this.shape_7.graphics.f("#C5C5C5").s().p("AAMDzIgkAAQgFAAgHglIgBgFQgMhCAAh4QAAiGANhIIABgIIANgrIBIAAIAAAOIgMG+IgCAZg");
        this.shape_7.setTransform(28.3,90.2);

        this.shape_8 = new cjs.Shape();
        this.shape_8.graphics.f("#C5C5C5").s().p("AgZDzQgEAAgIgqQgMhCAAh4QAAiNAOhJIANgrIBIAAQgNHlgBAAg");
        this.shape_8.setTransform(29.3,90.2);
        this.shape_8._off = true;

        this.timeline.addTween(cjs.Tween.get({}).to({state:[]}).to({state:[{t:this.shape}]},2).to({state:[{t:this.shape_1}]},1).to({state:[{t:this.shape_2}]},1).to({state:[{t:this.shape_3}]},1).to({state:[{t:this.shape_4}]},1).to({state:[{t:this.shape_5}]},1).to({state:[{t:this.shape_6}]},1).to({state:[{t:this.shape_7}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_8}]},1).wait(1));
        this.timeline.addTween(cjs.Tween.get(this.shape_8).wait(10).to({_off:false},0).wait(50));

        // HandleChupchik
        this.instance_1 = new lib.HandleChupchik("synched",0);
        this.instance_1.setTransform(6,88.6,1,1,0,0,0,0,7.3);
        this.instance_1._off = true;

        this.timeline.addTween(cjs.Tween.get(this.instance_1).wait(2).to({_off:false},0).to({scaleX:1.85,x:37.4},8,cjs.Ease.get(1)).wait(50));

        // Arm
        this.instance_2 = new lib.Symbol34("synched",0);
        this.instance_2.setTransform(27.4,88.5,1,1,0,0,0,34.6,0.4);
        this.instance_2._off = true;

        this.timeline.addTween(cjs.Tween.get(this.instance_2).wait(2).to({_off:false},0).to({regX:34.4,regY:0.5,scaleX:0.06,x:37.3,y:88.6},8,cjs.Ease.get(1)).wait(50));

        // Symbol 35
        this.instance_3 = new lib.Tween14("synched",0);
        this.instance_3.setTransform(36,88.5);
        this.instance_3._off = true;

        this.instance_4 = new lib.Tween15("synched",0);
        this.instance_4.setTransform(36,88.5);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[]}).to({state:[{t:this.instance_3}]},2).to({state:[{t:this.instance_4}]},8).wait(50));
        this.timeline.addTween(cjs.Tween.get(this.instance_3).wait(2).to({_off:false},0).to({_off:true},8,cjs.Ease.get(1)).wait(50));

        // Symbol 32
        this.instance_5 = new lib.Tween16("synched",2);
        this.instance_5.setTransform(39.2,29.5);
        this.instance_5._off = true;

        this.timeline.addTween(cjs.Tween.get(this.instance_5).wait(2).to({_off:false},0).wait(58));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-79.3,-87.1,167.2,215.8);


    (lib.Symbol31 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // v
        this.instance = new lib.Symbol33("synched",0);
        this.instance.setTransform(-45.6,90,1,1,0,0,0,14.8,0);

        this.timeline.addTween(cjs.Tween.get(this.instance).to({scaleX:0.21,x:36.1},10,cjs.Ease.get(1)).to({regX:15.1},43,cjs.Ease.get(-1)).to({regX:15.4,scaleX:0.04,x:40.6},2).to({regX:14.8,scaleX:1,x:-45.6},3,cjs.Ease.get(-1)).wait(1).to({scaleX:0.95,scaleY:1.06,x:-45.5},0).wait(1));

        // Layer 2
        this.shape = new cjs.Shape();
        this.shape.graphics.f("#C5C5C5").s().p("AAgDzQgigLgjgiQhGhGAAh4QAAiNBIhFQAXgWAYgLIAWgHIBJAAQgMHlgBAAg");
        this.shape.setTransform(-68.4,90.2);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#C5C5C5").s().p("AAcDzQgTgFgRgKQgPgJgOgUIgDgEQg4hEAAh2QAAiLA9hGIAFgGQASgWARgJIAWgFIA4AAIAOADQgNHNgBAQQgOAFgPAAg");
        this.shape_1.setTransform(-49.8,90.2);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f("#C5C5C5").s().p("AAYDzQgSgEgSgIQgMgHgNgXIgDgFQgthDAAh2QAAiLAxhGIAFgGQASgbANgHIAVgEIA3AAIAKAFIgMHXQgMAJgMAAg");
        this.shape_2.setTransform(-33.2,90.2);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f("#C5C5C5").s().p("AAWDzQgUgDgQgGQgLgFgMgbIgCgFQgmhDAAh2QAAiKAphGIAEgHQAQgfAMgFIATgDIA3AAIAIAHQgLGmgCArQgJANgJAAg");
        this.shape_3.setTransform(-18.6,90.2);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f("#C5C5C5").s().p("AATDzQgTgCgSgEQgIgEgLgeIgCgFQgehDAAh3QAAiIAhhHIADgHQAQgiAIgEIAUgCIA2AAIAFAJQgKGWgCA2QgIAQgGAAg");
        this.shape_4.setTransform(-5.9,90.2);

        this.shape_5 = new cjs.Shape();
        this.shape_5.graphics.f("#C5C5C5").s().p("AARDzIgkgEQgIgDgJggIgCgFQgZhCAAh4QAAiHAbhIIADgHQAOglAGgCIATgCIA2AAIAEALIgNHHQgFATgEAAg");
        this.shape_5.setTransform(4.9,90.2);

        this.shape_6 = new cjs.Shape();
        this.shape_6.graphics.f("#C5C5C5").s().p("AAPDzIgkgDQgHgBgIgiIgCgFQgUhCAAh4QAAiHAWhIIACgHQAPgoADgBIBIgBIADAMIgNHEQgDAVgDAAg");
        this.shape_6.setTransform(13.6,90.2);

        this.shape_7 = new cjs.Shape();
        this.shape_7.graphics.f("#C5C5C5").s().p("AANDzIgkgBQgFgBgIgkIgBgFQgRhCAAh4QAAiGAShIIABgIQAOgpACgBIBIAAIACANQgKF1gDBMQgCAXgCAAg");
        this.shape_7.setTransform(20.5,90.2);

        this.shape_8 = new cjs.Shape();
        this.shape_8.graphics.f("#C5C5C5").s().p("AAMDzIgkAAQgEgBgIgkIgBgGQgOhBAAh5QAAiFAPhIIABgIIAOgrIBIAAIABAOIgNG/IgCAYg");
        this.shape_8.setTransform(25.4,90.2);

        this.shape_9 = new cjs.Shape();
        this.shape_9.graphics.f("#C5C5C5").s().p("AAMDzIgkAAQgFAAgHglIgBgFQgMhCAAh4QAAiGANhIIABgIIANgrIBIAAIAAAOIgMG+IgCAZg");
        this.shape_9.setTransform(28.3,90.2);

        this.shape_10 = new cjs.Shape();
        this.shape_10.graphics.f("#C5C5C5").s().p("AgZDzQgEAAgIgqQgMhCAAh4QAAiNAOhJIANgrIBIAAQgNHlgBAAg");
        this.shape_10.setTransform(29.3,90.2);
        this.shape_10._off = true;

        this.shape_11 = new cjs.Shape();
        this.shape_11.graphics.f("#C5C5C5").s().p("AgXD2QgXj0AXj4IA2AAIAABmIAEAGIgEDkIAACcg");
        this.shape_11.setTransform(32.8,90.5);

        this.shape_12 = new cjs.Shape();
        this.shape_12.graphics.f("#C5C5C5").s().p("AgbDzQgDAAAAgKQgBgTgDgNQgMhDABh3QAAiNANhJQAFgSACgOQABgLgBAAIBIAAQgNHlgBAAg");
        this.shape_12.setTransform(36.3,90.2);

        this.shape_13 = new cjs.Shape();
        this.shape_13.graphics.f("#C5C5C5").s().p("AALDzIgkgCQgCAAgBgKIgFgYIgCgHQgRhDAAh2IAAgBQAAiFAShIIADgJIAJgfIACgKIgBAAIBJgBIABAMIgMHDQgDAWgCAAg");
        this.shape_13.setTransform(24.6,90.2);

        this.shape_14 = new cjs.Shape();
        this.shape_14.graphics.f("#C5C5C5").s().p("AASDzQgSgCgRgGQgEgCgDgHIgLgWIgDgHQgihEAAh1IAAAAQgBiIAmhHIAFgIIASgcIAEgIIAAAAIATgDIA4AAIAGAHIgMHQQgJAOgIAAg");
        this.shape_14.setTransform(-10.2,90.2);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape}]}).to({state:[{t:this.shape_1}]},1).to({state:[{t:this.shape_2}]},1).to({state:[{t:this.shape_3}]},1).to({state:[{t:this.shape_4}]},1).to({state:[{t:this.shape_5}]},1).to({state:[{t:this.shape_6}]},1).to({state:[{t:this.shape_7}]},1).to({state:[{t:this.shape_8}]},1).to({state:[{t:this.shape_9}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_10}]},1).to({state:[{t:this.shape_11}]},1).to({state:[{t:this.shape_12}]},1).to({state:[{t:this.shape_13}]},1).to({state:[{t:this.shape_14}]},1).to({state:[{t:this.shape}]},1).to({state:[]},1).wait(1));
        this.timeline.addTween(cjs.Tween.get(this.shape_10).wait(10).to({_off:false},0).wait(43).to({_off:true},1).wait(6));

        // HandleChupchik
        this.instance_1 = new lib.HandleChupchik("synched",0);
        this.instance_1.setTransform(6,88.6,1,1,0,0,0,0,7.3);

        this.timeline.addTween(cjs.Tween.get(this.instance_1).to({scaleX:1.85,x:37.4},10,cjs.Ease.get(1)).to({regX:0.1,x:37.6},43,cjs.Ease.get(-1)).to({regX:0,x:37.4},2).to({scaleX:1,x:6},3,cjs.Ease.get(-1)).wait(1).to({x:5.7},0).wait(1));

        // Arm
        this.instance_2 = new lib.Symbol34("synched",0);
        this.instance_2.setTransform(27.4,88.5,1,1,0,0,0,34.6,0.4);

        this.timeline.addTween(cjs.Tween.get(this.instance_2).to({regX:34.4,regY:0.5,scaleX:0.06,x:37.3,y:88.6},10,cjs.Ease.get(1)).to({regX:35.3,y:88.5},43,cjs.Ease.get(-1)).to({regX:34.4,y:88.6},2).to({regX:34.6,regY:0.4,scaleX:1,x:27.4,y:88.5},3,cjs.Ease.get(-1)).wait(1).to({regX:34.5,scaleX:1.02,x:27.3},0).wait(1));

        // Symbol 35
        this.instance_3 = new lib.Tween14("synched",0);
        this.instance_3.setTransform(36,88.5);

        this.instance_4 = new lib.Tween15("synched",0);
        this.instance_4.setTransform(36,88.5);
        this.instance_4._off = true;

        this.timeline.addTween(cjs.Tween.get(this.instance_3).to({_off:true},10,cjs.Ease.get(1)).wait(45).to({_off:false},3,cjs.Ease.get(-1)).wait(1).to({startPosition:0},0).wait(1));
        this.timeline.addTween(cjs.Tween.get(this.instance_4).to({_off:false},10,cjs.Ease.get(1)).to({startPosition:0},43,cjs.Ease.get(-1)).to({startPosition:0},2).to({_off:true},3,cjs.Ease.get(-1)).wait(2));

        // Symbol 32
        this.instance_5 = new lib.Tween16("synched",2);
        this.instance_5.setTransform(39.2,29.5);

        this.timeline.addTween(cjs.Tween.get(this.instance_5).wait(60));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-79.3,-87.1,167.2,215.8);


    (lib.sampling = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // outline
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#848484").ss(2).p("AOQFdI8fAAIAAq5IcfAAg");
        this.shape.setTransform(0,4.4);

        this.timeline.addTween(cjs.Tween.get(this.shape).wait(60));

        // mask (mask)
        var mask = new cjs.Shape();
        mask._off = true;
        mask.graphics.p("AuPFcIAAq3IcfAAIAAK3g");
        mask.setTransform(-0.4,5.3);

        // light
        this.instance = new lib.Group();
        this.instance.setTransform(54.4,-2.7,1,1,0,0,0,42.7,92.3);
        this.instance.alpha = 0.5;

        this.instance_1 = new lib.Group_1();
        this.instance_1.setTransform(13,0.4,1,1,0,0,0,50.3,95.4);
        this.instance_1.alpha = 0.5;

        this.instance_2 = new lib.Group_2();
        this.instance_2.setTransform(-41.8,2.3,1,1,0,0,0,38.5,57);
        this.instance_2.alpha = 0.5;

        this.instance.mask = this.instance_1.mask = this.instance_2.mask = mask;

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.instance_2},{t:this.instance_1},{t:this.instance}]}).wait(60));

        // Pepitas
        this.instance_3 = new lib.pepita_move("synched",47);
        this.instance_3.setTransform(39.1,-35.6,1,1,0,0,0,-39,-35.5);

        this.instance_4 = new lib.pepita_move("synched",51);
        this.instance_4.setTransform(13.7,-35.6,1,1,0,0,0,-39,-35.5);

        this.instance_5 = new lib.pepita_move("synched",55);
        this.instance_5.setTransform(-12.7,-35.6,1,1,0,0,0,-39,-35.5);

        this.instance_6 = new lib.pepita_move("synched",0);
        this.instance_6.setTransform(-38.9,-35.6,1,1,0,0,0,-39,-35.5);

        this.instance_3.mask = this.instance_4.mask = this.instance_5.mask = this.instance_6.mask = mask;

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.instance_6},{t:this.instance_5},{t:this.instance_4},{t:this.instance_3}]}).wait(60));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-92.2,-31.5,184.4,71.8);


    (lib.Reflection_maker = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Gray_Line
        this.instance = new lib.Gray_Line("single",14);
        this.instance.setTransform(-39,-11.7);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(60));

        // Gray_Line
        this.instance_1 = new lib.Gray_Line("single",14);
        this.instance_1.setTransform(-39,-15.7);

        this.timeline.addTween(cjs.Tween.get(this.instance_1).wait(60));

        // Gray_Line
        this.instance_2 = new lib.Gray_Line("single",14);
        this.instance_2.setTransform(-39,-19.7);

        this.timeline.addTween(cjs.Tween.get(this.instance_2).wait(60));

        // Gray_Line
        this.instance_3 = new lib.Gray_Line("single",14);
        this.instance_3.setTransform(-39,-23.7);

        this.timeline.addTween(cjs.Tween.get(this.instance_3).wait(60));

        // Gray_Line
        this.instance_4 = new lib.Gray_Line("single",14);
        this.instance_4.setTransform(-39,-27.7);

        this.timeline.addTween(cjs.Tween.get(this.instance_4).wait(60));

        // Gray_Line
        this.instance_5 = new lib.Gray_Line("single",14);
        this.instance_5.setTransform(-39,-31.7);

        this.timeline.addTween(cjs.Tween.get(this.instance_5).wait(60));

        // Layer 3
        this.instance_6 = new lib.v("synched",0);
        this.instance_6.setTransform(0.3,3.8);
        this.instance_6.alpha = 0;
        this.instance_6._off = true;

        this.timeline.addTween(cjs.Tween.get(this.instance_6).wait(38).to({_off:false},0).wait(1).to({y:3.1,alpha:0.136},0).wait(1).to({y:2.2,alpha:0.32},0).wait(1).to({y:1.2,alpha:0.526},0).wait(1).to({y:0.3,alpha:0.703},0).wait(1).to({y:-0.4,alpha:0.831},0).wait(1).to({y:-0.8,alpha:0.915},0).wait(1).to({y:-1,alpha:0.966},0).wait(1).to({y:-1.2,alpha:0.992},0).wait(1).to({alpha:1},0).wait(8).to({startPosition:0},0).to({alpha:0},4).wait(1));

        // Layer 2 (mask)
        var mask = new cjs.Shape();
        mask._off = true;
        mask.graphics.p("AoNF8IAAr4IQaAAIAAL4g");
        mask.setTransform(-2.2,0.7);

        // line
        this.instance_7 = new lib.line("synched",0);
        this.instance_7.setTransform(31.7,-30.3);
        this.instance_7._off = true;

        this.instance_7.mask = mask;

        this.timeline.addTween(cjs.Tween.get(this.instance_7).wait(32).to({_off:false},0).to({y:5.5},8,cjs.Ease.get(0.6)).to({y:50.6},5,cjs.Ease.get(-0.6)).to({_off:true},1).wait(14));

        // outlinebox
        this.instance_8 = new lib.BoxOutline_flicker("single",57);
        this.instance_8.setTransform(0,-0.7,1,1,0,0,0,22.7,-22.7);

        this.timeline.addTween(cjs.Tween.get(this.instance_8).wait(26).to({mode:"synched",startPosition:0},0).to({startPosition:6},6).to({alpha:0.352,startPosition:20},14).wait(14));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-47.7,-32.9,17.5,22.3);


    (lib.moving_line_1 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // green_will
        this.instance = new lib.green_will("synched",0);
        this.instance.setTransform(-61.8,0.3);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(60));

        // green_will
        this.instance_1 = new lib.green_will("synched",0);
        this.instance_1.setTransform(61.6,0.3);

        this.timeline.addTween(cjs.Tween.get(this.instance_1).wait(60));

        // mask (mask)
        var mask = new cjs.Shape();
        mask._off = true;
        mask.graphics.p("AphBaQglgBgagaQgagZAAgmIAAAAQAAgkAagaQAZgbAmAAITEAAQAlAAAZAbQAaAaAAAkIAAAAQAAAmgaAZQgZAaglABg");

        // lines
        this.instance_2 = new lib.lines("synched",0);
        this.instance_2.setTransform(1.2,-0.2);

        this.instance_2.mask = mask;

        this.timeline.addTween(cjs.Tween.get(this.instance_2).wait(60));

        // Layer 8
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#848484").ss(2).p("AK8ABQAAAlgbAZQgaAbgkAAIzFAAQgkAAgagbQgbgZAAglIAAgBQAAglAbgZQAagbAkAAITFAAQAlAAAZAbQAbAZAAAlg");

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("ApiBZQgkAAgbgZQgagbAAgkIAAgBQAAglAagaQAbgaAkAAITFAAQAlAAAZAaQAbAaAAAlIAAABQAAAkgbAbQgaAZgkAAg");

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_1},{t:this.shape}]}).wait(60));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-75.2,-10,146.8,20.3);


    (lib.Machine_C_02_NoCrate_In = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // machine_04
        this.instance = new lib.Symbol31copy2("synched",2,false);
        this.instance.setTransform(423.5,-14.9,1,1,0,0,0,4.1,0.2);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(60));

        // machine_05_springboard
        this.instance_1 = new lib.machine_05_springboard("synched",0);
        this.instance_1.setTransform(653.2,122.8,1,1,0,0,0,61.1,0.8);

        this.timeline.addTween(cjs.Tween.get(this.instance_1).wait(60));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(411.4,-139,252,271.2);


    (lib.Machine_C_02_NoCrate = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // machine_04
        this.instance = new lib.Symbol33("synched",0);
        this.instance.setTransform(455.5,74.9,0.206,1,0,0,0,14.8,0);

        this.instance_1 = new lib.HandleChupchik("synched",0);
        this.instance_1.setTransform(456.8,73.5,1.846,1,0,0,0,0,7.3);

        this.shape = new cjs.Shape();
        this.shape.graphics.f("#C5C5C5").s().p("AgYDzQgGAAgHgqQgLhCAAh4QAAiNANhJIANgrIBIAAQgNHlgBAAg");
        this.shape.setTransform(448.6,75.1);

        this.instance_2 = new lib.Symbol34("synched",0);
        this.instance_2.setTransform(456.6,73.5,0.06,1,0,0,0,34.4,0.5);

        this.instance_3 = new lib.Tween15("synched",0);
        this.instance_3.setTransform(455.3,73.4);

        this.instance_4 = new lib.Tween16("synched",2);
        this.instance_4.setTransform(458.6,14.4);

        this.instance_5 = new lib.Handle_3d("synched",0);
        this.instance_5.setTransform(455.9,73.7,1,1,0,0,0,0.1,4.3);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.instance_5},{t:this.instance_4},{t:this.instance_3},{t:this.instance_2},{t:this.shape},{t:this.instance_1},{t:this.instance}]}).to({state:[]},60).wait(1));

        // machine_05_springboard
        this.instance_6 = new lib.machine_05_springboard("synched",0);
        this.instance_6.setTransform(653.3,122.8,1,1,42.7,0,0,61.2,0.7);

        this.timeline.addTween(cjs.Tween.get(this.instance_6).wait(4).to({regX:61.1,rotation:38.2,x:653.2},0).to({regY:0.8,rotation:-1.4},7,cjs.Ease.get(1)).to({rotation:0},3,cjs.Ease.get(1)).to({_off:true},46).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(411.4,-139,252.2,271.5);


    (lib.Machine_C_02_FirstNoCreate = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // machine_04
        this.instance = new lib.Symbol31copy("synched",2,false);
        this.instance.setTransform(423.5,-14.9,1,1,0,0,0,4.1,0.2);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(60));

        // box02
        this.instance_1 = new lib.box_V("synched",0);
        this.instance_1.setTransform(293.5,103.9,1,1,-18.7,0,0,0.1,25.3);

        this.timeline.addTween(cjs.Tween.get(this.instance_1).wait(1).to({regY:-0.3,rotation:-22.6,x:293.3,y:79.1,startPosition:1},0).wait(1).to({regY:25.3,scaleX:1,scaleY:1,rotation:-26.5,x:312.9,y:101.7,startPosition:0},0).wait(1).to({regY:-0.3,rotation:-21.2,x:312.2,y:79.9,startPosition:1},0).wait(1).to({rotation:-15.8,x:323,y:81.3,startPosition:2},0).wait(1).to({scaleX:1,scaleY:1,rotation:-10.5,x:333.9,y:83,startPosition:3},0).wait(1).to({rotation:-5.1,x:344.8,y:84.8,startPosition:4},0).wait(1).to({regX:0.3,regY:25.4,rotation:0.2,x:355.7,y:112.4,startPosition:0},0).wait(1).to({regX:0.1,regY:-0.3,rotation:4.1,x:365.9,y:85.2,startPosition:1},0).wait(1).to({regX:0.4,regY:25.4,rotation:8,x:373,y:109.1,startPosition:0},0).wait(1).to({regX:0.1,regY:-0.3,rotation:4.6,x:383.3,y:84.2,startPosition:1},0).wait(1).to({rotation:1.2,x:390.4,y:84.8,startPosition:2},0).wait(1).to({regX:0.5,regY:25.6,rotation:-2.2,x:398.8,y:111.2,startPosition:0},0).wait(1).to({regX:0.1,regY:-0.3,rotation:-0.8,x:406.7,y:86,startPosition:1},0).wait(1).to({regX:0.5,regY:25.6,rotation:0.5,x:416.2,y:112.4,startPosition:0},0).wait(1).to({regX:0.1,regY:-0.3,scaleX:1,scaleY:1,rotation:0.4,x:424.8,y:86.6,startPosition:1},0).wait(1).to({x:433.7,startPosition:2},0).wait(1).to({rotation:0.3,x:442.5,y:86.7,startPosition:3},0).wait(1).to({x:451.4,startPosition:4},0).wait(1).to({rotation:0.2,x:460.2,y:86.8,startPosition:5},0).wait(1).to({x:469.1,startPosition:6},0).wait(1).to({scaleX:1,scaleY:1,rotation:0.1,x:477.9,y:86.9,startPosition:7},0).wait(1).to({x:486.8,startPosition:8},0).wait(1).to({regX:0,regY:25.2,rotation:0,x:496,y:112.9,startPosition:0},0).to({x:504.1,startPosition:1},1,cjs.Ease.get(0.9)).to({x:592.1,startPosition:0},21).to({startPosition:6},6).to({regX:0.1,regY:25.3,rotation:-0.7,y:114.5,startPosition:8},2).to({regX:0,scaleX:1.17,scaleY:0.69,rotation:26.4,x:600.3,y:85.1,startPosition:0},1).to({regX:0.1,regY:25.2,scaleX:0.79,scaleY:1.24,rotation:44.8,x:647.6,y:23.7,startPosition:1},1).to({regX:0.2,scaleX:0.86,scaleY:1.16,rotation:61.8,x:720.2,y:-8,startPosition:2},1).to({regX:0.1,scaleX:1,scaleY:1,rotation:65.4,x:767.3,y:-27.7,startPosition:0},2).to({_off:true},1).wait(1));

        // machine_05_springboard
        this.instance_2 = new lib.machine_05_springboard("synched",0);
        this.instance_2.setTransform(653.3,122.8,1,1,42.7,0,0,61.2,0.7);

        this.timeline.addTween(cjs.Tween.get(this.instance_2).wait(4).to({regX:61.1,rotation:38.2,x:653.2},0).to({regY:0.8,rotation:-1.4},7,cjs.Ease.get(1)).to({rotation:0},3,cjs.Ease.get(1)).wait(37).to({startPosition:0},0).to({rotation:-3},2,cjs.Ease.get(1)).to({regY:0.7,rotation:38.2},2,cjs.Ease.get(1)).to({regX:61.2,rotation:42.7,x:653.3},4,cjs.Ease.get(1)).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(250.8,-139,412.8,271.5);


    (lib.Machine_C_02 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // machine_04
        this.instance = new lib.Symbol31("synched",2);
        this.instance.setTransform(423.5,-14.9,1,1,0,0,0,4.1,0.2);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(60));

        // box02
        this.instance_1 = new lib.box_V("synched",0);
        this.instance_1.setTransform(293.5,103.9,1,1,-18.7,0,0,0.1,25.3);

        this.timeline.addTween(cjs.Tween.get(this.instance_1).wait(1).to({regY:-0.3,rotation:-22.6,x:293.3,y:79.1,startPosition:1},0).wait(1).to({regY:25.3,scaleX:1,scaleY:1,rotation:-26.5,x:312.9,y:101.7,startPosition:0},0).wait(1).to({regY:-0.3,rotation:-21.2,x:312.2,y:79.9,startPosition:1},0).wait(1).to({rotation:-15.8,x:323,y:81.3,startPosition:2},0).wait(1).to({scaleX:1,scaleY:1,rotation:-10.5,x:333.9,y:83,startPosition:3},0).wait(1).to({rotation:-5.1,x:344.8,y:84.8,startPosition:4},0).wait(1).to({regX:0.3,regY:25.4,rotation:0.2,x:355.7,y:112.4,startPosition:0},0).wait(1).to({regX:0.1,regY:-0.3,rotation:4.1,x:365.9,y:85.2,startPosition:1},0).wait(1).to({regX:0.4,regY:25.4,rotation:8,x:373,y:109.1,startPosition:0},0).wait(1).to({regX:0.1,regY:-0.3,rotation:4.6,x:383.3,y:84.2,startPosition:1},0).wait(1).to({rotation:1.2,x:390.4,y:84.8,startPosition:2},0).wait(1).to({regX:0.5,regY:25.6,rotation:-2.2,x:398.8,y:111.2,startPosition:0},0).wait(1).to({regX:0.1,regY:-0.3,rotation:-0.8,x:406.7,y:86,startPosition:1},0).wait(1).to({regX:0.5,regY:25.6,rotation:0.5,x:416.2,y:112.4,startPosition:0},0).wait(1).to({regX:0.1,regY:-0.3,scaleX:1,scaleY:1,rotation:0.4,x:424.8,y:86.6,startPosition:1},0).wait(1).to({x:433.7,startPosition:2},0).wait(1).to({rotation:0.3,x:442.5,y:86.7,startPosition:3},0).wait(1).to({x:451.4,startPosition:4},0).wait(1).to({rotation:0.2,x:460.2,y:86.8,startPosition:5},0).wait(1).to({x:469.1,startPosition:6},0).wait(1).to({scaleX:1,scaleY:1,rotation:0.1,x:477.9,y:86.9,startPosition:7},0).wait(1).to({x:486.8,startPosition:8},0).wait(1).to({regX:0,regY:25.2,rotation:0,x:496,y:112.9,startPosition:0},0).to({x:504.1,startPosition:1},1,cjs.Ease.get(0.9)).to({x:592.1,startPosition:0},21).to({startPosition:6},6).to({regX:0.1,regY:25.3,rotation:-0.7,y:114.5,startPosition:8},2).to({regX:0,scaleX:1.17,scaleY:0.69,rotation:26.4,x:600.3,y:85.1,startPosition:0},1).to({regX:0.1,regY:25.2,scaleX:0.79,scaleY:1.24,rotation:44.8,x:647.6,y:23.7,startPosition:1},1).to({regX:0.2,scaleX:0.86,scaleY:1.16,rotation:61.8,x:720.2,y:-8,startPosition:2},1).to({regX:0.1,scaleX:1,scaleY:1,rotation:65.4,x:767.3,y:-27.7,startPosition:0},2).to({_off:true},1).wait(1));

        // machine_05_springboard
        this.instance_2 = new lib.machine_05_springboard("synched",0);
        this.instance_2.setTransform(653.3,122.8,1,1,42.7,0,0,61.2,0.7);

        this.timeline.addTween(cjs.Tween.get(this.instance_2).wait(4).to({regX:61.1,rotation:38.2,x:653.2},0).to({regY:0.8,rotation:-1.4},7,cjs.Ease.get(1)).to({rotation:0},3,cjs.Ease.get(1)).wait(37).to({startPosition:0},0).to({rotation:-3},2,cjs.Ease.get(1)).to({regY:0.7,rotation:38.2},2,cjs.Ease.get(1)).to({regX:61.2,rotation:42.7,x:653.3},4,cjs.Ease.get(1)).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(250.8,-139,412.8,271.5);


    (lib.Machine_B_X = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // hop
        this.instance = new lib.hop("synched",0);
        this.instance.setTransform(-122.8,149.2);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(42).to({startPosition:0},0).wait(18));

        // moving_ line
        this.instance_1 = new lib.moving_line_1("synched",0);
        this.instance_1.setTransform(-121.3,119.9,1,1,0,0,0,-2,0);

        this.timeline.addTween(cjs.Tween.get(this.instance_1).wait(40).to({regX:-61.6,regY:0.5,x:-180.9,y:120.4,mode:"single",startPosition:42},0).to({regY:0.6,rotation:2.2,x:-180.8,y:120.5},2,cjs.Ease.get(0.99)).to({regX:-61.7,regY:0.5,rotation:-61.2,x:-180.9,y:120.4},2,cjs.Ease.get(0.99)).to({regX:-61.8,rotation:-67,y:120.5},3).to({regY:0.6,rotation:0.5,y:120.4},6,cjs.Ease.get(-1)).to({rotation:-2.9},2,cjs.Ease.get(1)).to({regX:-61.9,rotation:-0.2,x:-181},2,cjs.Ease.get(-1)).to({regX:-61.6,regY:0.5,rotation:0,x:-180.9},2,cjs.Ease.get(1)).wait(1));

        // machine_02
        this.instance_2 = new lib.machine_02_X("synched",0);
        this.instance_2.setTransform(-119.9,36);

        this.timeline.addTween(cjs.Tween.get(this.instance_2).wait(60));

        // MagnifyMask (mask)
        var mask = new cjs.Shape();
        mask._off = true;
        var mask_graphics_0 = new cjs.Graphics().p("AqbC6QhAhAAAhaQAAhZBAhAQBBhABaAAQBbAABABAQBABAAABZQAABahABAQhABBhbAAQhaAAhBhBg");
        var mask_graphics_20 = new cjs.Graphics().p("AqYDvQhAhAAAhbQAAhYBAhBQBAhABbABQBagBBABAQBBBBAABYQAABbhBBAQhABBhaAAQhbAAhAhBg");
        var mask_graphics_21 = new cjs.Graphics().p("Ap0EfQhAhAAAhaQAAhbBAg+QBBhABaAAQBbAABABAQBBA+AABbQAABahBBAQhABAhbAAQhaAAhBhAg");
        var mask_graphics_22 = new cjs.Graphics().p("ApXFHQhAhAAAhaQAAhbBAhAQBAg/BbAAQBbAAA/A/QBBBAAABbQAABahBBAQg/BAhbAAQhbAAhAhAg");
        var mask_graphics_23 = new cjs.Graphics().p("Ao9FmQg/hAgBhbQABhaA/hBQBBg+BbAAQBaAABAA+QBBBBAABaQAABbhBBAQhABAhaAAQhbAAhBhAg");
        var mask_graphics_24 = new cjs.Graphics().p("AowF7QhAhAAAhbQAAhaBAhBQBBhABaAAQBbAABABAQBBBBAABaQAABbhBBAQhABAhbAAQhaAAhBhAg");
        var mask_graphics_25 = new cjs.Graphics().p("AokGLQhAhAAAhaQAAhbBAhAQBAhABbAAQBaAABABAQBBBAAABbQAABahBBAQhABAhaAAQhbAAhAhAg");
        var mask_graphics_26 = new cjs.Graphics().p("AolGPQhAhAAAhbQAAhaBAhBQBBhABaAAQBbAABABAQBABBAABaQAABbhABAQhABAhbAAQhaAAhBhAg");
        var mask_graphics_27 = new cjs.Graphics().p("Ao2GSQhAhAAAhbQAAhaBAhBQBAhABbAAQBaAABABAQBBBBAABaQAABbhBBAQhABAhaAAQhbAAhAhAg");
        var mask_graphics_28 = new cjs.Graphics().p("ApIGVQhAhAAAhaQAAhbBAhAQBBhABaAAQBbAABABAQBABAAABbQAABahABAQhABBhbAAQhaAAhBhBg");
        var mask_graphics_29 = new cjs.Graphics().p("ApZGYQhAhAAAhaQAAhbBAhAQBAhABbAAQBaAABABAQBBBAAABbQAABahBBAQhABBhaAAQhbAAhAhBg");
        var mask_graphics_30 = new cjs.Graphics().p("AprGbQhAhAAAhaQAAhbBAhAQBBhABaAAQBaAABBBAQBABAAABbQAABahABAQhBBBhaAAQhaAAhBhBg");
        var mask_graphics_31 = new cjs.Graphics().p("AqAGeQhAhAAAhbQAAhaBAhBQBAhABbAAQBaAABABAQBBBBAABaQAABbhBBAQhABAhaAAQhbAAhAhAg");
        var mask_graphics_32 = new cjs.Graphics().p("AqWGgQhAhAAAhbQAAhaBAhBQBBhABaAAQBbAABABAQBABBAABaQAABbhABAQhABAhbAAQhaAAhBhAg");
        var mask_graphics_33 = new cjs.Graphics().p("AqrGiQhAhAAAhbQAAhaBAhBQBBhABaAAQBbAABABAQBABBAABaQAABbhABAQhABAhbAAQhaAAhBhAg");
        var mask_graphics_34 = new cjs.Graphics().p("ArAGkQhAhAAAhaQAAhbBAhAQBAhABbAAQBaAABABAQBBBAAABbQAABahBBAQhABBhaAAQhbAAhAhBg");
        var mask_graphics_35 = new cjs.Graphics().p("ArVGmQhAhAAAhaQAAhbBAhAQBAhABbAAQBaAABABAQBBBAAABbQAABahBBAQhABBhaAAQhbAAhAhBg");
        var mask_graphics_36 = new cjs.Graphics().p("ArrGoQhAhAAAhaQAAhbBAhAQBBhABaAAQBbAABABAQBABAAABbQAABahABAQhABBhbAAQhaAAhBhBg");
        var mask_graphics_37 = new cjs.Graphics().p("AsAGrQhAhAAAhbQAAhbBAhAQBAhABbAAQBbAABABAQBABAAABbQAABbhABAQhABAhbAAQhbAAhAhAg");
        var mask_graphics_38 = new cjs.Graphics().p("AsVGtQhAhAAAhbQAAhaBAhBQBAhABbAAQBaAABABAQBBBBAABaQAABbhBBAQhABBhaAAQhbAAhAhBg");
        var mask_graphics_39 = new cjs.Graphics().p("Ar8GBQhAhAAAhaQAAhbBAhAQBAhABbAAQBaAABABAQBBBAAABbQAABahBBAQhABBhaAAQhbAAhAhBg");
        var mask_graphics_40 = new cjs.Graphics().p("ArBEKQhAhAAAhaQAAhaBAg/QBAhABaAAQBbAABABAQBAA/AABaQAABahABAQhABBhbAAQhaAAhAhBg");
        var mask_graphics_41 = new cjs.Graphics().p("AqPCaQhBhAABhaQgBhZBBhBQBAhABaABQBbgBBABAQBBBBgBBZQABBahBBAQhABBhbAAQhaAAhAhBg");
        var mask_graphics_42 = new cjs.Graphics().p("ApxCaQhBhAAAhaQAAhZBBhAQBAhABbAAQBaAABABAQBABAAABZQAABahABAQhABAhaAAQhbAAhAhAg");
        var mask_graphics_43 = new cjs.Graphics().p("AppCAQhAhAAAhYQAAhbBAhAQBBhABaAAQBbAABABAQBABAAABbQAABYhABAQhABAhbAAQhaAAhBhAg");

        this.timeline.addTween(cjs.Tween.get(mask).to({graphics:mask_graphics_0,x:-73.2,y:25.2}).wait(20).to({graphics:mask_graphics_20,x:-73,y:30.4}).wait(1).to({graphics:mask_graphics_21,x:-69.3,y:35.2}).wait(1).to({graphics:mask_graphics_22,x:-66.4,y:39.2}).wait(1).to({graphics:mask_graphics_23,x:-63.8,y:42.3}).wait(1).to({graphics:mask_graphics_24,x:-62.5,y:44.4}).wait(1).to({graphics:mask_graphics_25,x:-61.4,y:46}).wait(1).to({graphics:mask_graphics_26,x:-61.4,y:46.4}).wait(1).to({graphics:mask_graphics_27,x:-63.2,y:46.7}).wait(1).to({graphics:mask_graphics_28,x:-64.9,y:47}).wait(1).to({graphics:mask_graphics_29,x:-66.7,y:47.3}).wait(1).to({graphics:mask_graphics_30,x:-68.4,y:47.7}).wait(1).to({graphics:mask_graphics_31,x:-70.6,y:47.9}).wait(1).to({graphics:mask_graphics_32,x:-72.7,y:48.1}).wait(1).to({graphics:mask_graphics_33,x:-74.8,y:48.3}).wait(1).to({graphics:mask_graphics_34,x:-77,y:48.5}).wait(1).to({graphics:mask_graphics_35,x:-79.1,y:48.7}).wait(1).to({graphics:mask_graphics_36,x:-81.2,y:48.9}).wait(1).to({graphics:mask_graphics_37,x:-83.3,y:49.2}).wait(1).to({graphics:mask_graphics_38,x:-85.5,y:49.4}).wait(1).to({graphics:mask_graphics_39,x:-83,y:45}).wait(1).to({graphics:mask_graphics_40,x:-77.1,y:33.2}).wait(1).to({graphics:mask_graphics_41,x:-72.1,y:14.2}).wait(1).to({graphics:mask_graphics_42,x:-69.1,y:-11.3}).wait(1).to({graphics:mask_graphics_43,x:-68.2,y:-24.5}).wait(17));

        // Layer 2
        this.instance_3 = new lib.X_on_box("synched",0);
        this.instance_3.setTransform(-183.5,112.5,1.134,1.134,0,0,0,0,25.3);
        this.instance_3._off = true;

        this.instance_3.mask = mask;

        this.timeline.addTween(cjs.Tween.get(this.instance_3).wait(11).to({_off:false},0).wait(1).to({regX:0.1,regY:-0.3,scaleX:1.14,scaleY:1.14,x:-180.3,y:83.4,startPosition:1},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-177.3,startPosition:2},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-174.3,startPosition:3},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-171.3,y:83.3,startPosition:4},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-168.3,startPosition:5},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-165.3,startPosition:6},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-162.3,startPosition:7},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-159.3,y:83.2,startPosition:8},0).wait(1).to({scaleX:1.15,scaleY:1.15,x:-156.3,startPosition:9},0).wait(1).to({scaleX:1.15,scaleY:1.15,x:-153.3,startPosition:10},0).wait(1).to({scaleX:1.15,scaleY:1.15,x:-150.3,startPosition:11},0).wait(1).to({scaleX:1.15,scaleY:1.15,x:-147.3,y:83.1,startPosition:12},0).wait(1).to({regX:0,regY:25.3,scaleX:1.15,scaleY:1.15,x:-144.4,y:112.6,startPosition:0},0).wait(1).to({regX:0.1,regY:-0.3,scaleX:1.15,scaleY:1.15,x:-143.2,y:83.1,startPosition:1},0).wait(1).to({scaleX:1.15,scaleY:1.15,x:-142.1,y:83.2,startPosition:2},0).wait(1).to({scaleX:1.15,scaleY:1.15,x:-141,startPosition:3},0).wait(1).to({scaleX:1.15,scaleY:1.15,x:-139.9,startPosition:4},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-138.8,startPosition:5},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-137.6,y:83.3,startPosition:6},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-136.5,startPosition:7},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-135.3,startPosition:8},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-134.1,y:83.4,startPosition:9},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-132.9,startPosition:10},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-131.7,startPosition:11},0).wait(1).to({scaleX:1.13,scaleY:1.13,x:-130.4,y:83.5,startPosition:12},0).wait(1).to({scaleX:1.13,scaleY:1.13,x:-129,startPosition:13},0).wait(1).to({scaleX:1.13,scaleY:1.13,x:-127.7,startPosition:14},0).wait(1).to({scaleX:1.13,scaleY:1.13,x:-126.2,y:83.6,startPosition:15},0).wait(1).to({regX:-0.1,regY:25.2,scaleX:1.13,scaleY:1.13,x:-124.9,y:112.4,startPosition:16},0).to({_off:true},1).wait(19));

        // box02
        this.instance_4 = new lib.box("synched",0);
        this.instance_4.setTransform(-183.5,112.5,1.134,1.134,0,0,0,0,25.3);
        this.instance_4._off = true;

        this.instance_4.mask = mask;

        this.timeline.addTween(cjs.Tween.get(this.instance_4).wait(11).to({_off:false},0).wait(1).to({regX:0.1,regY:-0.3,scaleX:1.14,scaleY:1.14,x:-180.3,y:83.4,startPosition:1},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-177.3,startPosition:2},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-174.3,startPosition:3},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-171.3,y:83.3,startPosition:4},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-168.3,startPosition:5},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-165.3,startPosition:6},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-162.3,startPosition:7},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-159.3,y:83.2,startPosition:8},0).wait(1).to({scaleX:1.15,scaleY:1.15,x:-156.3,startPosition:9},0).wait(1).to({scaleX:1.15,scaleY:1.15,x:-153.3,startPosition:10},0).wait(1).to({scaleX:1.15,scaleY:1.15,x:-150.3,startPosition:11},0).wait(1).to({scaleX:1.15,scaleY:1.15,x:-147.3,y:83.1,startPosition:12},0).wait(1).to({regX:0,regY:25.3,scaleX:1.15,scaleY:1.15,x:-144.4,y:112.6,startPosition:0},0).wait(1).to({regX:0.1,regY:-0.3,scaleX:1.15,scaleY:1.15,x:-143.2,y:83.1,startPosition:1},0).wait(1).to({scaleX:1.15,scaleY:1.15,x:-142.1,y:83.2,startPosition:2},0).wait(1).to({scaleX:1.15,scaleY:1.15,x:-141,startPosition:3},0).wait(1).to({scaleX:1.15,scaleY:1.15,x:-139.9,startPosition:4},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-138.8,startPosition:5},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-137.6,y:83.3,startPosition:6},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-136.5,startPosition:7},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-135.3,startPosition:8},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-134.1,y:83.4,startPosition:9},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-132.9,startPosition:10},0).wait(1).to({scaleX:1.14,scaleY:1.14,x:-131.7,startPosition:11},0).wait(1).to({scaleX:1.13,scaleY:1.13,x:-130.4,y:83.5,startPosition:12},0).wait(1).to({scaleX:1.13,scaleY:1.13,x:-129,startPosition:13},0).wait(1).to({scaleX:1.13,scaleY:1.13,x:-127.7,startPosition:14},0).wait(1).to({scaleX:1.13,scaleY:1.13,x:-126.2,y:83.6,startPosition:15},0).wait(1).to({regX:-0.1,regY:25.2,scaleX:1.13,scaleY:1.13,x:-124.9,y:112.4,startPosition:16},0).to({_off:true},1).wait(19));

        // box02
        this.instance_5 = new lib.box("synched",0);
        this.instance_5.setTransform(-240.2,112.3,1,1,0,0,0,0,25.2);

        this.timeline.addTween(cjs.Tween.get(this.instance_5).to({x:-183.5,y:112.4},11).wait(1).to({regX:0.1,regY:-0.3,x:-180.3,y:86.9,startPosition:1},0).wait(1).to({x:-177.3,startPosition:2},0).wait(1).to({x:-174.3,startPosition:3},0).wait(1).to({x:-171.3,startPosition:4},0).wait(1).to({x:-168.3,startPosition:5},0).wait(1).to({x:-165.3,startPosition:6},0).wait(1).to({x:-162.3,startPosition:7},0).wait(1).to({x:-159.3,startPosition:8},0).wait(1).to({x:-156.3,startPosition:9},0).wait(1).to({x:-153.3,startPosition:10},0).wait(1).to({x:-150.3,startPosition:11},0).wait(1).to({x:-147.3,startPosition:12},0).wait(1).to({regX:0,regY:25.2,x:-144.4,y:112.4,startPosition:0},0).wait(1).to({regX:0.1,regY:-0.3,x:-143.2,y:86.9,startPosition:1},0).wait(1).to({x:-142.1,startPosition:2},0).wait(1).to({x:-141,startPosition:3},0).wait(1).to({x:-139.9,startPosition:4},0).wait(1).to({x:-138.7,startPosition:5},0).wait(1).to({x:-137.6,startPosition:6},0).wait(1).to({x:-136.5,startPosition:7},0).wait(1).to({x:-135.3,startPosition:8},0).wait(1).to({x:-134.1,startPosition:9},0).wait(1).to({x:-132.9,startPosition:10},0).wait(1).to({x:-131.6,startPosition:11},0).wait(1).to({x:-130.3,startPosition:12},0).wait(1).to({x:-129,startPosition:13},0).wait(1).to({x:-127.6,startPosition:14},0).wait(1).to({x:-126.2,startPosition:15},0).wait(1).to({regX:0,regY:25.2,x:-124.8,y:112.4,startPosition:16},0).to({rotation:2.5,x:-113.7,y:113.5,startPosition:18},2,cjs.Ease.get(1)).to({regX:-0.5,regY:24.8,scaleX:1.31,scaleY:0.68,rotation:-32.2,x:-122.5,y:55.4,startPosition:19},1).to({regX:-0.6,regY:25.1,scaleX:0.89,scaleY:1.22,rotation:-38.2,x:-208.3,y:-58.7,startPosition:20},1).to({x:-295.9,y:-162.5,startPosition:21},1).to({_off:true},1).wait(14));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-265.9,-26,274,193);


    (lib.Machine_B = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // machine_02
        this.instance = new lib.machine_02("synched",0);
        this.instance.setTransform(-119.9,36);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(60));

        // glass (mask)
        var mask = new cjs.Shape();
        mask._off = true;
        var mask_graphics_0 = new cjs.Graphics().p("AqZC6Qg/g/AAhZQAAhYA+g/QBAg/BZAAQBaAAA+A/QA/A/ABBYQAABZg/A/Qg/A/haAAIAAAAQhZAAg/g/g");
        var mask_graphics_19 = new cjs.Graphics().p("AqZC6Qg/g/AAhZQAAhYA+g/QBAg/BZAAQBaAAA+A/QA/A/ABBYQAABZg/A/Qg/A/haAAIAAAAQhZAAg/g/g");
        var mask_graphics_20 = new cjs.Graphics().p("ArSC9Qg/g+AAhaQAAhXA/hAQA/g/BaAAQBZAAA/A/QA/BAAABXQAABag/A+Qg/A/hZABIgBAAQhZAAg/hAg");
        var mask_graphics_21 = new cjs.Graphics().p("Ar0DAQg/g/AAhaQAAhXA/g/QA/hABZAAQBaAAA/A/QA/BAAABXQAABag/A+Qg/A/hZABIgBAAQhZAAg/g/g");
        var mask_graphics_22 = new cjs.Graphics().p("AsADBQg/g/AAhaQAAhXA/g/QA/g/BagBQBZAAA/BAQA/A/AABXQAABag/A/Qg+A/haAAIAAAAQhaAAg/g/g");
        var mask_graphics_23 = new cjs.Graphics().p("ApGEgQhZgEg8hCQg8hCAEhaQAEhXBCg8QBCg8BaAEQBZAEA8BCQA8BDgFBXQgEBahBA7Qg+A4hSAAIgMAAg");
        var mask_graphics_24 = new cjs.Graphics().p("AonE9QhZgIg5hEQg6hFAIhZQAJhXBEg5QBFg5BZAIQBZAIA5BFQA5BDgIBZQgIBZhFA4Qg8AyhMAAIgVgBg");
        var mask_graphics_25 = new cjs.Graphics().p("AoMFYQhZgLg2hHQg2hHALhZQAMhZBHg0QBHg2BZAMQBYALA2BGQA2BGgLBZQgMBZhHA2Qg6AshGAAQgPAAgQgCg");
        var mask_graphics_26 = new cjs.Graphics().p("AnzFwQhZgOgzhKQg0hIAPhYQAPhZBIgxQBJg0BZAPQBYAPAzBHQA0BJgPBYQgPBYhJAzQg4AphCAAQgTAAgTgEg");
        var mask_graphics_27 = new cjs.Graphics().p("AneGGQhXgSgxhLQgyhKAShXQAShYBKgwQBKgxBYASQBYASAxBIQAxBLgSBYQgSBXhKAxQg2Akg+AAQgWAAgYgEg");
        var mask_graphics_28 = new cjs.Graphics().p("AnKGZQhYgVgvhMQgvhLAUhXQAVhXBLgwQBMgtBXAVQBYASAuBMQAvBMgUBXQgUBXhMAvQg1Ahg6AAQgZAAgagGg");
        var mask_graphics_29 = new cjs.Graphics().p("Am6GpQhXgWgthOQgthMAWhXQAXhXBMgtQBOgrBWAUQBXAXAsBNQAuBNgXBXQgWBWhNAtQgzAeg4AAQgbAAgdgHg");
        var mask_graphics_30 = new cjs.Graphics().p("AmsG3QhWgYgshPQgshNAZhWQAYhXBOgrQBOgrBWAYQBWAYArBOQArBOgYBWQgYBWhNArQgyAcg2AAQgeAAgegIg");
        var mask_graphics_31 = new cjs.Graphics().p("AmhHCQhWgagqhPQgqhOAahWQAZhVBPgrQBOgqBWAaQBWAZAqBPQAqBPgaBWQgaBWhOApQgxAbgzAAQggAAgggKg");
        var mask_graphics_32 = new cjs.Graphics().p("AmYHLQhWgbgphQQgphPAbhVQAbhVBPgqQBPgpBVAbQBWAbAoBPQApBQgaBVQgbBVhPApQgwAZgyAAQghAAghgKg");
        var mask_graphics_33 = new cjs.Graphics().p("AmSHRQhVgcgphQQgohPAchVQAbhVBQgpQBPgoBWAcQBVAbAnBQQApBQgcBVQgbBVhQAoQgvAYgxAAQgiAAgigLg");
        var mask_graphics_34 = new cjs.Graphics().p("AmOHVQhVgdgohQQgohPAchVQAchVBQgoQBQgoBVAcQBVAcAnBQQAoBQgcBVQgcBVhQAoQgvAXgwAAQgiAAgjgLg");
        var mask_graphics_35 = new cjs.Graphics().p("AmOHVQhVgcgohQQgohQAdhVQAchVBQgoQBQgnBVAcQBUAcAoBQQAnBQgcBVQgcBVhQAoQguAXgxAAQgiAAgjgMg");
        var mask_graphics_36 = new cjs.Graphics().p("AmVHVQhUgdgnhRQgmhQAdhUQAehVBQgnQBQgmBVAdQBUAeAmBQQAnBRgdBUQgeBVhQAmQgtAWgvAAQgkAAglgNg");
        var mask_graphics_37 = new cjs.Graphics().p("AmnHWQhTghgkhSQgjhSAhhTQAghTBSgkQBSgjBUAhQBTAhAjBRQAjBTghBTQghBThRAjQgrATgqAAQgoAAgogQg");
        var mask_graphics_38 = new cjs.Graphics().p("AnFHWQhRgmgehUQgehUAmhRQAmhRBUgeQBUgeBRAmQBRAmAeBUQAeBUgmBRQgmBRhUAeQglANglAAQguAAgtgVg");
        var mask_graphics_39 = new cjs.Graphics().p("AnrHWQhOgsgXhXQgYhWAthNQAshOBWgYQBWgXBOAtQBOAsAXBWQAXBXgtBNQgsBOhWAXQgdAIgdAAQg2AAgzgdg");
        var mask_graphics_40 = new cjs.Graphics().p("AoXHVQhKgzgPhYQgQhYAzhJQA0hKBXgQQBYgPBKAzQBJAzAPBYQAQBYg0BKQgzBJhXAPQgVAEgTAAQhBAAg4gng");
        var mask_graphics_41 = new cjs.Graphics().p("ApFHTQhEg6gHhZQgIhZA5hEQA6hFBZgIQBZgHBEA6QBFA5AHBZQAHBZg5BEQg6BFhYAHIgVABQhMAAg9gyg");
        var mask_graphics_42 = new cjs.Graphics().p("ApvHPQhAg/AAhZQAAhZA/hAQA/g/BZgBQBaABA/A/QBAA/gBBZQAABZg/BAQg/A/hZAAIgBAAQhYAAg/g/g");
        var mask_graphics_43 = new cjs.Graphics().p("AoBIVQhZgGg7hEQg7hDAHhZQAGhZBEg7QBDg7BZAGQBZAHA7BEQA7BDgHBZQgHBZhDA7Qg9A1hOAAIgRgBg");
        var mask_graphics_44 = new cjs.Graphics().p("AokIaQhYgMg3hHQg3hGAMhZQALhYBHg3QBHg3BYAMQBZALA3BHQA2BHgMBYQgLBZhHA3Qg6AthHAAQgOAAgQgCg");
        var mask_graphics_45 = new cjs.Graphics().p("Ao9IcQhYgPgzhJQg0hJAPhYQAPhXBJg0QBJg0BYAPQBYAPA0BJQAzBJgPBYQgPBYhJA0Qg5AohBAAQgTAAgUgEg");
        var mask_graphics_46 = new cjs.Graphics().p("ApMIeQhXgRgyhKQgyhLARhXQARhYBLgyQBKgyBXARQBYASAyBKQAyBKgSBYQgRBXhLAyQg3Alg+AAQgVAAgXgEg");
        var mask_graphics_47 = new cjs.Graphics().p("ApPIfQhXgRgyhLQgxhLAShXQAShYBKgxQBLgxBXARQBYATAxBKQAyBLgTBXQgSBYhLAxQg2Akg+AAQgWAAgXgFg");
        var mask_graphics_48 = new cjs.Graphics().p("ApJIbQhYgRgyhJQgzhKARhYQAQhYBKgyQBKgzBXARQBYAQAzBKQAyBKgRBXQgQBYhKAzQg4Amg/AAQgVAAgVgEg");
        var mask_graphics_49 = new cjs.Graphics().p("Ao4INQhZgMg2hHQg2hHANhZQAMhYBHg2QBIg2BYAMQBYAMA2BIQA2BHgMBYQgNBZhHA2Qg6AshFAAQgQAAgQgDg");
        var mask_graphics_50 = new cjs.Graphics().p("AofH3QhZgFg7hDQg7hDAGhaQAFhZBDg7QBDg7BZAFQBaAGA7BDQA7BDgGBZQgGBahDA7Qg9A2hPAAIgQgBg");
        var mask_graphics_51 = new cjs.Graphics().p("AqaGeQhBg9gDhZQgDhZA9hCQA9hBBZgDQBZgDBCA9QBBA9ADBZQACBZg9BCQg9BBhYADIgJAAQhUAAg+g6g");
        var mask_graphics_52 = new cjs.Graphics().p("Ap9GJQhIg1gNhZQgOhYA2hIQA1hIBYgNQBYgLBIAzQBIA2ANBYQANBYg1BIQg2BIhXANQgSADgRAAQhEAAg5grg");
        var mask_graphics_53 = new cjs.Graphics().p("ApgFwQhOgsgYhWQgYhWAshOQAshMBVgYQBXgYBOAsQBOAqAXBWQAYBWgsBOQgsBOhWAYQgeAIgdAAQg2AAgygcg");
        var mask_graphics_54 = new cjs.Graphics().p("ApFFUQhTghgihTQgjhSAihTQAihRBSgiQBSgjBTAiQBTAhAiBRQAiBSghBTQgiBThSAiQgpASgqAAQgpAAgpgRg");
        var mask_graphics_55 = new cjs.Graphics().p("AotE4QhWgXgshOQgthNAYhXQAXhUBOgsQBNgsBXAXQBWAYAsBNQAsBMgYBWQgXBXhNAsQgzAcg2AAQgdAAgegIg");
        var mask_graphics_56 = new cjs.Graphics().p("AoaEfQhYgPg0hIQg0hIAOhZQAOhWBIg0QBJg1BYAPQBYAOA0BIQA0BHgOBYQgOBZhIAzQg5AqhDAAQgSAAgTgDg");
        var mask_graphics_57 = new cjs.Graphics().p("AoMEJQhZgGg6hEQg6hEAGhZQAHhXBDg6QBEg6BZAGQBaAHA5BEQA6BEgGBXQgHBZhDA6Qg9A0hOAAIgSgBg");
        var mask_graphics_58 = new cjs.Graphics().p("AoDD7QhagBg+hBQg+hAAChaQAChXBAg+QBBg+BZACQBZACA+BAQA+BBgCBXQgCBahAA9Qg+A8hXAAIgEAAg");
        var mask_graphics_59 = new cjs.Graphics().p("AqZC3Qg/g+AAhaQAAhXA+hAQBAg/BZAAQBaAAA+A/QA/BAABBXQAABag/A+Qg/BAhaAAIAAAAQhZAAg/hAg");

        this.timeline.addTween(cjs.Tween.get(mask).to({graphics:mask_graphics_0,x:-73,y:25}).wait(19).to({graphics:mask_graphics_19,x:-73,y:25}).wait(1).to({graphics:mask_graphics_20,x:-78.6,y:25.3}).wait(1).to({graphics:mask_graphics_21,x:-82,y:25.5}).wait(1).to({graphics:mask_graphics_22,x:-83.2,y:25.6}).wait(1).to({graphics:mask_graphics_23,x:-78.9,y:28.9}).wait(1).to({graphics:mask_graphics_24,x:-74.9,y:31.9}).wait(1).to({graphics:mask_graphics_25,x:-71.3,y:34.7}).wait(1).to({graphics:mask_graphics_26,x:-68.1,y:37.2}).wait(1).to({graphics:mask_graphics_27,x:-65.2,y:39.5}).wait(1).to({graphics:mask_graphics_28,x:-62.7,y:41.5}).wait(1).to({graphics:mask_graphics_29,x:-60.6,y:43.3}).wait(1).to({graphics:mask_graphics_30,x:-58.7,y:44.8}).wait(1).to({graphics:mask_graphics_31,x:-57.3,y:46}).wait(1).to({graphics:mask_graphics_32,x:-56.1,y:47}).wait(1).to({graphics:mask_graphics_33,x:-55.3,y:47.7}).wait(1).to({graphics:mask_graphics_34,x:-54.8,y:48.1}).wait(1).to({graphics:mask_graphics_35,x:-54.8,y:48.1}).wait(1).to({graphics:mask_graphics_36,x:-55.1,y:48.3}).wait(1).to({graphics:mask_graphics_37,x:-56.2,y:48.6}).wait(1).to({graphics:mask_graphics_38,x:-57.9,y:49.2}).wait(1).to({graphics:mask_graphics_39,x:-60.2,y:50}).wait(1).to({graphics:mask_graphics_40,x:-62.9,y:50.8}).wait(1).to({graphics:mask_graphics_41,x:-65.9,y:51.8}).wait(1).to({graphics:mask_graphics_42,x:-68.8,y:52.7}).wait(1).to({graphics:mask_graphics_43,x:-71.5,y:53.4}).wait(1).to({graphics:mask_graphics_44,x:-73.7,y:54}).wait(1).to({graphics:mask_graphics_45,x:-75.4,y:54.4}).wait(1).to({graphics:mask_graphics_46,x:-76.4,y:54.7}).wait(1).to({graphics:mask_graphics_47,x:-76.5,y:54.9}).wait(1).to({graphics:mask_graphics_48,x:-76.3,y:54.3}).wait(1).to({graphics:mask_graphics_49,x:-75.6,y:52.8}).wait(1).to({graphics:mask_graphics_50,x:-74.6,y:50.4}).wait(1).to({graphics:mask_graphics_51,x:-73.6,y:47.3}).wait(1).to({graphics:mask_graphics_52,x:-72.7,y:43.6}).wait(1).to({graphics:mask_graphics_53,x:-72,y:39.7}).wait(1).to({graphics:mask_graphics_54,x:-71.7,y:35.7}).wait(1).to({graphics:mask_graphics_55,x:-71.8,y:32.1}).wait(1).to({graphics:mask_graphics_56,x:-72.1,y:29}).wait(1).to({graphics:mask_graphics_57,x:-72.5,y:26.7}).wait(1).to({graphics:mask_graphics_58,x:-72.8,y:25.2}).wait(1).to({graphics:mask_graphics_59,x:-73,y:24.7}).wait(1));

        // Layer 2
        this.instance_1 = new lib.v_on_box("synched",0);
        this.instance_1.setTransform(-240.3,112.5,1.122,1.122,0,0,0,-0.1,25.3);

        this.instance_1.mask = mask;

        this.timeline.addTween(cjs.Tween.get(this.instance_1).to({regY:25.2,scaleX:1.11,scaleY:1.11,x:-183.6,y:112.4,startPosition:11},11).to({regY:25.3,scaleX:1.12,scaleY:1.12,x:-144.5,y:112.6,startPosition:24},13).to({scaleX:1.13,scaleY:1.13,x:-98,startPosition:51},27).to({scaleX:1.13,scaleY:1.13,x:-17.6,y:112.7,startPosition:59},8).wait(1));

        // box02
        this.instance_2 = new lib.box("synched",0);
        this.instance_2.setTransform(-240.3,112.5,1.122,1.122,0,0,0,-0.1,25.3);

        this.instance_2.mask = mask;

        this.timeline.addTween(cjs.Tween.get(this.instance_2).to({regY:25.2,scaleX:1.11,scaleY:1.11,x:-183.6,y:112.4},11).wait(1).to({regX:0.1,regY:-0.3,scaleX:1.11,scaleY:1.11,x:-180.3,y:84,startPosition:1},0).wait(1).to({scaleX:1.11,scaleY:1.11,x:-177.3,startPosition:2},0).wait(1).to({scaleX:1.11,scaleY:1.11,x:-174.3,startPosition:3},0).wait(1).to({scaleX:1.12,scaleY:1.12,x:-171.3,startPosition:4},0).wait(1).to({x:-168.3,y:83.9,startPosition:5},0).wait(1).to({scaleX:1.12,scaleY:1.12,x:-165.3,startPosition:6},0).wait(1).to({scaleX:1.12,scaleY:1.12,x:-162.3,startPosition:7},0).wait(1).to({scaleX:1.12,scaleY:1.12,x:-159.3,startPosition:8},0).wait(1).to({scaleX:1.12,scaleY:1.12,x:-156.3,startPosition:9},0).wait(1).to({scaleX:1.12,scaleY:1.12,x:-153.3,startPosition:10},0).wait(1).to({x:-150.3,startPosition:11},0).wait(1).to({scaleX:1.12,scaleY:1.12,x:-147.3,startPosition:12},0).wait(1).to({regX:-0.1,regY:25.3,scaleX:1.12,scaleY:1.12,x:-144.5,y:112.6,startPosition:0},0).wait(1).to({regX:0.1,regY:-0.3,x:-143.2,y:83.8,startPosition:1},0).wait(1).to({x:-142.1,startPosition:2},0).wait(1).to({scaleX:1.12,scaleY:1.12,x:-141,startPosition:3},0).wait(1).to({x:-139.9,startPosition:4},0).wait(1).to({x:-138.7,startPosition:5},0).wait(1).to({x:-137.6,startPosition:6},0).wait(1).to({x:-136.5,startPosition:7},0).wait(1).to({x:-135.3,startPosition:8},0).wait(1).to({x:-134.1,startPosition:9},0).wait(1).to({scaleX:1.12,scaleY:1.12,x:-132.9,startPosition:10},0).wait(1).to({x:-131.6,startPosition:11},0).wait(1).to({x:-130.3,startPosition:12},0).wait(1).to({x:-129,startPosition:13},0).wait(1).to({x:-127.6,startPosition:14},0).wait(1).to({x:-126.2,startPosition:15},0).wait(1).to({scaleX:1.13,scaleY:1.13,x:-124.7,startPosition:16},0).wait(1).to({x:-123.1,startPosition:17},0).wait(1).to({x:-121.4,startPosition:18},0).wait(1).to({x:-119.6,y:83.7,startPosition:19},0).wait(1).to({scaleX:1.13,scaleY:1.13,x:-117.6,startPosition:20},0).wait(1).to({x:-115.5,startPosition:21},0).wait(1).to({x:-113.2,startPosition:22},0).wait(1).to({x:-110.7,startPosition:23},0).wait(1).to({scaleX:1.13,scaleY:1.13,x:-108,startPosition:24},0).wait(1).to({x:-104.9,startPosition:25},0).wait(1).to({scaleX:1.13,scaleY:1.13,x:-101.6,startPosition:26},0).wait(1).to({regX:-0.1,regY:25.3,x:-98,y:112.6,startPosition:0},0).to({scaleX:1.13,scaleY:1.13,x:-17.6,y:112.7,startPosition:8},8).wait(1));

        // box02
        this.instance_3 = new lib.box("synched",0);
        this.instance_3.setTransform(-240.2,112.3,1,1,0,0,0,0,25.2);

        this.timeline.addTween(cjs.Tween.get(this.instance_3).to({x:-183.5,y:112.4},11).wait(1).to({regX:0.1,regY:-0.3,x:-180.3,y:86.9,startPosition:1},0).wait(1).to({x:-177.3,startPosition:2},0).wait(1).to({x:-174.3,startPosition:3},0).wait(1).to({x:-171.3,startPosition:4},0).wait(1).to({x:-168.3,startPosition:5},0).wait(1).to({x:-165.3,startPosition:6},0).wait(1).to({x:-162.3,startPosition:7},0).wait(1).to({x:-159.3,startPosition:8},0).wait(1).to({x:-156.3,startPosition:9},0).wait(1).to({x:-153.3,startPosition:10},0).wait(1).to({x:-150.3,startPosition:11},0).wait(1).to({x:-147.3,startPosition:12},0).wait(1).to({regX:0,regY:25.2,x:-144.4,y:112.4,startPosition:0},0).wait(1).to({regX:0.1,regY:-0.3,x:-143.2,y:86.9,startPosition:1},0).wait(1).to({x:-142.1,startPosition:2},0).wait(1).to({x:-141,startPosition:3},0).wait(1).to({x:-139.9,startPosition:4},0).wait(1).to({x:-138.7,startPosition:5},0).wait(1).to({x:-137.6,startPosition:6},0).wait(1).to({x:-136.5,startPosition:7},0).wait(1).to({x:-135.3,startPosition:8},0).wait(1).to({x:-134.1,startPosition:9},0).wait(1).to({x:-132.9,startPosition:10},0).wait(1).to({x:-131.6,startPosition:11},0).wait(1).to({x:-130.3,startPosition:12},0).wait(1).to({x:-129,startPosition:13},0).wait(1).to({x:-127.6,startPosition:14},0).wait(1).to({x:-126.2,startPosition:15},0).wait(1).to({x:-124.7,startPosition:16},0).wait(1).to({x:-123.1,startPosition:17},0).wait(1).to({x:-121.4,startPosition:18},0).wait(1).to({x:-119.6,startPosition:19},0).wait(1).to({x:-117.6,startPosition:20},0).wait(1).to({x:-115.5,startPosition:21},0).wait(1).to({x:-113.2,startPosition:22},0).wait(1).to({x:-110.7,startPosition:23},0).wait(1).to({x:-108,startPosition:24},0).wait(1).to({x:-104.9,startPosition:25},0).wait(1).to({x:-101.6,startPosition:26},0).wait(1).to({regX:0,regY:25.2,x:-97.9,y:112.5,startPosition:0},0).to({x:-17.5,startPosition:8},8).wait(1));

        // Strip
        this.instance_4 = new lib.hop("synched",0);
        this.instance_4.setTransform(-122.8,149.2);

        this.instance_5 = new lib.moving_line_1("synched",0);
        this.instance_5.setTransform(-121.3,119.9,1,1,0,0,0,-2,0);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.instance_5},{t:this.instance_4}]}).wait(60));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-265.9,-26,274,193);


    (lib.machine_03_NoCrate = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // clock machine_3
        this.instance = new lib.clockmachine("synched",4);
        this.instance.setTransform(-51.1,-14.8);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(60));

        // clock machine_2
        this.instance_1 = new lib.clockmachine("synched",9);
        this.instance_1.setTransform(-79.4,-14.8);

        this.timeline.addTween(cjs.Tween.get(this.instance_1).wait(60));

        // clock machine_1
        this.instance_2 = new lib.clockmachine("synched",14);
        this.instance_2.setTransform(-107.7,-14.8);

        this.timeline.addTween(cjs.Tween.get(this.instance_2).wait(60));

        // Layer 2 (mask)
        var mask = new cjs.Shape();
        mask._off = true;
        mask.graphics.p("AuRFbIAAq1IACAAIcdAAIAEAAIAAK1g");
        mask.setTransform(-131.3,52.5);

        // sampling
        this.instance_3 = new lib.Group();
        this.instance_3.setTransform(-77,45.3,1,1,0,0,0,42.7,92.3);
        this.instance_3.alpha = 0.5;

        this.instance_4 = new lib.Group_1();
        this.instance_4.setTransform(-118.4,48.4,1,1,0,0,0,50.3,95.4);
        this.instance_4.alpha = 0.5;

        this.instance_5 = new lib.Group_2();
        this.instance_5.setTransform(-173.2,50.3,1,1,0,0,0,38.5,57);
        this.instance_5.alpha = 0.5;

        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#848484").ss(2).p("AOQFdIgEAAI8aAAIAAqwIAAgJIceAAg");
        this.shape.setTransform(-131.4,52.4);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#DFDFDF").s().p("AuPFcIAAq3IAFAAIcaAAIAAKuI8aAAIAAquIAAKuIcaAAIAAAJgAOQFTg");
        this.shape_1.setTransform(-131.8,53.3);

        this.instance_3.mask = this.instance_4.mask = this.instance_5.mask = this.shape.mask = this.shape_1.mask = mask;

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_1},{t:this.shape},{t:this.instance_5},{t:this.instance_4},{t:this.instance_3}]}).wait(60));

        // 6
        this.instance_6 = new lib.Symbol36("synched",9);
        this.instance_6.setTransform(-171.7,-18.1);

        this.timeline.addTween(cjs.Tween.get(this.instance_6).wait(60));

        // 5
        this.instance_7 = new lib.Symbol36("synched",11);
        this.instance_7.setTransform(-179.7,-18.1);

        this.timeline.addTween(cjs.Tween.get(this.instance_7).wait(60));

        // 4
        this.instance_8 = new lib.Symbol36("synched",13);
        this.instance_8.setTransform(-187.7,-18.1);

        this.timeline.addTween(cjs.Tween.get(this.instance_8).wait(60));

        // 3
        this.instance_9 = new lib.Symbol36("synched",15);
        this.instance_9.setTransform(-195.7,-18.1);

        this.timeline.addTween(cjs.Tween.get(this.instance_9).wait(60));

        // 2
        this.instance_10 = new lib.Symbol36("synched",17);
        this.instance_10.setTransform(-203.7,-18.1);

        this.timeline.addTween(cjs.Tween.get(this.instance_10).wait(60));

        // 1
        this.instance_11 = new lib.Symbol36("synched",0);
        this.instance_11.setTransform(-211.7,-18.1);

        this.timeline.addTween(cjs.Tween.get(this.instance_11).wait(60));

        // green buttons
        this.instance_12 = new lib.Lights("synched",19);
        this.instance_12.setTransform(-50.7,7,1,1,0,0,0,15.6,-3.4);

        this.instance_13 = new lib.Lights_static("synched",0);
        this.instance_13.setTransform(-208.7,7.3,1,1,0,0,0,15.6,-3.4);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#999999").ss(1,1,1).p("ABUhFQALAOAHAQIhpCRQgSgBgQgIgAA7haQAHAFAHAGIh7CpQgIgEgHgGgAhbAtQgKgVAAgYQAAgrAfgfQAfgfAqAAQAIAAAHABg");
        this.shape_2.setTransform(-149.4,68.5);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_2},{t:this.instance_13},{t:this.instance_12}]}).wait(60));

        // machine
        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f().s("#848484").ss(2).p("AIzo1I2UAAQg/AAgtAtQgsAtAABAIAAIaIi+AAIAAN1MAlvAAAIAA/nIqFAAIAAG+IAAAOAI5o1IgGAAAPBK+I8fAAIAAq6IcfAAg");
        this.shape_3.setTransform(-136,17);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f("#FFFFFF").s().p("Ay2P0IAAt1IC+AAIAAoaQgBg/AtguQAsgtA/AAIWUAAIAAAOIAAgOIAAm+IKEAAIAAfngAteK+IcfAAIAAq6I8fAAgAI4o1IgFAAg");
        this.shape_4.setTransform(-136,17);

        this.shape_5 = new cjs.Shape();
        this.shape_5.graphics.f().s("#848484").ss(2).p("EApfAAcMhS+AAAQgLAAgJgIQgIgJAAgLQAAgKAIgJQAJgIALAAMBS+AAAQAMAAAIAIQAJAJAAAKQAAALgJAJQgIAIgMAAg");
        this.shape_5.setTransform(9,121.5);

        this.shape_6 = new cjs.Shape();
        this.shape_6.graphics.f("#DFDFDF").s().p("EgpeAAcQgMAAgIgIQgJgJAAgLQAAgKAJgJQAIgIAMAAMBS9AAAQAMAAAIAIQAJAJAAAKQAAALgJAJQgIAIgMAAg");
        this.shape_6.setTransform(9,121.5);

        this.shape_7 = new cjs.Shape();
        this.shape_7.graphics.f("#C5C5C5").s().p("AghAiQgOgOAAgUQAAgTAOgOQAPgOASAAQAUAAAOAOQAOAOAAATQAAAUgOAOQgOAOgUAAQgSAAgPgOg");
        this.shape_7.setTransform(-31.5,108.7);

        this.shape_8 = new cjs.Shape();
        this.shape_8.graphics.f().s("#C5C5C5").ss(1.6).p("ABZAAQAAAkgaAbQgbAagkAAQgkAAgagaQgagbAAgkQAAgjAagbQAagaAkAAQAkAAAbAaQAaAbAAAjg");
        this.shape_8.setTransform(-31.5,108.7);

        this.shape_9 = new cjs.Shape();
        this.shape_9.graphics.f().s("#888888").ss(2.3,1).p("AgchTIA5Cn");
        this.shape_9.setTransform(-163.5,109.7);

        this.shape_10 = new cjs.Shape();
        this.shape_10.graphics.f().s("#888888").ss(2.3,1).p("AgchTIA5Cn");
        this.shape_10.setTransform(-178.2,109.4);

        this.shape_11 = new cjs.Shape();
        this.shape_11.graphics.f().s("#888888").ss(2.3,1).p("AgchTIA5Cn");
        this.shape_11.setTransform(-192.8,109);

        this.shape_12 = new cjs.Shape();
        this.shape_12.graphics.f().s("#888888").ss(2.3,1).p("AgchTIA5Cn");
        this.shape_12.setTransform(-207.6,108.7);

        this.shape_13 = new cjs.Shape();
        this.shape_13.graphics.f().s("#888888").ss(2.3,1).p("AgchTIA5Cn");
        this.shape_13.setTransform(-222.3,108.3);

        this.shape_14 = new cjs.Shape();
        this.shape_14.graphics.f().s("#888888").ss(2.3,1).p("AgchTIA5Cn");
        this.shape_14.setTransform(-237,108);

        this.shape_15 = new cjs.Shape();
        this.shape_15.graphics.f().s("#888888").ss(2.3,1).p("AgchTIA5Cn");
        this.shape_15.setTransform(-251.7,107.6);

        this.shape_16 = new cjs.Shape();
        this.shape_16.graphics.f("#8EDC91").s().p("AghAiQgOgOAAgUQAAgTAOgOQAPgOASAAQATAAAPAOQAOAOAAATQAAAUgOAOQgPAOgTAAQgSAAgPgOg");
        this.shape_16.setTransform(-146.3,108.7);

        this.shape_17 = new cjs.Shape();
        this.shape_17.graphics.f().s("#848484").ss(2).p("ABZAAQAAAkgaAbQgbAagkAAQgkAAgagaQgagbAAgkQAAgjAagbQAagaAkAAQAkAAAbAaQAaAbAAAjg");
        this.shape_17.setTransform(-146.3,108.7);

        this.shape_18 = new cjs.Shape();
        this.shape_18.graphics.f("#FFFFFF").s().p("Ag+A+QgagaAAgkQAAgjAagbQAagaAkAAQAlAAAaAaQAaAbAAAjQAAAlgaAZQgbAbgkAAQgkAAgagbg");
        this.shape_18.setTransform(-146.3,108.7);

        this.shape_19 = new cjs.Shape();
        this.shape_19.graphics.f("#8EDC91").s().p("AghAiQgOgOAAgUQAAgTAOgOQAOgOATAAQATAAAPAOQAOAOAAATQAAAUgOAOQgPAOgTAAQgTAAgOgOg");
        this.shape_19.setTransform(-268.5,108.7);

        this.shape_20 = new cjs.Shape();
        this.shape_20.graphics.f().s("#848484").ss(2).p("ABZAAQAAAlgaAZQgbAbgkAAQgjAAgbgbQgagZAAglQAAgkAagaQAbgaAjAAQAkAAAbAaQAaAaAAAkg");
        this.shape_20.setTransform(-268.5,108.7);

        this.shape_21 = new cjs.Shape();
        this.shape_21.graphics.f("#FFFFFF").s().p("Ag+A/QgagbAAgkQAAgkAagaQAagaAkAAQAkAAAbAaQAaAaAAAkQAAAkgaAbQgbAagkAAQgkAAgagag");
        this.shape_21.setTransform(-268.5,108.7);

        this.shape_22 = new cjs.Shape();
        this.shape_22.graphics.f().s("#848484").ss(2).p("AK8ABQAAAlgbAZQgaAbgkAAIzFAAQgkAAgagbQgbgZAAglIAAgBQAAglAbgZQAagbAkAAITFAAQAkAAAaAbQAbAZAAAlg");
        this.shape_22.setTransform(-207.5,108.7);

        this.shape_23 = new cjs.Shape();
        this.shape_23.graphics.f("#FFFFFF").s().p("ApiBZQgkAAgbgZQgagbABgkIAAgBQgBglAagaQAbgaAkAAITEAAQAlAAAaAaQAaAaAAAlIAAABQAAAkgaAbQgaAZglAAg");
        this.shape_23.setTransform(-207.5,108.7);

        this.instance_14 = new lib.ClipGroup();
        this.instance_14.setTransform(120.5,140.3,1,1,0,0,0,69,68);

        this.instance_15 = new lib.ClipGroup_1();
        this.instance_15.setTransform(252.5,140.3,1,1,0,0,0,69,68);

        this.instance_16 = new lib.ClipGroup_2();
        this.instance_16.setTransform(-21.5,140.3,1,1,0,0,0,69,68);

        this.instance_17 = new lib.ClipGroup_3();
        this.instance_17.setTransform(-205.4,140.3,1,1,0,0,0,69,68);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.instance_17},{t:this.instance_16},{t:this.instance_15},{t:this.instance_14},{t:this.shape_23},{t:this.shape_22},{t:this.shape_21},{t:this.shape_20},{t:this.shape_19},{t:this.shape_18},{t:this.shape_17},{t:this.shape_16},{t:this.shape_15},{t:this.shape_14},{t:this.shape_13},{t:this.shape_12},{t:this.shape_11},{t:this.shape_10},{t:this.shape_9},{t:this.shape_8},{t:this.shape_7},{t:this.shape_6},{t:this.shape_5},{t:this.shape_4},{t:this.shape_3}]}).wait(60));

        // moving line
        this.instance_18 = new lib.moving_line("synched",0);
        this.instance_18.setTransform(122.4,109,1,1,180,0,0,-2.1,0);

        this.timeline.addTween(cjs.Tween.get(this.instance_18).wait(60));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-282.6,-139.7,604.5,348.2);


    (lib.machine_03 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // clock machine_3
        this.instance = new lib.clockmachine("synched",4);
        this.instance.setTransform(-51.1,-14.8);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(60));

        // clock machine_2
        this.instance_1 = new lib.clockmachine("synched",9);
        this.instance_1.setTransform(-79.4,-14.8);

        this.timeline.addTween(cjs.Tween.get(this.instance_1).wait(60));

        // clock machine_1
        this.instance_2 = new lib.clockmachine("synched",14);
        this.instance_2.setTransform(-107.7,-14.8);

        this.timeline.addTween(cjs.Tween.get(this.instance_2).wait(60));

        // sampling
        this.instance_3 = new lib.sampling("synched",0);
        this.instance_3.setTransform(-127,43,1,1,0,0,0,4.4,-5);

        this.timeline.addTween(cjs.Tween.get(this.instance_3).wait(60));

        // 6
        this.instance_4 = new lib.Symbol36("synched",9);
        this.instance_4.setTransform(-171.7,-18.1);

        this.timeline.addTween(cjs.Tween.get(this.instance_4).wait(60));

        // 5
        this.instance_5 = new lib.Symbol36("synched",11);
        this.instance_5.setTransform(-179.7,-18.1);

        this.timeline.addTween(cjs.Tween.get(this.instance_5).wait(60));

        // 4
        this.instance_6 = new lib.Symbol36("synched",13);
        this.instance_6.setTransform(-187.7,-18.1);

        this.timeline.addTween(cjs.Tween.get(this.instance_6).wait(60));

        // 3
        this.instance_7 = new lib.Symbol36("synched",15);
        this.instance_7.setTransform(-195.7,-18.1);

        this.timeline.addTween(cjs.Tween.get(this.instance_7).wait(60));

        // 2
        this.instance_8 = new lib.Symbol36("synched",17);
        this.instance_8.setTransform(-203.7,-18.1);

        this.timeline.addTween(cjs.Tween.get(this.instance_8).wait(60));

        // 1
        this.instance_9 = new lib.Symbol36("synched",0);
        this.instance_9.setTransform(-211.7,-18.1);

        this.timeline.addTween(cjs.Tween.get(this.instance_9).wait(60));

        // Layer 2
        this.instance_10 = new lib.Lights("synched",19);
        this.instance_10.setTransform(-50.7,7,1,1,0,0,0,15.6,-3.4);

        this.instance_11 = new lib.Lights("synched",0);
        this.instance_11.setTransform(-208.7,7.3,1,1,0,0,0,15.6,-3.4);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.instance_11},{t:this.instance_10}]}).wait(60));

        // machine
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#848484").ss(2).p("AIzo1I2UAAQg/AAgtAtQgsAtAABAIAAIaIi+AAIAAN1MAlvAAAIAA/nIqFAAIAAG+IAAAOAI5o1IgGAAAPBK+I8fAAIAAq6IcfAAg");
        this.shape.setTransform(-136,17);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f("#FFFFFF").s().p("Ay2P0IAAt1IC+AAIAAoaQgBg/AtguQAsgtA/AAIWUAAIAAAOIAAgOIAAm+IKEAAIAAfngAteK+IcfAAIAAq6I8fAAgAI4o1IgFAAg");
        this.shape_1.setTransform(-136,17);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f().s("#848484").ss(2).p("EApfAAcMhS+AAAQgLAAgJgIQgIgJAAgLQAAgKAIgJQAJgIALAAMBS+AAAQAMAAAIAIQAJAJAAAKQAAALgJAJQgIAIgMAAg");
        this.shape_2.setTransform(9,121.5);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f("#DFDFDF").s().p("EgpeAAcQgMAAgIgIQgJgJAAgLQAAgKAJgJQAIgIAMAAMBS9AAAQAMAAAIAIQAJAJAAAKQAAALgJAJQgIAIgMAAg");
        this.shape_3.setTransform(9,121.5);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f("#C5C5C5").s().p("AghAiQgOgOAAgUQAAgTAOgOQAPgOASAAQAUAAAOAOQAOAOAAATQAAAUgOAOQgOAOgUAAQgSAAgPgOg");
        this.shape_4.setTransform(-31.5,108.7);

        this.shape_5 = new cjs.Shape();
        this.shape_5.graphics.f().s("#C5C5C5").ss(1.6).p("ABZAAQAAAkgaAbQgbAagkAAQgkAAgagaQgagbAAgkQAAgjAagbQAagaAkAAQAkAAAbAaQAaAbAAAjg");
        this.shape_5.setTransform(-31.5,108.7);

        this.shape_6 = new cjs.Shape();
        this.shape_6.graphics.f().s("#888888").ss(2.3,1).p("AgchTIA5Cn");
        this.shape_6.setTransform(-163.5,109.7);

        this.shape_7 = new cjs.Shape();
        this.shape_7.graphics.f().s("#888888").ss(2.3,1).p("AgchTIA5Cn");
        this.shape_7.setTransform(-178.2,109.4);

        this.shape_8 = new cjs.Shape();
        this.shape_8.graphics.f().s("#888888").ss(2.3,1).p("AgchTIA5Cn");
        this.shape_8.setTransform(-192.8,109);

        this.shape_9 = new cjs.Shape();
        this.shape_9.graphics.f().s("#888888").ss(2.3,1).p("AgchTIA5Cn");
        this.shape_9.setTransform(-207.6,108.7);

        this.shape_10 = new cjs.Shape();
        this.shape_10.graphics.f().s("#888888").ss(2.3,1).p("AgchTIA5Cn");
        this.shape_10.setTransform(-222.3,108.3);

        this.shape_11 = new cjs.Shape();
        this.shape_11.graphics.f().s("#888888").ss(2.3,1).p("AgchTIA5Cn");
        this.shape_11.setTransform(-237,108);

        this.shape_12 = new cjs.Shape();
        this.shape_12.graphics.f().s("#888888").ss(2.3,1).p("AgchTIA5Cn");
        this.shape_12.setTransform(-251.7,107.6);

        this.shape_13 = new cjs.Shape();
        this.shape_13.graphics.f("#8EDC91").s().p("AghAiQgOgOAAgUQAAgTAOgOQAPgOASAAQATAAAPAOQAOAOAAATQAAAUgOAOQgPAOgTAAQgSAAgPgOg");
        this.shape_13.setTransform(-146.3,108.7);

        this.shape_14 = new cjs.Shape();
        this.shape_14.graphics.f().s("#848484").ss(2).p("ABZAAQAAAkgaAbQgbAagkAAQgkAAgagaQgagbAAgkQAAgjAagbQAagaAkAAQAkAAAbAaQAaAbAAAjg");
        this.shape_14.setTransform(-146.3,108.7);

        this.shape_15 = new cjs.Shape();
        this.shape_15.graphics.f("#FFFFFF").s().p("Ag+A+QgagaAAgkQAAgjAagbQAagaAkAAQAlAAAaAaQAaAbAAAjQAAAlgaAZQgbAbgkAAQgkAAgagbg");
        this.shape_15.setTransform(-146.3,108.7);

        this.shape_16 = new cjs.Shape();
        this.shape_16.graphics.f("#8EDC91").s().p("AghAiQgOgOAAgUQAAgTAOgOQAOgOATAAQATAAAPAOQAOAOAAATQAAAUgOAOQgPAOgTAAQgTAAgOgOg");
        this.shape_16.setTransform(-268.5,108.7);

        this.shape_17 = new cjs.Shape();
        this.shape_17.graphics.f().s("#848484").ss(2).p("ABZAAQAAAlgaAZQgbAbgkAAQgjAAgbgbQgagZAAglQAAgkAagaQAbgaAjAAQAkAAAbAaQAaAaAAAkg");
        this.shape_17.setTransform(-268.5,108.7);

        this.shape_18 = new cjs.Shape();
        this.shape_18.graphics.f("#FFFFFF").s().p("Ag+A/QgagbAAgkQAAgkAagaQAagaAkAAQAkAAAbAaQAaAaAAAkQAAAkgaAbQgbAagkAAQgkAAgagag");
        this.shape_18.setTransform(-268.5,108.7);

        this.shape_19 = new cjs.Shape();
        this.shape_19.graphics.f().s("#848484").ss(2).p("AK8ABQAAAlgbAZQgaAbgkAAIzFAAQgkAAgagbQgbgZAAglIAAgBQAAglAbgZQAagbAkAAITFAAQAkAAAaAbQAbAZAAAlg");
        this.shape_19.setTransform(-207.5,108.7);

        this.shape_20 = new cjs.Shape();
        this.shape_20.graphics.f("#FFFFFF").s().p("ApiBZQgkAAgbgZQgagbABgkIAAgBQgBglAagaQAbgaAkAAITEAAQAlAAAaAaQAaAaAAAlIAAABQAAAkgaAbQgaAZglAAg");
        this.shape_20.setTransform(-207.5,108.7);

        this.instance_12 = new lib.ClipGroup();
        this.instance_12.setTransform(120.5,140.3,1,1,0,0,0,69,68);

        this.instance_13 = new lib.ClipGroup_1();
        this.instance_13.setTransform(252.5,140.3,1,1,0,0,0,69,68);

        this.instance_14 = new lib.ClipGroup_2();
        this.instance_14.setTransform(-21.5,140.3,1,1,0,0,0,69,68);

        this.instance_15 = new lib.ClipGroup_3();
        this.instance_15.setTransform(-205.4,140.3,1,1,0,0,0,69,68);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.instance_15},{t:this.instance_14},{t:this.instance_13},{t:this.instance_12},{t:this.shape_20},{t:this.shape_19},{t:this.shape_18},{t:this.shape_17},{t:this.shape_16},{t:this.shape_15},{t:this.shape_14},{t:this.shape_13},{t:this.shape_12},{t:this.shape_11},{t:this.shape_10},{t:this.shape_9},{t:this.shape_8},{t:this.shape_7},{t:this.shape_6},{t:this.shape_5},{t:this.shape_4},{t:this.shape_3},{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(60));

        // moving line
        this.instance_16 = new lib.moving_line("synched",0);
        this.instance_16.setTransform(122.4,109,1,1,180,0,0,-2.1,0);

        this.timeline.addTween(cjs.Tween.get(this.instance_16).wait(60));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-282.6,-85.3,604.5,293.8);


    (lib.machine_01 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // clock 04
        this.instance = new lib.clock("synched",0);
        this.instance.setTransform(86.8,-53.7,1,1,94.8);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(60));

        // clock 03
        this.instance_1 = new lib.clock("synched",0);
        this.instance_1.setTransform(66.4,-53.9,1,1,0,0,0,-1.2,-0.1);

        this.timeline.addTween(cjs.Tween.get(this.instance_1).wait(60));

        // clock 02
        this.instance_2 = new lib.clock("synched",0);
        this.instance_2.setTransform(48.4,-53.8,1,1,-96.1);

        this.timeline.addTween(cjs.Tween.get(this.instance_2).wait(60));

        // clock 01
        this.instance_3 = new lib.clock("synched",0);
        this.instance_3.setTransform(29.3,-53.7,1,1,69.7);

        this.timeline.addTween(cjs.Tween.get(this.instance_3).wait(60));

        // Reflection_maker
        this.instance_4 = new lib.Reflection_maker("synched",0);
        this.instance_4.setTransform(70,21.9);

        this.timeline.addTween(cjs.Tween.get(this.instance_4).wait(60));

        // clock needle
        this.instance_5 = new lib.clockneedle("synched",0);
        this.instance_5.setTransform(-41.6,-50.9);

        this.timeline.addTween(cjs.Tween.get(this.instance_5).wait(1).to({rotation:0.2,x:-41.7,y:-50.8},0).wait(1).to({rotation:0.8,y:-50.4},0).wait(1).to({rotation:1.6,y:-49.9},0).wait(1).to({rotation:2.5,y:-49.2},0).wait(1).to({rotation:3.5,y:-48.6},0).wait(1).to({rotation:4.3,y:-48},0).wait(1).to({rotation:5,y:-47.6},0).wait(1).to({rotation:5.3,y:-47.3},0).wait(1).to({regY:3.7,rotation:5.5,x:-41.6,y:-47.2},0).wait(1).to({regY:0,rotation:4.1,x:-41.4,y:-50.9},0).wait(1).to({rotation:0.3,x:-41.6},0).wait(1).to({rotation:-5.1,x:-42},0).wait(1).to({rotation:-11.1,x:-42.4},0).wait(1).to({rotation:-16.6,x:-42.7,y:-50.8},0).wait(1).to({rotation:-21,x:-43,y:-50.7},0).wait(1).to({rotation:-23.6,x:-43.1},0).wait(1).to({regX:0.1,regY:3.6,rotation:-24.5,x:-41.6,y:-47.4},0).wait(1).to({regX:0,regY:0,rotation:-23.7,x:-43.2,y:-50.6},0).wait(1).to({rotation:-21.2,x:-43,y:-50.7},0).wait(1).to({rotation:-17.2,x:-42.8,y:-50.8},0).wait(1).to({rotation:-12,x:-42.5,y:-50.9},0).wait(1).to({rotation:-5.8,x:-42.1},0).wait(1).to({rotation:0.9,x:-41.6,y:-51},0).wait(1).to({rotation:7.9,x:-41.2},0).wait(1).to({rotation:14.7,x:-40.8,y:-50.9},0).wait(1).to({rotation:21.1,x:-40.4,y:-50.8},0).wait(1).to({rotation:26.8,x:-40.1,y:-50.6},0).wait(1).to({rotation:31.6,x:-39.8,y:-50.5},0).wait(1).to({rotation:35.5,x:-39.6,y:-50.4},0).wait(1).to({rotation:38.2,x:-39.5,y:-50.3},0).wait(1).to({rotation:39.9,x:-39.4,y:-50.2},0).wait(1).to({regY:3.6,rotation:40.4,x:-41.7,y:-47.3},0).wait(1).to({regY:0,rotation:38.5,x:-39.5,y:-50.2},0).wait(1).to({rotation:35.1,x:-39.6,y:-50.4},0).wait(1).to({regX:-0.1,regY:3.6,rotation:33.7,x:-41.8,y:-47.4},0).wait(1).to({regX:0,regY:0,rotation:34.6,x:-39.7,y:-50.3},0).wait(1).to({rotation:36.7,x:-39.6},0).wait(1).to({rotation:39.5,x:-39.4,y:-50.2},0).wait(1).to({rotation:42,x:-39.3,y:-50.1},0).wait(1).to({rotation:43.6,y:-50},0).wait(1).to({regX:-0.1,regY:3.6,rotation:44.2,x:-41.7,y:-47.4},0).wait(1).to({regX:0,regY:0,rotation:42,x:-39.3,y:-50},0).wait(1).to({rotation:38.3,x:-39.5,y:-50.2},0).wait(1).to({regX:-0.1,regY:3.6,rotation:36.7,x:-41.8,y:-47.4},0).wait(1).to({regX:0,regY:0,rotation:37,x:-39.5,y:-50.2},0).wait(1).to({rotation:37.8},0).wait(1).to({rotation:39},0).wait(1).to({rotation:40.1,x:-39.4,y:-50.1},0).wait(1).to({rotation:41.1},0).wait(1).to({rotation:41.7,x:-39.3},0).wait(1).to({regX:-0.2,regY:3.6,rotation:41.9,x:-41.8,y:-47.5},0).wait(1).to({regX:0,regY:0,rotation:37.8,x:-39.5,y:-50.3},0).wait(1).to({regX:-0.3,regY:3.6,rotation:34.4,x:-41.9,y:-47.6},0).wait(1).to({regX:0,regY:0,rotation:32.9,x:-39.7,y:-50.4},0).wait(1).to({rotation:28.4,x:-39.9,y:-50.6},0).wait(1).to({rotation:21.9,x:-40.2,y:-50.7},0).wait(1).to({rotation:14.2,x:-40.6,y:-50.8},0).wait(1).to({rotation:6.6,x:-41},0).wait(1).to({regY:3.8,rotation:0,x:-41.6,y:-47.1},0).wait(1));

        // LightsBtm
        this.instance_6 = new lib.Lights("synched",0);
        this.instance_6.setTransform(107.1,73.5,1,1,0,0,0,15.6,-3.4);

        this.timeline.addTween(cjs.Tween.get(this.instance_6).wait(60));

        // machine
        this.shape = new cjs.Shape();
        this.shape.graphics.f().s("#C5C5C5").ss(2).p("AIDGGIwEAAQgJAAgGgGQgGgGAAgJIAAriQAAgIAGgGQAGgGAJAAIQEAAQAIAAAGAGQAGAGAAAIIAALiQAAAJgGAGQgGAGgIAAg");
        this.shape.setTransform(67.5,22.6);

        this.shape_1 = new cjs.Shape();
        this.shape_1.graphics.f().s("#888888").ss(2.4).p("AfzAmMg/lAAAQgQAAgKgLQgLgKAAgQIAAgBQAAgPALgLQAKgLAQAAMA/lAABQAQAAAKAKQALALAAAPIAAABQAAAQgLAKQgKALgQAAg");
        this.shape_1.setTransform(-1.4,104.8);

        this.shape_2 = new cjs.Shape();
        this.shape_2.graphics.f("#DFDFDF").s().p("A/yAmQgQAAgKgLQgLgLAAgPIAAgBQAAgPALgLQAKgLAQAAMA/lAAAQAQAAAKALQALALAAAPIAAABQAAAPgLALQgKALgQAAg");
        this.shape_2.setTransform(-1.4,104.8);

        this.shape_3 = new cjs.Shape();
        this.shape_3.graphics.f().s("#848484").ss(2).p("AAAH9IAAAAQASAAANgNQAOgNAAgTIAAufQAAgTgOgNQgNgNgSAAIAAAAQgSAAgNANQgNANAAATIAAOfQAAATANANQANANASAAg");
        this.shape_3.setTransform(-70,55);

        this.shape_4 = new cjs.Shape();
        this.shape_4.graphics.f("#DFDFDF").s().p("AAAH9QgSAAgMgNQgOgNAAgTIAAufQAAgTAOgNQAMgNASAAIAAAAQASAAANANQANANAAATIAAOfQAAATgNANQgNANgSAAg");
        this.shape_4.setTransform(-70,55);

        this.shape_5 = new cjs.Shape();
        this.shape_5.graphics.f().s("#848484").ss(2).p("AAAH9IAAAAQASAAANgNQAOgNAAgTIAAufQAAgTgOgNQgNgNgSAAIAAAAQgSAAgNANQgNANAAATIAAOfQAAATANANQANANASAAg");
        this.shape_5.setTransform(-61,55);

        this.shape_6 = new cjs.Shape();
        this.shape_6.graphics.f("#DFDFDF").s().p("AAAH9QgSAAgMgNQgOgNAAgTIAAufQAAgTAOgNQAMgNASAAIAAAAQASAAANANQANANAAATIAAOfQAAATgNANQgNANgSAAg");
        this.shape_6.setTransform(-61,55);

        this.shape_7 = new cjs.Shape();
        this.shape_7.graphics.f().s("#848484").ss(2).p("AJHq/IyNAAQgyAAgkAjQgkAkAAAyIAAUHIWBAAIAA0HQAAgygkgkQgkgjgyAAg");
        this.shape_7.setTransform(70,31.2);

        this.shape_8 = new cjs.Shape();
        this.shape_8.graphics.f("#FFFFFF").s().p("ArALBIAA0HQAAgzAkgjQAkgjAygBISNAAQAyABAkAjQAkAjAAAzIAAUHg");
        this.shape_8.setTransform(70,31.2);

        this.shape_9 = new cjs.Shape();
        this.shape_9.graphics.f().s("#848484").ss(2).p("AFAieIAALiIp/AAIAAyGICMAAQAWAAAOAOQAOAPAAAVIAAEYIFnAAQAlAAAaAaQAbAaAAAmg");
        this.shape_9.setTransform(171.5,43.7);

        this.shape_10 = new cjs.Shape();
        this.shape_10.graphics.f("#DFDFDF").s().p("Ak+JEIAAyGICLAAQAVAAAPAOQAPAPAAAUIAAEYIFmAAQAlAAAbAbQAaAaAAAlIAALjg");
        this.shape_10.setTransform(171.5,43.7);

        this.shape_11 = new cjs.Shape();
        this.shape_11.graphics.f().s("#848484").ss(2).p("AEnoqIAARVIpNAAIAAuXIEOAAIAAi+g");
        this.shape_11.setTransform(-27,45.5);

        this.shape_12 = new cjs.Shape();
        this.shape_12.graphics.f("#DFDFDF").s().p("AklIqIAAuWIEOAAIAAi+IE9AAIAARUg");
        this.shape_12.setTransform(-27,45.5);

        this.shape_13 = new cjs.Shape();
        this.shape_13.graphics.f().s("#C5C5C5").ss(2).p("ABTAJIAAgRQAAgOgKgLQgKgKgPAAIhfAAQgOAAgLAKQgKALAAAOIAAARQAAAOAKALQALAKAOAAIBfAAQAPAAAKgKQAKgLAAgOg");
        this.shape_13.setTransform(-42.2,5);

        this.shape_14 = new cjs.Shape();
        this.shape_14.graphics.f("#FFFFFF").s().p("AgvAsQgOAAgLgKQgKgLAAgOIAAgRQAAgOAKgLQALgKAOAAIBfAAQAPAAAKAKQAKALAAAOIAAARQAAAOgKALQgKAKgPAAg");
        this.shape_14.setTransform(-42.2,5);

        this.instance_7 = new lib.ClipGroup_0_1();
        this.instance_7.setTransform(-41.6,-47,1,1,0,0,0,8.3,11.3);

        this.shape_15 = new cjs.Shape();
        this.shape_15.graphics.f().s("#848484").ss(1.6).p("ACVAAQAAA9gsAsQgsAsg9AAQg9AAgrgsQgsgsAAg9QAAg8AsgsQArgsA9AAQA9AAAsAsQAsAsAAA8g");
        this.shape_15.setTransform(-41.6,-50);

        this.shape_16 = new cjs.Shape();
        this.shape_16.graphics.f("#C5C5C5").s().p("AhoBpQgrgsAAg9QAAg8ArgsQAsgsA8AAQA9AAAsAsQAsAsAAA8QAAA9gsAsQgsAsg9AAQg8AAgsgsg");
        this.shape_16.setTransform(-41.6,-50);

        this.shape_17 = new cjs.Shape();
        this.shape_17.graphics.f().s("#848484").ss(2).p("AgHBTIAQAAQAPAAAKgKQAKgLAAgOIAAhfQAAgPgKgKQgKgKgPAAIgQAAQgPAAgKAKQgKAKAAAPIgBBfQAAAOALALQAKAKAPAAg");
        this.shape_17.setTransform(9.9,-52.5);

        this.shape_18 = new cjs.Shape();
        this.shape_18.graphics.f("#DFDFDF").s().p("AgIBTQgOAAgLgKQgKgLAAgOIAAhfQABgPAJgKQALgKAOAAIARAAQAPAAAKAKQAKAKAAAPIAABfQAAAOgKALQgKAKgPAAg");
        this.shape_18.setTransform(9.9,-52.5);

        this.shape_19 = new cjs.Shape();
        this.shape_19.graphics.f().s("#C5C5C5").ss(2).p("AAUmGIAACXIgmCgIAmCPIAAFH");
        this.shape_19.setTransform(31.7,0);

        this.instance_8 = new lib.ClipGroup_4();
        this.instance_8.setTransform(-119.5,124.6,1,1,0,0,0,69,68);

        this.instance_9 = new lib.ClipGroup_1_1();
        this.instance_9.setTransform(185.5,124.6,1,1,0,0,0,69,68);

        this.instance_10 = new lib.ClipGroup_2_1();
        this.instance_10.setTransform(-16.5,124.6,1,1,0,0,0,69,68);

        this.instance_11 = new lib.Symbol13("synched",0);
        this.instance_11.setTransform(110.6,-53.2,0.899,0.899,90);
        this.instance_11.filters = [new cjs.ColorFilter(0, 0, 0, 1, 197, 197, 197, 0)];
        this.instance_11.cache(-11,-12,23,23);

        this.shape_20 = new cjs.Shape();
        this.shape_20.graphics.f().s("#848484").ss(2).p("AIJiLIwRAAQgNAAgKAJQgJAKAAANIAAD3IRRAAIAAj3QAAgNgJgKQgKgJgNAAg");
        this.shape_20.setTransform(70,-53.3);

        this.shape_21 = new cjs.Shape();
        this.shape_21.graphics.f("#FFFFFF").s().p("AooCLIAAj2QAAgNAJgJQAKgKANABIQRAAQANgBAKAKQAJAJAAANIAAD2g");
        this.shape_21.setTransform(70,-53.3);

        this.shape_22 = new cjs.Shape();
        this.shape_22.graphics.f().s("#C5C5C5").ss(2).p("Ak1FFIBGAAIAApCIIlAAIAAhGIpNgBQgPAAgIAGQgHAFAAAOg");
        this.shape_22.setTransform(-14.5,-22.5);

        this.shape_23 = new cjs.Shape();
        this.shape_23.graphics.f("#FFFFFF").s().p("Ak1FFIAApwQAAgOAIgFQAGgGAPAAIJOABIAABGIokAAIAAJCg");
        this.shape_23.setTransform(-14.5,-22.5);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.shape_23},{t:this.shape_22},{t:this.shape_21},{t:this.shape_20},{t:this.instance_11},{t:this.instance_10},{t:this.instance_9},{t:this.instance_8},{t:this.shape_19},{t:this.shape_18},{t:this.shape_17},{t:this.shape_16},{t:this.shape_15},{t:this.instance_7},{t:this.shape_14},{t:this.shape_13},{t:this.shape_12},{t:this.shape_11},{t:this.shape_10},{t:this.shape_9},{t:this.shape_8},{t:this.shape_7},{t:this.shape_6},{t:this.shape_5},{t:this.shape_4},{t:this.shape_3},{t:this.shape_2},{t:this.shape_1},{t:this.shape}]}).wait(60));

        // moving line
        this.instance_12 = new lib.movingline("synched",0);
        this.instance_12.setTransform(-66.1,93,1,1,0,0,0,-2.1,0);

        this.instance_13 = new lib.movingline("synched",0);
        this.instance_13.setTransform(69.1,93,1,1,180,0,0,-2.1,0);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.instance_13},{t:this.instance_12}]}).wait(60));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-227.7,-145.2,482.5,338);


    (lib.Machine_C_01NoCrate = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 3
        this.instance = new lib.Bulb("synched",0);
        this.instance.setTransform(177.5,84.5,0.524,0.524,0,0,0,20.5,20.5);
        this.instance.alpha = 0.57;

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(60));

        // machine_03
        this.instance_1 = new lib.machine_03_NoCrate("synched",0);
        this.instance_1.setTransform(252.2,36.7,1,1,0,0,0,19.6,24.4);

        this.timeline.addTween(cjs.Tween.get(this.instance_1).wait(60));

        // Machine_03_bg
        this.instance_2 = new lib.ClipGroup_0();
        this.instance_2.setTransform(107.6,55.6,1.077,1.266,0,0,0,96,95.4);

        this.timeline.addTween(cjs.Tween.get(this.instance_2).wait(60));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-50.1,-73,566.6,246);


    (lib.Machine_C_01_X = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 3
        this.instance = new lib.x("synched",0);
        this.instance.setTransform(177.5,84.5,0.524,0.524);
        this.instance.alpha = 0;
        this.instance._off = true;

        this.instance_1 = new lib.v("synched",0);
        this.instance_1.setTransform(177.5,84.5,0.524,0.524);
        this.instance_1.alpha = 0;

        this.timeline.addTween(cjs.Tween.get({}).to({state:[]}).to({state:[{t:this.instance}]},24).to({state:[{t:this.instance}]},2).to({state:[{t:this.instance}]},1).to({state:[{t:this.instance}]},4).to({state:[{t:this.instance}]},23).to({state:[{t:this.instance_1}]},5).wait(1));
        this.timeline.addTween(cjs.Tween.get(this.instance).wait(24).to({_off:false},0).to({alpha:1},2).to({alpha:0},1).to({alpha:1},4).wait(23).to({startPosition:0},0).to({_off:true,alpha:0},5).wait(1));

        // Layer 2
        this.instance_2 = new lib.Bulb("synched",0);
        this.instance_2.setTransform(177.5,84.5,0.524,0.524,0,0,0,20.5,20.5);
        this.instance_2.alpha = 0.57;

        this.timeline.addTween(cjs.Tween.get(this.instance_2).wait(60));

        // machine_03
        this.instance_3 = new lib.machine_03("synched",0);
        this.instance_3.setTransform(252.2,36.7,1,1,0,0,0,19.6,24.4);

        this.timeline.addTween(cjs.Tween.get(this.instance_3).wait(60));

        // box02
        this.instance_4 = new lib.box("synched",0);
        this.instance_4.setTransform(2.6,112.5,1,1,0,0,0,0,25.2);

        this.timeline.addTween(cjs.Tween.get(this.instance_4).to({x:240.9,y:112.7},39).wait(1).to({regX:0.1,regY:-0.3,x:246.8,y:87.2,startPosition:1},0).wait(1).to({x:252.7,startPosition:2},0).wait(1).to({x:258.5,startPosition:3},0).wait(1).to({x:264.4,startPosition:4},0).wait(1).to({x:270.2,startPosition:5},0).wait(1).to({regX:0,regY:25.2,x:276,y:112.7,startPosition:6},0).to({x:281.8,startPosition:7},1).to({_off:true},1).wait(13));

        // Machine_03_bg
        this.instance_5 = new lib.ClipGroup_0();
        this.instance_5.setTransform(107.6,55.6,1.077,1.266,0,0,0,96,95.4);

        this.timeline.addTween(cjs.Tween.get(this.instance_5).wait(60));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-50.1,-127.3,566.6,300.3);


    (lib.Machine_C_01 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 3
        this.instance = new lib.v("synched",0);
        this.instance.setTransform(177.5,84.5,0.524,0.524);
        this.instance.alpha = 0;
        this.instance._off = true;

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(25).to({_off:false},0).to({alpha:1},7).wait(21).to({startPosition:0},0).to({alpha:0},6).wait(1));

        // Layer 2
        this.instance_1 = new lib.Bulb("synched",0);
        this.instance_1.setTransform(177.5,84.5,0.524,0.524,0,0,0,20.5,20.5);
        this.instance_1.alpha = 0.57;

        this.timeline.addTween(cjs.Tween.get(this.instance_1).wait(60));

        // machine_03
        this.instance_2 = new lib.machine_03("synched",0);
        this.instance_2.setTransform(252.2,36.7,1,1,0,0,0,19.6,24.4);

        this.timeline.addTween(cjs.Tween.get(this.instance_2).wait(60));

        // box02
        this.instance_3 = new lib.box("synched",0);
        this.instance_3.setTransform(2.6,112.5,1,1,0,0,0,0,25.2);

        this.instance_4 = new lib.box_V("synched",0);
        this.instance_4.setTransform(352.7,113,0.829,1.446,0,0,0,0,25.3);
        this.instance_4._off = true;

        this.timeline.addTween(cjs.Tween.get(this.instance_3).to({x:240.9,y:112.7},39).wait(1).to({regX:0.1,regY:-0.3,x:246.8,y:87.2,startPosition:1},0).wait(1).to({x:252.7,startPosition:2},0).wait(1).to({x:258.5,startPosition:3},0).wait(1).to({x:264.4,startPosition:4},0).wait(1).to({x:270.2,startPosition:5},0).wait(1).to({x:276,startPosition:6},0).wait(1).to({x:281.9,startPosition:7},0).wait(1).to({x:287.7,startPosition:8},0).wait(1).to({x:293.6,startPosition:9},0).wait(1).to({x:299.4,startPosition:10},0).wait(1).to({x:305.3,startPosition:11},0).wait(1).to({x:311.1,startPosition:12},0).wait(1).to({x:316.9,startPosition:13},0).wait(1).to({x:322.8,startPosition:14},0).wait(1).to({x:328.6,startPosition:15},0).wait(1).to({x:334.5,startPosition:16},0).wait(1).to({regX:0,regY:25.2,x:340.2,y:112.8,startPosition:0},0).to({_off:true,regY:25.3,scaleX:0.83,scaleY:1.45,x:352.7,y:113},1).wait(3));
        this.timeline.addTween(cjs.Tween.get(this.instance_4).wait(56).to({_off:false},1).to({regY:25.4,scaleX:1.16,scaleY:0.89,x:331.8,y:99.4},1).to({regY:25.2,scaleX:1,scaleY:1,rotation:-10.5,x:308.3,y:104},1).wait(1));

        // Machine_03_bg
        this.instance_5 = new lib.ClipGroup_0();
        this.instance_5.setTransform(107.6,55.6,1.077,1.266,0,0,0,96,95.4);

        this.timeline.addTween(cjs.Tween.get(this.instance_5).wait(60));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-50.1,-127.3,566.6,300.3);


    (lib.Machine_A = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{"step 1":0});

        // pipe
        this.instance = new lib.pipe("synched",0);
        this.instance.setTransform(-549.4,-82.2);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(60));

        // machine_01
        this.instance_1 = new lib.machine_01("synched",0);
        this.instance_1.setTransform(-404.8,51.9,1,1,0,0,0,13.6,23.9);

        this.timeline.addTween(cjs.Tween.get(this.instance_1).wait(60));

        // box02
        this.instance_2 = new lib.box("synched",0);
        this.instance_2.setTransform(-549,77.7,0.967,1.141,0,0,0,0.1,25.4);
        this.instance_2._off = true;

        this.timeline.addTween(cjs.Tween.get(this.instance_2).wait(8).to({_off:false},0).to({regX:0.2,scaleX:0.84,scaleY:1.2,y:112.6},2,cjs.Ease.get(1)).to({regX:0.1,regY:25.3,scaleX:1.35,scaleY:0.75,y:112.2},1,cjs.Ease.get(1)).to({regY:25.4,scaleX:0.95,scaleY:1.15,x:-549.1},2).to({regX:0,regY:25.2,scaleX:1,scaleY:1,x:-549.2,y:112.1},2).to({startPosition:0},1).to({x:-322.2,y:112.3},26).wait(18));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-646.1,-175.7,458.4,348.7);


    (lib.MACHINE = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{MachineBBad:60,badSampling:180,step2:0,"step 3":60,"step 4":120,step5:180});

        // FirstLoopJumper
        this.instance = new lib.machine_05_springboard("synched",0);
        this.instance.setTransform(653,122.9,1,1,0,0,0,61.1,0.8);

        this.timeline.addTween(cjs.Tween.get(this.instance).to({_off:true},14).wait(286));

        // FirstLoop_Mask (mask)
        var mask = new cjs.Shape();
        mask._off = true;
        var mask_graphics_0 = new cjs.Graphics().p("EgasAmQMAAAhMfMA1ZAAAMAAABMfg");

        this.timeline.addTween(cjs.Tween.get(mask).to({graphics:mask_graphics_0,x:343.1,y:79.9}).wait(14).to({graphics:null,x:0,y:0}).wait(286));

        // Machine_C_02
        this.instance_1 = new lib.Machine_C_02("synched",0);
        this.instance_1.setTransform(538.2,30.8,1,1,0,0,0,538.5,30.8);

        this.instance_2 = new lib.Machine_C_02_FirstNoCreate("synched",0,false);
        this.instance_2.setTransform(538.2,30.8,1,1,0,0,0,538.5,30.8);

        this.instance_3 = new lib.Machine_C_02_NoCrate("synched",0);
        this.instance_3.setTransform(538.2,30.8,1,1,0,0,0,538.5,30.8);

        this.instance_4 = new lib.Machine_C_02_NoCrate_In("synched",0);
        this.instance_4.setTransform(538.2,30.8,1,1,0,0,0,538.5,30.8);

        this.instance_1.mask = this.instance_2.mask = this.instance_3.mask = this.instance_4.mask = mask;

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.instance_1}]}).to({state:[{t:this.instance_2}]},120).to({state:[{t:this.instance_3}]},60).to({state:[{t:this.instance_4}]},60).wait(60));

        // Machine_C_01
        this.instance_5 = new lib.Machine_C_01("synched",0);
        this.instance_5.setTransform(251.8,46.6,1,1,0,0,0,252.1,46.6);

        this.instance_6 = new lib.Machine_C_01NoCrate("synched",0);
        this.instance_6.setTransform(251.8,46.6,1,1,0,0,0,252.1,46.6);

        this.instance_7 = new lib.Machine_C_01_X("synched",0);
        this.instance_7.setTransform(251.8,46.6,1,1,0,0,0,252.1,46.6);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.instance_5}]}).to({state:[{t:this.instance_6}]},120).to({state:[{t:this.instance_7}]},60).to({state:[{t:this.instance_5}]},60).wait(60));

        // Machine_A
        this.instance_8 = new lib.Machine_A("synched",0);
        this.instance_8.setTransform(-407.1,22.4,1,1,0,0,0,-406.8,22.4);

        this.timeline.addTween(cjs.Tween.get(this.instance_8).wait(180).to({startPosition:4},0).wait(60).to({startPosition:0},0).wait(60));

        // Machine_B
        this.instance_9 = new lib.Machine_B("synched",0);
        this.instance_9.setTransform(-129.1,36,1,1,0,0,0,-128.8,36);

        this.instance_10 = new lib.Machine_B_X("synched",59);
        this.instance_10.setTransform(-129.1,36,1,1,0,0,0,-128.8,36);

        this.timeline.addTween(cjs.Tween.get({}).to({state:[{t:this.instance_9}]}).to({state:[{t:this.instance_10}]},60).to({state:[{t:this.instance_9}]},60).to({state:[{t:this.instance_9}]},60).to({state:[{t:this.instance_9}]},60).wait(60));
        this.timeline.addTween(cjs.Tween.get(this.instance_9).to({_off:true},60).wait(60).to({_off:false},0).wait(60).to({startPosition:0},0).wait(60).to({startPosition:0},0).wait(60));

        // Arm
        this.instance_11 = new lib.robot_arm("single",0);
        this.instance_11.setTransform(243.6,-5.1,1,1,0,0,0,-2.7,0);

        this.timeline.addTween(cjs.Tween.get(this.instance_11).wait(219).to({mode:"synched",startPosition:38,loop:false},0).wait(81));

        // computer
        this.instance_12 = new lib.computer("synched",0);
        this.instance_12.setTransform(68.2,-86.5,1,1,0,0,0,-2.5,0.5);

        this.timeline.addTween(cjs.Tween.get(this.instance_12).wait(300));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(-646.4,-175.7,1309.5,348.7);


// stage content:
    (lib.jfrog_v45_Html_5 = function(mode,startPosition,loop) {
        this.initialize(mode,startPosition,loop,{});

        // Layer 1
        this.instance = new lib.MACHINE();
        this.instance.setTransform(693.3,240.8,1,1,0,0,0,8.7,74.5);

        this.timeline.addTween(cjs.Tween.get(this.instance).wait(1));

    }).prototype = p = new cjs.MovieClip();
    p.nominalBounds = new cjs.Rectangle(729.4,155.6,1317.6,500.5);

})(lib = lib||{}, images = images||{}, createjs = createjs||{}, ss = ss||{});
var lib, images, createjs, ss;


