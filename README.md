jsvelocity
==========

Processing json data with apache velocity  ( http://velocity.apache.org/ ).

Original post was http://plindenbaum.blogspot.fr/2011/11/processing-json-data-with-apache.html


Compile
-------
Edit or create a file 'build.properties' in the base directory and set the properties for ivy.
It could be:
```
ivy.jar.dir=${user.home}/.ivy
ivy.install.version=2.3.0
```

The compile process requires the javaCC code compiler.

```bash
ant jsvelocity
```

Usage
-----

```bash
java -jar jsvelocity.jar (options) template.vm
```

Options
-------
* -I (dir) add an alternate path to search for resources.
* -s (key) (string) add this string into the context.
* -e (key) (json-expr) add this json into the context.
* -f (key) (json-file) add this json into the context.
* -i (key) and read stdin-json to the context.
* -C (key) (class.qualified.Name) add this Class into the context.
* -c (key) (class.qualified.Name) add an instance of Class into the context.
* -o (base directory) output base directory (default: current directory)

Default objects
---------------
* out : the writer (default is stdout). This object contains the following methods. close(), flush(), open(String), openIfMissing(String), getFile()
* tool : utility containing the following method: escapeC(String), escapeXml(String), escapeHttp(String)
* now: java.sql.Timestamp at startup

JSON
---------------
Nodes implement the following interfaces:

```java
package com.github.lindenb.jsvelocity.json;

import java.util.List;

public interface JSNode
	{
	public boolean isArray();
	public boolean isObject();
	public boolean isNumber();
	public boolean isString();
	public boolean isBoolean();
	public boolean isNull();
	public boolean isTrue();
	public boolean isFalse();
	public boolean isDecimal();
	public boolean isInteger();
	public boolean isComplex();
	public boolean isBigDecimal();
	public boolean isBigInteger();
	public abstract Object getValue();
	public String getId();
	public JSNode findById(String s);
	public JSNode getParentNode();
	public String getPath();
	public JSNode getRoot();
	}

public interface JSArray
	extends JSNode,List<JSNode>
	{

	}


public interface JSNull
	extends JSNode
	{

	}

public interface JSObject
	extends JSNode,Map<String, JSNode>
	{

	}

public interface JSPrimitive
	extends JSNode
	{
	}

```
numbers are stored using `java.math.BigInteger` or `java.math.BigDecimal`



Example:
--------
A JSON file 'test.json'
```json
{
individuals:[
	{
	name: "Riri",
	age: 8,
	duck: true
	},
	{
	name: "Fifi",
	age: 9,
	duck: true
	},
	{
	name: "Loulou",
	age: 10,
	duck: true
	}

	]

}

```
A Velocity template file:
```html
<html>
<body>
#foreach($indi in ${all.individuals})
<h1>${indi['name']}</h1>
Age:${indi.age}<br/>${indi.duck}
#end
<h3>String Constructors</h3>
<ul>
#foreach($S in ${STRING.constructors})
<li>${S}</li>
#end
</ul>
<p>${tool.escapeXml("<>&")}</p>
${out.open("/tmp/file.txt")}
Hello
${out.close()}
</body>
</html>
```

apply the template:
```bash
$ java -jar dist/jsvelocity.jar \
	-f all test.json \
	-C STRING java.lang.String \
	test.vm

<html>
<body>
<h1>Riri</h1>
Age:8<br/>true
<h1>Fifi</h1>
Age:9<br/>true
<h1>Loulou</h1>
Age:10<br/>true
<h3>String Constructors</h3>
<ul>
<li>public java.lang.String(byte[])</li>
<li>public java.lang.String(byte[],int,int)</li>
<li>public java.lang.String(byte[],java.nio.charset.Charset)</li>
<li>public java.lang.String(byte[],java.lang.String) throws java.io.UnsupportedEncodingException</li>
<li>public java.lang.String(byte[],int,int,java.nio.charset.Charset)</li>
<li>public java.lang.String(java.lang.StringBuilder)</li>
<li>public java.lang.String(java.lang.StringBuffer)</li>
<li>public java.lang.String(int[],int,int)</li>
<li>public java.lang.String(char[],int,int)</li>
<li>public java.lang.String(char[])</li>
<li>public java.lang.String(java.lang.String)</li>
<li>public java.lang.String()</li>
<li>public java.lang.String(byte[],int,int,java.lang.String) throws java.io.UnsupportedEncodingException</li>
<li>public java.lang.String(byte[],int)</li>
<li>public java.lang.String(byte[],int,int,int)</li>
</ul>
<p>&lt;&gt;&amp;</p>
</body>
</html>


$ more /tmp/file.txt 
Hello


```


webjsvelocity
=============
Web version of jsvelocity based on <b>Jetty</b>


Compilation
-----------

```bash
$ ant webjsvelocity
```


Options
-------
* -s (key) (string) add this string into the context.
* -e (key) (json-expr) add this json into the context.
* -f (key) (json-file) add this json into the context.
* -F (key) (json-file) add this json into the context. Dynamic Loading: the JSON file is reloaded for each request
* -i (key) and read stdin-json to the context.
* -C (key) (class.qualified.Name) add this Class into the context.
* -c (key) (class.qualified.Name) add an instance of Class into the context.
* -P (port) listen port.

Default objects
---------------
* out : the http writer (default is stdout).
* tool : utility containing the following method: escapeC(String), escapeXml(String), escapeHttp(String)
* now: java.sql.Timestamp of the request
* request: jetty current httpRequest
* response: jetty current httpResponse
* baseRequest :  current jetty Request


Example:
--------

A test with :

* JSON Data: https://github.com/lindenb/jsvelocity/blob/master/src/test/resources/json/lims.json 
* Velocity Macros: https://github.com/lindenb/jsvelocity/blob/master/src/test/resources/velocity/lims.vm

```bash
java -jar dist/webjsvelocity.jar  \
	-F lims src/test/resources/json/lims.json \
	src/test/resources/velocity/lims.vm
	
2013-10-17 12:43:35.566:INFO:oejs.Server:main: jetty-9.1.0.M0
2013-10-17 12:43:35.602:INFO:oejs.ServerConnector:main: Started ServerConnector@72dcb6{HTTP/1.1}{0.0.0.0:8080}
(...)

```


<img src="http://i.imgur.com/Yx5yakC.jpg"/>



