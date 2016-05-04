SHELL=/bin/bash
this.makefile=$(lastword $(MAKEFILE_LIST))
this.dir=$(dir $(realpath ${this.makefile}))
src.dir=${this.dir}src/main/java
generated.dir=${this.dir}src/main/generated-sources
tmp.dir=${this.dir}_tmp
tmp.mft=${tmp.dir}/META-INF/MANIFEST.MF
export dist.dir?=${this.dir}dist
ANT?=ant
JAVAC?=javac
JAVA?=java
JAR?=jar
XJC?=xjc


lib.dir?=lib

EMPTY :=
SPACE := $(EMPTY) $(EMPTY)

commons.codec.jars = \
	$(lib.dir)/commons-codec/commons-codec/1.10/commons-codec-1.10.jar

log4j.jars = \
	$(lib.dir)/org/apache/logging/log4j/log4j-core/2.5/log4j-core-2.5.jar \
	$(lib.dir)/org/apache/logging/log4j/log4j-api/2.5/log4j-api-2.5.jar

apache.velocity.jars  =  \
        $(lib.dir)/commons-collections/commons-collections/3.2.1/commons-collections-3.2.1.jar \
        $(lib.dir)/org/apache/velocity/velocity/1.7/velocity-1.7.jar \
        $(lib.dir)/commons-lang/commons-lang/2.4/commons-lang-2.4.jar

gson.jars  =  \
	$(lib.dir)/com/google/code/gson/gson/2.3.1/gson-2.3.1.jar


all_maven_jars = $(sort  ${log4j.jars} ${commons.codec.jars} ${apache.velocity.jars}  ${gson.jars})

define compile-cmd

## 1 : target name
## 2 : qualified main class name
## 3 : other deps

$(1)  : ${htsjdk.jars} \
		$(addsuffix .java,$(addprefix ${src.dir}/,$(subst .,/,$(2)))) \
		$(3) ${all_maven_jars}
	mkdir -p ${tmp.dir}/META-INF ${generated.dir}/java/$(dir $(subst .,/,$(2))) ${dist.dir}
	#compile
	${JAVAC} -d ${tmp.dir} -g -classpath "$$(subst $$(SPACE),:,$$(filter %.jar,$$^))" -sourcepath ${src.dir}:${generated.dir}/java $$(filter %.java,$$^)
	#create META-INF/MANIFEST.MF
	echo "Manifest-Version: 1.0" > ${tmp.mft}
	echo "Main-Class: $(2)" >> ${tmp.mft}
	echo "Class-Path: $$(realpath $$(filter %.jar,$$^)) ${dist.dir}/$(1).jar" | fold -w 71 | awk '{printf("%s%s\n",(NR==1?"": " "),$$$$0);}' >>  ${tmp.mft}
	echo -n "Git-Hash: " >> ${tmp.mft}
	$$(if $$(realpath .git/refs/heads/master),cat $$(realpath .git/refs/heads/master), echo "undefined")  >> ${tmp.mft} 
	echo -n "Compile-Date: " >> ${tmp.mft}
	date +%Y-%m-%d:%H-%m-%S >> ${tmp.mft}
	#create jar
	${JAR} cfm ${dist.dir}/$(1).jar ${tmp.mft}  -C ${tmp.dir} .
	#create bash executable
	echo '#!/bin/bash' > ${dist.dir}/$(1)
	echo '${JAVA} -Dfile.encoding=UTF8 -Xmx500m $(if ${http.proxy.host},-Dhtt.proxyHost=${http.proxy.host})  $(if ${http.proxy.port},-Dhtt.proxyPort=${http.proxy.por
	t}) -cp "$$(subst $$(SPACE),:,$$(realpath $$(filter %.jar,$$^))):${dist.dir}/$(1).jar" $(2) $$$$*' >> ${dist.dir}/$(1)
	chmod  ugo+rx ${dist.dir}/$(1)
	#cleanup
	rm -rf ${tmp.dir}

endef

.PHONY: all

all: jsvelocity

$(eval $(call compile-cmd,jsvelocity,com.github.lindenb.jsvelocity.JSVelocity,))

${all_maven_jars}  : 
	mkdir -p $(dir $@) && wget -O "$@" "http://central.maven.org/maven2/$(patsubst ${lib.dir}/%,%,$@)"

