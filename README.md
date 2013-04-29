jsvelocity
==========

Processing json data with apache velocity  ( http://velocity.apache.org/ ).

Original post was http://plindenbaum.blogspot.fr/2011/11/processing-json-data-with-apache.html


Compile
-------

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

Example:
--------
A JSON file:
```json
$ cat src/test/resources/json/test.json 

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
</body>
</html>
```

apply the template:
```bash
$ java -jar dist/jsvelocity.jar \
	-f all test.json test.vm
<html>
<body>
<h1>Riri</h1>
Age:8<br/>true
<h1>Fifi</h1>
Age:9<br/>true
<h1>Loulou</h1>
Age:10<br/>true
</body>
</html>
```

