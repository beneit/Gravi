public class Particles{
	public static double[][] parts;
	public static int n;
	private static double G=6.6742e-11;
	public static double[] sp;
	
	public Particles(double[][] nParts){
		parts = nParts;
		n = parts.length;
	}
	public Particles(){}
	
	public double [][] getParticles(){
		return parts;
	}
	public void setParticles(double[][] nparts){
		parts=nparts;
	}
	public void setParticles(Particles nparts){
		parts=nparts.getParticles();
	}
 
	public static double[][] eval(double dt, int k){
		for (int l=0;l<k;l++){			//#approx
			double[] x = new double[n];
			double[] y = new double[n];
			for (int i=0;i<n;i++){		//++calc all a parts
				for (int j=0;j<n;j++){
					if (j==i) j++;
					if (j!=n){
						x[i]=x[i]+parts[j][0]*(parts[j][1]-parts[i][1])*Math.pow(Math.pow(parts[j][1]-parts[i][1],2)+Math.pow(parts[j][2]-parts[i][2],2),-1.5);
						y[i]=y[i]+parts[j][0]*(parts[j][2]-parts[i][2])*Math.pow(Math.pow(parts[j][1]-parts[i][1],2)+Math.pow(parts[j][2]-parts[i][2],2),-1.5);
					}
				}
				x[i]=G*x[i];
				y[i]=G*y[i];
			}							//--calc all a parts
			for (int i=0;i<n;i++){		//++calc all v parts
				parts[i][3]=parts[i][3]+dt*x[i];
				parts[i][4]=parts[i][4]+dt*y[i];
			}							//--calc all v parts
			for (int i=0;i<n;i++){		//++calc all x parts
				parts[i][1]=parts[i][1]+dt*parts[i][3];
				parts[i][2]=parts[i][2]+dt*parts[i][4];
			}							//--calc all x parts	
		}
		return parts;
	}
	
	public static double[] calcMaxVol(){	//[0]:MaxVol,[i]:Schwerpunkt[i]
		if (parts==null){ return new double[3];}
			// calc schwerpunkt
		double nsp[] = {0,0,0};
		double m = 0;
		for (int i=0;i<n;i++){
			nsp[1]=nsp[1]+parts[i][0]*parts[i][1];
			nsp[2]=nsp[2]+parts[i][0]*parts[i][2];
			m=m+parts[i][0];
		}
		nsp[1]=nsp[1]/m; nsp[2]=nsp[2]/m;
			// calc max dif schwerpunkt-parts
		double newdif=0;
		for (int i=0;i<n;i++){
			newdif=Math.pow(Math.pow(nsp[1]-parts[i][1],2)+Math.pow(nsp[2]-parts[i][2],2),0.5);
			if (newdif>nsp[0]) nsp[0]=newdif;
		}
		nsp[0]=2.2*nsp[0];
		sp=nsp;
		//System.out.println("Volumen:"+nsp[0]+" Schwerpunkt: "+nsp[1]+", "+nsp[2]);
		return sp;
	}
	
	public static void printClass() {
		String s = "";
		for (int i=0;i<n;i++){
			s=s+"{"+parts[i][0]+","+parts[i][1]+","+parts[i][2]+","+parts[i][3]+","+parts[i][4]+"} ";
		}
		//System.out.println(this.getClass().getName()+": "+s);
		System.out.println(s);
	}
	public static void printClassp() {
		String s = "";
		for (int i=0;i<n;i++){
			s=s+"{"+(int)(parts[i][1])+","+(int)(parts[i][2])+","+(int)(parts[i][3])+","+(int)(parts[i][4])+"} ";
		}
		System.out.println(s);
	}
	public String toString() {
		String s = "";
		for (int i=0;i<n;i++){
			s=s+"{"+parts[i][0]+","+parts[i][1]+","+parts[i][2]+","+parts[i][3]+","+parts[i][4]+"} ";
		}
		return s;
	}
}