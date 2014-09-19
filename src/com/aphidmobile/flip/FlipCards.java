package com.aphidmobile.flip;

import static com.aphidmobile.flip.FlipRenderer.checkError;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


/*
Copyright 2012 Aphid Mobile

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
	 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 */

public class FlipCards {

	private static final int STATE_TIP = 0;
	private static final int STATE_TOUCH = 1;
	private static final int STATE_AUTO_ROTATE = 2;
	private static final int IDOL = 3;
	private static final int UP_ANI = 4;
	private static final int DOWN_ANI = 5;
	private Texture frontTexture;
	private Bitmap frontBitmap;

	private Card frontTopCard;

	private float angle = 0f;
	private int state = STATE_TIP;
	private int dir=1;
	private int mid = 3;
	public FlipCards() {
		frontTopCard = new Card();
	}

	public void reloadTexture(View frontView, View backView) {
		frontBitmap = GrabIt.takeScreenshot(frontView);
		setState(IDOL);
	}

	public void rotateBy(float delta) {
		angle += delta;
		if (angle > 180)
			angle = 180;
		else if (angle < 0)
			angle = 0;
	}

	public void setState(int state) {
		if (this.state != state) {
			this.state = state;
		}
	}

	public void draw(GL10 gl) {
		applyTexture(gl);
		
		switch (state) {
		case STATE_TOUCH:
			break;
		case UP_ANI:
			angle2 -= speed_fast;
			if(angle2 <= 2){
				counts = 0;
				setState(IDOL);
			}
			updateV();
			break;
		case DOWN_ANI:
			angle2 += speed_mid;
			if(angle2 >= 90){
				counts = 0;
				setState(IDOL);
			}
			updateV();
			break;
		case IDOL:
			wander();
			break;
		case STATE_AUTO_ROTATE:
			autoRotate(gl);
			break;
		default:
			break;
		}
		
		frontTopCard.draw(gl);
	}
	
	private int counts = 0;
	private void wander() {
		if(counts ++ >60){
			setState(STATE_AUTO_ROTATE);
		}
	}

	private float angle2 = 60;
	private float speed_slow = 0.5f;
	private float speed_mid = 3;
	private float speed_fast = 6;
	private void autoRotate(GL10 gl) {
		angle2 += speed_slow *dir;
		if(angle2 >= 70){
			dir = -dir;
			angle2 = 70;
		} 
		if(angle2 <= 4){
			dir = -dir;
			angle2 = 4;
		}
		
		updateV();
	}
		
	float W, H;
	float tw;
	float th;
	private void applyTexture(GL10 gl) {
		if (frontBitmap != null) {
			if (frontTexture != null)
				frontTexture.destroy(gl);

			frontTexture = Texture.createTexture(frontBitmap, gl);

			frontTopCard.setTexture(frontTexture);
			
			W = frontBitmap.getWidth();
			H = frontBitmap.getHeight();
			
			tw = frontBitmap.getWidth() / (float) frontTexture.getWidth();
			th = frontBitmap.getHeight() / (float) frontTexture.getHeight();
			
			checkError(gl);
			
			updateV();
			
			frontBitmap.recycle();
			frontBitmap = null;
		}

		
	}
	
	private void updateV(){
		int points= 2+4*mid, inds = points*3, vers = points*2;
		float w = W, h =  H / mid;
		float margin = 0f, deep = 0f;
		float[] cardVertices = new float[inds];
		float[] textures = new float[vers];
		short[] incs = new short[4*mid*3];
		
		/**
		 *  固定顶部两点 0,1
		 */
		cardVertices[0] = 0+margin;
		cardVertices[1] = H;
		cardVertices[2] = 0+deep;
		
		textures[0] = 0f;
		textures[1] = 0f;
		
		cardVertices[3] = w-margin;
		cardVertices[4] = H;
		cardVertices[5] = 0+deep;
		
		textures[2] = tw;
		textures[3] = 0f;
		
		/**
		 *  2,3,4,5顶点(x,y,z) 文理(u,v)
		 */
		for(int i = 1; i <= mid; i++){
			cardVertices[12*i-6] = 0+margin;
			cardVertices[12*i-5] = (float) (H - (i-1 +0.5f)*h*Math.sin(d2r(angle2)));
			cardVertices[12*i-4] = (float) (-h*0.5*Math.cos(d2r(angle2)));
			
			textures[8*i-4] = 0f;
			textures[8*i-3] = th*( i - 0.5f)/ mid;
			
			cardVertices[12*i-3] = w-margin;
			cardVertices[12*i-2] = (float) (H-(i-1 + 0.5f)*h*Math.sin(d2r(angle2)));
			cardVertices[12*i-1] = (float) (-h*0.5*Math.cos(d2r(angle2)));
			
			textures[8*i-2] = tw;
			textures[8*i-1] = th*( i - 0.5f)/ mid;
			
			cardVertices[12*i] = 0+margin;
			cardVertices[12*i+1] = (float) (H - i*h*Math.sin(d2r(angle2)));
			cardVertices[12*i+2] = 0+deep;
			
			textures[8*i] = 0f;
			textures[8*i+1] = th*i/mid;
			
			cardVertices[12*i+3] = w-margin;
			cardVertices[12*i+4] = (float) (H-i*h*Math.sin(d2r(angle2)));
			cardVertices[12*i+5] = 0+deep;
			
			textures[8*i+2] = tw;
			textures[8*i+3] = th*i/mid;
		}
		
		/**
		 *  三角形顶点索引
		 *  (1,0,2), (1,2,3), (3,2,4), (3,4,5)
		 */
		
		for(int i = 0; i < mid; i++){
			incs[12*i] = (short) (4*i+1);
			incs[12*i+1] = (short) (4*i);
			incs[12*i+2] = (short) (4*i+2);
			
			incs[12*i+3] = (short) (4*i+1);
			incs[12*i+4] = (short) (4*i+2);
			incs[12*i+5] = (short) (4*i+3);
			
			incs[12*i+6] = (short) (4*i+3);
			incs[12*i+7] = (short) (4*i+2);
			incs[12*i+8] = (short) (4*i+4);
			
			incs[12*i+9] = (short) (4*i+3);
			incs[12*i+10] = (short) (4*i+4);
			incs[12*i+11] = (short) (4*i+5);
			
		}
//		loadArray(arr, len)
		frontTopCard.setIndices(incs);
		frontTopCard.setCardVertices(cardVertices);
		frontTopCard.setTextureCoordinates(textures);
	}
	
	public void invalidateTexture() {
		//Texture is vanished when the gl context is gone, no need to delete it explicitly
		frontTexture = null;
	}

	private float lastY = -1;

	public boolean handleTouchEvent(MotionEvent event) {
		if (frontTexture == null)
			return false;

		float delta;

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				lastY = event.getY();
				setState(STATE_TOUCH);
				return true;
			case MotionEvent.ACTION_MOVE:
				return true;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				delta = lastY - event.getY();
				
				if (delta > 20) {
					angle2 = 90;
					setState(UP_ANI);
					return true;
				}
				
				if(delta < -20){
					angle2 = 0;
					setState(DOWN_ANI);
					return true;
				}
				
				counts = 0;
				setState(IDOL);
				
				return true;
		}

		return false;
	}
	
	// 辅助 ===================================================================================
	private void loadArray(short arr[], int len){
		int num = 0;
		for (int i = 0; i < len; num++){
			Log.d("vertex", "p"+num+":("+arr[i++]+", "+arr[i++]+", "+arr[i++]+")");
		}
	}
	
	private void loadArray(float arr[], int len){
		int num = 0;
		for (int i = 0; i < len; num++){
			Log.d("vertex", "p"+num+":("+arr[i++]+", "+arr[i++]+", "+arr[i++]+")");
		}
	}
	
	private void loadArray2(float arr[], int len){
		int num = 0;
		for (int i = 0; i < len; num++){
			Log.d("vertex", "p"+num+":("+arr[i++]+", "+arr[i++]+")");
		}
	}

	private float d2r(float _angle) {
		return (float) (Math.PI * _angle /180.0f);
	}
}
