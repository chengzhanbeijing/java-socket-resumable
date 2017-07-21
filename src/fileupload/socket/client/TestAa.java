package fileupload.socket.client;

public class TestAa {
	public static byte SEMI_COLON = 59;
	public static byte CR = 13;
	public static byte LF = 10;
	public static byte aa = 78;
	public static byte bb = 45;
	public static byte COLON = 58;
	public static byte[] END_HEADER = new byte[]{SEMI_COLON, CR, LF, CR, LF};
	public static byte[] chinese_zhong = new byte[]{aa,bb};
	public static void main(String[] args) {
		//TODO 判断Charset.forName()的设计模式
		//System.out.println(new String(chinese_zhong,Charset.forName("Unicode")));
		//System.out.println(new String(END_HEADER,Charset.forName("Ascii")));
		/*byte[] m = charToByte('中');
		byte[] m1;
		try {
			m1 = "中".getBytes("utf-8");
			for(byte temp:m){
				System.out.println(temp);
			}
			for(byte temp:m1){
				System.out.println(temp);
			}
			System.out.println(m1.length);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}*/
		//System.out.println(toSecond(20525));
		//System.out.println(reverse(toSecond(20525)));
		System.out.println(reverse(toSecond(15*16)));
		
	}
	public static byte[] charToByte(char c){
		byte[] c1 = new byte[2];
		c1[0] = (byte)((c&0xFF00)>>8);
		c1[1] = (byte)(c&0xFF);
		return c1;
	}
	
	public static String toSecond(int a){
		StringBuilder sb = new StringBuilder();
		while(a/2!=0){
			sb.append(a-(a/2)*2);
			a = a/2;
		}
		sb.append(a);
		return sb.toString();
	}
	
	public static String reverse(String str){
		int len = str.length();
		if(len>0){
			StringBuilder sb = new StringBuilder();
			for(int i=0;i<len;i++){
					sb.append(str.charAt(len-1-i));
			}
			return sb.toString();
		}else{
			return str;
		}
	}
}
