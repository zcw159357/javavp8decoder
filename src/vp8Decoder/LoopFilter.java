package vp8Decoder;


public class LoopFilter {
	public static void loopFilter(VP8Frame frame) {
		System.out.println("loop filter");
		System.out.println("filterLevel: "+frame.getFilterLevel());
		System.out.println("filterType: "+frame.getFilterType());
		int sharpnessLevel = frame.getSharpnessLevel();
		int loop_filter_level = frame.getFilterLevel();
		int interior_limit = frame.getFilterLevel();
		if( sharpnessLevel>0)
		{
		    interior_limit >>= sharpnessLevel > 4 ? 2 : 1;
		    if( interior_limit > 9 - sharpnessLevel)
		        interior_limit = 9 - sharpnessLevel;
		}
		if( interior_limit==0)
		    interior_limit = 1;

		System.out.println("interior_limit: "+interior_limit);
		
		int hev_threshold = 0;
		if( frame.getFrameType()==0)          /* current frame is a key frame */
		{
		     if( loop_filter_level >= 40)
		         hev_threshold = 2;
		     else if( loop_filter_level >= 15)
		         hev_threshold = 1;
		}
		else                                 /* current frame is an interframe */
		{
		     if( loop_filter_level >= 40)
		         hev_threshold = 3;
		     else if( loop_filter_level >= 20)
		         hev_threshold = 2;
		     else if( loop_filter_level >= 15)
		         hev_threshold = 1;
		}

		System.out.println("hev_threshold: "+hev_threshold);
		
		/* Luma and Chroma use the same inter-macroblock edge limit */
		int mbedge_limit = ((loop_filter_level + 2) * 2) + interior_limit;
		/* Luma and Chroma use the same inter-subblock edge limit */
		int sub_bedge_limit = (loop_filter_level * 2) + interior_limit;
		System.out.println("mbedge_limit: "+mbedge_limit);
		System.out.println("sub_bedge_limit: "+sub_bedge_limit);
		/*Segment seg2 = new Segment();
		seg2.P0 = 234;
		seg2.P1 = 221;
		seg2.P2 = 203;
		seg2.P3 = 190;
		seg2.Q0 = 191;
		seg2.Q1 = 168;
		seg2.Q2 = 147;
		seg2.Q3 = 142;
		System.out.println(seg2);
		subblock_filter(hev_threshold, interior_limit, sub_bedge_limit, seg2);
		System.out.println(seg2);
		System.exit(0);*/
		
		for(int y=0; y<frame.getMacroBlockRows(); y++)
			for(int x=0; x<frame.getMacroBlockCols(); x++)
			{
				MacroBlock rmb = frame.getMacroBlock(x, y);
				MacroBlock bmb = frame.getMacroBlock(x, y);
				
				//left
				if(x>0) {
					MacroBlock lmb = frame.getMacroBlock(x-1, y);
					for(int b=0; b<4; b++) {
						SubBlock rsb = rmb.getSubBlock(SubBlock.PLANE.Y1, 0, b);
						SubBlock lsb = lmb.getSubBlock(SubBlock.PLANE.Y1, 3, b);
						for(int a=0; a<4; a++) {
							int[][] rdest = rsb.getDest();
							int[][] ldest = lsb.getDest();

							Segment seg = new Segment();
							seg.P0 = ldest[3][a];
							seg.P1 = ldest[2][a];
							seg.P2 = ldest[1][a];
							seg.P3 = ldest[0][a];
							seg.Q0 = rdest[0][a];
							seg.Q1 = rdest[1][a];
							seg.Q2 = rdest[2][a];
							seg.Q3 = rdest[3][a];

							MBfilter(hev_threshold, interior_limit, mbedge_limit, seg);
							ldest[3][a] = seg.P0;
							ldest[2][a] = seg.P1;
							ldest[1][a] = seg.P2;
							ldest[0][a] = seg.P3;
							rdest[0][a] = seg.Q0;
							rdest[1][a] = seg.Q1;
							rdest[2][a] = seg.Q2;
							rdest[3][a] = seg.Q3;
							
						}
					}
				}
				//sb left
				for(int a=1; a<4;a++) {
					for(int b=0; b<4; b++) {
						SubBlock lsb = rmb.getSubBlock(SubBlock.PLANE.Y1, a-1, b);
						SubBlock rsb = rmb.getSubBlock(SubBlock.PLANE.Y1, a, b);
						for(int c=0; c<4; c++) {

							int[][] rdest = rsb.getDest();
							int[][] ldest = lsb.getDest();
							Segment seg = new Segment();
							seg.P0 = ldest[3][c];
							seg.P1 = ldest[2][c];
							seg.P2 = ldest[1][c];
							seg.P3 = ldest[0][c];
							seg.Q0 = rdest[0][c];
							seg.Q1 = rdest[1][c];
							seg.Q2 = rdest[2][c];
							seg.Q3 = rdest[3][c];
							subblock_filter(hev_threshold,interior_limit,sub_bedge_limit, seg);
							ldest[3][c] = seg.P0;
							ldest[2][c] = seg.P1;
							ldest[1][c] = seg.P2;
							ldest[0][c] = seg.P3;
							rdest[0][c] = seg.Q0;
							rdest[1][c] = seg.Q1;
							rdest[2][c] = seg.Q2;
							rdest[3][c] = seg.Q3;

						}
					}
				}
				//top
				if(y>0) {
					MacroBlock tmb = frame.getMacroBlock(x, y-1);
					for(int b=0; b<4; b++) {
						SubBlock tsb = tmb.getSubBlock(SubBlock.PLANE.Y1, b, 3);
						SubBlock bsb = bmb.getSubBlock(SubBlock.PLANE.Y1, b, 0);
						for(int a=0; a<4; a++) {
							int[][] bdest = bsb.getDest();
							int[][] tdest = tsb.getDest();
							Segment seg = new Segment();

							seg.P0 = tdest[a][3];
							seg.P1 = tdest[a][2];
							seg.P2 = tdest[a][1];
							seg.P3 = tdest[a][0];
							seg.Q0 = bdest[a][0];
							seg.Q1 = bdest[a][1];
							seg.Q2 = bdest[a][2];
							seg.Q3 = bdest[a][3];
							System.out.println("a: "+a);
							MBfilter(hev_threshold, interior_limit, mbedge_limit, seg);
							tdest[a][3] = seg.P0;
							tdest[a][2] = seg.P1;
							tdest[a][1] = seg.P2;
							tdest[a][0] = seg.P3;
							bdest[a][0] = seg.Q0;
							bdest[a][1] = seg.Q1;
							bdest[a][2] = seg.Q2;
							bdest[a][3] = seg.Q3;

						}
					}
				}
				//sb top
				for(int a=1; a<4;a++) {
					for(int b=0; b<4; b++) {
						SubBlock tsb = bmb.getSubBlock(SubBlock.PLANE.Y1, b, a-1);
						SubBlock bsb = bmb.getSubBlock(SubBlock.PLANE.Y1, b, a);
						for(int c=0; c<4; c++) {

							int[][] bdest = bsb.getDest();
							int[][] tdest = tsb.getDest();
							Segment seg = new Segment();
							seg.P0 = tdest[c][3];
							seg.P1 = tdest[c][2];
							seg.P2 = tdest[c][1];
							seg.P3 = tdest[c][0];
							seg.Q0 = bdest[c][0];
							seg.Q1 = bdest[c][1];
							seg.Q2 = bdest[c][2];
							seg.Q3 = bdest[c][3];
							subblock_filter(hev_threshold,interior_limit,sub_bedge_limit, seg);
							tdest[c][3] = seg.P0;
							tdest[c][2] = seg.P1;
							tdest[c][1] = seg.P2;
							tdest[c][0] = seg.P3;
							bdest[c][0] = seg.Q0;
							bdest[c][1] = seg.Q1;
							bdest[c][2] = seg.Q2;
							bdest[c][3] = seg.Q3;
						}
					}
				}
			}
		

	}
	
	private static void simple_segment(
		    int edge_limit,    /* do nothing if edge difference exceeds limit */
		    Segment seg
		) {
		    if( (abs(seg.P0 - seg.Q0)*2 + abs(seg.P1-seg.Q1)/2) <= edge_limit) {
		    	System.out.println("True");
		        common_adjust( true, seg);    /* use outer taps */
		    }
		    else {
		    	System.out.println(false);
		    }
	}
	
	/* All functions take (among other things) a segment (of length at most
    4 + 4 = 8) symmetrically straddling an edge.
    The pixel values (or pointers) are always given in order, from the
    "beforemost" to the "aftermost". So, for a horizontal edge (written "|"),
    an 8-pixel segment would be ordered p3 p2 p1 p0 | q0 q1 q2 q3. */
/* Filtering is disabled if the difference between any two adjacent "interior"
    pixels in the 8-pixel segment exceeds the relevant threshold (I). A more
    complex thresholding calculation is done for the group of four pixels that
    straddle the edge, in line with the calculation in simple_segment() above. */
	public static boolean filter_yes(
			int I,         /* limit on interior differences */
			int E,         /* limit at the edge */
			int p3, int p2, int p1, int p0, /* pixels before edge */
			int q0, int q1, int q2, int q3 /* pixels after edge */
) {
     return   (abs(p0 - q0)*2 + abs(p1-q1)/2) <= E
         &&   abs(p3 - p2) <= I && abs(p2 - p1) <= I    && abs(p1 - p0) <= I
         &&   abs(q3 - q2) <= I && abs(q2 - q1) <= I    && abs(q1 - q0) <= I;
     
     
     /*boolean a, b, c, d, e, f, g, r;
     System.out.println("p0: "+p0+ " q0: "+q0+" p1: "+p1+" q1:"+q1);
     System.out.println("E: "+E);
     System.out.println((abs(p0 - q0)*2 + abs(p1-q1)/2));
     a = (abs(p0 - q0)*2 + abs(p1-q1)/2) <= E;
     b = abs(p3 - p2) <= I;
     c = abs(p2 - p1) <= I;
     d = abs(p1 - p0) <= I;
     e = abs(q3 - q2) <= I;
     f = abs(q2 - q1) <= I;
     g = abs(q1 - q0) <= I;
     r = a && b && c && d &&e && f && g;
     System.out.println("a: "+a+" b: "+b+" c: "+c+" d: "+d+" e: "+e+" f: "+f+" g: "+g+" r: "+r);
     return r;*/
}

	/* Filtering is altered if (at least) one of the differences on either
    side of the edge exceeds a threshold (we have "high edge variance"). */
	public static boolean hev(
			int threshold,
			int p1, int p0, /* pixels before edge */
			int q0, int q1 /* pixels after edge */
	) {
	     return abs(p1 - p0) > threshold || abs(q1 - q0) > threshold;
	}

    public static void subblock_filter(
            int hev_threshold,     /* detect high edge variance */
            int interior_limit,    /* possibly disable filter */
            int edge_limit,
            Segment seg
      ) {
    		int p3 = u2s(seg.P3), p2 = u2s(seg.P2), p1 = u2s(seg.P1), p0 = u2s(seg.P0);
            int q0 = u2s(seg.Q0), q1 = u2s(seg.Q1), q2 = u2s(seg.Q2), q3 = u2s(seg.Q3);
           if( filter_yes( interior_limit, edge_limit, q3, q2, q1, q0, p0, p1, p2, p3))
           {

                 boolean hv = hev( hev_threshold, p1, p0, q0, q1);
                 int a = ( common_adjust( hv, seg) + 1) >> 1;
                 if( !hv) {
                	 seg.Q1 = s2u( q1 - a);
                	 seg.P1 = s2u( p1 + a);
                 }
           }
           else {

           }
      }
    
    static void MBfilter(
            int hev_threshold,     /* detect high edge variance */
            int interior_limit,    /* possibly disable filter */
            int edge_limit,
            Segment seg
     ) {
    		int p3 = u2s(seg.P3), p2 = u2s(seg.P2), p1 = u2s(seg.P1), p0 = u2s(seg.P0);
    		int q0 = u2s(seg.Q0), q1 = u2s(seg.Q1), q2 = u2s(seg.Q2), q3 = u2s(seg.Q3);
            if( filter_yes( interior_limit, edge_limit, q3, q2, q1, q0, p0, p1, p2, p3))
            {
            	System.out.println("filter_yes");
            	System.out.println("p0: "+p0);
            	System.out.println("s2u(p0): "+s2u(p0));
                if( !hev( hev_threshold, p1, p0, q0, q1))
                {
                	System.out.println("hev");
                    /* Same as the initial calculation in "common_adjust",
                       w is something like twice the edge difference */
                    int w = c( c(p1 - q1) + 3*(q0 - p0) );
                   
                    //System.out.println("ctest: "+c(200));
                    System.out.println("w: "+w);
                    /* 9/64 is approximately 9/63 = 1/7 and 1<<7 = 128 = 2*64.
                       So this a, used to adjust the pixels adjacent to the edge,
                       is something like 3/7 the edge difference. */
                    int a =  (27*w + 63) >> 7;
     				System.out.println("a: "+a);
     				System.out.println( 27*w + 63);
     				System.out.println( c(27*w + 63));
     				System.out.println( (27*w + 63) >> 7);
     				seg.Q0 = s2u( q0 - a);  seg.P0 = s2u( p0 + a);
                    /* Next two are adjusted by 2/7 the edge difference */
                    a = ( 18*w + 63) >> 7;
						System.out.println("a: "+a);
     				seg.Q1 = s2u( q1 - a);  seg.P1 = s2u( p1 + a);
                      /* Last two are adjusted by 1/7 the edge difference */
                      a = ( 9*w + 63) >> 7;
					System.out.println("a: "+a);
					seg.Q2 = s2u( q2 - a);  seg.P2 = s2u( p2 + a);
					System.out.println(seg);
                 } else                                   /* if hev, do simple filter */
                      common_adjust( true, seg);   /* using outer taps */
           }
      }


	
	private static int common_adjust(
		    boolean use_outer_taps,   /* filter is 2 or 4 taps wide */
		    Segment seg
		) {
		    int p1 = u2s( seg.P1);   /* retrieve and convert all 4 pixels */
		    int p0 = u2s( seg.P0);
		    int q0 = u2s( seg.Q0);
		    int q1 = u2s( seg.Q1);
		    /* Disregarding clamping, when "use_outer_taps" is false, "a" is 3*(q0-p0).
		     Since we are about to divide "a" by 8, in this case we end up
		     multiplying the edge difference by 5/8.
		     When "use_outer_taps" is true (as for the simple filter),
		     "a" is p1 - 3*p0 + 3*q0 - q1, which can be thought of as a refinement
		     of 2*(q0 - p0) and the adjustment is something like (q0 - p0)/4. */
		  int a = c( ( use_outer_taps? c(p1 - q1) : 0 ) + 3*(q0 - p0) );
		  /* b is used to balance the rounding of a/8 in the case where
		     the "fractional" part "f" of a/8 is exactly 1/2. */
		  int b = (a & 7)==4 ? -1 : 0;
		  /* Divide a by 8, rounding up when f >= 1/2.
		     Although not strictly part of the "C" language,
		     the right-shift is assumed to propagate the sign bit. */
		  a = c( a + 4) >> 3;
		  /* Subtract "a" from q0, "bringing it closer" to p0. */
		  seg.Q0 = s2u( q0 - a);
		  /* Add "a" (with adjustment "b") to p0, "bringing it closer" to q0.
		     The clamp of "a+b", while present in the reference decoder, is
		     superfluous; we have -16 <= a <= 15 at this point. */
		  seg.P0 = s2u( p0 + c( a + b));

		  return a;
		}



    private static int c( int v) {
          //return (int) (v < -128 ? -128 : (v > 127 ? v : 127));
    	int r=v;
    	if (v<-128)
    		r=-128;
    	if(v>127)
    		r=127;
    	return r;
    }
    
    /* Convert pixel value (0 <= v <= 255) to an 8-bit signed number. */
    private static int u2s( int v) { 
    	return (int) (v - 128);
    }
    /* Clamp, then convert signed number back to pixel value. */
    private static int s2u( int v) { 
    	return (int) ( c(v) + 128);
    	}

    private static int abs( int v) { 
    	return v < 0? -v : v;
    }
}
