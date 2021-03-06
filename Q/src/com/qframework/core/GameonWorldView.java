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



import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;


public class GameonWorldView {

	private GameonWorld mWorld;
	private GameonApp	mApp;
	private float mWidth;
	private float mHeight;
	private boolean doOnce = false;
	private boolean mLockedDraw = false;
    
    public GameonWorldView(GameonWorld world , GameonApp app) {
        
        mWorld = world;
        mApp = app;
    }
   

    public void onDrawFrame(GL2 gl, long delay) {
    	        
	    if (mLockedDraw)return;
	    gl.glClear(GL2.GL_COLOR_BUFFER_BIT);// | GL2.GL_DEPTH_BUFFER_BIT);
	    mWorld.draw(gl, delay);
    }
	        
    
    public void onSurfaceChanged(GL2 gl, int width, int height, GLU glu) {
    	GameonCS.setGlu(glu);
    	mWidth = (float)width;
		mHeight = (float)height;
    	mWorld.prepare(gl);

    	gl.glViewport(0, 0, width, height);
    	mWorld.onSurfaceChanged(gl, width, height);

    	mApp.setScreenBounds();
    }
        
    public void onSurfaceCreated(GL2 gl, GLU glu) {
    	if (!doOnce)
        {
    		GameonCS.setGlu(glu);
    		mApp.textures().clear();
    		mApp.textures().init(gl);
    		mApp.sounds().init(mApp);	
    		if (mApp.mSplashScreen != null && mApp.mSplashScreen.length() > 0)
    		{
    			mWorld.initSplash(gl, mApp.mSplashScreen, mApp.mSplashX1,mApp.mSplashY1,mApp.mSplashX2,mApp.mSplashY2);	
    		}
    		
    		doOnce = true;
    	}else
        {
    		mApp.textures().init(gl);
        }
    	
    	mWorld.onSurfaceCreated(gl);

    }

	public boolean drawSplash(GL2 gl, GLU glu) {
		
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        perspective(gl , 45.0f , (float)mWidth/(float)mHeight , 0.1f , 28.7f);
		
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();		

        glu.gluLookAt(0.0, 0.0, 5, 
	              0, 0, 0,    
	              0, 1.0f, 0.0f);            
		
        mWorld.drawSplash(gl);
        gl.glPopMatrix();
		
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPopMatrix();
		
		
		return true;
	}
	
	public void lockDraw(boolean lock)	
	{
		
		mLockedDraw = lock;
	}



    private void perspective(GL2 gl, float fovy, float aspect, float zmin , float  zmax)
    {
        float xmin, xmax, ymin, ymax;
        ymax = zmin * (float)Math.tan(fovy * Math.PI / 360.0f);
        ymin = -ymax;
        xmin = ymin * aspect;
        xmax = ymax * aspect;
    	gl.glFrustumf(xmin, xmax, ymin, ymax, zmin, zmax);
	 }
    

}
