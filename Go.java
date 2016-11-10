import java.awt.*; 
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.awt.event.*;


//Main
public abstract class Go{
	static boolean running = true;
	static boolean track = true; static int maxtrack=600; static int trackperiodperfps = 15;
	static double dt = 20; 			//dt/s einziger Parameter für die Genauigkeit
	static int k = 2000;			//Es werden nacheinander gleich k Zeitintervalle dt berechnet, die nicht alle graphisch dargstellt werden
	static int maxfps = 50;		//WICHTIG!!
	static Color[] color;
	static double[][][] savedparts; static double p; static double[] sp;
	static int x0; static int y0;
			
	public static void main(String[] args){
	
	//Systen.println(Double.MAX());
	//DEFINITIONS
		
		double ae=149597870e3; 	//Astronomische Einheit
		double[] r={57.9e9,108.2e9,ae,0.3844e9,228.0e9};
		double ew=5.974e24;		//Erdgewicht
		double[] m={332950*ew,0.055*ew,0.815*ew,ew,0.0123*ew,0.107*ew,317.83*ew,95.16*ew,14.54*ew,17.14*ew,0.0021*ew};
		double G=6.6742e-11;	//Gravitation

		
		double[][] parts =
		//			{{m[0],0,0,0,0},{ew,ae,0,0,Math.pow(G*m[0]/ae,0.5)}}; //System Sonne-Erde
		//			{{m[0],0,0,0,2000},{ew,ae,0,0,Math.pow(G*m[0]/ae,0.5)+2000}}; //System Sonne-Erde floating
					{{m[0],0,0,0,0},{ew,0,-ae,Math.pow(G*m[0]/ae,0.5),0},{m[4],+r[3],-ae,Math.pow(G*m[0]/ae,0.5),-Math.pow(G*ew/r[3],0.5)}}; //System Sonne-Erde-Mond
		//			{{ew,0,0,0,m[4]/ew*Math.pow(G*ew/r[3],0.5)},{m[4],r[3],0,0,-Math.pow(G*ew/r[3],0.5)}}; //System Erde-Mond
			//System Sonne-Merkur-Venus-Erde-Mars
		/*		 	{{330000*ew,x0,y0,0,0}, 		//sun
					{,,,,},						//mercury
					{,,,,},						//venus
					{ew,x0+ae/p,y0,0,1.5297e-3},//earth
					{,,,,}}; 					//mars*/
		//			{{30000*ew,0,0,0,-1000},{10000*ew,ae,0,0,3000}};  //System 30000*Erde + 10000*Erde
		//			{{m[0],x0,y0,0,0},{ew,x0,y0-0.5*ae/p,Math.pow(G*m[0]/ae,0.5)/p,0}};
		//			{{300000*ew,0,0,0,0.25*Math.pow(G*30000*ew/ae,0.5)},{150000*ew,ae,0,0,-0.5*Math.pow(G*30000*ew/ae,0.5)}};
		//			{{30000*ew,0,0,0,0.25*Math.pow(G*30000*ew/ae,0.5)},{15000*ew,ae,0,0,-0.5*Math.pow(G*30000*ew/ae,0.5)},{10000*ew,0+0.5*ae,0+ae,0,0}};
		//			{{300000*ew,0,0,0,0},{300000*ew,ae,0,0,500}};

		
		int n=parts.length;
		Particles.parts=parts;
		Particles.n=n;
		
		color = new Color[n];
		for (int i=0;i<n;i++){color[i]=Color.RED;}
		if (n>1){color[1]=Color.BLUE;}
		if (n>2){color[2]=Color.WHITE;}

	//GRAPHICS
		
		JFrame frame1 = new JFrame("Gravitational Movement");
		JPanel panel1 = new JPanel(new FlowLayout(0));
		
		JButton bRescale = new JButton("rescale");
		bRescale.addActionListener(new BRescale());
		//bRescale.setPreferredSize(new Dimension(100, 20));
		
	//	JFormattedTextField tft1 = new JFormattedTextField(NumberFormat.getIntegerInstance());
	//	ftf1.setValue(new Integer(k)); //get by (Integer)tft1.getValue();
		

		frame1.setIgnoreRepaint(true);
		frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				// Get graphics configuration...
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle rect = ge.getMaximumWindowBounds();
		Dimension dims = new Dimension(rect.width-40,rect.height-50);
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
			// Create off-screen drawing surface
		BufferedImage bi = gc.createCompatibleImage(dims.width,dims.height);
		
		    // Create canvas for painting...
		Canvas canvas1 = new Canvas();
		canvas1.setIgnoreRepaint(true);
		canvas1.setSize(dims);
		
		frame1.getContentPane().setLayout(new FlowLayout(1));
		panel1.add(bRescale);
		frame1.add(panel1);
															//frame1.add(bRescale, BorderLayout.LINE_START); 
		frame1.add(canvas1, BorderLayout.CENTER);

		frame1.pack();
		frame1.setBackground(Color.black);
		frame1.setVisible(true);
			// Create BackBuffer...
		canvas1.createBufferStrategy(2);
		BufferStrategy buffer = canvas1.getBufferStrategy();
		
		    // Objects needed for rendering...
		Graphics graphics = null;
		Graphics2D g2d = null;

		System.out.println(dims);
		run(dims,bi,buffer,graphics,g2d);
	}

	
	public static void run(Dimension dims,
					BufferedImage bi, BufferStrategy buffer, Graphics graphics, Graphics2D g2d){
		
		Color background = Color.BLACK;
		
		boolean contourb = false;
		double contour[] =
							{12700000,1238000};		//Erde-Mond
		sp=Particles.calcMaxVol();
		p=sp[0]/dims.height;
		Particles.printClass();
		x0=dims.width/2; y0=dims.height/2;
		
		int fps=maxfps;
		int skipticks = 1000/fps;
		long nextTick = System.currentTimeMillis();
		long sleepTime = 0;
			//	Save data
		int n = Particles.n;
		savedparts = new double[maxtrack][n][2];	//#Gespeicherte Positionen, #planets, #Koordinaten
		int j = 0;
		int o = 0;
		boolean savetrack = false;
		
			// Time
		double t = 0; boolean insek=true; boolean inh=false;boolean ind=false; boolean inm=false; boolean iny=false;
		String u = "s";
		if (dt*k/maxfps>10){insek=false; inh=true; u="h";}
		if (dt*k/maxfps/3600>10){inh=false; ind=true; u="d";}
		if (dt*k/maxfps/3600/24>10){ind=false; inm=true; u="mo";}
		if (dt*k/maxfps/3600/24/30.4375>10){inm=false; iny=true; u="y";}	
		
		while(running){
			try{
					// clear back buffer...
				g2d = bi.createGraphics();
				g2d.setColor(background);
				g2d.fillRect(0,0,dims.width,dims.height);
			
					// draw some ovals...
				if (track){
					if(o==trackperiodperfps){
						savetrack=true;
						o=0;
						if (j==maxtrack){j=0;}
					}
					else{o++;}
				}
				for (int i=0;i<n;i++){
					g2d.setColor(color[i]);
					if(savetrack){
						savedparts[j][i][0]=Particles.parts[i][1];
						savedparts[j][i][1]=Particles.parts[i][2];
					}
					if (track){
						for (int l=0;l<maxtrack;l++){
							g2d.fillOval((int)((savedparts[l][i][0]-sp[1])/p)+x0-1,(int)((savedparts[l][i][1]-sp[2])/p)+y0-1,2,2);
						}
					}
					if (contourb){
						g2d.drawOval((int)((Particles.parts[i][1]-sp[1]-contour[i]/2)/p+x0),(int)((Particles.parts[i][2]-sp[2]-contour[i]/2)/p+y0),(int)(contour[i]/p),(int)(contour[i]/p));
					}
					else{
						g2d.drawOval((int)((Particles.parts[i][1]-sp[1])/p)+x0-4,(int)((Particles.parts[i][2]-sp[2])/p)+y0-4,8,8);
					}
				}
				if (savetrack){
					savetrack=false;
					j++;
				}
				
				Particles.eval(dt,k);
				//Particles.printClassp();
				
					// display frames per second...
				g2d.setFont(new Font("Courier New",Font.PLAIN,12));
				g2d.setColor(Color.GREEN);
				g2d.drawString(String.format("FPS: %s",fps),20,20);
					// display realtime...
				g2d.drawString(String.format("t = %s"+u,(int)t),20,40);
				if(iny){
					t=t+dt*k/3600/24/365.25;
				}
				else{
					if(inm){
						t=t+dt*k/3600/24/30.4375;
						if(t>36){inm=false; iny=true; u="y"; t=t/12;}
					}
					else{
						if(ind){
							t=t+dt*k/3600/24;
							if(t>36){ind=false; inm=true; u="mo"; t=t/30.4375;}
						}
						else{
							if(inh){
								t=t+dt*k/3600;
								if(t>36){inh=false; ind=true; u="d"; t=t/24;}
							}
							else{
								t=t+dt*k;
								if(t>3600){insek=false; inh=true; u="h"; t=t/3600;}
							}
						}
					}
				}
				// display scale...
				g2d.drawLine(x0-50,20,x0+50,20);
				g2d.drawLine(x0-50,10,x0-50,30);g2d.drawLine(x0+50,10,x0+50,30);g2d.drawLine(x0,15,x0,25);
				g2d.drawString(String.format("100px = %skm",(int)(p*.1)),x0+70,20);
				
					// Blit image and flip...
				graphics = buffer.getDrawGraphics();
				graphics.drawImage(bi,0,0,null);
				if(!buffer.contentsLost())
				buffer.show();
				
					// Giving time...
				Thread.yield();
				nextTick += skipticks;
				sleepTime = nextTick - System.currentTimeMillis();
				if(sleepTime >= 0){
					Thread.sleep(sleepTime);
					if (fps<maxfps){
						fps++;
						skipticks=1000/fps;
					}
				}
				else{
					if (fps>5){
						fps--;
						skipticks=1000/fps;
					}
				}
			}
			catch(InterruptedException e){}
			finally{
					// release resources
				if(graphics != null) 
					graphics.dispose();
				if(g2d != null) 
					g2d.dispose();
			}
		}
	}
	
}

class BRescale implements ActionListener{
	public void actionPerformed(ActionEvent evt){
		
		double[] sp1=Particles.sp;
		double p1=Go.p;
		double[] sp2=Particles.calcMaxVol();
		double p2=p1*sp2[0]/sp1[0];

		Go.p=p2;
		Go.sp=sp2;
		
	}
}




