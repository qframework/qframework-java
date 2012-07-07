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

package com.qframework.core;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.media.opengl.GL2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ObjectsFactory {

	private HashMap<String, LayoutItem>		mItems = new HashMap<String, LayoutItem>();
	private GameonApp 	mApp;
	
	ObjectsFactory(GameonApp parent)
	{
		mApp = parent;
	}

	public LayoutItem get(String name)
	{
		LayoutItem model = mItems.get(name);
		return model;
	}
	void addModel( String name, LayoutItem item, String domainname)
	{
		if ( mItems.containsKey(name))
		{
			return;
		}
		item.mModel.mEnabled = true;
		
		RenderDomain domain = mApp.world().getDomainByName(domainname);
		if (domain != null)
		{
			mApp.world().add(item.mModel);
		}else
		{
			mApp.world().add(item.mModel);
		}		
		
		mItems.put(name, item);
	}
	
	void removeModel( String name)
	{
		if (! mItems.containsKey(name))
		{
			return;
		}	
		LayoutItem item = mItems.get(name);
	
		GameonModel model = item.mModel;
		//model.setVisible(false);
		mItems.remove( name);
		mApp.world().remove(model);
		// todo check if model hangs on in world
	}

	public void create(String name, String data, String loc, String color) {
		if ( mItems.containsKey(name))
		{
			return;
		}
		
		
		GameonModel model = mApp.items().getFromTemplate(name,data, color);
		if (model != null)
		{
			GameonModel modelnew = model.copyOfModel();
			
			LayoutItem item = new LayoutItem(mApp);
			item.mModel = modelnew;
			if (loc != null)
			{
				RenderDomain domain = mApp.world().getDomainByName(loc);
				//model.mLoc = domain.mRenderId;
				addModel(name,item, loc);
			}else
			{
				addModel(name,item, null);
			}
			
			
		}
	}	
	public void place(String name, String data, String state) {
		GameonModel.RefId refid = this.refId(name);
		
		LayoutItem item = mItems.get(refid.name);
		if (item == null)
		{
			return;
		}
		GameonModel model = item.mModel;

		float[] coords = new float[3];
		ServerkoParse.parseFloatArray(coords, data);

		GameonModelRef ref = model.getRefById(refid , 0);
		ref.setPosition(coords);		
		ref.set();
		
		if (state != null)
		{
			boolean visible = false;
			if (state.equals("visible"))
			{
				visible = true;
			}
			ref.setVisible(visible);			
		}

	}
	public void scale(String name, String data) {
		GameonModel.RefId refid = this.refId(name);
		
		LayoutItem item = mItems.get(refid.name);
		if (item == null)
		{
			return;
		}
		GameonModel model = item.mModel;
		float[] scale = new float[3];
		ServerkoParse.parseFloatArray(scale, data);
		GameonModelRef ref = model.getRefById(refid, 0);
		ref.setScale(scale);
		ref.set();
	
	}
	
	public void rotate(String name, String data) {
		
		GameonModel.RefId refid = this.refId(name);
		
		LayoutItem item = mItems.get(refid.name);
		if (item == null)
		{
			return;
		}
		GameonModel model = item.mModel;
		
		float[] vals = new float[3];
		ServerkoParse.parseFloatArray(vals , data );
        GameonModelRef r = model.ref(refid.id);
        r.setRotate(vals);
        r.set();
	}

	
	public void texture(String name, String data, String submodel) {
		GameonModel.RefId refid = this.refId(name);
		LayoutItem item = mItems.get(refid.name);
		if (item == null)
		{
			return;
		}
		GameonModel model = item.mModel;
		GameonModelRef r = model.getRefById(refid , 0);
		
		if (data != null && data.length() > 0)
		{
			int text = mApp.textures().getTexture(data);
			model.setTexture(text);
		}
		
		if (submodel != null && submodel.length() > 0)
		{
			int[] arr = new int[2];
			ServerkoParse.parseIntArray(arr,submodel);
			r.setOwner(arr[0] , arr[1]);
		}
		
	}
	
	//TODO mutliple references with name.refid , default 0!
	public void state(String name, String data) {
		GameonModel.RefId refid = this.refId(name);
		
		LayoutItem item = mItems.get(refid.name);
		if (item == null)
		{
			return;
		}
		GameonModel model = item.mModel;
		GameonModelRef ref = model.getRefById(refid , 0);
		boolean visible = false;
		if (data.equals("visible"))
		{
			visible = true;
		}
		
		if (model.ref(refid.id) == null)
		{
			this.place(name, "0,0,0", null);
		}
		
		ref.setVisible(visible);
		//model.setVisible(visible);
	}
	
	public void remove(String name, String data) {
		LayoutItem item = mItems.get(name);
		if (item == null)
		{
			return;
		}
		GameonModel model = item.mModel;
		removeModel(name);
	}
	
	public void initObjects(GL2 gl, JSONObject response)
    {
        // init layout
		try {
	    	JSONArray areas;
			areas = response.getJSONArray("object");
        
	        for (int a=0; a< areas.length(); a++)
	        {
	            JSONObject pCurr = areas.getJSONObject(a);
	            processObject(gl, pCurr);
	        }
		} catch (JSONException e) {
			e.printStackTrace();
        }

	}

    private void processObject(GL2 gl, JSONObject objData) {
    	try {
			String name = objData.getString("name");
			String template = objData.getString("template");
			String color= null;
			if (objData.has("color"))
			{
				color= objData.getString("color");
			}
			
			create(name , template, null , color);
			
			if (objData.has("location"))
			{
				String data = objData.getString("location");
				place(name, data, null);
			}
			if (objData.has("bounds"))
			{
				String data = objData.getString("bounds");
				scale(name, data);
			}			
			if (objData.has("texture"))
			{
				String data = objData.getString("texture");
				texture(name, data , "");
			}			
			if (objData.has("state"))
			{
				String data = objData.getString("state");
				state(name, data);
			}
			if (objData.has("iter"))
			{
				String data = objData.getString("iter");
				setIter(name, data);
			}
			if (objData.has("onclick"))
			{
				String data = objData.getString("onclick");
				setOnClick(name, data);
			}		
    	}
    	catch (JSONException e) {
    		e.printStackTrace();
        }    	
    }
    


	private GameonModel.RefId refId(String name)
    {
		GameonModel.RefId refdata = new GameonModel.RefId();
    	StringTokenizer tok = new StringTokenizer(name,".");
    	int count = tok.countTokens();
    	if ( count == 2)
    	{
    		
    		refdata.name = tok.nextToken();
    		String refid = tok.nextToken();
    		refdata.id = Integer.parseInt(refid);
    	}else if (count == 3)
		{
    		refdata.name = tok.nextToken();
    		tok.nextToken();
    		// we only support iter
    		refdata.alias = tok.nextToken();
    		refdata.id = -1;
		}else
    	{
    		refdata.name = name;
    		refdata.id = 0;
    	}
    	return refdata;
    }
    

	GameonModelRef getRef(String name)
	{
		GameonModel.RefId refid = this.refId(name);
		LayoutItem item = mItems.get(refid.name);
		if (item == null)
		{
			return null;
		}
		GameonModel model = item.mModel;
		GameonModelRef ref = model.getRefById(refid,0);
		return ref;
	}    
	

    private void setIter(String name, String data) 
    {
		GameonModel.RefId refid = this.refId(name);
		LayoutItem item = mItems.get(refid.name);
		if (item == null)
		{
			return;
		}
		GameonModel model = item.mModel;
		int num = Integer.parseInt(data);
		model.setupIter(num);
	}

    private void setOnClick(String name, String data) 
    {
		GameonModel.RefId refid = this.refId(name);
		LayoutItem item = mItems.get(refid.name);
		if (item == null)
		{
			return;
		}
		GameonModel model = item.mModel;
		model.mOnClick = data;
	}


}
