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
ant
```

Usage
-----

```bash
java -jar jsvelocity.jar (options) template.vm
```

Options
-------
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

