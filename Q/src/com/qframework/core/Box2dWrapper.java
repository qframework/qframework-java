package com.qframework.core;

import java.util.HashMap;
import java.util.Vector;

import javax.media.opengl.GL2;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Box2dWrapper {

	private HashMap<String,Box2dData>	mBox2dWorlds = new HashMap<String,Box2dData>();
	private Vector<Box2dData>	mBox2dWorldsVec = new Vector<Box2dData>();
	private GameonApp mApp;
	
	
	public static int MAPPING_XY = 0;
	public static int MAPPING_YZ = 1;
	public static int MAPPING_XZ = 2;

	public static int TYPE_FIXED = 0;
	public static int TYPE_DYNAMIC = 1;
	public static int TYPE_KINEMATIC = 2;
	public static int TYPE_AREA = 3;
	
	public static int SHAPE_BOX = 0;
	public static int SHAPE_CIRCLE = 1;
	
	boolean mActive = false;
	
	static public class ObjectProps
	{
		
		int 	mType = TYPE_FIXED;
		int 	mShape = SHAPE_BOX;
		int 	mGroupIndex = 0; 
		float 	mFriction = 0.3f;
		float 	mDensity = 1f;
		float 	mRestitution = 0.5f;
		
	}

	public class BodyData
	{
		LayoutArea	   mArea;
		GameonModelRef mRef;
		Body 		   mBody;
		ObjectProps	   mProps;
	}
	

	
	public class Box2dData
	{
		World mWorld;
		String mName;
		Vector<BodyData>	mAreaModels = new Vector<BodyData>();
		Vector<BodyData>	mDynModels = new Vector<BodyData>();
		Vector<BodyData>	mFixModels = new Vector<BodyData>();
		int mMapping = MAPPING_XY;
	}
	
	public Box2dWrapper(GameonApp app)
	{
		mApp = app;
	}
	
	public void initWorld(String worldname, String grav, String mapping)
	{
		if (this.mBox2dWorlds.containsKey("worldname"))
		{
			return;
		}
		
		float gravity[] = new float[2];
		ServerkoParse.parseFloatArray(gravity, grav);
		
		Vec2 vecgrav = new Vec2(gravity[0], gravity[1]);
		Box2dData data = new Box2dData();
		
		World world = new World( vecgrav  , true);
		data.mWorld = world;
		data.mName = worldname;
		if (mapping.equals("xy"))
		{
			data.mMapping = MAPPING_XY;
		}else if (mapping.equals("xz"))
		{
			data.mMapping = MAPPING_XZ;
		}if (mapping.equals("yz"))
		{
			data.mMapping = MAPPING_YZ;
		}
		
		mActive = true;
		
		mBox2dWorldsVec.add(data);
		mBox2dWorlds.put(worldname, data);
	}
	
	public void addDynObject(String worldname, String objname, GameonModelRef ref, ObjectProps props)
	{
		Box2dData data = this.mBox2dWorlds.get(worldname);
		if (data == null)
		{
			return;
		}
		
		
		// Dynamic Body
	    BodyDef bodyDef = new BodyDef();
	    bodyDef.type = BodyType.DYNAMIC;
	    bodyDef.position.set(ref.mPosition[0], ref.mPosition[1]);
	    Body body = data.mWorld.createBody(bodyDef);
	    
	    FixtureDef fixtureDef = new FixtureDef();
	    if (props.mShape == SHAPE_BOX)
	    {
	    	PolygonShape dynamicBox = new PolygonShape();
	    	dynamicBox.setAsBox(ref.mScale[0]/2,ref.mScale[1]/2);
	    	fixtureDef.shape = dynamicBox;
	    }else
	    {
	    	CircleShape dynamicCircle = new CircleShape();
	    	dynamicCircle.m_radius = ref.mScale[0]/2;
	    	fixtureDef.shape = dynamicCircle;
	    }
	    
	    fixtureDef.density=props.mDensity;
	    fixtureDef.friction=props.mFriction;
	    fixtureDef.restitution = props.mRestitution;
	    //kinematicBody
	    fixtureDef.filter.groupIndex = props.mGroupIndex;
	    body.createFixture(fixtureDef);
	    
	    BodyData bodydata = new BodyData();
	    bodydata.mRef = ref;
	    bodydata.mBody = body;
	    bodydata.mProps = props;
	    ref.assignPsyData(bodydata);
	    data.mDynModels.add(bodydata);
	}
	
	public void addAreaObject(String worldname, String objname, LayoutArea area, ObjectProps props)
	{
		Box2dData data = this.mBox2dWorlds.get(worldname);
		if (data == null)
		{
			return;
		}
		
		
		// Dynamic Body
	    BodyDef bodyDef = new BodyDef();
	    bodyDef.type = BodyType.DYNAMIC;
	    bodyDef.position.set(area.mLocation[0], area.mLocation[1]);
	    Body body = data.mWorld.createBody(bodyDef);
	    
	    FixtureDef fixtureDef = new FixtureDef();
    	PolygonShape dynamicBox = new PolygonShape();
    	dynamicBox.setAsBox(area.mBounds[0]/2,area.mBounds[1]/2);
    	fixtureDef.shape = dynamicBox;
	    
	    fixtureDef.density=props.mDensity;
	    fixtureDef.friction=props.mFriction;
	    fixtureDef.restitution = props.mRestitution;
	    //kinematicBody
	    fixtureDef.filter.groupIndex = props.mGroupIndex;
	    body.createFixture(fixtureDef);
	    
	    BodyData bodydata = new BodyData();
	    bodydata.mArea = area;
	    bodydata.mBody = body;
	    bodydata.mProps = props;
	    area.assignPsyData(bodydata);
	    data.mAreaModels.add(bodydata);
	}	
	public void addFixedObject(String worldname, String objname, GameonModelRef ref, ObjectProps props)
	{
		Box2dData data = this.mBox2dWorlds.get(worldname);
		if (data == null)
		{
			return;
		}
		
		BodyDef bodyDef = new BodyDef();
		
	    bodyDef.position.set(ref.mPosition[0], ref.mPosition[1]);
	    Body groundBody = data.mWorld.createBody(bodyDef);
	    
	    if (props.mShape == SHAPE_BOX)
	    {	    
	    	PolygonShape shape = new PolygonShape();
	    	shape.setAsBox(ref.mScale[0]/2,ref.mScale[1]/2);
	    	groundBody.createFixture(shape, 0);
	    }else
	    {
	    	CircleShape shape = new CircleShape();
	    	shape.m_radius = ref.mScale[0]/2;
	    	groundBody.createFixture(shape, 0);	    	
	    }
	    
	    
	    BodyData bodydata = new BodyData();
	    bodydata.mRef = ref;
	    bodydata.mBody = groundBody;
	    bodydata.mProps = props;
	    ref.assignPsyData(bodydata);
	    data.mFixModels.add(bodydata);
	    
	    
	}
	
	public void doFrame(long delay)
	{
		if (!mActive)
		{
			return;
		}
	    int velocityIterations = 6;
	    int positionIterations = 2;
	    float timeStep = 1/(float)delay;
	    for (Box2dData data : mBox2dWorldsVec)
	    {
	    	data.mWorld.step(timeStep, velocityIterations, positionIterations);
	    	for (BodyData bodydata: data.mDynModels)
	    	{
	    		if (bodydata.mRef == null)
	    		{
	    			continue;
	    		}
	    		Vec2 position = bodydata.mBody.getPosition();
	    		bodydata.mRef.setPosition(position.x, position.y, 0.1f);
	    		//System.out.println( " rot = " + bodydata.mBody.getAngle());
	    		bodydata.mRef.setRotate(0, 0, bodydata.mBody.getAngle() * 360 / 3.14f);
	    		bodydata.mRef.set();
	    	}
	    	for (BodyData bodydata: data.mAreaModels)
	    	{
	    		if (bodydata.mArea == null)
	    		{
	    			continue;
	    		}
	    		Vec2 position = bodydata.mBody.getPosition();
	    		bodydata.mArea.mLocation[0] = position.x;
	    		bodydata.mArea.mLocation[1] = position.y;
	    		bodydata.mArea.mRotation[2] = bodydata.mBody.getAngle() * 360 / 3.14f;
	    		bodydata.mArea.updateModelsTransformation();
	    	}
	    }
		

	}

	public void initObjects(GL2 gl, JSONObject response)
    {
        // init layout
		try {
	    	JSONArray areas;
	    	
	    	String worldid = response.getString("worldid");
	    	
			areas = response.getJSONArray("object");
        
	        for (int a=0; a< areas.length(); a++)
	        {
	            JSONObject pCurr = areas.getJSONObject(a);
	            processObject(gl, worldid,pCurr);
	        }
		} catch (JSONException e) {
			e.printStackTrace();
        }

	}

    private void processObject(GL2 gl, String worldid, JSONObject objData) {
    	try {
			String name = objData.getString("name");
			String type = objData.getString("type");
			String refid = objData.getString("refid");
			
			ObjectProps props = new ObjectProps();
			
			if (objData.has("template"))
			{
				String data = objData.getString("template");
				if (data.equals("box"))
				{
					props.mShape = SHAPE_BOX;
				}else
				{
					props.mShape = SHAPE_CIRCLE;
				}
			}
			if (objData.has("friction"))
			{
				String data = objData.getString("friction");
				props.mFriction = Float.parseFloat(data);
			}
			if (objData.has("density"))
			{
				String data = objData.getString("density");
				props.mDensity = Float.parseFloat(data);
			}
			if (objData.has("restitution"))
			{
				String data = objData.getString("restitution");
				props.mRestitution = Float.parseFloat(data);
			}
			if (objData.has("groupIndex"))
			{
				String data = objData.getString("groupIndex");
				props.mGroupIndex = Integer.parseInt(data);
			}			
			
			
			if (type.equals("dynamic"))
			{
				GameonModelRef ref = mApp.objects().getRef(refid);
				if (ref == null)
				{
					return;
				}
				
				props.mType = TYPE_DYNAMIC;
				this.addDynObject(worldid, name, ref , props);
			}else if (type.equals("fixed"))
			{
				GameonModelRef ref = mApp.objects().getRef(refid);
				if (ref == null)
				{
					return;
				}
				
				props.mType = TYPE_FIXED;
				this.addFixedObject(worldid, name, ref , props);
			}else if (type.equals("area"))
			{
				LayoutArea area = mApp.grid().getArea(refid);
				props.mType = TYPE_AREA;
				this.addAreaObject(worldid, name, area , props);
			}
    	}
    	catch (JSONException e) {
    		e.printStackTrace();
        }    	
    }

    boolean isActive()
    {
    	return mActive;
    }


    public void removeWorld(String worldname)
    {
		Box2dData data = this.mBox2dWorlds.get(worldname);
		if (data == null)
		{
			return;
		}

		for (BodyData bdata: data.mDynModels)
		{
			bdata.mRef.assignPsyData(null);
		}
		
		for (BodyData bdata: data.mFixModels)
		{
			bdata.mRef.assignPsyData(null);
		}
		
		for (BodyData bdata: data.mAreaModels)
		{
			bdata.mArea.assignPsyData(null);
		}				

		this.mBox2dWorlds.remove(data);
		this.mBox2dWorldsVec.remove(data);
		
		if (this.mBox2dWorldsVec.size() == 0)
		{
			this.mActive = false;
		}
    }
}

// TODO
// maping coords
// clearing, 
// faster iterations
// provjeri dali se stvarno kod objectsfactory kreira sa ref.id !

