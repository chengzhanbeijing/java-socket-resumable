package fileupload.socket.client;

public class TestThread extends Thread{

	private String name;

	public TestThread(String name) {
		super();
		this.name = name;
	}
	private int ticket = 10;
	public void run(){
		for(int i=0;i<20;i++){
			if(ticket>0){
				System.out.println(this.name+","+Thread.currentThread().getName()+"卖出票"+ticket--);
			}
		}
	}
	public static void main(String[] args) {
		Runnable t = new TestThread("主线程");
		new Thread(t,"a窗口").start();
		new Thread(t,"b窗口").start();
		new Thread(t,"c窗口").start();
		
	}
}
