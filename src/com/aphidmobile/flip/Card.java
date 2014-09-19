package com.aphidmobile.flip;

import static com.aphidmobile.flip.FlipRenderer.*;
import static com.aphidmobile.flip.Utils.*;
import static javax.microedition.khronos.opengles.GL10.*;

import javax.microedition.khronos.opengles.GL10;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

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

public class Card {
	public static final int AXIS_TOP = 0;
	public static final int AXIS_BOTTOM = 1;

	private float cardVertices[];

	private short[] indices = {0, 1, 2, 0, 2, 3};

	private FloatBuffer vertexBuffer;

	private ShortBuffer indexBuffer;

	private float textureCoordinates[];

	private FloatBuffer textureBuffer;

	private Texture texture;

	private boolean dirty = false;

	public Texture getTexture() {
		return texture;
	}

	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	public float[] getCardVertices() {
		return cardVertices;
	}
	
	public void setIndices(short[] incs) {
		this.indices = incs;
		this.dirty = true;
	}

	public short[] getIndices() {
		return indices;
	}

	public ShortBuffer getIndexBuffer() {
		return indexBuffer;
	}

	public void setCardVertices(float[] cardVertices) {
		this.cardVertices = cardVertices;
		this.dirty = true;
	}

	public void setTextureCoordinates(float[] textureCoordinates) {
		this.textureCoordinates = textureCoordinates;
		this.dirty = true;
	}
	
	public void draw(GL10 gl) {
		if (dirty)
			updateVertices();

		if (cardVertices == null)
			return;

		gl.glFrontFace(GL_CCW);

//		gl.glEnable(GL_CULL_FACE);
//		gl.glCullFace(GL_FRONT);

		gl.glEnableClientState(GL_VERTEX_ARRAY);

		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glColor4f(1f, 1.0f, 1f, 1.0f);

		if (isValidTexture(texture)) {
			gl.glEnable(GL_TEXTURE_2D);
			gl.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			gl.glTexCoordPointer(2, GL_FLOAT, 0, textureBuffer);
			gl.glBindTexture(GL_TEXTURE_2D, texture.getId()[0]);
		}

		checkError(gl);

		gl.glPushMatrix();
//		gl.glTranslatef(100f, 200f, 0f);
//		gl.glScalef(.8f, .8f, 1f);		
		gl.glVertexPointer(3, GL_FLOAT, 0, vertexBuffer);
		gl.glDrawElements(GL_TRIANGLE_STRIP, indices.length, GL_UNSIGNED_SHORT, indexBuffer);
//		gl.glDrawElements(GL_LINES, indices.length, GL_UNSIGNED_SHORT, indexBuffer);
		checkError(gl);

		gl.glPopMatrix();

		if (isValidTexture(texture)) {
			gl.glDisableClientState(GL_TEXTURE_COORD_ARRAY);
			gl.glDisable(GL_TEXTURE_2D);
		}

		

		checkError(gl);

		gl.glDisable(GL_BLEND);
		gl.glDisableClientState(GL_VERTEX_ARRAY);
//		gl.glDisable(GL_CULL_FACE);
	}

	private void updateVertices() {
		vertexBuffer = toFloatBuffer(cardVertices);
		indexBuffer = toShortBuffer(indices);
		textureBuffer = toFloatBuffer(textureCoordinates);
	}
}
