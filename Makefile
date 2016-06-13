SHELL=/bin/bash
this.makefile=$(lastword $(MAKEFILE_LIST))
this.dir=$(dir $(realpath ${this.makefile}))

#need local settings ? create a file 'local.mk' in this directory
ifneq ($(realpath local.mk),)
include $(realpath local.mk)
endif


src.dir=${this.dir}src/main/java
generated.dir=${this.dir}src/main/generated-sources
tmp.dir=${this.dir}_tmp
tmp.mft=${tmp.dir}/META-INF/MANIFEST.MF
export dist.dir?=${this.dir}dist
ANT?=ant
JAVAC?=javac
JAVA?=java
JAR?=jar


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

.PHONY: all jsvelocity test

all: test jsvelocity

test: jsvelocity
	${JAVA} -jar  ${dist.dir}/jsvelocity.jar -h
	${JAVA} -jar  ${dist.dir}/jsvelocity.jar tests/test001.vm
	${JAVA} -jar  ${dist.dir}/jsvelocity.jar -e str '"ATAatagtagta\"_"' tests/test002.vm
	${JAVA} -jar  ${dist.dir}/jsvelocity.jar -e value '1989128349182798723982792378912873' tests/test003.vm
	${JAVA} -jar  ${dist.dir}/jsvelocity.jar -e m '{"a":1,"b":null,"c":"abcd","d":[],"e":{},"d":true}' tests/test004.vm
	${JAVA} -jar  ${dist.dir}/jsvelocity.jar -e a '["a",1,"b",null,"c","abcd","d",[],"e",{},"d",true]' tests/test005.vm
	${JAVA} -jar  ${dist.dir}/jsvelocity.jar -e T 'true' -e F 'false' tests/test006.vm
	${JAVA} -jar  ${dist.dir}/jsvelocity.jar -e value '198912834918279872398.0E100' tests/test007.vm
	${JAVA} -jar  ${dist.dir}/jsvelocity.jar -e value 'null' tests/test008.vm
	
jsvelocity : ${dist.dir}/jsvelocity.jar

${dist.dir}/jsvelocity.jar: \
		${src.dir}/com/github/lindenb/jsvelocity/JSVelocity.java \
		${src.dir}/com/github/lindenb/jsvelocity/json/JSString.java \
		${src.dir}/com/github/lindenb/jsvelocity/json/JSNode.java \
		${src.dir}/com/github/lindenb/jsvelocity/json/JSDecimal.java \
		${src.dir}/com/github/lindenb/jsvelocity/json/JSArray.java \
		${src.dir}/com/github/lindenb/jsvelocity/json/JSInteger.java \
		${src.dir}/com/github/lindenb/jsvelocity/json/JSNull.java \
		${src.dir}/com/github/lindenb/jsvelocity/json/JSUtils.java \
		${src.dir}/com/github/lindenb/jsvelocity/json/JSMap.java \
		${src.dir}/com/github/lindenb/jsvelocity/json/JSBoolean.java \
		${src.dir}/com/github/lindenb/jsvelocity/Tools.java \
 		${all_maven_jars}
	mkdir -p ${tmp.dir}/META-INF ${dist.dir}
	#expand
	$(foreach J,$(filter %.jar,$^),unzip -o ${J} -d ${tmp.dir};)
	#compile
	${JAVAC} -d ${tmp.dir} -g -classpath "$(subst $(SPACE),:,$(filter %.jar,$^))" -sourcepath ${src.dir} $(filter %.java,$^)
	#create META-INF/MANIFEST.MF
	echo "Manifest-Version: 1.0" > ${tmp.mft}
	echo "Main-Class: com.github.lindenb.jsvelocity.JSVelocity" >> ${tmp.mft}
	echo -n "Git-Hash: " >> ${tmp.mft}
	$(if $(realpath .git/refs/heads/master),cat $(realpath .git/refs/heads/master), echo "undefined")  >> ${tmp.mft}
	echo -n "Compile-Date: " >> ${tmp.mft}
	date +%Y-%m-%d:%H-%m-%S >> ${tmp.mft}
	#create lgo4j config file
	echo '<?xml version="1.0" encoding="UTF-8"?><Configuration status="WARN"><Appenders><Console name="Console" target="SYSTEM_OUT"><PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/></Console></Appenders><Loggers><Root level="error"><AppenderRef ref="Console"/></Root></Loggers></Configuration>' > ${tmp.dir}/log4j2.xml
	#create jar
	${JAR} cfm $@ ${tmp.mft}  -C ${tmp.dir} .
	#create bash executable
	echo '#!/bin/bash' > ${dist.dir}/jsvelocity
	echo '${JAVA} -Dfile.encoding=UTF8 -Xmx500m $(if ${http.proxy.host},-Dhtt.proxyHost=${http.proxy.host})  $(if ${http.proxy.port},-Dhtt.proxyPort=${http.proxy.port}) -cp "${dist.dir}/jsvelocity.jar" com.github.lindenb.jsvelocity.JSVelocity  $$*' >> ${dist.dir}/jsvelocity
	chmod  ugo+rx ${dist.dir}/jsvelocity
	#cleanup
	rm -rf ${tmp.dir}


${all_maven_jars}  : 
	mkdir -p $(dir $@) && wget -O "$@" "http://central.maven.org/maven2/$(patsubst ${lib.dir}/%,%,$@)"

