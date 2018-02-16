jsvelocity
==========

A Macro Processor.

Process structured data (json, yaml, tables, csv...) with Apache Velocity  ( [http://velocity.apache.org/](http://velocity.apache.org/) ).

Original post was http://plindenbaum.blogspot.fr/2011/11/processing-json-data-with-apache.html


Compile
-------

The compile process requires GNU make, java oracle JDK 8.* , wget and an internet connection.

```bash
make
```

Usage
-----

```bash
java -jar jsvelocity.jar (options) template.vm
```



Options
-------

```
    -C, --class
      Add this java Class into the context. class is wrapped into a 
      org.apache.velocity.tools.generic.ClassTool 
      Default: []
    -D, --dir, --directory
      Output directory
    -e, --expr
      Add this JSON-Expression into the context..
      Default: []
    -gson, --gson
      Do not convert json object to java. Keep the com.google.gson.* objects
      Default: false
    -h, --help
      Show Help
    -c, --instance
      Add this java instance into the context..
      Default: []
    -f, --json
      Add this JSON-File into the context..
      Default: []
    -lenient, --lenient
      Use a lenient json parser
      Default: false
    -o, --output
      Output File. Default: standout
    -p, --property, --properties
      Add this Java property file into the context..
      Default: []
    -s, --string
      Add this String into the context..
      Default: []
    -T, --template
      inline Velocity Template expression
    -tsv, --tsv
      Read tab delimited table in file.
      Default: []
    -y, --yaml
      Add this YAML-File into the context..
      Default: []

```

Examples
--------

### Example

```
$ java -jar dist/jsvelocity.jar -T 'Hello'
```

>Hello


### Example

```
$ java -jar dist/jsvelocity.jar -s c World -T 'Hello $c !'
```

>Hello World !


### Example

```
$ java -jar dist/jsvelocity.jar -s c World -T 'Hello ${tool.before($c,"l")} !'
```

>Hello Wor !


### Example

```
$ java -jar dist/jsvelocity.jar -s c World -T 'Hello ${tool.after($c,"l")} !'
```

>Hello d !


### Example

```
$ java -jar dist/jsvelocity.jar -s c World -T 'Hello ${tool.md5($c)} !'
```

>Hello f5a7924e621e84c9280a9a27e1bcb7f6 !


### Example

```
$ java -jar dist/jsvelocity.jar -s c World -T 'Hello ${tool.sha1($c)} !'
```

>Hello 70c07ec18ef89c5309bbb0937f3a6342411e1fdd !


### Example

```
$ seq 1 10 |paste - - > tmp.tsv
java -jar dist/jsvelocity.jar -tsv c tmp.tsv -T 'matrix[0][1]= ${c[0][1]}'
```

>`matrix[0][1]= 2`


### Example

```
$ java -jar dist/jsvelocity.jar -C c java.lang.String -T '${c.getPackage().getName()}'
```

> java.lang


### Example

```
$ java -jar dist/jsvelocity.jar -e c '[1,2,3]' -T '${c.size()} c[0]=${c[0]}'
```

>3 c[0]=1


### Example

```
$ cat t.yaml 
```

```yaml
mylist:
- 'item 1'
- 'item 2'
```

```
$ java -jar dist/jsvelocity.jar -y c t.yaml -T '${c.mylist[1]}'
```

>item 2


Default objects
---------------
* `out` : the writer (default is stdout). An instance of java.io.Writer
* `tool` : instance of utilities. see the code [./src/main/java/com/github/lindenb/jsvelocity/Tools.java](./src/main/java/com/github/lindenb/jsvelocity/Tools.java)
* `now`: java.sql.Timestamp at startup

Tools
-----

Tools ( ./src/main/java/com/github/lindenb/jsvelocity/Tools.java ) implement the following methods (non exhaustive... ):

```java
public String capitalize(final Object o) ;
public boolean isBlank(final Object o) ;
public String escapeCsv(final Object o) ;
public String escapeHtml(final Object o);
public String escapeJava(final Object o) ;
public String escapeJson(final Object o) ;
public String escapeXml(final Object o) ;
public String md5(final Object o) ;
public String sha1(final Object o) ;
public String left(final Object o,int l);
public String right(final Object o,int l) ;
public String before(final Object o,Object subo) ;
public String after(final Object o,Object subo) ;
public Integer parseInt(final Object o);
public Double parseDouble(final Object o) ;
```

Custom Velocity Directives
--------------------------

###Divert Directive

```
"#divert("newfile.txt",false)Hello World#{end}
```

###Javascript Directive

```
#javascript(1,"A") print(args[1]); for(var i=8;i<11;i++) print(""+i);print(J);#{end}
```

 
###ReadFile Directive

- method 'table' :  read file as `List<List<String>>`
- method 'hashtable': first line is header. read the file as `List<Map<String,String>>` 
- method 'hash' : first line is header, primary key 'pkey' must be provided . read the file as `Map<String,<Map<String,String>>`
-  method 'list' :  read file as `<List<String>>` 
- method 'json' :  read file as JSON
- method 'yaml' :  read file as YAML
- method 'properties' :  read file as `java.util.Properties` files


```
#readfile("T","input.data")
#readfile("T","input.data","method:table;delim:comma;")
#readfile("T","input.data","method:hash;pkey:y;delim:comma;")
#readfile("T","input.data","method:hashtable;delim:comma;")
```

###Json Directive

```
#json("variableName")[1,2,3,4,{}]#{end}
```

Author
------

Pierre Lindenbaum PhD @yokofakun


History:
--------

* 2018-02-16 : moved to all new version 3
* 2014-07 Removed WebJSvelocity
* 2014-07  changed some signatures:
  * getPath/getNodePath
  * getValue/getNodeValue
  * getParent/getParentNode
