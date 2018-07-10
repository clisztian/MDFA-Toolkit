package ch.imetrica.mdfa.util;

public class MdfaUtil {

	
	
	public static double[] convolve(double[] x, double[] h) {

        final int xLen = x.length;
        final int hLen = h.length;

        if (xLen == 0 || hLen == 0) {
            return null;
        }

        // initialize the output array
        final int totalLength = xLen + hLen - 1;
        final double[] y = new double[totalLength];

        for (int n = 0; n < totalLength; n++) {

        	double yn = 0;
            int k = Math.max(0, n + 1 - xLen);
            int j = n - k;
            while (k < hLen && j >= 0) {
                yn += x[j--] * h[k++];
            }
            y[n] = yn;
        }
        return y;
    }
	
	
	public static double[] plus(double[] y, double[] x, double mult) {
		
		for(int i = 0; i < y.length; i++) {
			y[i] += mult*x[i];
		}
		return y;
	}
	
}
