package com.github.lindenb.jsvelocity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class JSVelocityTest {
	
	private String concat(final Object...o)
		{
		return Arrays.stream(o).map(O->O.toString()).collect(Collectors.joining("\n"));
		}
	
	private File createTmpFile(final String suffix,final String content) throws IOException {
		final File f = File.createTempFile("test", suffix);
		f.deleteOnExit();
		final PrintWriter pw = new PrintWriter(f);
		if(content!=null) pw.print(content);
		pw.flush();
		pw.close();
		return f;
		}
	
	private String readFile(final File f) throws IOException {
		final StringBuilder b=new StringBuilder();
		final FileReader r = new FileReader(f);
		int c;
		while((c=r.read())!=-1) b.append((char)c);
		r.close();
		return b.toString();
		}
	
	@Test
	public void test1() throws IOException {
		final File out = File.createTempFile("test", ".out");
		final File f=createTmpFile(".vm","Hello");
		Assert.assertEquals(readFile(f),"Hello");
		JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				"-o",out.getPath(),
				f.getPath()
				}));
		Assert.assertEquals(readFile(out),"Hello");
		Assert.assertTrue(out.delete());
	}
	
	@Test
	public void test2() throws IOException {
		final File out = File.createTempFile("test", ".out");
		final File f=createTmpFile(".vm","Hello $world ${world}");
		JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				"-s","world","Pierre",
				"-o",out.getPath(),
				f.getPath()
				}));
		Assert.assertEquals(readFile(out),"Hello Pierre Pierre");
		Assert.assertTrue(out.delete());
	}
	
	@DataProvider(name = "json1")
	public Object[][] createJsonExamples() {
	 return new Object[][] {
		   {"Hello ${J.name} ${J.age} ${J.a.size()} ${J.a.parent().name}","{\"name\":\"Pierre\",\"age\":17,\"a\":[1,2,3,4]}","Hello Pierre 17 4 Pierre"},
		   {"#if(${J.name})YES#end","{\"name\":\"Pierre\"}","YES"},
		   {"#if(${J.zzzz})YES#end","{\"name\":\"Pierre\"}",""},
		   {"${J[0]} ${J[2]}","[123,null,456]","123 456"}
	 };
	}
	
	@Test(dataProvider = "json1")
	public void testJsonExpr(final String templateStr, final String jsonStr,final String outputStr) throws IOException {
		final File out = File.createTempFile("test", ".out");
		final File f=createTmpFile(".vm",templateStr);
		JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				"-e","J",jsonStr,
				"-o",out.getPath(),
				f.getPath()
				}));
		Assert.assertEquals(readFile(out),outputStr);
		Assert.assertTrue(out.delete());
	}
	
	@Test(dataProvider = "json1")
	public void testJsonFile(final String templateStr, final String jsonStr,final String outputStr) throws IOException {
		final File out = File.createTempFile("test", ".out");
		final File f=createTmpFile(".vm",templateStr);
		final File js=createTmpFile(".js",jsonStr);
		JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				"-f","J",js.getPath(),
				"-o",out.getPath(),
				f.getPath()
				}));
		Assert.assertEquals(readFile(out),outputStr);
		Assert.assertTrue(out.delete());
	}

	@Test(dataProvider = "json1")
	public void testJsonDirective(final String templateStr, final String jsonStr,final String outputStr) throws IOException {
		final File out = File.createTempFile("test", ".out");
		final File f=createTmpFile(".vm","#json(\"J\")"+jsonStr+"#{end}"+templateStr);
		JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				"-o",out.getPath(),
				f.getPath()
				}));
		Assert.assertEquals(readFile(out),outputStr);
		Assert.assertTrue(out.delete());
	}

	@Test(dataProvider = "json1")
	public void testDivert(final String templateStr, final String jsonStr,final String outputStr) throws IOException {
		final File out = File.createTempFile("test", ".out");
		final File divertFile = File.createTempFile("test", ".divert");
		divertFile.delete();
		final File f=createTmpFile(".vm",
			"HELLO"+
			"#divert(\""+divertFile.getPath()+"\",false)"+templateStr+"#{end}" +
			"#divert(\""+divertFile.getPath()+"\",true)"+templateStr+"#{end}"
			);
		JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				"-e","J",jsonStr,
				"-o",out.getPath(),
				f.getPath()
				}));
		Assert.assertTrue(divertFile.exists());		
		Assert.assertEquals(readFile(out),"HELLO");
		Assert.assertEquals(readFile(divertFile),outputStr+outputStr);
		Assert.assertTrue(out.delete());		
		Assert.assertTrue(divertFile.delete());	
	}
	
}
