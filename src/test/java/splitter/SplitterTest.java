package splitter;

import org.junit.Test;
import static org.junit.Assert.*;

public class SplitterTest {
	
	@Test
	public void test1() throws InterruptedException {
		Splitter s = new Splitter(2);
		assertEquals( 100, s.val(0) );
		assertEquals( 0, s.val(1) );
		
		
		for (int i = 0; i < 10; i++) {
			s.setItem(1, 30);
			Thread.sleep(10);
			assertEquals( 70, s.val(0));
			assertEquals( 30, s.val(1) );
			
			s.setItem(0, 20);
			Thread.sleep(10);
			assertEquals( 20, s.val(0) );
			assertEquals( 80, s.val(1) );
			
			s.setItem(0, 30);
			Thread.sleep(10);
			assertEquals( 30, s.val(0) );
			assertEquals( 70, s.val(1) );
			
			s.setItem(1, 30);
			Thread.sleep(10);
			assertEquals( 70, s.val(0) );
			assertEquals( 30, s.val(1) );
			
			s.setItem(1, 100);
			Thread.sleep(10);
			assertEquals( 0, s.val(0) );
			assertEquals( 100, s.val(1) );
			
		}
		
	}
	
	@Test
	public void test2() throws InterruptedException {
		Splitter s = new Splitter(3);
		assertEquals( 100, s.val(0) );
		assertEquals( 0, s.val(1) );
		assertEquals( 0, s.val(2) );
		
		s.setItem(1, 30);
		Thread.sleep(10);
		assertEquals( 70, s.val(0));
		assertEquals( 30, s.val(1) );
		assertEquals( 0, s.val(2) );
		
		s.setItem(2, 20);
		Thread.sleep(10);
		assertEquals( 50, s.val(0) );
		assertEquals( 30, s.val(1) );
		assertEquals( 20, s.val(2) );
		
		s.setItem(0, 90);
		Thread.sleep(10);
		assertEquals( 90, s.val(0) );
		assertEquals( 0, s.val(1) );
		assertEquals( 10, s.val(2) );
		
		s.setItem(2, 90);
		Thread.sleep(10);
		assertEquals( 10, s.val(0) );
		assertEquals( 0, s.val(1) );
		assertEquals( 90, s.val(2) );
		
		s.setItem(1, 95);
		Thread.sleep(10);
		assertEquals( 0, s.val(0) );
		assertEquals( 95, s.val(1) );
		assertEquals( 5, s.val(2) );
		
	}
	
	@Test
	public void test3() throws InterruptedException {
		Splitter s = new Splitter(3);
		assertEquals( 100, s.val(0) );
		assertEquals( 0, s.val(1) );
		assertEquals( 0, s.val(2) );
		
		s.setItem(2, 30);
		Thread.sleep(10);
		assertEquals( 70, s.val(0));
		assertEquals( 0, s.val(1) );
		assertEquals( 30, s.val(2) );
		
		s.setItem(1, 20);
		Thread.sleep(10);
		assertEquals( 50, s.val(0) );
		assertEquals( 20, s.val(1) );
		assertEquals( 30, s.val(2) );
		
		s.setItem(0, 90);
		Thread.sleep(10);
		assertEquals( 90, s.val(0) );
		assertEquals( 10, s.val(1) );
		assertEquals( 0, s.val(2) );
		
		s.setItem(2, 90);
		Thread.sleep(10);
		assertEquals( 10, s.val(0) );
		assertEquals( 0, s.val(1) );
		assertEquals( 90, s.val(2) );
		
	}
	
	@Test
	public void test4() throws InterruptedException {
		Splitter s = new Splitter(3);
		
		for (int i = 0; i < 100; i++) {
			s.setItem(1, 30);
			s.setItem(2, 20);
			s.setItem(0, 90);
			s.setItem(2, 90);
			
			Thread.sleep(10);
			
			assertEquals( 10, s.val(0) );
			assertEquals( 0, s.val(1) );
			assertEquals( 90, s.val(2) );	
		}
			
	}
	
	@Test
	public void test5() throws InterruptedException {
		Splitter s = new Splitter(4);
		
		s.setItem(0, 50);
		s.setItem(1, 20);
		
		Thread.sleep(50);
		
		assertEquals( 50, s.val(0) );
		assertEquals( 20, s.val(1) );
		assertEquals( 30, s.val(2) );
		
		s.setItem(2, 40);
				
		Thread.sleep(50);
		
		assertEquals( 40, s.val(0) );
		assertEquals( 20, s.val(1) );
		assertEquals( 40, s.val(2) );
		assertEquals( 0, s.val(3) );
		
		s.setItem(0, 0);
		
		Thread.sleep(50);
		
		assertEquals( 0, s.val(0) );
		assertEquals( 20, s.val(1) );
		assertEquals( 40, s.val(2) );
		assertEquals( 40, s.val(3) );
		
		s.setItem(3, 10);
		
		Thread.sleep(50);
		
		assertEquals( 0, s.val(0) );
		assertEquals( 50, s.val(1) );
		assertEquals( 40, s.val(2) );
		assertEquals( 10, s.val(3) );
		
		s.setItem(3, 90);
		
		Thread.sleep(50);
		
		assertEquals( 0, s.val(0) );
		assertEquals( 0, s.val(1) );
		assertEquals( 10, s.val(2) );
		assertEquals( 90, s.val(3) );
		
	}

}
