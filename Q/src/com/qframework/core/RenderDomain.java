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

import java.util.Iterator;
import java.util.Vector;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;



public class RenderDomain {

    private Vector<GameonModel> mVisibleModelList = new Vector<GameonModel>();
    private Vector<GameonModel> mVisibleModelList2 = new Vector<GameonModel>();
    
    private TextRender		mTexts;
    private GameonApp mApp;
    private boolean	mPanX = false;
    private boolean	mPanY = false;
    private float   mPanCoords[] = { -1000.0f,1000.0f,-1000.0f,1000.0f};
    private float 	mLastPanX = -1;
    private float 	mLastPanY = -1;
    protected int 	mRenderId = -1;
    private float mSpaceBottomLeft[] = new float[2];
    private float mSpaceTopRight[] = new float[2];
    protected String	mName;

	private float 	mFov = 45;
	private float 	mNear = 0.1f;
	private float 	mFar = 8.7f;
	protected float mOffsetX = 0;
	protected float mOffsetY = 0;
	protected float mWidth;
	protected float mHeight;
	
	protected int mViewport[] = new int[4];
	
	protected float mOffXPerct = 0.0f;
	protected float mOffYPerct = 0.0f;
	protected float mWidthPerct = 1.0f;
	protected float mHeightPerct = 1.0f;
	protected float mAspect = 1.0f;
	GameonCS	mCS;

	protected boolean mVisible = false;
	GameonModel	mHorizontalScroller;
	GameonModel	mVerticalScroller;
	
	RenderDomain(String name, GameonApp app, float w, float h) {
		mApp = app;
		mName = name;
		mTexts = new TextRender(this);
		mCS = new GameonCS(mApp);
		mWidth = w;
		mHeight = h;
		mViewport[2] = (int)w;
		mViewport[3] = (int)h;
	}
	
	

	public void draw(GL2 gl, long delay) {

		if (!mVisible)
		{
			return;
		}
		// TODO cache old call
		gl.glViewport(mViewport[0], mViewport[1], mViewport[2], mViewport[3] );
		
		gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        perspective(gl , mFov , (float)mWidth/(float)mHeight, mNear , mFar, true);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
	    gl.glLoadIdentity();
        mCS.applyCamera(gl, delay);

        
		int len = mVisibleModelList.size();
		for (int a=0; a< len; a++) {
			GameonModel model = mVisibleModelList.get(a);
			if (!model.mHasAlpha)
				model.draw(gl, mRenderId);
		}
		for (int a=0; a < len ; a++) {
			GameonModel model = mVisibleModelList.get(a);
			if (model.mHasAlpha)
				model.draw(gl, mRenderId);
		}

		len = mVisibleModelList2.size();
		for (int a=0; a< len; a++) {
			GameonModel model = mVisibleModelList2.get(a);
			if (!model.mHasAlpha)
				model.draw(gl, mRenderId);
		}
		for (int a=0; a < len ; a++) {
			GameonModel model = mVisibleModelList2.get(a);
			if (model.mHasAlpha)
				model.draw(gl, mRenderId);
		}
		
			mTexts.render(gl);
	}

	public void clear() {

		// TODO clear refs from GameonModel ? or not
		for (GameonModel model : mVisibleModelList)
		{
			model.hideDomainRefs(this.mRenderId);
		}
		for (GameonModel model : mVisibleModelList2)
		{
			model.hideDomainRefs(this.mRenderId);
		}		
		mTexts.clear();
	}

	public void setVisible(GameonModel model)
	{
		if (model.mIsModel)
		{
			if (mVisibleModelList2.indexOf(model) < 0)
			{
				for (int a=0; a < mVisibleModelList2.size(); a++)
				{
					GameonModel m = mVisibleModelList2.get(a);
					if (m.mTextureID == model.mTextureID)
					{
						mVisibleModelList2.add(a,model);
						return;
					}
				}
				mVisibleModelList2.add(model);	
			}
		}else
		{
			if (mVisibleModelList.indexOf(model) < 0)
			{
				for (int a=0; a < mVisibleModelList.size(); a++)
				{
					GameonModel m = mVisibleModelList.get(a);
					if (m.mTextureID == model.mTextureID)
					{
						mVisibleModelList.add(a,model);
						return;
					}
				}				
				mVisibleModelList.add(model);	
			}		
		}
	}
	
	public void remVisible(GameonModel model, boolean force)
	{
		int countvis = model.getVisibleRefs(this.mRenderId);
		if (countvis > 0 && !force)
		{
			return;
		}
		
		if (model.mIsModel)
		{
			if (mVisibleModelList2.indexOf(model) >= 0)
			{
				mVisibleModelList2.remove(model);	
			}
		}else
		{
			if (mVisibleModelList.indexOf(model) >= 0)
			{
				mVisibleModelList.remove(model);	
			}		
		}
	}


	protected TextRender texts()
	{
		return mTexts;
		
	}

    private void perspective(GL2 gl, float fovy, float aspect, float zmin , float  zmax,boolean frustrumUpdate)
    {
        float xmin, xmax, ymin, ymax;
        ymax = zmin * (float)Math.tan(fovy * Math.PI / 360.0f);
        ymin = -ymax;
        xmin = ymin * aspect;
        xmax = ymax * aspect;
        if (frustrumUpdate)
        {
        	gl.glFrustumf(xmin, xmax, ymin, ymax, zmin, zmax);
        }else
        {
        	mCS.saveProjection(xmin , xmax , ymin , ymax , zmin , zmax);
        }
	 }
    
	public void setFov(float fovf, float nearf, float farf) {
		mFar = farf;
		mNear = nearf;
		mFov = fovf;
    	perspective(null , mFov , (float)mWidth/(float)mHeight, mNear , mFar, false);
	}

	public void onSurfaceChanged(GL2 gl, int width, int height)
	{
		float newWidth = (float)width;
		float newHeight = (float)height;

		mWidth = this.mWidthPerct * newWidth;
		mHeight = this.mHeightPerct * newHeight;
		
		mOffsetX = this.mOffXPerct * newWidth;
		
		mOffsetY = this.mOffYPerct * newHeight;
		
		
		mViewport[0] = (int)mOffsetX;
		mViewport[1] = (int)mOffsetY;
		mViewport[2] = (int)mWidth;
		mViewport[3] = (int)mHeight;

		
    	
    	mCS.saveViewport( mViewport, (float)width, (float)height);
    	perspective(gl , mFov , (float)mWidth/(float)mHeight, mNear , mFar, false);
    	
	}
	
	public void onSurfaceCreated(GL2 gl) {
		
	}
	
	public void removeText(TextItem text)
	{
		this.mTexts.remove(text);
	}



	public void setBounds(GL2 gl, int width, int height, float[] coords) {
		mOffXPerct = coords[0];
		mOffYPerct = coords[1];
		mWidthPerct = coords[2];
		mHeightPerct = coords[3];
		
		this.onSurfaceChanged(gl, width, height);
		
	}



	public void show() {
		mVisible = true;
		
	}
	
	public void hide() {
		mVisible = false;
		
	}



	public AreaIndexPair onTouchModel(float x, float y, boolean click, boolean noareas) 
	{
    	float rayVec[] = new float[3];
    	float eye[] = null;
		
		mCS.screen2spaceVec(x, y, rayVec);
		eye = mCS.eye();
		
		AreaIndexPair data = null;
		int len = mVisibleModelList.size();
		for (int a=0; a< len; a++) {
			GameonModel model = mVisibleModelList.get(a);
			if (noareas && model.mParentArea!= null)
			{
				continue;
			}
			data = model.onTouch(eye , rayVec , mRenderId,click);
			if (data != null)
			{
				return data;
			}
		}

		len = mVisibleModelList2.size();
		for (int a=0; a< len; a++) {
			GameonModel model = mVisibleModelList2.get(a);
			if (noareas && model.mParentArea!= null)
			{
				continue;
			}			
			data = model.onTouch(eye , rayVec , mRenderId,click);
			if (data != null)
			{
				return data;
			}			
		}

		return null;
	}



	public void pan(String mode, String scrollers, String coords) 
	{
		if (mode.equals("enable"))
		{
			// enable x and y
			mPanX = true;
			mPanY = true;
		}else if (mode.equals("enablex"))
		{
			// enable x
			mPanX = true;
			mPanY = false;			
		}else if (mode.equals("enabley"))
		{
			// enable y
			mPanX = false;
			mPanY = true;			
		}else if (mode.equals("disable"))
		{
			// disable all
			mPanX = false;
			mPanY = false;			
		}
		if (coords != null)
		{
			ServerkoParse.parseFloatArray(mPanCoords, coords);
		}
		
		if (scrollers != null && scrollers.equals("true"))
		{
			if (mPanX)
			{
				//mHorizontalScroller = new GameonModel("horscroll", mApp, null);
			}
		}
	}



	public boolean onPan(float x, float y) 
	{
		
		if (!mPanX && !mPanY)
		{
			return false;
		}
		
		if ( x < mOffsetX || x > mOffsetX + mWidth || y < mOffsetY || y > mOffsetY + mHeight)
		{
			return false;
		}
		
		// calculate delta
		if (mLastPanX == -1)
		{
			mLastPanX = x; mLastPanY = y;
			return true;
		}
		
		float deltax = x - mLastPanX;
		float deltay = y - mLastPanY;

		
		float eye[] = mCS.eye();

		float lookat[] = mCS.lookat();
		
		float lasteyex = eye[0];
		float lasteyey = eye[1];
		float lastlookx = lookat[0];
		float lastlooky = lookat[1];
		
		
		if (mPanX)
		{
			eye[0] -= deltax/50.0f;
			lookat[0] -= deltax/50.0f;
		}
		if (mPanY)
		{
			eye[1] += deltay/50.0f;
			lookat[1] += deltay/50.0f;
		}
		
		
		System.out.println( eye[0] );
		mCS.setCamera(lookat , eye);
		mLastPanX = x; mLastPanY = y;
		
		boolean canreturn = false;
		float lastrunx = eye[0];
		float lastruny = eye[1];
		
		do 
		{
			
			lastrunx = eye[0];
			lastruny = eye[1];
			

			
			mCS.screen2space(mOffsetX+mWidth, -mOffsetY, mSpaceTopRight);
			mCS.screen2space(mOffsetX, -mOffsetY+mHeight, mSpaceBottomLeft);

			System.out.println( mSpaceBottomLeft[0]+","+ mSpaceBottomLeft[1]+","+ 
					mSpaceTopRight[0] +","+  mSpaceTopRight[1]);
			
			System.out.println( mSpaceBottomLeft[0]+","+ mSpaceBottomLeft[1]+","+ 
								mSpaceTopRight[0] +","+  mSpaceTopRight[1]);
			
			canreturn = true;
			if (mPanCoords[0] != -1000)
			{
				if (mPanX)
				{
					if ((mPanCoords[1] - mPanCoords[0]) < (mSpaceTopRight[0] - mSpaceBottomLeft[0]))
					{
						eye[0] = lasteyex;
						lookat[0] = lastlookx;
						mCS.setCamera(lookat , eye);
						canreturn = true;
						
					}else
					if (mSpaceBottomLeft[0] < mPanCoords[0]+0.001 || mSpaceTopRight[0] > mPanCoords[1]-0.001)
					{
						eye[0] = (lasteyex + eye[0]) / 2;
						lookat[0] = (lastlookx + lookat[0]) / 2;
						mCS.setCamera(lookat , eye);
						canreturn = false;
					}
				}
				
				if (mPanY )
				{
					if ((mPanCoords[3] - mPanCoords[2]) < (mSpaceTopRight[1] - mSpaceBottomLeft[1]))
					{
						eye[1] = lasteyey;
						lookat[1] = lastlooky;
						mCS.setCamera(lookat , eye);
						canreturn = true;						
					}else
					if (mSpaceBottomLeft[1] < mPanCoords[2]+0.001 || mSpaceTopRight[1] > mPanCoords[3]-0.001)
					{
						eye[1] = (lasteyey + eye[1]) / 2;
						lookat[1] = (lastlooky + lookat[1]) / 2;
						mCS.setCamera(lookat , eye);
						canreturn = false;
					}
				}
			}
			if (lastrunx == eye[0] && lastruny == eye[1])
			{
				break;
			}
		}while(!canreturn);
 		return true;
	}



	public void resetPan() {
		mLastPanX = -1;
		mLastPanY = -1;
		
	}	
	
}

