/*
   Copyright 2012, Telum Slavonski Brod, Croatia.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
   This file is part of QFramework project, and can be used only as part of project.
   Should be used for peace, not war :)   
*/

// when camera is changed we can get coordinates for screen bounds
// we also have information about screen width/height

// setup layout
function setuplayout()
{
	// round to fixed for better display, but use full float in calculations
    var x1 = Q.layout.hudxmin.toFixed(1);
    var x2 = Q.layout.hudxmax.toFixed(1);    
    var y1 = Q.layout.hudymin.toFixed(1);
    var y2 = Q.layout.hudymax.toFixed(1);
    
    var wx1 = Q.layout.worldxmin.toFixed(1);
    var wx2 = Q.layout.worldxmax.toFixed(1);    
    var wy1 = Q.layout.worldymin.toFixed(1);
    var wy2 = Q.layout.worldymax.toFixed(1);

	var areas = new Array();
	
	// add text labels
	var item = new LayoutArea();
	item.type = "text.mline";   // multiline
	item.background="FFEE1111";
	item.location= "0.0,0.0,0.0";
	item.size = "2,22";			// size 2 rows with 22 chars
	item.bounds="3.2,0.4";
	item.text = " this is on world domain ";
	areas.push(item);

	var item = new LayoutArea();
	item.type = "text.mline";   // multiline
	item.background="FFEE1111";
	item.location= "0.0,0.8,0.0";
	item.display ="hud";
	item.size = "2,22";			// size 2 rows with 22 chars
	item.bounds="3.2,0.4";
	item.text = " this is on hud domain ";
	areas.push(item);

	var item = new LayoutArea();
	item.type = "text.mline";   // multiline
	item.background="FF1111EE";
	item.location= "0.0,0.0,0.0";
	item.display ="domain1";
	item.size = "2,22";			// size 2 rows with 22 chars
	item.bounds="12.0,4.0";
	item.text = " this is on domain1 ";
	areas.push(item);
	

	var item = new LayoutArea();
	item.type = "text.mline";   // multiline
	item.background="FF11EE11";
	item.location= "0.0,0.0,2.0";
	item.display ="domain2";
	item.size = "2,22";			// size 2 rows with 22 chars
	item.bounds="4.5,1.0";
	item.text = " this is on domain2 ";
	areas.push(item);
	
	// add exit area
	var areaExit = new LayoutArea();    
	areaExit.type = 'layout.back';
	areaExit.background = 'FFFFFFFF,icons.2.8.8';
	areaExit.location= '0.0,0.4';
	areaExit.bounds = '0.20,0.20';
	areaExit.display = 'hud';
	areaExit.onclick = 'js:test_exit';
    areas.push(areaExit);
    
	Q.layout.add("domains", areas).now();
	// show page
	Q.layout.show("domains").now();	
	
	
}


function test_exit(area,index)
{
	Q.startUpdate();
	Q.layout.clear("domains");
	// go to default
	Q.camera.fit( 4.0,0);
	Q.camera.fitHud( 4.0,0);
	Q.layout.show('mainmenu');
	
	Q.domains.remove("domain1");
	Q.domains.remove("domain2");
	
	Q.sendUpdate();
}

function setupDomain()
{
	Q.startUpdate();
	Q.domains.create("domain1",1,"0,0,0.4,0.2");
	Q.domains.create("domain2",2,"0.6,0.8,0.4,0.2");
	Q.domains.show("domain1");
	Q.domains.show("domain2");
	Q.sendUpdate();
}



// change camera to see difference
Q.camera.set(0,0,0, 0,-2,2).now();
// put layout into queue to allow camera change to take effect
setupDomain();
Q.evals(0,"setuplayout();").now();

