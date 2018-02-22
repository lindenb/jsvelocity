package com.github.lindenb.jsvelocity;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class JSVelocityTest {
	
	
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
	
	@Test
	public void testTsv() throws IOException {
		final File out = createTmpFile(".txt","A\tB\n12\t23");
		JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				"--tsv","T",out.getPath(),
				"-o",out.getPath(),
				"-T","${T[0][1]}${T[1][0]}"
				}));
		Assert.assertEquals(readFile(out),"B12");
		Assert.assertTrue(out.delete());
	}
	
	@Test
	public void testHashTable() throws IOException {
		final File out = createTmpFile(".txt","A\tB\n12\t23\nx\ty");
		JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				"--hashtable","T",out.getPath(),
				"-o",out.getPath(),
				"-T","${T[0][\"A\"]}/${T[1][\"B\"]}"
				}));
		Assert.assertEquals(readFile(out),"12/y");
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
	public void testInlineTemplate(final String templateStr, final String jsonStr,final String outputStr) throws IOException {
		final File out = File.createTempFile("test", ".out");
		JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				"-s","world","Pierre",
				"-o",out.getPath(),
				"-e","J",jsonStr,
				"-T",templateStr
				}));
		Assert.assertEquals(readFile(out),outputStr);
		Assert.assertTrue(out.delete());
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
	
	@DataProvider(name = "yaml1")
	public Object[][] createYamlExamples() {
	 return new Object[][] {
		   {"${J[\"invoice\"]}","invoice: 34843","34843"}
	 };
	}
	@Test(dataProvider = "yaml1")
	public void testYaml(final String templateStr, final String yamlStr,final String outputStr) throws IOException {
		final File out = File.createTempFile("test", ".out");
		final File f=createTmpFile(".vm",templateStr);
		final File yamlFile =createTmpFile(".yml",yamlStr);
		JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				"--yaml","J",yamlFile.getPath(),
				"-o",out.getPath(),
				f.getPath()
				}));
		Assert.assertEquals(readFile(out),outputStr);
		Assert.assertTrue(out.delete());
	}

	
	
	@Test
	public void testJavascript() throws IOException {
		final String templateStr="#javascript(1,\"A\") print(args[1]); for(var i=8;i<11;i++) print(\"\"+i);print(J);#{end}";
		final File f=createTmpFile(".vm",templateStr);
		final String outputStr="A8910HELLO";
		final File out = File.createTempFile("test", ".out");
		JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				"-s","J","HELLO",
				"-o",out.getPath(),
				f.getPath()
				}));
		Assert.assertEquals(readFile(out),outputStr);
		Assert.assertTrue(out.delete());		
		}
	@Test
	public void testReadFileList() throws IOException {
		final File dataFile=createTmpFile(".txt","A\nB\nC");
		final String templateStr="#readfile(\"T\",\""+dataFile.getPath()+"\")${T.size()}${T[0]}${T[1]}${T[2]}";
		final File f=createTmpFile(".vm",templateStr);
		final File out = File.createTempFile("test", ".out");
		JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				"-o",out.getPath(),
				f.getPath()
				}));
		Assert.assertEquals(readFile(out),"3ABC");
		Assert.assertTrue(out.delete());		
		Assert.assertTrue(dataFile.delete());		
		}
	@Test
	public void testReadFileTable() throws IOException {
		final File dataFile=createTmpFile(".txt","A,B,C\nD,E,F");
		final String templateStr="#readfile(\"T\",\""+dataFile.getPath()+"\",\"method:table;delim:comma;\")${T[0][1]}${T[1][0]}";
		final File f=createTmpFile(".vm",templateStr);
		final File out = File.createTempFile("test", ".out");
		JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				"-o",out.getPath(),
				f.getPath()
				}));
		Assert.assertEquals(readFile(out),"BD");
		Assert.assertTrue(out.delete());		
		Assert.assertTrue(dataFile.delete());		
		}
	
	@Test
	public void testReadFileHash() throws IOException {
		final File dataFile=createTmpFile(".txt","x,y,z\nD,TITI,F\nG,TOTO,I");
		final String templateStr="#readfile(\"T\",\""+dataFile.getPath()+"\",\"method:hash;pkey:y;delim:comma;\")${T[\"TITI\"][\"x\"]}${T[\"TOTO\"][\"z\"]}";
		final File f=createTmpFile(".vm",templateStr);
		final File out = File.createTempFile("test", ".out");
		JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				"-o",out.getPath(),
				f.getPath()
				}));
		Assert.assertEquals(readFile(out),"DI");
		Assert.assertTrue(out.delete());		
		Assert.assertTrue(dataFile.delete());		
		}
	@Test
	public void testReadFileHashTable() throws IOException {
		final File dataFile=createTmpFile(".txt","x,y,z\nD,TITI,F\nG,TOTO,I");
		final String templateStr="#readfile(\"T\",\""+dataFile.getPath()+"\",\"method:hashtable;delim:comma;\")${T[0][\"x\"]}${T[1][\"z\"]}";
		final File f=createTmpFile(".vm",templateStr);
		final File out = File.createTempFile("test", ".out");
		JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				"-o",out.getPath(),
				f.getPath()
				}));
		Assert.assertEquals(readFile(out),"DI");
		Assert.assertTrue(out.delete());		
		Assert.assertTrue(dataFile.delete());		
		}
	@Test
	public void testClass() throws IOException {
		final File f=createTmpFile(".vm","${S.name}");
		final File out = File.createTempFile("test", ".out");
		JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				"-o",out.getPath(),
				"-C","S","java.lang.String",
				f.getPath()
				}));
		Assert.assertEquals(readFile(out),String.class.getName());
		Assert.assertTrue(out.delete());		
		}
	
	@Test
	public void testClassInstance() throws IOException {
		final File f=createTmpFile(".vm","${S.length()}");
		final File out = File.createTempFile("test", ".out");
		JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				"-o",out.getPath(),
				"-c","S","java.lang.String",
				f.getPath()
				}));
		Assert.assertEquals(readFile(out),"0");
		Assert.assertTrue(out.delete());		
		}
	
	@Test
	public void testClassInstanceStr() throws IOException {
		final File out = File.createTempFile("test", ".out");
		JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				"-o",out.getPath(),
				"-cstr","S","java.io.File",System.getProperty("java.io.tmpdir",""),
				"-T","${S.exists()} ${S.isDirectory()}"
				}));
		Assert.assertEquals(readFile(out),"true true");
		Assert.assertTrue(out.delete());		
		}
	@Test
	public void testXml() throws IOException {
		final File xml = File.createTempFile("tmp", ".xml");
		PrintWriter pw=new PrintWriter(xml);
		pw.write("<a><b/></a>");
		pw.flush();
		pw.close();
		final File out = File.createTempFile("test", ".out");
		JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				"-o",out.getPath(),
				"--xml","X",xml.getPath(),
				"-T","${X.documentElement.nodeName}"
				}));
		Assert.assertEquals(readFile(out),"a");
		Assert.assertTrue(out.delete());		
		Assert.assertTrue(xml.delete());		
		}
	
	@DataProvider(name = "tooldata1")
	public Object[][] createData1Example() {
	 return new Object[][] {
		   {"${J.toUpperCase()}","-s","hello","HELLO"},
		   {"${tool.escapeXml($J)}","-s","<>","&lt;&gt;"},
		   {"${tool.escapeHtml($J)}","-s","<>","&lt;&gt;"},
		   {"${tool.capitalize($J)}","-s","hello world","Hello World"},
		   {"${tool.escapeJson($J)}","-s","\"","\\\""},
		   {"${tool.escapeJava($J)}","-s","\"","\\\""},
		   {"${tool.left($J,2)}","-s","12345","12"},
		   {"${tool.left($J,10)}","-s","12345","12345"},
		   {"${tool.right($J,2)}","-s","12345","45"},
		   {"${tool.right($J,10)}","-s","12345","12345"},
		   {"${tool.md5($J)}","-s","","d41d8cd98f00b204e9800998ecf8427e"},
		   {"${tool.sha1($J)}","-s","","da39a3ee5e6b4b0d3255bfef95601890afd80709"},
		   {"${tool.before(\"GAATTC\",\"T\")}","-s","","GAA"},
		   {"${tool.after(\"GAATTC\",\"T\")}","-s","","TC"},
		   {"${tool.before(\"GAATTC\",\"X\")}","-s","",""},
		   {"${tool.parseInt($J)}","-s","12345","12345"},
		   {"${tool.parseInt($J)}","-s","12345.123","12345"},
		   {"${tool.parseDouble($J)}","-s","12345.123","12345.123"},
		   {"#foreach($i in ${tool.range($J)})$i#end","-s","4","0123"},
		   {"#foreach($i in ${tool.range(1,$J)})$i#end","-s","4","123"},
		   {"#foreach($i in ${tool.range(1,$J,2)})$i#end","-s","4","13"},
		   {"#if(${tool.nextId} > 0)YES#end","-s","","YES"},
		   {"#if(${tool.split($J,2).size()} > 0 )YES#end","-e","[1,2,4,5,10,11]","YES"}

	 };
	}
	
	@Test(dataProvider = "tooldata1")
	public void testTool(final String templateStr,final String option, final String jsonStr,final String outputStr) throws IOException {
		final File out = File.createTempFile("test", ".out");
		final File f=createTmpFile(".vm",templateStr);
		JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				option,"J",jsonStr,
				"-o",out.getPath(),
				f.getPath()
				}));
		Assert.assertEquals(readFile(out),outputStr);
		Assert.assertTrue(out.delete());
	}

	
	@DataProvider(name = "tooldata2")
	public Object[][] createData2Example() {
		final String xmlstr1="<a><b><c>a</c><c>1234</c><c>false</c></b></a>";
	 return new Object[][] {
		   {"$tool.parseInt(${tool.xpathNumber($X,\"count(//c)\")})",xmlstr1,"3"},
		   {"${tool.xpathString($X,\"/a/b/c[2]/text()\")}",xmlstr1,"1234"},
		   {"${tool.xpath($X,\"//c\").size()}",xmlstr1,"3"}

	 };
	}
	
	@Test(dataProvider = "tooldata2")
	public void testXpathTool(
			final String templateStr,
			final String xmlStr,
			final String outputStr
			) throws IOException {
		final File out = File.createTempFile("test", ".out");
		final File xmlFile=createTmpFile(".xml",xmlStr);
		final JSVelocity instance = new JSVelocity();
		Assert.assertEquals(0,instance.execute(
			new String[] {
				"--xml","X",xmlFile.getPath(),
				"-o",out.getPath(),
				"-T",templateStr
				}));
		Assert.assertEquals(readFile(out),outputStr);
		Assert.assertTrue(out.delete());
		Assert.assertTrue(xmlFile.delete());
	}

}
