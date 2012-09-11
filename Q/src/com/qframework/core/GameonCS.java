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

public class GameonCS {
    
	 private float mCanvasW;
	 private float mCanvasH;
	 private float mCanvasZ;
	 private int mViewport[] = { 0 ,0,0,0};
	 private float mLookAt [] = new float[16];
	 private float mProjection [] = new float[16];
	 
	 private float mCameraEye [];
	 private float mCameraLookAt [];
	 private boolean mHudInit = false;
	 private boolean mSpaceInit = false;
	 static private  GLU glu;
	 private float mBBox[] = new float[8];
	 private float mUpZ[];
	 private float mScreenHeight = 0;
	 private float mScreenWidth = 0;
	 private GameonApp mApp;
	 private GameonModelRef mCameraData;
	 private GameonModelRef mAnimDataStart;
	 private GameonModelRef mAnimDataEnd;
	 public GameonCS(GameonApp app)
	 {
		 mApp = app;
		 mCameraData = new GameonModelRef(null, 0);
		 
		 mCameraEye = mCameraData.mAreaPosition;
		 mCameraLookAt = mCameraData.mPosition;
		 mUpZ = mCameraData.mScale;
		 
		 mCameraEye[0] = 0;
		 mCameraEye[1] = 0;
		 mCameraEye[2] = 5;
		 
		 
		 mUpZ[0] = 0;
		 mUpZ[1] = 1;
		 mUpZ[2] = 0;
		 
		 
		 
	 }
    
	 static public void setGlu(GLU g)
    {
        glu = g;
    }

     void saveViewport(int viewport[] , float w, float h) {
    	mScreenHeight = h;
    	mScreenWidth = w;
    	mViewport[0] = viewport[0];
    	mViewport[1] = viewport[1];
    	mViewport[2] = viewport[2];
    	mViewport[3] = viewport[3];
    }
    
     void saveProjection( float left, float right, float top, float bottom, float near, float far )
    {
    	 GMath.frustrum(left,right,top,bottom,near,far, mProjection);
        //gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, mProjection, 0);
    }


     void set()
	{
    	
	}	
	 void init(float canvasw, float canvash, int canvasz ) {

		mCanvasW = (float)canvasw;
		mCanvasH = (float)canvash;
		mCanvasZ = (float)canvasz;
	}


	public  void screen2space(float x, float y, float[] spacecoords) {
		float c[] = new float[3];
		float f[] = new float[3];
		y = mViewport[3] - y;

		glu.gluUnProject(x,y, 1.0f,mLookAt, 0 ,
				mProjection, 0 ,
				mViewport, 0 ,
				f , 0);
		glu.gluUnProject(x,y, 0.0f,mLookAt, 0 ,
				mProjection, 0 ,
				mViewport, 0 ,
				c , 0);    		
		
		f[0] -= c[0];
		f[1] -= c[1];
		f[2] -= c[2];
		float rayLength = (float)Math.sqrt(c[0]*c[0] + c[1]*c[1] + c[2]*c[2]);
		//normalize
		f[0] /= rayLength;
		f[1] /= rayLength;
		f[2] /= rayLength;


		float dot1, dot2;

		float pointInPlaneX = 0;
		float pointInPlaneY = 0;
		float pointInPlaneZ = 0;
		float planeNormalX = 0;
		float planeNormalY = 0;
		float planeNormalZ = -1;

		pointInPlaneX -= c[0];
		pointInPlaneY -= c[1];
		pointInPlaneZ -= c[2];

		dot1 = (planeNormalX * pointInPlaneX) + (planeNormalY * pointInPlaneY) + (planeNormalZ * pointInPlaneZ);
		dot2 = (planeNormalX * f[0]) + (planeNormalY * f[1]) + (planeNormalZ * f[2]);

		float t = dot1/dot2;

		f[0] *= t;
		f[1] *= t;

		spacecoords[0] = f[0] + c[0];
		spacecoords[1] = f[1] + c[1];

	}

	public  void saveLookAt(float[] eye, float[] center, float[] up) {
		for (int a=0; a< 16; a++)
		{
			mLookAt[a] = 0;
		}
        GMath.lookAtf(mLookAt, eye , center , up);
		
	}


	public void screen2spaceVec(float x,float y, float[] vec)
	{
		float c[] = new float[3];
		y = mScreenHeight - y;
		glu.gluUnProject(x,y, 1.0f,mLookAt, 0 ,
				mProjection, 0 ,
				mViewport, 0 ,
				c , 0);
		vec[0] = c[0] - this.mCameraEye[0];
		vec[1] = c[1] - this.mCameraEye[1];
		vec[2] = c[2] - this.mCameraEye[2];
	}
	
	
	
	public float snap_cam_z(float eye[],  float center[],  float up[]) 
	{
		 
	    float lookAt[] = new float[16];
	    float eye2[] = new float[3];
	    for (int a=0; a< 3;a++)
	    {
	    	eye2[a] = eye[a];
	    }
	    
	    for (float  ez = eye[2]; ez <10; ez += 0.05)
	    {
	        float cordx = mCanvasW;
	        float cordy = 0;

	        float x,y,z;
	        eye2[2] = ez;
	        float win[] = new float[3];
	        GMath.lookAtf(lookAt,eye2,center,up);
	        
	        boolean res =glu.gluProject(cordx, cordy, 0.0f, 
                    lookAt, 0,
                    mProjection,0,
                    mViewport,0,
                    win,0);
	        //System.out.println( ez + " "+  res + " "+ win[0] + " "+ win[1] + " " + win[2]);
	        if ( res &&  win[0]>  mViewport[0] && win[0] < mViewport[2] && win[2] >= 0.9800 && win[2] <= 1.100)
	        {
	        	mCameraEye[0] = 0;
	        	mCameraEye[1] = 0;
	        	mCameraEye[2] = ez;
	        	
	        	mCameraLookAt[0] = 0;
	        	mCameraLookAt[1] = 0;
	        	mCameraLookAt[2] = 0;
	        	
		   		 mUpZ[0] = 0;
				 mUpZ[1] = 1;
				 mUpZ[2] = 0;

	            saveLookAt(mCameraEye , mCameraLookAt , mUpZ);         	        	
	            return ez;
	        }
	         
	    }
	    return 0;
	}
	


	public void setCamera(float[] lookat , float[] eye)
	{
	    mCameraEye[0] = eye[0];
	    mCameraEye[1] = eye[1];
	    mCameraEye[2] = eye[2];    

	    mCameraLookAt[0] = lookat[0];
	    mCameraLookAt[1] = lookat[1];
	    mCameraLookAt[2] = lookat[2];    
	    
	    saveLookAt(mCameraEye , mCameraLookAt , mUpZ);
	    
	}

	public void applyCamera(GL2 gl, long delta)
	{
		if (mCameraData.animating())
		{
			mCameraData.animate(delta);
			saveLookAt(mCameraEye ,  mCameraLookAt , mUpZ);
		}
	    if (!mSpaceInit)
	    {
	        saveLookAt(mCameraEye ,  mCameraLookAt , mUpZ);
	    }

	    gl.glMultMatrixf(mLookAt,0);
	}


	public void getScreenBounds(float world[])
	{

		float temp[] = new float[2];
	    screen2space(mViewport[0] , mViewport[1], temp);
	    world[0] = temp[0] ;world[1] = temp[1];
	    
	    screen2space(mViewport[2] , mViewport[1], temp);
	    world[2] = temp[0] ;world[3] = temp[1];
	    
	    screen2space(mViewport[2] , mViewport[3], temp);
	    world[4] = temp[0] ;world[5] = temp[1];
	    
	    screen2space(mViewport[0] , mViewport[3], temp);
	    world[6] = temp[0] ;world[7] = temp[1];
	    
	    
	    for (int a=0; a< 8; a++)
	    {
	    	mBBox[a] = world[a];
	    }
	    
	}

	public float getCanvasW()
	{
	    return mViewport[2] - mViewport[0];
	}

	public float getCanvasH()
	{
	    return mViewport[3] - mViewport[1];
	}

	public float worldWidth()
	{
		return mBBox[2] - mBBox[0];
	}
	
	public float worldHeight()
	{
		return mBBox[2] - mBBox[0];
	}	
	
	public float worldCenterX()
	{
		return (mBBox[2] + mBBox[0]) / 2;
	}
	public float worldCenterY()
	{
		return (mBBox[1] + mBBox[5]) / 2;
	}	


	public float[] eye()
	{
		return mCameraEye;
	}

	public void moveCamera(float[] lookat, float[] eye, long animdelay) 
	{
		if (mAnimDataStart == null)
		{
			mAnimDataStart = new GameonModelRef(null, 0);
		}
		if (mAnimDataEnd == null)
		{
			mAnimDataEnd = new GameonModelRef(null, 0);
		}
		
		mAnimDataStart.copy(mCameraData);
		mAnimDataEnd.copy(mCameraData);
		mAnimDataEnd.setAreaPosition(eye);
		mAnimDataEnd.setPosition(lookat);
		
		mApp.anims().createAnim( mAnimDataStart, mAnimDataEnd , mCameraData , (int)animdelay, 2 , null , 1, false, false);
	}

	public float[] lookat() {
		return mCameraLookAt;
	}

	public float[] up() {
		return mUpZ;
	}
	
}
