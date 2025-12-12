/*
 * Copyright (C) 2012-2018 Gregory Hedlund
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.session.editor.view.speechAnalysis;

import java.util.Arrays;
 
 public class ColorMap
 {
     public int size;
     public byte r[];
     public byte g[];
     public byte b[];
 
     public static ColorMap getJet()
     {
         return getJet(64);
     }
 
     public static ColorMap getJet(int n)
     {
         byte r[] = new byte[n];
         byte g[] = new byte[n];
         byte b[] = new byte[n];
         
         int maxval = 255;
         Arrays.fill(g, 0, n/8, (byte)0);
         for(int x = 0; x < n/4; x++)
             g[x+n/8] = (byte)(maxval*x*4/n);
         Arrays.fill(g, n*3/8, n*5/8, (byte)maxval);
         for(int x = 0; x < n/4; x++)
             g[x+n*5/8] = (byte)(maxval-(maxval*x*4/n));
         Arrays.fill(g, n*7/8, n, (byte)0);
 
         for(int x = 0; x < g.length; x++)
             b[x] = g[(x+n/4) % g.length];
         Arrays.fill(b, n*7/8, n, (byte)0);
         Arrays.fill(g, 0, n/8, (byte)0);
         for(int x = n/8; x < g.length; x++)
             r[x] = g[(x+n*6/8) % g.length];
         
         ColorMap cm = new ColorMap();
         cm.size = n;
         cm.r = r;
         cm.g = g;
         cm.b = b;
        
         return cm;
     }
 
     public static ColorMap getGreyscale(int n) {
    	 byte r[] = new byte[n];
         byte g[] = new byte[n];
         byte b[] = new byte[n];
         
         if(n > 256) n = 256;
         int step = 0xff / n;
         
         for(int i = 0; i < n; i++) {
        	 r[i] = g[i] = b[i] = (byte)(0xff - Math.max((i * step), 0));
         }
         
         ColorMap cm = new ColorMap();
         cm.size = n;
         cm.r = r;
         cm.g = g;
         cm.b = b;
         
         return cm;
     }
     
     public static ColorMap getBlue(int n) {
    	 byte r[] = new byte[n];
         byte g[] = new byte[n];
         byte b[] = new byte[n];
         
         if(n > 256) n = 256;
         int step = 0xff / n;
         
         for(int i = 0; i < n; i++) {
        	 r[i] = g[i] = 0;
        	 b[i] = (byte)(Math.max(0xff - (i * step), 0));
         }
         
         r[0] = g[0] = b[0] = (byte)0xff;
         
         ColorMap cm = new ColorMap();
         cm.size = n;
         cm.r = r;
         cm.g = g;
         cm.b = b;
        
         return cm;
     }
 
     public int getColor(int idx)
     {
         int pixel = ((r[idx] << 16) & 0xff0000)
             | ((g[idx] << 8) & 0xff00)
             | (b[idx] & 0xff);
 
         return pixel;
     }
     
     public int size() {
    	 return size;
     }
     
 }
