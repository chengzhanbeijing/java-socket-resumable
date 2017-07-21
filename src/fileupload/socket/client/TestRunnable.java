package fileupload.socket.client;

public class TestRunnable implements Runnable{
	private String name;
	
	private int ticket = 10;
	
	public TestRunnable(String name) {
		super();
		this.name = name;
	}

	public static void main(String[] args) {
		/*TestRunnable ab = new TestRunnable("runn");
		new Thread(ab,"th1").start();
		new Thread(ab,"th2").start();
		new Thread(ab,"th3").start();*/
		test();
	}
	
	public static void test(){
		Runnable abs1 = new TestRunnable("run1");
		Runnable abs2 = new TestRunnable("run2");
		Runnable abs3 = new TestRunnable("run3");
		new Thread(abs1,"th1").start();
		new Thread(abs2,"th2").start();
		new Thread(abs3,"th3").start();
	}

	@Override
	public void run() {
		for(int i=0;i<20;i++){
			if(ticket>0){
				System.out.println(this.name+","+Thread.currentThread().getName()+"卖出票"+ticket--);
			}
		}
	}

}
